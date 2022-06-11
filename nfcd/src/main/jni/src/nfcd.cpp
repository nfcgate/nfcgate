#include <nfcd/nfcd.h>
#include <link.h>
#include <dlfcn.h>

#include <set>
#include <fstream>
#include <sstream>

__attribute__((unused)) static void hookNative() __attribute__((constructor));
std::unique_ptr<SymbolTable> SymbolTable::mInstance;
EventQueue EventQueue::mInstance;
Config hookValues;
def_NFA_CONN_CBACK *origNfaConnCBack;
bool hookEnabled = false;
bool patchEnabled = false;
bool guardEnabled = true;
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
 */
tNFC_STATUS hook_NFC_SetConfig(uint8_t tlv_size, uint8_t *p_param_tlvs) {
    hNFC_SetConfig->precall();

    LOGD("NFC_SetConfig()");

    Config cfg, actual;
    cfg.parse(tlv_size, p_param_tlvs);

    for (auto &opt : cfg.options()) {
        // indicates whether this option would override one of the hook options
        bool conflict = false;
        for (auto &hook_opt : hookValues.options())
            if (hook_opt.type() == opt.type())
                conflict = true;

        // log config values with type codes
        std::stringstream bruce;
        bruce << "NFC_SetConfig Option " << opt.name() << "(" << (int)opt.type() << "):";
        loghex(bruce.str().c_str(), opt.value(), opt.len());

        // prevent config values from overriding hook iff guard is enabled
        if (!guardEnabled || !conflict)
            actual.add(opt);
    }

    config_ref bin_stream;
    actual.build(bin_stream);
    tNFC_STATUS r = hNFC_SetConfig->call<def_NFC_SetConfig>(actual.total(), bin_stream.get());

    hNFC_SetConfig->postcall();
    return r;
}

tNFC_STATUS hook_ce_select_t4t() {
    hce_select_t4t->precall();

    LOGD("hook_ce_select_t4t()");
    LOGD("Patch enabled: %d", patchEnabled);

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
    auto eventName = System::nfaEventName(event);

    if (eventData)
        LOGD("hook_NFA_Event: %s(%d) with status %d", eventName.c_str(), event, *(uint8_t *)eventData);
    else
        LOGD("hook_NFA_Event: %s(%d)", eventName.c_str(), event);

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

static bool strContains(const std::string &s, const std::string &q) {
    return s.find(q) != std::string::npos;
}
static bool strStartsWith(const std::string &s, const std::string &q) {
    return q.size() < s.size() && std::equal(q.begin(), q.end(), s.begin());
}
static bool strEndsWith(const std::string &s, const std::string &q) {
    return q.size() < s.size() && std::equal(q.rbegin(), q.rend(), s.rbegin());
}

static std::set<std::string> getLoadedLibraries() {
    std::ifstream maps("/proc/self/maps");
    LOG_ASSERT_XR(maps.is_open(), {}, "Error loading proc maps");

    std::set<std::string> result;
    for (std::string line; std::getline(maps, line); ) {
        auto inx = line.find_last_of(' ');

        if (inx != std::string::npos) {
            auto name = line.substr(inx + 1);

            if (strEndsWith(name, ".so"))
                result.emplace(name);
        }
    }

    return result;
}

static std::string findLibNFC() {
    for (const auto &candidate : getLoadedLibraries()) {
        // library path must contain "nfc" somewhere
        if (!strContains(candidate, "nfc"))
            continue;

        // library file must be accessible
        if (access(candidate.c_str(), R_OK) != 0)
            continue;

        // library symbol table must contain the expected symbol
        if (SymbolTable::create(candidate) &&
                SymbolTable::instance()->contains("NFC_SetConfig"))
            return candidate;
    }

    return "";
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

static void hookNative() {
    // check if NCI library exists and is readable + is loaded
    auto lib = findLibNFC();
    auto libRe = "^" + escapeBRE(lib) + "$";

    LOG_ASSERT_X(!lib.empty(), "Library not found or not accessible");
    LOGI("Library found at %s", lib.c_str());

    // try to obtain handle of already loaded library
    void *handle = dlopen(lib.c_str(), RTLD_NOLOAD);
    LOG_ASSERT_X(handle, "Could not obtain library handle");

    // create symbol -> size mapping
    LOG_ASSERT_X(SymbolTable::create(lib), "Building symbol table failed");

    // begin installing hooks
    IHook::init();
    {
        // NFC config
        hNFC_SetConfig = IHook::hook("NFC_SetConfig", (void *) &hook_NFC_SetConfig, handle, libRe);
        LOG_ASSERT_X(hNFC_SetConfig->isHooked(), "Hooking NFC_SetConfig failed");

        // discovery
        hNFA_StartRfDiscovery = new Symbol("NFA_StartRfDiscovery", handle);
        hNFA_StopRfDiscovery = new Symbol("NFA_StopRfDiscovery", handle);
        LOG_ASSERT_X(hNFA_StartRfDiscovery->address<void>(), "Symbol lookup failed for NFA_StartRfDiscovery");
        LOG_ASSERT_X(hNFA_StopRfDiscovery->address<void>(), "Symbol lookup failed for NFA_StopRfDiscovery");

        // polling
        hNFA_EnablePolling = new Symbol("NFA_EnablePolling", handle);
        hNFA_DisablePolling = new Symbol("NFA_DisablePolling", handle);
        LOG_ASSERT_X(hNFA_EnablePolling->address<void>(), "Symbol lookup failed for NFA_EnablePolling");
        LOG_ASSERT_X(hNFA_DisablePolling->address<void>(), "Symbol lookup failed for NFA_DisablePolling");

        // NFC routing
        hce_select_t4t = IHook::hook("ce_select_t4t", (void *) &hook_ce_select_t4t, handle, libRe);
        LOG_ASSERT_X(hce_select_t4t->isHooked(), "Hooking ce_select_t4t failed");
        hce_cb = new Symbol("ce_cb", handle);
        LOG_ASSERT_X(hce_cb->address<void>(), "Symbol lookup failed for ce_cb");

        // NFA callback
        nfa_dm_cb = new Symbol("nfa_dm_cb", handle);
        LOG_ASSERT_X(nfa_dm_cb->address<void>(), "Symbol lookup failed for nfa_dm_cb");
        LOG_ASSERT_X(hookNFA_CB(), "Hooking nfa_cb failed");
    }
    // finish installing hooks
    LOG_ASSERT_X(IHook::finish(), "Hooking failed");

    // hooking success
    hookEnabled = true;
}
