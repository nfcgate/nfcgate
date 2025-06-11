#include <nfcd/nfcd.h>
#include <android/dlext.h>
#include <dlfcn.h>
#include <link.h>

HookGlobals globals;

using def_get_exported_namespace = android_namespace_t*(const char *name);

void hook_nfaConnectionCallback(uint8_t event, void *eventData) {
    auto eventName = System::nfaEventName(event);

    if (eventData)
        LOGI("hook_NFA_Event: %s(%d) with status %d", eventName.c_str(), event, *(uint8_t *)eventData);
    else
        LOGI("hook_NFA_Event: %s(%d)", eventName.c_str(), event);

    // call original callback
    globals.origNfaConnCBack(event, eventData);
    // enqueue event
    globals.eventQueue.enqueue(event, eventData ? *(uint8_t*)eventData : 0);
}

/**
 * Prevent already set values from being overwritten.
 */
tNFC_STATUS hook_NFC_SetConfig(uint8_t tlv_size, uint8_t *p_param_tlvs) {
    globals.hNFC_SetConfig->preCall();

    LOGI("hook_NFC_SetConfig: filtering config stream");

    Config cfg, actual;
    cfg.parse(tlv_size, p_param_tlvs);
    for (auto &opt : cfg.options()) {
        // indicates whether this option would override one of the hook options
        bool conflict = false;
        for (auto &hook_opt : globals.hookValues.options())
            if (hook_opt.type() == opt.type())
                conflict = true;

        // log config values with type codes
        std::stringstream bruce;
        bruce << "NFC_SetConfig Option " << opt.name() << "(" << (int)opt.type() << ", "
              << (!globals.guardEnabled ? "own" : "system") << ", "
              << (globals.guardEnabled && conflict ? "blocked" : "pass") << ")";
        loghex(bruce.str().c_str(), opt.value(), opt.len());

        // prevent config values from overriding hook iff guard is enabled
        if (!globals.guardEnabled || !conflict)
            actual.add(opt);
    }

    // build new config stream
    config_ref bin_stream;
    actual.build(bin_stream);

    // call original function with new config stream
    auto result = globals.hNFC_SetConfig->call<def_NFC_SetConfig>(actual.total(), bin_stream.get());

    // fix hook if needed
    globals.installHooks();

    globals.hNFC_SetConfig->postCall();
    return result;
}

tNFC_STATUS hook_NFC_DiscoveryStart(uint8_t num_params, tNCI_DISCOVER_PARAMS *p_params, void* p_cback) {
    globals.hNFC_DiscoveryStart->preCall();

    LOGI("hook_NFC_DiscoveryStart: Begin: %d, %p", num_params, p_params);

    tNCI_DISCOVER_PARAMS new_params[num_params];
    uint8_t new_num_params = 0;

    if (globals.patchEnabled) {
        for (int i = 0; i < num_params; ++i) {
            tNCI_DISCOVER_PARAMS param = p_params[i];
            // only block "listen" types that are not in the set of allowed types
            bool blocked = param.type >= 0x80 && globals.discoveryTypes.count(param.type) == 0;

            LOGD("  param[%d].type %x", i, param.type);
            LOGD("  param[%d].frequency %d", i, param.frequency);
            LOGD("  param[%d] blocked? %s", i, blocked ? "true" : "false");

            if (!blocked)
                new_params[new_num_params++] = param;
        }

        num_params = new_num_params;
        p_params = new_params;
    }
    else
        LOGD("hook_NFC_DiscoveryStart: patch disabled");

    auto res = globals.hNFC_DiscoveryStart->call<def_NFC_DiscoveryStart>(num_params, p_params, p_cback);
    LOGI("hook_NFC_DiscoveryStart: Result: %x", res);

    globals.hNFC_DiscoveryStart->postCall();
    return res;
}

tNFA_STATUS hook_NFA_Enable(void *p_dm_cback, void *p_conn_cback) {
    globals.hNFA_Enable->preCall();

    std::lock_guard<std::mutex> lock(globals.hookInstallMutex);
    LOGD("hook_NFA_Enable: Hooking p_conn_cback");

    // save original callback, replace with hook callback
    globals.origNfaConnCBack = (def_NFA_CONN_CBACK *) p_conn_cback;
    p_conn_cback = (void *) hook_nfaConnectionCallback;

    // call original function with hook connection callback
    auto result = globals.hNFA_Enable->call<decltype(hook_NFA_Enable)>(p_dm_cback, p_conn_cback);
    if (shouldTry(globals.hookDynamicResult)) {
        LOGI("hook_NFA_Enable: Delayed hook success");
        globals.hookDynamicResult = HookResult::SUCCESS;
    }

    globals.hNFA_Enable->postCall();
    return result;
}

tNFC_STATUS hook_ce_select_t4t() {
    globals.hce_select_t4t->preCall();

    LOGD("hook_ce_select_t4t()");
    LOGD("Patch enabled: %d", globals.patchEnabled);

    tNFC_STATUS r = globals.hce_select_t4t->call<def_ce_select_t4t>();
    if (globals.patchEnabled) {
        int offset = System::sdkInt() < System::O_1 ? CE_CB_STATUS_PRE_O : CE_CB_STATUS_POST_O;
        auto ce_cb_status = globals.hce_cb->address<uint8_t>() + offset;
        // bypass ISO 7816 SELECT requirement for AID selection
        *ce_cb_status |= CE_T4T_STATUS_WILDCARD_AID_SELECTED;
    }

    globals.hce_select_t4t->postCall();
    return r;
}

HookResult HookGlobals::installHooks() {
    std::lock_guard<std::mutex> lock(hookInstallMutex);

    // setup hooking if needed
    if (shouldTry(hookSetupResult)) {
        hookSetupResult = setupHooking();
        if (hookSetupResult != HookResult::SUCCESS)
            LOGW("Hooking setup failed with %d", static_cast<int>(hookSetupResult));
        else
            LOGI("Hooking setup success");
    }
    // install static hooks if needed
    if (hookSetupResult == HookResult::SUCCESS && shouldTry(hookStaticResult)) {
        hookStaticResult = installStaticHooks();
        if (hookStaticResult != HookResult::SUCCESS)
            LOGW("Hooking static hooks failed with %d", static_cast<int>(hookStaticResult));
        else
            LOGI("Hooking static hooks success");
    }
    // install dynamic hooks if needed
    if (hookStaticResult == HookResult::SUCCESS && shouldTry(hookDynamicResult)) {
        hookDynamicResult = installDynamicHooks();
        if (hookDynamicResult != HookResult::SUCCESS)
            LOGW("Hooking dynamic hooks failed with %d, may be delayed (waiting for NFA_Enable or NFC_SetConfig)",
                 static_cast<int>(hookDynamicResult));
        else
            LOGI("Hooking dynamic hooks success");
    }

    return hookStatus();
}

HookResult HookGlobals::hookStatus() {
    // check for errors in ascending order of priority, unknown state implies a previous error
    auto results = {hookSetupResult, hookStaticResult, hookDynamicResult};
    if (anyMatches(results, HookResult::ERROR_FATAL))
        return HookResult::ERROR_FATAL;
    else if (anyMatches(results, HookResult::ERROR_RETRY))
        return HookResult::ERROR_RETRY;

    return HookResult::SUCCESS;
}

HookResult HookGlobals::setupHooking() {
    // create library map info
    LOG_ASSERT_S(mapInfo.create(), return HookResult::ERROR_FATAL, "Could not create map");

    // try to lookup namespace symbol (only required on Android >= 15)
    if (System::sdkInt() >= System::SdkVersion::V)
        getExportedNamespace = findDefaultSymbol("android_get_exported_namespace");

    // check if NCI library exists and is loaded
    if (mLibNFC.empty()) {
        const auto candidate = findLibNFC();
        LOG_ASSERT_S(candidate, return HookResult::ERROR_RETRY, "Library not found or not accessible");

        mLibNFC = candidate.value();
        LOGI("Library found at %s", mLibNFC.name().c_str());
    }

    return HookResult::SUCCESS;
}

HookResult HookGlobals::installStaticHooks() {
    // begin installing hooks
    Hook::init();
    {
        // NFC/NFA main functions
        ASSERT_HOOK(mLibNFC.findAndHookOnce(hNFC_SetConfig, "NFC_SetConfig", (void *) &hook_NFC_SetConfig));
        ASSERT_HOOK(mLibNFC.findAndHookOnce(hNFC_DiscoveryStart, "NFC_DiscoveryStart", (void *)&hook_NFC_DiscoveryStart));
        ASSERT_HOOK(mLibNFC.findAndHookOnce(hNFA_Enable, "NFA_Enable", (void *)&hook_NFA_Enable));

        // discovery
        ASSERT_HOOK(hNFA_StartRfDiscovery = mLibNFC.findSymbol("NFA_StartRfDiscovery"));
        ASSERT_HOOK(hNFA_StopRfDiscovery = mLibNFC.findSymbol("NFA_StopRfDiscovery"));

        // polling / listening
        ASSERT_HOOK(hNFA_EnablePolling = mLibNFC.findSymbol("NFA_EnablePolling"));
        ASSERT_HOOK(hNFA_DisablePolling = mLibNFC.findSymbol("NFA_DisablePolling"));
        ASSERT_HOOK(hNFA_EeModeSet = mLibNFC.findSymbol("NFA_EeModeSet"));
        ASSERT_HOOK(hNFA_EeGetInfo = mLibNFC.findSymbol("NFA_EeGetInfo"));

        // NFC routing
        ASSERT_HOOK(mLibNFC.findAndHookOnce(hce_select_t4t, "ce_select_t4t", (void *)&hook_ce_select_t4t));
        ASSERT_HOOK(hce_cb = mLibNFC.findSymbol("ce_cb"));

        // NFA callback
        ASSERT_HOOK(nfa_dm_cb = mLibNFC.findSymbol("nfa_dm_cb"));
    }
    // finish installing hooks
    LOG_ASSERT_S(Hook::finish(), return HookResult::ERROR_FATAL, "Hooking install failed");

    return HookResult::SUCCESS;
}

HookResult HookGlobals::installDynamicHooks() {
    uint32_t offset = findNFACBOffset();
    LOG_ASSERT_S(offset != 0, return HookResult::ERROR_RETRY, "Finding p_conn_cback offset failed");

    auto **p_nfa_conn_cback = (def_NFA_CONN_CBACK **) (nfa_dm_cb->address<uint8_t>() + offset);
    LOG_ASSERT_S(*p_nfa_conn_cback, return HookResult::ERROR_RETRY, "NFA_CB is null");

    // ensure to hook only once
    if (*p_nfa_conn_cback != &hook_nfaConnectionCallback) {
        LOGD("installDynamicHooks: Hooking NFA_CB");

        // save old nfa connection callback
        origNfaConnCBack = *p_nfa_conn_cback;
        // set new nfa connection callback
        *p_nfa_conn_cback = &hook_nfaConnectionCallback;
    } else
        LOGD("installDynamicHooks: NFA_CB already hooked");

    return HookResult::SUCCESS;
}

static std::optional<LoadedLibrary> selectJNICandidate(const std::vector<LoadedLibrary> &candidates) {
    // if there are multiple candidates containing NFC_SetConfig, select the JNI library
    for (const auto &candidate : candidates) {
        // check if the candidate is a JNI library by looking for JNI_OnLoad
        if (candidate.findSymbol("JNI_OnLoad"))
            return candidate;
    }

    // there are multiple candidates, but none is a JNI library -> error
    return std::nullopt;
}

std::optional<LoadedLibrary> HookGlobals::findLibNFC() const {
    std::vector<LoadedLibrary> finalists;

    for (const auto &libPath : mapInfo.loadedLibraries()) {
        LoadedLibrary candidate(libPath);
        LOGD("findLibNFC: candidate: %s", candidate.name().c_str());

        // condition 1: library path must contain "nfc" case insensitive somewhere
        if (!StringUtil::strContains(StringUtil::toLower(candidate.name()), "nfc"))
            continue;
        LOGD("findLibNFC: candidate contains 'nfc', checking symbols: %s", candidate.name().c_str());

        // condition 2: library symbol table must contain the expected symbol
        if (!candidate.createSymbolTable() || !candidate.symbolTable().contains("NFC_SetConfig"))
            continue;
        LOGD("findLibNFC: candidate contains symbol 'NFC_SetConfig': %s", candidate.name().c_str());

        // condition 3: library handle must be found
        if (!candidate.findLibraryHandle())
            continue;
        LOGD("findLibNFC: candidate handle found: %s", candidate.name().c_str());

        finalists.push_back(candidate);
    }

    if (finalists.empty())
        return std::nullopt;
    else if (finalists.size() == 1)
        return finalists.front();
    else
        return selectJNICandidate(finalists);
}

bool HookGlobals::checkNFACBOffset(uint32_t offset) const {
    LOGD("checkOffset: trying offset 0x%x", offset);

    // try to get nfa_dm_cb[offset]
    auto **p_nfa_conn_cback = (def_NFA_CONN_CBACK**)(nfa_dm_cb->address<uint8_t>() + offset);
    LOG_ASSERT_S(*p_nfa_conn_cback, return false, "p_conn_cback is null, offset may be invalid");

    // Criteria 1: lookup the memory range in which the candidate p_nfa_conn_cback is,
    // this ensures that the pointer must be nonzero, and points to any valid memory address
    auto lookup = mapInfo.lookupRange(reinterpret_cast<uintptr_t>(*p_nfa_conn_cback));
    LOG_ASSERT_S(lookup, return false, "p_conn_cback range lookup failed, offset likely invalid");

    // Criteria 2: check that the candidate p_nfa_conn_cback is in an executable section
    LOGD("checkOffset: candidate p_conn_cback %p with permissions %d in object file %s",
         *p_nfa_conn_cback, lookup.range->perms, lookup.library->label.c_str());
    LOG_ASSERT_S((lookup.range->perms & 1) == 1, return false,
                 "p_conn_cback permissions not execute, offset likely invalid");

    // Criteria 3: check that the candidate p_nfa_conn_cback is in an object file containing "jni" "nfc" or "nci"
    // in the name, this ensures that the pointer doesn't point to an unrelated object file in the process
    auto lowerCaseLabel = StringUtil::toLower(lookup.library->label);
    LOG_ASSERT_S(StringUtil::strContainsAny(lowerCaseLabel, {"jni", "nfc", "nci"}), return false,
                 "p_conn_cback not in any NFC-related object file, offset likely invalid");

    // all checks passed, offset likely valid
    LOGD("checkOffset: success");
    return true;
}

uint32_t HookGlobals::findNFACBOffset() {
    // search [standard_offset, standard_offset + 2]
    for (uint32_t i = 0; i < 2; i++) {
        uint32_t offset = NFA_DM_CB_CONN_CBACK + (i * sizeof(void*));

        if (checkNFACBOffset(offset))
            return offset;
    }

    return 0;
}

void *HookGlobals::getLibraryHandle(const char *filename) const {
    int flag = RTLD_NOW | RTLD_NOLOAD;

    // try with standard dlopen first
    if (void *result = dlopen(filename, flag)) {
        LOGI("Library %s handle found in global or current namespace", filename);
        return result;
    }

    // if the getExportedNamespace symbol is available, try using the list of known namespaces
    if (getExportedNamespace) {
        for (const char *nsName : KNOWN_NAMESPACES) {
            if (void *result = dlopenWithNamespace(filename, flag, nsName)) {
                LOGI("Library %s handle found in namespace %s", filename, nsName);
                return result;
            }
        }
    }

    return nullptr;
}

void *HookGlobals::dlopenWithNamespace(const char *filename, int flag, const char *nsName) const {
    if (auto *ns = getExportedNamespace->call<def_get_exported_namespace>(nsName)) {
        android_dlextinfo info = {
                .flags = ANDROID_DLEXT_USE_NAMESPACE,
                .library_namespace = ns
        };

        return android_dlopen_ext(filename, flag, &info);
    }

    return nullptr;
}

Symbol_ref HookGlobals::findDefaultSymbol(const std::string &name) const {
    auto result = std::make_shared<DefaultSymbol>(name);
    LOG_ASSERT_S(result->isValid(), return {}, "Default symbol %s not found", name.c_str());
    return result;
}
