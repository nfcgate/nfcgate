#include <nfcd/nfcd.h>
#include <jni.h>

static void beginCollectingEvents() {
    globals.eventQueue.beginCollecting();
}

static void waitForEvent(uint8_t event, bool checkStatus = true) {
    uint8_t status;
    if (globals.eventQueue.waitFor(event, status, 500)) {
        if (checkStatus && status != 0)
            LOGW("[event] Unexpected status for %s: expected 0, got %d",
                 System::nfaEventName(event).c_str(), status);
    }
    else
        LOGW("[event] Waiting for %s failed: timeout reached", System::nfaEventName(event).c_str());
}

std::set<tNCI_DISCOVERY_TYPE> discoveryTypesFromConfig(const Config &config) {
    std::set<tNCI_DISCOVERY_TYPE> result;

    for (const auto &option : config.options()) {
        if (option.name().find("LA") == 0) {
            result.emplace(NCI_DISCOVERY_TYPE_LISTEN_A);
            result.emplace(NCI_DISCOVERY_TYPE_LISTEN_A_ACTIVE);
        }
        else if (option.name().find("LB") == 0) {
            result.emplace(NCI_DISCOVERY_TYPE_LISTEN_B);
        }
        else if (option.name().find("LF") == 0) {
            result.emplace(NCI_DISCOVERY_TYPE_LISTEN_F);
            result.emplace(NCI_DISCOVERY_TYPE_LISTEN_F_ACTIVE);
        }
    }

    return result;
}

void nfaEnableDiscovery() {
    beginCollectingEvents();
    globals.hNFA_StartRfDiscovery->call<def_NFA_StartRfDiscovery>();
    LOGD("[nfcd] Starting RF discovery");
    waitForEvent(NFA_RF_DISCOVERY_STARTED_EVT);
}

void nfaDisableDiscovery() {
    beginCollectingEvents();
    globals.hNFA_StopRfDiscovery->call<def_NFA_StopRfDiscovery>();
    LOGD("[nfcd] Stopping RF discovery");
    waitForEvent(NFA_RF_DISCOVERY_STOPPED_EVT, false);
}

void nfaEnablePolling() {
    /*
     * Note: only enable known technologies, since enabling all (0xFF) also enabled exotic
     * proprietary ones, which may fail to start without special configuration
     */
    beginCollectingEvents();
    globals.hNFA_EnablePolling->call<def_NFA_EnablePolling>(SAFE_TECH_MASK);
    LOGD("[nfcd] Enabling polling");
    waitForEvent(NFA_POLL_ENABLED_EVT);
}

void nfaDisablePolling() {
    beginCollectingEvents();
    globals.hNFA_DisablePolling->call<def_NFA_DisablePolling>();
    LOGD("[nfcd] Disabling polling");
    waitForEvent(NFA_POLL_DISABLED_EVT);
}

void disableEEs() {
    for (uint16_t eeHandle : globals.eeManager.findActiveEEs()) {
        LOGD("[nfcd] Deactivating EE: %x", eeHandle);

        globals.hNFA_EeModeSet->call<def_NFA_EeModeSet>(eeHandle, NFA_EE_MD_DEACTIVATE);
        usleep(25000);

        globals.eeManager.markDeactivated(eeHandle);
    }
}
void reenableEEs() {
    for (uint16_t eeHandle : globals.eeManager.deactivatedEEs()) {
        LOGD("[nfcd] Re-activating EE: %x", eeHandle);

        globals.hNFA_EeModeSet->call<def_NFA_EeModeSet>(eeHandle, NFA_EE_MD_ACTIVATE);
        usleep(25000);

        globals.eeManager.markActivated(eeHandle);
    }
}

void limitDiscoveryTypes(std::set<tNCI_DISCOVERY_TYPE> discoveryTypes) {
    globals.discoveryTypes = discoveryTypes;
    for (tNCI_DISCOVERY_TYPE discoveryType : discoveryTypes)
        LOGD("[nfcd] Limiting listen to type %d", discoveryType);
}

void applyConfig(Config &config) {
    config_ref bin_stream;
    config.build(bin_stream);

    globals.guardEnabled = false;
    globals.hNFC_SetConfig->callHook<def_NFC_SetConfig>(config.total(), bin_stream.get());
    globals.guardEnabled = true;

    // wait for config to set before returning
    usleep(35000);
}

extern "C" {
    JNIEXPORT jint JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_installHooks(JNIEnv *, jobject) {
        return static_cast<int>(globals.installHooks());
    }

    JNIEXPORT jboolean JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_isPatchEnabled(JNIEnv *, jobject) {
        return globals.patchEnabled;
    }

    JNIEXPORT void JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_setConfig(JNIEnv *env, jobject, jbyteArray config) {
        // early return if hook not fully installed
        if (globals.hookStatus() != HookResult::SUCCESS) {
            LOGE("[nfcd] Failed to set config because native hook is not fully installed");
            return;
        }

        // parse config value stream
        jsize config_len = env->GetArrayLength(config);
        jbyte *config_data = env->GetByteArrayElements(config, nullptr);
        globals.hookValues.parse(config_len, (uint8_t *) config_data);
        env->ReleaseByteArrayElements(config, config_data, 0);

        // begin re-routing AIDs and limiting discovery types
        globals.patchEnabled = true;

        // disable discovery before changing anything
        nfaDisableDiscovery();
        {
            // disable EEs that may be interfering with our config
            disableEEs();
            // limit discovery types only for the selected technologies
            auto discoveryTypes = discoveryTypesFromConfig(globals.hookValues);
            if (!discoveryTypes.empty())
                limitDiscoveryTypes(discoveryTypes);
            // apply the config stream
            applyConfig(globals.hookValues);
            // disable polling
            nfaDisablePolling();
        }
        // re-enable discovery after changes were made
        nfaEnableDiscovery();
    }

    JNIEXPORT void JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_resetConfig(JNIEnv *, jobject) {
        // early return if hook not fully installed
        if (globals.hookStatus() != HookResult::SUCCESS) {
            LOGE("[nfcd] Failed to set config because native hook is not fully installed");
            return;
        }

        if (globals.patchEnabled) {
            // stop re-routing AIDs and limiting discovery types
            globals.patchEnabled = false;
            globals.discoveryTypes.clear();

            // disable discovery before changing anything
            nfaDisableDiscovery();
            // re-enable polling
            nfaEnablePolling();
            // re-enable previously disabled EEs
            reenableEEs();
            // re-enable discovery after changes were made
            nfaEnableDiscovery();
        }
    }
}
