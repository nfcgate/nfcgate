package de.tu_darmstadt.seemoo.nfcgate.network.data;

public class SendRecord {
    private final int mSession;
    private final byte[] mData;

    public SendRecord(int session, byte[] data) {
        mSession = session;
        mData = data;
    }

    public int getSession() {
        return mSession;
    }

    public byte[] getData() {
        return mData;
    }
}
