package tud.seemuh.nfcgate.gui.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
public class LoggingFragment extends Fragment implements DialogInterface.OnClickListener {
    private static final String TAG = "LoggingFragment";

    private static LoggingFragment mFragment;

    private ListView mListView;
    private ArrayAdapter<NfcSession> mListAdapter;
    private TextView mNotifyTextView;

    private NfcSession mActionSession;

    // List of Session objects
    private ArrayList<NfcSession> mSessions = new ArrayList<NfcSession>();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate options menu
        menuInflater.inflate(R.menu.menu_log, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // Refresh button clicked
                refreshSessionList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout
        View v = inflater.inflate(R.layout.fragment_logging_list, container, false);

        // Get our ListView
        mListView = (ListView) v.findViewById(R.id.sessionList);
        // and our TextView
        mNotifyTextView = (TextView) v.findViewById(R.id.loggingFragmentNotifyTextView);

        // Create an ArrayAdapter to display our NFC Sessions
        mListAdapter = new ArrayAdapter<NfcSession>(v.getContext(), R.layout.fragment_logging_row);

        // Attach the adapter
        mListView.setAdapter(mListAdapter);

        // Set up onClick-Listeners
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

    @Override
    public void onResume() {
        super.onResume();
        // Load values from the database
        // refreshSessionList();
        new AsyncSessionLoader().execute();
    }

    protected void onListItemClick(View v, int pos, long id) {
        // start a new activity here to display the details of the clicked list element
        NfcSession selectedSession = mListAdapter.getItem(pos);
        Intent intent = new Intent(getActivity(), LoggingDetailActivity.class);
        // Provide new activity with the SessionID via the Extras Bundle
        intent.putExtra("SessionID", selectedSession.getID());
        startActivity(intent);
    }

    private void refreshSessionList() {
        // TODO This is a little hack-y
        // Clear existing session data
        mNotifyTextView.setText("");
        mNotifyTextView.setVisibility(TextView.GONE);
        mNotifyTextView.invalidate();
        mSessions.clear();
        // Clear display
        mListAdapter.clear();
        // Reload Fragment, because Android is retarded.
        FragmentTransaction tr = getFragmentManager().beginTransaction();
        tr.detach(this);
        tr.attach(this);
        tr.commit();
    }

    // Helper function to notify the GUI thread if no sessions exist
    private void notifyNoSessions() {
        mNotifyTextView.setText(getText(R.string.logging_no_sessions_found));
        mNotifyTextView.setVisibility(View.VISIBLE);
    }

    protected boolean onLongListItemClick(View v, int pos, long id) {
        // Get the long-clicked Session object
        mActionSession = mListAdapter.getItem(pos);
        // Show the long-press menu
        getLongPressMenu().show();
        return true;
    }

    public void onBackPressed() {
        this.onListItemClick(getView(),0,0);
    }

    public void addSession(NfcSession session) {
        mSessions.add(session);
    }

    public void updateSessionView() {
        // Add all items to the ArrayAdapter
        mListAdapter.addAll(mSessions);
        // Notify the ArrayAdapter that the data has changed
        mListAdapter.notifyDataSetChanged();
    }

    public static LoggingFragment getInstance() {
        if(mFragment == null) {
            mFragment = new LoggingFragment();
        }
        return mFragment;
    }

    private AlertDialog getDeleteConfirmationDialog() {
        // Create an AlertDialog to confirm the deletion of a session
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.deletion_dialog_text))
                .setPositiveButton(getString(R.string.deletion_dialog_confirm), this)
                .setNegativeButton(getString(R.string.deletion_dialog_cancel), this)
                .setTitle(getString(R.string.title_dialog_delete));
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private AlertDialog getLongPressMenu() {
        // Create an AlertDialog to display the long-press menu
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.array_log_menu, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) { // Delete dialog - delete confirmed
            new AsyncSessionDeleter().execute(mActionSession.getID());
        } else if (which == 0) { // List-Interface - Rename
            getRenameSessionDialog().show();
        } else if (which == 1) { // List-Interface - Delete
            getDeleteConfirmationDialog().show();
        }
    }

    private void renameSession(String name) {
        mActionSession.setName(name);
        new AsyncSessionRenamer().execute(mActionSession);
    }

    private AlertDialog getRenameSessionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set up text
        builder.setTitle(getText(R.string.title_dialog_rename));
        builder.setMessage(getText(R.string.rename_dialog_text));

        // Add input value
        final EditText input = new EditText(getActivity());
        if (mActionSession.getName() != null) {
            input.setText(mActionSession.getName());
        }
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.rename_dialog_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                renameSession(input.getText().toString());
            }
        });

        builder.setNegativeButton(getString(R.string.rename_dialog_cancel), this);

        return builder.create();
    }

    protected void confirmSessionDelete() {
        // Session has been deleted. Notify the user and refresh session list
        Toast.makeText(getActivity(), getString(R.string.deletion_done), Toast.LENGTH_SHORT).show();
        refreshSessionList();
    }

    protected void confirmSessionRename() {
        Toast.makeText(getActivity(), getString(R.string.rename_done), Toast.LENGTH_SHORT).show();
        refreshSessionList();
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
                notifyNoSessions();
                return;
            }
            do {
                // prepare session object
                long ID = c.getLong(c.getColumnIndexOrThrow(SessionLoggingContract.SessionMeta._ID));
                Log.d(TAG, "onPostExecute: Processing Session " + ID);
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

    private class AsyncSessionDeleter extends AsyncTask<Long, Void, Void> {
        private final String TAG = "AsyncSessionDeleter";

        private SQLiteDatabase mDB;

        @Override
        protected Void doInBackground(Long... longs) {
            Log.d(TAG, "doInBackground: Started");
            // Get a DB object
            SessionLoggingDbHelper dbHelper = new SessionLoggingDbHelper(getActivity());
            mDB = dbHelper.getWritableDatabase();

            // Construct query
            // Define Selection
            String selection = SessionLoggingContract.SessionMeta._ID + " LIKE ?";
            // Define Selection Arguments
            String[] selectionArgs = { String.valueOf(longs[0]) };

            // Perform query
            Log.d(TAG, "doInBackground: Performing deletion");
            mDB.delete(
                    SessionLoggingContract.SessionMeta.TABLE_NAME,
                    selection,
                    selectionArgs
            );

            Log.d(TAG, "doInBackground: Deletion done, returning");
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mDB.close();
            confirmSessionDelete();
        }
    }

    private class AsyncSessionRenamer extends AsyncTask<NfcSession, Void, Void> {
        private final String TAG = "AsyncSessionRenamer";

        private SQLiteDatabase mDB;

        @Override
        protected Void doInBackground(NfcSession... nfc) {
            Log.d(TAG, "doInBackground: Started");
            // Get a DB object
            SessionLoggingDbHelper dbHelper = new SessionLoggingDbHelper(getActivity());
            mDB = dbHelper.getWritableDatabase();

            // Construct query
            // Content values
            ContentValues values = new ContentValues();
            values.put(SessionLoggingContract.SessionMeta.COLUMN_NAME_NAME, nfc[0].getName());
            // Define Selection
            String selection = SessionLoggingContract.SessionMeta._ID + " LIKE ?";
            // Define Selection Arguments
            String[] selectionArgs = { String.valueOf(nfc[0].getID()) };

            // Perform query
            Log.d(TAG, "doInBackground: Performing update");
            mDB.update(
                    SessionLoggingContract.SessionMeta.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );

            Log.d(TAG, "doInBackground: Update done, returning");
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mDB.close();
            confirmSessionRename();
        }
    }
}
