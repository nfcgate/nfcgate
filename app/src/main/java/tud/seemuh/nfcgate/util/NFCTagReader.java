package tud.seemuh.nfcgate.util;

import android.nfc.Tag;
import java.util.Hashtable;


/**
 * Interface to all NFCTagReader-Classes.
 * Created by Max on 25.10.14.
 */
public interface NFCTagReader {
    public static final int READER_ISODEP            = 0;
    public static final int READER_MIFARE_CLASSIC    = 1;
    public static final int READER_MIFARE_ULTRALIGHT = 2;
    public static final int READER_NDEF              = 3;
    public static final int READER_NDEF_FORMATABLE   = 4;
    public static final int READER_NFC_A             = 5;
    public static final int READER_NFC_B             = 6;
    public static final int READER_NFC_BARCODE       = 7;
    public static final int READER_NFC_F             = 8;
    public static final int READER_NFC_V             = 9;

    /**
     * Send a raw command to the NFC chip, receiving the answer as a byte[]
     *
     * @param command: byte[]-representation of the command to be sent
     * @return byte[]-representation of the answer of the NFC chip
     *
     * TODO: Note to self: "Applications must not append the EoD (CRC) to the payload, it will be
     *       automatically calculated. "
     */
    public byte[] sendCmd(byte[] command);

    /**
     * Returns a HashTable containing meta-bytes. The exact bytes are protocol-specific.
     * An exact list of which bytes will be returned will be added as soon as the functionality
     * is finished.
     *
     * @return A Hashtable<String, byte[]> containing a mapping between the meta-byte names
     *     and their values.
     *
     * TODO: Add list of provided bytes.
     */
    public Hashtable<String, byte[]> getMetaBytes();

    /**
     * Returns the integer representation of the protocol the used implementation is speaking.
     * This will be one of the READER_* constants defined by the util.NFCTagReader interface.
     *
     * @return integer representation of the underlying NFC tag reader protocol
     */
    public int getProtocol();
}
