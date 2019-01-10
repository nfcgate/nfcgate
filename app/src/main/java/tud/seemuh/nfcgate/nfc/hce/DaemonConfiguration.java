package tud.seemuh.nfcgate.nfc.hce;

import android.content.Context;
import android.content.Intent;

/**
 * Interface to the nfc daemon patches
 */
public class DaemonConfiguration {
    private Context mCtx;

    public DaemonConfiguration(Context ctx) {
        mCtx = ctx;
    }

    /**
     * Sets the config in the NFC Service hook
     *
     * @param config A config stream enables the hook, null disables it
     */
    public void setConfig(byte[] config) {
        send(getIntent("SET_CONFIG").putExtra("config", config));
    }

    /**
     * Sets the polling state
     *
     * @param enabled True enables polling, false disables it
     */
    public void setPolling(boolean enabled) {
        send(getIntent("SET_POLLING").putExtra("enabled", enabled));
    }

    /**
     * Enables or disables on-device capture
     *
     * @param enabled True enables on-device capture, false disables it
     */
    public void setCapture(boolean enabled) {
        send(getIntent("SET_CAPTURE").putExtra("enabled", enabled));
    }

    private Intent getIntent(String op) {
        return new Intent()
                .setAction("tud.seemuh.nfcgate.daemoncall")
                .putExtra("op", op);
    }

    private void send(Intent intent) {
        mCtx.sendBroadcast(intent);
    }
}
