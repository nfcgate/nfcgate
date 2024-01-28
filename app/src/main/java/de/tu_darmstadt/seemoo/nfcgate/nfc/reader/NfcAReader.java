package de.tu_darmstadt.seemoo.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.NfcA;
import androidx.annotation.NonNull;

import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigBuilder;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.OptionType;

/**
 * Implements a NFCTagReader using the NfcA technology
 */
public class NfcAReader extends NFCTagReader {
    /**
     * Provides a NFC reader interface
     *
     * @param tag: A tag using the NfcA technology.
     */
    NfcAReader(Tag tag) {
        super(NfcA.get(tag));
    }

    @NonNull
    @Override
    public ConfigBuilder getConfig() {
        ConfigBuilder builder = new ConfigBuilder();
        NfcA readerA = (NfcA) mReader;

        builder.add(OptionType.LA_NFCID1, readerA.getTag().getId());
        builder.add(OptionType.LA_SEL_INFO, (byte)readerA.getSak());
        builder.add(OptionType.LA_BIT_FRAME_SDD, readerA.getAtqa()[0]);
        builder.add(OptionType.LA_PLATFORM_CONFIG, readerA.getAtqa()[1]);

        return builder;
    }

}
