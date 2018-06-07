package tud.seemuh.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.util.Log;

import java.io.IOException;

import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;

/**
 * Implements an NFCTagReader using the NfcV technology
 *
 */
public class NfcVReader implements NFCTagReader {
    private final static String TAG = "NFC_READER_NFCV";
    private NfcV mAdapter;

    /**
     * Constructor of NfcVReader-Class, providing an NFC reader interface using the NfcV-Tech.
     *
     * @param tag: A tag using the NfcV technology.
     */
    public NfcVReader(Tag tag) {
        // Create NFC Adapter to use
        mAdapter = NfcV.get(tag);
        try{
            // Connect the adapter to the NFC card
            mAdapter.connect();
        } catch(IOException e) {
            // Something went wrong. For the moment, we will only log this
            Log.e(TAG, "Constructor: Encountered IOException in constructor: " + e);
        }
    }

    @Override
    public byte[] sendCmd(byte[] command) {
        try {
            // Transceive command (transmit command and receive answer)
            byte[] retval = mAdapter.transceive(command);
            Log.d(TAG, "sendCmd: Transceived succesfully");

            return retval;
        } catch(IOException e) {
            Log.e(TAG, "sendCmd: Encountered IOException in sendCmd: ", e);
            return null;
        }
    }

    @Override
    public void closeConnection() {
        try{
            mAdapter.close();
        } catch(IOException e) {
            Log.e(TAG, "closeConnection: Encountered IOException: ", e);
        }
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.NFC_V;
    }

    @Override
    public boolean isConnected() { return mAdapter.isConnected(); }

    @Override
    public ConfigBuilder getConfig() {
        // TODO: V tags cannot be emulated (yet)
        return new ConfigBuilder();
    }
}
