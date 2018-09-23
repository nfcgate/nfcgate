package tud.seemuh.nfcgate.nfc.modes;

import java.util.List;

import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.network.data.NetworkStatus;
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

    protected NfcComm getResponse(NfcComm data) {
        // reached end of log
        if (mReplayIndex + 1 >= mReplayLog.size())
            return null;

        // TODO: switch from response by position to response by data
        NfcComm result = mReplayLog.get(mReplayIndex + 1).getNfcComm();

        // advance position in log
        mReplayIndex += 2;
        return result;
    }

    @Override
    public void onData(boolean isForeign, NfcComm data) {
        // no-op: override in UI
    }

    @Override
    public void onNetworkStatus(NetworkStatus status) {
        // no-op: override in UI
    }
}
