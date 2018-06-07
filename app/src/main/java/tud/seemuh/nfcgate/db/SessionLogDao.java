package tud.seemuh.nfcgate.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SessionLogDao {
    @Query("SELECT * FROM SessionLog ORDER BY Date ASC")
    LiveData<List<SessionLog>> getAll();

    @Insert
    long insert(SessionLog log);

    @Delete
    void delete(SessionLog log);
}
