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

    // Date (needed for Session logging display)
    private String mDate;

    private boolean filterChanged = false;

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
     * Instantiate an NfcComm object for regular NFC Traffic
     * @param source The source of the NFC data, as chosen from the Enum
     * @param data The raw data itself
     * @param data_pf The raw data, before it was changed by a filter
     */
    public NfcComm (Source source, byte[] data, byte[] data_pf) {
        mSource = source;
        mType = Type.NFCBytes;
        mBytes = data;
        mBytes_prefilter = data_pf;

        filterChanged = true;
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

    /**
     * Instantiate an NfcComm object for Anticollision data
     * @param atqa Anticol protocol ATQA data
     * @param sak Anticol protocol sak data
     * @param hist Antocol protocol Historical byte
     * @param uid Anticol protocol UID
     * @param atqa_pf Anticol protocol ATQA data, before change by filter
     * @param sak_pf Anticol protocol sak data, before change by filter
     * @param hist_pf Antocol protocol Historical byte, before change by filter
     * @param uid_pf Anticol protocol UID, before change by filter
     */
    public NfcComm(byte[] atqa, byte sak, byte[] hist, byte[] uid,
                   byte[] atqa_pf, byte sak_pf, byte[] hist_pf, byte[] uid_pf) {
        mSource = Source.CARD;
        mType   = Type.AnticolBytes;
        mAtqa   = atqa;
        mSak    = sak;
        mHist   = hist;
        mUid    = uid;

        mAtqa_prefilter = atqa_pf;
        mSak_prefilter  = sak_pf;
        mHist_prefilter = hist_pf;
        mUid_prefilter  = uid_pf;

        filterChanged = true;
    }

    public void setDate(String date) {
        mDate = date;
    }
    // Setters for postfilter data
    public void setData(byte[] data) {
        mBytes = data;
        filterChanged = true;
    }

    public void setAtqa(byte[] atqa) {
        mAtqa = atqa;
        filterChanged = true;
    }

    public void setSak(byte sak) {
        mSak = sak;
        filterChanged = true;
    }

    public void setHist(byte[] hist) {
        mHist = hist;
        filterChanged = true;
    }

    public void setUid(byte[] uid) {
        mUid = uid;
        filterChanged = true;
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

    public boolean isChanged() {
        return filterChanged;
    }

    // Getters for prefilter data
    // TODO Use these in the Sink implementation, where appropriate
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mType == Type.NFCBytes) {
            if (mSource == Source.HCE) {
                sb.append("R: ");
            } else {
                sb.append("C: ");
            }
            if (isChanged()) {
                sb.append(Utils.bytesToHex(mBytes) + " (" + Utils.bytesToHex(mBytes_prefilter) + ")");
            } else {
                sb.append(Utils.bytesToHex(mBytes));
            }
        } else {
            if (isChanged()) {
                sb.append("Card data: UID: ");
                sb.append(Utils.bytesToHex(mUid));
                sb.append(" (" + Utils.bytesToHex(mUid_prefilter) + ") - SAK: ");
                sb.append(Utils.bytesToHex(mSak) + " (" + Utils.bytesToHex(mSak_prefilter) + ") - ATQA: ");
                sb.append(Utils.bytesToHex(mAtqa) + " (" + Utils.bytesToHex(mAtqa_prefilter) + ") - Hist: ");
                sb.append(Utils.bytesToHex(mHist) + " (" + Utils.bytesToHex(mHist_prefilter) + ")");
            } else {
                sb.append("Card data: UID: ");
                sb.append(Utils.bytesToHex(mUid));
                sb.append(" - SAK: ");
                sb.append(Utils.bytesToHex(mSak));
                sb.append(" - ATQA: ");
                sb.append(Utils.bytesToHex(mAtqa));
                sb.append(" - Hist: ");
                sb.append(Utils.bytesToHex(mHist));
            }
        }
        return sb.toString();
    }
}
