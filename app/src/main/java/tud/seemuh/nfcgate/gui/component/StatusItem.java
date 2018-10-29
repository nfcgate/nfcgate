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

    public State getState() {
        return mState;
    }

    public StatusItem setState(State state) {
        mState = state;
        return this;
    }

    public StatusItem setValue(String value) {
        mValue = value;
        return this;
    }

    public StatusItem setValue(boolean yesNo) {
        return setValue(yesNo ? "Yes" : "No");
    }
}