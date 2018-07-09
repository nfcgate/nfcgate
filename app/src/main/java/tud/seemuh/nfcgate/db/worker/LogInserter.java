package tud.seemuh.nfcgate.db.worker;

import android.content.Context;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import tud.seemuh.nfcgate.db.AppDatabase;
import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.db.SessionLog;
import tud.seemuh.nfcgate.util.NfcComm;

public class LogInserter {
    private AppDatabase mDatabase;
    private BlockingQueue<NfcComm> mQueue = new LinkedBlockingQueue<>();

    public LogInserter(Context ctx) {
        mDatabase = AppDatabase.getDatabase(ctx);
        new LogInserterThread().start();
    }

    public void log(NfcComm data) {
        try {
            mQueue.put(data);
        } catch (InterruptedException ignored) { }
    }

    class LogInserterThread extends Thread {
        private long mSessionId = -1;

        LogInserterThread() {
            // ensure JVM stops this thread at the end of app
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    NfcComm data = mQueue.take();

                    if (mSessionId == -1)
                        mSessionId = mDatabase.sessionLogDao().insert(new SessionLog(new Date()));

                    mDatabase.nfcCommEntryDao().insert(new NfcCommEntry(data, mSessionId));
                } catch (InterruptedException ignored) { }
            }
        }
    }
}
