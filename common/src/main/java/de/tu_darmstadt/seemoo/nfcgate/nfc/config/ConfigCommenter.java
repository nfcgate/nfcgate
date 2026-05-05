package de.tu_darmstadt.seemoo.nfcgate.nfc.config;

import android.annotation.SuppressLint;

import de.tu_darmstadt.seemoo.nfcgate.util.Utils;

public final class ConfigCommenter {
    private static final double CARRIER_FREQUENCY = 13.56e6;

    public static String comment(ConfigOption option) {
        byte[] value = option.getData();

        return switch (option.getType()) {
            case LA_BIT_FRAME_SDD -> bitFrameSDD(value);
            case LA_PLATFORM_CONFIG -> platformConfig(value);
            case LA_SEL_INFO -> selInfo(value);
            case LI_A_RATS_TB1 -> ratsTB1(value);
            case LI_A_BIT_RATE -> bitRate(value);
            case LI_A_RATS_TC1 -> ratsTC1(value);
            case LA_NFCID1, LI_A_HIST_BY -> "";
            default -> "Unknown config option: " + Utils.bytesToHex((byte) option.getType().value);
        };
    }

    private static String bitFrameSDD(byte[] value) {
        assertValueSize(value, 1);
        return String.format("[%s] (Bits 5-0)", byteToBits(value[0], 0, 5));
    }

    private static String platformConfig(byte[] value) {
        assertValueSize(value, 1);
        return String.format("[%s] (Bits 4-0)", byteToBits(value[0], 0, 4));
    }

    private static String selInfo(byte[] value) {
        assertValueSize(value, 1);

        boolean compliant = (value[0] & 0x20) != 0;
        return String.format("[ISO/IEC 14443-4 %s]",
                compliant ?  "compliant" : "non-compliant");
    }

    @SuppressLint("DefaultLocale")
    private static String ratsTB1(byte[] value) {
        assertValueSize(value, 1);

        int sfgi = (value[0] & 0xFF) >> 4, fwi = value[0] & 0xF;
        return String.format("[FWI: %d ~ %s] [SFGI: %d ~ %s]",
                fwi, integerToTime(fwi), sfgi, integerToTime(sfgi));
    }

    private static String bitRate(byte[] value) {
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
    private static String ratsTC1(byte[] value) {
        assertValueSize(value, 1);

        boolean nad = (value[0] & 0x01) != 0, cid = (value[0] & 0x02) != 0;
        return String.format("[NAD: %s] [CID: %s]",
                nad ? "supported" : "unsupported",
                cid ? "supported" : "unsupported");
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

    @SuppressWarnings("SameParameterValue")
    private static void assertValueSize(byte[] value, int size) {
        if (value.length != size)
            throw new IllegalArgumentException("Invalid value size: " + value.length);
    }
}
