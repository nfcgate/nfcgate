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
    JNIEXPORT jboolean JNICALL Java_tud_seemuh_nfcgate_xposed_Native_isEnabled(JNIEnv *, jobject) {
        return patchEnabled;
    }

    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_disablePolling(JNIEnv *, jobject) {
        enableDisablePolling(false);
    }

    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_enablePolling(JNIEnv *, jobject) {
        enableDisablePolling(true);
    }

    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_setEnabled(JNIEnv *, jobject, jboolean enabled) {
        patchEnabled = enabled;
        uploadConfig(enabled ? patchValues : origValues);
    }

    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_uploadConfiguration(JNIEnv *env, jobject, jbyteArray config) {
        jsize config_len = env->GetArrayLength(config);
        jbyte *config_data = env->GetByteArrayElements(config, 0);

        patchValues.parse(config_len, (uint8_t *) config_data);

        env->ReleaseByteArrayElements(config, config_data, 0);
    }
}