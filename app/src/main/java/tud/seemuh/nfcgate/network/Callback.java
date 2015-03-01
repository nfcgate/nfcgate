package tud.seemuh.nfcgate.network;

/**
 * Created by max on 01.03.15.
 */
public interface Callback {
    public void onDataReceived(byte[] data);
}
