package tud.seemuh.nfcgate.nfc;

import java.util.List;

import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.util.NfcComm;

public class NfcLogReplayer {
    private boolean mReader;
    private List<NfcCommEntry> mReplayLog;
    private int mReplayIndex = 0;

    public NfcLogReplayer(boolean reader, List<NfcCommEntry> replayLog) {
        mReader = reader;
        mReplayLog = replayLog;
    }

    public NfcComm getResponse(NfcComm request) {
        // next log entry does not exist -> do nothing
        if (mReplayIndex >= mReplayLog.size())
            return null;

        // get next entry
        NfcComm next = mReplayLog.get(mReplayIndex).getNfcComm();

        if (request != null && next.isCard() == request.isCard()) {
            // request matches the log entry we were expecting
            mReplayIndex++;
            return getResponse(null);
        }
        else if (request == null && next.isCard() != mReader) {
            // next entry matches our type
            mReplayIndex++;
            return next;
        }

        // either wrong request or next log entry does not match our type: wait
        return null;
    }
}
