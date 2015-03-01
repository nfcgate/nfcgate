package tud.seemuh.nfcgate.reader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.util.Log;

import java.io.IOException;

import tud.seemuh.nfcgate.util.Utils;

/**
 * Implements an NFCTagReader using the IsoDep technology
 *
 */
public class IsoDepReaderImpl extends NfcAReaderImpl {

    private final static String TAG = "NFC_READER_ISODEP";
    private IsoDep mAdapter = null;

    /**
     * Constructor of IsoDepReader-Class, providing an NFC reader interface using the NfcA-Tech.
     *
     * @param tag: A tag using the NfcA technology.
     */
    public IsoDepReaderImpl(Tag tag) {
        super(tag);
        Log.d(TAG, "IsoDep constructor called");
        mAdapter = IsoDep.get(tag);
    }


    /**
     * Returns the integer representation of the protocol the used implementation is speaking.
     * This will be one of the READER_* constants defined by the util.NFCTagReader interface.
     *
     * @return integer representation of the underlying NFC tag reader protocol
     */
    public int getProtocol() {
        return READER_ISODEP;
    }

    public byte[] getHistoricalBytes() {
        return mAdapter.getHistoricalBytes();
    }

}