package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * A simple conditional that checks if something starts with a certain value
 */
public class StartsWith extends Conditional {
    public StartsWith(byte[] pattern, TARGET target) throws FilterInitException {
        super(pattern, target);
    }

    public StartsWith(byte[] pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(pattern, target, field);
    }

    public StartsWith(byte pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(pattern, target, field);
    }

    // Helper Functions
    /**
     * Helper function to check if mMatchPattern is a prefix of a provided byte[].
     * @param compare byte[] to compare with
     * @return True if mMatchPattern is a prefix of compare, false otherwise
     */
    private boolean isPrefix(byte[] compare) {
        if (compare.length < mMatchPattern.length) return false;
        for (int i = 0; i < mMatchPattern.length; i++) {
            if (mMatchPattern[i] != compare[i]) return false;
        }
        return true;
    }

    /**
     * Helper function to check if mMatchPattern is a prefix of a provided byte
     * @param compare byte to compare with.
     * @return True if mMatchPatternByte is a prefix of compare, false otherwise.
     */
    private boolean isPrefix(byte compare) {
        return mMatchByte == compare;
    }

    @Override
    protected boolean checkNfcData(NfcComm nfcdata) {
        return isPrefix(nfcdata.getData());
    }

    @Override
    protected boolean checkUidData(NfcComm nfcdata) {
        return false;
        // FIXME
        //return isPrefix(nfcdata.getUid());
    }

    @Override
    protected boolean checkAtqaData(NfcComm nfcdata) {
        return false;
        // FIXME
        //return isPrefix(nfcdata.getAtqa());
    }

    @Override
    protected boolean checkHistData(NfcComm nfcdata) {
        return false;
        // FIXME
        //return isPrefix(nfcdata.getHist());
    }

    @Override
    protected boolean checkSakData(NfcComm nfcdata) {
        return false;
        // FIXME
        //return isPrefix(nfcdata.getSak());
    }
}
