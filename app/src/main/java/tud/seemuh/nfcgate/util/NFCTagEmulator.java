package tud.seemuh.nfcgate.util;

/**
 * Interface to all NFC Tag Emulator classes
 *
 * Created by Max on 25.10.14.
 */
public interface NFCTagEmulator {
    // These constants are a proposal to the developer of this part of the app
    // If you find that they do not fit your needs, feel free to change them.
    // Just notify everyone else who may be using them :)
    // (It may be that individual protocols have to be emulated using different classes,
    // this is why these constants are defined to distinguish between them)
    public static final int EMULATOR_ISODEP            = 0;
    public static final int EMULATOR_MIFARE_CLASSIC    = 1;
    public static final int EMULATOR_MIFARE_ULTRALIGHT = 2;
    public static final int EMULATOR_NDEF              = 3;
    public static final int EMULATOR_NDEF_FORMATABLE   = 4;
    public static final int EMULATOR_NFC_A             = 5;
    public static final int EMULATOR_NFC_B             = 6;
    public static final int EMULATOR_NFC_BARCODE       = 7;
    public static final int EMULATOR_NFC_F             = 8;
    public static final int EMULATOR_NFC_V             = 9;
    
    /**
     * Send a command or response to the card reader.
     * @param command The byte[]-representation of the command to be sent
     * @return The byte[]-representation of the reply, if there is one
     *
     * TODO: Note to dev: Probably a case for "sendResponseApdu()" from HostAdpuService
     */
    public byte[] sendCmd(byte[] command);

    /**
     * Returns the integer representation of the protocol the used implementation is speaking.
     * This will be one of the EMULATOR_* constants defined by the util.NFCTagReader interface.
     *
     * @return integer representation of the underlying NFC tag reader protocol
     */
    public int getProtocol();
}
