package tud.seemuh.nfcgate.util.filter.conditional;

import java.util.Arrays;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * A simple conditional that checks if the provided data is equal to the provided pattern.
 */
public class Equals implements Conditional {
    private TARGET mTarget;
    private byte[] mMatchPattern;
    private byte mMatchByte;
    private ANTICOLFIELD mAnticolTarget;

    public Equals(byte[] pattern, TARGET target) throws FilterInitException {
        if (target == TARGET.ANTICOL) throw new FilterInitException("Wrong constructor signature for Anticol data.");
        mTarget = target;
        mMatchPattern = pattern;
    }

    public Equals(byte[] pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target == TARGET.NFC || field == ANTICOLFIELD.SAK) throw new FilterInitException("Wrong constructor signature for NFC data.");
        mTarget = target;
        mMatchPattern = pattern;
        mAnticolTarget = field;
    }

    public Equals(byte pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target == TARGET.NFC || field != ANTICOLFIELD.SAK) throw new FilterInitException("Wrong constructor signature for NFC data.");
        mTarget = target;
        mMatchByte = pattern;
        mAnticolTarget = field;
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        if (mTarget == TARGET.NFC) {
            return (nfcdata.getType() == NfcComm.Type.NFCBytes) && Arrays.equals(nfcdata.getData(), mMatchPattern);
        } else {
            if (nfcdata.getType() != NfcComm.Type.AnticolBytes) return false;
            if (mAnticolTarget == ANTICOLFIELD.UID) {
                return Arrays.equals(nfcdata.getUid(), mMatchPattern);
            } else if (mAnticolTarget == ANTICOLFIELD.ATQA) {
                return Arrays.equals(nfcdata.getAtqa(), mMatchPattern);
            } else if (mAnticolTarget == ANTICOLFIELD.HIST) {
                return Arrays.equals(nfcdata.getHist(), mMatchPattern);
            } else if (mAnticolTarget == ANTICOLFIELD.SAK) {
                return nfcdata.getSak() == mMatchByte;
            }
        }
        return false;
    }
}
