package tud.seemuh.nfcgate.db;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class SessionLogJoin {
    @Embedded
    private SessionLog sessionLog;

    @Relation(parentColumn = "id", entityColumn = "sessionId")
    private List<NfcCommEntry> nfcCommEntries;

    public SessionLogJoin(SessionLog sessionLog) {
        this.sessionLog = sessionLog;
    }

    public SessionLog getSessionLog() {
        return sessionLog;
    }

    public void setSessionLog(SessionLog sessionLog) {
        this.sessionLog = sessionLog;
    }

    public List<NfcCommEntry> getNfcCommEntries() {
        return nfcCommEntries;
    }

    public void setNfcCommEntries(List<NfcCommEntry> nfcCommEntries) {
        this.nfcCommEntries = nfcCommEntries;
    }
}
