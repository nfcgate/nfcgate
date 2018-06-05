package tud.seemuh.nfcgate.nfc.modes;

import tud.seemuh.nfcgate.network.NetworkStatus;
import tud.seemuh.nfcgate.util.NfcComm;

public class RelayMode extends BaseMode {
    private boolean mReader;

    public RelayMode(boolean reader) {
        mReader = reader;
    }

    @Override
    public void onEnable() {
        // look for a tag
        mManager.enablePolling();

        // enable or disable reader mode
        mManager.setReaderMode(mReader);

        // connect to the network
        mManager.getNetwork().connect();
    }

    @Override
    public void onDisable() {
        // disable reader mode
        mManager.setReaderMode(false);

        // disconnect from the network
        mManager.getNetwork().disconnect();
    }

    @Override
    public void onData(NfcComm data) {
        if (data.isCard() && mReader
                || !data.isCard() && !mReader)
            // send own data over network
            mManager.getNetwork().send(data);
        else
            // apply foreign data
            mManager.applyData(data);
    }

    @Override
    public void onNetworkStatus(NetworkStatus status) {
        // no-op: override in UI
    }
}
