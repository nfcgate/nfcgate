package tud.seemuh.nfcgate.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {TagInfo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TagInfoDao tagInfoDao();

    private static AppDatabase mInstance;

    public static AppDatabase getDatabase(Context context) {
        if (mInstance == null)
            mInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "nfcgate").build();
        return mInstance;
    }
}
