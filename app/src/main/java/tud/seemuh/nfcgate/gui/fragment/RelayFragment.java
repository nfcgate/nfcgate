package tud.seemuh.nfcgate.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.worker.LogInserter;
import tud.seemuh.nfcgate.network.data.NetworkStatus;
import tud.seemuh.nfcgate.nfc.modes.RelayMode;
import tud.seemuh.nfcgate.util.NfcComm;

public class RelayFragment extends BaseNetworkFragment {
    // database reference
    LogInserter mLogInserter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        // set relay action text
        v.<TextView>findViewById(R.id.txt_action).setText("Emulate");

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // database setup
        mLogInserter = new LogInserter(getActivity());
    }

    @Override
    protected void reset() {
        super.reset();

        // show selector, hide tag wait indicator
        setSelectorVisible(true);
        setTagWaitVisible(false);
    }

    protected void onSelect(boolean reader) {
        if (checkNetwork()) {
            // print status
            setSemaphore(R.drawable.semaphore_light_red, "Connecting to Network");

            // toggle selector visibility
            setSelectorVisible(false);
            setTagWaitVisible(true);

            // enable reader or emulator mode
            getNfc().startMode(new UIRelayMode(reader));
        }
    }

    class UIRelayMode extends RelayMode {
        UIRelayMode(boolean reader) {
            super(reader);
        }

        @Override
        public void onData(boolean isForeign, NfcComm data) {
            super.onData(isForeign, data);

            // log to database
            mLogInserter.log(data);

            // log to UI
            logAppend(data.toString());
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
}
