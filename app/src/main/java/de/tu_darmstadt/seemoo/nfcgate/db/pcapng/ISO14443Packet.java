package de.tu_darmstadt.seemoo.nfcgate.db.pcapng;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.tu_darmstadt.seemoo.nfcgate.db.pcapng.base.PcapPacket;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class ISO14443Packet extends PcapPacket {
    private static final byte DATA_PICC_TO_PCD_CRC_DROPPED = (byte) 0xFB;
    private static final byte DATA_PCD_TO_PICC_CRC_DROPPED = (byte) 0xFA;

    private NfcComm mData;

    public ISO14443Packet() {
        // read mode
    }

    public ISO14443Packet(NfcComm data) {
        // write mode
        mData = data;
    }

    public NfcComm getData() {
        return mData;
    }

    @Override
    public PcapPacket read(DataInputStream in) throws IOException {
        // read packet header including timestamp and payload
        super.read(in);

        // read payload
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mPayload);
        try (final DataInputStream packetIn = new DataInputStream(inputStream)) {

            // version
            packetIn.skipBytes(1);
            // event
            boolean isCard = packetIn.readByte() == DATA_PICC_TO_PCD_CRC_DROPPED;
            // length
            short length = packetIn.readShort();
            // PCB
            packetIn.skipBytes(1);
            // data
            byte[] data = new byte[length - 1];
            packetIn.read(data, 0, data.length);

            mData = new NfcComm(isCard, mInterfaceIndex == 1, data, mTimestamp);
        }

        return this;
    }

    @Override
    public int write(DataOutputStream out) throws IOException {
        // prepare payload
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (final DataOutputStream packetOut = new DataOutputStream(byteOut)) {
            final byte[] data = mData.getData();

            // ISO 14443 header (4 bytes)
            // version
            packetOut.writeByte(0);
            // event
            packetOut.writeByte(mData.isCard() ? DATA_PICC_TO_PCD_CRC_DROPPED :
                    DATA_PCD_TO_PICC_CRC_DROPPED);
            // len (data len + 1 byte for I_BLOCK PCB)
            packetOut.writeShort(data.length + 1);

            // part of frame
            // I_BLOCK PCB: 0000010
            packetOut.writeByte(0x02);
            // actual data
            packetOut.write(data);
        }

        mInterfaceIndex = mData.isInitial() ? 1 : 0;
        mPayload = byteOut.toByteArray();
        mTimestamp = mData.getTimestamp();
        return super.write(out);
    }
}
