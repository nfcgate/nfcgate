#include <dlfcn.h>
#include <unistd.h>

#include <nfcd/nfcd.h>
#include <nfcd/hook/hook.h>
#include <nfcd/helper/Symbol.h>

bool patchEnabled = false;
struct hook_t hook_config;
struct hook_t hook_rfcback;
struct hook_t hook_senddata;
struct hook_t hook_deactivate;
struct hook_t hook_nfa_stop_rf_discovery;
struct hook_t hook_nfa_disable_polling;
struct hook_t hook_nfa_start_rf_discovery;
struct hook_t hook_nfa_enable_polling;

SymbolTable *SymbolTable::mInstance;
static void hookNative(const char *);
static void onLoad() __attribute__((constructor));

void onLoad() {
    LOGI("onLoad");

#ifdef __aarch64__
    LOGI("ARM64 detected!");
    const char *path = "/system/lib64/libnfc-nci.so";
#elif __arm__
    LOGI("ARM detected!");
    const char *path = "/system/lib/libnfc-nci.so";
#endif

    // check if NCI library exists and is readable
    if (access(path, R_OK) != 0) {
        LOGE("libnfc-nci library does not exist. Exiting.");
        return;
    }

    // create symbol mapping
    SymbolTable::create(path);

    // hook methods
    hookNative(path);
}

/**
 * hook into native functions of the libnfc-nci nfc driver
 */
static void hookNative(const char *libpath) {
    // library should be already loaded -> get handle
    void *handle = dlopen(libpath, RTLD_NOLOAD);
    LOGI("Handle is %p", handle);

    hook_symbol(&hook_config, handle, "NFC_SetConfig", (void *) &hook_NfcSetConfig,
                (void **) &nci_orig_NfcSetConfig);
    hook_symbol(&hook_rfcback, handle, "NFC_SetStaticRfCback", (void *) &hook_SetRfCback,
                (void **) &nci_orig_SetRfCback);

    hook_symbol(&hook_senddata, handle, "NFC_SendData", (void *) &hook_NfcSenddata,
                (void **) &nfc_orig_sendData);
    hook_symbol(&hook_deactivate, handle, "NFC_Deactivate", (void *) &hook_NfcDeactivate,
                (void **) &nfc_orig_deactivate);

    hook_symbol(&hook_nfa_stop_rf_discovery, handle, "NFA_StopRfDiscovery",
                (void *) &hook_NfaStopRfDiscovery, (void **) &nfa_orig_stop_rf_discovery);
    hook_symbol(&hook_nfa_disable_polling, handle, "NFA_DisablePolling",
                (void *) &hook_NfaDisablePolling, (void **) &nfa_orig_disable_polling);
    hook_symbol(&hook_nfa_start_rf_discovery, handle, "NFA_StartRfDiscovery",
                (void *) &hook_NfaStartRfDiscovery, (void **) &nfa_orig_start_rf_discovery);
    hook_symbol(&hook_nfa_enable_polling, handle, "NFA_EnablePolling",
                (void *) &hook_NfaEnablePolling, (void **) &nfa_orig_enable_polling);

    // find pointer to ce_t4t control structure
    ce_cb = (tCE_CB*)dlsym(handle, "ce_cb");
}