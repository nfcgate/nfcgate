package tud.seemuh.nfcgate.hce;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;


import com.google.protobuf.ByteString;

import tud.seemuh.nfcgate.network.SimpleNetworkConnectionClientImpl;
import tud.seemuh.nfcgate.network.SimpleNetworkConnectionClientImpl.Callback;
import tud.seemuh.nfcgate.util.Utils;
import tud.seemuh.nfcgate.network.c2c.C2C;

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

        // Package the ADPU into a C2C message
        C2C.NFCData.Builder apduMessage= C2C.NFCData.newBuilder();
        apduMessage.setDataSource(C2C.NFCData.DataSource.READER);
        apduMessage.setDataBytes(ByteString.copyFrom(apdu));

        // Serialize the message into a byte[]
        byte[] apduMessageBytes = apduMessage.build().toByteArray();

        // Send NFCData message bytes
        SimpleNetworkConnectionClientImpl.getInstance().sendBytes(apduMessageBytes);
        Log.d(TAG, "nfc: " + Utils.bytesToHex(apdu));

        return DONT_RESPOND;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i("HceTest", "Deactivated: " + reason);
    }
}
