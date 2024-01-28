package de.tu_darmstadt.seemoo.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import androidx.annotation.NonNull;

import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigBuilder;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.OptionType;

/**
 * Implements an NFCTagReader using the NfcF technology
 */
public class NfcFReader extends NFCTagReader {
    /**
     * Provides a NFC reader interface
     *
     * @param tag: A tag using the NfcF technology.
     */
    NfcFReader(Tag tag) {
        super(NfcF.get(tag));
    }

    @NonNull
    @Override
    public ConfigBuilder getConfig() {
        ConfigBuilder builder = new ConfigBuilder();
        NfcF readerF = (NfcF) mReader;

        // join systemcode and nfcid2
        byte[] t3t_identifier_1 = new byte[10];
        System.arraycopy(readerF.getSystemCode(), 0, t3t_identifier_1, 0, 2);
        System.arraycopy(readerF.getTag().getId(), 0, t3t_identifier_1, 2, 8);

        // set bit at index 1 to indicate activation of t3t_identifier_1
        byte[] t3t_flags = new byte[] { 1, 0 };

        builder.add(OptionType.LF_T3T_IDENTIFIERS_1, t3t_identifier_1);
        builder.add(OptionType.LF_T3T_FLAGS, t3t_flags);
        builder.add(OptionType.LF_T3T_PMM, readerF.getManufacturer());

        return builder;
    }
}
