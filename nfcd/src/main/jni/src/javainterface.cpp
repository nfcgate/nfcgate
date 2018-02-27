#include <nfcd/nfcd.h>
#include <cstring>

extern "C" {
    JNIEXPORT jboolean JNICALL Java_tud_seemuh_nfcgate_xposed_Native_isEnabled(JNIEnv* env, jobject javaThis);
    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_setEnabled(JNIEnv* env, jobject javaThis, jboolean enabled);
    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_uploadConfiguration(JNIEnv* env, jobject javaThis, jbyteArray _config);
    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_disablePolling(JNIEnv* env, jobject javaThis);
    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_enablePolling(JNIEnv* env, jobject javaThis);
}


JNIEXPORT jboolean JNICALL Java_tud_seemuh_nfcgate_xposed_Native_isEnabled(JNIEnv* env, jobject javaThis) {
    return patchEnabled;
}

JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_disablePolling(JNIEnv* env, jobject javaThis) {
    disablePolling();
}

JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_enablePolling(JNIEnv* env, jobject javaThis) {
    enablePolling();
}

JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_setEnabled(JNIEnv* env, jobject javaThis, jboolean enabled) {
    patchEnabled = enabled;
    if(enabled) {
        uploadPatchConfig();
    } else {
        uploadOriginalConfig();
    }
}

JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_xposed_Native_uploadConfiguration(JNIEnv* env, jobject javaThis, jbyteArray _config) {
    jsize config_len = env->GetArrayLength(_config);
    jbyte* config = env->GetByteArrayElements(_config, 0);

    patchValues.parse(config_len, (uint8_t*)config);

    env->ReleaseByteArrayElements(_config, config, 0);
}