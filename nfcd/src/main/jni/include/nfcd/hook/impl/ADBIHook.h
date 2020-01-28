#ifndef NFCD_ADBIHOOK_H
#define NFCD_ADBIHOOK_H

#include <nfcd/hook/IHook.h>

// maximum trampoline size
#define TR_MAX_SIZE 52

class ADBIHook : public IHook {
public:
    ADBIHook(const std::string &name, void *hook, void *libraryHandle);

    void precall() override;

    void postcall() override;

protected:
    void hook() override;

    bool constructTrampoline();

    bool swapTrampoline(bool install);

    bool hookCacheflush();

    bool unprotect();

    // trampoline bytes, original bytes
    uint8_t mTrampoline[TR_MAX_SIZE], mStored[TR_MAX_SIZE];
    // trampoline size, alignment size
    unsigned long mTrampolineSize = 0, mAlignment = 0;
    // thumb mode (unused for arm64)
    bool mIsThumb = false;
};

#endif //NFCD_ADBIHOOK_H
