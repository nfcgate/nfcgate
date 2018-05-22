package tud.seemuh.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.util.Log;

import java.io.IOException;

import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.nfc.config.OptionType;

/**
 * Implements an NFCTagReader using the NfcF technology
 *
 */
public class NfcFReader implements NFCTagReader {
    private final static String TAG = "NFC_READER_NFCF";
    private NfcF mAdapter;

    /**
     * Constructor of NfcFReader-Class, providing an NFC reader interface using the NfcF-Tech.
     *
     * @param tag: A tag using the NfcF technology.
     */
    public NfcFReader(Tag tag) {
        // Create NFC Adapter to use
        mAdapter = NfcF.get(tag);
        try{
            // Connect the adapter to the NFC card
            mAdapter.connect();
        } catch(IOException e) {
            // Something went wrong. For the moment, we will only log this
            Log.e(TAG, "Constructor: Encountered IOException in constructor: " + e);
        }
    }

    /**
     * Send a raw command to the NFC chip, receiving the answer as a byte[]
     *
     * @param command: byte[]-representation of the command to be sent
     * @return byte[]-representation of the answer of the NFC chip
     */
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

    /**
     * Closes the adapter, signalling that communication is over. Should be called only
     * when no further communication with the adapter will follow, as the adapter will become
     * unusable from this
     */
    @Override
    public void closeConnection() {
        try{
            mAdapter.close();
        } catch(IOException e) {
            Log.e(TAG, "closeConnection: Encountered IOException: ", e);
        }
    }

    /**
     * Returns the integer representation of the protocol the used implementation is speaking.
     * This will be one of the READER_* constants defined by the util.NFCTagReader interface.
     *
     * @return integer representation of the underlying NFC tag reader protocol
     */
    @Override
    public int getProtocol() {
        return READER_NFC_F;
    }

    @Override
    public boolean isConnected() { return mAdapter.isConnected(); }

    @Override
    public ConfigBuilder getConfig() {
        ConfigBuilder builder = new ConfigBuilder();

        // join systemcode and nfcid2
        byte[] t3t_identifier_1 = new byte[10];
        System.arraycopy(mAdapter.getSystemCode(), 0, t3t_identifier_1, 0, 2);
        System.arraycopy(mAdapter.getTag().getId(), 0, t3t_identifier_1, 2, 8);

        // set bit at index 1 to indicate activation of t3t_identifier_1
        byte[] t3t_flags = new byte[] { 1, 0 };

        builder.add(OptionType.LF_T3T_IDENTIFIERS_1, t3t_identifier_1);
        builder.add(OptionType.LF_T3T_FLAGS, t3t_flags);
        builder.add(OptionType.LF_T3T_PMM, mAdapter.getManufacturer());

        return builder;
    }
}
