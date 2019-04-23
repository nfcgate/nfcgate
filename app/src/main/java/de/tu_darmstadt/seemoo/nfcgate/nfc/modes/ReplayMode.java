package de.tu_darmstadt.seemoo.nfcgate.nfc.modes;

import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.db.NfcCommEntry;
import de.tu_darmstadt.seemoo.nfcgate.network.data.NetworkStatus;
import de.tu_darmstadt.seemoo.nfcgate.nfc.NfcLogReplayer;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

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
