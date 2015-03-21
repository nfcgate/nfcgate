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
    protected boolean checkNfcData(NfcComm nfcdata) {
        return isSuffix(nfcdata.getData());
    }

    @Override
    protected boolean checkUidData(NfcComm nfcdata) {
        return isSuffix(nfcdata.getUid());
    }

    @Override
    protected boolean checkAtqaData(NfcComm nfcdata) {
        return isSuffix(nfcdata.getAtqa());
    }

    @Override
    protected boolean checkHistData(NfcComm nfcdata) {
        return isSuffix(nfcdata.getHist());
    }

    @Override
    protected boolean checkSakData(NfcComm nfcdata) {
        return isSuffix(nfcdata.getSak());
    }
}
