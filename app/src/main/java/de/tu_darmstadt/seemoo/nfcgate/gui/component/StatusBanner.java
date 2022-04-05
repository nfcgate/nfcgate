package de.tu_darmstadt.seemoo.nfcgate.gui.component;

import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.tu_darmstadt.seemoo.nfcgate.R;
import de.tu_darmstadt.seemoo.nfcgate.gui.MainActivity;

public class StatusBanner {
    // state for each color
    public enum State {
        GREEN,
        YELLOW,
        RED,
        IDLE
    }

    // UI references
    private RelativeLayout mBanner;
    private TextView mBannerText;

    public StatusBanner(MainActivity act) {
        // get components
        mBanner = act.findViewById(R.id.banner);
        mBannerText = act.findViewById(R.id.banner_text);
    }

    private int colorByState(State state) {
        switch (state) {
            default:
            case IDLE:
                return 0xFF000000;
            case RED:
            case YELLOW:
            case GREEN:
                return 0xFFFFFFFF;
        }
    }

    @DrawableRes
    private int backgroundByState(State state) {
        switch (state) {
            case RED: return R.color.status_red;
            case YELLOW: return R.color.status_yellow;
            case GREEN: return R.color.status_green;
            default: case IDLE: return R.color.status_idle;
        }
    }

    public void set(State state, String message) {
        mBanner.setBackgroundResource(backgroundByState(state));
        mBannerText.setTextColor(colorByState(state));
        mBannerText.setText(message);
        setVisibility(true);
    }

    public void setWarning(String message) {
        set(State.YELLOW, "Warning: " + message);
    }

    public void setError(String message) {
        set(State.RED, "Error: " + message);
    }

    public void setVisibility(boolean visible) {
        mBanner.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void reset() {
        setVisibility(false);
    }
}
