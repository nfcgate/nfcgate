package tud.seemuh.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.TagTechnology;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.nfc.config.OptionType;
import tud.seemuh.nfcgate.nfc.config.Technologies;
import tud.seemuh.nfcgate.util.Utils;

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
