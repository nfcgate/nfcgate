package tud.seemuh.nfcgate.db.pcap;

import java.io.DataInputStream;
import java.io.IOException;

import tud.seemuh.nfcgate.util.NfcComm;

public interface PcapReadableObject {
    NfcComm read(DataInputStream in) throws IOException;
}
