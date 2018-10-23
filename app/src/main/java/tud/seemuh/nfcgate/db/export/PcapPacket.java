package tud.seemuh.nfcgate.db.export;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import tud.seemuh.nfcgate.util.NfcComm;

public class PcapPacket implements PcapWriteableObject {
    protected final NfcComm mData;
    protected final ISO14443Packet mISOPacket;
    public static final int HEADER_LEN = 16;

    public PcapPacket(NfcComm data) {
        mData = data;
        mISOPacket = new ISO14443Packet(this);
    }

    @Override
    public int write(DataOutputStream out) throws IOException {
        final long secs = mData.getTimestamp() / 1000;
        final long usecs = mData.getTimestamp() * 1000;

        // timestamp
        out.writeInt((int) secs);
        out.writeInt((int) usecs);

        // get data of containing ISO 14443 packet
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream packetOut = new DataOutputStream(byteOut);
        int written = mISOPacket.write(packetOut);
        packetOut.close();
        byte[] isoPacket = byteOut.toByteArray();

        // packet length (original)
        out.writeInt(written);
        // packet length (actual)
        out.writeInt(written);
        // data
        out.write(isoPacket);

        return HEADER_LEN + written;
    }

    public NfcComm getData() {
        return mData;
    }
}
