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
import tud.seemuh.nfcgate.network.NetworkStatus;
import tud.seemuh.nfcgate.nfc.modes.RelayMode;
import tud.seemuh.nfcgate.util.NfcComm;

public class RelayFragment extends BaseNetworkFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        // set relay action text
        v.<TextView>findViewById(R.id.txt_action).setText("Emulate");

        return v;
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

            // enable reader or emulator mode
            getNfc().startMode(new UIRelayMode(reader));
        }
    }

    private void handleStatus(NetworkStatus status) {
        switch (status) {
            case ERROR:
                setSemaphore(R.drawable.semaphore_light_red, "Connection error");
                break;
            case CONNECTED:
                setSemaphore(R.drawable.semaphore_light_yellow, "Connected, waiting for partner");
                break;
            case PARTNER_CONNECT:
                setSemaphore(R.drawable.semaphore_light_green, "Connected to partner");
                break;
            case PARTNER_LEFT:
                setSemaphore(R.drawable.semaphore_light_red, "Partner left");
                break;
        }
    }

    class UIRelayMode extends RelayMode {
        UIRelayMode(boolean reader) {
            super(reader);
        }

        @Override
        public void onData(NfcComm data) {
            super.onData(data);

            // log to database
            mLogInserter.log(data);
        }

        @Override
        public void onNetworkStatus(final NetworkStatus status) {
            super.onNetworkStatus(status);

            // add log entry
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
