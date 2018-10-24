package tud.seemuh.nfcgate.db.pcap;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tud.seemuh.nfcgate.util.NfcComm;

public class PcapInputStream {
    protected DataInputStream mIn;

    public PcapInputStream(InputStream in) throws IOException {
        mIn = new DataInputStream(in);

        // read global pcap header
        assertFormat("magic", 0xa1b2c3d4, mIn.readInt());
        assertFormat("version (major)", 2, mIn.readShort());
        assertFormat("version (minor)", 4, mIn.readShort());
        assertFormat("GMT to local correction", 0, mIn.readInt());
        assertFormat("accuracy", 0, mIn.readInt());
        assertFormat("max packet length", 65535, mIn.readInt());
        assertFormat("link type", ISO14443Packet.LINKTYPE, mIn.readInt());
    }

    public List<NfcComm> read() throws IOException {
        List<NfcComm> result = new ArrayList<>();

        while (mIn.available() > 0)
            result.add(new PcapPacket().read(mIn));

        return result;
    }

    private void assertFormat(String what, int expected, int actual) throws IOException {
        if (expected != actual)
            throw new IOException(String.format("Pcap format error. %s: %d vs %d", what, expected, actual));
    }
}
