package tud.seemuh.nfcgate.util.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * An AsyncTask to make sure the database has been created and updated when we first try to use
 * it. This is ensured by running this task in the onCreate-Method of the MainActivity.
 * If the database is already up to date, this will be side-effect free. Otherwise, the database
 * scheme will be created or updated, as needed.
 */
public class DbInitTask extends AsyncTask<Context, Void, Void> {
    private final String TAG = "DbInitTask";

    @Override
    protected Void doInBackground(Context... ctx) {
        if (ctx.length == 0) return null;
        SessionLoggingDbHelper dbh = new SessionLoggingDbHelper(ctx[0]);
        Log.d(TAG, "doInBackground: Init DB - started");
        dbh.getWritableDatabase();
        // TODO Verify and fix contents of database (inconsistent session etc)
        Log.d(TAG, "doInBackground: Init DB - done");
        return null;
    }
}
