package tud.seemuh.nfcgate.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import tud.seemuh.nfcgate.util.NfcComm;

import static tud.seemuh.nfcgate.util.Utils.bytesToHexDump;

@Entity(foreignKeys = {
        @ForeignKey(entity = SessionLog.class, parentColumns = "id", childColumns = "sessionId")
})
public class NfcCommEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo
    private NfcComm nfcComm;

    @ColumnInfo
    private long sessionId;

    public NfcCommEntry(NfcComm nfcComm, long sessionId) {
        this.nfcComm = nfcComm;
        this.sessionId = sessionId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NfcComm getNfcComm() {
        return nfcComm;
    }

    public void setNfcComm(NfcComm nfcComm) {
        this.nfcComm = nfcComm;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        final byte[] data = nfcComm.getData();
        final boolean card = nfcComm.isCard();
        final boolean initial = nfcComm.isInitial();

        sb.append("card: ");
        sb.append(card);
        sb.append(" ");
        sb.append("initial: ");
        sb.append(initial);
        sb.append("\n");
        sb.append("\n");
        sb.append(bytesToHexDump(data));

        return sb.toString();
    }
}
