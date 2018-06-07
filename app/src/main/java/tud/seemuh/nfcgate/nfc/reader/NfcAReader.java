package tud.seemuh.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.util.Log;

import java.io.IOException;

import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.nfc.config.OptionType;

/**
 * Implements an NFCTagReader using the NfcA technology
 *
 */
public class NfcAReader implements NFCTagReader {
    private final static String TAG = "NFC_READER_NFCA";
    private NfcA mAdapter;

    /**
     * Constructor of NfcAReader-Class, providing an NFC reader interface using the NfcA-Tech.
     *
     * @param tag: A tag using the NfcA technology.
     */
    public NfcAReader(Tag tag) {
        // Create NFC Adapter to use
        mAdapter = NfcA.get(tag);
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
        return Protocol.NFC_A;
    }

    @Override
    public boolean isConnected() { return mAdapter.isConnected(); }

    @Override
    public ConfigBuilder getConfig() {
        ConfigBuilder builder = new ConfigBuilder();

        builder.add(OptionType.LA_SEL_INFO, (byte)mAdapter.getSak());
        builder.add(OptionType.LA_BIT_FRAME_SDD, mAdapter.getAtqa()[0]);
        builder.add(OptionType.LA_PLATFORM_CONFIG, mAdapter.getAtqa()[1]);
        builder.add(OptionType.LA_NFCID1, mAdapter.getTag().getId());

        return builder;
    }

}
