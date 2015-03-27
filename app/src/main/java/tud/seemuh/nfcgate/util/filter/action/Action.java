package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * Interface for Actions, as used in the Filter infrastructure. Please check the relevant Wiki
 * article for more information.
 */
public abstract class Action {
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

    protected TARGET mTarget;
    protected ANTICOLFIELD mAnticolTarget;
    protected byte[] mNewContent;
    protected byte mNewContentByte;
    protected int mOffset;
    protected Action[] mActionList;

    // Different constructors for different types of actions
    public Action(byte[] content, int offset, TARGET target) throws FilterInitException {
        if (target != TARGET.NFC) throw new FilterInitException("Wrong constructor for target type");
        if (offset < 0) throw new FilterInitException("Offset must be positive");
        mTarget = target;
        mNewContent = content;
        mOffset = offset;
    }

    public Action(byte[] content, int offset, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target != TARGET.ANTICOL || field == ANTICOLFIELD.SAK)
            throw new FilterInitException("Wrong constructor for target type");
        if (offset < 0) throw new FilterInitException("Offset must be positive");
        mTarget = target;
        mNewContent = content;
        mOffset = offset;
        mAnticolTarget = field;
    }

    public Action(byte content, int offset, TARGET target, ANTICOLFIELD targetfield) throws FilterInitException {
        if (target != TARGET.ANTICOL || targetfield != ANTICOLFIELD.SAK)
            throw new FilterInitException("Wrong constructor for target type");
        if (offset < 0) throw new FilterInitException("Offset must be positive");
        mTarget = target;
        mNewContentByte = content;
        mAnticolTarget = targetfield;
    }

    public Action(byte[] content, TARGET target) throws FilterInitException {
        if (target != TARGET.NFC) throw new FilterInitException("Wrong constructor for target type");
        mTarget = target;
        mNewContent = content;
    }

    public Action(byte[] content, TARGET target, ANTICOLFIELD targetfield) throws FilterInitException {
        if (target != TARGET.ANTICOL || targetfield == ANTICOLFIELD.SAK) throw new FilterInitException("Wrong constructor for target type");
        mTarget = target;
        mNewContent = content;
        mAnticolTarget = targetfield;
    }

    public Action(byte content, TARGET target, ANTICOLFIELD targetfield) throws FilterInitException {
        if (target != TARGET.ANTICOL || targetfield != ANTICOLFIELD.SAK) throw new FilterInitException("Wrong constructor for target type");
        mTarget = target;
        mNewContentByte = content;
        mAnticolTarget = targetfield;
    }

    public Action(int offset, TARGET target) throws FilterInitException {
        if (target != TARGET.NFC) throw new FilterInitException("Illegal constructor for target type");
        if (offset < 0) throw new FilterInitException("Offset must be larger than or equal to 0");
        mOffset = offset;
        mTarget = target;
    }

    public Action(Action... act) throws FilterInitException {
        if (act == null || act.length == 0)
            throw new FilterInitException("Most provide at least one action");
        mActionList = act;
    }

    /**
     * Perform the requested action on the NFC data
     * @param nfcdata NfcComm object with NFC data
     * @return Modified NfcComm object with new NFC data
     */
    public NfcComm performAction(NfcComm nfcdata) {
        if ((nfcdata.getType() == NfcComm.Type.NFCBytes && mTarget == TARGET.ANTICOL)
                || (nfcdata.getType() == NfcComm.Type.AnticolBytes && mTarget == TARGET.NFC)) {
            return nfcdata;
        }
        if (mTarget == TARGET.NFC) {
            return modifyNfcData(nfcdata);
        } else if (mAnticolTarget == ANTICOLFIELD.UID) {
            return modifyUidData(nfcdata);
        } else if (mAnticolTarget == ANTICOLFIELD.ATQA) {
            return modifyAtqaData(nfcdata);
        } else if (mAnticolTarget == ANTICOLFIELD.HIST) {
            return modifyHistData(nfcdata);
        } else if (mAnticolTarget == ANTICOLFIELD.SAK) {
            return modifySakData(nfcdata);
        }
        return nfcdata;
    }

    // The following abstract methods will all be implemented by actual implementations to perform
    // the actual desired actions.
    protected abstract NfcComm modifyNfcData(NfcComm nfcdata);

    protected abstract NfcComm modifyUidData(NfcComm nfcdata);

    protected abstract NfcComm modifyAtqaData(NfcComm nfcdata);

    protected abstract NfcComm modifyHistData(NfcComm nfcdata);

    protected abstract NfcComm modifySakData(NfcComm nfcdata);
}
