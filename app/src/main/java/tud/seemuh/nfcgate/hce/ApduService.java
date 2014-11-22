package tud.seemuh.nfcgate.hce;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import tud.seemuh.nfcgate.network.SimpleNetworkConnectionClientImpl;
import tud.seemuh.nfcgate.util.Utils;
import tud.seemuh.nfcgate.network.SimpleNetworkConnectionClientImpl.Callback;

public class ApduService extends HostApduService {

    private final static String TAG = "ApduService";
    Socket _socket;
    private final byte[] DONT_RESPOND = new byte[]{};

    private Callback mCallback = new SimpleNetworkConnectionClientImpl.Callback() {
        @Override
        public void onDataReceived(byte[] data) {
            Log.d(TAG, "callback: " + Utils.bytesToHex(data));
            ApduService.this.sendResponseApdu(data);
        }
    };

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        if (apdu.length >= 2 && apdu[0] == (byte)0 && apdu[1] == (byte)0xa4) {

            Log.i(TAG, "App selected");

            //SimpleNetworkConnectionClientImpl.getInstance().connect("192.168.178.31", 5566);
            SimpleNetworkConnectionClientImpl.getInstance().setCallback(mCallback);
            // TODO momentan den SELECT befehl nicht weiter leiten
            return new byte[]{0};
        }

        SimpleNetworkConnectionClientImpl.getInstance().sendBytes(apdu);
        Log.d(TAG, "nfc: " + Utils.bytesToHex(apdu));

        return DONT_RESPOND;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i("HceTest", "Deactivated: " + reason);
    }
}