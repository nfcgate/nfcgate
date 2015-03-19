package tud.seemuh.nfcgate.util.filter.action;

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
}
