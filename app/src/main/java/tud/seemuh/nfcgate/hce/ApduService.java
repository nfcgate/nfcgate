package tud.seemuh.nfcgate.hce;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import tud.seemuh.nfcgate.network.Callback;
import tud.seemuh.nfcgate.network.CallbackImpl;
import tud.seemuh.nfcgate.network.NetHandler;
import tud.seemuh.nfcgate.network.SimpleLowLevelNetworkConnectionClientImpl;
import tud.seemuh.nfcgate.util.Utils;

public class ApduService extends HostApduService {
    private final static String TAG = "ApduService";

    private NetHandler Handler = NetHandler.getInstance();

    /**
     * empty apdu byte array
     * when returned in the processCommandApdu, the hce service will not respond to the
     * reader request
     */
    private final byte[] DONT_RESPOND = new byte[]{};

    /**
     * Callback from the network thread whenever we get data from it
     */
    private Callback mCallback = Handler.getCallback().setAPDUService(this);

    public ApduService() {
        Handler.notifyReaderFound();
    }

    /**
     * callback from the hce service when a apdu from a reader is received
     * @param apdu apdu data received from hce service
     * @param extras not used
     * @return apdu to answer
     */
    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        // the byte sequence 0x00a4 is a SELECT command. this is always the first command we get
        // when a reader wants to talk to us

        // Package the ADPU into a C2C message
        Handler.sendAPDUMessage(apdu);

        Log.d(TAG, "nfc: " + Utils.bytesToHex(apdu));

        return DONT_RESPOND;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i(TAG, "Deactivated: " + reason);
        Handler.notifyReaderRemoved();
    }
}
