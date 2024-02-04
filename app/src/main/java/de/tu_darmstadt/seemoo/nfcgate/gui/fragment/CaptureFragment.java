package de.tu_darmstadt.seemoo.nfcgate.gui.fragment;

import android.nfc.Tag;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tu_darmstadt.seemoo.nfcgate.R;
import de.tu_darmstadt.seemoo.nfcgate.nfc.reader.NFCTagReader;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class CaptureFragment extends BaseFragment {
    // UI references
    LinearLayout mStartButton, mStopButton;
    TextView mIdleText, mProgressText;
    ImageView mStartIcon, mStopIcon;

    // state
    boolean mCaptureActive = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_capture, container, false);

        // setup
        mStartButton = v.findViewById(R.id.capture_start);
        mStopButton = v.findViewById(R.id.capture_stop);
        mIdleText = v.findViewById(R.id.capture_idle_text);
        mProgressText = v.findViewById(R.id.capture_progress_text);
        mStartIcon = v.findViewById(R.id.capture_start_icon);
        mStopIcon = v.findViewById(R.id.capture_stop_icon);

        updateState();

        // handlers
        mStartButton.setOnClickListener(v12 -> startCapture());
        mStopButton.setOnClickListener(v1 -> stopCapture());

        return v;
    }

    void updateState() {
        mStartIcon.setColorFilter(mCaptureActive ? 0 : 0xffd50000);
        mStartButton.setEnabled(!mCaptureActive);
        mStopIcon.setColorFilter(mCaptureActive ? 0xffd50000 : 0);
        mStopButton.setEnabled(mCaptureActive);
        mIdleText.setVisibility(mCaptureActive ? View.GONE : View.VISIBLE);
        mProgressText.setVisibility(mCaptureActive ? View.VISIBLE : View.GONE);
    }

    void startCapture() {
        mCaptureActive = true;
        updateState();

        getNfc().setCaptureEnabled(true);
    }

    void stopCapture() {
        mCaptureActive = false;
        updateState();

        getNfc().setCaptureEnabled(false);
    }

    public static NfcComm fromBundle(Bundle b) {
        String type = b.getString("type");
        long timestamp = b.getLong("timestamp");

        if ("INITIAL".equals(type)) {
            Tag initial = b.getParcelable("data");
            byte[] data = initial != null ? NFCTagReader.create(initial).getConfig().build() : new byte[0];
            return new NfcComm(true, true, data, timestamp);
        }
        else {
            byte[] data = b.getByteArray("data");
            return new NfcComm("TAG".equals(type), false, data, timestamp);
        }
    }

}
