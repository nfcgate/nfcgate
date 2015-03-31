package tud.seemuh.nfcgate.gui.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
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
import tud.seemuh.nfcgate.gui.LoggingDetailActivity;
import tud.seemuh.nfcgate.util.NfcSession;
import tud.seemuh.nfcgate.util.db.SessionLoggingContract;
import tud.seemuh.nfcgate.util.db.SessionLoggingDbHelper;

/**
 * Display the session log
 */
public class LoggingFragment extends Fragment{

    private static LoggingFragment mFragment;

    private ListView mListView;
    private ArrayAdapter<NfcSession> mListAdapter;

    // List of Session objects
    private List<NfcSession> mSessions = new ArrayList<NfcSession>();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_log, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshSessionList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logging_list, container, false);

        mListView = (ListView) v.findViewById(R.id.sessionList);

        mListAdapter = new ArrayAdapter<NfcSession>(v.getContext(), R.layout.fragment_logging_row);

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

        // We want to introduce our own icons to the Action bar => Set HasOptionsMenu to true
        this.setHasOptionsMenu(true);

        return v;
    }

    protected void onListItemClick(View v, int pos, long id) {
        // start a new activity here to display the details of the clicked list element
        NfcSession selectedSession = mListAdapter.getItem(pos);
        Intent intent = new Intent(getActivity(), LoggingDetailActivity.class);
        intent.putExtra("SessionID", selectedSession.getID());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshSessionList();
    }

    private void refreshSessionList() {
        // TODO This is a little hack-y
        mSessions.clear();
        mListAdapter.clear();
        new AsyncSessionLoader().execute();
    }
 /*
    @Override
    public void onResume() {

        super.onResume();
        getActivity().getSupportFragmentManager().popBackStack();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){

                    // handle back button

                    return true;

                }

                return false;
            }
        });
    } */

    protected boolean onLongListItemClick(View v, int pos, long id) {
        // TODO Create and show menu to delete, rename, ...
        return true;
    }

    public void onBackPressed() {
        this.onListItemClick(getView(),0,0);
    }

    public void addSession(NfcSession session) {
        mSessions.add(session);
    }

    public void updateSessionView() {
        mListAdapter.addAll(mSessions);
        mListAdapter.notifyDataSetChanged();
    }

    public static LoggingFragment getInstance() {

        if(mFragment == null) {
            mFragment = new LoggingFragment();
        }
        return mFragment;
    }

    private class AsyncSessionLoader extends AsyncTask<Void, Void, Cursor> {
        private final String TAG = "AsyncSessionLoader";

        private SQLiteDatabase mDB;

        @Override
        protected Cursor doInBackground(Void... voids) {
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
                long ID = c.getLong(c.getColumnIndexOrThrow(SessionLoggingContract.SessionMeta._ID));
                String name = c.getString(c.getColumnIndexOrThrow(SessionLoggingContract.SessionMeta.COLUMN_NAME_NAME));
                String date = c.getString(c.getColumnIndexOrThrow(SessionLoggingContract.SessionMeta.COLUMN_NAME_DATE));
                NfcSession session = new NfcSession(date, ID, name);

                // Add session object
                addSession(session);
            } while (c.moveToNext()); // Iterate until all elements of the cursor have been processed
            // Close the cursor, freeing the used memory
            updateSessionView();
            Log.d(TAG, "onPostExecute: Closing connection and finishing");
            c.close();
            mDB.close();
        }
    }
}
