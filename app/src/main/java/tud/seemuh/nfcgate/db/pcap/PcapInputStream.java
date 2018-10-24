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
        // magic
        assertFormat(mIn.readInt() == 0xa1b2c3d4);
        // version
        assertFormat(mIn.readShort() == 2);
        assertFormat(mIn.readShort() == 4);
        // GMT to local correction
        assertFormat(mIn.readInt() == 0);
        // accuracy
        assertFormat(mIn.readInt() == 0);
        // max length of packets
        assertFormat(mIn.readInt() == 65535);
        // link type
        assertFormat(mIn.readInt() == 264);
    }

    public List<NfcComm> read() throws IOException {
        List<NfcComm> result = new ArrayList<>();

        while (mIn.available() > 0)
            result.add(new PcapPacket().read(mIn));

        return result;
    }

    private void assertFormat(boolean condition) throws IOException {
        if (!condition)
            throw new IOException("Pcap format error");
    }
}
