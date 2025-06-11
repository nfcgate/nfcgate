#include <unistd.h>

#include <nfcd/error.h>
#include <nfcd/helper/Config.h>
#include <nfcd/helper/EEManager.h>
#include <nfcd/helper/EventQueue.h>
#include <nfcd/helper/LoadedLibrary.h>
#include <nfcd/helper/MapInfo.h>
#include <nfcd/helper/StringUtil.h>
#include <nfcd/helper/StructSizeProber.h>
#include <nfcd/helper/SymbolTable.h>
#include <nfcd/helper/System.h>
#include <nfcd/hook/Hook.h>

extern tNFC_STATUS hook_NFC_SetConfig(uint8_t tlv_size, uint8_t *p_param_tlvs);
extern tNFC_STATUS hook_NFC_DiscoveryStart(uint8_t num_params, tNCI_DISCOVER_PARAMS *p_params, void* p_cback);
extern tNFA_STATUS hook_NFA_Enable(void *p_dm_cback, void *p_conn_cback);
extern tNFC_STATUS hook_ce_select_t4t (void);

using def_NFC_SetConfig = decltype(hook_NFC_SetConfig);
using def_NFC_DiscoveryStart = decltype(hook_NFC_DiscoveryStart);
using def_NFA_StopRfDiscovery = tNFA_STATUS();
using def_NFA_DisablePolling = tNFA_STATUS();
using def_NFA_StartRfDiscovery = tNFA_STATUS();
using def_NFA_EnablePolling = tNFA_STATUS(tNFA_TECHNOLOGY_MASK poll_mask);
using def_NFA_EeModeSet = tNFA_STATUS(uint16_t ee_handle, uint8_t mode);
using def_NFA_EeGetInfo = tNFA_STATUS(uint8_t* p_num_nfcee, void * p_info);
using def_NFA_CONN_CBACK = void(uint8_t event, void *data);
using def_ce_select_t4t = decltype(hook_ce_select_t4t);

enum class HookResult : int {
    SUCCESS = 0,
    ERROR_RETRY = 1,
    ERROR_FATAL = 2,
    UNKNOWN = 3,
};
#define ASSERT_HOOK(x) ASSERT_S(x, return HookResult::ERROR_FATAL)

inline bool shouldTry(HookResult result) {
    return result == HookResult::UNKNOWN || result == HookResult::ERROR_RETRY;
}
inline bool anyMatches(const std::initializer_list<HookResult> &results, HookResult search) {
    return std::any_of(results.begin(), results.end(),[=](HookResult r) { return r == search; });
}

class HookGlobals {
public:
    static constexpr const char *KNOWN_NAMESPACES[] = {
        "default",
        "system",
        "sphal",
        "product",
    };

    MapInfo mapInfo;
    Symbol_ref getExportedNamespace;

    Config origValues, hookValues;
    EventQueue eventQueue;

    HookResult hookSetupResult = HookResult::UNKNOWN;
    HookResult hookStaticResult = HookResult::UNKNOWN;
    HookResult hookDynamicResult = HookResult::UNKNOWN;

    bool patchEnabled = false;
    bool guardEnabled = true;

    EEManager eeManager;
    std::set<tNCI_DISCOVERY_TYPE> discoveryTypes;

    Hook_ref hNFC_SetConfig;
    Hook_ref hNFC_DiscoveryStart;
    Hook_ref hNFA_Enable;
    Hook_ref hce_select_t4t;
    Symbol_ref nfa_dm_cb;
    Symbol_ref hce_cb;
    Symbol_ref hNFA_StopRfDiscovery;
    Symbol_ref hNFA_DisablePolling;
    Symbol_ref hNFA_StartRfDiscovery;
    Symbol_ref hNFA_EnablePolling;
    Symbol_ref hNFA_EeModeSet;
    Symbol_ref hNFA_EeGetInfo;

    def_NFA_CONN_CBACK *origNfaConnCBack = nullptr;
    std::mutex hookInstallMutex;

    HookResult installHooks();
    HookResult hookStatus();

protected:
    friend LoadedLibrary;

    HookResult setupHooking();
    HookResult installStaticHooks();
    HookResult installDynamicHooks();

    std::optional<LoadedLibrary> findLibNFC() const;
    bool checkNFACBOffset(uint32_t offset) const;
    uint32_t findNFACBOffset();

    void *getLibraryHandle(const char *filename) const;
    void *dlopenWithNamespace(const char *filename, int flag, const char *nsName) const;
    Symbol_ref findDefaultSymbol(const std::string &name) const;

    LoadedLibrary mLibNFC;
};

extern HookGlobals globals;
