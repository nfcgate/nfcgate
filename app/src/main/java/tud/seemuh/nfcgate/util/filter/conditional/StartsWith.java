package tud.seemuh.nfcgate.util.filter.conditional;

import java.util.Arrays;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * A simple conditional that checks if something starts with a certain value
 */
public class StartsWith implements Conditional {
    private TARGET mTarget;
    private byte[] mMatchPattern;
    private ANTICOLFIELD mAnticolTarget;

    private boolean isPrefix(byte[] compare) {
        if (compare.length < mMatchPattern.length) return false;
        for (int i = 0; i < mMatchPattern.length; i++) {
            if (mMatchPattern[i] != compare[i]) return false;
        }
        return true;
    }

    private boolean isPrefix(byte compare) {
        return mMatchPattern.length <= 1 && mMatchPattern[0] == compare;
    }

    public StartsWith(byte[] pattern, TARGET target) throws FilterInitException {
        if (target == TARGET.ANTICOL) throw new FilterInitException("Wrong constructor signature for Anticol data.");
        mTarget = target;
        mMatchPattern = pattern;
    }

    public StartsWith(byte[] pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target == TARGET.NFC) throw new FilterInitException("Wrong constructor signature for NFC data.");
        mTarget = target;
        mMatchPattern = pattern;
        mAnticolTarget = field;
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
