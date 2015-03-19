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
     * Filter APDU data according to Conditional and Action
     * @param apdu NFC Data
     * @return Potentially modified NFC Data
     */
    public byte[] filter(byte[] apdu) {
        if (mCond.applies(apdu)) {
            return mAction.performAction(apdu);
        } else {
            return apdu;
        }
    }

    /**
     * Filter Anticol data according to Conditional and Action
     * @param anticol NfcComm object with Anticol data
     * @return Potentially modified NfcComm object with Anticol data
     */
    public NfcComm filter(NfcComm anticol) {
        if (mCond.applies(anticol)) {
            return mAction.performAction(anticol);
        } else {
            return anticol;
        }
    }
}
