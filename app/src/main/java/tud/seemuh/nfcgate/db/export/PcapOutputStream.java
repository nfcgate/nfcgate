package tud.seemuh.nfcgate.db.export;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import tud.seemuh.nfcgate.gui.component.FileShare;
import tud.seemuh.nfcgate.util.NfcComm;

public class PcapOutputStream implements FileShare.IFileShareable {
    protected DataOutputStream mOut;
    protected ByteArrayOutputStream mArrayOut;
    protected Date mDate;

    public PcapOutputStream() {
        try {
            mArrayOut = new ByteArrayOutputStream();
            mOut = new DataOutputStream(mArrayOut);

            // write global pcap header
            // magic
            mOut.writeInt(0xa1b2c3d4);
            // version
            mOut.writeShort(2);
            mOut.writeShort(4);
            // GMT to local correction
            mOut.writeInt(0);
            // accuracy
            mOut.writeInt(0);
            // max length of captured packets
            mOut.writeInt(65535);
            // data link type
            mOut.writeInt(264);      // LINKTYPE_ISO_14443
        } catch (IOException e) {
            Log.e("NFCGATE", "Internal pcap stream error", e);
        }
    }

    public void write(List<NfcComm> nfcComms) {
        for (NfcComm nfcComm : nfcComms) {
            write(new PcapPacket(nfcComm));
        }
    }

    public void write(NfcComm nfcComm) {
        write(new PcapPacket(nfcComm));
    }

    public void write(PcapPacket packet) {
        try {
            packet.write(mOut);
        } catch (IOException e) {
            Log.e("NFCGATE", "Internal pcap stream error", e);
        }
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        mOut.close();
        stream.write(mArrayOut.toByteArray());
    }
}
