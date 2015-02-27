#ifndef __ANDROID__
#define __ANDROID__
#endif
#include <android/log.h>
#include <jni.h>
#include "vendor/substrate.h"
#include "vendor/libnfc.h"

#define LOG_TAG "NATIVENFC"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__ )
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


// java.cpp
void hookJava(JNIEnv *jni, jclass _class);


// hooks.cpp
tNFC_STATUS newNfcSetConfig (UINT8 tlv_size, UINT8 *p_param_tlvs);
void newSetRfCback(tNFC_CONN_CBACK *p_cback);
extern NFC_SetStaticRfCback *oldSetRfCback;
extern NFC_SetConfig *oldNfcSetConfig;
extern tCE_CB *ce_cb;