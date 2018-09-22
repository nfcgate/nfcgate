package tud.seemuh.nfcgate.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.network.ServerConnection;

public class ReplayFragment extends BaseNetworkFragment implements LoggingFragment.LogItemSelectedCallback {
    // session selection reference
    LoggingFragment mLoggingFragment = new LoggingFragment();

    // replay partner connection
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

        // show reader/tag selector
        setSelectorVisible(true);

        // set subtitle
        getMainActivity().getSupportActionBar().setSubtitle("Session " + sessionId);
    }

    protected void onSelect(boolean reader) {
        // print status
        setSemaphore(R.drawable.semaphore_light_red, "Connecting to Network");

        // hide selector, show tag wait indicator
        setSelectorVisible(false);
        setTagWaitVisible(true);

        /*
        // enable reader or emulator mode
        getNfc().startMode(new RelayFragment.UIRelayMode(reader));
        */
    }
}
