package tud.seemuh.nfcgate.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.nfc.NfcManager;

public class ReplayFragment extends Fragment {
    // UI references
    View mTagWaiting;
    LinearLayout mSelector;
    TextView mLog;
    ImageView mSemaphoreLight;
    TextView mSemaphoreText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_replay, container, false);

        // setup
        mTagWaiting = v.findViewById(R.id.tag_wait);
        mSelector = v.findViewById(R.id.replay_selector);
        mLog = v.findViewById(R.id.replay_log);
        mSemaphoreLight = v.findViewById(R.id.tag_semaphore_light);
        mSemaphoreText = v.findViewById(R.id.tag_semaphore_text);

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
        resetReplay();
        return v;
    }

    /**
     * Called when user selects reader or tag
     */
    private void onSelect(boolean reader) {
        /*
        // enable reader or emulator mode
        getNfc().startMode(new RelayFragment.UIRelayMode(reader));
        */

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

    private void resetReplay() {
        getNfc().stopMode();
        setSelectorVisible(true);
        setSemaphore(R.drawable.semaphore_light_red, "Connecting to network");
    }

    public NfcManager getNfc() {
        return ((MainActivity) getActivity()).getNfc();
    }
}
