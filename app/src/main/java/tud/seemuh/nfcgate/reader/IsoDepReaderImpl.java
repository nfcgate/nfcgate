package tud.seemuh.nfcgate.reader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;

/**
 * Implements an NFCTagReader using the NfcA technology
 *
 * Created by Max on 27.10.14.
 */
public class IsoDepReaderImpl implements NFCTagReader {
    private IsoDep mAdapter = null;

    /**
     * Constructor of NfcAReader-Class, providing an NFC reader interface using the NfcA-Tech.
     *
     * @param tag: A tag using the NfcA technology.
     */
    public IsoDepReaderImpl(Tag tag) {
        Log.d("NFC_READER_ISODEP", "IsoDep constructor called");
        mAdapter = IsoDep.get(tag);
        try {
            mAdapter.connect();
        } catch (Exception e) {
            //TODO
            Log.e("NFC_READER_ISODEP", "Encountered error in constructor: " + e);
        }
    }

    /**
     * Send a raw command to the NFC chip, receiving the answer as a byte[]
     *
     * @param command: byte[]-representation of the command to be sent
     * @return byte[]-representation of the answer of the NFC chip
     *
     * TODO: Note to self: "Applications must not append the EoD (CRC) to the payload, it will be
     *       automatically calculated. "
     */
    public byte[] sendCmd(byte[] command) {
        try {
            //mAdapter.connect();
            byte[] retval = mAdapter.transceive(command);
            //mAdapter.close();
            Log.i("NFC_READER_ISODEP", "Transceived succesfully, returned: " + retval.toString());
            return retval;
        } catch(IOException e) {
            // TODO: Handle Exception properly
            Log.e("NFC_READER_ISODEP", "Encountered IOException in sendCmd: " + e);
            return null;
        } catch(Exception e) {
            //TODO
            Log.e("NFC_READER_ISODEP", "Encountered IOException in sendCmd: " + e);
            return null;
        }
    }

    /**
     * Close the connection to the mAapter, only do this at the END of the app
     * consecutive commands must be executed without close in between!
     */
    public void closeConnection() {
        try{
            mAdapter.close();
        } catch(IOException e) {
            //TODO
        }
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

}