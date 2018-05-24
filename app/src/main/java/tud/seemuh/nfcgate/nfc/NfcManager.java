package tud.seemuh.nfcgate.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
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
import tud.seemuh.nfcgate.nfc.config.Technologies;
import tud.seemuh.nfcgate.nfc.hce.ApduService;
import tud.seemuh.nfcgate.nfc.hce.DaemonConfiguration;
import tud.seemuh.nfcgate.nfc.reader.IsoDepReader;
import tud.seemuh.nfcgate.nfc.reader.NFCTagReader;
import tud.seemuh.nfcgate.nfc.reader.NfcAReader;
import tud.seemuh.nfcgate.nfc.reader.NfcBReader;
import tud.seemuh.nfcgate.nfc.reader.NfcFReader;
import tud.seemuh.nfcgate.nfc.reader.NfcVReader;
import tud.seemuh.nfcgate.util.NfcComm;

public class NfcManager implements NfcAdapter.ReaderCallback, NetworkManager.Callback {
    private final String TAG = "NfcManager";

    // singleton
    static NfcManager mInstance;
    public static NfcManager getInstance() {
        return mInstance;
    }

    // callbacks
    public interface Callback {
        void notify(NfcComm data);
    }
    private Callback mCallback = null;

    // references
    private MainActivity mActivity;
    private NfcAdapter mAdapter;
    private ApduService mApduService;
    private DaemonConfiguration mDaemon;
    private NetworkManager mNetwork;

    // state
    private boolean mReaderMode = false;
    private boolean mPollingEnabled = true;
    private Tag mTag;
    private NFCTagReader mReader;

    // mode
    public enum Mode {
        None,
        Clone,
        Relay,
        Replay
    }
    private Mode mMode = Mode.None;

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
     * Indicates whether the XPosed module is enabled
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
        enableDisableReaderMode();
    }

    /**
     * Set current handling mode
     */
    public void setMode(Mode mode) {
        mMode = mode;
    }

    public void enableCloneMode() {
        mMode = Mode.Clone;

        // enable polling because we are looking for a tag
        enablePolling();
    }

    /**
     * Allows the ApduService to set its reference in the manager
     */
    public void setApduService(ApduService apduService) {
        mApduService = apduService;
    }

    /**
     * Adds the specified data callback to the list of callbacks
     */
    public void setCallback(Callback cb) {
        mCallback = cb;
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
        mTag = tag;
        mReader = fromTag();

        if (mReader != null) {
            // handle initial card data according to mode
            handleData(new NfcComm(true, mReader.getConfig().build()));
        }
    }

    /**
     * Handles card data by mode
     */
    public void handleData(NfcComm data) {
        // pass initial data through callbacks
        notifyCallbacks(data);

        switch (mMode) {
            case Clone:
                // clone tag and immediately disable polling to avoid detecting same tag again
                applyData(data);
                disablePolling();
                break;
            case Relay:
                if (data.isCard() && mReaderMode || !data.isCard() && !mReaderMode)
                    // send own data over network
                    mNetwork.send(data);
                else
                    // apply foreign data
                    applyData(data);
                break;
            default:
                // drop
                mReader.closeConnection();
                break;
        }
    }

    // PRIVATE

    private void enablePolling() {
        if (!mPollingEnabled)
            mDaemon.enablePolling();

        mPollingEnabled = true;
    }

    private void disablePolling() {
        mDaemon.disablePolling();
        mPollingEnabled = false;
    }

    /**
     * Applies own or foreign data
     */
    private void applyData(NfcComm data) {
        if (data.isInitial()) {
            // upload to service and enable
            mDaemon.upload(data.getData());
            mDaemon.enable();
        }
        else if (mReaderMode) {
            // send data to tag and get reply
            byte[] reply = mReader.sendCmd(data.getData());
            if (reply == null) {
                // TODO: errorhandling
            }

            // send reply
            handleData(new NfcComm(true, reply));
        }
        else {
            // send data to reader
            if (mApduService != null)
                mApduService.sendResponse(data.getData());
            // else TODO: errorhandling

        }
    }

    /**
     * Notifies all callbacks of the data
     */
    private void notifyCallbacks(NfcComm data) {
        if (mCallback != null)
            mCallback.notify(data);
    }

    /**
     * Picks the highest available technology for a given Tag
     */
    private NFCTagReader fromTag() {
        List<String> technologies = Arrays.asList(mTag.getTechList());

        // look for higher layer technology
        if (technologies.contains(Technologies.IsoDep)) {
            // an IsoDep tag can be backed by either NfcA or NfcB technology
            if (technologies.contains(Technologies.A))
                return new IsoDepReader(mTag, NfcA.get(mTag));
            else if (technologies.contains(Technologies.B))
                return new IsoDepReader(mTag, NfcB.get(mTag));
            else
                Log.e(TAG, "Unknown tag technology backing IsoDep" +
                        TextUtils.join(", ", technologies));
        }

        for (String tech : technologies) {
            switch (tech) {
                case Technologies.A:
                    return new NfcAReader(mTag);
                case Technologies.B:
                    return new NfcBReader(mTag);
                case Technologies.F:
                    return new NfcFReader(mTag);
                case Technologies.V:
                    return new NfcVReader(mTag);
            }
        }

        return null;
    }

    @Override
    public void onReceive(final NfcComm data) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // handle data on UI thread
                handleData(data);
            }
        });
    }

    @Override
    public void onConnectionStatus() {
        // TODO: report this
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
