#include <nfcd/nfcd.h>
#include <jni.h>

void enableDisablePolling(bool enable) {
    LOGD("%s polling", (enable ? "enabling" : "disabling"));

    hNFA_StopRfDiscovery->call<def_NFA_StopRfDiscovery>();
    usleep(10000);
    if (enable)
        hNFA_EnablePolling->call<def_NFA_EnablePolling>(0xff);
    else
        hNFA_DisablePolling->call<def_NFA_DisablePolling>();
    usleep(10000);
    hNFA_StartRfDiscovery->call<def_NFA_StartRfDiscovery>();
    usleep(10000);
}

void uploadConfig(Config &config) {
    LOGI("uploadConfig");

    config_ref bin_stream;
    config.build(bin_stream);

    /*
     * Note: Disable discovery before setting the config,
     * because NFCID cannot be set during discovery according to the standard
     * (even though broadcom permits it, nxp does not)
     */
    hNFC_Deactivate->call<def_NFC_Deactivate>(0);

    guardConfig = false;
    hNFC_SetConfig->call<def_NFC_SetConfig>(config.total(), bin_stream.get());
    guardConfig = true;

    hNFC_Deactivate->call<def_NFC_Deactivate>(3);
}

extern "C" {
    JNIEXPORT jboolean JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_isHookEnabled(JNIEnv *, jobject) {
        return hookEnabled;
    }

    JNIEXPORT jboolean JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_isPatchEnabled(JNIEnv *, jobject) {
        return patchEnabled;
    }

    JNIEXPORT void JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_setConfiguration(JNIEnv *env, jobject, jbyteArray config) {
        if (!env->IsSameObject(config, nullptr)) {
            jsize config_len = env->GetArrayLength(config);
            jbyte *config_data = env->GetByteArrayElements(config, 0);
            hookValues.parse(config_len, (uint8_t *) config_data);
            env->ReleaseByteArrayElements(config, config_data, 0);

            patchEnabled = true;
            uploadConfig(hookValues);
        }
        else {
            patchEnabled = false;
            uploadConfig(origValues);
        }
    }

    JNIEXPORT void JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_setPolling(JNIEnv *, jobject, jboolean enabled) {
        enableDisablePolling(enabled);
    }
}
