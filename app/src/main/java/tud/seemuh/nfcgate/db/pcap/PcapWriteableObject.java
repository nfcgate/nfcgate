package tud.seemuh.nfcgate.db.pcap;

import java.io.DataOutputStream;
import java.io.IOException;

public interface PcapWriteableObject {
    int write(DataOutputStream out) throws IOException;
}
