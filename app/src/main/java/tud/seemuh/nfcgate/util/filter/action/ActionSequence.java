package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;

/**
 * Perform a Sequence of actions, one after another.
 */
public class ActionSequence extends Action {
    public ActionSequence(Action... act) throws FilterInitException {
        super(act);
    }

    @Override
    public NfcComm performAction(NfcComm nfcdata) {
        for (Action ac : mActionList) {
            nfcdata = ac.performAction(nfcdata);
        }
        return nfcdata;
    }

    // Functions not needed, but have to be implemented to satisfy the interface
    @Override
    protected NfcComm modifyNfcData(NfcComm nfcdata) {
        return null;
    }

    @Override
    protected NfcComm modifyUidData(NfcComm nfcdata) {
        return null;
    }

    @Override
    protected NfcComm modifyAtqaData(NfcComm nfcdata) {
        return null;
    }

    @Override
    protected NfcComm modifyHistData(NfcComm nfcdata) {
        return null;
    }

    @Override
    protected NfcComm modifySakData(NfcComm nfcdata) {
        return null;
    }
}
