package tud.seemuh.nfcgate.network;

import android.widget.TextView;

import tud.seemuh.nfcgate.hce.ApduService;

/**
 * Created by max on 01.03.15.
 */
public interface Callback {
    public void onDataReceived(byte[] data);

    public Callback setAPDUService(ApduService as);

    public void setDebugView(TextView ldebugView);

    public void setConnectionStatusView(TextView connStatusView);

    public void setPeerStatusView(TextView view);
}
