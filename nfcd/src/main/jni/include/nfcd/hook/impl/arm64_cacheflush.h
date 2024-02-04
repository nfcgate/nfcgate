/**
 * Motivation for NOT using the builtin cacheflush:
 * http://www.mono-project.com/news/2016/09/12/arm64-icache/
 */

#ifndef __arm__
/**
 * Authors:
 *   Paolo Molaro (lupus@ximian.com)
 *   Dietmar Maurer (dietmar@ximian.com)
 *
 * (C) 2003 Ximian, Inc.
 * Copyright 2003-2011 Novell, Inc (http://www.novell.com)
 * Copyright 2011 Xamarin, Inc (http://www.xamarin.com)
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 * 
 * https://github.com/lewurm/mono/blob/44520cdd9c7eee06cb0b248c5da8177299674ba1/mono/mini/mini-arm64.c#L1773-L1809
 */
#define MIN(a, b) (((a) < (b)) ? (a) : (b))

void arm64_cacheflush(unsigned long p, size_t size)
{
    /* Don't rely on GCC's __clear_cache implementation, as it caches
     * icache/dcache cache line sizes, that can vary between cores on
     * big.LITTLE architectures. */
    unsigned long end = p + size;
    unsigned long addr, ctr_el0;
    static size_t icache_line_size = 0xffff, dcache_line_size = 0xffff;
    size_t isize, dsize;

    asm volatile ("mrs %0, ctr_el0" : "=r" (ctr_el0));
    isize = 4 << ((ctr_el0 >> 0 ) & 0xf);
    dsize = 4 << ((ctr_el0 >> 16) & 0xf);

    /* determine the global minimum cache line size */
    icache_line_size = isize = MIN (icache_line_size, isize);
    dcache_line_size = dsize = MIN (dcache_line_size, dsize);

    addr = p & ~(unsigned long) (dsize - 1);
    for (; addr < end; addr += dsize)
            asm volatile("dc civac, %0" : : "r" (addr) : "memory");
    asm volatile("dsb ish" : : : "memory");

    addr = p & ~(unsigned long) (isize - 1);
    for (; addr < end; addr += isize)
            asm volatile("ic ivau, %0" : : "r" (addr) : "memory");

    asm volatile ("dsb ish" : : : "memory");
    asm volatile ("isb" : : : "memory");
}
#endif
