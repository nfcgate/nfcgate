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

    /**
     * empty apdu byte array
     * when returned in the processCommandApdu, the hce service will not respond to the
     * reader request
     */
    private final byte[] DONT_RESPOND = new byte[]{};

    /**
     * Callback from the network threa whenever we get data from it
     */
    private Callback mCallback = new SimpleNetworkConnectionClientImpl.Callback() {
        @Override
        public void onDataReceived(byte[] data) {
            // send apdu from network to reader
            ApduService.this.sendResponseApdu(data);
        }
    };

    /**
     * callback from the hce service when a apdu from a reader is received
     * @param apdu apdu data received from hce service
     * @param extras not used
     * @return apdu to answer
     */
    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        // the byte sequence 0x00a4 is a SELECT command. this is ever the first command we get
        // when a reader wants to talk to us
        if (apdu.length >= 2 && apdu[0] == (byte)0 && apdu[1] == (byte)0xa4) {

            Log.i(TAG, "App selected");

            // new nfc interaction, so set the network callback to us
            SimpleNetworkConnectionClientImpl.getInstance().setCallback(mCallback);
            // for the moment, we do not relay the select. This is only for the reader board
            // to select our app. The second apdu is the conversation with the card
            return new byte[]{0};
        }
        // raw send the apdu over the network
        SimpleNetworkConnectionClientImpl.getInstance().sendBytes(apdu);
        Log.d(TAG, "nfc: " + Utils.bytesToHex(apdu));

        return DONT_RESPOND;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i("HceTest", "Deactivated: " + reason);
    }
}