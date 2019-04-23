package de.tu_darmstadt.seemoo.nfcgate.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import de.tu_darmstadt.seemoo.nfcgate.network.c2c.C2C.NFCData;

/**
 * The NfcComm-Class provides an object to store NFC bytes and information about them.
 * It is used to pass information to Sinks, including metadata like the source of the bytes.
 */
public class NfcComm {
    private NFCData mData;

    /**
     * Instantiate a NfcComm object for regular NFC Traffic
     */
    public NfcComm(boolean fromCard, boolean isInitial, byte[] data) {
        this(fromCard, isInitial, data, System.currentTimeMillis());
    }

    /**
     * Instantiate a NfcComm object for regular NFC Traffic
     */
    public NfcComm(boolean fromCard, boolean isInitial, byte[] data, long timestamp) {
        mData = NFCData.newBuilder()
                .setDataSource(fromCard ? NFCData.DataSource.CARD : NFCData.DataSource.READER)
                .setDataType(isInitial ? NFCData.DataType.INITIAL : NFCData.DataType.CONTINUATION)
                .setTimestamp(timestamp)
                .setData(ByteString.copyFrom(data))
                .build();
    }

    /**
     * Instantiate a NfcComm object from serialized data
     */
    public NfcComm(byte[] data) {
        try {
            mData = NFCData.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * True if initial data, false on continuation
     */
    public boolean isInitial() {
        return mData.getDataType() == NFCData.DataType.INITIAL;
    }

    /**
     * True if card source, false on reader source
     */
    public boolean isCard() {
        return mData.getDataSource() == NFCData.DataSource.CARD;
    }

    /**
     * Unix timestamp in millis
     */
    public long getTimestamp() {
        return mData.getTimestamp();
    }

    /**
     * Returns the binary config stream if initial or APDU if continuation
     */
    public byte[] getData() {
        return mData.getData().toByteArray();
    }

    /**
     * Returns serialized NFCData
     */
    public byte[] toByteArray() {
        return mData.toByteArray();
    }

    @Override
    public String toString() {
        String letter = isCard() ? "C" : "R";
        String initial = isInitial() ? "(initial) " : "";
        String data = Utils.bytesToHex(getData());
        return String.format("%s: %s%s", letter, initial, data);
    }
}
