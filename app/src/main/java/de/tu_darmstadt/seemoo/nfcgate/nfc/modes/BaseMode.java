package de.tu_darmstadt.seemoo.nfcgate.nfc.modes;

import de.tu_darmstadt.seemoo.nfcgate.network.data.NetworkStatus;
import de.tu_darmstadt.seemoo.nfcgate.nfc.NfcManager;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public abstract class BaseMode {
    protected NfcManager mManager;

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
