package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * The logical OR of a number of Conditionals
 */
public class Or extends Conditional {
    public Or(Conditional... cond) throws FilterInitException {
        super(cond);
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        boolean rv = false;
        for (Conditional c : mCondList) {
            rv = rv || c.applies(nfcdata);
            if (rv) return true;
        }
        return false;
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
