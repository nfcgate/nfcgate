package de.tu_darmstadt.seemoo.nfcgate.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SessionLogDao {
    @Query("SELECT * FROM SessionLog ORDER BY Date DESC")
    LiveData<List<SessionLog>> getAll();

    @Insert
    long insert(SessionLog log);

    @Delete
    void delete(SessionLog log);
}
