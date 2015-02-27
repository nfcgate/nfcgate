
#include "nfcd.h"

/**
* Hook Java Code to overwrite findSelectAid() behavior
**/
static jstring $HostEmulationManager$findSelectAid(JNIEnv *jni, jobject _this, jbyteArray byteArr) {
    return jni->NewStringUTF("F0010203040506");
}


void hookJava(JNIEnv *jni, jclass _class) {
    // hook into the findSelectAid method in the HostEmulationManager
    jmethodID method = jni->GetMethodID(_class, "findSelectAid", "([B)Ljava/lang/String;");
    if (method != NULL) {
        LOGI("captain hook");
        MSJavaHookMethod(jni, _class, method, (void *) &$HostEmulationManager$findSelectAid, NULL);
    } else {
        LOGE("nope");
        LOGI("Exception");
        jni->ExceptionDescribe();
        jni->ExceptionClear();
    }
}