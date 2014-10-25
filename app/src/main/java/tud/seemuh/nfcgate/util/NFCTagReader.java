package tud.seemuh.nfcgate.util;

import android.nfc.Tag;

/**
 * Interface to all NFCTagReader-Classes.
 * Created by Max on 25.10.14.
 */
public interface NFCTagReader {
    /**
     * Read an NFC Tag, returning the Tag object.
     *
     * @return Tag object of the read NFC Tag
     */
    public Tag readTag();

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
}
