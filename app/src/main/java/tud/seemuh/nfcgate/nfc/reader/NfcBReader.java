package tud.seemuh.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.NfcB;
import android.util.Log;

import java.io.IOException;

import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.nfc.config.OptionType;

/**
 * Implements an NFCTagReader using the NfcB technology
 *
 */
public class NfcBReader implements NFCTagReader {
    private final static String TAG = "NFC_READER_NFCB";
    private NfcB mAdapter;

    /**
     * Constructor of NfcBReader-Class, providing an NFC reader interface using the NfcB-Tech.
     *
     * @param tag: A tag using the NfcA technology.
     */
    public NfcBReader(Tag tag) {
        // Create NFC Adapter to use
        mAdapter = NfcB.get(tag);
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
        return Protocol.NFC_B;
    }

    @Override
    public boolean isConnected() { return mAdapter.isConnected(); }

    @Override
    public ConfigBuilder getConfig() {
        ConfigBuilder builder = new ConfigBuilder();

        builder.add(OptionType.LB_NFCID0, mAdapter.getTag().getId());
        builder.add(OptionType.LB_APPLICATION_DATA, mAdapter.getApplicationData());
        builder.add(OptionType.LB_SFGI, mAdapter.getProtocolInfo()[0]);
        builder.add(OptionType.LB_SENSB_INFO, mAdapter.getProtocolInfo()[1]);
        builder.add(OptionType.LB_ADC_FO, mAdapter.getProtocolInfo()[2]);

        return builder;
    }

}
