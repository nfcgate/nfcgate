package de.tu_darmstadt.seemoo.nfcgate.nfc.reader;

import android.annotation.SuppressLint;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigBuilder;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigOption;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.OptionType;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.Technologies;

/**
 * Implements an NFCTagReader using the IsoDep technology
 *
 */
public class IsoDepReader extends NFCTagReader {
    private static final int TA_PRESENT = 0x10;   // TA(1) present (bit 5)
    private static final int TB_PRESENT = 0x20;   // TB(1) present (bit 6)
    private static final int TC_PRESENT = 0x40;   // TC(1) present (bit 7)

    private final NFCTagReader mUnderlying;
    private byte[] mAtsRes = null, mAttribRes = null;
    private boolean mSwapTB1 = false;

    /**
     * Provides a NFC reader interface
     *
     * @param tag: A tag using the IsoDep technology.
     * @param underlying: The underlying technology type (e.g., Technologies.A, Technologies.B)
     */
    IsoDepReader(Tag tag, String underlying) {
        super(IsoDep.get(tag));

        // set extended timeout
        ((IsoDep) mReader).setTimeout(5000);
        // extract ATS or ATTRIB response bytes if hook active
        byte[] resBytes = extractTagResBytes();

        // determine underlying technology
        if (underlying.equals(Technologies.A)) {
            mUnderlying = new NfcAReader(tag);
            mAtsRes = resBytes;
        }
        else {
            mUnderlying = new NfcBReader(tag);
            mAttribRes = resBytes;
        }
    }

    public void setSwapTB1(boolean value) {
        mSwapTB1 = value;
    }

    @Override
    public ConfigBuilder getConfig() {
        ConfigBuilder builder = mUnderlying.getConfig();
        IsoDep readerIsoDep = (IsoDep) mReader;

        // an IsoDep tag can be backed by either NfcA or NfcB technology, build config accordingly
        if (mUnderlying instanceof NfcAReader) {
            builder.addAll(parseAtsRes());
            builder.add(OptionType.LI_A_HIST_BY, readerIsoDep.getHistoricalBytes());
        }
        else {
            builder.addAll(parseAttribRes());
            builder.add(OptionType.LI_B_H_INFO_RSP, readerIsoDep.getHiLayerResponse());
        }

        return builder;
    }

    @SuppressLint("DefaultLocale")
    List<ConfigOption> parseAtsRes() {
        // empty ATS response
        if (mAtsRes == null || mAtsRes.length == 0)
            return null;

        // prepare result list
        List<ConfigOption> result = new ArrayList<>();

        // T0 indicates which of the optional bytes is present
        byte t0 = mAtsRes[0];
        int index = 1;

        // parse TA(1) if present
        if ((t0 & TA_PRESENT) != 0) {
            if (index == mAtsRes.length)
                throw new IllegalArgumentException("ATS_RES invalid: TA(1) expected but not present");

            byte ta = mAtsRes[index++];
            result.add(new ConfigOption(OptionType.LI_A_BIT_RATE, findMaxNCIBitRate(ta)));
        }

        // parse TB(1) if present
        if ((t0 & TB_PRESENT) != 0) {
            if (index == mAtsRes.length)
                throw new IllegalArgumentException("ATS_RES invalid: TB(1) expected but not present");

            byte tb = mAtsRes[index++];
            // flip the TB1 nibbles if needed
            if (mSwapTB1) {
                int fwi = (tb & 0xFF) >> 4, sfgi = tb & 0xF;
                tb = (byte) (fwi | (sfgi << 4));
            }
            result.add(new ConfigOption(OptionType.LI_A_RATS_TB1, tb));
        }

        // parse TC(1) if present
        if ((t0 & TC_PRESENT) != 0) {
            if (index == mAtsRes.length)
                throw new IllegalArgumentException("ATS_RES invalid: TC(1) expected but not present");

            byte tc = mAtsRes[index];
            result.add(new ConfigOption(OptionType.LI_A_RATS_TC1, tc));
        }

        return result;
    }

    private static byte findMaxNCIBitRate(byte ta) {
        // extract positive integer value from TA(1)
        int tai = ta & 0xFF;
        // bits 7-4: Card to reader divisor (DS)
        // bits 3-0: Reader to card divisor (DR)
        int ds = (tai >> 4) & 0x0F;  // Card to reader
        int dr = tai & 0x0F;         // Reader to card

        // Check if 212 kbps (0x01) is supported
        byte result = 0x00;
        if ((ds & 0x01) != 0 && (dr & 0x01) != 0)
            result = 0x01;
        // Check if 424 kbps (0x02) is supported
        if ((ds & 0x03) == 0x03 && (dr & 0x03) == 0x03)
            result = 0x02;
        // Check if 848 kbps (0x03) is supported
        if ((ds & 0x07) == 0x07 && (dr & 0x07) == 0x07)
            result = 0x03;

        return result;
    }

    List<ConfigOption> parseAttribRes() {
        return List.of();
    }
}
