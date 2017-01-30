

#include "nfcd.h"
#include "vendor/adbi/hook.h"
#include <dlfcn.h>
#include <unistd.h>
#include <stdio.h>



bool patchEnabled = false;
struct hook_t hook_config;
struct hook_t hook_rfcback;
struct hook_t hook_senddata;
struct hook_t hook_deactivate;
struct hook_t hook_nfa_stop_rf_discovery;
struct hook_t hook_nfa_disable_polling;
struct hook_t hook_nfa_start_rf_discovery;
struct hook_t hook_nfa_enable_polling;

static void onHostEmulationLoad(JNIEnv *jni, jclass _class, void *data);
static void hookNative();
const char *hooklibfile = "/system/lib/libnfc-nci.so";


static void onModuleLoad() __attribute__((constructor));

void onModuleLoad() {
    LOGI("onModuleLoad::begin");
    hookNative();
    LOGI("onModuleLoad::end");
}


/**
 * find a native symbol and hook it
 */
static void findAndHook(struct hook_t* eph, void* handle, const char *symbol, void* hookf, void **original) {
    *original = dlsym(handle, symbol);
    if(hook(eph, (unsigned int)*original, hookf) != -1) {
        LOGI("HOOKNFC hooked: %s", symbol);
    }
}

/**
 * hook into native functions of the libnfc-nci broadcom nfc driver
 */
static void hookNative() {
    if(access(hooklibfile, F_OK) == -1) {
        LOGE("could not access %s to load symbols", hooklibfile);
        return;
    }
    void *handle = dlopen(hooklibfile, 0);

    findAndHook(&hook_config,  handle, "NFC_SetConfig",        (void*)&hook_NfcSetConfig, (void**)&nci_orig_NfcSetConfig);
    findAndHook(&hook_rfcback, handle, "NFC_SetStaticRfCback", (void*)&hook_SetRfCback,   (void**)&nci_orig_SetRfCback);

    findAndHook(&hook_senddata, handle, "NFC_SendData", (void*)&hook_NfcSenddata, (void**)&nfc_orig_sendData);
    findAndHook(&hook_deactivate, handle, "NFC_Deactivate", (void*)&hook_NfcDeactivate, (void**)&nfc_orig_deactivate);

    findAndHook(&hook_nfa_stop_rf_discovery, handle, "NFA_StopRfDiscovery", (void*) &hook_NfaStopRfDiscovery,  (void**) &nfa_orig_stop_rf_discovery);
    findAndHook(&hook_nfa_disable_polling, handle, "NFA_DisablePolling", (void*) &hook_NfaDisablePolling, (void**) &nfa_orig_disable_polling);
    findAndHook(&hook_nfa_start_rf_discovery, handle, "NFA_StartRfDiscovery", (void*) &hook_NfaStartRfDiscovery, (void**) &nfa_orig_start_rf_discovery);
    findAndHook(&hook_nfa_enable_polling, handle, "NFA_EnablePolling", (void*) &hook_NfaEnablePolling, (void**) &nfa_orig_enable_polling);

    // find pointer to ce_t4t control structure
    ce_cb = (tCE_CB*)dlsym(handle, "ce_cb");
}

/**
 * simple logging function for byte buffers
 */
void loghex(const char *desc, const uint8_t *data, const int len) {
    int strlen = len * 3 + 1;
    char *msg = (char *) malloc((size_t) strlen);
    for (uint8_t i = 0; i < len; i++) {
        sprintf(msg + i * 3, " %02x", (unsigned int) *(data + i));
    }
    LOGI("%s%s",desc, msg);
    free(msg);
}