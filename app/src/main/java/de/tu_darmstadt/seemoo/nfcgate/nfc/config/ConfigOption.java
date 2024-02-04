package de.tu_darmstadt.seemoo.nfcgate.nfc.config;

import de.tu_darmstadt.seemoo.nfcgate.util.Utils;

/**
 * Represents a single NCI configuration option with an option code, its length and data
 */
public class ConfigOption {
    private final OptionType mID;
    private final byte[] mData;

    ConfigOption(OptionType ID, byte[] data) {
        mID = ID;
        mData = data;
    }

    ConfigOption(OptionType ID, byte data) {
        this(ID, new byte[] { data });
    }

    public int len() {
        return mData.length;
    }

    public void push(byte[] data, int offset) {
        data[offset + 0] = mID.getID();
        data[offset + 1] = (byte)mData.length;

        System.arraycopy(mData, 0, data, offset + 2, mData.length);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("Type: ");
        result.append(mID.toString());

        if (mData.length > 1) {
            result.append(" (");
            result.append(mData.length);
            result.append(")");
        }

        result.append(", Value: 0x");
        result.append(Utils.bytesToHex(mData));

        return result.toString();
    }
}
