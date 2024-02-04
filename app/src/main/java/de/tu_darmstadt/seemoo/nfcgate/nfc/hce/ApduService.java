package de.tu_darmstadt.seemoo.nfcgate.nfc.hce;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import de.tu_darmstadt.seemoo.nfcgate.nfc.NfcManager;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;
import de.tu_darmstadt.seemoo.nfcgate.util.Utils;

/**
 * The ApduService class contains the logic for interaction with the Android HCE interface.
 * Here, we receive messages from the card reader and pass them on to the NfcManager.
 */
public class ApduService extends HostApduService {
    private final static String TAG = "ApduService";

    private final NfcManager mNfcManager = NfcManager.getInstance();

    /**
     * Returning an empty APDU response causes the hce service to wait
     */
    private final byte[] DONT_RESPOND = new byte[]{};

    public ApduService() {
        mNfcManager.setApduService(this);
    }

    /**
     * Callback from the hce service when a apdu from a reader is received
     * @param apdu apdu data received from hce service
     * @param extras not used
     * @return apdu to answer
     */
    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d(TAG, "APDU-IN: " + Utils.bytesToHex(apdu));

        // Package the ADPU into a NfcComm object
        NfcComm nfcdata = new NfcComm(false, false, apdu);

        // Send the object to the handler
        mNfcManager.handleData(false, nfcdata);

        // Tell the HCE implementation to wait
        return DONT_RESPOND;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i(TAG, "Deactivated: " + reason);
        mNfcManager.setApduService(null);
    }

    public void sendResponse(byte[] apdu) {
        Log.d(TAG, "APDU-OUT: " + Utils.bytesToHex(apdu));
        sendResponseApdu(apdu);
    }
}
