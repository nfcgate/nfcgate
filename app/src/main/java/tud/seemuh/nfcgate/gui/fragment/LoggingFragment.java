package tud.seemuh.nfcgate.gui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.SessionLog;
import tud.seemuh.nfcgate.db.model.SessionLogViewModel;

public class LoggingFragment extends Fragment {
    // UI references
    ListView mLog;
    TextView mEmptyText;
    ActionMode mActionMode;

    // db data
    private SessionLogViewModel mLogModel;
    private SessionLogListAdapter mLogAdapter;

    // callback
    public interface LogItemSelectedCallback {
        void onLogItemSelected(int sessionId);
    }
    LogItemSelectedCallback mCallback = new LogItemSelectedDefaultCallback();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logging, container, false);

        // setup
        mLog = v.findViewById(R.id.session_log);
        mEmptyText = v.findViewById(R.id.txt_empty);

        // setup db model
        mLogModel = ViewModelProviders.of(this).get(SessionLogViewModel.class);
        mLogModel.getSessionLogs().observe(this, new Observer<List<SessionLog>>() {
            @Override
            public void onChanged(@Nullable List<SessionLog> sessionLogs) {
                mLogAdapter.clear();
                mLogAdapter.addAll(sessionLogs);
                mLogAdapter.notifyDataSetChanged();

                // toggle empty message
                setEmptyTextVisible(sessionLogs.isEmpty());
            }
        });

        // handlers
        mLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null)
                    mActionMode.finish();

                if (position >= 0)
                    mCallback.onLogItemSelected(mLogAdapter.getItem(position).getId());
            }
        });
        mLog.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mActionMode = getActivity().<Toolbar>findViewById(R.id.toolbar).startActionMode(new ActionModeCallback());
                mActionMode.setTitle("Log Action");
                mActionMode.setSubtitle("Session " + mLogAdapter.getItem(position).getId());
                view.setSelected(true);
                return true;
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLogAdapter = new SessionLogListAdapter(getActivity(), R.layout.list_log);
        mLog.setAdapter(mLogAdapter);
    }

    private void setEmptyTextVisible(boolean visible) {
        mEmptyText.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setLogItemSelectedCallback(LogItemSelectedCallback callback) {
        mCallback = callback;
    }

    /**
     * Class implementing the default log item action: open details view
     */
    class LogItemSelectedDefaultCallback implements LogItemSelectedCallback {
        @Override
        public void onLogItemSelected(int sessionId) {
            // open detail view with log information
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_content, SessionLogEntryFragment.newInstance(sessionId, SessionLogEntryFragment.Type.VIEW, null), "log_entry")
                    .addToBackStack(null)
                    .commit();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.toolbar_log_view, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    // TODO:
                    mode.finish();
                    return true;
                case R.id.action_share:
                    // TODO:
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }

    private class SessionLogListAdapter extends ArrayAdapter<SessionLog> {
        private int mResource;

        public SessionLogListAdapter(@NonNull Context context, int resource) {
            super(context, resource);

            mResource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;

            if (v == null)
                v = LayoutInflater.from(getContext()).inflate(mResource, null);

            final SessionLog entry = getItem(position);
            if (entry != null) {
                final SessionLog.SessionType type = entry.getType();
                final Date date = entry.getDate();

                // set image indicating card or reader
                switch (type) {
                    case RELAY:
                        v.<ImageView>findViewById(R.id.type).setImageResource(R.drawable.ic_relay_black_24dp);
                        break;
                    case REPLAY:
                        v.<ImageView>findViewById(R.id.type).setImageResource(R.drawable.ic_replay_black_24dp);
                        break;
                }
                v.<TextView>findViewById(R.id.title).setText(date.toString());
            }

            return v;
        }
    }
}
