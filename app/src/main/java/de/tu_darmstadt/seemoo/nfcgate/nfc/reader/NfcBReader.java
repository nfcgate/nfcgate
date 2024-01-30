package de.tu_darmstadt.seemoo.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.NfcB;
import androidx.annotation.NonNull;

import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigBuilder;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.OptionType;

/**
 * Implements a NFCTagReader using the NfcB technology
 */
public class NfcBReader extends NFCTagReader {
    /**
     * Provides a NFC reader interface
     *
     * @param tag: A tag using the NfcB technology.
     */
    NfcBReader(Tag tag) {
        super(NfcB.get(tag));
    }

    @NonNull
    @Override
    public ConfigBuilder getConfig() {
        ConfigBuilder builder = new ConfigBuilder();
        NfcB readerB = (NfcB) mReader;

        builder.add(OptionType.LB_NFCID0, readerB.getTag().getId());
        builder.add(OptionType.LB_APPLICATION_DATA, readerB.getApplicationData());
        builder.add(OptionType.LB_SFGI, readerB.getProtocolInfo()[0]);
        builder.add(OptionType.LB_SENSB_INFO, readerB.getProtocolInfo()[1]);
        builder.add(OptionType.LB_ADC_FO, readerB.getProtocolInfo()[2]);

        return builder;
    }

}
