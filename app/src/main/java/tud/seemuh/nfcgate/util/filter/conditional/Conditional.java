package tud.seemuh.nfcgate.util.filter.conditional;

import tud.seemuh.nfcgate.util.NfcComm;

/**
 * Interface for conditionals, as used in the Filter infrastructure. Please check the Wiki for more
 * Information.
 */
public interface Conditional {
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
     * Check if the conditional matches the provided NfcComm data
     * @param nfcdata NfcComm object with NFC data
     * @return True if the conditional matches, false otherwise
     */
    public boolean applies(NfcComm nfcdata);
}
