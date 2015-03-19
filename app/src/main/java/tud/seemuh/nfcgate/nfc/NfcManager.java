package tud.seemuh.nfcgate.nfc;

import android.nfc.Tag;
import android.util.Log;

import java.util.concurrent.BlockingQueue;

import tud.seemuh.nfcgate.network.HighLevelNetworkHandler;
import tud.seemuh.nfcgate.nfc.hce.ApduService;
import tud.seemuh.nfcgate.nfc.reader.BCM20793Workaround;
import tud.seemuh.nfcgate.nfc.reader.IsoDepReader;
import tud.seemuh.nfcgate.nfc.reader.NFCTagReader;
import tud.seemuh.nfcgate.nfc.reader.NfcAReader;
import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterManager;
import tud.seemuh.nfcgate.util.sink.SinkManager;

/**
 * The NFC Manager is responsible for all NFC Interactions.
 */
public class NfcManager {
    private final String TAG = "NfcManager";

    // NFC Objects
    private Tag mTag;
    private NFCTagReader mReader;
    private ApduService mApduService;

    // Sink Manager
    private SinkManager mSinkManager;
    private BlockingQueue<NfcComm> mSinkManagerQueue;

    // Filter Manager
    private FilterManager mFilterManager;

    // Network Handler
    private HighLevelNetworkHandler mNetworkHandler;

    // Workaround
    private BCM20793Workaround mBroadcomWorkaroundRunnable;
    private Thread mBroadcomWorkaroundThread;


    // Private helper functions
    private void notifySinkManager(NfcComm nfcdata) {
        if (mSinkManagerQueue == null) {
            Log.e(TAG, "notifySinkManager: Trying to notify, but Queue is still null. Ignoring.");
            return;
        }
        try {
            mSinkManagerQueue.add(nfcdata);
        } catch (IllegalStateException e) {
            Log.e(TAG, "notifySinkManager: Tried to notify sm, but queue is full. Ignoring.");
        }
    }

    // TODO Logging
    private NfcComm handleAnticolDataCommon(NfcComm nfcdata) {
        nfcdata = mFilterManager.filterAnticolData(nfcdata);
        notifySinkManager(nfcdata);
        return nfcdata;
    }

    private NfcComm handleHceDataCommon(NfcComm nfcdata) {
        nfcdata = mFilterManager.filterHCEData(nfcdata);
        notifySinkManager(nfcdata);
        return nfcdata;
    }

    private NfcComm handleCardDataCommon(NfcComm nfcdata) {
        nfcdata = mFilterManager.filterCardData(nfcdata);
        notifySinkManager(nfcdata);
        return nfcdata;
    }

    // Reference setters
    /**
     * Set the Reference to the NFC Tag
     * @param tag The NFC Tag object
     */
    public void setTag(Tag tag) {
        mTag = tag;

        boolean found_supported_tag = false;

        // Identify tag type
        for(String type: tag.getTechList()) {
            Log.i(TAG, "setTag: Tag TechList: " + type);
            if("android.nfc.tech.IsoDep".equals(type)) {
                found_supported_tag = true;

                mReader = new IsoDepReader(tag);
                Log.d(TAG, "setTag: Chose IsoDep technology.");
                break;
            } else if("android.nfc.tech.NfcA".equals(type)) {
                found_supported_tag = true;

                mReader = new NfcAReader(tag);
                Log.d(TAG, "setTag: Chose NfcA technology.");
                break;
            }
        }

        if (found_supported_tag) {
            // Start the workaround thread, if needed
            startWorkaround(mTag);

            // TODO Usually, we send Anticol data at this point
        }
    }

    /**
     * Set the Reference to the ApduService
     * @param apduService The ApduService object
     */
    public void setApduService(ApduService apduService) {
        mApduService = apduService;
    }

    /**
     * Set the Reference to the SinkManager
     * @param sinkManager The SinkManager object
     * @param smq The BlockingQueue connected with the SinkManager
     */
    public void setSinkManager(SinkManager sinkManager, BlockingQueue<NfcComm> smq) {
        mSinkManager = sinkManager;
        mSinkManagerQueue = smq;
    }

    /**
     * Set the reference to the FilterManager
     * @param filterManager The FilterManager object
     */
    public void setFilterManager(FilterManager filterManager) {
        mFilterManager = filterManager;
    }

    /**
     * Set the reference to the HighLevelNetworkHandler
     * @param netHandler The HighLevelNetworkHandler object
     */
    public void setNetworkHandler(HighLevelNetworkHandler netHandler) {
        mNetworkHandler = netHandler;
    }

    // NFC Interactions
    /**
     * Send NFC data to the card
     * @param nfcdata NFcComm object containing the message for the card
     */
    public void sendToCard(NfcComm nfcdata) {
        nfcdata = handleHceDataCommon(nfcdata);
        // TODO
    }

    /**
     * Send NFC data to the Reader
     * @param nfcdata NfcComm object containing the message for the Reader
     */
    public void sendToReader(NfcComm nfcdata) {
        nfcdata = handleCardDataCommon(nfcdata);
        // TODO
    }

    // HCE Handler
    /**
     * Called by the ApduService when a new APDU is received
     * @param nfcdata An NfcComm object containing the APDU
     */
    public void handleHCEData(NfcComm nfcdata) {
        nfcdata = handleHceDataCommon(nfcdata);
        // TODO
    }

    // Anticol
    /**
     * Get the Anticollision data of the attached card
     * @return NfcComm object with anticol data
     */
    public NfcComm getAnticolData() {
        // Get Anticol data
        byte[] uid = mReader.getUID();
        byte[] atqa = mReader.getAtqa();
        byte sak = mReader.getSak();
        byte[] hist = mReader.getHistoricalBytes();

        // Create NfcComm object
        NfcComm anticol = new NfcComm(atqa, sak, hist, uid);

        // Pass NfcComm object through Filter
        anticol = handleAnticolDataCommon(anticol);

        // Return NfcComm object w/ anticol data
        return anticol;
    }

    /**
     * Set the Anticollision data in the native code patch
     * @param anticol NfcComm object containing the Anticol data
     */
    public void setAnticolData(NfcComm anticol) {
        anticol = handleAnticolDataCommon(anticol);
        // TODO
    }

    // Workaround Handling
    /**
     * Start workaround, if needed
     * @param tag The NFC Tag object
     */
    public void startWorkaround(Tag tag) {
        // Check if the device is running a specific Broadcom chipset (used in the Nexus 4, for example)
        // The drivers for this chipset contain a bug which lead to DESFire cards being set into a specific mode
        // We want to avoid that, so we use a workaround. This starts another thread that prevents
        // the keepalive function from running and thus prevents it from setting the mode of the card
        // Other devices work fine without this workaround, so we only activate it on bugged chipsets
        // TODO Only activate for DESFire cards
        if (BCM20793Workaround.workaroundNeeded()) {
            Log.i(TAG, "setTag: Problematic broadcom chip found, activate workaround");

            // Initialize a runnable object
            mBroadcomWorkaroundRunnable = new BCM20793Workaround(tag);

            // Start up a new thread
            mBroadcomWorkaroundThread = new Thread(mBroadcomWorkaroundRunnable);
            mBroadcomWorkaroundThread.start();

            // Notify the Handler
            if (mNetworkHandler != null) mNetworkHandler.notifyCardWorkaroundConnected();
        } else {
            Log.i(TAG, "setTag: No problematic broadcom chipset found, leaving workaround inactive");
        }
    }

    /**
     * Stop the Broadcom Workaround thread, if it exists.
     */
    public void stopWorkaround() {
        if (mBroadcomWorkaroundThread != null) mBroadcomWorkaroundThread.interrupt();
        mBroadcomWorkaroundThread = null;
        mBroadcomWorkaroundRunnable = null;
    }
}