package tud.seemuh.nfcgate.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DB helper for the SessionLogging database
 */
public class SessionLoggingDbHelper extends SQLiteOpenHelper {
    // Universal constants for the construction of SQL queries
    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INT = " INTEGER";
    private static final String TYPE_BYTES = " BLOB";
    private static final String TYPE_DATETIME = " DATETIME";
    private static final String OPT_PRIMARY_KEY = " PRIMARY KEY";
    private static final String OPT_NOT_NULL = " NOT NULL";
    private static final String OPT_DEFAULT_ZERO = " DEFAULT 0";
    private static final String OPT_DEFAULT_NOW = " DEFAULT CURRENT_TIMESTAMP";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES_SESSIONMETA
            = "CREATE TABLE " + SessionLoggingContract.SessionMeta.TABLE_NAME + " (" +
            SessionLoggingContract.SessionMeta._ID + TYPE_INT + OPT_PRIMARY_KEY + COMMA_SEP +
            SessionLoggingContract.SessionMeta.COLUMN_NAME_FINISHED + TYPE_INT + OPT_DEFAULT_ZERO + COMMA_SEP +
            SessionLoggingContract.SessionMeta.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
            SessionLoggingContract.SessionMeta.COLUMN_NAME_DATE + TYPE_DATETIME + OPT_DEFAULT_NOW +
            ");";

    private static final String SQL_CREATE_ENTRIES_SESSIONEVENT
            = "CREATE TABLE " + SessionLoggingContract.SessionEvent.TABLE_NAME + " (" +
            SessionLoggingContract.SessionEvent._ID + TYPE_INT + OPT_PRIMARY_KEY + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_SESSION_ID + TYPE_INT + OPT_NOT_NULL + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_DATE + TYPE_DATETIME + OPT_DEFAULT_NOW + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_TYPE + TYPE_INT + OPT_NOT_NULL + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_NFCDATA + TYPE_BYTES + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_UID + TYPE_BYTES + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_ATQA + TYPE_BYTES + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_SAK + TYPE_BYTES + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_HIST + TYPE_BYTES + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_NFCDATA_PREFILTER + TYPE_BYTES + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_UID_PREFILTER + TYPE_BYTES + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_ATQA_PREFILTER + TYPE_BYTES + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_SAK_PREFILTER + TYPE_BYTES + COMMA_SEP +
            SessionLoggingContract.SessionEvent.COLUMN_NAME_HIST_PREFILTER + TYPE_BYTES + COMMA_SEP +
            "FOREIGN KEY(" + SessionLoggingContract.SessionEvent.COLUMN_NAME_SESSION_ID + ") REFERENCES " +
            SessionLoggingContract.SessionMeta.TABLE_NAME + "(" + SessionLoggingContract.SessionMeta._ID + ")" +
            " ON DELETE CASCADE" + ");";

    private final String SQL_DROP_SESSIONMETA
            = "DROP TABLE " + SessionLoggingContract.SessionMeta.TABLE_NAME + ";";

    private final String SQL_DROP_SESSIONEVENT
            = "DROP TABLE " + SessionLoggingContract.SessionEvent.TABLE_NAME + ";";

    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "SessionLogging.db";

    public SessionLoggingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_SESSIONMETA);
        db.execSQL(SQL_CREATE_ENTRIES_SESSIONEVENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Proper update code as soon as we go into production with this
        db.execSQL(SQL_DROP_SESSIONEVENT);
        db.execSQL(SQL_DROP_SESSIONMETA);
        onCreate(db);
    }
}
