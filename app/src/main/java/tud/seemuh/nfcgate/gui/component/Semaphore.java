package tud.seemuh.nfcgate.gui.component;

import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.MainActivity;

public class Semaphore {
    // state for each color
    public enum State {
        GREEN,
        YELLOW,
        RED,
        IDLE
    }

    // UI references
    private RelativeLayout mSemaphore;
    private TextView mSemaphoreText;

    public Semaphore(MainActivity act) {
        // get components
        mSemaphore = act.findViewById(R.id.tag_semaphore);
        mSemaphoreText = act.findViewById(R.id.tag_semaphore_text);

        // enable semaphore and set default
        mSemaphore.setVisibility(View.VISIBLE);
        reset();
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
            case RED: return R.color.semaphore_red;
            case YELLOW: return R.color.semaphore_yellow;
            case GREEN: return R.color.semaphore_green;
            default: case IDLE: return R.color.semaphore_idle;
        }
    }

    public void set(State state, String message) {
        mSemaphore.setBackgroundResource(backgroundByState(state));
        mSemaphoreText.setTextColor(colorByState(state));
        mSemaphoreText.setText("Network: " + message);
    }

    public void reset() {
        set(State.IDLE, "Idle");
    }
}
