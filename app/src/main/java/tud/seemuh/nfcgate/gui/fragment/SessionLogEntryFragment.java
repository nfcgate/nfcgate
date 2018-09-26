package tud.seemuh.nfcgate.gui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.db.SessionLogJoin;
import tud.seemuh.nfcgate.db.model.SessionLogEntryViewModel;
import tud.seemuh.nfcgate.db.model.SessionLogEntryViewModelFactory;
import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.util.NfcComm;

import static tud.seemuh.nfcgate.util.Utils.bytesToHexDump;

public class SessionLogEntryFragment extends Fragment {
    // UI references
    ListView mLogEntries;

    // db data
    private SessionLogEntryViewModel mLogEntryModel;
    private SessionLogListAdapter mLogEntriesAdapter;
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
        mLogEntriesAdapter = new SessionLogListAdapter(getActivity(), R.layout.list_log_entry);
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

    private class SessionLogListAdapter extends ArrayAdapter<NfcCommEntry> {

        private int mResource;

        public SessionLogListAdapter(@NonNull Context context, int resource) {
            super(context, resource);

            mResource = resource;
        }

        public SessionLogListAdapter(@NonNull Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);

            mResource = resource;
        }

        public SessionLogListAdapter(@NonNull Context context, int resource, @NonNull NfcCommEntry[] objects) {
            super(context, resource, objects);

            mResource = resource;
        }

        public SessionLogListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull NfcCommEntry[] objects) {
            super(context, resource, textViewResourceId, objects);

            mResource = resource;
        }

        public SessionLogListAdapter(@NonNull Context context, int resource, @NonNull List<NfcCommEntry> objects) {
            super(context, resource, objects);

            mResource = resource;
        }

        public SessionLogListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<NfcCommEntry> objects) {
            super(context, resource, textViewResourceId, objects);

            mResource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;

            if (v == null)
                v = LayoutInflater.from(getContext()).inflate(mResource, null);

            final NfcCommEntry entry = getItem(position);
            if (entry != null) {
                final NfcComm nfcComm = entry.getNfcComm();

                // set image indicating card or reader
                v.<ImageView>findViewById(R.id.type).setImageResource(nfcComm.isCard() ?
                        R.drawable.ic_tag_grey_60dp : R.drawable.ic_reader_grey_60dp);

                // set content to either config stream or binary content
                v.<TextView>findViewById(R.id.data).setText(nfcComm.isInitial() ?
                        new ConfigBuilder(nfcComm.getData()).toString() : bytesToHexDump(nfcComm.getData()));
            }

            return v;
        }
    }
}
