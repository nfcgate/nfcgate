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
import tud.seemuh.nfcgate.util.db.SessionLoggingContract;
import tud.seemuh.nfcgate.util.db.SessionLoggingDbHelper;

/**
 * Fragment that contains the list of events in a specific session
 */
public class LoggingDetailFragment extends Fragment {
    private ListView mListView;
    private ArrayAdapter<NfcComm> mListAdapter;

    private long mSessionID;

    private List<NfcComm> mEventList = new ArrayList<NfcComm>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logging_detail, container, false);

        mListView = (ListView) v.findViewById(R.id.sessionDetailList);

        mListAdapter = new ArrayAdapter<NfcComm>(v.getContext(), R.layout.fragment_logging_row);

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

        Bundle bundle = getArguments();
        if (bundle != null) {
            mSessionID = bundle.getLong("SessionID");
        }

        // Notify System that we would like to add an options menu.
        this.setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshEventList();
    }

    private void refreshEventList() {
        // TODO This is a little hack-y
        mEventList.clear();
        new AsyncDetailLoader().execute(mSessionID);
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
            case R.id.action_edit:
                // TODO
                return true;
            case R.id.action_delete:
                // TODO
                return true;
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
        mListAdapter.clear();
        mListAdapter.addAll(mEventList);
        mListAdapter.notifyDataSetChanged();
    }

    private class AsyncDetailLoader extends AsyncTask<Long, Void, Cursor> {
        private final String TAG = "AsyncDetailLoader";

        private SQLiteDatabase mDB;

        @Override
        protected Cursor doInBackground(Long... SessionID) {
            Log.d(TAG, "doInBackground: Started");
            // Get a DB object
            SessionLoggingDbHelper dbHelper = new SessionLoggingDbHelper(getActivity());
            mDB = dbHelper.getReadableDatabase();

            // Construct query
            // Define Projection
            String[] projection = {
                    SessionLoggingContract.SessionEvent._ID,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_SESSION_ID,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_DATE,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_TYPE,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_NFCDATA,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_UID,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_ATQA,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_SAK,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_HIST,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_NFCDATA_PREFILTER,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_UID_PREFILTER,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_ATQA_PREFILTER,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_SAK_PREFILTER,
                    SessionLoggingContract.SessionEvent.COLUMN_NAME_HIST_PREFILTER,
            };
            // Define Sort order
            String sortorder = SessionLoggingContract.SessionEvent._ID + " ASC";
            // Define Selection
            String selection = SessionLoggingContract.SessionEvent.COLUMN_NAME_SESSION_ID + " LIKE ?";
            // Define Selection Arguments
            Log.i(TAG, "doInBackground: SessionID = " + SessionID[0]);
            String[] selectionArgs = { String.valueOf(SessionID[0]) };

            // Perform query
            Log.d(TAG, "doInBackground: Performing query");
            Cursor c = mDB.query(
                    SessionLoggingContract.SessionEvent.TABLE_NAME,  // Target Table
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
                // prepare NfcComm object
                NfcComm comm;
                String date = c.getString(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_DATE));
                int type    = c.getInt(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_TYPE));
                if (type == SessionLoggingContract.SessionEvent.VALUE_TYPE_ANTICOL) {
                    // Anticol
                    byte[] uid     = c.getBlob(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_UID));
                    byte[] atqa    = c.getBlob(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_ATQA));
                    byte[] sak     = c.getBlob(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_SAK));
                    byte[] hist    = c.getBlob(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_HIST));
                    byte[] uid_pf  = c.getBlob(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_UID_PREFILTER));
                    byte[] atqa_pf = c.getBlob(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_ATQA_PREFILTER));
                    byte[] sak_pf  = c.getBlob(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_SAK_PREFILTER));
                    byte[] hist_pf = c.getBlob(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_HIST_PREFILTER));
                    if (uid_pf != null) {
                        // Saving prefilter data is all-or-nothing: If one value is changed, all are saved
                        // We now need to properly initialize the NfcComm object.
                        comm = new NfcComm(atqa, sak[0], hist, uid, atqa_pf, sak_pf[0], hist_pf, uid_pf);
                    } else {
                        comm = new NfcComm(atqa, sak[0], hist, uid);
                    }

                } else {
                    // NFC Data
                    byte[] bytes    = c.getBlob(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_NFCDATA));
                    byte[] bytes_pf = c.getBlob(c.getColumnIndexOrThrow(SessionLoggingContract.SessionEvent.COLUMN_NAME_NFCDATA_PREFILTER));
                    NfcComm.Source source;
                    if (type == SessionLoggingContract.SessionEvent.VALUE_TYPE_CARD) {
                        source = NfcComm.Source.CARD;
                    } else {
                        source = NfcComm.Source.HCE;
                    }
                    if (bytes_pf != null) {
                        comm = new NfcComm(source, bytes, bytes_pf);
                    } else {
                        comm = new NfcComm(source, bytes);
                    }
                }
                addNfcEvent(comm);
            } while (c.moveToNext()); // Iterate until all elements of the cursor have been processed
            // Close the cursor, freeing the used memory
            updateSessionView();
            Log.d(TAG, "onPostExecute: Closing connection and finishing");
            c.close();
            mDB.close();
        }
    }
}
