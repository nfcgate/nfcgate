#include <dlfcn.h>
#include <unistd.h>
#include <sys/mman.h>

#include <nfcd/hook/Hook.h>
#include <nfcd/hook/arm64_cacheflush.h>
#include <nfcd/helper/Symbol.h>

Hook::Hook(void *handle, const char *symbol, void *redirect) {
    // find symbol
    mSymbol = dlsym(handle, symbol);

    // if redirect enabled
    if (redirect) {
        // redirect to this symbol
        mHook = redirect;
        // get symbol alignment
        mAlignment = SymbolTable::instance()->getSize(symbol);
        // construct trampoline for this architecture
        constructTrampoline();
        // unprotect the region
        unprotect();
        // install the trampoline
        swapTrampoline(true);
    }
}

void Hook::precall() {
    // uninstall trampoline while hook is running to avoid recursion
    if (mHook)
        swapTrampoline(false);
}

void Hook::postcall() {
    // install trampoline again when hook is finished
    if (mHook)
        swapTrampoline(true);
}

void Hook::constructTrampoline() {
    unsigned long symbol = (unsigned long) mSymbol;
    unsigned long hook = (unsigned long) mHook;

    // arm addresses are on a 4 byte boundary, thumb on 3
    bool symbol_is_thumb = symbol % 4 != 0, hook_is_thumb = hook % 4 != 0;

#ifdef __arm__
    if (symbol_is_thumb && hook_is_thumb) {
        LOGI("THUMB");
        mIsThumb = true;

        unsigned char trampoline[20];

        trampoline[1] = 0xb4;
        trampoline[0] = 0x60; // push {r5,r6}
        trampoline[3] = 0xa5;
        trampoline[2] = 0x03; // add r5, pc, #12
        trampoline[5] = 0x68;
        trampoline[4] = 0x2d; // ldr r5, [r5]
        trampoline[7] = 0xb0;
        trampoline[6] = 0x02; // add sp,sp,#8
        trampoline[9] = 0xb4;
        trampoline[8] = 0x20; // push {r5}
        trampoline[11] = 0xb0;
        trampoline[10] = 0x81; // sub sp,sp,#4
        trampoline[13] = 0xbd;
        trampoline[12] = 0x20; // pop {r5, pc}
        trampoline[15] = 0x46;
        trampoline[14] = 0xaf; // mov pc, r5 ; just to pad to 4 byte boundary

        // insert hook address
        memcpy(&trampoline[16], &hook, sizeof(unsigned long));

        // store trampoline
        mTrampolineSize = sizeof(trampoline);
        memcpy(mTrampoline, trampoline, mTrampolineSize);
    } else if (!symbol_is_thumb && !hook_is_thumb) {
        LOGI("ARM");
        mIsThumb = false;

        unsigned int trampoline[3];

        trampoline[0] = 0xe59ff000; // LDR pc, [pc, #0]
        trampoline[1] = hook;
        trampoline[2] = hook;

        // store trampoline
        mTrampolineSize = sizeof(trampoline);
        memcpy(mTrampoline, trampoline, mTrampolineSize);
    } else
        LOGE("addr %p and hook %p don't match!", mSymbol, mHook);
#else
    if (!symbol_is_thumb && !hook_is_thumb) {
        LOGI("ARM64");
        mIsThumb = false;

        unsigned int trampoline[4];

        trampoline[0] = 0x58000050; // ldr x16, #8  -> x16 is IP0, load hook address
        trampoline[1] = 0xD61F0200; // br  x16      -> branch to register without modifying any regs

        // insert hook address
        trampoline[2] = hook & 0xffffffff;
        trampoline[3] = (hook >> 32) & 0xffffffff;

        // store trampoline
        mTrampolineSize = sizeof(trampoline);
        memcpy(mTrampoline, trampoline, mTrampolineSize);
    } else
        LOGE("addr %p and hook %p don't match!", mSymbol, mHook);
#endif
}

void Hook::swapTrampoline(bool install) {
    void *symbol = mSymbol;

    // prevent writing trampoline in potentially bigger symbol
    if (mAlignment < mTrampolineSize)
        LOGE("trampoline size larger than symbol alignment");
    else if (mAlignment == 0)
        LOGW("symbol has no alignment, overwrite possible");

    // adjust symbol to thumb boundary
    if (mIsThumb)
        symbol = (void *)((unsigned long) symbol - 1);

    // store symbol bytes
    if (install)
        std::memcpy(mStored, symbol, mTrampolineSize);

    // install/restore trampoline bytes
    std::memcpy(symbol, install ? mTrampoline : mStored, mTrampolineSize);
    // flush cache in symbol
    hookCacheflush();
}

void Hook::hookCacheflush() {
    unsigned long begin = (unsigned long) mSymbol;
    unsigned long end = begin + mTrampolineSize;

#ifdef __arm__
    const int syscall = 0xf0002;
    __asm __volatile (
    "mov	 r0, %0\n"
    "mov	 r1, %1\n"
    "mov	 r7, %2\n"
    "movs    r2, #0x0\n"
    "svc     0x00000000\n"
    :
    :    "r" (begin), "r" (end), "r" (syscall)
    :    "r0", "r1", "r7"
    );
#else
    arm64_cacheflush(begin, mTrampolineSize);
#endif
}

void Hook::unprotect() {
    int RWX = PROT_READ | PROT_WRITE | PROT_EXEC;
    unsigned long page_size = sysconf(_SC_PAGESIZE);

    unsigned long first_page = ~(page_size - 1) & (unsigned long) mSymbol;
    unsigned long last_page = ~(page_size - 1) & ((unsigned long) mSymbol + mTrampolineSize);

    if (mprotect((void *) first_page, page_size + (last_page - first_page), RWX) != 0)
        LOGE("Error unprotecting pages");
}
