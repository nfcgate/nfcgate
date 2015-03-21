package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * A conditional that checks if the length of a piece of data equals a certain integer
 */
public class Length extends Conditional {
    public Length(int length, TARGET target) throws FilterInitException {
        super(length, target);
    }

    public Length(int length, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(length, target, field);
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        if (mTarget == TARGET.NFC) {
            return (nfcdata.getType() == NfcComm.Type.NFCBytes)
                    && nfcdata.getData().length == mInteger;
        } else {
            if (nfcdata.getType() != NfcComm.Type.AnticolBytes) return false;
            if (mAnticolTarget == ANTICOLFIELD.UID) {
                return nfcdata.getUid().length == mInteger;
            } else if (mAnticolTarget == ANTICOLFIELD.ATQA) {
                return nfcdata.getAtqa().length == mInteger;
            } else if (mAnticolTarget == ANTICOLFIELD.HIST) {
                return nfcdata.getHist().length == mInteger;
            } else if (mAnticolTarget == ANTICOLFIELD.SAK) {
                return mInteger == 1;  // This assumes that the SAK value is set at all
            }
        }
        return false;
    }
}
