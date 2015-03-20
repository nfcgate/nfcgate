package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * Conditional that always returns true, as long as the provided NfcComm object is of the correct
 * type.
 */
public class All implements Conditional {
    private TARGET mTarget;

    public All(TARGET target) throws FilterInitException {
        if (target == TARGET.ANTICOL) throw new FilterInitException("Wrong constructor signature for Anticol data.");
        mTarget = target;
    }

    public All(TARGET target, ANTICOLFIELD field) throws FilterInitException {
        if (target == TARGET.NFC) throw new FilterInitException("Wrong constructor signature for NFC data.");
        mTarget = target;
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        return (nfcdata.getType() == NfcComm.Type.NFCBytes && mTarget == TARGET.NFC)
                || (nfcdata.getType() == NfcComm.Type.AnticolBytes && mTarget == TARGET.ANTICOL);
    }
}
