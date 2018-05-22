package tud.seemuh.nfcgate.util;

import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;

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
        Continuation,
        Initial
    }

    private Source mSource;
    private Type mType;
    // Contains the used bytes
    private byte[] mBytes;

    // Contains the config options
    ConfigBuilder mConfig = null;

    // Date (needed for Session logging display)
    private String mDate;

    /**
     * Instantiate an NfcComm object for regular NFC Traffic
     */
    public NfcComm (Source source, byte[] data) {
        mSource = source;
        mType = Type.Continuation;
        mBytes = data;
    }

    /**
     * Instantiate an NfcComm object for initial data
     */
    public NfcComm (ConfigBuilder config) {
        mSource = Source.CARD;
        mType = Type.Initial;
        mConfig = config;
    }

    public void setDate(String date) {
        mDate = date;
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

    public ConfigBuilder getConfig() {
        return mConfig;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mType == Type.Continuation) {
            if (mSource == Source.HCE)
                sb.append("R: ");
            else
                sb.append("C: ");

            sb.append(Utils.bytesToHex(mBytes));
        } else {
            sb.append(mConfig.toString());
        }
        return sb.toString();
    }
}
