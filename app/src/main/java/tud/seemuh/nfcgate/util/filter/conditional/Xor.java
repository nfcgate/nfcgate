package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * This is the boolean XOR for two parameters (true if and only if exactly one of the provided
 * Conditionals is true).
 * For more than two conditionals, the behaviour stays the same: It is true if and only if exactly
 * one of the Conditionals is true. This is not quite the behaviour of a regular XOR chain, but
 * should be less confusing.
 *
 * As an example, with ^ as the XOR operator: The regular XOR would show the following behaviour:
 * XOR(true, true, true) = true ^ true ^ true = false ^ true = true
 * Our XOR will return false for the same use, as more than one input is true.
 */
public class Xor extends Conditional {
    public Xor(Conditional... cond) throws FilterInitException {
        super(cond);
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        int cnt = 0;
        for (Conditional c : mCondList) {
            if (c.applies(nfcdata)) cnt++;
            if (cnt > 1) return false;
        }
        return (cnt == 1);
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
