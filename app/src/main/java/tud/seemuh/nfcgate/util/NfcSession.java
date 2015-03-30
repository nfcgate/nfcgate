package tud.seemuh.nfcgate.util;

/**
 * Created by max on 30.03.15.
 */
public class NfcSession {
    private String mDate;
    private String mName;
    private int mID;

    public NfcSession(String date, int id) {
        mDate = date;
        mID = id;
    }

    public NfcSession(String date, int id, String name) {
        mDate = date;
        mName = name;
        mID = id;
    }

    @Override
    public String toString() {
        if (mName != null) {
            return mName + " (" + mDate + ")";
        } else {
            return mDate;
        }
    }

    public int getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public String getDate() {
        return mDate;
    }
}
