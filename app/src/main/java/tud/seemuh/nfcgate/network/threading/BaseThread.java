package tud.seemuh.nfcgate.network.threading;

import java.io.IOException;
import java.net.Socket;

import tud.seemuh.nfcgate.network.ServerConnection;

/**
 * A interruptible thread that properly handles interrupt()
 */
public abstract class BaseThread extends Thread {
    // set on interrupt
    private boolean mExit = false;

    ServerConnection mConnection;
    Socket mSocket;

    BaseThread(ServerConnection connection) {
        mConnection = connection;

        // ensure JVM stops this thread at the end of app
        setDaemon(true);
    }

    @Override
    public void run() {
        // per-thread init
        try {
            mSocket = mConnection.openSocket();
            if (mSocket == null)
                throw new IOException("Socket error");

            initThread();
        } catch (IOException e) {
            mExit = true;
            onError(e);
        }

        while (!mExit && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(10);
                runInternal();
            }
            catch (InterruptedException e) {
                // This flag is important as sleep() resets the interrupted flag
                mExit = true;
            }
            catch (IOException e) {
                mExit = true;
                onError(e);
            }
        }

        // close socket
        mConnection.closeSocket();
    }

    abstract void initThread() throws IOException;
    abstract void runInternal() throws IOException;
    abstract void onError(Exception e);
}
