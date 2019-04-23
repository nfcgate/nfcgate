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
    private AppDatabase mDatabase;
    private SessionLog.SessionType mSessionType;
    private BlockingQueue<NfcComm> mQueue = new LinkedBlockingQueue<>();
    private long mSessionId = -1;

    // callback
    private SIDChangedListener mListener;

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
            mQueue.put(data);
        } catch (InterruptedException ignored) { }
    }

    public void reset() {
        log(new NfcComm(false, true, null));
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

                    // set session id if none is set or reset it on new initial data
                    if (mSessionId == -1 || data.isInitial())
                        setSessionId(mDatabase.sessionLogDao().insert(new SessionLog(new Date(), mSessionType)));

                    // do not log empty initials only used for reset
                    if (!data.isInitial() || data.getData() != null)
                        mDatabase.nfcCommEntryDao().insert(new NfcCommEntry(data, mSessionId));

                } catch (InterruptedException ignored) { }
            }
        }
    }
}
