/*
 *  Collin's Binary Instrumentation Tool/Framework for Android
 *  Collin Mulliner <collin[at]mulliner.org>
 *
 *  (c) 2012,2013
 *
 *  License: LGPL v2.1
 *
 */

#include <android/log.h>
#include <stdint.h>

#define log(...) __android_log_print(ANDROID_LOG_DEBUG, "ADBI", __VA_ARGS__)

// maximum trampoline size
#define TR_MAX_SIZE 52

struct hook_t {
    // trampoline bytes
    uint8_t jump[TR_MAX_SIZE];
    // original bytes
    uint8_t store[TR_MAX_SIZE];
    // length of used bytes in jump and store
    size_t size;
    // original symbol, hook symbol
    void *orig, *hook;
    // thumb mode (unused for arm64)
    bool thumb;
};

typedef unsigned long ulong;

void hook_symbol(struct hook_t *eph, void *handle, const char *symbol, void *hookf, void **original);
void hook_precall(struct hook_t *h);
void hook_postcall(struct hook_t *h);
void unhook(struct hook_t *h);

/* internal methods */
int hook(struct hook_t *h, void *addr, void *hookf);
void hook_cacheflush(void *begin, size_t size);
bool construct_trampoline(struct hook_t *h);
