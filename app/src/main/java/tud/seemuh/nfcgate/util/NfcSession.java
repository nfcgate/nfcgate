package tud.seemuh.nfcgate.util;

/**
 * Data container for Session Data.
 */
public class NfcSession {
    private String mDate;
    private String mName;
    private long mID;

    public NfcSession(String date, long id) {
        mDate = date;
        mID = id;
    }

    public NfcSession(String date, long id, String name) {
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

    public long getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public String getDate() {
        return mDate;
    }
}
