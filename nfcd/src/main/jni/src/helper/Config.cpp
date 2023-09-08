#include <nfcd/helper/Config.h>

#include <string>
#include <unordered_map>

std::unordered_map<uint8_t, std::string> knownConfigTypes = {
        // COMMON
        {0x00, "TOTAL_DURATION"},
        {0x01, "CON_DEVICES_LIMIT"},
        // 0x02 - 0x07 RFU

        // POLL A
        {0x08, "PA_BAIL_OUT"},
        // 0x09 - 0x0F RFU

        // POLL B
        {0x10, "PB_AFI"},
        {0x11, "PB_BAIL_OUT"},
        {0x12, "PB_ATTRIB_PARAM1"},
        {0x13, "PB_SENSB_REQ_PARAM"},
        // 0x14 - 0x17 RFU

        // POLL F
        {0x18, "PF_BIT_RATE"},
        {0x19, "PF_RC_CODE"},
        // 0x1A - 0x1F RFU

        // POLL ISO-DEP
        {0x20, "PB_H_INFO"},
        {0x21, "PI_BIT_RATE"},
        {0x22, "PA_ADV_FEAT"},
        // 0x23 - 0x27 RFU

        // POLL NFC-DEP
        {0x28, "PN_NFC_DEP_SPEED"},
        {0x29, "PN_ATR_REQ_GEN_BYTES"},
        {0x2A, "PN_ATR_REQ_CONFIG"},
        // 0x2B - 0x2F RFU

        // LISTEN A
        {0x30, "LA_BIT_FRAME_SDD"},
        {0x31, "LA_PLATFORM_CONFIG"},
        {0x32, "LA_SEL_INFO"},
        {0x33, "LA_NFCID1"},
        // 0x34 - 0x37 RFU

        // LISTEN B
        {0x38, "LB_SENSB_INFO"},
        {0x39, "LB_NFCID0"},
        {0x3A, "LB_APPLICATION_DATA"},
        {0x3B, "LB_SFGI"},
        {0x3C, "LB_ADC_FO"},
        // 0x3D - 0x3F RFU

        // LISTEN F
        {0x40, "LF_T3T_IDENTIFIERS_1"},
        {0x41, "LF_T3T_IDENTIFIERS_2"},
        {0x42, "LF_T3T_IDENTIFIERS_3"},
        {0x43, "LF_T3T_IDENTIFIERS_4"},
        {0x44, "LF_T3T_IDENTIFIERS_5"},
        {0x45, "LF_T3T_IDENTIFIERS_6"},
        {0x46, "LF_T3T_IDENTIFIERS_7"},
        {0x47, "LF_T3T_IDENTIFIERS_8"},
        {0x48, "LF_T3T_IDENTIFIERS_9"},
        {0x49, "LF_T3T_IDENTIFIERS_10"},
        {0x4A, "LF_T3T_IDENTIFIERS_11"},
        {0x4B, "LF_T3T_IDENTIFIERS_12"},
        {0x4C, "LF_T3T_IDENTIFIERS_13"},
        {0x4D, "LF_T3T_IDENTIFIERS_14"},
        {0x4E, "LF_T3T_IDENTIFIERS_15"},
        {0x4F, "LF_T3T_IDENTIFIERS_16"},
        {0x50, "LF_PROTOCOL_TYPE"},
        {0x51, "LF_T3T_PMM"},
        {0x52, "LF_T3T_MAX"},
        {0x53, "LF_T3T_FLAGS"},
        {0x54, "LF_CON_BITR_F"},
        {0x55, "LF_ADV_FEAT"},
        // 0x56 - 0x57 RFU

        // LISTEN ISO-DEP
        {0x58, "LI_FWI"},
        {0x59, "LA_HIST_BY"},
        {0x5A, "LB_H_INFO_RESP"},
        {0x5B, "LI_BIT_RATE"},
        // 0x5C - 0x5F RFU

        // LISTEN NFC-DEP
        {0x60, "LN_WT"},
        {0x61, "LN_ATR_RES_GEN_BYTES"},
        {0x62, "LN_ATR_RES_CONFIG"},
        // 0x63 - 0x7F RFU

        // OTHER
        {0x80, "RF_FIELD_INFO"},
        {0x81, "RF_NFCEE_ACTION"},
        {0x82, "NFCDEP_OP"},
        // 0x83 - 0x84 RFU
        {0x85, "NFCC_CONFIG_CONTROL"},
        // 0x86 - 0x9F RFU
};

std::string Option::name() const {
    auto it = knownConfigTypes.find(mType);
    return it != knownConfigTypes.end() ? it->second : "Unknown";
}

void Option::push(config_ref &config, uint8_t &offset) {
    /*
     * Each config option has:
     * - 1 byte type
     * - 1 byte length
     * - length byte data
     */
    config.get()[offset + 0] = type();
    config.get()[offset + 1] = len();

    memcpy(&config.get()[offset + 2], value(), len());
    offset += len() + 2;
}

void Config::build(config_ref &config) {
    uint8_t offset = 0;

    // calculate total size of needed buffer
    for (auto &opt : mOptions)
        mTotal += opt.len() + 2;

    // allocate buffer
    config.reset(new uint8_t[mTotal]);

    // push each option to buffer
    for (auto &opt : mOptions)
        opt.push(config, offset);
}

void Config::parse(uint8_t size, uint8_t *stream) {
    mOptions.clear();
    mTotal = 0;

    for (uint8_t offset = 0; offset < size - 2; ) {
        uint8_t type = stream[offset + 0];
        uint8_t len = stream[offset + 1];

        mOptions.emplace_back(type, &stream[offset + 2], len);
        offset += len + 2;
    }
}
