package tud.seemuh.nfcgate.db.pcap;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tud.seemuh.nfcgate.util.NfcComm;

public class PcapInputStream {
    protected DataInputStream mIn;

    public PcapInputStream(InputStream in) {
        try {
            mIn = new DataInputStream(in);

            // read global pcap header
            // magic(4), version(4), GMT/local(4), accuracy(4), max lng(4)
            mIn.skipBytes(20);
            // link type
            if (mIn.readInt() != 264)
                throw new IOException("Wrong data link type. Expected ISO_14443");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<NfcComm> read() {
        List<NfcComm> results = new ArrayList<>();

        try {
            while (mIn.available() > 0)
                results.add(new PcapPacket().read(mIn));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }
}
