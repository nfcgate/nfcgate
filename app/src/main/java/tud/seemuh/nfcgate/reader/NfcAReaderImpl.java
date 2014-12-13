package tud.seemuh.nfcgate.reader;

import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.util.Log;

import java.io.IOException;

/**
 * Implements an NFCTagReader using the NfcA technology
 *
 */
public class NfcAReaderImpl implements NFCTagReader {
    private NfcA mAdapter = null;

    /**
     * Constructor of NfcAReader-Class, providing an NFC reader interface using the NfcA-Tech.
     *
     * @param tag: A tag using the NfcA technology.
     */
    public NfcAReaderImpl(Tag tag) {
        Log.d("NFC_READER_NFCA", "NfcA constructor called");
        // Create NFC Adapter to use
        mAdapter = NfcA.get(tag);
        try{
            // Connect the adapter to the NFC card
            mAdapter.connect();
        } catch(IOException e) {
            // Something went wrong. For the moment, we will only log this
            //TODO
            Log.e("NFC_READER_NFCA", "Encountered IOException in constructor: " + e);
        }
    }

    /**
     * Send a raw command to the NFC chip, receiving the answer as a byte[]
     *
     * @param command: byte[]-representation of the command to be sent
     * @return byte[]-representation of the answer of the NFC chip
     */
    public byte[] sendCmd(byte[] command) {
        try {
            // Transceive command (transmit command and receive answer)
            byte[] retval = mAdapter.transceive(command);

            Log.i("NFC_READER_NFCA", "Transceived succesfully");
            return retval;
        } catch(IOException e) {
            // TODO: Handle Exception properly
            Log.e("NFC_READER_NFCA", "Encountered IOException in sendCmd: " + e);
            return null;
        }
    }

    /**
     * Closes the adapter, signalling that communication is over. Should be called only
     * when no further communication with the adapter will follow, as the adapter will become
     * unusable from this
     */
    public void closeConnection() {
        try{
            mAdapter.close();
        } catch(IOException e) {
            Log.e("NFC_READER_NFCA", "Encountered IOException in sendCmd: " + e);
        }
    }

    /**
     * Returns the integer representation of the protocol the used implementation is speaking.
     * This will be one of the READER_* constants defined by the util.NFCTagReader interface.
     *
     * @return integer representation of the underlying NFC tag reader protocol
     */
    public int getProtocol() {
        return READER_NFC_A;
    }

    public boolean isConnected() { return mAdapter.isConnected(); }

}