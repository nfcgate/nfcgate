package tud.seemuh.nfcgate.util;

import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.nfc.config.ConfigOption;

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

    // Contains the config options
    ConfigBuilder mConfig = null, mConfig_prefilter = null;

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
     */
    public NfcComm (ConfigBuilder config) {
        mSource = Source.CARD;
        mType = Type.AnticolBytes;
        mConfig = config;
    }

    /**
     * Instantiate an NfcComm object for Anticollision data
     */
    public NfcComm(ConfigBuilder config, ConfigBuilder config_pf) {
        mSource = Source.CARD;
        mType   = Type.AnticolBytes;
        mConfig = config;

        mConfig_prefilter = config_pf;
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

    public boolean isChanged() {
        return filterChanged;
    }

    // Getters for prefilter data
    // TODO Use these in the Sink implementation, where appropriate
    public byte[] getOldData() {
        return mBytes_prefilter;
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
            sb.append(mConfig.toString());

            if (isChanged()) {
                sb.append("Prefilter: \n");
                sb.append(mConfig_prefilter.toString());
            }
        }
        return sb.toString();
    }
}
