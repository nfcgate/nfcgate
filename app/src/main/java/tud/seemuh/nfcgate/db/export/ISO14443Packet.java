package tud.seemuh.nfcgate.db.export;

import java.io.DataOutputStream;
import java.io.IOException;

public class ISO14443Packet implements PcapWriteableObject {
    public static final int DATA_PICC_TO_PCD_CRC_DROPPED = 0xFB;
    public static final int DATA_PCD_TO_PICC_CRC_DROPPED = 0xFA;
    public static final int HEADER_LEN = 4;

    protected final PcapPacket mPacket;

    public ISO14443Packet(PcapPacket packet) {
        mPacket = packet;
    }

    @Override
    public int write(DataOutputStream out) throws IOException {
        final byte[] data = mPacket.getData().getData();

        // ISO 14443 header (4 bytes)
        // version
        out.writeByte(0);
        // event
        out.writeByte(mPacket.mData.isCard() ? DATA_PICC_TO_PCD_CRC_DROPPED :
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
