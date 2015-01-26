package tud.seemuh.nfcgate.hce;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;


import com.google.protobuf.ByteString;

import tud.seemuh.nfcgate.network.CallbackImpl;
import tud.seemuh.nfcgate.network.NetHandler;
import tud.seemuh.nfcgate.network.SimpleLowLevelNetworkConnectionClientImpl;
import tud.seemuh.nfcgate.network.SimpleLowLevelNetworkConnectionClientImpl.Callback;
import tud.seemuh.nfcgate.util.Utils;
import tud.seemuh.nfcgate.network.c2c.C2C;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper.MessageCase;

public class ApduService extends HostApduService {
    private final static String TAG = "ApduService";

    private NetHandler Handler = new NetHandler();

    /**
     * empty apdu byte array
     * when returned in the processCommandApdu, the hce service will not respond to the
     * reader request
     */
    private final byte[] DONT_RESPOND = new byte[]{};

    /**
     * Callback from the network threa whenever we get data from it
     */
    private Callback mCallback = new CallbackImpl(this);

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

        SimpleLowLevelNetworkConnectionClientImpl.getInstance().setCallback(mCallback);
        /*
        if (apdu.length >= 2 && apdu[0] == (byte)0 && apdu[1] == (byte)0xa4) {
            // FIXME This is our terrible workaround, which we should remove.

            Log.i(TAG, "App selected");

            // new nfc interaction, so set the network callback to us
            SimpleLowLevelNetworkConnectionClientImpl.getInstance().setCallback(mCallback);
            // for the moment, we do not relay the select. This is only for the reader board
            // to select our app. The second apdu is the conversation with the card
            return new byte[]{0};
        }*/

        // Package the ADPU into a C2C message
        C2C.NFCData.Builder apduMessage= C2C.NFCData.newBuilder();
        apduMessage.setDataSource(C2C.NFCData.DataSource.READER);
        apduMessage.setDataBytes(ByteString.copyFrom(apdu));

        // Send the message
        Handler.sendMessage(apduMessage.build(), MessageCase.NFCDATA);

        Log.d(TAG, "nfc: " + Utils.bytesToHex(apdu));

        return DONT_RESPOND;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i("HceTest", "Deactivated: " + reason);
    }
}
