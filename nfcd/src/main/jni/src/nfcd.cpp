#include <nfcd/nfcd.h>
#include <dlfcn.h>
#include <sstream>

static void hookNative() __attribute__((constructor));
SymbolTable *SymbolTable::mInstance;
EventQueue EventQueue::mInstance;
Config origValues, hookValues;
def_NFA_CONN_CBACK *origNfaConnCBack;
bool hookEnabled = false;
bool patchEnabled = false;
bool guardConfig = true;
IHook *hNFC_SetConfig;
IHook *hce_select_t4t;
Symbol *nfa_dm_cb;
Symbol *hce_cb;
Symbol *hNFA_StopRfDiscovery;
Symbol *hNFA_DisablePolling;
Symbol *hNFA_StartRfDiscovery;
Symbol *hNFA_EnablePolling;

/**
 * Prevent already set values from being overwritten.
 * Save original values to reset them when disabling hook.
 */
tNFC_STATUS hook_NFC_SetConfig(uint8_t tlv_size, uint8_t *p_param_tlvs) {
    hNFC_SetConfig->precall();

    loghex("NfcSetConfig IN", p_param_tlvs, tlv_size);
    LOGD("NfcSetConfig Enabled: %d", patchEnabled);

    Config cfg, actual;
    cfg.parse(tlv_size, p_param_tlvs);

    for (auto &opt : cfg.options()) {
        // if this option would override one of the hook options, prevent it
        bool preventMe = false;

        for (auto &hook_opt : hookValues.options())
            if (hook_opt.type() == opt.type())
                preventMe = true;

        if (!preventMe || !guardConfig)
            actual.add(opt);
        else
            // keep for restore
            origValues.add(opt);
    }

    // any of our values got modified and we are active those values are already changed in stream
    config_ref bin_stream;
    actual.build(bin_stream);
    loghex("NfcSetConfig OUT", bin_stream.get(), actual.total());
    tNFC_STATUS r = hNFC_SetConfig->call<def_NFC_SetConfig>(actual.total(), bin_stream.get());

    hNFC_SetConfig->postcall();
    return r;
}

tNFC_STATUS hook_ce_select_t4t() {
    hce_select_t4t->precall();

    LOGD("hook_ce_select_t4t()");
    LOGD("hook_ce_select_t4t Enabled: %d", patchEnabled);

    tNFC_STATUS r = hce_select_t4t->call<def_ce_select_t4t>();
    if (patchEnabled) {
        int offset = System::sdkInt() < System::O_1 ? CE_CB_STATUS_PRE_O : CE_CB_STATUS_POST_O;
        auto ce_cb_status = hce_cb->address<uint8_t>() + offset;
        // bypass ISO 7816 SELECT requirement for AID selection
        *ce_cb_status |= CE_T4T_STATUS_WILDCARD_AID_SELECTED;
    }

    hce_select_t4t->postcall();
    return r;
}

void hook_nfaConnectionCallback(uint8_t event, void *eventData) {
    if (eventData)
        LOGD("hook_NFA_Event: event %d with status %d", event, *(uint8_t *)eventData);
    else
        LOGD("hook_NFA_Event: event %d without status", event);

    // call original callback
    origNfaConnCBack(event, eventData);
    // enqueue event
    EventQueue::instance().enqueue(event, eventData ? *(uint8_t*)eventData : 0);
}

bool hookNFA_CB() {
    auto **p_nfa_conn_cback = (def_NFA_CONN_CBACK**)(nfa_dm_cb->address<uint8_t>() +
            NFA_DM_CB_CONN_CBACK);
    LOG_ASSERT_XR(p_nfa_conn_cback && *p_nfa_conn_cback, false, "NFA_CB invalid");

    // save old nfa connection callback
    origNfaConnCBack = *p_nfa_conn_cback;
    // set new nfa connection callback
    *p_nfa_conn_cback = &hook_nfaConnectionCallback;

    return true;
}

static std::string escapeBRE(const std::string &in) {
    std::stringstream bruce;

    for (char c : in) {
        switch (c) {
            case '.':
            case '[':
            case ']':
            case '^':
            case '$':
            case '*':
            case '\\':
                bruce << "\\" << c;
                break;

            default:
                bruce << c;
                break;
        }
    }

    return bruce.str();
}

static std::string findLibNFC() {
    std::string bases[] = {
#ifdef __aarch64__
        "/system/lib64/",
        "/system/system_ext/lib64/",
#elif __arm__
        "/system/lib/",
        "/system/system_ext/lib/",
#endif
    };
    std::string names[] = {
        "libnfc-nci.so",
        "libnqnfc-nci.so",
        "libnxpnfc-nci.so",
    };

    for (auto &base : bases) {
        for (auto &name : names) {
            auto path = base + name;

            if (access(path.c_str(), R_OK) == 0)
                return path;
        }
    }

    return "";
}

static void hookNative() {
    // check if NCI library exists and is readable + is loaded
    auto libStr = findLibNFC();
    auto libReStr = "^" + escapeBRE(libStr) + "$";
    auto *lib = libStr.c_str();
    auto *lib_re = libReStr.c_str();

    LOG_ASSERT_X(!libStr.empty(), "Library not found or not accessible");
    LOGI("Library found at %s", lib);

    // try to obtain handle of already loaded library
    void *handle = dlopen(lib, RTLD_NOLOAD);
    LOG_ASSERT_X(handle, "Could not obtain library handle");

    // create symbol -> size mapping
    LOG_ASSERT_X(SymbolTable::create(lib), "Hooking failed");

    // begin installing hooks
    IHook::init();
    {
        // NFC config
        hNFC_SetConfig = IHook::hook("NFC_SetConfig", (void *) &hook_NFC_SetConfig, handle, lib_re);
        LOG_ASSERT_X(hNFC_SetConfig->isHooked(), "Hooking failed");

        // discovery
        hNFA_StartRfDiscovery = new Symbol("NFA_StartRfDiscovery", handle);
        hNFA_StopRfDiscovery = new Symbol("NFA_StopRfDiscovery", handle);
        LOG_ASSERT_X(hNFA_StartRfDiscovery->address<void>(), "Symbol lookup failed");
        LOG_ASSERT_X(hNFA_StopRfDiscovery->address<void>(), "Symbol lookup failed");

        // polling
        hNFA_EnablePolling = new Symbol("NFA_EnablePolling", handle);
        hNFA_DisablePolling = new Symbol("NFA_DisablePolling", handle);
        LOG_ASSERT_X(hNFA_EnablePolling->address<void>(), "Symbol lookup failed");
        LOG_ASSERT_X(hNFA_DisablePolling->address<void>(), "Symbol lookup failed");

        // NFC routing
        hce_select_t4t = IHook::hook("ce_select_t4t", (void *) &hook_ce_select_t4t, handle, lib_re);
        LOG_ASSERT_X(hce_select_t4t->isHooked(), "Hooking failed");
        hce_cb = new Symbol("ce_cb", handle);
        LOG_ASSERT_X(hce_cb->address<void>(), "Hooking failed");

        // NFA callback
        nfa_dm_cb = new Symbol("nfa_dm_cb", handle);
        LOG_ASSERT_X(nfa_dm_cb->address<void>(), "Hooking failed");
        LOG_ASSERT_X(hookNFA_CB(), "Hooking failed");
    }
    // finish installing hooks
    LOG_ASSERT_X(IHook::finish(), "Hooking failed");

    // hooking success
    hookEnabled = true;
}
