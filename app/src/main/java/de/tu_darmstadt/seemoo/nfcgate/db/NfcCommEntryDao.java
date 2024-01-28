package de.tu_darmstadt.seemoo.nfcgate.db;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface NfcCommEntryDao {
    @Insert
    void insert(NfcCommEntry log);
}
