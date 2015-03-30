package tud.seemuh.nfcgate.util.sink;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.db.SessionLoggingContract;
import tud.seemuh.nfcgate.util.db.SessionLoggingDbHelper;

/**
 * Sink for Session logging
 */
public class SessionLoggingSink implements Sink {
    public String TAG = "SessionLoggingSink";

    private BlockingQueue<NfcComm> mQueue;
    private SQLiteDatabase mDB;
    private Context mContext;

    private long mSessionID;

    public SessionLoggingSink(Context ctx) throws SinkInitException {
        mContext = ctx;
    }

    @Override
    public void setQueue(BlockingQueue<NfcComm> readQueue) {
        mQueue = readQueue;
    }

    @Override
    public void run() {
        // Initialization phase
        SessionLoggingDbHelper helper = new SessionLoggingDbHelper(mContext);
        mDB = helper.getWritableDatabase();

        Log.i(TAG, "run: Starting");
        openNewSession();
        Log.i(TAG, "run: New Session created: " + mSessionID);

        int msgCount = 0;

        // Main loop
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Get a new message
                NfcComm msg = mQueue.take();
                msgCount += 1;

                if (msg.getType() == NfcComm.Type.AnticolBytes) {
                    // We are dealing with an Anticol message
                    addAnticolMessage(msg);

                } else if (msg.getType() == NfcComm.Type.NFCBytes) {
                    // We are dealing with regular NFC traffic.
                    if (msg.getSource() == NfcComm.Source.CARD) {
                        // Write out NFC data sent by card
                        addCardMessage(msg);
                    } else if (msg.getSource() == NfcComm.Source.HCE) {
                        // Write out NFC data sent by reader
                        addHCEMessage(msg);
                    } else {
                        Log.e(TAG, "run: Unhandled message source, doing nothing");
                    }
                } else {
                    Log.e(TAG, "run: Unhandled message type, doing nothing");
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "run: Interrupted, exiting.");
                break;
            }
        }

        // Main loop terminated, clean up
        if (msgCount > 0) {
            Log.i(TAG, "run: Closing Session");
            closeSession();
        } else {
            Log.i(TAG, "run: No messages processed, deleting session");
            deleteSession();
        }
        mDB.close();
        Log.i(TAG, "run: Stopping");
    }


    private void openNewSession() {
        // Prepare values object
        ContentValues values = new ContentValues();
        // We only set the FINISHED column to false, as all other columns are set correctly
        // by default
        values.put(SessionLoggingContract.SessionMeta.COLUMN_NAME_FINISHED,
                SessionLoggingContract.SessionMeta.VALUE_FINISHED_FALSE);

        // Insert new session object and return the ID
        mSessionID = mDB.insert(SessionLoggingContract.SessionMeta.TABLE_NAME,
                null,
                values);
    }


    private void closeSession() {
        // Prepare values
        ContentValues values = new ContentValues();
        values.put(SessionLoggingContract.SessionMeta.COLUMN_NAME_FINISHED,
                SessionLoggingContract.SessionMeta.VALUE_FINISHED_TRUE);

        // Selection String
        String selection = SessionLoggingContract.SessionMeta._ID + " LIKE ?";
        // Selection arg
        String[] valueArg = { String.valueOf(mSessionID) };

        // perform the update
        mDB.update(SessionLoggingContract.SessionMeta.TABLE_NAME,
                values,
                selection,
                valueArg);
    }

    private void deleteSession() {
        // Selection String
        String selection = SessionLoggingContract.SessionMeta._ID + " LIKE ?";
        // Selection Args
        String[] selectArgs = { String.valueOf(mSessionID) };

        // Execute the delete
        mDB.delete(SessionLoggingContract.SessionMeta.TABLE_NAME,
                selection,
                selectArgs);
    }

    private void addHCEMessage(NfcComm msg) {
        addNFCMessageCommon(msg, SessionLoggingContract.SessionEvent.VALUE_TYPE_HCE);
    }

    private void addCardMessage(NfcComm msg) {
        addNFCMessageCommon(msg, SessionLoggingContract.SessionEvent.VALUE_TYPE_CARD);
    }

    private void addNFCMessageCommon(NfcComm msg, int type) {
        // Init ContentValues object
        ContentValues values = new ContentValues();

        // Add values
        // Meta values
        values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_SESSION_ID, mSessionID);
        values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_TYPE, type);
        // Content values
        values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_NFCDATA, msg.getData());
        if (msg.isChanged()) {
            values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_NFCDATA_PREFILTER, msg.getOldData());
        }

        // Commit to database
        mDB.insert(SessionLoggingContract.SessionEvent.TABLE_NAME,
                null,
                values);
    }

    private void addAnticolMessage(NfcComm msg) {
        // Init ContentValues object
        ContentValues values = new ContentValues();

        // Add values
        // Meta values
        values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_SESSION_ID, mSessionID);
        values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_TYPE, SessionLoggingContract.SessionEvent.VALUE_TYPE_ANTICOL);
        // Content Values
        values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_UID, msg.getUid());
        values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_ATQA, msg.getAtqa());
        values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_SAK, msg.getSak());
        values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_HIST, msg.getHist());
        if (msg.isChanged()) {
            values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_UID_PREFILTER, msg.getOldUid());
            values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_ATQA_PREFILTER, msg.getOldAtqa());
            values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_SAK_PREFILTER, msg.getOldSak());
            values.put(SessionLoggingContract.SessionEvent.COLUMN_NAME_HIST_PREFILTER, msg.getOldHist());
        }

        // Commit to database
        mDB.insert(SessionLoggingContract.SessionEvent.TABLE_NAME,
                null,
                values);
    }
}