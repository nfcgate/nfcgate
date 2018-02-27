package tud.seemuh.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.util.Log;

import java.io.IOException;

import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.nfc.config.OptionType;
import tud.seemuh.nfcgate.util.Utils;

/**
 * Implements an NFCTagReader using the IsoDep technology
 *
 */
public class IsoDepReader implements NFCTagReader {
    private final static String TAG = "NFC_READER_ISODEP";
    private IsoDep mAdapter = null;

    /**
     * Constructor of NfcAReader-Class, providing an NFC reader interface using the NfcA-Tech.
     *
     * @param tag: A tag using the NfcA technology.
     */
    public IsoDepReader(Tag tag) {
        // Create NFC Adapter to use
        mAdapter = IsoDep.get(tag);
        try {
            // Connect to the NFC card
            mAdapter.connect();
            mAdapter.setTimeout(5000);
        } catch (Exception e) {
            Log.e(TAG, "Constructor: Encountered Exception: ", e);
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
            // Transceive command and store reply
            Log.d(TAG, "sendCmd: R-APDU-OUT: " + Utils.bytesToHex(command));
            byte[] retval = mAdapter.transceive(command);
            Log.d(TAG, "sendCmd: R-APDU-IN: " + Utils.bytesToHex(retval));

            return retval;
        } catch(IOException e) {
            Log.e(TAG, "sendCmd: Encountered IOException in sendCmd: ", e);
            return null;
        } catch(Exception e) {
            Log.e(TAG, "sendCmd: Encountered Exception in sendCmd: ", e);
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
        Log.d(TAG, "closeConnection was called!");
        try{
            mAdapter.close();
        } catch(IOException e) {
            Log.e(TAG, "closeConnection: Encountered IOException in closeConnection: ", e);
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
        return READER_ISODEP;
    }

    @Override
    public boolean isConnected() { return mAdapter.isConnected(); }

    @Override
    public ConfigBuilder getConfig() {
        ConfigBuilder builder = new ConfigBuilder();

        NfcA tagA = NfcA.get(mAdapter.getTag());

        builder.add(OptionType.LA_SEL_INFO, (byte)tagA.getSak());
        builder.add(OptionType.LA_BIT_FRAME_SDD, tagA.getAtqa()[0]);
        builder.add(OptionType.LA_PLATFORM_CONFIG, tagA.getAtqa()[1]);
        builder.add(OptionType.LA_NFCID1, mAdapter.getTag().getId());
        builder.add(OptionType.LA_HIST_BY, mAdapter.getHistoricalBytes());

        return builder;
    }

}