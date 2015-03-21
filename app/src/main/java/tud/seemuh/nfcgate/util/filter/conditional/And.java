package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;

/**
 * The logical AND of two conditionals
 */
public class And extends Conditional {
    public And(Conditional cond1, Conditional cond2) {
        super(cond1, cond2);
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        return mCond1.applies(nfcdata) && mCond2.applies(nfcdata);
    }

    // All of these functions are never used, but have to be implemented in order to conform to the
    // superclass.
    @Override
    protected boolean checkNfcData(NfcComm nfcdata) {
        return false;
    }

    @Override
    protected boolean checkUidData(NfcComm nfcdata) {
        return false;
    }

    @Override
    protected boolean checkAtqaData(NfcComm nfcdata) {
        return false;
    }

    @Override
    protected boolean checkHistData(NfcComm nfcdata) {
        return false;
    }

    @Override
    protected boolean checkSakData(NfcComm nfcdata) {
        return false;
    }
}
