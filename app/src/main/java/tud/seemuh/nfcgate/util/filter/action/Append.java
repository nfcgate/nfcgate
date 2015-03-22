package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * Action to append data to something
 */
public class Append extends Action {
    public Append(byte[] content, TARGET target) throws FilterInitException {
        super(content, target);
    }

    // No constructor for Anticol bytes => They cannot be appended to (fixed length)

    // Helper function to do the actual appending
    private byte[] doAppend(byte[] base) {
        byte[] result = new byte[base.length + mNewContent.length];
        System.arraycopy(base, 0, result, 0, base.length);
        System.arraycopy(mNewContent, 0, result, base.length, mNewContent.length);
        return result;
    }

    @Override
    protected NfcComm modifyNfcData(NfcComm nfcdata) {
        nfcdata.setData(doAppend(nfcdata.getData()));
        return nfcdata;
    }

    // Anticol cannot be appended to => Return the original value
    @Override
    protected NfcComm modifyUidData(NfcComm nfcdata) {
        return nfcdata;
    }

    @Override
    protected NfcComm modifyAtqaData(NfcComm nfcdata) {
        return nfcdata;
    }

    @Override
    protected NfcComm modifyHistData(NfcComm nfcdata) {
        return nfcdata;
    }

    @Override
    protected NfcComm modifySakData(NfcComm nfcdata) {
        return nfcdata;
    }
}
