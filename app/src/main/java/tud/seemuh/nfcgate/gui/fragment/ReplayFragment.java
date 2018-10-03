package tud.seemuh.nfcgate.gui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.PreferenceManagerFix;
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
import tud.seemuh.nfcgate.network.NetworkManager;
import tud.seemuh.nfcgate.network.data.NetworkStatus;
import tud.seemuh.nfcgate.nfc.NfcLogReplayer;
import tud.seemuh.nfcgate.nfc.modes.RelayMode;
import tud.seemuh.nfcgate.util.NfcComm;

public class ReplayFragment extends BaseNetworkFragment implements LoggingFragment.LogItemSelectedCallback {
    // session selection reference
    LoggingFragment mLoggingFragment = new LoggingFragment();

    // replay data
    List<NfcCommEntry> mSessionLog;
    boolean mOfflineReplay = true;
    UIReplayer mReplayer;

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

        // release replayer network
        if (mReplayer != null)
            mReplayer.release();

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
        // get preference data
        SharedPreferences prefs = PreferenceManagerFix.getDefaultSharedPreferences(getActivity());
        mOfflineReplay = !prefs.getBoolean("network", false);

        // quit if network check fails
        if (!mOfflineReplay && !checkNetwork())
            return;

        // print network status
        if (!mOfflineReplay)
            setSemaphore(R.drawable.semaphore_light_red, "Connecting to Network");

        // hide selector, show tag wait indicator
        setSelectorVisible(false);
        setTagWaitVisible(true);

        // init replayer and mode
        mReplayer = new UIReplayer(reader);
        getNfc().startMode(new UIReplayMode(reader));

        // initial tickle required for tag replay
        tickleReplayer();
    }

    void tickleReplayer() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mReplayer.onReceive(null);
            }
        });
    }

    /**
     * Offline replay mode
     */
    class UIReplayMode extends RelayMode {
        UIReplayMode(boolean reader) {
            super(reader);

            // prevent network connect in offline mode
            mOnline = !mOfflineReplay;
        }

        @Override
        public void onData(boolean isForeign, NfcComm data) {
            // log to database and UI
            //mLogInserter.log(data); // TODO: separate relay and replay log
            logAppend(data.toString());

            // forward data to NFC or network
            super.onData(isForeign, data);
        }

        @Override
        protected void toNetwork(final NfcComm data) {
            if (!mOfflineReplay)
                // send to actual network
                super.toNetwork(data);
            else
                // simulate network send
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mReplayer.onReceive(data);
                    }
                });
        }

        @Override
        public void onNetworkStatus(final NetworkStatus status) {
            super.onNetworkStatus(status);

            // report status
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleStatus(status);
                    }
                });
            }
        }
    }

    class UIReplayer implements NetworkManager.Callback {
        NfcLogReplayer mReplayer;
        NetworkManager mReplayNetwork = null;

        UIReplayer(boolean reader) {
            mReplayer = new NfcLogReplayer(reader, mSessionLog);

            if (!mOfflineReplay) {
                mReplayNetwork = new NetworkManager(getMainActivity(), this);
                mReplayNetwork.connect();
            }
        }

        void release() {
            if (mReplayNetwork != null)
                mReplayNetwork.disconnect();
        }

        @Override
        public void onReceive(NfcComm data) {
            // get response
            NfcComm response = mReplayer.getResponse(data);

            if (response != null && mOfflineReplay)
                // simulate network receive
                getNfc().handleData(true, response);
            else if (response != null)
                // actual network receive
                mReplayNetwork.send(response);

            // if we need to take action, schedule a tickle
            if (!mReplayer.shouldWait())
                tickleReplayer();
        }

        @Override
        public void onNetworkStatus(final NetworkStatus status) {
            // report status
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleStatus(status);
                    }
                });
            }
        }
    }
}
