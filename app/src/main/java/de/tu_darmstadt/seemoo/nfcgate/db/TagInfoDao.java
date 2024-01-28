package de.tu_darmstadt.seemoo.nfcgate.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

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
