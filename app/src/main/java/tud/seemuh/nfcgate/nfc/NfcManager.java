package tud.seemuh.nfcgate.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.network.NetworkManager;
import tud.seemuh.nfcgate.network.data.NetworkStatus;
import tud.seemuh.nfcgate.nfc.config.Technologies;
import tud.seemuh.nfcgate.nfc.hce.ApduService;
import tud.seemuh.nfcgate.nfc.hce.DaemonConfiguration;
import tud.seemuh.nfcgate.nfc.modes.BaseMode;
import tud.seemuh.nfcgate.nfc.reader.IsoDepReader;
import tud.seemuh.nfcgate.nfc.reader.NFCTagReader;
import tud.seemuh.nfcgate.nfc.reader.NfcAReader;
import tud.seemuh.nfcgate.nfc.reader.NfcBReader;
import tud.seemuh.nfcgate.nfc.reader.NfcFReader;
import tud.seemuh.nfcgate.nfc.reader.NfcVReader;
import tud.seemuh.nfcgate.util.NfcComm;

public class NfcManager implements NfcAdapter.ReaderCallback, NetworkManager.Callback {
    private static final String TAG = "NfcManager";

    // singleton
    private static NfcManager mInstance;
    public static NfcManager getInstance() {
        return mInstance;
    }

    // references
    private MainActivity mActivity;
    private NfcAdapter mAdapter;
    private ApduService mApduService;
    private DaemonConfiguration mDaemon;
    private NetworkManager mNetwork;

    // state
    private boolean mReaderMode = false;
    private boolean mPollingEnabled = true;
    private NFCTagReader mReader;
    private BaseMode mMode = null;

    public NfcManager(MainActivity activity) {
        mActivity = activity;
        mAdapter = NfcAdapter.getDefaultAdapter(activity);
        mDaemon = new DaemonConfiguration(mActivity);
        mNetwork = new NetworkManager(mActivity, this);

        // save instance for service communication
        mInstance = this;
    }

    /**
     * Indicates whether this device has NFC capability
     */
    public boolean hasNfc() {
        return mAdapter != null;
    }

    /**
     * Indicates whether this device has HCE
     */
    public boolean hasHce() {
        return mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION);
    }

    /**
     * Indicates whether the Xposed module is enabled
     * This is hooked by the module to return true
     */
    public static boolean isHookLoaded() {
        return false;
    }

    /**
     * Indicates whether NFC is enabled or disabled
     */
    public boolean isEnabled() {
        return hasNfc() && mAdapter.isEnabled();
    }

    /**
     * Enable or disable reader mode
     */
    public void setReaderMode(boolean enabled) {
        mReaderMode = enabled;

        // apply setting if nfc is enabled
        if (isEnabled())
            enableDisableReaderMode();
    }

    public void startMode(BaseMode mode) {
        mMode = mode;

        // enable
        mode.setManager(this);
        mode.onEnable();
    }

    public void stopMode() {
        if (mMode != null)
            mMode.onDisable();

        mMode = null;
    }

    /**
     * Get current network manager
     */
    public NetworkManager getNetwork() {
        return mNetwork;
    }

    /**
     * Allows the ApduService to set its reference in the manager
     */
    public void setApduService(ApduService apduService) {
        mApduService = apduService;
    }

    /**
     * Resume NFC activity
     */
    public void onResume() {
        if (isEnabled()) {
            enableForegroundDispatch();
            enableDisableReaderMode();
        }
    }

    /**
     * Pause NFC activity
     */
    public void onPause() {
        if (isEnabled()) {
            disableForegroundDispatch();
        }
    }

    /**
     * Called for every discovered tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        // Select technology by tag
        mReader = NFCTagReader.create(tag);

        if (mReader != null) {
            Log.i(TAG, "Discovered new Tag: " + mReader.getClass().getName());

            // connect to tag
            mReader.connect();

            // handle initial card data according to mode
            handleData(false, new NfcComm(true, true, mReader.getConfig().build()));
        }
    }

    /**
     * Handles card data by mode
     */
    public void handleData(boolean isForeign, NfcComm data) {
        Log.v(TAG, "handleData foreign: " + isForeign + ", " + data.getData().length + " bytes");

        if (mMode != null)
            mMode.onData(isForeign, data);
        else
            mReader.close();
    }

    /**
     * Stops polling for new tags
     */
    public void disablePolling() {
        if (mPollingEnabled)
            mDaemon.setPolling(false);

        mPollingEnabled = false;
    }

    /**
     * Starts polling for new tags
     */
    public void enablePolling() {
        if (!mPollingEnabled)
            mDaemon.setPolling(true);

        mPollingEnabled = true;
    }

    /**
     * Start capturing on-device NFC data
     */
    public void enableCapture() {
        mDaemon.setCapture(true);
    }

    /**
     * Stop capturing on-device NFC data
     */
    public void disableCapture() {
        mDaemon.setCapture(false);
    }

    /**
     * Applies own or foreign data
     */
    public void applyData(NfcComm data) {
        Log.v(TAG, "applyData of " + data.getData().length + " bytes");

        if (data.isInitial()) {
            // send configuration to service
            mDaemon.setConfig(data.getData());
        }
        else if (mReaderMode) {
            // send data to tag and get reply
            byte[] reply = mReader.transceive(data.getData());

            // send reply
            if (reply == null)
                Log.w(TAG, "Empty TAG reply");
            else
                handleData(false, new NfcComm(true, false, reply));
        }
        else {
            // send data to reader
            if (mApduService == null)
                Log.w(TAG, "No APDU Service connected");
            else
                mApduService.sendResponse(data.getData());
        }
    }

    /**
     * Forward status to current mode
     */
    @Override
    public void onNetworkStatus(NetworkStatus status) {
        if (mMode != null)
            mMode.onNetworkStatus(status);
    }

    // PRIVATE

    @Override
    public void onReceive(final NfcComm data) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // handle data on UI thread
                handleData(true, data);
            }
        });
    }

    /**
     * Enable or disable reader mode for this activity
     */
    private void enableDisableReaderMode() {
        if (mReaderMode) {
            // Read all techs, skip NDEF to skip P2P
            int flags = NfcAdapter.FLAG_READER_NFC_A |
                        NfcAdapter.FLAG_READER_NFC_B |
                        NfcAdapter.FLAG_READER_NFC_F |
                        NfcAdapter.FLAG_READER_NFC_V |
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

            mAdapter.enableReaderMode(mActivity, this, flags, null);
        }
        else {
            mAdapter.disableReaderMode(mActivity);
        }
    }

    /**
     * Configure NFC to deliver new tags using the given pending intent. Also gives us priority
     * over all other system apps. Call in onResume()
     */
    private void enableForegroundDispatch() {
        Intent intent = new Intent(mActivity, mActivity.getClass());
        PendingIntent pendingIntent =
                PendingIntent.getActivity(mActivity, 0, intent, 0);

        // Register the activity, pass null techLists as a wildcard
        mAdapter.enableForegroundDispatch(mActivity, pendingIntent, null, null);
    }

    /**
     * Disables priority dispatching. Call in onPause()
     */
    private void disableForegroundDispatch() {
        // Disable dispatch as documentation requires
        mAdapter.disableForegroundDispatch(mActivity);
    }
}
