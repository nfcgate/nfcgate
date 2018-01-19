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
#define log(...) __android_log_print(ANDROID_LOG_DEBUG, "ADBI", __VA_ARGS__);

struct hook_t {
	union {
		unsigned int arm[3];
		unsigned char thumb[20];
		unsigned int arm64[13];
	} jump;

	union {
		unsigned int arm[3];
		unsigned char thumb[20];
		unsigned int arm64[13];
	} store;

#ifdef __aarch64__
	unsigned long int orig, patch;
#else
    unsigned int orig, patch;
    bool thumb;
#endif
};

void hook_precall(struct hook_t *h);
void hook_postcall(struct hook_t *h);
int hook(struct hook_t *h, void *addr, void *hookf);
void unhook(struct hook_t *h);
