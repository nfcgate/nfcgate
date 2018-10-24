package tud.seemuh.nfcgate.db.pcap;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import tud.seemuh.nfcgate.util.NfcComm;

public class PcapPacket implements PcapWriteableObject, PcapReadableObject {
    public static final int HEADER_LEN = 16;
    protected final ISO14443Packet mISOPacket;
    protected NfcComm mData;

    public PcapPacket() {
        mISOPacket = new ISO14443Packet(this);
    }

    public PcapPacket(NfcComm data) {
        mData = data;
        mISOPacket = new ISO14443Packet(this);
    }

    @Override
    public NfcComm read(DataInputStream in) throws IOException {
        // TODO: recover timestamp and set it in NfcComm

        // sec(4), usec(4), orig len(4), act len(4)
        in.skipBytes(16);

        return mISOPacket.read(in);
    }

    @Override
    public int write(DataOutputStream out) throws IOException {
        final long secs = mData.getTimestamp() / 1000;
        final long usecs = mData.getTimestamp() * 1000; // FIXME

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
