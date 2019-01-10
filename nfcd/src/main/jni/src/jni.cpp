#include <nfcd/nfcd.h>
#include <jni.h>

void enableDisablePolling(bool enable) {
    LOGD("%s polling", (enable ? "enabling" : "disabling"));

    hook_NFA_StopRfDiscovery();
    usleep(10000);
    enable ? hook_NFA_EnablePolling(0xff) : hook_NFA_DisablePolling();
    usleep(10000);
    hook_NFA_StartRfDiscovery();
    usleep(10000);
}

void uploadConfig(Config &config) {
    config_ref bin_stream;
    config.build(bin_stream);

    /*
     * Note: Disable discovery before setting the config,
     * because NFCID cannot be set during discovery according to the standard
     * (even though broadcom permits it, nxp does not)
     */
    hook_NFC_Deactivate(0);
    // call original method instead of hooked one to prevent our config being overwritten by hook
    hNFC_SetConfig->callOther<decltype(hook_NFC_SetConfig)>(config.total(), bin_stream.get());
    hook_NFC_Deactivate(3);
}

extern "C" {
    JNIEXPORT jboolean JNICALL Java_tud_seemuh_nfcgate_xposed_Native_isHookEnabled(JNIEnv *, jobject) {
        return hookEnabled;
    }

    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_setConfiguration(JNIEnv *env, jobject, jbyteArray config) {
        if (!env->IsSameObject(config, nullptr)) {
            jsize config_len = env->GetArrayLength(config);
            jbyte *config_data = env->GetByteArrayElements(config, 0);
            hookValues.parse(config_len, (uint8_t *) config_data);
            env->ReleaseByteArrayElements(config, config_data, 0);

            hookEnabled = true;
            uploadConfig(hookValues);
        }
        else {
            hookEnabled = false;
            uploadConfig(origValues);
        }
    }

    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_setPolling(JNIEnv *, jobject, jboolean enabled) {
        enableDisablePolling(enabled);
    }
}