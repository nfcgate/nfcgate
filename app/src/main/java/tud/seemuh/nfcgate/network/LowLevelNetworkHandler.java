package tud.seemuh.nfcgate.network;


public interface LowLevelNetworkHandler {

    public void sendBytes(byte[] msg);

    public LowLevelTCPHandler setCallback(Callback callback);

    public void disconnect();

}
