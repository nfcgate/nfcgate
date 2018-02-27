package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

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
    protected NfcComm modifyNfcData(NfcComm nfcdata) {
        nfcdata.setData(mNewContent);
        return nfcdata;
    }

    @Override
    protected NfcComm modifyUidData(NfcComm nfcdata) {
        //nfcdata.setUid(mNewContent); // FIXME:
        return nfcdata;
    }

    @Override
    protected NfcComm modifyAtqaData(NfcComm nfcdata) {
        //nfcdata.setAtqa(mNewContent);
        return nfcdata;
    }

    @Override
    protected NfcComm modifyHistData(NfcComm nfcdata) {
        //nfcdata.setHist(mNewContent);
        return nfcdata;
    }

    @Override
    protected NfcComm modifySakData(NfcComm nfcdata) {
        //nfcdata.setSak(mNewContentByte);
        return nfcdata;
    }
}
