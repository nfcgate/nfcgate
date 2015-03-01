#ifndef __ANDROID__
#define __ANDROID__
#endif
#include <android/log.h>
#include <jni.h>
#include "vendor/substrate.h"
#include "vendor/libnfc.h"

#define LOG_TAG "NATIVENFC"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__ )
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__ )
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


struct s_chip_config {
    uint8_t atqa;
    uint8_t sak;
    uint8_t hist;
    uint8_t uid[64];
    uint8_t uid_len;
};

// main.cpp
extern bool patchEnabled;
void loghex(const char *desc, const uint8_t *data, const int len);

// java.cpp
void hookJava(JNIEnv *jni, jclass _class);


// chip.cpp
tNFC_STATUS hook_NfcSetConfig (UINT8 tlv_size, UINT8 *p_param_tlvs);
void hook_SetRfCback(tNFC_CONN_CBACK *p_cback);
void uploadPatchConfig();
void uploadOriginalConfig();
extern NFC_SetStaticRfCback *nci_SetRfCback;
extern NFC_SetConfig *nci_NfcSetConfig;
extern tCE_CB *ce_cb;
extern struct s_chip_config patchValues;

// ipc.cpp
void ipc_prepare();
void ipc_init();
