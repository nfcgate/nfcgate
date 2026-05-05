package de.tu_darmstadt.seemoo.nfcgate.nfc.config;

import de.tu_darmstadt.seemoo.nfcgate.util.Utils;

/**
 * Represents a single NCI configuration option with an option code, its length and data
 */
public class ConfigOption {
    private final OptionType mType;
    private final byte[] mData;

    public ConfigOption(OptionType type, byte[] data) {
        mType = type;
        mData = data;
    }

    public ConfigOption(OptionType type, byte data) {
        this(type, new byte[] { data });
    }

    public OptionType getType() {
        return mType;
    }
    public byte[] getData() {
        return mData;
    }
    public int len() {
        return mData.length;
    }

    public void push(byte[] data, int offset) {
        data[offset] = mType.getID();
        data[offset + 1] = (byte)mData.length;

        System.arraycopy(mData, 0, data, offset + 2, mData.length);
    }

    @Override
    public String toString() {
        String comment = ConfigCommenter.comment(this);
        String commentLine = comment != null && !comment.isBlank() ? "\n  " + comment : "";

        if (mData.length == 1)
            return String.format("Type: %s, Value: %s%s", mType, Utils.bytesToHex(mData), commentLine);

        return String.format("Type: %s (%d), Value: %s%s", mType, mData.length, Utils.bytesToHex(mData), commentLine);
    }
}
