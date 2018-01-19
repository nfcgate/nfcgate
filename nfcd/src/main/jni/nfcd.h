#ifndef __ANDROID__
#define __ANDROID__
#endif

#include <android/log.h>
#include <jni.h>
#include <stdint.h>
#include "vendor/libnfc.h"

#define LOG_TAG "NATIVENFC"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__ )
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__ )
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/**
 * all values we override in one struct
 */
struct s_chip_config {
    uint8_t atqa;
    uint8_t sak;
    uint8_t hist[64];
    uint8_t hist_len;
    uint8_t uid[64];
    uint8_t uid_len;
};

// main.cpp
extern bool patchEnabled;

// chip.cpp
tNFC_STATUS hook_NfcSetConfig (UINT8 tlv_size, UINT8 *p_param_tlvs);
void hook_SetRfCback(tNFC_CONN_CBACK *p_cback);
tNFC_STATUS hook_NfcSenddata(UINT8 conn_id, BT_HDR *p_data);
tNFC_STATUS hook_NfcDeactivate (UINT8 deactivate_type);
tNFA_STATUS hook_NfaStopRfDiscovery();
tNFA_STATUS hook_NfaDisablePolling();
tNFA_STATUS hook_NfaStartRfDiscovery();
tNFA_STATUS hook_NfaEnablePolling(tNFA_TECHNOLOGY_MASK poll_mask);

void uploadPatchConfig();
void uploadOriginalConfig();
void enablePolling();
void disablePolling();

extern NFC_SetStaticRfCback *nci_orig_SetRfCback;
extern NFC_SetConfig *nci_orig_NfcSetConfig;
extern NFC_SendData *nfc_orig_sendData;
extern NFC_Deactivate *nfc_orig_deactivate;
extern NFA_StopRfDiscovery *nfa_orig_stop_rf_discovery;
extern NFA_DisablePolling *nfa_orig_disable_polling;
extern NFA_StartRfDiscovery *nfa_orig_start_rf_discovery;
extern NFA_EnablePolling *nfa_orig_enable_polling;

extern tCE_CB *ce_cb;
extern struct s_chip_config patchValues;
extern struct hook_t hook_config;
extern struct hook_t hook_rfcback;
extern struct hook_t hook_senddata;
extern struct hook_t hook_deactivate;
extern struct hook_t hook_nfa_stop_rf_discovery;
extern struct hook_t hook_nfa_disable_polling;
extern struct hook_t hook_nfa_start_rf_discovery;
extern struct hook_t hook_nfa_enable_polling;