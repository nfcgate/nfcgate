package tud.seemuh.nfcgate.nfc.hce;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import tud.seemuh.nfcgate.network.Callback;
import tud.seemuh.nfcgate.network.HighLevelProtobufHandler;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.Utils;

/**
 * The ApduService class contains the logic for interaction with the Android HCE interface.
 * Here, we receive messages from the card reader and pass them on to the network interface.
 * The answer is determined by the other device and the reply is passed to the ApduService by
 * the Callback class.
 */
public class ApduService extends HostApduService {
    private final static String TAG = "ApduService";

    private NfcManager mNfcManager = NfcManager.getInstance();

    /**
     * empty apdu byte array
     * when returned in the processCommandApdu, the hce service will not respond to the
     * reader request
     */
    private final byte[] DONT_RESPOND = new byte[]{};

    public ApduService() {
        mNfcManager.setApduService(this);
    }

    /**
     * callback from the hce service when a apdu from a reader is received
     * @param apdu apdu data received from hce service
     * @param extras not used
     * @return apdu to answer
     */
    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {

        Log.d(TAG, "APDU-IN: " + Utils.bytesToHex(apdu));

        // Package the ADPU into a NfcComm object
        NfcComm nfcdata = new NfcComm(NfcComm.Source.HCE, apdu);

        // Send the object to the handler
        mNfcManager.handleHCEData(nfcdata);

        // Tell the HCE implementation to wait a moment
        return DONT_RESPOND;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i(TAG, "Deactivated: " + reason);
        mNfcManager.unsetApduService();
    }

    public void sendResponse(byte[] apdu) {
        Log.d(TAG, "APDU-OUT: " + Utils.bytesToHex(apdu));
        sendResponseApdu(apdu);
    }
}
