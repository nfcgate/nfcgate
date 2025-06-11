#ifndef NFCD_ADBIHOOK_H
#define NFCD_ADBIHOOK_H

#include <nfcd/hook/Hook.h>

// maximum trampoline size
#define TR_MAX_SIZE 52

class ADBIHook : public Hook {
public:
    ADBIHook(void *libraryHandle, const SymbolTable &symbolTable, const std::string &name, void *hookFn);

    void preCall() override;
    void postCall() override;

protected:
    bool constructTrampoline();
    bool swapTrampoline(bool install);
    bool hookCacheFlush();
    bool unprotect();

    // trampoline bytes, original bytes
    uint8_t mTrampoline[TR_MAX_SIZE] = {0}, mStored[TR_MAX_SIZE] = {0};
    // trampoline size, alignment size
    unsigned long mTrampolineSize = 0, mAlignment = 0;
    // thumb mode (unused for arm64)
    bool mIsThumb = false;
};

#endif //NFCD_ADBIHOOK_H
