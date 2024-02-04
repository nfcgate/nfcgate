package de.tu_darmstadt.seemoo.nfcgate.db.worker;

import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class LogEntry {
    private final boolean mValid;
    private final NfcComm mData;

    LogEntry() {
        mData = null;
        mValid = false;
    }

    LogEntry(NfcComm data) {
        mData = data;
        mValid = true;
    }

    NfcComm getData() {
        return mData;
    }

    boolean isValid() {
        return mValid;
    }
}
