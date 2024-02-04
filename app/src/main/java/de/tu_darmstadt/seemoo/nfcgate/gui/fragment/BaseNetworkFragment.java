package de.tu_darmstadt.seemoo.nfcgate.gui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManagerFix;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tu_darmstadt.seemoo.nfcgate.R;
import de.tu_darmstadt.seemoo.nfcgate.db.worker.LogInserter;
import de.tu_darmstadt.seemoo.nfcgate.gui.component.StatusBanner;
import de.tu_darmstadt.seemoo.nfcgate.gui.log.SessionLogEntryFragment;
import de.tu_darmstadt.seemoo.nfcgate.network.data.NetworkStatus;

public abstract class BaseNetworkFragment extends BaseFragment implements LogInserter.SIDChangedListener {
    // UI references
    View mTagWaiting;
    TextView mTagWaitingText;
    LinearLayout mSelector;
    StatusBanner mStatusBanner;

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
        mStatusBanner = new StatusBanner(getMainActivity());

        // selector setup
        v.<LinearLayout>findViewById(R.id.select_reader).setOnClickListener(view -> onSelect(true));
        v.<LinearLayout>findViewById(R.id.select_tag).setOnClickListener(view -> onSelect(false));

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
        if (item.getItemId() == R.id.action_refresh) {
            reset();
        }
        return super.onOptionsItemSelected(item);
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
        mTagWaitingText.setText(getString(R.string.network_waiting_for,
                getString(reader ? R.string.network_reader : R.string.network_tag)));
        mTagWaiting.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected void handleStatus(NetworkStatus status) {
        switch (status) {
            case ERROR:
                mStatusBanner.set(StatusBanner.State.RED, getString(R.string.network_error));
                break;
            case CONNECTING:
                mStatusBanner.set(StatusBanner.State.RED, getString(R.string.network_connecting));
                break;
            case CONNECTED:
                mStatusBanner.set(StatusBanner.State.YELLOW, getString(R.string.network_connected_wait));
                break;
            case PARTNER_CONNECT:
                mStatusBanner.set(StatusBanner.State.GREEN, getString(R.string.network_connected));
                break;
            case PARTNER_LEFT:
                mStatusBanner.set(StatusBanner.State.RED, getString(R.string.network_disconnected));
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
            getMainActivity().showWarning(getString(R.string.error_no_connection));
            return false;
        }

        // check if the server connection is properly configured
        if (!isServerConfigured()) {
            getMainActivity().showWarning(getString(R.string.error_no_hostname));
            return false;
        }

        if (!getNfc().isEnabled()) {
            getMainActivity().showWarning(getString(R.string.error_nfc_disabled));
            return false;
        }

        return true;
    }

    /**
     * Reset method called initially and when user presses reset button
     */
    protected void reset() {
        getNfc().stopMode();
        mStatusBanner.set(StatusBanner.State.IDLE, getString(R.string.network_idle));

        if (mLogInserter != null)
            mLogInserter.reset();
    }

    /**
     * Setup method called when user selects reader or tag
     */
    protected abstract void onSelect(boolean reader);
}
