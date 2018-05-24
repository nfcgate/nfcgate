package tud.seemuh.nfcgate.network;

public class SendRecord {
    private int mSession;
    private byte[] mData;

    SendRecord(int session, byte[] data) {
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