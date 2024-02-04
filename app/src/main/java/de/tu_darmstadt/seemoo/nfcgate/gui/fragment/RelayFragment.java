package de.tu_darmstadt.seemoo.nfcgate.gui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.tu_darmstadt.seemoo.nfcgate.R;
import de.tu_darmstadt.seemoo.nfcgate.db.SessionLog;
import de.tu_darmstadt.seemoo.nfcgate.db.worker.LogInserter;
import de.tu_darmstadt.seemoo.nfcgate.network.data.NetworkStatus;
import de.tu_darmstadt.seemoo.nfcgate.nfc.modes.RelayMode;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class RelayFragment extends BaseNetworkFragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        // set relay action text
        v.<TextView>findViewById(R.id.txt_action).setText(getString(R.string.relay_action));

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // database setup
        mLogInserter = new LogInserter(getActivity(), SessionLog.SessionType.RELAY, this);
    }

    @Override
    protected void reset() {
        super.reset();

        // show selector, hide tag wait indicator
        setSelectorVisible(true);
        setTagWaitVisible(false, false);
    }

    protected void onSelect(boolean reader) {
        if (checkNetwork()) {
            // set status indicator
            handleStatus(NetworkStatus.CONNECTING);

            // toggle selector visibility
            setSelectorVisible(false);
            setTagWaitVisible(true, !reader);

            // enable reader or emulator mode
            getNfc().startMode(new UIRelayMode(reader));
        }
    }

    class UIRelayMode extends RelayMode {
        UIRelayMode(boolean reader) {
            super(reader);
        }

        void runOnUI(Runnable r) {
            FragmentActivity activity = getActivity();
            if (activity != null)
                activity.runOnUiThread(r);
        }

        @Override
        public void onData(boolean isForeign, NfcComm data) {
            // log to database and UI
            mLogInserter.log(data);

            // hide wait indicator
            runOnUI(() -> setTagWaitVisible(false, false));

            // forward data to NFC or network
            super.onData(isForeign, data);
        }

        @Override
        public void onNetworkStatus(final NetworkStatus status) {
            super.onNetworkStatus(status);

            // report status
            runOnUI(() -> handleStatus(status));
        }
    }
}
