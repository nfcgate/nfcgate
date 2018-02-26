/*
 * All external files were taken from google's libnfc-nci source at
 * https://android.googlesource.com/platform/external/libnfc-nci/
 * from commit id 9d803cdb037118044011e200e065999199e4aafc (master at the time)
 * and modified as little as possible to allow for an easy update in the future.
 *
 * Every change to the files has been either
 * a) whitespace change
 * b) addition of a
 *    #if 0
 *    [#else
 *    ...]
 *    #endif
 */

#include <nfcd/external/nfa_api.h>
#include <nfcd/external/ce_int.h>
