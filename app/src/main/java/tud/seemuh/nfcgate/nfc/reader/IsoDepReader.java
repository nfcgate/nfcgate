package tud.seemuh.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.TagTechnology;
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
    private final TagTechnology mUnderlyingTech;
    private IsoDep mAdapter;

    /**
     * Constructor of NfcAReader-Class, providing an NFC reader interface using the NfcA-Tech.
     *
     * @param tag: A tag using the NfcA technology.
     */
    public IsoDepReader(Tag tag, TagTechnology underlyingTech) {
        mUnderlyingTech = underlyingTech;

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

    @Override
    public void closeConnection() {
        Log.d(TAG, "closeConnection was called!");
        try{
            mAdapter.close();
        } catch(IOException e) {
            Log.e(TAG, "closeConnection: Encountered IOException in closeConnection: ", e);
        }
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.ISODEP;
    }

    @Override
    public boolean isConnected() { return mAdapter.isConnected(); }

    @Override
    public ConfigBuilder getConfig() {
        ConfigBuilder builder = new ConfigBuilder();

        // an IsoDep tag can be backed by either NfcA or NfcB technology, build config accordingly
        if (mUnderlyingTech instanceof NfcA) {
            final NfcA underlyingTag = (NfcA) this.mUnderlyingTech;

            builder.add(OptionType.LA_NFCID1, mAdapter.getTag().getId());
            builder.add(OptionType.LA_SEL_INFO, (byte)underlyingTag.getSak());
            builder.add(OptionType.LA_BIT_FRAME_SDD, underlyingTag.getAtqa()[0]);
            builder.add(OptionType.LA_PLATFORM_CONFIG, underlyingTag.getAtqa()[1]);

            builder.add(OptionType.LA_HIST_BY, mAdapter.getHistoricalBytes());
        } else if (mUnderlyingTech instanceof NfcB) {
            final NfcB underlyingTag = (NfcB) this.mUnderlyingTech;

            builder.add(OptionType.LB_NFCID0, mAdapter.getTag().getId());
            builder.add(OptionType.LB_APPLICATION_DATA, underlyingTag.getApplicationData());
            builder.add(OptionType.LB_SFGI, underlyingTag.getProtocolInfo()[0]);
            builder.add(OptionType.LB_SENSB_INFO, underlyingTag.getProtocolInfo()[1]);
            builder.add(OptionType.LB_ADC_FO, underlyingTag.getProtocolInfo()[2]);

            builder.add(OptionType.LB_H_INFO_RSP, mAdapter.getHiLayerResponse());
        }

        return builder;
    }
}
