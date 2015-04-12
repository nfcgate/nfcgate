package tud.seemuh.nfcgate.network;

/**
 * Interface for low-level network communication.
 */
public interface LowLevelNetworkHandler {

    /**
     * Send a byte[] over a connection in a way in which the respective receiver is able to receive
     * and decode it.
     * @param msg The byte[] to send
     */
    public void sendBytes(byte[] msg);

    /**
     * Set the callback method. A LowLevelNetworkHandler MUST pass any received bytes to the
     * callback if it is set. The byte[] passed to the Callback MUST be equal to the byte[] passed
     * to the sendBytes(byte[] msg) function on the other end.
     * @param callback An instantiated Object implementing the Callback interface
     * @return The LowLevelNetworkHandler instance ("this")
     */
    public LowLevelNetworkHandler setCallback(Callback callback);

    /**
     * Disconnect any active connections, stop all started threads, generally clean up.
     */
    public void disconnect();

}
