package de.tu_darmstadt.seemoo.nfcgate.db;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import android.content.Context;
import androidx.annotation.NonNull;

@Database(entities = {TagInfo.class, SessionLog.class, NfcCommEntry.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TagInfoDao tagInfoDao();
    public abstract SessionLogDao sessionLogDao();
    public abstract SessionLogJoinDao sessionLogJoinDao();
    public abstract NfcCommEntryDao nfcCommEntryDao();

    private static AppDatabase mInstance;

    public static AppDatabase getDatabase(Context context) {
        if (mInstance == null)
            mInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "nfcgate")
                    .addMigrations(MIGRATION_1_2)
                    .build();
        return mInstance;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE SessionLog ADD COLUMN type INTEGER DEFAULT 0");
        }
    };
}
