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

#include "hook.h"
#include <sys/mman.h>
#include <cstring>
#include <dlfcn.h>

/**
 * find a native symbol and hook it
 */
void findAndHook(struct hook_t* eph, void* handle, const char *symbol, void* hookf, void **original) {
    *original = dlsym(handle, symbol);

    /*if(hook(eph, *original, hookf) != -1)
        log("HOOKNFC hooked: %s", symbol);*/
}

#ifdef __arm__
void inline hook_cacheflush(unsigned int begin, unsigned int end)
{
	const int syscall = 0xf0002;
	__asm __volatile (
		"mov	 r0, %0\n"			
		"mov	 r1, %1\n"
		"mov	 r7, %2\n"
		"movs    r2, #0x0\n"
		"svc     0x00000000\n"
		:
		:	"r" (begin), "r" (end), "r" (syscall)
		:	"r0", "r1", "r7"
		);
}

int hook(struct hook_t *h, void *addr_ptr, void *hookf_ptr)
{
	int i;
	unsigned int addr = (unsigned int)addr_ptr, hookf = (unsigned int)hookf_ptr;
	
	log("HOOKNFC: addr  = %p\n", addr_ptr);
	log("HOOKNFC: hookf = %p\n", hookf_ptr);

	if ((addr % 4 == 0 && hookf % 4 != 0) || (addr % 4 != 0 && hookf % 4 == 0)) {
		log("HOOKNFC: addr %p and hook %p\n don't match!\n", addr_ptr, hookf_ptr);
		return -1;
	}

    // change the property of current page to writeable

    unsigned int page_size = sysconf(_SC_PAGESIZE);
    unsigned int entry_page_start = ~((page_size) - 1) & (addr);
    if(mprotect((void*)entry_page_start, page_size, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
    	// log("mprotect: %u", errno);
    	return -1;
    }

	if (addr % 4 == 0) {
		log("ARM\n");

		h->thumb = false;
		h->patch = hookf;
		h->orig = addr;

		log("orig = %x\n", h->orig);

		h->jump.arm[0] = 0xe59ff000; // LDR pc, [pc, #0]
		h->jump.arm[1] = h->patch;
		h->jump.arm[2] = h->patch;

		for (i = 0; i < 3; i++)
			h->store.arm[i] = ((int*)h->orig)[i];
		for (i = 0; i < 3; i++)
			((int*)h->orig)[i] = h->jump.arm[i];
	}
	else {
		log("THUMB\n");

		h->thumb = true;
		h->patch = hookf;
		h->orig = addr;

		h->jump.thumb[1] = 0xb4;
		h->jump.thumb[0] = 0x60; // push {r5,r6}
		h->jump.thumb[3] = 0xa5;
		h->jump.thumb[2] = 0x03; // add r5, pc, #12
		h->jump.thumb[5] = 0x68;
		h->jump.thumb[4] = 0x2d; // ldr r5, [r5]
		h->jump.thumb[7] = 0xb0;
		h->jump.thumb[6] = 0x02; // add sp,sp,#8
		h->jump.thumb[9] = 0xb4;
		h->jump.thumb[8] = 0x20; // push {r5}
		h->jump.thumb[11] = 0xb0;
		h->jump.thumb[10] = 0x81; // sub sp,sp,#4
		h->jump.thumb[13] = 0xbd;
		h->jump.thumb[12] = 0x20; // pop {r5, pc}
		h->jump.thumb[15] = 0x46;
		h->jump.thumb[14] = 0xaf; // mov pc, r5 ; just to pad to 4 byte boundary

		memcpy(&h->jump.thumb[16], (unsigned char*)&h->patch, sizeof(unsigned int));

		unsigned int orig = addr - 1; // sub 1 to get real address

		for (i = 0; i < 20; i++)
			h->store.thumb[i] = ((unsigned char*)orig)[i];
		for (i = 0; i < 20; i++)
			((unsigned char*)orig)[i] = h->jump.thumb[i];
	}

	hook_cacheflush(h->orig, h->orig + sizeof(h->jump.thumb));
	return 1;
}

void hook_precall(struct hook_t *h)
{
	int i;
	
	if (h->thumb) {
		unsigned int orig = h->orig - 1;
		for (i = 0; i < 20; i++) {
			((unsigned char*)orig)[i] = h->store.thumb[i];
		}
	}
	else {
		for (i = 0; i < 3; i++)
			((int*)h->orig)[i] = h->store.arm[i];
	}

	hook_cacheflush(h->orig, h->orig + sizeof(h->jump.thumb));
}

void hook_postcall(struct hook_t *h)
{
	int i;

	if (h->thumb) {
		unsigned int orig = h->orig - 1;
		for (i = 0; i < 20; i++)
			((unsigned char*)orig)[i] = h->jump.thumb[i];
	}
	else {
		for (i = 0; i < 3; i++)
			((int*)h->orig)[i] = h->jump.arm[i];
	}

	hook_cacheflush(h->orig, h->orig + sizeof(h->jump.thumb));
}

void unhook(struct hook_t *h)
{
	log("unhooking %x , hook = %x ", h->orig, h->patch)
	hook_precall(h);
}

#else

void inline hook_cacheflush(unsigned long int begin, unsigned long int end)
{
    __builtin___clear_cache((char*)begin, (char*)end);
}

int hook(struct hook_t *h, void *addr_ptr, void *hookf_ptr)
{
    int i;
    unsigned long int addr = (unsigned long int) addr_ptr;
    unsigned long int hookf = (unsigned long int)hookf_ptr;

    log("HOOKNFC: addr  = %p\n", addr_ptr);
    log("HOOKNFC: hookf = %p\n", hookf_ptr);

    if (addr % 4 == 0) {
        log("ARM64\n");

        h->patch = hookf;
        h->orig = addr;

        h->jump.arm64[0] = 0xd10083ff; // 				sub     sp, sp, #0x20
        h->jump.arm64[1] = 0xa9017bfd; // 				stp     x29, x30, [sp, #0x10]
        h->jump.arm64[2] = 0xa90023e7; //					stp     x7, x8, [sp]
        h->jump.arm64[3] = 0x94000001; //					bl      label1
        h->jump.arm64[4] = 0xaa1e03e7; // label1:	mov     x7, x30
        h->jump.arm64[5] = 0xf841c0e8; // 				ldr     x8, [x7, #28]
        h->jump.arm64[6] = 0xd63f0100; //					blr     x8
        h->jump.arm64[7] = 0xa94023e7; //					ldp     x7, x8, [sp]
        h->jump.arm64[8] = 0xa9417bfd; // 				ldp     x29, x30, [sp, #0x10]
        h->jump.arm64[9] = 0x910083ff; // 				add     sp, sp, #0x20
        h->jump.arm64[10] = 0xd65f03c0; //				ret
        h->jump.arm64[11] = h->patch & 0xffffffff; //store patch address
        h->jump.arm64[12] = (h->patch >> 32) & 0xffffffff;

        for (i = 0; i < 13; i++)
            h->store.arm64[i] = ((int*)h->orig)[i];

        for (i = 0; i < 13; i++)
            ((int*)h->orig)[i] = h->jump.arm64[i];
    }

    hook_cacheflush(h->orig, h->orig + sizeof(h->jump.arm64));
    return 1;
}

void hook_precall(struct hook_t *h)
{
    for (int i = 0; i < 13; i++)
        ((int*)h->orig)[i] = h->store.arm64[i];

    hook_cacheflush(h->orig, h->orig + sizeof(h->jump.arm64));
}

void hook_postcall(struct hook_t *h)
{
    for (int i = 0; i < 13; i++)
        ((int*)h->orig)[i] = h->jump.arm64[i];

    hook_cacheflush(h->orig, h->orig+sizeof(h->jump.arm64));
}

void unhook(struct hook_t *h)
{
    log("unhooking %lx , hook = %lx \n", h->orig, h->patch)
    hook_precall(h);
}

#endif
