#include <unistd.h>
#include <sys/mman.h>

#include <nfcd/nfcd.h>
#include <nfcd/hook/impl/ADBIHook.h>
#include <nfcd/hook/impl/arm64_cacheflush.h>

ADBIHook::ADBIHook(void *libraryHandle, const SymbolTable &symbolTable, const std::string &name, void *hookFn) :
        Hook(libraryHandle, symbolTable, name, hookFn) {

    // get symbol alignment
    mAlignment = symbolTable.getSize(mName);
    // construct trampoline for this architecture
    LOG_ASSERT_S(constructTrampoline(), return, "Trampoline construction failed");
    // unprotect the region
    LOG_ASSERT_S(unprotect(), return, "Unprotecting failed");
    // install the trampoline
    LOG_ASSERT_S(swapTrampoline(true), return, "Trampoline installation failed");

    // success
    mHooked = true;
}

void ADBIHook::preCall() {
    Hook::preCall();

    // uninstall trampoline while hook is running to avoid recursion
    if (mHooked)
        LOG_ASSERT(swapTrampoline(false), "PreCall uninstall failed");
}
void ADBIHook::postCall() {
    Hook::postCall();

    // install trampoline again when hook is finished
    if (mHooked)
        LOG_ASSERT(swapTrampoline(true), "PostCall install failed");
}

bool ADBIHook::constructTrampoline() {
    unsigned long symbol = (unsigned long) mAddress;
    unsigned long hook = (unsigned long) mHookFn;

    // arm addresses are on a 4 byte boundary, thumb on 3
    bool symbol_is_thumb = symbol % 4 != 0, hook_is_thumb = hook % 4 != 0;

#ifdef __arm__
    if (symbol_is_thumb && hook_is_thumb) {
        LOGI("Constructing THUMB trampoline");
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
        std::memcpy(&trampoline[16], &hook, sizeof(unsigned long));

        // store trampoline
        mTrampolineSize = sizeof(trampoline);
        std::memcpy(mTrampoline, trampoline, mTrampolineSize);
    } else if (!symbol_is_thumb && !hook_is_thumb) {
        LOGI("Constructing ARM trampoline");
        mIsThumb = false;

        unsigned int trampoline[3];
        trampoline[0] = 0xe59ff000; // LDR pc, [pc, #0]
        trampoline[1] = hook;
        trampoline[2] = hook;

        // store trampoline
        mTrampolineSize = sizeof(trampoline);
        std::memcpy(mTrampoline, trampoline, mTrampolineSize);
    } else {
        LOGE("Alignment mismatch of symbol %p and hook %p", mAddress, mHookFn);
        return false;
    }
#else
    if (!symbol_is_thumb && !hook_is_thumb) {
        LOGI("Constructing ARM64 trampoline for %s", mName.c_str());
        mIsThumb = false;

        unsigned int trampoline[4];
        trampoline[0] = 0x58000050; // ldr x16, #8  -> x16 is IP0, load hook address
        trampoline[1] = 0xD61F0200; // br  x16      -> branch to register without modifying any regs

        // insert hook address
        trampoline[2] = hook & 0xffffffff;
        trampoline[3] = (hook >> 32) & 0xffffffff;

        // store trampoline
        mTrampolineSize = sizeof(trampoline);
        std::memcpy(mTrampoline, trampoline, mTrampolineSize);
    } else {
        LOGE("Alignment mismatch of symbol %p and hook %p", mAddress, mHookFn);
        return false;
    }
#endif
    return true;
}

bool ADBIHook::swapTrampoline(bool install) {
    void *symbol = mAddress;

    // prevent writing trampoline in potentially smaller symbol
    if (mAlignment < mTrampolineSize) {
        LOGE("Trampoline size %lu larger than approximate symbol size %lu", mTrampolineSize, mAlignment);
        return false;
    }
    else if (mAlignment == 0)
        LOGW("Symbol has no approximate size, possible overwrite");

    /* adjust symbol to thumb boundary:
     *   symbol points to first instruction, but in thumb mode (operand - instruction - operand)
     *   a operand precedes the first instruction. In order to overwrite the operand,
     *   subtract a byte from symbol so it points to the first operand.
     */
    if (mIsThumb)
        symbol = (void *)((unsigned long) symbol - 1);

    // store original symbol bytes
    if (install)
        std::memcpy(mStored, symbol, mTrampolineSize);

    // install/restore trampoline bytes
    std::memcpy(symbol, install ? mTrampoline : mStored, mTrampolineSize);
    // flush cache in symbol
    return hookCacheFlush();
}

bool ADBIHook::hookCacheFlush() {
    unsigned long begin = (unsigned long) mAddress;
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
    return true;
}

bool ADBIHook::unprotect() {
    int RWX = PROT_READ | PROT_WRITE | PROT_EXEC;
    unsigned long page_size = sysconf(_SC_PAGESIZE);

    unsigned long first_page = ~(page_size - 1) & (unsigned long) mAddress;
    unsigned long last_page = ~(page_size - 1) & ((unsigned long) mAddress + mTrampolineSize);

    if (mprotect((void *) first_page, page_size + (last_page - first_page), RWX) != 0) {
        LOGE("Error unprotecting %p - %p: %d", (void*)first_page, (void*)last_page, errno);
        return false;
    }
    return true;
}
