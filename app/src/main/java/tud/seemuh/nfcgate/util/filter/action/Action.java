package tud.seemuh.nfcgate.util.filter.action;

import tud.seemuh.nfcgate.util.NfcComm;

/**
 * Interface for Actions, as used in the Filter infrastructure. Please check the relevant Wiki
 * article for more information.
 */
public interface Action {
    /**
     * Perform the requested action on the APDU data
     * @param apdu NFC Data
     * @return The modified APDU data as a byte[]
     */
    public byte[] performAction(byte[] apdu);

    /**
     * Perform the requested action on the Anticol data
     * @param anticol NfcComm object with Anticol data
     * @return Modified NfcComm object with new Anticol data
     */
    public NfcComm performAction(NfcComm anticol);
}
