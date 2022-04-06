#ifndef NFCGATE_SYSTEM_H
#define NFCGATE_SYSTEM_H

/* NCI definitions */
using tNFC_STATUS = uint8_t;
using tNFA_STATUS = uint8_t;
using tNFA_TECHNOLOGY_MASK = uint8_t;
// Wildcard AID selected
#define CE_T4T_STATUS_WILDCARD_AID_SELECTED 0x40
// offset to ce_cb->mem.t4t.status field (ce_int.h)
#define CE_CB_STATUS_POST_O 0xd0
#define CE_CB_STATUS_PRE_O 0xd8
// offset to nfa_dm_cb->p_conn_cback field (nfa_dm_int.h)
#define NFA_DM_CB_CONN_CBACK (8 * sizeof(void*))

// modified from nfa_api.h
#define NFA_POLL_ENABLED_EVT 0
#define NFA_POLL_DISABLED_EVT 1
#define NFA_RF_DISCOVERY_STARTED_EVT 30
#define NFA_RF_DISCOVERY_STOPPED_EVT 31

// modified from DEFAULT_TECH_MASK in NativeNfcManager.cpp
#define SAFE_TECH_MASK (0x01 | 0x02 | 0x04 | 0x08 | 0x40 | 0x80 | 0x20)

class System {
public:
    enum SdkVersion {
        O_1 = 26,       //< Oreo (8.0.0)
        O_2 = 27,       //< Oreo (8.1.0)
        P = 28,         //< Pie (9)
        Q = 29,         //< Android10 (10)
    };

    static int sdkInt();

protected:
    static int sSdkInt;
};

#endif //NFCGATE_SYSTEM_H
