package de.tu_darmstadt.seemoo.nfcgate.db.pcapng.base;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.gui.component.FileShare;

public class PcapStream implements FileShare.IFileShareable {
    private static final int BLOCK_TYPE_SECTION = 0x0A0D0D0A;
    private static final int BLOCK_LEN_SECTION = 4*7;
    private static final int BLOCK_TYPE_INTERFACE = 0x00000001;
    private static final int BLOCK_LEN_INTERFACE = 4*5;
    private static final int BYTE_ORDER_MAGIC =  0x1A2B3C4D;

    private final List<PcapPacket> mPackets = new ArrayList<>();
    private final short[] mLinkTypes;

    public PcapStream(short[] linkType) {
        mLinkTypes = linkType;
    }

    public void append(PcapPacket packet) {
        mPackets.add(packet);
    }

    public List<PcapPacket> getPackets() {
        return mPackets;
    }

    public void read(InputStream stream) throws IOException {
        DataInputStream in = new DataInputStream(stream);

        // Section Header Block
        assertEq("block type", BLOCK_TYPE_SECTION, in.readInt());
        assertEq("block len", BLOCK_LEN_SECTION, in.readInt());
        assertEq("byte order magic", BYTE_ORDER_MAGIC, in.readInt());
        assertEq("version (major)", 1, in.readShort());
        assertEq("version (minor)", 0, in.readShort());
        // ignore section length
        in.skipBytes(8);
        assertEq("block len", BLOCK_LEN_SECTION, in.readInt());

        // Interface Description Blocks
        for (short linkType : mLinkTypes) {
            assertEq("block type", BLOCK_TYPE_INTERFACE, in.readInt());
            assertEq("block len", BLOCK_LEN_INTERFACE, in.readInt());
            assertEq("block len", linkType, in.readShort());
            // ignore reserved
            in.skipBytes(2);
            // ignore snaplen
            in.skipBytes(4);
            assertEq("block len", BLOCK_LEN_INTERFACE, in.readInt());
        }

        while (in.available() > 0)
            mPackets.add(readPacket(in));

        in.close();
    }

    protected PcapPacket readPacket(DataInputStream in) throws IOException {
        return new PcapPacket().read(in);
    }

    private void assertEq(String what, int expected, int actual) throws IOException {
        if (expected != actual)
            throw new IOException(String.format("Pcap format error. %s: %d vs %d", what, expected, actual));
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        DataOutputStream out = new DataOutputStream(stream);

        // Section Header Block
        // block type
        out.writeInt(BLOCK_TYPE_SECTION);
        // block total length
        out.writeInt(BLOCK_LEN_SECTION);
        // byte order magic
        out.writeInt(BYTE_ORDER_MAGIC);
        // version (major + minor)
        out.writeShort(1);
        out.writeShort(0);
        // section length (not specified)
        out.writeLong(-1L);
        // block total length
        out.writeInt(BLOCK_LEN_SECTION);

        // Interface Description Blocks
        for (short linkType : mLinkTypes) {
            // block type
            out.writeInt(BLOCK_TYPE_INTERFACE);
            // block total length
            out.writeInt(BLOCK_LEN_INTERFACE);
            // link type
            out.writeShort(linkType);
            // reserved
            out.writeShort(0);
            // snapLen (no limit)
            out.writeInt(0);
            // block total length
            out.writeInt(BLOCK_LEN_INTERFACE);
        }

        // write packets
        for (PcapPacket packet : mPackets)
            packet.write(out);

        out.close();
    }
}
