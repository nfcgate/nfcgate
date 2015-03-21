package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * An action to replace bytes in a message, starting at an offset
 */
public class ReplaceBytes extends Action {
    public ReplaceBytes(byte[] content, int offset, TARGET target) throws FilterInitException {
        super(content, offset, target);
    }

    public ReplaceBytes(byte[] content, int offset, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(content, offset, target, field);
    }

    // Not strictly necessary, as SAK is only one byte anyway, so this is identical to ReplaceContent
    public ReplaceBytes(byte content, int offset, TARGET target, ANTICOLFIELD targetfield) throws FilterInitException {
        super(content, offset, target, targetfield);
    }

    private byte[] doReplacement(byte[] base) {
        // Check if Substitution would exceed byte array
        if (mOffset + mNewContent.length > base.length) return base;

        // Perform the substitution
        System.arraycopy(mNewContent, 0, base, mOffset, mNewContent.length);
        return base;
    }

    @Override
    protected NfcComm modifyNfcData(NfcComm nfcdata) {
        nfcdata.setData(doReplacement(nfcdata.getData()));
        return nfcdata;
    }

    @Override
    protected NfcComm modifyUidData(NfcComm nfcdata) {
        nfcdata.setUid(doReplacement(nfcdata.getUid()));
        return nfcdata;
    }

    @Override
    protected NfcComm modifyAtqaData(NfcComm nfcdata) {
        nfcdata.setAtqa(doReplacement(nfcdata.getAtqa()));
        return nfcdata;
    }

    @Override
    protected NfcComm modifyHistData(NfcComm nfcdata) {
        nfcdata.setHist(doReplacement(nfcdata.getHist()));
        return nfcdata;
    }

    @Override
    protected NfcComm modifySakData(NfcComm nfcdata) {
        nfcdata.setSak(mNewContentByte);
        return nfcdata;
    }
}
