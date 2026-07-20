package de.tu_darmstadt.seemoo.nfcgate.nfc.hce;

import android.content.Intent;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.gui.MainActivity;
import de.tu_darmstadt.seemoo.nfcgate.xposed.InjectionBroadcastWrapper;

/**
 * Interface to the nfc daemon patches
 */
public class DaemonManager {
    private final MainActivity mActivity;
    private boolean mIsHookEnabled = false;
    private Date mLastResponse = null;

    public DaemonManager(MainActivity activity) {
        mActivity = activity;
    }

    /**
     * Receives daemon response intents
     */
    public void onResponse(Intent intent) {
        String responseType = intent.getStringExtra("type");

        if ("HOOK_STATUS".equals(responseType)) {
            mIsHookEnabled = intent.getBooleanExtra("hookEnabled", false);
            mActivity.getNfc().notifyStatusChanged();
            mLastResponse = new Date();
        }
    }

    public boolean isHookEnabled() {
        return mIsHookEnabled;
    }

    /**
     * Sets the config in the NFC Service hook
     *
     * @param config A config stream, enables the hook
     */
    public void beginSetConfig(byte[] config) {
        send(getIntent("SET_CONFIG").putExtra("config", config));
    }

    /**
     * Resets the config and polling
     */
    public void beginResetConfig() {
        send(getIntent("RESET_CONFIG"));
    }

    /**
     * Sets the polling state
     *
     * @param enabled True enables polling, false disables it
     */
    public void beginSetPolling(boolean enabled) {
        send(getIntent("SET_POLLING").putExtra("enabled", enabled));
    }

    /**
     * Enables or disables on-device capture
     *
     * @param enabled True enables on-device capture, false disables it
     */
    public void beginSetCapture(boolean enabled) {
        if (enabled)
            send(getIntent("SET_CAPTURE").putExtra("enabled", true));
        else {
            try (LocalSocketThread thread = new LocalSocketThread()) {
                // start listening for capture data in thread
                thread.start();
                // disable capture, which triggers sending the capture data to the socket
                send(getIntent("SET_CAPTURE").putExtra("enabled", false));

                // wait for capture data to be received, with 5s timeout
                thread.join(5 * 1000);
            } catch (IOException | InterruptedException e) {
                Log.e("NFC", "Error handling capture socket", e);
            }
        }
    }

    /**
     * Lazily installs hooks if needed, receives the current hook status
     */
    public void beginInstallHooks() {
        send(getIntent("INSTALL_HOOKS"));
    }

    public void onResume() {
        // debounce getting hook status because receiving the response also triggers onResume
        if (mLastResponse == null || (new Date().getTime() - mLastResponse.getTime()) > 5000)
            beginInstallHooks();
    }

    private Intent getIntent(String op) {
        return new Intent()
                .setAction(InjectionBroadcastWrapper.NFCGATE_BROADCAST)
                .putExtra("op", op);
    }

    private void send(Intent intent) {
        mActivity.sendBroadcast(intent);
    }

    protected class LocalSocketThread extends Thread implements AutoCloseable {
        private final LocalServerSocket mServer;
        private boolean mIsClosing = false;

        public LocalSocketThread() throws IOException {
            mServer = new LocalServerSocket(InjectionBroadcastWrapper.CAPTURE_SOCKET_NAME);

            setDaemon(true);
        }

        @Override
        public void close() throws IOException {
            mIsClosing = true;
            mServer.close();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try (LocalSocket socket = mServer.accept()) {
                try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
                    List<byte[]> captureData = (List<byte[]>) ois.readObject();
                    mActivity.runOnUiThread(() -> mActivity.importCapture(captureData));

                    Log.i("NFC", "Received capture data: " + captureData.size() + " entries");
                } catch (ClassNotFoundException e) {
                    Log.e("NFC", "Error handling capture data", e);
                }
            } catch (IOException e) {
                if (mIsClosing)
                    Log.i("NFC", "Capture socket closed while waiting (timeout likely)");
                else
                    Log.e("NFC", "Error in capture socket", e);
            }
        }
    }
}
