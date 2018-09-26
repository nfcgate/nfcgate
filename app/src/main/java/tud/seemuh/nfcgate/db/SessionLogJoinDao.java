package tud.seemuh.nfcgate.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SessionLogJoinDao {
    @Query("SELECT * FROM SessionLog INNER JOIN NfcCommEntry ON SessionLog.id = NfcCommEntry.sessionId ORDER BY Date ASC, NfcCommEntry.entryId ASC")
    List<SessionLogJoin> getAll();

    @Query("SELECT * FROM SessionLog INNER JOIN NfcCommEntry ON SessionLog.id = NfcCommEntry.sessionId WHERE SessionLog.id=:sessionId ORDER BY NfcCommEntry.entryId ASC")
    LiveData<SessionLogJoin> get(long sessionId);
}
