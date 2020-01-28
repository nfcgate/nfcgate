#include <dlfcn.h>
#include <unistd.h>

#include <nfcd/error.h>
#include <nfcd/helper/Config.h>
#include <nfcd/helper/SymbolTable.h>
#include <nfcd/helper/System.h>
#include <nfcd/hook/IHook.h>

// hook definitions
extern Config origValues, hookValues;
extern bool hookEnabled;
extern bool guardConfig;
extern IHook *hNFC_SetConfig;
extern IHook *hce_select_t4t;
extern Symbol *hce_cb;
extern Symbol *hNFC_Deactivate;
extern Symbol *hNFA_StopRfDiscovery;
extern Symbol *hNFA_DisablePolling;
extern Symbol *hNFA_StartRfDiscovery;
extern Symbol *hNFA_EnablePolling;

extern tNFC_STATUS hook_NFC_SetConfig(uint8_t tlv_size, uint8_t *p_param_tlvs);
extern tNFC_STATUS hook_ce_select_t4t (void);

using def_NFC_SetConfig = decltype(hook_NFC_SetConfig);
using def_NFC_Deactivate = tNFC_STATUS(uint8_t deactivate_type);
using def_NFA_StopRfDiscovery = tNFA_STATUS();
using def_NFA_DisablePolling = tNFA_STATUS();
using def_NFA_StartRfDiscovery = tNFA_STATUS();
using def_NFA_EnablePolling = tNFA_STATUS(tNFA_TECHNOLOGY_MASK poll_mask);
using def_ce_select_t4t = decltype(hook_ce_select_t4t);


inline const char *libnfc_path() {
#ifdef __aarch64__
    return "/system/lib64/libnfc-nci.so";
#elif __arm__
    return "/system/lib/libnfc-nci.so";
#endif
}
inline const char *libnfc_re() {
    return "^/system/lib.*/libnfc-nci\\.so$";
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
