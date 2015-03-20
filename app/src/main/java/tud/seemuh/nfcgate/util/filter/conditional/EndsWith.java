package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * A simple conditional that checks if something ends with a certain value
 */
public class EndsWith extends Conditional {
    public EndsWith(byte[] pattern, TARGET target) throws FilterInitException {
        super(pattern, target);
    }

    public EndsWith(byte[] pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(pattern, target, field);
    }

    public EndsWith(byte pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(pattern, target, field);
    }

    // Helper functions

    /**
     * Helper function to check if mMatchPattern is a suffix of the provided byte[].
     * @param compare The byte[] to run the check on
     * @return True if mMatchPattern is a suffix, false otherwise
     */
    private boolean isSuffix(byte[] compare) {
        if (compare.length < mMatchPattern.length) return false;
        int offset = compare.length - mMatchPattern.length;
        for (int i = 0; i < mMatchPattern.length; i++) {
            if (mMatchPattern[i] != compare[offset + i]) return false;
        }
        return true;
    }

    /**
     * Helper function to check if mMatchPatternByte is a suffix of the provided byte (which can
     * only happen if they are identical).
     * @param compare The byte to compare with
     * @return True if mMatchPatternByte is a suffix of compare, false otherwise.
     */
    private boolean isSuffix(byte compare) {
        return mMatchByte == compare;
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        if (mTarget == TARGET.NFC) {
            return (nfcdata.getType() == NfcComm.Type.NFCBytes) && isSuffix(nfcdata.getData());
        } else {
            if (nfcdata.getType() != NfcComm.Type.AnticolBytes) return false;
            if (mAnticolTarget == ANTICOLFIELD.UID) {
                return isSuffix(nfcdata.getUid());
            } else if (mAnticolTarget == ANTICOLFIELD.ATQA) {
                return isSuffix(nfcdata.getAtqa());
            } else if (mAnticolTarget == ANTICOLFIELD.HIST) {
                return isSuffix(nfcdata.getHist());
            } else if (mAnticolTarget == ANTICOLFIELD.SAK) {
                return isSuffix(nfcdata.getSak());
            }
        }
        return false;
    }
}
