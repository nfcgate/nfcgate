package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * An action to replace bytes in a message, starting at an offset
 */
public class ReplaceBytes implements Action {
    private TARGET mTarget;
    private ANTICOLFIELD mAnticolTarget;
    private byte[] mNewContent;
    private byte mNewContentByte;
    private int mOffset;

    public ReplaceBytes(byte[] content, int offset, TARGET target) throws FilterInitException {
        if (target != TARGET.NFC) throw new FilterInitException("Wrong constructor for target type");
        if (offset < 0) throw new FilterInitException("Offset must be positive");
        mTarget = target;
        mNewContent = content;
        mOffset = offset;
    }

    public ReplaceBytes(byte[] content, int offset, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target != TARGET.ANTICOL || field == ANTICOLFIELD.SAK)
            throw new FilterInitException("Wrong constructor for target type");
        if (offset < 0) throw new FilterInitException("Offset must be positive");
        mTarget = target;
        mNewContent = content;
        mOffset = offset;
        mAnticolTarget = field;
    }

    // Not strictly necessary, as SAK is only one byte anyway, so this is identical to ReplaceContent
    public ReplaceBytes(byte content, int offset, TARGET target, ANTICOLFIELD targetfield) throws FilterInitException {
        if (target != TARGET.ANTICOL || targetfield != ANTICOLFIELD.SAK)
            throw new FilterInitException("Wrong constructor for target type");
        if (offset < 0) throw new FilterInitException("Offset must be positive");
        mTarget = target;
        mNewContentByte = content;
        mAnticolTarget = targetfield;
    }

    private byte[] doReplacement(byte[] base) {
        // Check if Substitution would exceed byte array
        if (mOffset + mNewContent.length > base.length) return base;

        // Perform the substitution
        System.arraycopy(mNewContent, 0, base, mOffset, mNewContent.length);
        return base;
    }

    @Override
    public NfcComm performAction(NfcComm nfcdata) {
        if ((nfcdata.getType() == NfcComm.Type.NFCBytes && mTarget == TARGET.ANTICOL)
                || (nfcdata.getType() == NfcComm.Type.AnticolBytes && mTarget == TARGET.NFC)) {
            return nfcdata;
        }
        if (mTarget == TARGET.NFC) {
            nfcdata.setData(doReplacement(nfcdata.getData()));
        } else if (mAnticolTarget == ANTICOLFIELD.UID) {
            nfcdata.setUid(doReplacement(nfcdata.getUid()));
        } else if (mAnticolTarget == ANTICOLFIELD.ATQA) {
            nfcdata.setAtqa(doReplacement(nfcdata.getAtqa()));
        } else if (mAnticolTarget == ANTICOLFIELD.HIST) {
            nfcdata.setHist(doReplacement(nfcdata.getHist()));
        } else if (mAnticolTarget == ANTICOLFIELD.SAK) {
            nfcdata.setSak(mNewContentByte);
        }
        return nfcdata;
    }
}
