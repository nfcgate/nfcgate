package tud.seemuh.nfcgate.util;

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
    // Contains the used bytes
    private byte[] mBytes;
    // Contains the bytes before the filter process on this device
    private byte[] mBytes_prefilter;

    // Contains the used (post-filtering) anticol data
    private byte[] mAtqa;
    private byte mSak;
    private byte[] mHist;
    private byte[] mUid;

    // Contains the Anticol data before filtering
    private byte[] mAtqa_prefilter;
    private byte mSak_prefilter;
    private byte[] mHist_prefilter;
    private byte[] mUid_prefilter;

    /**
     * Instantiate an NfcComm object for regular NFC Traffic
     * @param source The source of the NFC data, as chosen from the Enum
     * @param data The raw data itself
     */
    public NfcComm (Source source, byte[] data) {
        mSource = source;
        mType = Type.NFCBytes;
        mBytes = data;
        mBytes_prefilter = data;
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
        mAtqa = mAtqa_prefilter = atqa;
        mSak = mSak_prefilter = sak;
        mHist = mHist_prefilter = hist;
        mUid = mUid_prefilter = uid;
    }

    // Setters for postfilter data
    public void setData(byte[] data) {
        mBytes = data;
    }

    public void setAtqa(byte[] atqa) {
        mAtqa = atqa;
    }

    public void setSak(byte sak) {
        mSak = sak;
    }

    public void setHist(byte[] hist) {
        mHist = hist;
    }

    public void setUid(byte[] uid) {
        mUid = uid;
    }

    // Variable getters
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

    // Getters for prefilter data
    public byte[] getOldData() {
        return mBytes_prefilter;
    }

    public byte[] getOldAtqa() {
        return mAtqa_prefilter;
    }

    public byte getOldSak() {
        return mSak_prefilter;
    }

    public byte[] getOldHist() {
        return mHist_prefilter;
    }

    public byte[] getOldUid() {
        return mUid_prefilter;
    }
}
