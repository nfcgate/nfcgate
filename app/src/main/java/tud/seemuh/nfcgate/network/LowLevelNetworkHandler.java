package tud.seemuh.nfcgate.network;


public interface LowLevelNetworkHandler {

    public void sendBytes(byte[] msg);

    public SimpleLowLevelNetworkConnectionClientImpl setCallback(Callback callback);

}
