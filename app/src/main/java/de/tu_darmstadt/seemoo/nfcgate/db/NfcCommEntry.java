package de.tu_darmstadt.seemoo.nfcgate.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

@Entity(indices = {@Index("sessionId")},
        foreignKeys = {
                @ForeignKey(entity = SessionLog.class, parentColumns = "id", childColumns = "sessionId", onDelete = ForeignKey.CASCADE)
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
