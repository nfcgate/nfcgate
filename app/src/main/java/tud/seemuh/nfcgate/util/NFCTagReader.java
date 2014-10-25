package tud.seemuh.nfcgate.util;

import android.nfc.Tag;
import java.util.Hashtable;


/**
 * Interface to all NFCTagReader-Classes.
 * Created by Max on 25.10.14.
 */
public interface NFCTagReader {
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
}
