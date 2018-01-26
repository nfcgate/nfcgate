#include "nfcd.h"
#include "config.h"
#include "vendor/adbi/hook.h"
#include <cstring>
#include <unistd.h>

/**
 * Commands of the NCI configuration interface
 */

struct s_chip_config origValues = { 0 };
struct s_chip_config patchValues = { 0 };

NFC_SetStaticRfCback *nci_orig_SetRfCback;
NFC_SetConfig *nci_orig_NfcSetConfig;
NFC_SendData  *nfc_orig_sendData;
NFC_Deactivate  *nfc_orig_deactivate;

NFA_StopRfDiscovery  *nfa_orig_stop_rf_discovery;
NFA_DisablePolling *nfa_orig_disable_polling;
NFA_StartRfDiscovery *nfa_orig_start_rf_discovery;
NFA_EnablePolling *nfa_orig_enable_polling;

tCE_CB *ce_cb;

void nci_SetRfCback(tNFC_CONN_CBACK *p_cback) {
    hook_precall(&hook_rfcback);
    nci_orig_SetRfCback(p_cback);
    hook_postcall(&hook_rfcback);
}

tNFC_STATUS nci_NfcSetConfig (uint8_t size, uint8_t *tlv) {
    adbi_log("HOOKNFC: nci_NfcSetConfig() ENTER");
    hook_precall(&hook_config);
    tNFC_STATUS r = nci_orig_NfcSetConfig(size, tlv);
    hook_postcall(&hook_config);
    adbi_log("HOOKNFC: nci_NfcSetConfig() LEAVE");
    return r;
}

/**
 * hooked SetRfCback implementation.
 * call the original function, but modify the control structure if the patch is enabled
 */
void hook_SetRfCback(tNFC_CONN_CBACK *p_cback) {
    LOGD("hook_SetRfCback");
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

tNFC_STATUS hook_NfcDeactivate(UINT8 deactivate_type) {
    hook_precall(&hook_deactivate);
    tNFC_STATUS r;
    adbi_log("HOOKNFC deactivate(), we got %d", deactivate_type);
    r = nfc_orig_deactivate(deactivate_type);
    hook_postcall(&hook_deactivate);
    return r;
}

tNFC_STATUS hook_NfcSenddata(UINT8 conn_id, BT_HDR *p_data) {
    hook_precall(&hook_senddata);
    adbi_log("HOOKNFC senddata() offset: %d, len: %d", p_data->offset, p_data->len);
    loghex("HOOKNFC data:",  ((UINT8 *)(p_data + 1) + p_data->offset), 16);
    tNFC_STATUS r = nfc_orig_sendData(conn_id, p_data);
    hook_postcall(&hook_senddata);
    return r;
}

tNFA_STATUS  hook_NfaStopRfDiscovery(void) {
    hook_precall(&hook_nfa_stop_rf_discovery);
    adbi_log("HOOKNFC hook_NfaStopRfDiscovery()");
    tNFA_STATUS r = nfa_orig_stop_rf_discovery();
    hook_postcall(&hook_nfa_stop_rf_discovery);
    return r;
}

tNFA_STATUS  hook_NfaDisablePolling(void) {
    hook_precall(&hook_nfa_disable_polling);
    adbi_log("HOOKNFC hook_nfa_disable_polling()");
    tNFA_STATUS r = nfa_orig_disable_polling();
    hook_postcall(&hook_nfa_disable_polling);
    return r;
}

tNFA_STATUS hook_NfaStartRfDiscovery() {
    hook_precall(&hook_nfa_start_rf_discovery);
    adbi_log("HOOKNFC hook_NfaStartRfDiscovery()");
    tNFA_STATUS r = nfa_orig_start_rf_discovery();
    hook_postcall(&hook_nfa_start_rf_discovery);
    return r;
}

tNFA_STATUS hook_NfaEnablePolling(tNFA_TECHNOLOGY_MASK poll_mask) {
    hook_precall(&hook_nfa_enable_polling);
    adbi_log("HOOKNFC hook_NfaEnablePolling() 0x%x", poll_mask);
    tNFA_STATUS r = nfa_orig_enable_polling(poll_mask);
    hook_postcall(&hook_nfa_enable_polling);
    return r;
}

/**
 * hooked NfcSetConfig implementation
 */
tNFC_STATUS hook_NfcSetConfig (uint8_t size, uint8_t *tlv) {
    /*
     * read the config stream:
     *      if we are enabled and a previously set value gets overridden,
     *      then upload our configuration again afterwards
     *
     * in any case: save the values to allow re-uploading them when disabling the patch
     */
    loghex("HOOKNFC NfcSetConfig IN", tlv, size);
    LOGD("NfcSetConfig Enabled: %d", patchEnabled);

    Config cfg;
    cfg.parse(size, tlv);

    for (auto &opt : cfg.options()) {
        switch(opt.type()) {
            case NFC_PMID_LA_BIT_FRAME_SDD:
                origValues.bit_frame_sdd = *opt.value();
                LOGD("NfcSetConfig Read: BIT FRAME SDD 0x%02x", *opt.value());
                if (patchEnabled)
                    opt.value(&patchValues.bit_frame_sdd, 1);
                LOGD("NfcSetConfig NEW: BIT FRAME SDD 0x%02x %d", *opt.value(), opt.len());
            break;
            case NFC_PMID_LA_PLATFORM_CONFIG:
                origValues.platform_config = *opt.value();
                LOGD("NfcSetConfig Read: PLATFORM CONFIG 0x%02x", *opt.value());
                if (patchEnabled)
                    opt.value(&patchValues.platform_config, 1);
            break;
            case NFC_PMID_LA_SEL_INFO:
                origValues.sak = *opt.value();
                LOGD("NfcSetConfig Read: SAK  0x%02x", *opt.value());
                if (patchEnabled)
                    opt.value(&patchValues.sak, 1);
            break;
            case NFC_PMID_LA_HIST_BY:
                if (opt.len() > sizeof(origValues.hist))
                    LOGE("cannot handle an hist with len=0x%02x", opt.len());
                else {
                    memcpy(origValues.hist, opt.value(), opt.len());
                    origValues.uid_len = opt.len();
                    loghex("NfcSetConfig Read: HIST", opt.value(), opt.len());
                    if (patchEnabled)
                        opt.value(patchValues.hist, patchValues.hist_len);
                }
            break;
            case NFC_PMID_LA_NFCID1:
                if (opt.len() > sizeof(origValues.uid))
                    LOGE("cannot handle an uid with len=0x%02x", opt.len());
                else {
                    memcpy(origValues.uid, opt.value(), opt.len());
                    origValues.uid_len = opt.len();
                    loghex("NfcSetConfig Read: UID", opt.value(), opt.len());
                    if (patchEnabled)
                        opt.value(patchValues.uid, patchValues.uid_len);
                }
                break;
            default:
                LOGD("NfcSetConfig Read: %x len %d", opt.type(), opt.len());
                break;
        }
    }

    // any of our values got modified and we are active those values are already changed in stream
    config_ref bin_stream;
    cfg.build(bin_stream);
    loghex("HOOKNFC NfcSetConfig OUT", bin_stream.get(), cfg.total());
    tNFC_STATUS r = nci_NfcSetConfig(cfg.total(), bin_stream.get());

    return r;
}

/**
 * build a new configuration stream and upload it into the broadcom nfc controller
 */
static void uploadConfig(struct s_chip_config config) {
    Config cfg;
    cfg.add(NFC_PMID_LA_SEL_INFO, &config.sak);
    cfg.add(NFC_PMID_LA_BIT_FRAME_SDD, &config.bit_frame_sdd);
    cfg.add(NFC_PMID_LA_PLATFORM_CONFIG, &config.platform_config);
    cfg.add(NFC_PMID_LA_NFCID1, config.uid, config.uid_len);
    cfg.add(NFC_PMID_LA_HIST_BY, config.hist, config.hist_len);

    config_ref bin_stream;
    cfg.build(bin_stream);

    nci_NfcSetConfig(cfg.total(), bin_stream.get());
    //loghex("HOOKNFC Upload:", cfg, i);
}

void disablePolling() {
    adbi_log("HOOKNFC disable polling");
    hook_NfaStopRfDiscovery();
    usleep(10000);
    hook_NfaDisablePolling();
    usleep(10000);
    hook_NfaStartRfDiscovery();
    usleep(10000);
}

void enablePolling() {
    adbi_log("HOOKNFC enablePolling()");
    hook_NfaStopRfDiscovery();
    usleep(10000);
    hook_NfaEnablePolling(0xff);
    usleep(10000);
    hook_NfaStartRfDiscovery();
}

/**
 * upload the values we got from the ipc
 */
void uploadPatchConfig() {
    /*
     * Note: Disable discovery before setting the config,
     * because NFCID cannot be set during discovery according to the standard
     * (even though broadcom permits it, nxp does not)
     */
    hook_NfcDeactivate(0);
    uploadConfig(patchValues);
    hook_NfcDeactivate(3);
}

/**
 * upload the values we collected in  NfcSetConfig
 */
void uploadOriginalConfig() {
    hook_NfcDeactivate(0);
    uploadConfig(origValues);
    hook_NfcDeactivate(3);
}
