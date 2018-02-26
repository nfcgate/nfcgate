/*
 *  Collin's Binary Instrumentation Tool/Framework for Android
 *  Collin Mulliner <collin[at]mulliner.org>
 *  http://www.mulliner.org/android/
 *
 *  (c) 2012,2013
 *
 *  License: LGPL v2.1
 *
 */

#include <dlfcn.h>
#include <cstring>
#include <unistd.h>
#include <sys/mman.h>

#include <nfcd/hook/hook.h>
#include <nfcd/hook/arm64_cacheflush.h>
#include <nfcd/helper/Symbol.h>

/**
 * find a native symbol and hook it
 */
void hook_symbol(struct hook_t *eph, void *handle, const char *symbol, void *hookf, void **original) {
    *original = dlsym(handle, symbol);

    // get symbol alignment
    eph->alignment = SymbolTable::instance()->getSize(symbol);

    // try to hook symbol
    if (hook(eph, *original, hookf) != -1)
        adbi_log("NATIVENFC hooked: %s", symbol);
    else
        adbi_log("NATIVENFC error hooking: %s", symbol);
}

void unprotect_region(void *target, size_t size) {
    ulong page_size = sysconf(_SC_PAGESIZE);

    ulong first_page = ~(page_size - 1) & (ulong) target;
    ulong last_page = ~(page_size - 1) & ((ulong) target + size);

    int ret = mprotect((void *) first_page, page_size + (last_page - first_page), PROT_READ | PROT_WRITE | PROT_EXEC);
    if (ret != 0)
        adbi_log("Error unprotecting pages %lu to %lu (failed with %d)", first_page, last_page, ret);
}

void swap_trampoline(struct hook_t *h, const void *trampoline, void *original, size_t size) {
    void *target = h->orig;

    if (h->thumb) {
        /*
         * target points to first instruction, but in thumb mode (operand - instruction - operand)
         * a operand precedes the first instruction. In order to overwrite the operand,
         * subtract a byte from target so it points to the first operand.
         */
        target = (void *) ((ulong) target - 1);
    }

    // store original bytes
    if (original != nullptr)
        memcpy(original, target, size);

    // overwrite with trampoline
    adbi_log("NATIVENFC: final destination: %p from %p with size %ul", target, trampoline, size);
    memcpy(target, trampoline, size);

    // flush cache in region
    hook_cacheflush(target, size);
}

int hook(struct hook_t *h, void *address, void *hook) {
    adbi_log("HOOKNFC: address = %p\n", address);
    adbi_log("HOOKNFC: hook = %p\n", hook);

    // save hook data
    h->orig = address;
    h->hook = hook;

    // architecture specific trampoline
    if (!construct_trampoline(h))
        return -1;

    // check trampoline size vs alignment
    if (h->alignment == 0)
        adbi_log("WARNING: symbol has no alignment, overwrite possible");
    else if(h->alignment < h->size) {
        adbi_log("ERROR: trampoline size larger than symbol alignment -> overwrite imminent");
        return -1;
    }

    // unprotect region and write trampoline, store original bytes
    unprotect_region(address, h->size);
    swap_trampoline(h, h->jump, h->store, h->size);
    return 1;
}

void hook_precall(struct hook_t *h) {
    // restore original bytes
    swap_trampoline(h, h->store, nullptr, h->size);
}

void hook_postcall(struct hook_t *h) {
    // restore trampoline bytes
    swap_trampoline(h, h->jump, nullptr, h->size);
}

void unhook(struct hook_t *h) {
    adbi_log("unhooking %p , hook = %p", h->orig, h->hook);
    hook_precall(h);
}

void hook_cacheflush(void *pbegin, size_t size) {
    ulong begin = (ulong) pbegin;
    ulong end = begin + size;
    void *pend = (void *) end;

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
    arm64_cacheflush(begin, size);
#endif
}

bool construct_trampoline(struct hook_t *h) {
    ulong addr = (ulong) h->orig;
    ulong hook = (ulong) h->hook;

    // arm addresses are on a 4 byte boundary, thumb on 3
    bool addr_is_thumb = addr % 4 != 0, hook_is_thumb = hook % 4 != 0;

#ifdef __arm__
    if (addr_is_thumb && hook_is_thumb) {
        adbi_log("THUMB\n");
        h->thumb = true;

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
        memcpy(&trampoline[16], &hook, sizeof(ulong));

        // store trampoline
        h->size = sizeof(trampoline);
        memcpy(h->jump, trampoline, h->size);
    } else if (!addr_is_thumb && !hook_is_thumb) {
        adbi_log("ARM\n");
        h->thumb = false;

        unsigned int trampoline[3];

        trampoline[0] = 0xe59ff000; // LDR pc, [pc, #0]
        trampoline[1] = hook;
        trampoline[2] = hook;

        // store trampoline
        h->size = sizeof(trampoline);
        memcpy(h->jump, trampoline, h->size);
    } else {
        adbi_log("HOOKNFC: addr %p and hook %p\n don't match!\n", h->orig, h->hook);
        return false;
    }
#else
    if (!addr_is_thumb && !hook_is_thumb) {
        adbi_log("ARM64\n");
        h->thumb = false;

        unsigned int trampoline[4];

        trampoline[0] = 0x58000050; // ldr x16, #8  -> x16 is IP0, load hook address
        trampoline[1] = 0xD61F0200; // br  x16      -> branch to register without modifying any regs

        // insert hook address
        trampoline[2] = hook & 0xffffffff;
        trampoline[3] = (hook >> 32) & 0xffffffff;

        // store trampoline
        h->size = sizeof(trampoline);
        memcpy(h->jump, trampoline, h->size);
    } else {
        adbi_log("HOOKNFC: addr %p and hook %p\n don't match!\n", h->orig, h->hook);
        return false;
    }
#endif
    return true;
}

