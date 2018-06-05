package tud.seemuh.nfcgate.nfc.modes;

import tud.seemuh.nfcgate.network.NetworkStatus;
import tud.seemuh.nfcgate.util.NfcComm;

public class CloneMode extends BaseMode {
    @Override
    public void onEnable() {
        // enable polling because we are looking for a tag
        mManager.enablePolling();
    }

    @Override
    public void onDisable() {
        // ignore all further tags
        mManager.disablePolling();
    }

    @Override
    public void onData(NfcComm data) {
        // clone tag and immediately disable clone mode to avoid cloning same tag again
        mManager.applyData(data);
        // TODO: disable
    }

    @Override
    public void onNetworkStatus(NetworkStatus status) {
        // no-op: clone mode has no network activity
    }
}
