package tud.seemuh.nfcgate.util;

/**
 * Interface to all NFC Tag Emulator classes
 *
 * Created by Max on 25.10.14.
 */
public interface NFCTagEmulator {
    /**
     * Send a command or response to the card reader.
     * @param command The byte[]-representation of the command to be sent
     * @return The byte[]-representation of the reply, if there is one
     *
     * TODO: Note to dev: Probably a case for "sendResponseApdu()" from HostAdpuService
     */
    public byte[] sendCmd(byte[] command);
}
