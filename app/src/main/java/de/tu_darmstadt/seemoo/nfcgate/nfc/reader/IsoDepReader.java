package de.tu_darmstadt.seemoo.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import androidx.annotation.NonNull;

import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigBuilder;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.OptionType;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.Technologies;

/**
 * Implements an NFCTagReader using the IsoDep technology
 *
 */
public class IsoDepReader extends NFCTagReader {
    private NFCTagReader mUnderlying;

    /**
     * Provides a NFC reader interface
     *
     * @param tag: A tag using the IsoDep technology.
     */
    IsoDepReader(Tag tag, String underlying) {
        super(IsoDep.get(tag));

        // set extended timeout
        ((IsoDep) mReader).setTimeout(5000);

        // determine underlying technology
        if (underlying.equals(Technologies.A))
            mUnderlying = new NfcAReader(tag);
        else
            mUnderlying = new NfcBReader(tag);
    }

    @NonNull
    @Override
    public ConfigBuilder getConfig() {
        ConfigBuilder builder = mUnderlying.getConfig();
        IsoDep readerIsoDep = (IsoDep) mReader;

        // an IsoDep tag can be backed by either NfcA or NfcB technology, build config accordingly
        if (mUnderlying instanceof NfcAReader)
            builder.add(OptionType.LA_HIST_BY, readerIsoDep.getHistoricalBytes());
        else
            builder.add(OptionType.LB_H_INFO_RSP, readerIsoDep.getHiLayerResponse());

        return builder;
    }
}
