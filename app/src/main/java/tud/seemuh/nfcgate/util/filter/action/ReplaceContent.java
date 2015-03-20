package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;

/**
 * An action which replaces the contents of the targeted field with a constant.
 */
public class ReplaceContent implements Action {

    private TARGET mTarget;
    private ANTICOLFIELD mAnticolTarget;
    private byte[] mNewContent;
    private byte mNewContentByte;

    public ReplaceContent(byte[] content, TARGET target) throws FilterInitException {
        if (target != TARGET.NFC) throw new FilterInitException("Wrong constructor for target type");
        mTarget = target;
        mNewContent = content;
    }

    public ReplaceContent(byte[] content, TARGET target, ANTICOLFIELD targetfield) throws FilterInitException {
        if (target != TARGET.ANTICOL || targetfield == ANTICOLFIELD.SAK) throw new FilterInitException("Wrong constructor for target type");
        mTarget = target;
        mNewContent = content;
        mAnticolTarget = targetfield;
    }

    public ReplaceContent(byte content, TARGET target, ANTICOLFIELD targetfield) throws FilterInitException {
        if (target != TARGET.ANTICOL || targetfield != ANTICOLFIELD.SAK) throw new FilterInitException("Wrong constructor for target type");
        mTarget = target;
        mNewContentByte = content;
        mAnticolTarget = targetfield;
    }

    @Override
    public NfcComm performAction(NfcComm nfcdata) {
        if ((nfcdata.getType() == NfcComm.Type.NFCBytes && mTarget == TARGET.ANTICOL)
         || (nfcdata.getType() == NfcComm.Type.AnticolBytes && mTarget == TARGET.NFC)) {
            return nfcdata;
        }
        if (mTarget == TARGET.NFC) {
            nfcdata.setData(mNewContent);
        } else if (mAnticolTarget == ANTICOLFIELD.UID) {
            nfcdata.setUid(mNewContent);
        } else if (mAnticolTarget == ANTICOLFIELD.ATQA) {
            nfcdata.setAtqa(mNewContent);
        } else if (mAnticolTarget == ANTICOLFIELD.HIST) {
            nfcdata.setHist(mNewContent);
        } else if (mAnticolTarget == ANTICOLFIELD.SAK) {
            nfcdata.setSak(mNewContentByte);
        }
        return nfcdata;
    }
}
