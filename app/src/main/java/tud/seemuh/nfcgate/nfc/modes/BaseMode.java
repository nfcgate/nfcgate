package tud.seemuh.nfcgate.nfc.modes;

import tud.seemuh.nfcgate.network.NetworkStatus;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.util.NfcComm;

public abstract class BaseMode {
    NfcManager mManager;

    // used by manager to set own reference before enabling this mode
    public void setManager(NfcManager manager) {
        mManager = manager;
    }

    // lifetime methods
    public abstract void onEnable();
    public abstract void onDisable();

    // action and log methods
    public abstract void onData(boolean isForeign, NfcComm data);
    public abstract void onNetworkStatus(NetworkStatus status);
}
