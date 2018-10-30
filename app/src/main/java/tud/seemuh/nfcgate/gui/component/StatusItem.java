package tud.seemuh.nfcgate.gui.component;

public class StatusItem {
    public enum State {
        OK,
        WARN,
        ERROR
    }

    // state
    private String mName;
    private String mValue;
    private String mMessage;
    private State mState;

    public StatusItem(String name) {
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

    public StatusItem setValue(boolean yesNo) {
        return setValue(yesNo ? "Yes" : "No");
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