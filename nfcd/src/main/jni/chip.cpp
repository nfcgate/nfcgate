

#include "nfcd.h"

#define CFG_TYPE_ATQA  0x31
#define CFG_TYPE_SAK   0x32
#define CFG_TYPE_UID   0x33
#define CFG_TYPE_HIST  0x59

static void uploadConfig(const s_chip_config config);

struct s_chip_config origValues = { 0 };
struct s_chip_config patchValues = { 0 };

NFC_SetStaticRfCback *nci_SetRfCback;
NFC_SetConfig *nci_NfcSetConfig;
tCE_CB *ce_cb;

void hook_SetRfCback(tNFC_CONN_CBACK *p_cback) {
    nci_SetRfCback(p_cback);
    if(p_cback != NULL && patchEnabled) {
        // fake that the default aid is selected
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_CC_FILE_SELECTED);
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_NDEF_SELECTED);
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_T4T_APP_SELECTED);
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_REG_AID_SELECTED);
        ce_cb->mem.t4t.status |= CE_T4T_STATUS_WILDCARD_AID_SELECTED;
    }
}

tNFC_STATUS hook_NfcSetConfig (uint8_t size, uint8_t *tlv) {

    loghex("NfcSetConfig", tlv, size);
    uint8_t i = 0;
    bool needUpload = false;
    // read the configuration bytestream and extract the values that we indent to override
    // if we are in an active mode and the value gets overridden, then upload our configuration afterwards
    // in any case: safe the values to allow re-uploading when deaktivation the patch
    while (size > i + 2) {
        // first byte: type
        // second byte: len (if len=0, then val=0)
        // following bytes: value (length: len)
        uint8_t type = *(tlv + i);
        uint8_t len  = *(tlv + i + 1);
        uint8_t *valbp = tlv + i + 2;
        uint8_t firstval = len ? *valbp : 0;
        i += 2 + len;

        switch(type) {
            case CFG_TYPE_ATQA:
                needUpload = true;
                origValues.atqa = firstval;
                LOGD("NfcSetConfig Read: ATQA 0x%02x", firstval);
                break;
            case CFG_TYPE_SAK:
                needUpload = true;
                origValues.sak = firstval;
                LOGD("NfcSetConfig Read: SAK  0x%02x", firstval);
                break;
            case CFG_TYPE_HIST:
                needUpload = true;
                origValues.hist = firstval;
                LOGD("NfcSetConfig Read: HIST 0x%02x", firstval);
                break;
            case CFG_TYPE_UID:
                needUpload = true;
                if(len > sizeof(origValues.uid)) {
                    LOGE("cannot handle an uid with len=0x%02x", len);
                } else {
                    memcpy(origValues.uid, valbp, len);
                    origValues.uid_len = len;
                    loghex("NfcSetConfig Read: UID", valbp, len);
                }
                break;
        }
    }
    tNFC_STATUS r = nci_NfcSetConfig(size, tlv);

    if(needUpload && patchEnabled) {
        patchValues.atqa = 0x03;
        patchValues.sak = 0x20;
        patchValues.hist = 0x80;
        patchValues.uid_len = 3;
        patchValues.uid[0] = 0xaa;
        patchValues.uid[1] = 0xbb;
        patchValues.uid[2] = 0xcc;
        uploadPatchConfig();
    }
    return r;
}


static void pushcfg(uint8_t *cfg, uint8_t &i, uint8_t type, uint8_t value) {
    cfg[i++] = type;
    if(value) {
      cfg[i++] = 1; // len
      cfg[i++] = value;
    } else {
      cfg[i++] = 0;
    }
}

static void uploadConfig(const struct s_chip_config config) {
    // cfg: type1, paramlen1, param1, type2, paramlen2....
    uint8_t cfg[80];
    uint8_t i=0;
    pushcfg(cfg, i, CFG_TYPE_SAK,  config.sak);
    pushcfg(cfg, i, CFG_TYPE_HIST, config.hist);
    pushcfg(cfg, i, CFG_TYPE_ATQA, config.atqa);

    cfg[i++] = CFG_TYPE_UID;
    cfg[i++] = config.uid_len;

    memcpy(cfg+i, config.uid, config.uid_len);

    nci_NfcSetConfig(i+config.uid_len, cfg);
    loghex("Upload:", cfg, i+config.uid_len);
}

void uploadPatchConfig() {
    uploadConfig(patchValues);
}

void uploadOriginalConfig() {
    uploadConfig(origValues);
}
