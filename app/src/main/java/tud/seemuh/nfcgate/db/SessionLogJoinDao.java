package tud.seemuh.nfcgate.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public interface SessionLogJoinDao {
    @Transaction
    @Query("SELECT id, date, type FROM SessionLog INNER JOIN NfcCommEntry ON SessionLog.id = NfcCommEntry.sessionId WHERE SessionLog.id=:sessionId ORDER BY NfcCommEntry.entryId ASC")
    LiveData<SessionLogJoin> get(long sessionId);
}
