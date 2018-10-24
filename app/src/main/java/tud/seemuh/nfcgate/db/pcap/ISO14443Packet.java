package tud.seemuh.nfcgate.db.pcap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import tud.seemuh.nfcgate.util.NfcComm;

public class ISO14443Packet implements PcapWriteableObject, PcapReadableObject {
    public static final byte DATA_PICC_TO_PCD_CRC_DROPPED = (byte) 0xFB;
    public static final byte DATA_PCD_TO_PICC_CRC_DROPPED = (byte) 0xFA;
    public static final byte LINKTYPE = (byte) 264;
    public static final int HEADER_LEN = 4;

    protected final PcapPacket mPacket;

    public ISO14443Packet(PcapPacket packet) {
        mPacket = packet;
    }

    @Override
    public NfcComm read(DataInputStream in) throws IOException {
        // version
        in.skipBytes(1);
        // event
        boolean isCard = in.readByte() == DATA_PICC_TO_PCD_CRC_DROPPED;
        // length
        short length = in.readShort();
        // PCB
        in.skipBytes(1);

        byte[] data = new byte[length - 1];
        in.read(data, 0, data.length);

        return new NfcComm(isCard, false, data);
    }

    @Override
    public int write(DataOutputStream out) throws IOException {
        final byte[] data = mPacket.getData().getData();

        // ISO 14443 header (4 bytes)
        // version
        out.writeByte(0);
        // event
        out.writeByte(mPacket.getData().isCard() ? DATA_PICC_TO_PCD_CRC_DROPPED :
                DATA_PCD_TO_PICC_CRC_DROPPED);
        // len (data len + 1 byte for I_BLOCK PCB)
        out.writeShort(data.length + 1);

        // part of frame
        // I_BLOCK PCB: 0000010
        out.writeByte(0x02);

        // actual data
        out.write(data);

        out.close();

        return HEADER_LEN + 1 + data.length;
    }
}
