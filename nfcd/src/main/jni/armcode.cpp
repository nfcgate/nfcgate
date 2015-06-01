#include "nfcd.h"


void hook_SetRfCback_arm(tNFC_CONN_CBACK *p_cback) {
    hook_SetRfCback(p_cback);
}
tNFC_STATUS hook_NfcSetConfig_arm (uint8_t size, uint8_t *tlv) {
    return hook_NfcSetConfig(size, tlv);
}
