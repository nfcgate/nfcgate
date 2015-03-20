package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * Created by max on 20.03.15.
 */
public class EndsWith implements Conditional {
    private byte[] mMatchPattern;
    private byte mMatchByte;
    private TARGET mTarget;
    private ANTICOLFIELD mAnticolTarget;

    public EndsWith(byte[] pattern, TARGET target) throws FilterInitException {
        if (target == TARGET.ANTICOL) throw new FilterInitException("Wrong constructor signature for Anticol data.");
        mTarget = target;
        mMatchPattern = pattern;
    }

    public EndsWith(byte[] pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target == TARGET.NFC || field == ANTICOLFIELD.SAK)
            throw new FilterInitException("Wrong constructor signature for NFC data.");
        mTarget = target;
        mMatchPattern = pattern;
        mAnticolTarget = field;
    }

    public EndsWith(byte pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target == TARGET.NFC || field != ANTICOLFIELD.SAK)
            throw new FilterInitException("Wrong constructor signature for NFC data.");
        mTarget = target;
        mMatchByte = pattern;
        mAnticolTarget = field;
    }

    // Helper functions
    private boolean isSuffix(byte[] compare) {
        if (compare.length < mMatchPattern.length) return false;
        int offset = compare.length - mMatchPattern.length;
        for (int i = 0; i < mMatchPattern.length; i++) {
            if (mMatchPattern[i] != compare[offset + i]) return false;
        }
        return true;
    }

    private boolean isSuffix(byte compare) {
        return mMatchByte == compare;
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        if (mTarget == TARGET.NFC) {
            return (nfcdata.getType() == NfcComm.Type.NFCBytes) && isSuffix(nfcdata.getData());
        } else {
            if (nfcdata.getType() != NfcComm.Type.AnticolBytes) return false;
            if (mAnticolTarget == ANTICOLFIELD.UID) {
                return isSuffix(nfcdata.getUid());
            } else if (mAnticolTarget == ANTICOLFIELD.ATQA) {
                return isSuffix(nfcdata.getAtqa());
            } else if (mAnticolTarget == ANTICOLFIELD.HIST) {
                return isSuffix(nfcdata.getHist());
            } else if (mAnticolTarget == ANTICOLFIELD.SAK) {
                return isSuffix(nfcdata.getSak());
            }
        }
        return false;
    }
}
