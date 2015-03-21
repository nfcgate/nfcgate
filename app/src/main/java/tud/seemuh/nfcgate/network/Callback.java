package tud.seemuh.nfcgate.network;

import tud.seemuh.nfcgate.nfc.NfcManager;

/**
 * Interface for networking callbacks. A networking callback is passed a byte[] into its
 * onDataReceived-Function as it is received over the network.
 *
 * Any implementation MUST make certain method calls in sensible places inside some of these
 * functions. The calls are documented in the comments at the respective functions.
 */
public interface Callback {
    /**
     * onDataReceived receives a raw byte[]. The callback is responsible for decoding and acting
     * on it. When execution of onDataReceived ends, the data MUST be processed completely
     * (including any NFC interactions), and any required replies MUST be sent.
     *
     * @param data The raw bytes of the message, as it was received over the network.
     */
    public void onDataReceived(byte[] data);

    /**
     * This method sets the NfcManager object that is required for all NFC interactions.
     * @param nfcManager NfcManager object
     */
    public void setNfcManager(NfcManager nfcManager);

    /**
     * Called when the underlying communication channel breaks down. MUST notify the
     * HighLevelNetworkHandler using the disconnectBrokenPipe() method. MAY perform other work.
     */
    public void notifyBrokenPipe();

    /**
     * Called when the Network connection is shutting down. MUST shut down any started threads and
     * generally clean up after itself. MUST be in a state where it can be restarted without errors
     * afterwards (nulling relevant variables, ...).
     */
    public void shutdown();
}
