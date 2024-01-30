package de.tu_darmstadt.seemoo.nfcgate.db;

import androidx.room.TypeConverter;

import java.util.Date;

import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class Converters {
    @TypeConverter
    public static NfcComm fromBytearray(byte[] data) {
        return data == null ? null : new NfcComm(data);
    }

    @TypeConverter
    public static byte[] NfcCommToBytearray(NfcComm nfcComm) {
        return nfcComm == null ? null : nfcComm.toByteArray();
    }

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static SessionLog.SessionType intToType(int type) {
        return SessionLog.SessionType.values()[type];
    }

    @TypeConverter
    public static int typeToInt(SessionLog.SessionType type) {
        return type.ordinal();
    }
}
