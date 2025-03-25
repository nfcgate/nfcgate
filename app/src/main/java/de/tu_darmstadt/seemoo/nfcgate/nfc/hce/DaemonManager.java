package de.tu_darmstadt.seemoo.nfcgate.nfc.hce;

import android.content.Intent;
import android.os.Bundle;

import java.util.Date;

import de.tu_darmstadt.seemoo.nfcgate.gui.MainActivity;

/**
 * Interface to the nfc daemon patches
 */
public class DaemonManager {
    private final MainActivity mActivity;
    private boolean mIsHookEnabled = false;
    private Date mLastResponse = null;

    public DaemonManager(MainActivity activity) {
        mActivity = activity;
    }

    /**
     * Receives daemon response intents
     */
    public void onResponse(Intent intent) {
        String responseType = intent.getStringExtra("type");

        if ("CAPTURE".equals(responseType))
            mActivity.importCapture(intent.<Bundle>getParcelableArrayListExtra("capture"));
        else if ("HOOK_STATUS".equals(responseType)) {
            mIsHookEnabled = intent.getBooleanExtra("hookEnabled", false);
            mActivity.getNfc().notifyStatusChanged();
            mLastResponse = new Date();
        }
    }

    public boolean isHookEnabled() {
        return mIsHookEnabled;
    }

    /**
     * Sets the config in the NFC Service hook
     *
     * @param config A config stream, enables the hook
     */
    public void beginSetConfig(byte[] config) {
        send(getIntent("SET_CONFIG").putExtra("config", config));
    }

    /**
     * Resets the config and polling
     */
    public void beginResetConfig() {
        send(getIntent("RESET_CONFIG"));
    }

    /**
     * Sets the polling state
     *
     * @param enabled True enables polling, false disables it
     */
    public void beginSetPolling(boolean enabled) {
        send(getIntent("SET_POLLING").putExtra("enabled", enabled));
    }

    /**
     * Enables or disables on-device capture
     *
     * @param enabled True enables on-device capture, false disables it
     */
    public void beginSetCapture(boolean enabled) {
        send(getIntent("SET_CAPTURE").putExtra("enabled", enabled));
    }

    /**
     * Lazily installs hooks if needed, receives the current hook status
     */
    public void beginInstallHooks() {
        send(getIntent("INSTALL_HOOKS"));
    }

    public void onResume() {
        // debounce getting hook status because receiving the response also triggers onResume
        if (mLastResponse == null || (new Date().getTime() - mLastResponse.getTime()) > 5000)
            beginInstallHooks();
    }

    private Intent getIntent(String op) {
        return new Intent()
                .setAction("de.tu_darmstadt.seemoo.nfcgate.daemoncall")
                .putExtra("op", op);
    }

    private void send(Intent intent) {
        mActivity.sendBroadcast(intent);
    }
}
