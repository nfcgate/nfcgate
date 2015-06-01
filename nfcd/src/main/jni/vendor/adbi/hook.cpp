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
#include <cstring>
#include <sys/mman.h>
#include <unistd.h>


void inline hook_cacheflush(unsigned int begin, unsigned int end)
{	
	const int syscall = 0xf0002;
	__asm __volatile (
		"mov	 r0, %0\n"			
		"mov	 r1, %1\n"
		"mov	 r7, %2\n"
		"mov     r2, #0x0\n"
		"svc     0x00000000\n"
		:
		:	"r" (begin), "r" (end), "r" (syscall)
		:	"r0", "r1", "r7"
		);
}

int hook(struct hook_t *h, unsigned int addr, void *hookf)
{
	int i;
	
	log("addr  = %x\n", (unsigned int)addr)
	log("hookf = %x\n", (unsigned int)hookf)

	if ((addr % 4 == 0 && (unsigned int)hookf % 4 != 0) || (addr % 4 != 0 && (unsigned int)hookf % 4 == 0)) {
		log("addr 0x%x and hook 0x%x\n don't match!\n", (unsigned int)addr, (unsigned int)hookf);
		return -1;
	}

    // change the property of current page to writeable

    unsigned int page_size = sysconf(_SC_PAGESIZE);
    unsigned int entry_page_start = ~((page_size) - 1) & (addr);
    if(mprotect((void*)entry_page_start, page_size, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
    	log("mprotect: %u", errno);
    	return -1;
    }

	if (addr % 4 == 0) {
		log("ARM\n")
		h->thumb = 0;
		h->patch = (unsigned int)hookf;
		h->orig = addr;
		log("orig = %x\n", h->orig)
		h->jump[0] = 0xe59ff000; // LDR pc, [pc, #0]
		h->jump[1] = h->patch;
		h->jump[2] = h->patch;
		for (i = 0; i < 3; i++)
			h->store[i] = ((int*)h->orig)[i];
		for (i = 0; i < 3; i++)
			((int*)h->orig)[i] = h->jump[i];
	}
	else {
		log("THUMB")
		h->thumb = 1;
		h->patch = (unsigned int)hookf;
		h->orig = addr;
		h->jumpt[1] = 0xb4;
		h->jumpt[0] = 0x60; // push {r5,r6}
		h->jumpt[3] = 0xa5;
		h->jumpt[2] = 0x03; // add r5, pc, #12
		h->jumpt[5] = 0x68;
		h->jumpt[4] = 0x2d; // ldr r5, [r5]
		h->jumpt[7] = 0xb0;
		h->jumpt[6] = 0x02; // add sp,sp,#8
		h->jumpt[9] = 0xb4;
		h->jumpt[8] = 0x20; // push {r5}
		h->jumpt[11] = 0xb0;
		h->jumpt[10] = 0x81; // sub sp,sp,#4
		h->jumpt[13] = 0xbd;
		h->jumpt[12] = 0x20; // pop {r5, pc}
		h->jumpt[15] = 0x46;
		h->jumpt[14] = 0xaf; // mov pc, r5 ; just to pad to 4 byte boundary
		memcpy(&h->jumpt[16], (unsigned char*)&h->patch, sizeof(unsigned int));
		unsigned int orig = addr - 1; // sub 1 to get real address
		for (i = 0; i < 20; i++)
			h->storet[i] = ((unsigned char*)orig)[i];
		for (i = 0; i < 20; i++)
			((unsigned char*)orig)[i] = h->jumpt[i];
	}
	hook_cacheflush((unsigned int)h->orig, (unsigned int)h->orig+sizeof(h->jumpt));
	return 1;
}

void hook_precall(struct hook_t *h)
{
	int i;
	
	if (h->thumb) {
		unsigned int orig = h->orig - 1;
		for (i = 0; i < 20; i++) {
			((unsigned char*)orig)[i] = h->storet[i];
		}
	}
	else {
		for (i = 0; i < 3; i++)
			((int*)h->orig)[i] = h->store[i];
	}	
	hook_cacheflush((unsigned int)h->orig, (unsigned int)h->orig+sizeof(h->jumpt));
}

void hook_postcall(struct hook_t *h)
{
	int i;

	if (h->thumb) {
		unsigned int orig = h->orig - 1;
		for (i = 0; i < 20; i++)
			((unsigned char*)orig)[i] = h->jumpt[i];
	}
	else {
		for (i = 0; i < 3; i++)
			((int*)h->orig)[i] = h->jump[i];
	}
	hook_cacheflush((unsigned int)h->orig, (unsigned int)h->orig+sizeof(h->jumpt));	
}

void unhook(struct hook_t *h)
{
	log("unhooking %s = %x  hook = %x ", h->name, h->orig, h->patch)
	hook_precall(h);
}
