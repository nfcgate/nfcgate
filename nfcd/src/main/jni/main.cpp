

#include "nfcd.h"
#include "vendor/hook.h"
#include <dlfcn.h>
#include <unistd.h>
#include <stdio.h>



bool patchEnabled = false;

static void onHostEmulationLoad(JNIEnv *jni, jclass _class, void *data);
static void hookNative();
const char *hooklibfile = "/system/lib/libnfc-nci.so";


static void onModuleLoad() __attribute__((constructor));

void onModuleLoad() {
    LOGI("onModuleLoad::begin");
    hookNative();
    ipc_init();
    LOGI("onModuleLoad::end");
}

JNIEXPORT jboolean JNICALL Java_tud_seemuh_nfcgate_xposed_Hooks_isPatchEnabled(JNIEnv* env, jobject javaThis) {
    return patchEnabled;
}

/**
 * find a native symbol and hook it
 */
static void findAndHook(void* handle, const char *symbol, void* hook, void **original) {
    *original = dlsym(handle, symbol);
    do_hook(hooklibfile, (uint32_t)hook, symbol);
    LOGI("hooked: %s", symbol);
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

    //findAndHook(handle, "dummy", (void*)&hook_NfcSetConfig, (void**)&nci_NfcSetConfig);
    findAndHook(handle, "NFC_SetConfig", (void*)&hook_NfcSetConfig, (void**)&nci_NfcSetConfig);
    findAndHook(handle, "NFC_SetStaticRfCback", (void*)&hook_SetRfCback, (void**)&nci_SetRfCback);


    if(nci_NfcSetConfig == hook_NfcSetConfig) LOGI("original missing");

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