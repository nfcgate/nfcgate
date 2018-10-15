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
        mListener.onSIDChanged(sid);
    }

    public void log(NfcComm data) {
        try {
            mQueue.put(data);
        } catch (InterruptedException ignored) { }
    }

    public void reset() {
        setSessionId(-1);
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
                        setSessionId(mDatabase.sessionLogDao().insert(new SessionLog(new Date(), mSessionType)));

                    mDatabase.nfcCommEntryDao().insert(new NfcCommEntry(data, mSessionId));
                } catch (InterruptedException ignored) { }
            }
        }
    }
}
