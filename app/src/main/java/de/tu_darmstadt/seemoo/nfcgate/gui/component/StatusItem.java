package de.tu_darmstadt.seemoo.nfcgate.gui.component;

import android.content.Context;

import de.tu_darmstadt.seemoo.nfcgate.R;

public class StatusItem {
    public enum State {
        OK,
        WARN,
        ERROR
    }

    private final Context mContext;

    // state
    private final String mName;
    private String mValue;
    private String mMessage;
    private State mState;

    public StatusItem(Context context, String name) {
        mContext = context;
        mName = name;
        mState = State.OK;
    }

    public String getName() {
        return mName;
    }

    public String getValue() {
        return mValue;
    }

    public String getMessage() {
        return mMessage;
    }

    public State getState() {
        return mState;
    }

    public StatusItem setValue(String value) {
        mValue = value;
        return this;
    }

    public StatusItem setValue(boolean yes) {
        return setValue(mContext.getString(yes ? R.string.status_yes : R.string.status_no));
    }

    public void setWarn(String message) {
        mState = State.WARN;
        mMessage = message;
    }

    public void setError(String message) {
        mState = State.ERROR;
        mMessage = message;
    }

    @Override
    public String toString() {
        return mName + ": " + mValue;
    }
}
