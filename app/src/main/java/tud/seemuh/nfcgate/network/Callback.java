package tud.seemuh.nfcgate.network;

import android.widget.TextView;

import tud.seemuh.nfcgate.hce.ApduService;

public interface Callback {
    public void onDataReceived(byte[] data);

    public Callback setAPDUService(ApduService as);
}
