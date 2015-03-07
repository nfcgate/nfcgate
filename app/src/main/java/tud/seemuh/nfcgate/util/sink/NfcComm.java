package tud.seemuh.nfcgate.util.sink;

/**
 * The NfcComm-Class provides an object to store NFC bytes and information about them.
 * It is used to pass information to Sinks, including metadata like the source of the bytes.
 */
public class NfcComm {
    public enum Source {
        HCE,
        CARD
    }

    public enum Type {
        NFCBytes,
        AnticolBytes
    }

    private Source mSource;
    private Type mType;
    private byte[] mBytes;

    private byte[] mAtqa;
    private byte mSak;
    private byte[] mHist;
    private byte[] mUid;

    /**
     * Instantiate an NfcComm object for regular NFC Traffic
     * @param source The source of the NFC data, as chosen from the Enum
     * @param data The raw data itself
     */
    public NfcComm (Source source, byte[] data) {
        mSource = source;
        mType = Type.NFCBytes;
        mBytes = data;
    }

    /**
     * Instantiate an NfcComm object for Anticollision data
     * @param atqa Anticol protocol ATQA data
     * @param sak Anticol protocol sak data
     * @param hist Antocol protocol Historical byte
     * @param uid Anticol protocol UID
     */
    public NfcComm (byte[] atqa, byte sak, byte[] hist, byte[] uid) {
        mSource = Source.CARD;
        mType = Type.AnticolBytes;
        mAtqa = atqa;
        mSak = sak;
        mHist = hist;
        mUid = uid;
    }

    // What follows are regular getters for the variables
    // It is the responsibility of the Sink implementation to check if the return value is not null.
    public Type getType() {
        return mType;
    }

    public Source getSource() {
        return mSource;
    }

    public byte[] getData() {
        return mBytes;
    }

    public byte[] getAtqa() {
        return mAtqa;
    }

    public byte getSak() {
        return mSak;
    }

    public byte[] getHist() {
        return mHist;
    }

    public byte[] getUid() {
        return mUid;
    }
}
