package de.tu_darmstadt.seemoo.nfcgate.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TagInfoDao {
    @Query("SELECT * FROM TagInfo ORDER BY Name ASC")
    LiveData<List<TagInfo>> getAll();

    @Insert
    void insert(TagInfo tagInfo);

    @Delete
    void delete(TagInfo tagInfo);
}
