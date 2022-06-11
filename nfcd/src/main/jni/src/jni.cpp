#include <nfcd/nfcd.h>
#include <jni.h>

static void beginCollectingEvents() {
    EventQueue::instance().beginCollecting();
}

static void waitForEvent(uint8_t event, bool checkStatus = true) {
    uint8_t status;
    if (EventQueue::instance().waitFor(event, status, 500)) {
        if (checkStatus && status != 0)
            LOGD("[event] Unexpected status for event %d: expected 0, got %d", event, status);
    }
    else
        LOGD("[event] Waiting for event %d failed: timeout reached", event);
}

void setPollingEnabled(bool enable) {
    LOGD("[polling] %s", (enable ? "Enabling" : "Disabling"));

    beginCollectingEvents();
    hNFA_StopRfDiscovery->call<def_NFA_StopRfDiscovery>();
    LOGD("[polling] Stopping RF discovery");
    waitForEvent(NFA_RF_DISCOVERY_STOPPED_EVT, false);

    if (enable) {
        /*
         * Note: only enable known technologies, since enabling all (0xFF) also enabled exotic
         * proprietary ones, which may fail to start without special configuration
         */
        beginCollectingEvents();
        hNFA_EnablePolling->call<def_NFA_EnablePolling>(SAFE_TECH_MASK);
        LOGD("[polling] Enabling polling");
        waitForEvent(NFA_POLL_ENABLED_EVT);
    }
    else {
        beginCollectingEvents();
        hNFA_DisablePolling->call<def_NFA_DisablePolling>();
        LOGD("[polling] Disabling polling");
        waitForEvent(NFA_POLL_DISABLED_EVT);
    }

    beginCollectingEvents();
    hNFA_StartRfDiscovery->call<def_NFA_StartRfDiscovery>();
    LOGD("[polling] Starting RF discovery");
    waitForEvent(NFA_RF_DISCOVERY_STARTED_EVT);
}

void uploadConfig(Config &config) {
    LOGI("[config]");

    config_ref bin_stream;
    config.build(bin_stream);

    // NCI standard states that NFCID cannot be set during discovery
    beginCollectingEvents();
    hNFA_StopRfDiscovery->call<def_NFA_StopRfDiscovery>();
    LOGD("[config] Stopping RF discovery");
    waitForEvent(NFA_RF_DISCOVERY_STOPPED_EVT, false);

    guardEnabled = false;
    hNFC_SetConfig->callHook<def_NFC_SetConfig>(config.total(), bin_stream.get());
    guardEnabled = true;

    // wait for config to set before returning
    usleep(35000);
}

extern "C" {
    JNIEXPORT jboolean JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_isHookEnabled(JNIEnv *, jobject) {
        return hookEnabled;
    }

    JNIEXPORT jboolean JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_isPatchEnabled(JNIEnv *, jobject) {
        return patchEnabled;
    }

    JNIEXPORT void JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_setConfiguration(JNIEnv *env, jobject, jbyteArray config) {
        if (!env->IsSameObject(config, nullptr)) {
            jsize config_len = env->GetArrayLength(config);
            jbyte *config_data = env->GetByteArrayElements(config, nullptr);
            hookValues.parse(config_len, (uint8_t *) config_data);
            env->ReleaseByteArrayElements(config, config_data, 0);

            patchEnabled = true;
            uploadConfig(hookValues);

            // disable polling after setting config, re-enable discovery
            setPollingEnabled(false);
        }
        else {
            patchEnabled = false;
        }
    }

    JNIEXPORT void JNICALL Java_de_tu_1darmstadt_seemoo_nfcgate_xposed_Native_setPolling(JNIEnv *, jobject, jboolean enabled) {
        setPollingEnabled(enabled);
    }
}
