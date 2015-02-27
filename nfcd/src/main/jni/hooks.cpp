

#include "nfcd.h"

NFC_SetStaticRfCback *oldSetRfCback;
NFC_SetConfig *oldNfcSetConfig;
tCE_CB *ce_cb;


tNFC_STATUS newNfcSetConfig (UINT8 tlv_size, UINT8 *p_param_tlvs) {
    LOGI("newNfcSetConfig, %02x", tlv_size);
    for(UINT8 i=0; i<tlv_size; i++) {
        LOGI("byte: %02x", p_param_tlvs[i]);
    }

    if(tlv_size == 0x06) {
        p_param_tlvs[2] = 0x20; // SAK
        UINT8 var_arr[] = {
            // UID
            0x33, 0x07, 0x04, 0x7e, 0x89, 0x49, 0xbe, 0x25, 0x80,
            // HIST byte
            0x59, 0x01, 0x80,
            // ATQA (First byte)
            0x31, 0x01, 0x03
        };
        oldNfcSetConfig(sizeof(var_arr), var_arr);
    }
    return oldNfcSetConfig(tlv_size, p_param_tlvs);
}

void newSetRfCback(tNFC_CONN_CBACK *p_cback) {
    oldSetRfCback(p_cback);
    if(p_cback != NULL) {
        // fake that the default aid is selected
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_CC_FILE_SELECTED);
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_NDEF_SELECTED);
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_T4T_APP_SELECTED);
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_REG_AID_SELECTED);
        ce_cb->mem.t4t.status |= CE_T4T_STATUS_WILDCARD_AID_SELECTED;
    }
}
