#include <dlfcn.h>
#include <unistd.h>

#include <nfcd/libnfc-external.h>
#include <nfcd/helper/Config.h>
#include <nfcd/helper/Symbol.h>
#include <nfcd/hook/Hook.h>

#define NFCD_DEBUG true

// hook definitions
extern Config origValues, hookValues;
extern bool hookEnabled;
extern Hook *hNFC_SetConfig;
tNFC_STATUS hook_NFC_SetConfig(UINT8 tlv_size, UINT8 *p_param_tlvs);
tNFC_STATUS hook_NFC_Deactivate(UINT8 deactivate_type);
tNFA_STATUS hook_NFA_StopRfDiscovery();
tNFA_STATUS hook_NFA_DisablePolling();
tNFA_STATUS hook_NFA_StartRfDiscovery();
tNFA_STATUS hook_NFA_EnablePolling(tNFA_TECHNOLOGY_MASK poll_mask);

inline const char *libnfc_path() {
#ifdef __aarch64__
    LOGI("ARM64 detected!");
    return "/system/lib64/libnfc-nci.so";
#elif __arm__
    LOGI("ARM detected!");
    return "/system/lib/libnfc-nci.so";
#endif
}

inline void loghex(const char *desc, const uint8_t *data, const int len) {
    int strlen = len * 3 + 1;
    char *msg = (char *) malloc((size_t) strlen);
    msg[strlen - 1] = '\0';
    for (uint8_t i = 0; i < len; i++) {
        sprintf(msg + i * 3, " %02x", (unsigned int) *(data + i));
    }
    LOGI("%s%s",desc, msg);
    free(msg);
}
