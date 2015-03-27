package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;

/**
 * The logical NOT of a conditional
 */
public class Not extends Conditional {
    public Not(Conditional cond1) {
        super(cond1);
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        return !mSingleCond.applies(nfcdata);
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
