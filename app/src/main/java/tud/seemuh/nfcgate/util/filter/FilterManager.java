package tud.seemuh.nfcgate.util.filter;

import java.util.LinkedList;
import java.util.List;

import tud.seemuh.nfcgate.util.NfcComm;

/**
 * The FilterManager keeps track of all filters that are used on incoming and outgoing NFC traffic.
 */
public class FilterManager {
    private List<Filter> mHCEFilters     = new LinkedList<Filter>();
    private List<Filter> mCardFilters    = new LinkedList<Filter>();
    private List<Filter> mAnticolFilters = new LinkedList<Filter>();

    private boolean mHCENonEmpty     = false;
    private boolean mCardNonEmpty    = false;
    private boolean mAnticolNonEmpty = false;

    // Filter management
    /**
     * Add a Filter that should be invoked on HCE Data (data coming from the Reader)
     * @param filter The filter that should be invoked.
     */
    public void addHCEDataFilter(Filter filter) {
        mHCEFilters.add(filter);
        mHCENonEmpty = true;
    }

    /**
     * Add a Filter that should be invoked on Card Data (data coming from the Card)
     * @param filter The filter that should be invoked.
     */
    public void addCardDataFilter(Filter filter) {
        mCardFilters.add(filter);
        mCardNonEmpty = true;
    }

    /**
     * Add a filter that should be invoked on Anticollision data
     * @param filter The filter that should be invoked.
     */
    public void addAnticolDataFilter(Filter filter) {
        mAnticolFilters.add(filter);
        mAnticolNonEmpty = true;
    }

    // Actual filtering functions
    /**
     * Execute filters that are registered for HCE data
     * @param nfcdata The APDU that should be filtered
     * @return The filtered HCE data
     */
    public NfcComm filterHCEData(NfcComm nfcdata) {
        if (mHCENonEmpty) {
            if (nfcdata.getType() != NfcComm.Type.NFCBytes) return nfcdata;
            for (Filter f : mHCEFilters) {
                nfcdata = f.filter(nfcdata);
            }
        }
        return nfcdata;
    }

    /**
     * Execute filters that are registered for Card data
     * @param nfcdata The APDU that should be filtered
     * @return The filtered Card data
     */
    public NfcComm filterCardData(NfcComm nfcdata) {
        if (mCardNonEmpty) {
            if (nfcdata.getType() != NfcComm.Type.NFCBytes) return nfcdata;
            for (Filter f : mCardFilters) {
                nfcdata = f.filter(nfcdata);
            }
        }
        return nfcdata;
    }

    /**
     * Execute filters that are registered for Anticollision data
     * @param anticol NfcComm object containing anticol data
     */
    public NfcComm filterAnticolData(NfcComm anticol) {
        if (mAnticolNonEmpty) {
            if (anticol.getType() != NfcComm.Type.AnticolBytes) return anticol;
            for (Filter f : mAnticolFilters) {
                anticol = f.filter(anticol);
            }
        }
        return anticol;
    }
}
