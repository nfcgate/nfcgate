package tud.seemuh.nfcgate.util.filter.conditional;

import java.util.Arrays;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * A simple conditional that checks if the provided data is equal to the provided pattern.
 */
public class Equals extends Conditional {
    public Equals(byte[] pattern, TARGET target) throws FilterInitException {
        super(pattern, target);
    }

    public Equals(byte[] pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(pattern, target, field);
    }

    public Equals(byte pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(pattern, target, field);
    }

    @Override
    protected boolean checkNfcData(NfcComm nfcdata) {
        return Arrays.equals(nfcdata.getData(), mMatchPattern);
    }

    @Override
    protected boolean checkUidData(NfcComm nfcdata) {
        return false;
        // FIXME
        //return Arrays.equals(nfcdata.getUid(), mMatchPattern);
    }

    @Override
    protected boolean checkAtqaData(NfcComm nfcdata) {
        return false;
        // FIXME
        //return Arrays.equals(nfcdata.getAtqa(), mMatchPattern);
    }

    @Override
    protected boolean checkHistData(NfcComm nfcdata) {
        return false;
        // FIXME
        //return Arrays.equals(nfcdata.getHist(), mMatchPattern);
    }

    @Override
    protected boolean checkSakData(NfcComm nfcdata) {
        return false;
        // FIXME
        //return nfcdata.getSak() == mMatchByte;
    }
}
