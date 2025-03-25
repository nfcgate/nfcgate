#ifndef NFCD_ERROR_H
#define NFCD_ERROR_H

#include <cstdlib>
#include <errno.h>
#include <android/log.h>

#define LOG_TAG "NATIVENFC"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__ )
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__ )
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__ )
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define _LOG_PRINT(...) LOGE("Error at %s:%d", __FILE__, __LINE__); LOGE(__VA_ARGS__)

// assert x, return on error
#define ASSERT_X(x) do { if (!(x)) { return; } } while(0)
#define ASSERT_S(x, s) do { if (!(x)) { s; } } while(0)
// assert x, print to error log, execute statement
#define LOG_ASSERT_S(x, s, ...) do { if (!(x)) { _LOG_PRINT(__VA_ARGS__); s; } } while(0)
#define LOG_ASSERT(x, ...) LOG_ASSERT_S(x, /*unused*/, __VA_ARGS__)

inline void loghex(const char *desc, const uint8_t *data, const int len) {
    int strlen = len * 3 + 1;
    char *msg = (char *) malloc((size_t) strlen);
    msg[strlen - 1] = '\0';
    for (uint8_t i = 0; i < len; i++) {
        sprintf(msg + i * 3, " %02x", (unsigned int) *(data + i));
    }
    LOGI("%s%s",desc, msg);
    free(msg);
}

#endif // NFCD_ERROR_H
