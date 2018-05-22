package tud.seemuh.nfcgate.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class TagInfo {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo
    private String name;

    @ColumnInfo
    private byte[] configurationData;

    public TagInfo(String name, byte[] configurationData) {
        this.name = name;
        this.configurationData = configurationData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getConfigurationData() {
        return configurationData;
    }

    public void setConfigurationData(byte[] configurationData) {
        this.configurationData = configurationData;
    }

    @Override
    public String toString() {
        return getName();
    }
}
