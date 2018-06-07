package tud.seemuh.nfcgate.nfc.reader;

import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;

/**
 * Interface to all NFCTagReader-Classes.
 */
public interface NFCTagReader {
    /*
     * Constants to indicate the used NFC reader technology
     */
    enum Protocol {
        NFCDEP,
        ISODEP,
        MIFARE_CLASSIC,
        MIFARE_ULTRALIGHT,
        NFC_A,
        NFC_B,
        NFC_BARCODE,
        NFC_F,
        NFC_V
    }

    /**
     * Send a raw command to the NFC chip, receiving the answer as a byte[]
     *
     * @param command: byte[]-representation of the command to be sent
     * @return byte[]-representation of the answer of the NFC chip
     */
    byte[] sendCmd(byte[] command);

    /**
     * Returns the protocol used by the reader
     */
    Protocol getProtocol();

    /**
     * Closes the connection, no further communication will be possible
     */
    void closeConnection();

    /**
     * Indicates whether the connection is open
     */
    boolean isConnected();

    /**
     * Returns a config object with options set to emulate this tag
     */
    ConfigBuilder getConfig();
}
