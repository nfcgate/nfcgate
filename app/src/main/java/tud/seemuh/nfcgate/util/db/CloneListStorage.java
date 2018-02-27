package tud.seemuh.nfcgate.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.util.NfcComm;

public class CloneListStorage  extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "clonemode";

    // table name
    private static final String TABLE_NAME = "list";

    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_CONFIG = "config";

    public CloneListStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_CONFIG + " BLOB"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    public void add(CloneListItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, item.toString());
        NfcComm ac = item.getAnticolData();
        values.put(KEY_CONFIG, ac.getConfig().build());

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    public void delete(CloneListItem item) {
        delete(item.getId());
    }

    public void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public List<CloneListItem> getAll() {
        List<CloneListItem> contactList = new ArrayList<CloneListItem>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                contactList.add(createByCursor(cursor));
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    private CloneListItem createByCursor(Cursor c) {
        NfcComm ac = new NfcComm(new ConfigBuilder(c.getBlob(c.getColumnIndex(KEY_CONFIG))));
        return new CloneListItem(ac, c.getString(c.getColumnIndex(KEY_NAME)), c.getInt(c.getColumnIndex(KEY_ID)));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }
}
