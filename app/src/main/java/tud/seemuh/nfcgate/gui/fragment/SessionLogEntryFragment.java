package tud.seemuh.nfcgate.gui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.db.SessionLogJoin;
import tud.seemuh.nfcgate.db.model.SessionLogEntryViewModel;
import tud.seemuh.nfcgate.db.model.SessionLogEntryViewModelFactory;

public class SessionLogEntryFragment extends Fragment {
    // UI references
    ListView mLogEntries;

    // db data
    private SessionLogEntryViewModel mLogEntryModel;
    private ArrayAdapter<NfcCommEntry> mLogEntriesAdapter;
    private long mSessionLog;

    public static SessionLogEntryFragment newInstance(long sessionLog) {
        SessionLogEntryFragment fragment = new SessionLogEntryFragment();

        Bundle args = new Bundle();
        args.putLong("sessionLog", sessionLog);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSessionLog = getArguments().getLong("sessionLog");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_log_entry, container, false);

        // setup
        mLogEntries = v.findViewById(R.id.log_entries);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // setup db model
        mLogEntryModel = ViewModelProviders.of(this, new SessionLogEntryViewModelFactory(getActivity().getApplication(), mSessionLog))
                .get(SessionLogEntryViewModel.class);

        mLogEntryModel.getSession().observe(this, new Observer<SessionLogJoin>() {
            @Override
            public void onChanged(@Nullable SessionLogJoin sessionLogJoin) {
                mLogEntriesAdapter.clear();

                if (sessionLogJoin != null) {
                    actionBar.setSubtitle(sessionLogJoin.getSessionLog().toString());

                    mLogEntriesAdapter.addAll(sessionLogJoin.getNfcCommEntries());
                    mLogEntriesAdapter.notifyDataSetChanged();
                }
            }
        });

        // setup db data and view adapter
        mLogEntriesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        mLogEntries.setAdapter(mLogEntriesAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Log.d("", "BACK PRESSED");
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
