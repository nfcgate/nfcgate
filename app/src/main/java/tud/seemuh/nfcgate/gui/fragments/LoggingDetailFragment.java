package tud.seemuh.nfcgate.gui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
public class LoggingDetailFragment extends Fragment implements DialogInterface.OnClickListener {
    private ListView mListView;
    private ArrayAdapter<NfcComm> mListAdapter;

    private TextView mSessionTitle;
    private TextView mSessionDate;

    private long mSessionID;
    private NfcSession mSession;

    private List<NfcComm> mEventList = new ArrayList<NfcComm>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View v = inflater.inflate(R.layout.fragment_logging_detail, container, false);

        // Get references to all used Views
        mListView = (ListView) v.findViewById(R.id.sessionDetailList);
        mSessionTitle = (TextView) v.findViewById(R.id.loggingDetailsTitleTextView);
        mSessionDate = (TextView) v.findViewById(R.id.loggingDetailsDateTextView);

        // Create ArrayAdapter to display our NfcComm objects
        mListAdapter = new ArrayAdapter<NfcComm>(v.getContext(), R.layout.fragment_logging_row);

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

        // Get the parameters
        Bundle bundle = getArguments();
        if (bundle != null) {
            mSessionID = bundle.getLong("SessionID");
        } else {
            // If we were not provided with a Bundle, exit (something is wrong)
            getActivity().finish();
        }

        // Notify System that we would like to add an options menu.
        this.setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh information about the session...
        refreshSessionInfo();
        // ...and its events
        refreshEventList();
    }

    private void refreshEventList() {
        // TODO This is a little hack-y
        mEventList.clear();
        new AsyncDetailLoader().execute(mSessionID);
    }

    private void refreshSessionInfo() {
        new AsyncSessionLoader().execute(mSessionID);
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
                renameSession();
                return true;
            case R.id.action_delete:
                deleteSession();  // Delete button pressed
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected boolean onLongListItemClick(View v, int pos, long id) {
        // Long-Press events. Currently not used.
        return true;
    }

    protected void onListItemClick(View v, int pos, long id) {
        // start a new activity here to display the details of the clicked list element
        // Currently not used
    }

    protected void deleteSession() {
        getDeleteConfirmationDialog().show();
    }

    protected void renameSession() {
        getRenameSessionDialog().show();
    }

    private AlertDialog getDeleteConfirmationDialog() {
        // "Delete"-Confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.deletion_dialog_text))
                .setPositiveButton(getString(R.string.deletion_dialog_confirm), this)
                .setNegativeButton(R.string.deletion_dialog_cancel, this)
                .setTitle(getString(R.string.title_dialog_delete));
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private AlertDialog getRenameSessionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set up text
        builder.setTitle(getText(R.string.title_dialog_rename));
        builder.setMessage(getText(R.string.rename_dialog_text));

        // Add input value
        final EditText input = new EditText(getActivity());
        if (mSession.getName() != null) {
            input.setText(mSession.getName());
        }
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.rename_dialog_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doSessionRename(input.getText().toString());
            }
        });

        builder.setNegativeButton(getString(R.string.rename_dialog_cancel), this);

        return builder.create();
    }

    private void doSessionRename(String name) {
        mSession.setName(name);
        new AsyncSessionRenamer().execute(mSession);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            new AsyncSessionDeleter().execute(mSessionID);
        }
    }

    protected void confirmDelete() {
        // Confirm that the session has been deleted
        Toast.makeText(getActivity(), getString(R.string.deletion_done), Toast.LENGTH_SHORT).show();
        // Exit from the activity, as the underlying session has been deleted
        getActivity().finish();
    }

    protected void confirmSessionRename() {
        Toast.makeText(getActivity(), getString(R.string.rename_done), Toast.LENGTH_SHORT).show();
        refreshSessionInfo();
    }

    protected void addNfcEvent(NfcComm nfccomm) {
        mEventList.add(nfccomm);
    }

    protected void updateSessionView() {
        // Clear the display of the ArrayAdapter
        mListAdapter.clear();
        // Add new information
        mListAdapter.addAll(mEventList);
        // Notify that the contents have changed
        mListAdapter.notifyDataSetChanged();
    }

    protected void setSessionDetails(NfcSession sess) {
        // Set the session details (called from an AsyncTask)
        if (sess.getName() != null) {
            mSessionTitle.setText("Session: " + sess.getName());
        } else {
            mSessionTitle.setText("Session " + sess.getID());
        }
        mSessionDate.setText("Recorded: " + sess.getDate());
        mSession = sess;
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

    private class AsyncSessionLoader extends AsyncTask<Long, Void, Cursor> {
        private final String TAG = "AsyncSessionLoader";

        private SQLiteDatabase mDB;

        @Override
        protected Cursor doInBackground(Long... longs) {
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
            String selection = SessionLoggingContract.SessionMeta._ID + " LIKE ?";
            // Define Selection Arguments
            String[] selectionArgs = {String.valueOf(longs[0])};

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
                return;
            }

            // prepare session object
            long ID = c.getLong(c.getColumnIndexOrThrow(SessionLoggingContract.SessionMeta._ID));
            String name = c.getString(c.getColumnIndexOrThrow(SessionLoggingContract.SessionMeta.COLUMN_NAME_NAME));
            String date = c.getString(c.getColumnIndexOrThrow(SessionLoggingContract.SessionMeta.COLUMN_NAME_DATE));
            NfcSession session = new NfcSession(date, ID, name);

            // Update session information
            setSessionDetails(session);
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
            confirmDelete();
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
