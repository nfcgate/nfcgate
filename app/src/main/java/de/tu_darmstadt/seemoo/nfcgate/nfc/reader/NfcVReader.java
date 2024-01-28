package de.tu_darmstadt.seemoo.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.NfcV;
import androidx.annotation.NonNull;

import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigBuilder;

/**
 * Implements an NFCTagReader using the NfcV technology
 */
public class NfcVReader extends NFCTagReader {
    /**
     * Provides a NFC reader interface
     *
     * @param tag: A tag using the NfcV technology.
     */
    NfcVReader(Tag tag) {
        super(NfcV.get(tag));
    }

    @NonNull
    @Override
    public ConfigBuilder getConfig() {
        // TODO: V tags cannot be emulated (yet)
        return new ConfigBuilder();
    }
}
