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
    private SessionLog.SessionType mSessionType;
    private BlockingQueue<NfcComm> mQueue = new LinkedBlockingQueue<>();
    private long mSessionId = -1;

    public LogInserter(Context ctx, SessionLog.SessionType sessionType) {
        mDatabase = AppDatabase.getDatabase(ctx);
        mSessionType = sessionType;
        new LogInserterThread().start();
    }

    public void log(NfcComm data) {
        try {
            mQueue.put(data);
        } catch (InterruptedException ignored) { }
    }

    public void reset() {
        mSessionId = -1;
    }

    class LogInserterThread extends Thread {
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
                        mSessionId = mDatabase.sessionLogDao().insert(new SessionLog(new Date(), mSessionType));

                    mDatabase.nfcCommEntryDao().insert(new NfcCommEntry(data, mSessionId));
                } catch (InterruptedException ignored) { }
            }
        }
    }
}
