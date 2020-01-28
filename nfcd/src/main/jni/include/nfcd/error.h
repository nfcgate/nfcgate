#ifndef NFCD_ERROR_H
#define NFCD_ERROR_H

#include <errno.h>
#include <android/log.h>

#define LOG_TAG "NATIVENFC"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__ )
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__ )
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__ )
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// assert x, print to error log on error
#define LOG_ASSERT(x, ...) if (!(x)) { LOGE(__VA_ARGS__); }
// assert x, print to error log, return on error
#define LOG_ASSERT_X(x, ...) if (!(x)) { LOGE(__VA_ARGS__); return; }
#define LOG_ASSERT_XR(x, r, ...) if (!(x)) { LOGE(__VA_ARGS__); return r; }

#endif // NFCD_ERROR_H
