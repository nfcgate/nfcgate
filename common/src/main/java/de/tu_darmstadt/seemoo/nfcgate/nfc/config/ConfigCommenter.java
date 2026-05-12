package de.tu_darmstadt.seemoo.nfcgate.nfc.config;

import android.annotation.SuppressLint;

import de.tu_darmstadt.seemoo.nfcgate.util.Utils;

public final class ConfigCommenter {
    private static final double CARRIER_FREQUENCY = 13.56e6;

    public static String comment(ConfigOption option) {
        byte[] value = option.getData();

        return switch (option.getType()) {
            case LA_BIT_FRAME_SDD -> formatBitFrameSDD(value);
            case LA_PLATFORM_CONFIG -> formatPlatformConfig(value);
            case LA_SEL_INFO -> formatSelInfo(value);
            case LB_SENSB_INFO -> formatSensbInfo(value);
            case LB_SFGI -> formatSfgi(value);
            case LB_FWI_ADC_FO -> formatFwiAdcFo(value);
            case LI_A_RATS_TB1 -> formatRatsTB1(value);
            case LB_BIT_RATE, LI_A_BIT_RATE -> formatBitRate(value);
            case LI_A_RATS_TC1 -> formatRatsTC1(value);
            case LA_NFCID1, LB_NFCID0, LB_APPLICATION_DATA, LF_T3T_IDENTIFIERS_1, LF_T3T_FLAGS, LF_T3T_PMM,
                 LI_A_HIST_BY, LI_B_H_INFO_RSP -> "";
            default -> "Unknown config option: " + Utils.bytesToHex((byte) option.getType().value);
        };
    }

    // +++ NFC-A

    private static String formatBitFrameSDD(byte[] value) {
        assertValueSize(value, 1);
        return String.format("[%s] (Bits 5-0)", byteToBits(value[0], 0, 5));
    }

    private static String formatPlatformConfig(byte[] value) {
        assertValueSize(value, 1);
        return String.format("[%s] (Bits 4-0)", byteToBits(value[0], 0, 4));
    }

    private static String formatSelInfo(byte[] value) {
        assertValueSize(value, 1);

        boolean compliant = (value[0] & 0x20) != 0;
        return String.format("[ISO/IEC 14443-4 %s]",
                compliant ?  "compliant" : "non-compliant");
    }

    // +++ NFC-B

    private static String formatSensbInfo(byte[] value) {
        assertValueSize(value, 1);

        int fsc = (value[0] & 0xFF) >> 4, pt = value[0] & 0xF;
        boolean compliant = (pt & 0x01) != 0;
        return String.format("[Frame Size: %s] [ISO/IEC 14443-4 %s]",
                fscBToFrameSize((byte) fsc), compliant ? "compliant" : "non-compliant");
    }

    @SuppressLint("DefaultLocale")
    private static String formatSfgi(byte[] value) {
        assertValueSize(value, 1);

        int sfgi = (value[0] & 0xFF) >> 4;
        return String.format("[SFGI: %d ~ %s]", sfgi, integerToTime(sfgi));
    }

    @SuppressLint("DefaultLocale")
    private static String formatFwiAdcFo(byte[] value) {
        assertValueSize(value, 1);

        int fwi = (value[0] & 0xFF) >> 4, adc = ((value[0] & 0xff) >> 2) & 0x03, fo = value[0] & 0x03;
        boolean afiEncoded = (adc & 0x01) != 0, nad = (fo & 0x02) != 0, did = (fo & 0x01) != 0;
        return String.format("[FWI: %d ~ %s] [AFI is %s] [NAD: %s] [DID: %s]",
                fwi, integerToTime(fwi), afiEncoded ? "encoded" : "not encoded",
                nad ? "supported" : "unsupported", did ? "supported" : "unsupported");
    }

    // +++ ISO-DEP

    @SuppressLint("DefaultLocale")
    private static String formatRatsTB1(byte[] value) {
        assertValueSize(value, 1);

        int sfgi = (value[0] & 0xFF) >> 4, fwi = value[0] & 0xF;
        return String.format("[FWI: %d ~ %s] [SFGI: %d ~ %s]",
                fwi, integerToTime(fwi), sfgi, integerToTime(sfgi));
    }

    private static String formatBitRate(byte[] value) {
        assertValueSize(value, 1);

        if (value[0] == 0x1)
            return "[212 kbps]";
        else if (value[0] == 0x2)
            return "[424 kbps]";
        else if (value[0] == 0x3)
            return "[848 kbps]";
        else
            return "[Unknown bit rate]";

    }

    @SuppressLint("DefaultLocale")
    private static String formatRatsTC1(byte[] value) {
        assertValueSize(value, 1);

        boolean nad = (value[0] & 0x01) != 0, did = (value[0] & 0x02) != 0;
        return String.format("[NAD: %s] [DID: %s]",
                nad ? "supported" : "unsupported", did ? "supported" : "unsupported");
    }

    // helpers

    @SuppressLint("DefaultLocale")
    private static String integerToTime(int value) {
        double timeMs = (256 * 16d / CARRIER_FREQUENCY) * Math.pow(2, value) * 1000;
        if (timeMs > 10)
            return String.format("%.0f ms", timeMs);
        else if (timeMs > 1)
            return String.format("%.1f ms", timeMs);
        else
            return String.format("%.0f us", timeMs * 1000);
    }

    @SuppressWarnings("SameParameterValue")
    private static String byteToBits(byte value, int offset, int length) {
        StringBuilder bits = new StringBuilder();
        for (int i = offset + length - 1; i >= offset; i--)
            bits.append((value & (1 << i)) != 0 ? "1" : "0");

        return bits.toString();
    }

    private static String fscBToFrameSize(byte value) {
        return switch (value) {
            case 0x00 -> "16 bytes";
            case 0x01 -> "24 bytes";
            case 0x02 -> "32 bytes";
            case 0x03 -> "40 bytes";
            case 0x04 -> "48 bytes";
            case 0x05 -> "64 bytes";
            case 0x06 -> "96 bytes";
            case 0x07 -> "128 bytes";
            case 0x08 -> "256 bytes";
            case 0x09 -> "512 bytes";
            case 0x0A -> "1024 bytes";
            case 0x0B -> "2048 bytes";
            case 0x0C -> "4096 bytes";
            default -> "RFU > 4096 bytes";
        };
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertValueSize(byte[] value, int size) {
        if (value.length != size)
            throw new IllegalArgumentException("Invalid value size: " + value.length);
    }
}
