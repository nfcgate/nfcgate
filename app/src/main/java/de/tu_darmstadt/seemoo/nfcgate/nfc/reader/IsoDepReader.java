package de.tu_darmstadt.seemoo.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import androidx.annotation.NonNull;

import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigBuilder;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.OptionType;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.Technologies;
import de.tu_darmstadt.seemoo.nfcgate.util.ATS;

/**
 * Implements an NFCTagReader using the IsoDep technology
 *
 */
public class IsoDepReader extends NFCTagReader {
    private final NFCTagReader mUnderlying;

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
        if (mUnderlying instanceof NfcAReader) {
            // For NFC-A based ISO-DEP, try to get ATS data first
            ATS ats = getAts();
            if (ats != null) {
                Log.i("IsoDepReader", "Adding ATS config");
                builder.addAll(ats.getConfig());
            } else {
                // Fallback to original logic if ATS retrieval fails
                builder.add(OptionType.LA_HIST_BY, readerIsoDep.getHistoricalBytes());
            }
        } else {
            builder.add(OptionType.LB_H_INFO_RSP, readerIsoDep.getHiLayerResponse());
        }

        return builder;
    }

    /**
     * Attempts to get the ATS (Answer To Select) response by issuing a RATS command manually
     * 
     * @return parsed ATS response object, or null if RATS command fails or not possible to execute
     */
    public ATS getAts() {
        try {
            this.close();
            this.mUnderlying.connect();
            // RATS format: E0 FSDI|CID (where FSDI=Frame Size and CID=Card Identifier)
            // Using FSDI=8 (256 bytes) and CID=0
            byte[] ratsCommand = new byte[] { (byte) 0xE0, (byte) 0x80 };
            byte[] atsResponse = this.mUnderlying.transceive(ratsCommand);
            return ATS.parse(atsResponse);
        } catch (Exception e) {
            Log.e("IsoDepReader", "Could not get full ATS due to " + Log.getStackTraceString(e));
            // If something fails, return null
            return null;
        } finally {
            this.mUnderlying.close();
            this.connect();
        }
    }
}
