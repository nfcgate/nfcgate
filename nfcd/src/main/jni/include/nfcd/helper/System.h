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
