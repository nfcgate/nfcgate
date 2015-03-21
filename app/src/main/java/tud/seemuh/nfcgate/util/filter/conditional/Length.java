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
    protected boolean checkNfcData(NfcComm nfcdata) {
        return nfcdata.getData().length == mInteger;
    }

    @Override
    protected boolean checkUidData(NfcComm nfcdata) {
        return nfcdata.getUid().length == mInteger;
    }

    @Override
    protected boolean checkAtqaData(NfcComm nfcdata) {
        return nfcdata.getAtqa().length == mInteger;
    }

    @Override
    protected boolean checkHistData(NfcComm nfcdata) {
        return nfcdata.getHist().length == mInteger;
    }

    @Override
    protected boolean checkSakData(NfcComm nfcdata) {
        return mInteger == 1;
    }
}
