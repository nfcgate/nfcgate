package tud.seemuh.nfcgate.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.AppDatabase;
import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.db.SessionLog;
import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.network.NetworkStatus;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.nfc.modes.RelayMode;
import tud.seemuh.nfcgate.util.NfcComm;

public class RelayFragment extends Fragment implements BaseFragment {
    // UI references
    View mTagWaiting;
    LinearLayout mSelector;
    TextView mLog;
    ImageView mSemaphoreLight;
    TextView mSemaphoreText;

    // database reference
    AppDatabase mDatabase;
    long mSessionId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_relay, container, false);

        // setup
        mTagWaiting = v.findViewById(R.id.tag_wait);
        mSelector = v.findViewById(R.id.relay_selector);
        mLog = v.findViewById(R.id.relay_log);
        mSemaphoreLight = v.findViewById(R.id.tag_semaphore_light);
        mSemaphoreText = v.findViewById(R.id.tag_semaphore_text);

        // database setup
        mDatabase = AppDatabase.getDatabase(getActivity());

        // selector setup
        v.<Button>findViewById(R.id.btn_reader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(true);
            }
        });
        v.<Button>findViewById(R.id.btn_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(false);
            }
        });

        // log autoscroll
        mLog.setMovementMethod(new ScrollingMovementMethod());

        setHasOptionsMenu(true);
        setSelectorVisible(true);
        return v;
    }

    @Override
    public String getTagName() {
        return "relay";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_relay, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // TODO:
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when user selects reader or tag
     */
    private void onSelect(boolean reader) {
        // enable reader or emulator mode
        getNfc().startMode(new UIRelayMode(reader));

        // toggle selector visibility
        setSelectorVisible(false);
    }

    /**
     * Shows or hides the selector
     */
    private void setSelectorVisible(boolean visible) {
        mSelector.setVisibility(visible ? View.VISIBLE : View.GONE);
        mTagWaiting.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    private void setSemaphore(int lightId, String message) {
        mSemaphoreLight.setImageResource(lightId);
        mSemaphoreText.setText(message);
    }

    private void handleStatus(NetworkStatus status) {
        mLog.append("Status changed: " + status.name() + "\n");

        // semaphore
        if (status == NetworkStatus.PARTNER_WAIT)
            setSemaphore(R.drawable.semaphore_light_yellow, "Waiting for partner...");
        else if (status == NetworkStatus.PARTNER_CONNECT)
            setSemaphore(R.drawable.semaphore_light_green, "Connected to partner...");
        else
            setSemaphore(R.drawable.semaphore_light_red, "Connecting to network...");
    }

    public NfcManager getNfc() {
        return ((MainActivity) getActivity()).getNfc();
    }

    class UIRelayMode extends RelayMode {
        UIRelayMode(boolean reader) {
            super(reader);
        }

        @Override
        public void onData(NfcComm data) {
            super.onData(data);

            // create new session if needed
            if (mSessionId == -1 || data.isInitial())
                mSessionId = mDatabase.sessionLogDao().insert(new SessionLog(new Date()));

            // log to database
            mDatabase.nfcCommEntryDao().insert(new NfcCommEntry(data, mSessionId));
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
