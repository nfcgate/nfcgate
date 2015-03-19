package tud.seemuh.nfcgate.network;

import android.nfc.Tag;

import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.nfc.hce.ApduService;

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
     * This method sets the APDUService object that is required to send APDU replies on the HCE
     * phone. Returns the Callback object ("this").
     *
     * @param as The APDUService object
     * @return The Callback object ("this")
     */
    public Callback setAPDUService(ApduService as);

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

    /**
     * Called when the Broadcom BCM20793 workaround thread is to be killed (if it is running). MAY
     * be called when no thread is running (so make sure it does not NPE in that case). MUST stop
     * the workaround thread.
     */
    public void disconnectCardWorkaround();

    /**
     * Passes a reference to the Tag object to the Callback implementation. MUST notify connected
     * session partner about the Anticollision bytes (using the HighLevelNetworkHandler.sendAnticol()
     * method). MUST start the Broadcom BCM20793 workaround thread, where applicable (find out if
     * it is needed using the BCM20793Workaround.workaroundNeeded() static function).
     *
     * @param tag The Tag object that was passed with the android systen intent
     * @return true if the tag is supported, false otherwise.
     */
    public boolean setTag(Tag tag);
}
