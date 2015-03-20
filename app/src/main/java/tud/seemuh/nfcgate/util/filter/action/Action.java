package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;

/**
 * Interface for Actions, as used in the Filter infrastructure. Please check the relevant Wiki
 * article for more information.
 */
public interface Action {
    public enum TARGET {
        NFC,
        ANTICOL
    }

    public enum ANTICOLFIELD {
        UID,
        ATQA,
        SAK,
        HIST
    }

    /**
     * Perform the requested action on the NFC data
     * @param nfcdata NfcComm object with NFC data
     * @return Modified NfcComm object with new NFC data
     */
    public NfcComm performAction(NfcComm nfcdata);
}
