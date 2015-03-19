package tud.seemuh.nfcgate.util.filter;

/**
 * The FilterManager keeps track of all filters that are used on incoming and outgoing NFC traffic.
 */
public class FilterManager {
    // Filter management

    /**
     * Add a Filter that should be invoked on HCE Data (data coming from the Reader)
     * @param filter The filter that should be invoked.
     */
    public void addHCEDataFilter(Filter filter) {
        // TODO
    }

    /**
     * Add a Filter that should be invoked on Card Data (data coming from the Card)
     * @param filter The filter that should be invoked.
     */
    public void addCardDataFilter(Filter filter) {
        // TODO
    }

    /**
     * Add a filter that should be invoked on Anticollision data
     * @param filter The filter that should be invoked.
     */
    public void addAnticolDataFilter(Filter filter) {
        // TODO
    }

    // Actual filtering functions

    /**
     * Execute filters that are registered for HCE data
     * @param apdu The APDU that should be filtered
     */
    public void filterHCEData(byte[] apdu) {
        // TODO
    }

    /**
     * Execute filters that are registered for Card data
     * @param apdu The APDU that should be filtered
     */
    public void filterCardData(byte[] apdu) {
        // TODO
    }

    /**
     * Execute filters that are registered for Anticollision data
     * @param uid The UID of the Anticollision data
     * @param atqa The ATQA of the Anticollision data
     * @param sak The SAK of the Anticollision data
     * @param hist The Historical Bytes of the Anticollision data
     */
    public void filterAnticolData(byte[] uid, byte[] atqa, byte sak, byte[] hist) {
        // TODO
    }
}
