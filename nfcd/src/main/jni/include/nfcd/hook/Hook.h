#ifndef NFCD_HOOK_H
#define NFCD_HOOK_H

#include <cstdint>
#include <functional>
#include <android/log.h>

// maximum trampoline size
#define TR_MAX_SIZE 52

#define LOG_TAG "NATIVENFC"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__ )
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__ )
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__ )
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGEX(...) do { LOGE(__VA_ARGS__); return; } while(0)

class Hook {
public:
    Hook(void *handle, const char *symbol, void *redirect);

    void precall();

    void postcall();

    template <typename T>
    T* call() {
        return (T*)(mSymbol);
    }

private:
    void constructTrampoline();

    void swapTrampoline(bool install);

    void hookCacheflush();

    void unprotect();

    // symbol address, hook address
    void *mSymbol = nullptr, *mHook = nullptr;
    // trampoline bytes, original bytes
    uint8_t mTrampoline[TR_MAX_SIZE], mStored[TR_MAX_SIZE];
    // trampoline size, alignment size
    unsigned long mTrampolineSize = 0, mAlignment = 0;
    // thumb mode (unused for arm64)
    bool mIsThumb = false;
};

#endif //NFCD_HOOK_H
