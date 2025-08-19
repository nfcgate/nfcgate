package de.tu_darmstadt.seemoo.nfcgate.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigBuilder;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.OptionType;


/**
 * Answer To Select (ATS) parser for ISO14443-4 Type A cards.
 * The ATS contains configuration parameters returned by a card in response to a RATS command.
 * According to ISO14443-4, the ATS structure is:
 * - TL (1 byte): Length of ATS
 * - T0 (1 byte): Format byte (FSCI, FWI, presence of optional TA/TB/TC bytes)
 * - TA(1) (optional): supported divisors
 * - TB(1) (optional): FWI and SFGI values
 * - TC(1) (optional): CID/NAD support
 * - Historical bytes (optional): Application-specific data
 */
public final class ATS {
    // T0 format byte bit masks
    private static final int TA_PRESENT = 0x10;   // TA(1) present (bit 5)
    private static final int TB_PRESENT = 0x20;   // TB(1) present (bit 6)
    private static final int TC_PRESENT = 0x40;   // TC(1) present (bit 7)

    /**
     * Parse ATS bytes into NCI configuration
     *
     * @param atsBytes The complete ATS response bytes
     * @return ConfigBuilder containing NCI configuration options that were extracted from the ATS data
     * @throws IllegalArgumentException if the ATS data is invalid
     */
    public static ConfigBuilder parse(byte[] atsBytes) {
        if (atsBytes == null || atsBytes.length < 2) {
            throw new IllegalArgumentException("ATS must be at least 2 bytes (TL + T0)");
        }

        int tl = atsBytes[0] & 0xFF;
        if (tl != atsBytes.length - 1) {
            throw new IllegalArgumentException("ATS length mismatch: TL=" + tl + ", actual=" + atsBytes.length);
        }

        ConfigBuilder builder = new ConfigBuilder();

        byte t0 = atsBytes[1];
        int offset = 2;

        // Parse optional interface bytes

        if ((t0 & TA_PRESENT) != 0) {
            if (offset >= atsBytes.length) {
                throw new IllegalArgumentException("ATS truncated: TA(1) expected but not present");
            }
            byte ta = atsBytes[offset++];
            // Set closest DR/DS bit config of TA(1) if present
            builder.add(OptionType.LI_A_BIT_RATE, new byte[] { getClosestNciBitRateConfig(ta) });
        }

        if ((t0 & TB_PRESENT) != 0) {
            if (offset >= atsBytes.length) {
                throw new IllegalArgumentException("ATS truncated: TB(1) expected but not present");
            }
            byte tb = atsBytes[offset++];
            // Add TB(1) if present - contains FWI and SFGI
            builder.add(OptionType.LI_A_RATS_TB1, new byte[] { tb });
        }

        if ((t0 & TC_PRESENT) != 0) {
            if (offset >= atsBytes.length) {
                throw new IllegalArgumentException("ATS truncated: TC(1) expected but not present");
            }
            byte tc = atsBytes[offset++];
            // Add TC(1) if present - contains CID/NAD support info
            builder.add(OptionType.LI_A_RATS_TC1, new byte[] { tc });
        }

        // Extract historical bytes (remaining bytes)
        byte[] historical = new byte[atsBytes.length - offset - 1];
        if (historical.length > 0) {
            System.arraycopy(atsBytes, offset, historical, 0, historical.length);
            // Add historical bytes if present
            if (historical.length != 0) {
                builder.add(OptionType.LA_HIST_BY, historical);
            }

        }

        return builder;
    }

    /**
     * Get the closest NCI bit rate configuration that doesn't exceed the original TA value.
     *
     * @param taValue The original TA(1) byte containing divisor values
     * @return The closest NCI bit rate configuration byte
     */
    private static byte getClosestNciBitRateConfig(byte taValue) {
        // NCI bit rate configurations:
        // 0x00 -> 106 kbps -> 0b_0_000_0_000
        // 0x01 -> 212 kbps -> 0b_0_001_0_001  
        // 0x02 -> 424 kbps -> 0b_0_011_0_011
        // 0x03 -> 848 kbps -> 0b_0_111_0_111

        int ta = taValue & 0xFF;
        // Extract divisor values from TA(1)
        // Bits 7-4: Card to reader divisor (DSI)
        // Bits 3-0: Reader to card divisor (DRI)
        int dsi = (ta >> 4) & 0x0F;  // Card to reader
        int dri = ta & 0x0F;         // Reader to card

        // Find the highest NCI config that doesn't exceed either direction
        byte maxConfig = 0x00;  // Start with 106 kbps

        // Check if 212 kbps (0x01) is supported
        if ((dsi & 0x01) != 0 && (dri & 0x01) != 0) {
            maxConfig = 0x01;
        }

        // Check if 424 kbps (0x02) is supported  
        if ((dsi & 0x03) == 0x03 && (dri & 0x03) == 0x03) {
            maxConfig = 0x02;
        }

        // Check if 848 kbps (0x03) is supported
        if ((dsi & 0x07) == 0x07 && (dri & 0x07) == 0x07) {
            maxConfig = 0x03;
        }

        return maxConfig;
    }
}
