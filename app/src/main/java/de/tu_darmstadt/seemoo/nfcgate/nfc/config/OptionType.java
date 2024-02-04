package de.tu_darmstadt.seemoo.nfcgate.nfc.config;

/**
 * Represents all NCI configuration options that can occur in initial card data
 */
public enum OptionType {
    // LISTEN A

        // ATQA[0]
        LA_BIT_FRAME_SDD(0x30),
        // ATQA[1]
        LA_PLATFORM_CONFIG(0x31),
        // SAK
        LA_SEL_INFO(0x32),
        // UID
        LA_NFCID1(0x33),

    // LISTEN B

        // PUPI
        LB_NFCID0(0x39),
        // Bytes 6-9 of SENSB
        LB_APPLICATION_DATA(0x3A),
        // Start-Up Frame Guard Time (Protocol byte 1)
        LB_SFGI(0x3B),
        // Max Frames (128 bytes) / Protocol Type ISO-DEP support (Protocol byte 2)
        LB_SENSB_INFO(0x38),
        // FWI / ADC / F0 (Protocol byte 3)
        LB_ADC_FO(0x3C),

    // LISTEN F

        // contains [0:2] SystemCode and [3:10] NFCID2
        LF_T3T_IDENTIFIERS_1(0x40),
        // bitmask of valid T3T_IDENTIFIERS
        LF_T3T_FLAGS(0x53),
        // "manufacturer" aka PAD0, PAD1, MRTI_check, MRTI_update, PAD2
        LF_T3T_PMM(0x51),

    // LISTEN ISO-DEP

        // Historical bytes (NCI spec calls this LI_A_HIST_BY)
        LA_HIST_BY(0x59),
        // Higher layer response field
        LB_H_INFO_RSP(0x5A),
    ;

    // implementation details
    final int value;

    OptionType(int val) {
        value = val;
    }

    public byte getID() {
        return (byte)value;
    }

    public static OptionType fromType(byte type) {
        for (OptionType optionType : OptionType.values())
            if (optionType.getID() == type)
                return optionType;

        return null;
    }
}
