package tud.seemuh.nfcgate.network.threading;

import java.io.IOException;

/**
 * A interruptible thread that properly handles interrupt()
 */
public abstract class BaseThread extends Thread {
    // set on interrupt
    private boolean mExit = false;

    BaseThread() {
        // ensure JVM stops this thread at the end of app
        setDaemon(true);
    }

    @Override
    public void run() {
        // per-thread init
        try {
            initThread();
        } catch (IOException e) {
            mExit = true;
            onError();
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
                onError();
            }
        }
    }

    abstract void initThread() throws IOException;
    abstract void runInternal() throws IOException;
    abstract void onError();
}
