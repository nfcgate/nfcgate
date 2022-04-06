#include <nfcd/nfcd.h>

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

void hookNFA_CB() {
    auto **p_nfa_conn_cback = (def_NFA_CONN_CBACK**)(nfa_dm_cb->address<uint8_t>() +
            NFA_DM_CB_CONN_CBACK);

    // save old nfa connection callback
    origNfaConnCBack = *p_nfa_conn_cback;
    // set new nfa connection callback
    *p_nfa_conn_cback = &hook_nfaConnectionCallback;
}

static void hookNative() {
    IHook::init();

    // check if NCI library exists and is readable + is loaded
    const char *lib_path = libnfc_path();
    LOGI("Library expected at %s", lib_path);
    LOG_ASSERT_X(access(lib_path, R_OK) == 0, "Library not accessible");

    void *handle = dlopen(lib_path, RTLD_NOLOAD);
    LOG_ASSERT_X(handle, "Could not obtain library handle");

    // create symbol mapping
    SymbolTable::create(lib_path);

    hNFC_SetConfig = IHook::hook("NFC_SetConfig", (void *) &hook_NFC_SetConfig, handle, libnfc_re());

    hNFA_StopRfDiscovery = new Symbol("NFA_StopRfDiscovery", handle);
    hNFA_DisablePolling = new Symbol("NFA_DisablePolling", handle);
    hNFA_StartRfDiscovery = new Symbol("NFA_StartRfDiscovery", handle);
    hNFA_EnablePolling = new Symbol("NFA_EnablePolling", handle);

    hce_select_t4t = IHook::hook("ce_select_t4t", (void *)&hook_ce_select_t4t, handle, libnfc_re());
    hce_cb = new Symbol("ce_cb", handle);

    nfa_dm_cb = new Symbol("nfa_dm_cb", handle);
    hookNFA_CB();

    hookEnabled = true;
    IHook::finish();
}
