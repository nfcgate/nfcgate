

#include "nfcd.h"
#include <dlfcn.h>
#include <unistd.h>
#include <stdio.h>

bool patchEnabled = false;

static void onHostEmulationLoad(JNIEnv *jni, jclass _class, void *data);
static void hookNative();

/**
 * configure substrate to hook into the app_process process (zygote)
 */
MSConfig(MSFilterExecutable, "/system/bin/app_process")

/**
 * intialize call of substrate
 */
MSInitialize {
    // when in zygote, register for a callback when the HostEmulationManager gets loaded.
    // this is our signal that we reached the nfc daemon process
    const char *classname = "com/android/nfc/cardemulation/HostEmulationManager";
    MSJavaHookClassLoad(NULL, classname, &onHostEmulationLoad);
    ipc_prepare();
}

/**
 * callback when HostEmulationManager gets loaded.
 * => we are now in the nfc daemon process
 */
static void onHostEmulationLoad(JNIEnv *jni, jclass _class, void *data) {
    LOGI("onHostEmulationLoad, loading hooks");
    // hooking into the java and native part of the nfcd
    hookJava(jni, _class);
    hookNative();
    ipc_init();
}

/**
 * hook into native functions of the libnfc-nci broadcom nfc driver
 */
static void hookNative() {
    const char *libfile = "/system/lib/libnfc-nci.so";
    if(access(libfile, F_OK) == -1) {
        LOGE("could not access %s to load symbols", libfile);
        return;
    }

    void *handle = dlopen(libfile, 0);

    // find function pointer to NFC_SetStaticRfCback symbol and hook into it
    void *fptr = dlsym(handle, "NFC_SetStaticRfCback");
    if(fptr) {
        MSHookFunction(fptr, (void*)&hook_SetRfCback, (void**)&nci_SetRfCback);
        LOGI("hooked: NFC_SetStaticRfCback");
    } else {
        LOGE("could NOT hook: NFC_SetStaticRfCback");
    }

    // find pointer to ce_t4t control structure
    ce_cb = (tCE_CB*)dlsym(handle, "ce_cb");

    // find NFC_SetConfig
    fptr = dlsym(handle, "NFC_SetConfig");
    if(fptr) {
        MSHookFunction(fptr, (void *) &hook_NfcSetConfig, (void **) &nci_NfcSetConfig);
        LOGI("hooked: NFC_SetConfig()");
    } else {
        LOGE("could NOT hook: NFC_SetConfig()");
    }
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