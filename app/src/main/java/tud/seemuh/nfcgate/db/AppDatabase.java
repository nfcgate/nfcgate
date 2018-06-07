package tud.seemuh.nfcgate.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {TagInfo.class, SessionLog.class, NfcCommEntry.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TagInfoDao tagInfoDao();
    public abstract SessionLogDao sessionLogDao();
    public abstract SessionLogJoinDao sessionLogJoinDao();
    public abstract NfcCommEntryDao nfcCommEntryDao();

    private static AppDatabase mInstance;

    public static AppDatabase getDatabase(Context context) {
        if (mInstance == null)
            mInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "nfcgate").build();
        return mInstance;
    }
}
