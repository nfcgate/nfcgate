package tud.seemuh.nfcgate.util.db;

import android.provider.BaseColumns;

/**
 * SQL Contract class for the Session Logging database, as proposed in
 * https://developer.android.com/training/basics/data-storage/databases.html
 */
public final class SessionLoggingContract {
    public SessionLoggingContract() {}

    // Table that saves meta information about a Session
    public static abstract class SessionMeta{
        public static final String TABLE_NAME = "session_meta";
        public static final String COLUMN_NAME_SESSION_ID = "sessionID";
        // Finished is set to 1 once the session is done, so we can detect unfinished sessions
        public static final String COLUMN_NAME_FINISHED = "finished";
        // Name may be null, can be set by the user later
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DATE = "timestamp";
    }

    // Table that saves actual events inside of a session
    public static abstract class SessionEvent implements BaseColumns {
        public static final String TABLE_NAME = "session_event";
        // Information to link the session to an Entry from SessionMeta
        public static final String COLUMN_NAME_SESSION_ID = "sessionID";
        // Meta information about the message
        public static final String COLUMN_NAME_DATE = "timestamp";
        public static final String COLUMN_NAME_SOURCE = "source";
        public static final String COLUMN_NAME_TYPE = "type";
        // The actual data. Either nfcdata or the other fields may be set
        public static final String COLUMN_NAME_NFCDATA = "nfcdata";
        public static final String COLUMN_NAME_UID = "uid";
        public static final String COLUMN_NAME_ATQA = "atqa";
        public static final String COLUMN_NAME_SAK = "sak";
        public static final String COLUMN_NAME_HIST = "hist";
        // Pre-Filter values, where changed. NULL if they have not changed.
        public static final String COLUMN_NAME_NFCDATA_PREFILTER = "nfcdata_pf";
        public static final String COLUMN_NAME_UID_PREFILTER = "uid_pf";
        public static final String COLUMN_NAME_ATQA_PREFILTER = "atqa_pf";
        public static final String COLUMN_NAME_SAK_PREFILTER = "sak_pf";
        public static final String COLUMN_NAME_HIST_PREFILTER = "hist_pf";

        // Constants - Data source
        public static final int VALUE_SOURCE_HCE = 0;
        public static final int VALUE_SOURCE_CARD = 1;
        // Constants - Data type
        public static final int VALUE_TYPE_NFC = 0;
        public static final int VALUE_TYPE_ANTICOL = 1;
    }
}
