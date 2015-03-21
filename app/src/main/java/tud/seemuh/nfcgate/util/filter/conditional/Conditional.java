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

    /**
     * Check if the conditional matches the provided NfcComm data
     * @param nfcdata NfcComm object with NFC data
     * @return True if the conditional matches, false otherwise
     */
    public abstract boolean applies(NfcComm nfcdata);
}
