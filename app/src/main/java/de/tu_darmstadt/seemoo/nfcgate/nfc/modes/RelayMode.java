package de.tu_darmstadt.seemoo.nfcgate.nfc.modes;

import de.tu_darmstadt.seemoo.nfcgate.network.data.NetworkStatus;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class RelayMode extends BaseMode {
    private final boolean mReader;
    protected boolean mOnline = true;

    public RelayMode(boolean reader) {
        mReader = reader;
    }

    @Override
    public void onEnable() {
        // look for a tag in reader mode, do not look for tags in tag mode
        mManager.setPollingEnabled(mReader);

        // enable or disable reader mode
        mManager.setReaderMode(mReader);

        // connect to the network
        if (mOnline)
            mManager.getNetwork().connect();
    }

    @Override
    public void onDisable() {
        // re-enable polling if disabled
        mManager.setPollingEnabled(true);
        // disable reader mode
        mManager.setReaderMode(false);

        // disconnect from the network
        if (mOnline)
            mManager.getNetwork().disconnect();
    }

    @Override
    public void onNetworkStatus(NetworkStatus status) {
        // no-op: override in UI
    }

    @Override
    public void onData(boolean isForeign, NfcComm data) {
        // accept only foreign data of other type than we are
        if (isForeign && data.isCard() != mReader) {
            // apply foreign data
            mManager.applyData(data);
        }
        // accept only own data of our type
        else if (!isForeign && data.isCard() == mReader) {
            // send own data over network
            toNetwork(data);
        }
    }

    protected void toNetwork(NfcComm data) {
        // default action is to send to network
        mManager.getNetwork().send(data);
    }
}
