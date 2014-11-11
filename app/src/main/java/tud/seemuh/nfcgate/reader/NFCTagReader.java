package tud.seemuh.nfcgate.reader;

import android.nfc.Tag;
import java.util.Hashtable;


/**
 * Interface to all NFCTagReader-Classes.
 * Created by Max on 25.10.14.
 */
public interface NFCTagReader {
    // Constants to indicate the used NFC reader technology
    // Not all of these are guaranteed to be available, as the android API does not guarantee
    // support for some of these.
    public static final int READER_ISODEP            = 0;
    public static final int READER_MIFARE_CLASSIC    = 1;
    public static final int READER_MIFARE_ULTRALIGHT = 2;
    public static final int READER_NFC_A             = 3;
    public static final int READER_NFC_B             = 4;
    public static final int READER_NFC_BARCODE       = 5;
    public static final int READER_NFC_F             = 6;
    public static final int READER_NFC_V             = 7;

    /**
     * Send a raw command to the NFC chip, receiving the answer as a byte[]
     *
     * @param command: byte[]-representation of the command to be sent
     * @return byte[]-representation of the answer of the NFC chip
     */
    public byte[] sendCmd(byte[] command);

    /**
     * Returns the integer representation of the protocol the used implementation is speaking.
     * This will be one of the READER_* constants defined by the util.NFCTagReader interface.
     *
     * @return integer representation of the underlying NFC tag reader protocol
     */
    public int getProtocol();

    /**
     * Closes the adapter, signalling that communication is over. Should be called only
     * when no further communication with the adapter will follow, as the adapter will become
     * unusable from this
     */
    public void closeConnection();
}
