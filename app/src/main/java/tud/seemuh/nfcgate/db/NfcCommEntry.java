package tud.seemuh.nfcgate.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import tud.seemuh.nfcgate.util.NfcComm;

@Entity(foreignKeys = {
        @ForeignKey(entity = SessionLog.class, parentColumns = "id", childColumns = "sessionId")
})
public class NfcCommEntry {
    @PrimaryKey(autoGenerate = true)
    private int entryId;

    @ColumnInfo
    private NfcComm nfcComm;

    @ColumnInfo
    private long sessionId;

    public NfcCommEntry(NfcComm nfcComm, long sessionId) {
        this.nfcComm = nfcComm;
        this.sessionId = sessionId;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
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
        return nfcComm.toString();
    }
}
