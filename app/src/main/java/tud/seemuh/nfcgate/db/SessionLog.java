package tud.seemuh.nfcgate.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
public class SessionLog {
    public static SimpleDateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public enum SessionType {
        RELAY,
        REPLAY
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo
    private Date date;

    @ColumnInfo
    private SessionType type;

    public SessionLog(Date date, SessionType type) {
        this.date = date;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ISO_DATE.format(date);
    }
}
