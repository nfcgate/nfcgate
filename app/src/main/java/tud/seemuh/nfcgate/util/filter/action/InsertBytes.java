package tud.seemuh.nfcgate.util.filter.action;

import android.util.Log;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * An action to insert bytes into a message, starting at an offset
 */
public class InsertBytes extends Action {
    public InsertBytes(byte[] content, int offset, TARGET target) throws FilterInitException {
        super(content, offset, target);
    }

    // Cannot insert into Anticol bytes (fixed length), so we will not provide a constructor for it.

    private byte[] doInsert(byte[] base) {
        // If the offset is out of bounds, return the input
        if (mOffset > base.length) {
            Log.e("InsertBytes", "doInsert: Offset > input length, doing nothing.");
            return base;
        }

        // Allocate new byte[]
        byte[] result = new byte[base.length + mNewContent.length];

        // Perform the insertion
        if (mOffset > 0) {
            System.arraycopy(base, 0, result, 0, mOffset);
        }
        System.arraycopy(mNewContent, 0, result, mOffset, mNewContent.length);
        System.arraycopy(base, mOffset, result, mOffset+mNewContent.length, base.length - mOffset);

        // Return new byte[]
        return result;
    }

    @Override
    protected NfcComm modifyNfcData(NfcComm nfcdata) {
        nfcdata.setData(doInsert(nfcdata.getData()));
        return nfcdata;
    }

    // Can't insert into Anticol, so returning nfcdata here
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
