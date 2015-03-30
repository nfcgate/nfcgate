package tud.seemuh.nfcgate.gui.fragments;

import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.util.db.SessionLoggingContract;
import tud.seemuh.nfcgate.util.db.SessionLoggingDbHelper;

/**
 * Created by Tom on 28.03.2015.
 */
public class LoggingFragment extends Fragment{

    private static LoggingFragment mFragment;

    private ListView mListView;
    private ArrayAdapter<String> mlistAdapter;

    // items to be filled with sink data at a later point
    // TODO @MAX -> insert proper values from db here (using getter/setter)
    private String[] mItems = new String[] {"dummy1","dummy2","dummy3","dummy4"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logging_list, container, false);

        mListView = (ListView) v.findViewById(R.id.sessionList);

        ArrayList<String> mSessionItemsList = new ArrayList<String>();
        mSessionItemsList.addAll(Arrays.asList(mItems));

        mlistAdapter = new ArrayAdapter<String>(v.getContext(), R.layout.fragment_logging_row, mSessionItemsList);

        mListView.setAdapter(mlistAdapter);

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

        return v;
    }

    protected void onListItemClick(View v, int pos, long id) {
        // start a new activity here to display the details of the clicked list element
        if (mlistAdapter.getItem(0).equals("go back") && pos == 0)
        {
            //reload session overview  (e.g go back to previous screen)
            String[] temp = new String[] {"dummy1","dummy2","dummy3","dummy4"};
            this.setmItems(temp);
            mlistAdapter.clear();
            mlistAdapter.addAll(this.getmItems());
            mlistAdapter.notifyDataSetChanged();
        }
        else
        {
            // load the specific session the clicked on into the array
            // temporarily used dummy text
            // TODO @MAX -> insert proper values from db here
            this.setmItems(new String[] {"go back","test1","test2","test3"});
            mlistAdapter.clear();
            mlistAdapter.addAll(this.getmItems());
            mlistAdapter.notifyDataSetChanged();
        }
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
        // Warning: item gets immediately deleted !without warning! on long click  TODO improve, maybe by displaying a warning before removing the item
        String selectedItem = mlistAdapter.getItem(pos);
        mlistAdapter.remove(selectedItem);
        mlistAdapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), "Item " + pos + " removed!", Toast.LENGTH_LONG).show();
        return true;
    }

    public void onBackPressed() {
        this.onListItemClick(getView(),0,0);
    }

    public void setmItems(String[] newItems)
    {
        System.arraycopy(newItems,0, mItems,0,newItems.length);
    }

    public String[] getmItems()
    {
        return this.mItems;
    }

    public static LoggingFragment getInstance() {

        if(mFragment == null) {
            mFragment = new LoggingFragment();
        }
        return mFragment;
    }

    private class AsyncSessionLoader extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... voids) {
            // Get a DB object
            SessionLoggingDbHelper dbHelper = new SessionLoggingDbHelper(getActivity());
            SQLiteDatabase db = dbHelper.getReadableDatabase();

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
            Cursor c = db.query(
                    SessionLoggingContract.SessionEvent.TABLE_NAME,  // Target Table
                    projection,    // Which fields are we interested in?
                    selection,     // Selection clause
                    selectionArgs, // Arguments to clause
                    null,          // Grouping (not desired in this case)
                    null,          // Filtering (not desired in this case)
                    sortorder      // Sort order
            );

            // Close connection to the database
            db.close();
            return c;
        }

        @Override
        protected void onPostExecute(Cursor c) {
            // Move to the first element of the cursor
            c.moveToFirst();
            do {
                // TODO Process data from cursor, insert into List
            } while (c.moveToNext()); // Iterate until all elements of the cursor have been processed
            // Close the cursor, freeing the used memory
            c.close();
        }
    }
}
