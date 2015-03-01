
#include <string.h>
#include <jni.h>
#include <unistd.h>
#include <android/log.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include "../../../../nfcd/src/main/jni/ipc.h"

/**
 * native part of the hce.DaemonConfiguration class.
 * this code connects to the unix domain socket of the nfc damon patch
 * and transmitts/receives "ipcpacket"s
 */

#define LOG_TAG "NFCIPC-C"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__ )
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__ )

extern "C" {
    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_hce_DaemonConfiguration_enablePatch(JNIEnv* env, jobject javaThis);
    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_hce_DaemonConfiguration_disablePatch(JNIEnv* env, jobject javaThis);
    JNIEXPORT jboolean JNICALL Java_tud_seemuh_nfcgate_hce_DaemonConfiguration_isPatchEnabled(JNIEnv* env, jobject javaThis);
    JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_hce_DaemonConfiguration_uploadConfiguration(JNIEnv* env, jobject javaThis, jbyte atqa, jbyte sak, jbyte hist, jbyteArray uid);
}

void sendPacket(const ipcpacket p);
void recvPacket(ipcpacket *p);
int sock = 0;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    return JNI_VERSION_1_6;
}

/**
 * send an ENABLE packet to the nfcd
 */
JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_hce_DaemonConfiguration_enablePatch(JNIEnv* env, jobject javaThis) {
    LOGI("enablePatch");
    ipcpacket p;
    p.type = ipctype::ENABLE;
    sendPacket(p);
}

/**
 * send an DISABLE packet to the nfcd
 */
JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_hce_DaemonConfiguration_disablePatch(JNIEnv* env, jobject javaThis) {
    LOGI("disablePatch");
    ipcpacket p;
    p.type = ipctype::DISABLE;
    sendPacket(p);
}

/**
 * read the current patch status
 */
JNIEXPORT jboolean JNICALL Java_tud_seemuh_nfcgate_hce_DaemonConfiguration_isPatchEnabled(JNIEnv* env, jobject javaThis) {
    ipcpacket p;
    p.type = ipctype::STATUS;
    // request state and receive response
    sendPacket(p);
    recvPacket(&p);
    return p.type == ipctype::ENABLE;
}

/**
 * send the new chip configuration
 */
JNIEXPORT void JNICALL Java_tud_seemuh_nfcgate_hce_DaemonConfiguration_uploadConfiguration(JNIEnv* env, jobject javaThis, jbyte atqa, jbyte sak, jbyte hist, jbyteArray _uid) {
    LOGI("uploadConfiguration");
    jsize len = env->GetArrayLength(_uid);
    if(len > sizeof(ipcpacket::uid)) {
        jclass Exception = env->FindClass("java/lang/Exception");
        env->ThrowNew(Exception, "uid bigger than buffer");
    }
    // build an ipcpacket with all values and transmit it
    ipcpacket p;
    p.type = ipctype::CONFIGURE;
    p.atqa = atqa;
    p.sak = sak;
    p.hist = hist;
    p.uid_len = len;
    jbyte* uid = env->GetByteArrayElements(_uid, 0);
    memcpy(p.uid, uid, len);
    env->ReleaseByteArrayElements(_uid, uid, 0);
    sendPacket(p);
}

/**
 * connect to the nfc daemon
 */
void connect() {
    sock = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sock == -1) {
        LOGE("E socket: %s", strerror(errno));
        return;
    }

    struct sockaddr_un sa;
    sa.sun_family = AF_UNIX;
    strcpy(sa.sun_path, IPC_SOCK_FILE);
    int len = strlen(sa.sun_path) + sizeof(sa.sun_family);
    if (connect(sock, (struct sockaddr *)&sa, len) == -1) {
        LOGE("E connect: %s", strerror(errno));
    }
}

/**
 * send a packet to the nfcd. reconnect if necessary
 */
void sendPacket(const ipcpacket p) {
    if(socket == 0) connect();
    int error = 0;
    socklen_t len = sizeof (error);
    if(getsockopt (sock, SOL_SOCKET, SO_ERROR, &error, &len ) != 0) connect();

    if (send(sock, &p, sizeof(ipcpacket), 0) == -1) {
        LOGE("E send: %s", strerror(errno));
    }
}

/**
 * receive a packet from the nfcd.
 */
void recvPacket(ipcpacket *p) {
    recv(sock, p, sizeof(ipcpacket), 0);
}
