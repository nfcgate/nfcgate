package tud.seemuh.nfcgate.gui.log;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tud.seemuh.nfcgate.db.AppDatabase;
import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.db.SessionLog;
import tud.seemuh.nfcgate.db.SessionLogJoin;
import tud.seemuh.nfcgate.db.model.SessionLogEntryViewModel;
import tud.seemuh.nfcgate.db.model.SessionLogEntryViewModelFactory;
import tud.seemuh.nfcgate.db.pcapng.ISO14443Stream;
import tud.seemuh.nfcgate.gui.component.FileShare;
import tud.seemuh.nfcgate.util.NfcComm;

public class LogAction {
    private Fragment mFragment;
    private SessionLogEntryViewModel mLogEntryModel;
    private List<NfcComm> mLogItems = new ArrayList<>();

    public LogAction(Fragment fragment) {
        mFragment = fragment;
    }

    public void delete(final SessionLog session) {
        new Thread() {
            @Override
            public void run() {
                AppDatabase.getDatabase(mFragment.getActivity()).sessionLogDao().delete(session);
            }
        }.start();
    }

    public void share(final SessionLog session) {
        // clear previous items
        mLogItems.clear();

        // setup db model
        mLogEntryModel = ViewModelProviders.of(mFragment, new SessionLogEntryViewModelFactory(
                mFragment.getActivity().getApplication(), session.getId()))
                .get(SessionLogEntryViewModel.class);

        mLogEntryModel.getSession().observe(mFragment, new Observer<SessionLogJoin>() {
            @Override
            public void onChanged(@Nullable SessionLogJoin sessionLogJoin) {
                if (sessionLogJoin != null && mLogItems.isEmpty()) {
                    for (NfcCommEntry nfcCommEntry : sessionLogJoin.getNfcCommEntries())
                        mLogItems.add(nfcCommEntry.getNfcComm());

                    share(session, mLogItems);
                }
            }
        });
    }

    public void share(SessionLog sessionLog, List<NfcComm> logItems) {
        // share pcap
        new FileShare(mFragment.getActivity())
                .setPrefix(sessionLog.toString())
                .setExtension(".pcapng")
                .setMimeType("application/*")
                .share(new ISO14443Stream().append(logItems));
    }
}
