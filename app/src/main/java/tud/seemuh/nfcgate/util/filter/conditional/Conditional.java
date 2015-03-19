package tud.seemuh.nfcgate.util.filter.conditional;

/**
 * Interface for conditionals, as used in the Filter infrastructure. Please check the Wiki for more
 * Information.
 */
public interface Conditional {
    /**
     * Check if the conditional matches the provided APDU.
     * @param apdu NFC data
     * @return True if the conditional matches, False otherwise
     */
    public boolean applies(byte[] apdu);
}
