package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;

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

    /**
     * Perform the requested action on the NFC data
     * @param nfcdata NfcComm object with NFC data
     * @return Modified NfcComm object with new NFC data
     */
    public abstract NfcComm performAction(NfcComm nfcdata);
}
