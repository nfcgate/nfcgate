package tud.seemuh.nfcgate.gui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManagerFix;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.worker.LogInserter;
import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.gui.component.Semaphore;
import tud.seemuh.nfcgate.network.data.NetworkStatus;
import tud.seemuh.nfcgate.nfc.NfcManager;

public abstract class BaseNetworkFragment extends Fragment implements LogInserter.SIDChangedListener {
    // UI references
    View mTagWaiting;
    TextView mTagWaitingText;
    LinearLayout mSelector;
    Semaphore mSemaphore;

    // database log reference
    LogInserter mLogInserter;
    SessionLogEntryFragment mLogFragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_network, container, false);

        // setup
        mTagWaiting = v.findViewById(R.id.tag_wait);
        mTagWaitingText = v.findViewById(R.id.tag_wait_text);
        mSelector = v.findViewById(R.id.selector);
        mSemaphore = new Semaphore(getMainActivity());

        // selector setup
        v.<LinearLayout>findViewById(R.id.select_reader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(true);
            }
        });
        v.<LinearLayout>findViewById(R.id.select_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(false);
            }
        });

        setHasOptionsMenu(true);
        reset();
        return v;
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
                reset();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSIDChanged(long sessionID) {
        // first, close old fragment if exists
        if (mLogFragment != null) {
            getMainActivity().getSupportFragmentManager().beginTransaction()
                    .remove(mLogFragment)
                    .commit();
        }

        // if new session exists, show log fragment
        if (sessionID > -1) {
            mLogFragment = SessionLogEntryFragment.newInstance(sessionID, SessionLogEntryFragment.Type.LIVE, null);
            getMainActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.lay_content, mLogFragment)
                    .commit();
        }
    }

    protected void setSelectorVisible(boolean visible) {
        mSelector.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected void setTagWaitVisible(boolean visible, boolean reader) {
        mTagWaitingText.setText("Waiting for " + (reader ? "Reader" : "Tag") + "...");
        mTagWaiting.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected void handleStatus(NetworkStatus status) {
        switch (status) {
            case ERROR:
                mSemaphore.set(Semaphore.State.RED, "Error");
                break;
            case CONNECTING:
                mSemaphore.set(Semaphore.State.RED, "Connecting to network");
                break;
            case CONNECTED:
                mSemaphore.set(Semaphore.State.YELLOW, "Connected, wait for partner");
                break;
            case PARTNER_CONNECT:
                mSemaphore.set(Semaphore.State.GREEN, "Connected to partner");
                break;
            case PARTNER_LEFT:
                mSemaphore.set(Semaphore.State.RED, "Partner left");
                break;
        }
    }

    /**
     * Returns true if any network connection appears to be online
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Returns true if any server hostname was configured in settings
     */
    private boolean isServerConfigured() {
        SharedPreferences prefs = PreferenceManagerFix.getDefaultSharedPreferences(getActivity());
        return !prefs.getString("host", "").isEmpty();
    }

    /**
     * Checks if any network is available and the server connection is properly configured
     */
    protected boolean checkNetwork() {
        // check if any network connection is available
        if (!isNetworkAvailable()) {
            getMainActivity().showWarning("No network connection available.");
            return false;
        }

        // check if the server connection is properly configured
        if (!isServerConfigured()) {
            getMainActivity().showWarning("No hostname configured in settings.");
            return false;
        }

        return true;
    }

    /**
     * Reset method called initially and when user presses reset button
     */
    protected void reset() {
        getNfc().stopMode();
        mSemaphore.reset();

        if (mLogInserter != null)
            mLogInserter.reset();
    }

    /**
     * Setup method called when user selects reader or tag
     */
    protected abstract void onSelect(boolean reader);

    protected MainActivity getMainActivity() {
        return ((MainActivity) getActivity());
    }

    protected NfcManager getNfc() {
        return getMainActivity().getNfc();
    }
}
