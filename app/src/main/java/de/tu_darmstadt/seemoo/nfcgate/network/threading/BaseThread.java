package de.tu_darmstadt.seemoo.nfcgate.network.threading;

import java.io.IOException;
import java.net.Socket;

import de.tu_darmstadt.seemoo.nfcgate.network.ServerConnection;

/**
 * An interruptible thread that properly handles interrupt()
 */
public abstract class BaseThread extends Thread {
    // set on interrupt
    private boolean mExit = false;

    final ServerConnection mConnection;
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
                runInternal();
            }
            catch (InterruptedException e) {
                // loop
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
    abstract void runInternal() throws IOException, InterruptedException;
    abstract void onError(Exception e);
}
