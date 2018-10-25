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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.db.SessionLog;
import tud.seemuh.nfcgate.db.SessionLogJoin;
import tud.seemuh.nfcgate.db.model.SessionLogEntryViewModel;
import tud.seemuh.nfcgate.db.model.SessionLogEntryViewModelFactory;
import tud.seemuh.nfcgate.db.pcapng.ISO14443Stream;
import tud.seemuh.nfcgate.gui.component.FileShare;
import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.util.NfcComm;

import static tud.seemuh.nfcgate.util.Utils.bytesToHexDump;

public class SessionLogEntryFragment extends Fragment {
    public enum Type {
        // look at a prerecorded log
        VIEW,
        // like VIEW with the ability to confirm or reject this log
        SELECT,
        // like view but with autoscroll and updates
        LIVE
    }

    // UI references
    ListView mLogEntries;

    // db data
    private SessionLogEntryViewModel mLogEntryModel;
    private SessionLogEntryListAdapter mLogEntriesAdapter;
    private long mSessionId;
    private Type mType;

    // current data
    private SimpleDateFormat mIsoDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private List<NfcComm> mLogData = new ArrayList<>();
    private SessionLog mSessionLog;

    // callback
    public interface LogSelectedCallback {
        void onLogSelected(long sessionId);
    }
    LogSelectedCallback mCallback;

    public static SessionLogEntryFragment newInstance(long sessionId, Type type, LogSelectedCallback cb) {
        return new SessionLogEntryFragment()
                .setup(sessionId, type, cb);
    }

    SessionLogEntryFragment setup(long sessionId, Type type, LogSelectedCallback cb) {
        mSessionId = sessionId;
        mType = type;
        mCallback = cb;
        return this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_log_entry, container, false);

        // setup
        mLogEntries = v.findViewById(R.id.log_entries);

        // enable custom toolbar actions
        setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        // view requires a back button
        if (mType == Type.VIEW) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // setup db model
        mLogEntryModel = ViewModelProviders.of(this, new SessionLogEntryViewModelFactory(getActivity().getApplication(), mSessionId))
                .get(SessionLogEntryViewModel.class);

        mLogEntryModel.getSession().observe(this, new Observer<SessionLogJoin>() {
            @Override
            public void onChanged(@Nullable SessionLogJoin sessionLogJoin) {
                mLogEntriesAdapter.clear();
                mLogData.clear();

                if (sessionLogJoin != null) {
                    // save current log data
                    mSessionLog = sessionLogJoin.getSessionLog();
                    for (NfcCommEntry nfcCommEntry : sessionLogJoin.getNfcCommEntries())
                        mLogData.add(nfcCommEntry.getNfcComm());

                    // add log data to list adapter
                    mLogEntriesAdapter.addAll(mLogData);
                    mLogEntriesAdapter.notifyDataSetChanged();

                    // live requires autoscroll, view and select require subtitle
                    if (mType == Type.LIVE)
                        mLogEntries.setSelection(mLogEntriesAdapter.getCount() - 1);
                    else
                        actionBar.setSubtitle(mSessionLog.toString());
                }
            }
        });

        // setup db data and view adapter
        mLogEntriesAdapter = new SessionLogEntryListAdapter(getActivity(), R.layout.list_log_entry);
        mLogEntries.setAdapter(mLogEntriesAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // select mode requires an accept action, view mode a share action
        if (mType == Type.SELECT)
            inflater.inflate(R.menu.toolbar_log_choose, menu);
        else if (mType == Type.VIEW)
            inflater.inflate(R.menu.toolbar_log_view, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.action_yes:
                mCallback.onLogSelected(mSessionId);
                return true;
            case R.id.action_share:
                // share pcap
                new FileShare(getActivity())
                        .setPrefix(mIsoDate.format(mSessionLog.getDate()))
                        .setExtension(".pcapng")
                        .setMimeType("application/*")
                        .share(new ISO14443Stream().append(mLogData));
        }
        return super.onOptionsItemSelected(item);
    }

    private class SessionLogEntryListAdapter extends ArrayAdapter<NfcComm> {
        private int mResource;

        SessionLogEntryListAdapter(@NonNull Context context, int resource) {
            super(context, resource);

            mResource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;

            if (v == null)
                v = LayoutInflater.from(getContext()).inflate(mResource, null);

            final NfcComm nfcComm = getItem(position);
            if (nfcComm != null) {
                // set image indicating card or reader
                v.<ImageView>findViewById(R.id.type).setImageResource(nfcComm.isCard() ?
                        R.drawable.ic_tag_grey_60dp : R.drawable.ic_reader_grey_60dp);

                // set content to either config stream or binary content
                v.<TextView>findViewById(R.id.data).setText(nfcComm.isInitial() ?
                        new ConfigBuilder(nfcComm.getData()).toString() : bytesToHexDump(nfcComm.getData()));

                // set timestamp
                v.<TextView>findViewById(R.id.timestamp).setText(mIsoDate.format(new Date(nfcComm.getTimestamp())));
            }

            return v;
        }
    }
}
