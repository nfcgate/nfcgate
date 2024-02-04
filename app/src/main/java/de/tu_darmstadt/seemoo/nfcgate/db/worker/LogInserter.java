package de.tu_darmstadt.seemoo.nfcgate.db.worker;

import android.content.Context;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.tu_darmstadt.seemoo.nfcgate.db.AppDatabase;
import de.tu_darmstadt.seemoo.nfcgate.db.NfcCommEntry;
import de.tu_darmstadt.seemoo.nfcgate.db.SessionLog;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class LogInserter {
    public interface SIDChangedListener {
        void onSIDChanged(long sessionID);
    }

    // database
    private final AppDatabase mDatabase;
    private final SessionLog.SessionType mSessionType;
    private final BlockingQueue<LogEntry> mQueue = new LinkedBlockingQueue<>();
    private long mSessionId = -1;

    // callback
    private final SIDChangedListener mListener;

    public LogInserter(Context ctx, SessionLog.SessionType sessionType, SIDChangedListener listener) {
        mDatabase = AppDatabase.getDatabase(ctx);
        mSessionType = sessionType;
        mListener = listener;
        new LogInserterThread().start();
    }

    private void setSessionId(long sid) {
        mSessionId = sid;

        if (mListener != null)
            mListener.onSIDChanged(sid);
    }

    public void log(NfcComm data) {
        try {
            mQueue.put(new LogEntry(data));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public void reset() {
        try {
            mQueue.put(new LogEntry());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
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
                    LogEntry entry = mQueue.take();

                    // set session id if none is set or reset it on reset data
                    if (!entry.isValid())
                        setSessionId(-1);
                    else if (mSessionId == -1)
                        setSessionId(mDatabase.sessionLogDao().insert(new SessionLog(new Date(), mSessionType)));

                    if (entry.isValid())
                        mDatabase.nfcCommEntryDao().insert(new NfcCommEntry(entry.getData(), mSessionId));

                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
