package tud.seemuh.nfcgate.gui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.animation.CircleAngleAnimation;
import tud.seemuh.nfcgate.gui.view.CircleView;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.nfc.hce.DaemonConfiguration;
import tud.seemuh.nfcgate.util.NfcComm;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BasicCloneActivity extends Activity {
    private static final String TAG = "BasicCloneActivity";

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    // animation duration to fill the circle with green
    private static final int UI_GREEN_FILL_MILLIS = 1000;
    // Time to wait until starting the evil pressing countdown of doom
    private static final int UI_WAIT_COUNTDOWN_MILLIS = 30000;
    // animation duration for retreating green circle until reset
    private static final int UI_RESET_COUNTDOWN_MILLIS = 30000;
    // animation duration to recolor the background from red to white
    private static final int UI_RECOLOR_BACKGROUND_MILLIS = 500;

    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private CircleView mCircleView;
    private TextView mAdditionalTextView;
    private ImageView mBrandingView;

    private Handler mCountdownHandler;
    private AnimationDrawable mAnimDrawable;

    private PendingIntent mPendingIntent;
    private NfcAdapter mAdapter;

    private NfcManager mNfcManager;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Display ActionBar - Commented out because we don't need the action bar
            // Delayed display of UI elements
            //ActionBar actionBar = getActionBar();
            //if (actionBar != null) {
            //    actionBar.show();
            //}
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize Daemon configuration
        DaemonConfiguration.Init(this);

        Log.d(TAG, "Activity started");

        setContentView(R.layout.activity_basic_clone);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);

        mCircleView = (CircleView) findViewById(R.id.circleView);
        mAdditionalTextView = (TextView) findViewById(R.id.simpleui_additional_text);
        mBrandingView = (ImageView) findViewById(R.id.branding_view);

        mNfcManager = NfcManager.getInstance();
        mNfcManager.setContext(this);
        mNfcManager.start();
        mNfcManager.setCloneModeEnabled();

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //toggle();
            }
        });

        // Cycle through branding images
        mAnimDrawable = new AnimationDrawable();
        mAnimDrawable.addFrame(getResources().getDrawable(R.drawable.crisp_negative), 5000);
        mAnimDrawable.addFrame(getResources().getDrawable(R.drawable.tud_negative), 5000);
        mAnimDrawable.addFrame(getResources().getDrawable(R.drawable.cysec_negative), 5000);
        mAnimDrawable.addFrame(getResources().getDrawable(R.drawable.seemoo_negative), 5000);
        mAnimDrawable.setEnterFadeDuration(1000);
        mAnimDrawable.setExitFadeDuration(1000);
        mAnimDrawable.setOneShot(false);
        mBrandingView.setImageDrawable(mAnimDrawable);



        // Prepare PendingIntent to catch NFC intents if the app is already running
        // Based on https://developer.android.com/guide/topics/connectivity/nfc/advanced-nfc.html#foreground-dispatch
        mPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        // findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Enable NFC Tag discovery foreground dispatch
        mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);

        // Check if we were started with an intent, and pass the Tag on if it was a TECH_DISCOVERED intent
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(this.getIntent().getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(this.getIntent().getAction())) {
            Log.i(TAG, "onResume(): starting onNewIntent()...");
            this.onNewIntent(this.getIntent());
        }

        // Start branding image cycler
        mAnimDrawable.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent(): started");
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Log.i(TAG, "Discovered tag with intent: " + intent);
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            mNfcManager.setTag(tag);
            mNfcManager.setAnticolData(mNfcManager.getAnticolData());

            //This toast is very useful for debugging, DONT delete it!!!
            // Toast.makeText(this, "Found Tag", Toast.LENGTH_SHORT).show();
            animSetCardDiscovered();
        } else {
            Log.d(TAG, "onNewIntent(): intent action = " + intent.getAction());
        }
    }

    private void animSetCardDiscovered() {
        // Animate circle
        CircleAngleAnimation animation = new CircleAngleAnimation(mCircleView, 360);
        animation.setDuration(UI_GREEN_FILL_MILLIS);
        mAdditionalTextView.setText("");
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCircleView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        animResetCardStatus();
                        return false;
                    }
                });
                animWaitForCountdown();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing
            }
        });
        mCircleView.startAnimation(animation);

        ((TextView)mContentView).setText(R.string.simpleclone_touch_lock);
    }

    private void animWaitForCountdown() {
        // End of circle turning green
        mCircleView.setCircleBackgroundColor(Color.RED);
        // Wait 30 seconds before starting the reset countdown
        mCountdownHandler = new Handler();
        mCountdownHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animStartCountdown();
            }
        }, UI_WAIT_COUNTDOWN_MILLIS);

    }

    private void animStartCountdown() {
        // Set description text and hide branding view
        mAdditionalTextView.setText(R.string.simpleclone_touch_reset);
        Animation fadein = new AlphaAnimation(0, 1);
        fadein.setDuration(1000);
        mAdditionalTextView.startAnimation(fadein);

        Animation fadeout = new AlphaAnimation(1, 0);
        fadeout.setInterpolator(new LinearInterpolator());
        fadeout.setStartOffset(0);
        fadeout.setDuration(1000);
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBrandingView.setVisibility(View.GONE);
                mAnimDrawable.stop();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing
            }
        });
        mBrandingView.startAnimation(fadeout);

        // Prepare animation
        CircleAngleAnimation anim2 = new CircleAngleAnimation(mCircleView, 0);
        anim2.setInterpolator(new LinearInterpolator());
        // Start reset countdown
        anim2.setDuration(UI_RESET_COUNTDOWN_MILLIS);
        anim2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animResetCardStatus();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing
            }
        });
        mCircleView.startAnimation(anim2);
    }

    private void animResetCardStatus() {
        // Cancel all pending animations
        mCircleView.animate().cancel();
        mCircleView.clearAnimation();
        // Reset angle to zero
        mCircleView.setAngle(0.0f);
        // Stop any pending delayed operations
        mCountdownHandler.removeCallbacksAndMessages(null);

        // Reset emulated anticol values
        mNfcManager.setAnticolData(new NfcComm(
                new byte[] {(byte)0x44, (byte)0x03},
                (byte) 0x20,
                new byte[] {(byte)0x80},
                new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}
        ));

        // Animate color change of circle
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.RED, Color.WHITE);
        colorAnimation.setDuration(UI_RECOLOR_BACKGROUND_MILLIS);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCircleView.setCircleBackgroundColor((int) valueAnimator.getAnimatedValue());
                mCircleView.invalidate();
            }
        });
        colorAnimation.start();
        // Set description texts correctly
        ((TextView)mContentView).setText(R.string.simpleclone_touch_card);
        Animation fadeout = new AlphaAnimation(1, 0);
        fadeout.setDuration(1000);
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAdditionalTextView.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mAdditionalTextView.startAnimation(fadeout);
        mBrandingView.setVisibility(View.VISIBLE);
        Animation fadein = new AlphaAnimation(0, 1);
        fadein.setDuration(1000);
        fadein.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimDrawable.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing
            }
        });
        mBrandingView.startAnimation(fadein);

    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
