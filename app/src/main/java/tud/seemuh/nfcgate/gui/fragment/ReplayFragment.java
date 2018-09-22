package tud.seemuh.nfcgate.gui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.db.SessionLogJoin;
import tud.seemuh.nfcgate.db.model.SessionLogEntryViewModel;
import tud.seemuh.nfcgate.db.model.SessionLogEntryViewModelFactory;
import tud.seemuh.nfcgate.network.NetworkStatus;
import tud.seemuh.nfcgate.network.ServerConnection;
import tud.seemuh.nfcgate.nfc.modes.BaseMode;
import tud.seemuh.nfcgate.nfc.modes.RelayMode;
import tud.seemuh.nfcgate.nfc.modes.ReplayMode;
import tud.seemuh.nfcgate.util.NfcComm;

public class ReplayFragment extends BaseNetworkFragment implements LoggingFragment.LogItemSelectedCallback {
    // session selection reference
    LoggingFragment mLoggingFragment = new LoggingFragment();

    // replay data
    List<NfcCommEntry> mSessionLog;
    ServerConnection mReplayConnection;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        // set relay action text
        v.<TextView>findViewById(R.id.txt_action).setText("Replay");

        // setup log item callback
        mLoggingFragment.setLogItemSelectedCallback(this);

        // insert logging fragment in content area
        getMainActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.lay_content, mLoggingFragment)
                .commit();

        return v;
    }

    @Override
    protected void reset() {
        super.reset();

        // show session selector
        getMainActivity().getSupportFragmentManager().beginTransaction()
                .show(mLoggingFragment)
                .commit();

        // hide selector and tag wait indicator
        setSelectorVisible(false);
        setTagWaitVisible(false);

        // clear subtitle
        getMainActivity().getSupportActionBar().setSubtitle("Select session");
    }

    @Override
    public void onLogItemSelected(int sessionId) {
        // hide session selection
        getMainActivity().getSupportFragmentManager().beginTransaction()
                .hide(mLoggingFragment)
                .commit();

        // set subtitle
        getMainActivity().getSupportActionBar().setSubtitle("Session " + sessionId);

        // load session data
        ViewModelProviders.of(this, new SessionLogEntryViewModelFactory(getActivity().getApplication(), sessionId))
                .get(SessionLogEntryViewModel.class)
                .getSession()
                .observe(this, new Observer<SessionLogJoin>() {
                    @Override
                    public void onChanged(@Nullable SessionLogJoin sessionLogJoin) {
                        if (sessionLogJoin != null) {
                            mSessionLog = sessionLogJoin.getNfcCommEntries();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // show reader/tag selector after session data is loaded
                                    setSelectorVisible(true);
                                }
                            });
                        }
                    }
                });
    }

    protected void onSelect(boolean reader) {
        // print status
        setSemaphore(R.drawable.semaphore_light_red, "Connecting to Network");

        // hide selector, show tag wait indicator
        setSelectorVisible(false);
        setTagWaitVisible(true);

        // enable reader or emulator replay mode
        getNfc().startMode(new ReplayFragment.UIReplayMode(reader));
    }

    class UIReplayMode extends ReplayMode {
        UIReplayMode(boolean reader) {
            super(reader, mSessionLog);
        }

        @Override
        public void onData(boolean isForeign, NfcComm data) {
            super.onData(isForeign, data);
        }
    }
}
