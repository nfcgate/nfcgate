#ifndef NFCD_HOOK_H
#define NFCD_HOOK_H

#include <cstdint>
#include <functional>
#include <type_traits>

#include <nfcd/error.h>

// maximum trampoline size
#define TR_MAX_SIZE 52

class Hook {
public:
    Hook(void *handle, const char *symbol, void *redirect);

    void precall();

    void postcall();

    template <typename Fn, typename... Args>
    typename std::result_of<Fn*(Args...)>::type call(Args&&... args) {
        return ((Fn*)mSymbol)(std::forward<Args>(args)...);
    }

    template <typename Fn, typename... Args>
    typename std::result_of<Fn*(Args...)>::type callOther(Args&&... args) {
        precall();
        auto r = ((Fn*)mSymbol)(std::forward<Args>(args)...);
        postcall();
        return r;
    }

private:
    bool constructTrampoline();

    bool swapTrampoline(bool install);

    bool hookCacheflush();

    bool unprotect();

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
