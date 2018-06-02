package tud.seemuh.nfcgate.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.network.NetworkStatus;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.util.NfcComm;

public class RelayFragment extends Fragment implements BaseFragment, NfcManager.Callback {
    // UI references
    View mTagWaiting;
    LinearLayout mSelector;
    TextView mLog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_relay, container, false);

        // setup
        mTagWaiting = v.findViewById(R.id.tag_wait);
        mSelector = v.findViewById(R.id.relay_selector);
        mLog = v.findViewById(R.id.relay_log);

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
        // get callbacks
        getNfc().setCallback(this);

        // enable relay, connect to server, etc
        getNfc().enableRelayMode(reader);

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

    @Override
    public void notify(NfcComm data) {
        // no-op
    }

    @Override
    public void onNetworkStatus(final NetworkStatus status) {
        // add log entry
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLog.append("Status changed: " + status.name() + "\n");
            }
        });
    }

    public NfcManager getNfc() {
        return ((MainActivity) getActivity()).getNfc();
    }
}
