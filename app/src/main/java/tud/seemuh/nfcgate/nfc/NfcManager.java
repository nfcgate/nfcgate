package tud.seemuh.nfcgate.nfc;

import android.nfc.Tag;

import java.util.concurrent.BlockingQueue;

import tud.seemuh.nfcgate.network.HighLevelNetworkHandler;
import tud.seemuh.nfcgate.nfc.hce.ApduService;
import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterManager;
import tud.seemuh.nfcgate.util.sink.SinkManager;

/**
 * The NFC Manager is responsible for all NFC Interactions.
 */
public class NfcManager {
    // Reference setters

    /**
     * Set the Reference to the NFC Tag
     * @param tag The NFC Tag object
     */
    public void setTag(Tag tag) {
        // TODO
    }

    /**
     * Set the Reference to the ApduService
     * @param apduService The ApduService object
     */
    public void setApduService(ApduService apduService) {
        // TODO
    }

    /**
     * Set the Reference to the SinkManager
     * @param sinkManager The SinkManager object
     * @param smq The BlockingQueue connected with the SinkManager
     */
    public void setSinkManager(SinkManager sinkManager, BlockingQueue<NfcComm> smq) {
        // TODO
    }

    /**
     * Set the reference to the FilterManager
     * @param filterManager The FilterManager object
     */
    public void setFilterManager(FilterManager filterManager) {
        // TODO
    }

    /**
     * Set the reference to the HighLevelNetworkHandler
     * @param netHandler The HighLevelNetworkHandler object
     */
    public void setNetworkHandler(HighLevelNetworkHandler netHandler) {
        // TODO
    }

    // NFC Interactions

    /**
     * Send NFC data to the card
     * @param nfcdata NFcComm object containing the message for the card
     */
    public void sendToCard(NfcComm nfcdata) {
        // TODO
    }

    /**
     * Send NFC data to the Reader
     * @param nfcdata NfcComm object containing the message for the Reader
     */
    public void sendToReader(NfcComm nfcdata) {
        // TODO
    }

    // HCE Handler
    /**
     * Called by the ApduService when a new APDU is received
     * @param nfcdata An NfcComm object containing the APDU
     */
    public void handleHCEData(NfcComm nfcdata) {
        // TODO
    }

    // Anticol
    /**
     * Get the Anticollision data of the attached card
     * @return NfcComm object with anticol data
     */
    public NfcComm getAnticolData() {
        // TODO
        return null;
    }

    /**
     * Set the Anticollision data in the native code patch
     * @param anticol NfcComm object containing the Anticol data
     */
    public void setAnticolData(NfcComm anticol) {
        // TODO
    }
}