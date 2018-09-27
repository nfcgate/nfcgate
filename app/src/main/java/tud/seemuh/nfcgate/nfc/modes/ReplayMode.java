package tud.seemuh.nfcgate.nfc.modes;

import java.util.List;

import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.network.data.NetworkStatus;
import tud.seemuh.nfcgate.nfc.NfcLogReplayer;
import tud.seemuh.nfcgate.util.NfcComm;

public class ReplayMode extends BaseMode {
    private boolean mReplayReader;
    private NfcLogReplayer mLogReplayer;

    public ReplayMode(boolean reader, List<NfcCommEntry> replayLog) {
        mReplayReader = reader;
        mLogReplayer = new NfcLogReplayer(reader, replayLog);
    }

    @Override
    public void onEnable() {
        // look for a tag
        mManager.enablePolling();

        // enable or disable reader mode
        mManager.setReaderMode(mReplayReader);
    }

    @Override
    public void onDisable() {
        // disable reader mode
        mManager.setReaderMode(false);
    }

    @Override
    public void onData(boolean isForeign, NfcComm data) {
        if (isForeign) {
            // apply replay response
            mManager.applyData(data);
        }
        else {
            // get response for request and handle it
            NfcComm response = mLogReplayer.getResponse(data);

            if (response != null)
                mManager.handleData(true, response);
        }
    }

    @Override
    public void onNetworkStatus(NetworkStatus status) {
        // no-op: override in UI
    }
}
