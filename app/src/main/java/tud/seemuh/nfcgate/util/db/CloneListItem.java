package tud.seemuh.nfcgate.util.db;


import tud.seemuh.nfcgate.util.NfcComm;

public class CloneListItem {

    private int mId;
    private String mName;
    private CloneListStorage mStorage;
    private NfcComm mAnticol;

    public CloneListItem(NfcComm anticol, String name, int id) {
        mId = id;
        mName = name;
        mAnticol = anticol;
    }

    public CloneListItem(NfcComm anticol, String name) {
        mName = name;
        mAnticol = anticol;
    }

    public String toString() {
        return mName;
    }

    public int getId() {
        return mId;
    }

    public NfcComm getAnticolData() {
        return mAnticol;
    }

}
