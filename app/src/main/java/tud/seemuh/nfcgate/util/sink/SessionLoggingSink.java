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

        long sessionID = openNewSession();

        // Main loop
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Get a new message
                NfcComm msg = mQueue.take();

                if (msg.getType() == NfcComm.Type.AnticolBytes) {
                    // We are dealing with an Anticol message
                    if (msg.isChanged()) {
                        // TODO
                    } else {
                        // TODO
                    }


                } else if (msg.getType() == NfcComm.Type.NFCBytes) {
                    // We are dealing with regular NFC traffic.
                    if (msg.getSource() == NfcComm.Source.CARD) {
                        // Write out NFC data sent by card
                        if (msg.isChanged()) {
                            // TODO
                        } else {
                            // TODO
                        }
                    } else if (msg.getSource() == NfcComm.Source.HCE) {
                        // Write out NFC data sent by reader
                        if (msg.isChanged()) {
                            // TODO
                        } else {
                            // TODO
                        }
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
        closeSession(sessionID);
        mDB.close();
    }

    private long openNewSession() {
        // Prepare values object
        ContentValues values = new ContentValues();
        // We only set the FINISHED column to false, as all other columns are set correctly
        // by default
        values.put(SessionLoggingContract.SessionMeta.COLUMN_NAME_FINISHED,
                SessionLoggingContract.SessionMeta.VALUE_FINISHED_FALSE);

        // Insert new session object and return the ID
        return mDB.insert(SessionLoggingContract.SessionMeta.TABLE_NAME,
                null,
                values);
    }

    private void closeSession(long sessionid) {
        // Prepare values
        ContentValues values = new ContentValues();
        values.put(SessionLoggingContract.SessionMeta.COLUMN_NAME_FINISHED,
                SessionLoggingContract.SessionMeta.VALUE_FINISHED_TRUE);

        // Selection String
        String selection = SessionLoggingContract.SessionMeta._ID + " LIKE ?";
        // Selection arg
        String[] valueArg = { String.valueOf(sessionid) };

        // perform the update
        mDB.update(SessionLoggingContract.SessionMeta.TABLE_NAME,
                values,
                selection,
                valueArg);
    }
}