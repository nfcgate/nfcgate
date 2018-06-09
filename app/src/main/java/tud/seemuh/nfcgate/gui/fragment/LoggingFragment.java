package tud.seemuh.nfcgate.gui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.SessionLog;
import tud.seemuh.nfcgate.gui.model.SessionLogViewModel;

public class LoggingFragment extends Fragment implements BaseFragment {
    // UI references
    ListView mLog;

    // db data
    private SessionLogViewModel mLogModel;
    private ArrayAdapter<SessionLog> mLogAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logging, container, false);

        // setup
        mLog = v.findViewById(R.id.session_log);

        // setup db model
        mLogModel = ViewModelProviders.of(this).get(SessionLogViewModel.class);
        mLogModel.getTagInfos().observe(this, new Observer<List<SessionLog>>() {
            @Override
            public void onChanged(@Nullable List<SessionLog> tagInfos) {
                mLogAdapter.clear();
                mLogAdapter.addAll(tagInfos);
                mLogAdapter.notifyDataSetChanged();
            }
        });

        // handlers
        mLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    // open detail view with log information
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_content, SessionLogEntryFragment.newInstance(mLogAdapter.getItem(position).getId()), "log_entry")
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLogAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        mLog.setAdapter(mLogAdapter);
    }

    @Override
    public String getTagName() {
        return "logging";
    }
}
