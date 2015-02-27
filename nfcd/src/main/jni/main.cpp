

#include "nfcd.h"
#include <dlfcn.h>
#include <unistd.h>

static void onHostEmulationLoad(JNIEnv *jni, jclass _class, void *data);
static void hookNative();

// configure substrate to hook into the app_process process (zygote)
MSConfig(MSFilterExecutable, "/system/bin/app_process")

MSInitialize {
    // when in zygote, register for a callback when the HostEmulationManager gets loaded.
    // this is our signal that we reached the nfd daemon process
    const char *classname = "com/android/nfc/cardemulation/HostEmulationManager";
    MSJavaHookClassLoad(NULL, classname, &onHostEmulationLoad);

}


static void onHostEmulationLoad(JNIEnv *jni, jclass _class, void *data) {
    LOGI("onHostEmulationLoad, loading hooks");
    // hooking into the java and native part of the nfcd
    hookJava(jni, _class);
    hookNative();
}


static void hookNative() {
    const char *libfile = "/system/lib/libnfc-nci.so";
    if( access(libfile, F_OK) == -1 ) {
        LOGE("could not access %s to load symbols", libfile);
        return;
    }

    void *handle = dlopen(libfile, 0);

    // find function pointer to NFC_SetStaticRfCback symbol and hook into it
    void *fptr = dlsym(handle, "NFC_SetStaticRfCback");
    if(fptr) {
        MSHookFunction(fptr, (void*)&newSetRfCback, (void**)&oldSetRfCback);
        LOGI("hooked: NFC_SetStaticRfCback");
    } else {
        LOGE("could NOT hook: NFC_SetStaticRfCback");
    }

    // find pointer to ce_t4t control structure
    ce_cb = (tCE_CB*)dlsym(handle, "ce_cb");

    // find NFC_SetConfig
    void *fptr2 = dlsym(handle, "NFC_SetConfig");
    if(fptr2) {
        MSHookFunction(fptr2, (void *) &newNfcSetConfig, (void **) &oldNfcSetConfig);
        LOGI("hooked: NFC_SetConfig()");
    } else {
        LOGE("could NOT hook: NFC_SetConfig()");
    }
}
