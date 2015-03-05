
#include "nfcd.h"


static jstring (*oldfindSelectAid)(JNIEnv *, jobject, jbyteArray);

/**
* Hook Java Code to overwrite findSelectAid() behavior
**/
static jstring $HostEmulationManager$findSelectAid(JNIEnv *jni, jobject _this, jbyteArray byteArr) {
    if(patchEnabled) {
        // F0010203040506 is a aid registered by the nfcgate hce service
        return jni->NewStringUTF("F0010203040506");
    }
    return oldfindSelectAid(jni, _this, byteArr);
}

/**
 * hook the java part: find the findSelectAid() method and replace the implementation with our own
 */
void hookJava(JNIEnv *jni, jclass _class) {
    // hook into the findSelectAid method in the HostEmulationManager
    jmethodID method = jni->GetMethodID(_class, "findSelectAid", "([B)Ljava/lang/String;");
    if (method != NULL) {
        LOGI("captain hook");
        MSJavaHookMethod(jni, _class, method, (void *) &$HostEmulationManager$findSelectAid, (void**)&oldfindSelectAid);
    } else {
        LOGI("Exception");
        jni->ExceptionDescribe();
        jni->ExceptionClear();
    }
}
