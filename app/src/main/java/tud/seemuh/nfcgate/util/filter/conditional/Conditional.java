package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * Interface for conditionals, as used in the Filter infrastructure. Please check the Wiki for more
 * Information.
 */
public abstract class Conditional {
    public enum TARGET {
        NFC,
        ANTICOL
    }

    public enum ANTICOLFIELD {
        UID,
        ATQA,
        SAK,
        HIST
    }

    protected byte[] mMatchPattern;
    protected byte mMatchByte;
    protected TARGET mTarget;
    protected ANTICOLFIELD mAnticolTarget;
    protected int mInteger;
    protected Conditional mCond1;
    protected Conditional mCond2;

    public Conditional(byte[] pattern, TARGET target) throws FilterInitException {
        if (target == TARGET.ANTICOL) throw new FilterInitException("Wrong constructor signature for Anticol data.");
        mTarget = target;
        mMatchPattern = pattern;
    }

    public Conditional(byte[] pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target == TARGET.NFC || field == ANTICOLFIELD.SAK)
            throw new FilterInitException("Wrong constructor signature for NFC data.");
        mTarget = target;
        mMatchPattern = pattern;
        mAnticolTarget = field;
    }

    public Conditional(byte pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target == TARGET.NFC || field != ANTICOLFIELD.SAK)
            throw new FilterInitException("Wrong constructor signature for NFC data.");
        mTarget = target;
        mMatchByte = pattern;
        mAnticolTarget = field;
    }

    public Conditional(int integer, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target == TARGET.NFC)
            throw new FilterInitException("Wrong constructor signature for NFC data.");
        mTarget = target;
        mInteger = integer;
        mAnticolTarget = field;
    }

    public Conditional(int integer, TARGET target) throws FilterInitException {
        if (target == TARGET.ANTICOL) throw new FilterInitException("Wrong constructor signature for Anticol data.");
        mTarget = target;
        mInteger = integer;
    }

    public Conditional(TARGET target) throws  FilterInitException {
        if (target == TARGET.ANTICOL) throw new FilterInitException("Wrong constructor signature for Anticol data.");
        mTarget = target;
    }

    public Conditional(TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target == TARGET.NFC)
            throw new FilterInitException("Wrong constructor signature for NFC data.");
        mTarget = target;
        mAnticolTarget = field;
    }

    public Conditional(Conditional cond1, Conditional cond2) {
        mCond1 = cond1;
        mCond2 = cond2;
    }

    /**
     * Check if the conditional matches the provided NfcComm data
     * @param nfcdata NfcComm object with NFC data
     * @return True if the conditional matches, false otherwise
     */
    public boolean applies(NfcComm nfcdata) {
        if (mTarget == TARGET.NFC) {
            return (nfcdata.getType() == NfcComm.Type.NFCBytes) && checkNfcData(nfcdata);
        } else {
            if (nfcdata.getType() != NfcComm.Type.AnticolBytes) return false;
            if (mAnticolTarget == ANTICOLFIELD.UID) {
                return checkUidData(nfcdata);
            } else if (mAnticolTarget == ANTICOLFIELD.ATQA) {
                return checkAtqaData(nfcdata);
            } else if (mAnticolTarget == ANTICOLFIELD.HIST) {
                return checkHistData(nfcdata);
            } else if (mAnticolTarget == ANTICOLFIELD.SAK) {
                return checkSakData(nfcdata);
            }
        }
        return false;
    }

    protected abstract boolean checkNfcData(NfcComm nfcdata);

    protected abstract boolean checkUidData(NfcComm nfcdata);

    protected abstract boolean checkAtqaData(NfcComm nfcdata);

    protected abstract boolean checkHistData(NfcComm nfcdata);

    protected abstract boolean checkSakData(NfcComm nfcdata);
}
