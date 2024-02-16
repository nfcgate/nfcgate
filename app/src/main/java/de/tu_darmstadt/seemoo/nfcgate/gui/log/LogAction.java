package de.tu_darmstadt.seemoo.nfcgate.gui.log;

import androidx.lifecycle.ViewModelProviders;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.db.AppDatabase;
import de.tu_darmstadt.seemoo.nfcgate.db.NfcCommEntry;
import de.tu_darmstadt.seemoo.nfcgate.db.SessionLog;
import de.tu_darmstadt.seemoo.nfcgate.db.model.SessionLogEntryViewModel;
import de.tu_darmstadt.seemoo.nfcgate.db.model.SessionLogEntryViewModelFactory;
import de.tu_darmstadt.seemoo.nfcgate.db.pcapng.ISO14443Stream;
import de.tu_darmstadt.seemoo.nfcgate.gui.component.FileShare;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class LogAction {
    private final Fragment mFragment;
    private final List<NfcComm> mLogItems = new ArrayList<>();

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
        final SessionLogEntryViewModel mLogEntryModel = ViewModelProviders.of(mFragment, new SessionLogEntryViewModelFactory(
                        mFragment.getActivity().getApplication(), session.getId()))
                .get(SessionLogEntryViewModel.class);

        mLogEntryModel.getSession().observe(mFragment, sessionLogJoin -> {
            if (sessionLogJoin != null && mLogItems.isEmpty()) {
                for (NfcCommEntry nfcCommEntry : sessionLogJoin.getNfcCommEntries())
                    mLogItems.add(nfcCommEntry.getNfcComm());

                share(session, mLogItems);
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
