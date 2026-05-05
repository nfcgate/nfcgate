#ifndef NFCGATE_SYSTEM_H
#define NFCGATE_SYSTEM_H

/* NCI definitions */
using tNFC_STATUS = uint8_t;
using tNFA_STATUS = uint8_t;
using tNFA_TECHNOLOGY_MASK = uint8_t;
using tNCI_DISCOVERY_TYPE = uint8_t;
// Wildcard AID selected
#define CE_T4T_STATUS_WILDCARD_AID_SELECTED 0x40
// NCI OK
#define NCI_STATUS_OK 0x00

// offset to ce_cb->mem.t4t.status field (ce_int.h)
#define CE_CB_STATUS_POST_O 0xd0
#define CE_CB_STATUS_PRE_O 0xd8
// usual offset to nfa_dm_cb->p_conn_cback field (nfa_dm_int.h)
#define NFA_DM_CB_CONN_CBACK (8 * sizeof(void*))

// modified from nfc_api.h
#define NFC_ACTIVATE_DEVT 0x4004
#define NFC_INTERFACE_ISO_DEP 2

typedef struct {
    uint8_t rats;
} tNFC_INTF_LA_ISO_DEP;
typedef struct {
    uint8_t ats_res_len;
    uint8_t ats_res[64];
    // ignore the rest as the size of ats_res varies across versions and the fields after it are not relevant
} tNFC_INTF_PA_ISO_DEP;
typedef struct {
    uint8_t attrib_res_len;
    uint8_t attrib_res[58];
} tNFC_INTF_PB_ISO_DEP;
typedef struct {
    uint8_t attrib_req_len;
    uint8_t attrib_req[58];
} tNFC_INTF_LB_ISO_DEP;
typedef struct {
    uint8_t rf_disc_id;
    uint8_t protocol;
    uint8_t rf_tech_param_mode;
    // rf_tech_param is a union of all possible technology parameters, the length of which was determined as follows:
    // tNCI_RF_PA_PARAMS:
    // 14 = 2 + 1 + 10 + 1 (2012 00c3aad)
    // 17 = 2 + 1 + 10 + 1 + 1 + 2 (since 2013 251a2cb)
    // tNCI_RF_PB_PARAMS;
    // 17 = 1 + 12 + 4 (2012 00c3aad)
    // 18 = 1 + 12 + 4 + 1 (since 2024 6271a2c)
    // tNCI_RF_PF_PARAMS;
    // 30 = 1 + 1 + 18 + 8 + 1 + 1 (since 2012 00c3aad)
    // tNCI_RF_LF_PARAMS;
    // 8 (since 2012 00c3aad)
    // tNFC_RF_PISO15693_PARAMS;
    // 10 = 1 + 1 + 8 (since 2012 00c3aad)
    // tNFC_RF_PKOVIO_PARAMS;
    // 17 = 1 + 16 (2012 00c3aad)
    // 33 = 1 + 32 (since 2013 251a2cb)
    // tNCI_RF_ACM_P_PARAMS;
    // 116 = 1 + 64 + 1 + 1 + 48 + 1 (since 2017 053c73a)
    // ---
    // possible sizes:
    // 2012 00c3aad: 30 (~ API 16 4.1.0)
    // 2013 251a2cb: 33 (~ API 16 4.1.2)
    // 2017 053c73a: 116 (~ API 28 9.0.0)
    uint8_t rf_tech_param[116 /* or 33 or 30 */];
    uint8_t data_mode;
    uint8_t tx_bitrate;
    uint8_t rx_bitrate;
    uint8_t intf_type;
    union {
        tNFC_INTF_LA_ISO_DEP la_iso;
        tNFC_INTF_PA_ISO_DEP pa_iso;
        tNFC_INTF_LB_ISO_DEP lb_iso;
        tNFC_INTF_PB_ISO_DEP pb_iso;
    };
} tNFC_ACTIVATE_DEVT;

// modified from nfa_api.h

typedef struct {
    uint8_t type;
    uint8_t frequency;
} tNCI_DISCOVER_PARAMS;

/* NFA EE status */
#define NFA_EE_STATUS_INACTIVE 0x01
/* NFA EE modes */
#define NFA_EE_MD_ACTIVATE 0x01
#define NFA_EE_MD_DEACTIVATE 0x00

/* NFA Connection Callback Events */
#define NFA_POLL_ENABLED_EVT 0
#define NFA_POLL_DISABLED_EVT 1
#define NFA_RF_DISCOVERY_STARTED_EVT 30
#define NFA_RF_DISCOVERY_STOPPED_EVT 31
#define NFA_SET_P2P_LISTEN_TECH_EVT 33

/* NFA Tech Mask Values */
#define NFA_TECHNOLOGY_MASK_A 0x01
#define NFA_TECHNOLOGY_MASK_B 0x02
#define NFA_TECHNOLOGY_MASK_F 0x04
#define NFA_TECHNOLOGY_MASK_V 0x08
#define NFA_TECHNOLOGY_MASK_A_ACTIVE 0x40
#define NFA_TECHNOLOGY_MASK_F_ACTIVE 0x80
#define NFA_TECHNOLOGY_MASK_KOVIO 0x20
// modified from DEFAULT_TECH_MASK in NativeNfcManager.cpp
#define SAFE_TECH_MASK \
    (NFA_TECHNOLOGY_MASK_A | NFA_TECHNOLOGY_MASK_B | NFA_TECHNOLOGY_MASK_F | \
     NFA_TECHNOLOGY_MASK_V | NFA_TECHNOLOGY_MASK_A_ACTIVE | NFA_TECHNOLOGY_MASK_F_ACTIVE | \
     NFA_TECHNOLOGY_MASK_KOVIO)

/* NCI Discovery Mask Values */
#define NCI_DISCOVERY_TYPE_POLL_A 0x00
#define NCI_DISCOVERY_TYPE_POLL_B 0x01
#define NCI_DISCOVERY_TYPE_POLL_F 0x02
#define NCI_DISCOVERY_TYPE_POLL_V 0x06
#define NCI_DISCOVERY_TYPE_LISTEN_A 0x80
#define NCI_DISCOVERY_TYPE_LISTEN_B 0x81
#define NCI_DISCOVERY_TYPE_LISTEN_F 0x82
#define NCI_DISCOVERY_TYPE_LISTEN_A_ACTIVE 0x83
#define NCI_DISCOVERY_TYPE_LISTEN_F_ACTIVE 0x85
#define NCI_DISCOVERY_TYPE_LISTEN_ISO15693 0x86

/* EE interface types */
#define NCI_NFCEE_INTERFACE_APDU 0x00
#define NCI_NFCEE_INTERFACE_HCI_ACCESS 0x01
#define NCI_NFCEE_INTERFACE_T3T 0x02
#define NCI_NFCEE_INTERFACE_TRANSPARENT 0x03
#define NCI_NFCEE_INTERFACE_PROPRIETARY 0x80

class System {
public:
    enum SdkVersion {
        O_1 = 26,       //< Oreo (8.0.0)
        O_2 = 27,       //< Oreo (8.1.0)
        P = 28,         //< Pie (9)
        Q = 29,         //< Quince Tart (10)
        V = 35,         //< Vanilla Ice Cream (15)
    };

    static int sdkInt();

    static std::string nfaEventName(uint8_t event);

protected:
    static int sSdkInt;
};

#endif //NFCGATE_SYSTEM_H
