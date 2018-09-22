package tud.seemuh.nfcgate.nfc.modes;

import java.util.List;

import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.network.NetworkStatus;
import tud.seemuh.nfcgate.util.NfcComm;

public class ReplayMode extends BaseMode {
    private boolean mReplayReader;
    private List<NfcCommEntry> mReplayLog;
    private int mReplayIndex = 0;

    public ReplayMode(boolean reader, List<NfcCommEntry> replayLog) {
        mReplayReader = reader;
        mReplayLog = replayLog;
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
        // apply a response from log
        mManager.applyData(mReplayLog.get(mReplayIndex + 1).getNfcComm());

        // advance position in log
        mReplayIndex += 2;
    }

    @Override
    public void onNetworkStatus(NetworkStatus status) {
        // no-op: override in UI
    }
}
