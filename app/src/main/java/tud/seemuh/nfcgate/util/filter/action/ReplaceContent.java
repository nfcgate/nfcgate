package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;

/**
 * An action which replaces the contents of the targeted field with a constant.
 */
public class ReplaceContent extends Action {

    public ReplaceContent(byte[] content, TARGET target) throws FilterInitException {
        super(content, target);
    }

    public ReplaceContent(byte[] content, TARGET target, ANTICOLFIELD targetfield) throws FilterInitException {
        super(content, target, targetfield);
    }

    public ReplaceContent(byte content, TARGET target, ANTICOLFIELD targetfield) throws FilterInitException {
        super(content, target, targetfield);
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
