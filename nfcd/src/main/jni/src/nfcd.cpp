#include <nfcd/nfcd.h>

static void hookNative() __attribute__((constructor));
SymbolTable *SymbolTable::mInstance;
Config origValues, patchValues;
bool patchEnabled = false;
Hook *hNFC_SetConfig;
Hook *hNFC_Deactivate;
Hook *hNFA_StopRfDiscovery;
Hook *hNFA_DisablePolling;
Hook *hNFA_StartRfDiscovery;
Hook *hNFA_EnablePolling;

/**
 * Prevent already set values from being overwritten.
 * Save original values to reset them when disabling patch.
 */
tNFC_STATUS hook_NFC_SetConfig(uint8_t size, uint8_t *tlv) {
    hNFC_SetConfig->precall();

    loghex("NfcSetConfig IN", tlv, size);
    LOGD("NfcSetConfig Enabled: %d", patchEnabled);

    Config cfg, actual;
    cfg.parse(size, tlv);

    for (auto &opt : cfg.options()) {
        // if this option would override one of the patch options, prevent it
        bool preventMe = false;

        for (auto &patch_opt : patchValues.options())
            if (patch_opt.type() == opt.type())
                preventMe = true;

        if (!preventMe)
            actual.add(opt);
        else
            // keep for restore
            origValues.add(opt);
    }

    // any of our values got modified and we are active those values are already changed in stream
    config_ref bin_stream;
    actual.build(bin_stream);
    loghex("NfcSetConfig OUT", bin_stream.get(), actual.total());
    tNFC_STATUS r = hNFC_SetConfig->call<decltype(hook_NFC_SetConfig)>()(actual.total(), bin_stream.get());

    hNFC_SetConfig->postcall();
    return r;
}

tNFC_STATUS hook_NFC_Deactivate(UINT8 deactivate_type) {
    hNFC_Deactivate->precall();

    LOGD("hook_NFC_Deactivate(%d)", deactivate_type);
    tNFC_STATUS r = hNFC_Deactivate->call<decltype(hook_NFC_Deactivate)>()(deactivate_type);

    hNFC_Deactivate->postcall();
    return r;
}

tNFA_STATUS hook_NFA_StopRfDiscovery(void) {
    hNFA_StopRfDiscovery->precall();

    LOGD("hook_NFA_StopRfDiscovery()");
    tNFA_STATUS r = hNFA_StopRfDiscovery->call<decltype(hook_NFA_StopRfDiscovery)>()();

    hNFA_StopRfDiscovery->postcall();
    return r;
}

tNFA_STATUS hook_NFA_DisablePolling(void) {
    hNFA_DisablePolling->precall();

    LOGD("hook_NFA_DisablePolling()");
    tNFA_STATUS r = hNFA_DisablePolling->call<decltype(hook_NFA_DisablePolling)>()();

    hNFA_DisablePolling->postcall();
    return r;
}

tNFA_STATUS hook_NFA_StartRfDiscovery() {
    hNFA_StartRfDiscovery->precall();

    LOGD("hook_NFA_StartRfDiscovery()");
    tNFA_STATUS r = hNFA_StartRfDiscovery->call<decltype(hook_NFA_StartRfDiscovery)>()();

    hNFA_StartRfDiscovery->postcall();
    return r;
}

tNFA_STATUS hook_NFA_EnablePolling(tNFA_TECHNOLOGY_MASK poll_mask) {
    hNFA_EnablePolling->precall();

    LOGD("hook_NFA_EnablePolling(0x%x)", poll_mask);
    tNFA_STATUS r = hNFA_EnablePolling->call<decltype(hook_NFA_EnablePolling)>()(poll_mask);

    hNFA_EnablePolling->postcall();
    return r;
}

static void hookNative() {
    const char *lib_path = libnfc_path();
    void *handle = dlopen(lib_path, RTLD_NOLOAD);

    // check if NCI library exists and is readable + is loaded
    if (access(lib_path, R_OK) != 0)
        LOGEX("Could not find libnfc-nci");
    if (!handle)
        LOGEX("Could not obtain handle of libnfc-nci");

    // create symbol mapping
    SymbolTable::create(lib_path);

    hNFC_SetConfig = new Hook(handle, "NFC_SetConfig", (void *)&hook_NFC_SetConfig);
#if NFCD_DEBUG
    hNFC_Deactivate = new Hook(handle, "NFC_Deactivate", (void *)&hook_NFC_Deactivate);
    hNFA_StopRfDiscovery = new Hook(handle, "NFA_StopRfDiscovery", (void *)&hook_NFA_StopRfDiscovery);
    hNFA_DisablePolling = new Hook(handle, "NFA_DisablePolling", (void *)&hook_NFA_DisablePolling);
    hNFA_StartRfDiscovery = new Hook(handle, "NFA_StartRfDiscovery", (void *)&hook_NFA_StartRfDiscovery);
    hNFA_EnablePolling = new Hook(handle, "NFA_EnablePolling", (void *)&hook_NFA_EnablePolling);
#else
    hNFC_Deactivate = new Hook(handle, "NFC_Deactivate", nullptr);
    hNFA_StopRfDiscovery = new Hook(handle, "NFA_StopRfDiscovery", nullptr);
    hNFA_DisablePolling = new Hook(handle, "NFA_DisablePolling", nullptr);
    hNFA_StartRfDiscovery = new Hook(handle, "NFA_StartRfDiscovery", nullptr);
    hNFA_EnablePolling = new Hook(handle, "NFA_EnablePolling", nullptr);
#endif
}