package de.tu_darmstadt.seemoo.nfcgate.nfc.modes;

import de.tu_darmstadt.seemoo.nfcgate.network.data.NetworkStatus;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class CloneMode extends BaseMode {
    @Override
    public void onEnable() {
        // enable polling because we are looking for a tag
        mManager.enablePolling();
    }

    @Override
    public void onDisable() {
        // no-op
    }

    @Override
    public void onData(boolean isForeign, NfcComm data) {
        // clone tag and immediately disable clone mode to avoid cloning same tag again
        mManager.applyData(data);
        mManager.disablePolling();
    }

    @Override
    public void onNetworkStatus(NetworkStatus status) {
        // no-op: clone mode has no network activity
    }
}
