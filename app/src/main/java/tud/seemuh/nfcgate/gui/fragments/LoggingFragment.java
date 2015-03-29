package tud.seemuh.nfcgate.gui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;

import tud.seemuh.nfcgate.R;

/**
 * Created by Tom on 28.03.2015.
 */
public class LoggingFragment extends Fragment{

    private static LoggingFragment mFragment;

    private ListView mListView;
    private ArrayAdapter<String> mlistAdapterSession;

    private String[] mSessionItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logging_list, container, false);

        mListView = (ListView) v.findViewById(R.id.sessionList);

        // items to be filled with sink data at a later point  TODO @Max: insert your data into the array using the getter/setters below
        mSessionItems = new String[] {"dummy1","dummy2","dummy3","dummy4"};  // dummy test data

        ArrayList<String> mSessionItemsList = new ArrayList<String>();
        mSessionItemsList.addAll(Arrays.asList(mSessionItems));

        mlistAdapterSession = new ArrayAdapter<String>(v.getContext(), R.layout.fragment_logging_row, mSessionItemsList);

        mListView.setAdapter(mlistAdapterSession);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int pos,long id) {
                onListItemClick(v,pos,id);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                return onLongListItemClick(v,pos,id);
            }
        });


        return v;
    }

    protected void onListItemClick(View v, int pos, long id) {
        // start a new activity here to display the details of the clicked list element
    }

    protected boolean onLongListItemClick(View v, int pos, long id) {
        // display delete button for the clicked list element
        return true;
    }


    public void setmSessionItems(String[] newItems)
    {
        System.arraycopy(newItems,0,mSessionItems,0,newItems.length);
    }

    public String[] getmSessionItems()
    {
        return this.mSessionItems;
    }

    public static LoggingFragment getInstance() {

        if(mFragment == null) {
            mFragment = new LoggingFragment();
        }
        return mFragment;
    }
}
