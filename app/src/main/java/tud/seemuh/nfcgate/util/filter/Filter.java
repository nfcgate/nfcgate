package tud.seemuh.nfcgate.util.filter;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.action.Action;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;

/**
 * Filters are used to change APDU bytes on the fly. Please check the detailed documentation in the
 * wiki article on Filters.
 */
public class Filter {
    private Conditional mCond;
    private Action mAction;

    /**
     * Initialize a Filter object with a Conditional and an Action
     * @param cond The conditional that should be checked
     * @param action The action that should be performed
     */
    public Filter(Conditional cond, Action action) {
        mCond = cond;
        mAction = action;
    }

    /**
     * Filter NFC data according to Conditional and Action
     * @param nfcdata NfcComm object with NFC data
     * @return Potentially modified NfcComm object with NFC data
     */
    public NfcComm filter(NfcComm nfcdata) {
        if (mCond.applies(nfcdata)) {
            return mAction.performAction(nfcdata);
        } else {
            return nfcdata;
        }
    }
}
