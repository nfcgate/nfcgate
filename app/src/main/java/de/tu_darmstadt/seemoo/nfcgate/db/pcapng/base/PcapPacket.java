package de.tu_darmstadt.seemoo.nfcgate.db.pcapng.base;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PcapPacket {
    protected static final int BLOCK_TYPE_EPB = 6;
    private static final int BLOCK_LEN_EPB = 32;

    protected int mInterfaceIndex = 0;
    protected long mTimestamp = 0;
    protected byte[] mPayload = null;

    protected PcapPacket() {

    }

    public PcapPacket read(DataInputStream in) throws IOException {
        // Enhanced Packet Block
        // block type
        in.skipBytes(4);
        // block length with padding
        int blockLength = in.readInt();
        // interface index
        mInterfaceIndex = in.readInt();
        // timestamp
        int timestampHigh = in.readInt();
        int timestampLow = in.readInt();
        // packet length (original + actual)
        in.skipBytes(4);
        int packetLength = in.readInt();
        // payload
        mPayload = new byte[packetLength];
        in.read(mPayload, 0, packetLength);
        // padding
        in.skipBytes(blockLength - packetLength - BLOCK_LEN_EPB);
        // block length
        in.skipBytes(4);

        // timestamp from microseconds in millis
        mTimestamp = ((long) timestampHigh << 32 | timestampLow & 0xFFFFFFFFL) / 1000;

        return this;
    }

    public int write(DataOutputStream out) throws IOException {
        // prepare timestamp from millis in microseconds
        final long usecs = mTimestamp * 1000;
        final int timestampHigh = (int) (usecs >> 32);
        final int timestampLow = (int) usecs;
        // prepare length + pad to 4 bytes
        final byte[] blockPadding = new byte[(4 - (mPayload.length % 4)) % 4];
        final int blockLength = BLOCK_LEN_EPB + mPayload.length + blockPadding.length;

        // Enhanced Packet Block
        // block type
        out.writeInt(BLOCK_TYPE_EPB);
        // total block length
        out.writeInt(blockLength);
        // interface index
        out.writeInt(mInterfaceIndex);
        // timestamp
        out.writeInt(timestampHigh);
        out.writeInt(timestampLow);
        // packet length (original + actual)
        out.writeInt(mPayload.length);
        out.writeInt(mPayload.length);
        // data + padding
        out.write(mPayload);
        out.write(blockPadding);
        // total block length
        out.writeInt(blockLength);

        return blockLength;
    }
}
