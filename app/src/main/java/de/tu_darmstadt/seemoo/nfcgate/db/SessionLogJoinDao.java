package de.tu_darmstadt.seemoo.nfcgate.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public interface SessionLogJoinDao {
    @Transaction
    @Query("SELECT id, date, type FROM SessionLog INNER JOIN NfcCommEntry ON SessionLog.id = NfcCommEntry.sessionId WHERE SessionLog.id=:sessionId ORDER BY NfcCommEntry.entryId ASC")
    LiveData<SessionLogJoin> get(long sessionId);
}
