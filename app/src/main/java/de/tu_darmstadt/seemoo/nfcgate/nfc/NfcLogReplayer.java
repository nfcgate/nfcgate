package de.tu_darmstadt.seemoo.nfcgate.nfc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tu_darmstadt.seemoo.nfcgate.db.NfcCommEntry;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class NfcLogReplayer {
    private final boolean mReader;
    private final String mMode;
    private final List<NfcCommEntry> mReplayLog;
    private int mReplayIndex = 0;

    public NfcLogReplayer(boolean reader, String mode, List<NfcCommEntry> replayLog) {
        mReader = reader;
        mMode = mode;
        mReplayLog = replayLog;
    }

    private NfcComm getNext() {
        // next log entry does not exist -> do nothing
        if (mReplayIndex >= mReplayLog.size())
            return null;

        // get next entry
        return mReplayLog.get(mReplayIndex).getNfcComm();
    }

    public NfcComm getResponse(NfcComm request) {
        switch (mMode) {
            case "index":
                return getIndexBasedResponse(request);

            case "pattern":
                return getPatternBasedResponse(request);

            default:
                throw new IllegalArgumentException("Unknown replay mode " + mMode);
        }
    }

    public boolean shouldWait() {
        // get next entry
        NfcComm next = getNext();

        // wait if no next entry exists or next entry is not our type
        return next == null || next.isCard() == mReader;
    }

    /**
     * Returns the next communication to be sent by "our" side based on the index
     * or null if we need to wait or no matching communication was found
     */
    private NfcComm getIndexBasedResponse(NfcComm request) {
        // get next entry
        NfcComm next = getNext();

        if (request != null && next != null && next.isCard() == request.isCard()) {
            // request matches the log entry we were expecting
            mReplayIndex++;
            return getIndexBasedResponse(null);
        }
        else if (request == null && next != null && next.isCard() != mReader) {
            // next entry matches our type
            mReplayIndex++;
            // update date by creating new NfcComm from old one
            return new NfcComm(next.isCard(), next.isInitial(), next.getData());
        }

        // either wrong request or next log entry does not match our type: wait
        return null;
    }

    /**
     * Returns the next communication to be sent by "our" side based on
     * a mix of the index and data patterns or null if we need to wait
     * or no matching communication was found
     */
    private NfcComm getPatternBasedResponse(NfcComm request) {
        // get next entry
        NfcComm next = getNext();

        // if the other side sent a request exactly matching our expectations, use index-based resp
        if (request != null && next != null && next.isCard() == request.isCard()
                && Arrays.equals(next.getData(), request.getData())) {
            return getIndexBasedResponse(request);
        }
        // if we just need our next communication, use index-based resp
        else if (request == null) {
            return getIndexBasedResponse(null);
        }
        // if we have a request but it does not exactly match the expected data, use pattern-based
        else {
            int predictedIndex = getPredictedIndex(request);

            // jump to best prediction and continue index-based resp from there
            if (predictedIndex >= 0)
                mReplayIndex = predictedIndex;

            return getIndexBasedResponse(request);
        }
    }

    /**
     * Predict the current log index for given request based on the best score in the scoreMap
     */
    private int getPredictedIndex(NfcComm request) {
        Map<Integer, Integer> scoreMap = makeScoreMap(request);
        int cIndex = -1, cScore = -1;

        for (Map.Entry<Integer, Integer> entry : scoreMap.entrySet()) {
            int index = entry.getKey(), score = entry.getValue();

            // index has better score or better ranking at equal score
            if (score > cScore || (score == cScore && rankIndex(index) < rankIndex(cIndex))) {
                cIndex = index;
                cScore = score;
            }
        }

        return cIndex;
    }

    /**
     * Build a (index, score) map with a score for each suitable log entry index
     */
    private Map<Integer, Integer> makeScoreMap(NfcComm request) {
        Map<Integer, Integer> result = new HashMap<>();

        for (int i = 0; i < mReplayLog.size(); i++) {
            NfcComm entry = mReplayLog.get(i).getNfcComm();

            // calc only for other side
            if (entry.isCard() == mReader)
                result.put(i, calcScore(entry, request));
        }

        return result;
    }

    /**
     * Ranks given index against mReplayIndex using forward distance. Lower is better
     */
    private int rankIndex(int index) {
        if (index == -1) return Integer.MAX_VALUE;
        else if (index > mReplayIndex) return index - mReplayIndex;
        else return Math.max(0, mReplayLog.size() - mReplayIndex) + index;
    }

    /**
     * Matches length and content of given data. Higher score is better
     */
    private int calcScore(NfcComm entry, NfcComm request) {
        byte[] bEntry = entry.getData(), bRequest = request.getData();

        // length based score: 10 for perfect match, one less for each absolute difference
        int lScore = Math.max(0, 10 - (Math.abs(bEntry.length - bRequest.length)));

        // prefix based score
        int pScore;
        for (pScore = 0; pScore < Math.min(bEntry.length, bRequest.length); pScore++)
            if (bEntry[pScore] != bRequest[pScore])
                break;

        // combine
        return pScore + lScore;
    }
}
