package tud.seemuh.nfcgate.util.filter;

import tud.seemuh.nfcgate.util.filter.action.Action;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;

/**
 * Filters are used to change APDU bytes on the fly. Please check the detailed documentation in the
 * wiki article on Filters.
 */
public class Filter {
    private Conditional mCond;
    private Action mAction;

    public Filter(Conditional cond, Action action) {
        mCond = cond;
        mAction = action;
    }

    public byte[] filter(byte[] apdu) {
        if (mCond.applies(apdu)) {
            return mAction.performAction(apdu);
        } else {
            return apdu;
        }
    }
}
