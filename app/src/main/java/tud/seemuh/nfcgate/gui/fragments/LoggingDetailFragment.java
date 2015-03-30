package tud.seemuh.nfcgate.gui.fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.NfcSession;
import tud.seemuh.nfcgate.util.db.SessionLoggingContract;
import tud.seemuh.nfcgate.util.db.SessionLoggingDbHelper;

/**
 * Fragment that contains the list of events in a specific session
 */
public class LoggingDetailFragment extends Fragment {
    private ListView mListView;
    private ArrayAdapter<NfcComm> mListAdapter;

    private List<NfcComm> mEventList = new ArrayList<NfcComm>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logging_detail, container, false);

        mListView = (ListView) v.findViewById(R.id.sessionDetailList);

        mListAdapter = new ArrayAdapter<NfcComm>(v.getContext(), R.layout.fragment_logging_detail);

        mListView.setAdapter(mListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {
                onListItemClick(v, pos, id);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                return onLongListItemClick(v, pos, id);
            }
        });


        // Notify System that we would like to add an options menu.
        this.setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_logging_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            // TODO
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected boolean onLongListItemClick(View v, int pos, long id) {
        // TODO Create and show menu to delete, rename, ...
        return true;
    }

    protected void onListItemClick(View v, int pos, long id) {
        // start a new activity here to display the details of the clicked list element
        // TODO
    }

    protected void addNfcEvent(NfcComm nfccomm) {
        mEventList.add(nfccomm);
    }

    protected void updateSessionView() {
        mListAdapter.addAll(mEventList);
        mListAdapter.notifyDataSetChanged();
    }

    private class AsyncDetailLoader extends AsyncTask<Void, Void, Cursor> {
        private final String TAG = "AsyncDetailLoader";

        private SQLiteDatabase mDB;

        @Override
        protected Cursor doInBackground(Void... voids) {
            // TODO Update
            Log.d(TAG, "doInBackground: Started");
            // Get a DB object
            SessionLoggingDbHelper dbHelper = new SessionLoggingDbHelper(getActivity());
            mDB = dbHelper.getReadableDatabase();

            // Construct query
            // Define Projection
            String[] projection = {
                    SessionLoggingContract.SessionMeta._ID,
                    SessionLoggingContract.SessionMeta.COLUMN_NAME_NAME,
                    SessionLoggingContract.SessionMeta.COLUMN_NAME_DATE,
            };
            // Define Sort order
            String sortorder = SessionLoggingContract.SessionMeta.COLUMN_NAME_DATE + " DESC";
            // Define Selection
            String selection = SessionLoggingContract.SessionMeta.COLUMN_NAME_FINISHED + " LIKE ?";
            // Define Selection Arguments
            String[] selectionArgs = { String.valueOf(SessionLoggingContract.SessionMeta.VALUE_FINISHED_TRUE) };

            // Perform query
            Log.d(TAG, "doInBackground: Performing query");
            Cursor c = mDB.query(
                    SessionLoggingContract.SessionMeta.TABLE_NAME,  // Target Table
                    projection,    // Which fields are we interested in?
                    selection,     // Selection clause
                    selectionArgs, // Arguments to clause
                    null,          // Grouping (not desired in this case)
                    null,          // Filtering (not desired in this case)
                    sortorder      // Sort order
            );

            Log.d(TAG, "doInBackground: Query done, returning");
            return c;
        }

        @Override
        protected void onPostExecute(Cursor c) {
            // Move to the first element of the cursor
            Log.d(TAG, "onPostExecute: Beginning processing of Sessions");
            if (!c.moveToFirst()) {
                Log.i(TAG, "onPostExecute: Cursor empty, doing nothing.");
                // TODO Signal GUI that the cursor is empty
                return;
            }
            do {
                // prepare session object
                // TODO Do stuff
            } while (c.moveToNext()); // Iterate until all elements of the cursor have been processed
            // Close the cursor, freeing the used memory
            updateSessionView();
            Log.d(TAG, "onPostExecute: Closing connection and finishing");
            c.close();
            mDB.close();
        }
    }
}
