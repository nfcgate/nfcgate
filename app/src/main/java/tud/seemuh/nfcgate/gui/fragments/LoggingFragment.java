package tud.seemuh.nfcgate.gui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import tud.seemuh.nfcgate.R;

/**
 * Created by Tom on 28.03.2015.
 */
public class LoggingFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static LoggingFragment mFragment;

    private String[] mSessionItems;
    private String[] mCommandsOfSessionItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logging, container, false);

        Spinner mSession = (Spinner) v.findViewById(R.id.selectASession);
        Spinner mCommandsOfSession = (Spinner) v.findViewById(R.id.commandsOfSelectedSession);

        // items to be filled with sink data at a later point  TODO @Max: insert your data into the array using getter / setter (see below)
        mSessionItems = new String[]{"dummy1","dummy2","dummy3"};  // dummy test data
        ArrayAdapter<String> sessionAdapter = new ArrayAdapter<String>(v.getContext(),android.R.layout.simple_spinner_item,mSessionItems);
        sessionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSession.setAdapter(sessionAdapter);
        mSession.setOnItemSelectedListener(this);

        mCommandsOfSessionItems = new String[]{"dummy4","dummy5","dummy6"};
        ArrayAdapter<String> commandsOfSessionAdapter = new ArrayAdapter<String>(v.getContext(),android.R.layout.simple_spinner_item,mCommandsOfSessionItems);
        commandsOfSessionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCommandsOfSession.setAdapter(commandsOfSessionAdapter);
        mCommandsOfSession.setOnItemSelectedListener(this);

        return v;
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.selectASession)
        {
            switch (position) {
                case 0:
                    // Whatever you want to happen when the first item gets selected
                    break;
                case 1:
                    // Whatever you want to happen when the second item gets selected
                    break;
                case 2:
                    // Whatever you want to happen when the third item gets selected
                    break;

            }
        }
        else if(spinner.getId() == R.id.commandsOfSelectedSession)
        {
            switch (position) {
                case 0:
                    // Whatever you want to happen when the first item gets selected
                    break;
                case 1:
                    // Whatever you want to happen when the second item gets selected
                    break;
                case 2:
                    // Whatever you want to happen when the third item gets selected
                    break;

            }
        }


    }

    public void setmSessionItems(String[] newItems)
    {
        System.arraycopy(newItems,0,mSessionItems,0,newItems.length);
    }

    public void setmCommandsOfSessionItems(String[] newItems)
    {
        System.arraycopy(newItems,0,mCommandsOfSessionItems,0,newItems.length);
    }

    public String[] getmSessionItems()
    {
        return this.mSessionItems;
    }

    public String[] getmCommandsOfSessionItems()
    {
        return this.mCommandsOfSessionItems;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // add something here
    }

    public static LoggingFragment getInstance() {

        if(mFragment == null) {
            mFragment = new LoggingFragment();
        }

        return mFragment;
    }
}
