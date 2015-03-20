package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * A simple conditional that checks if something starts with a certain value
 */
public class StartsWith extends Conditional {
    private boolean isPrefix(byte[] compare) {
        if (compare.length < mMatchPattern.length) return false;
        for (int i = 0; i < mMatchPattern.length; i++) {
            if (mMatchPattern[i] != compare[i]) return false;
        }
        return true;
    }

    private boolean isPrefix(byte compare) {
        return mMatchByte == compare;
    }

    public StartsWith(byte[] pattern, TARGET target) throws FilterInitException {
        super(pattern, target);
    }

    public StartsWith(byte[] pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(pattern, target, field);
    }

    public StartsWith(byte pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(pattern, target, field);
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        if (mTarget == TARGET.NFC) {
            return (nfcdata.getType() == NfcComm.Type.NFCBytes) && isPrefix(nfcdata.getData());
        } else {
            if (nfcdata.getType() != NfcComm.Type.AnticolBytes) return false;
            if (mAnticolTarget == ANTICOLFIELD.UID) {
                return isPrefix(nfcdata.getUid());
            } else if (mAnticolTarget == ANTICOLFIELD.ATQA) {
                return isPrefix(nfcdata.getAtqa());
            } else if (mAnticolTarget == ANTICOLFIELD.HIST) {
                return isPrefix(nfcdata.getHist());
            } else if (mAnticolTarget == ANTICOLFIELD.SAK) {
                return isPrefix(nfcdata.getSak());
            }
        }
        return false;
    }
}
