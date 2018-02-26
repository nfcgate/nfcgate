/******************************************************************************
 *
 *  Copyright (C) 2009-2014 Broadcom Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/


/******************************************************************************
 *
 *  This file contains the Near Field Communication (NFC) API function
 *  external definitions.
 *
 ******************************************************************************/

#ifndef NFC_API_H
#define NFC_API_H

#if 0

#include "nfc_target.h"
#include "nci_defs.h"
#include "nfc_hal_api.h"
#include "gki.h"

#include "vendor_cfg.h"

#else
    #include "nci_defs.h"
    #include "tags_defs.h"
    #include "bt_types.h"
#endif

/* NFC application return status codes */
#define NFC_STATUS_OK                   NCI_STATUS_OK                   /* Command succeeded    */
#define NFC_STATUS_REJECTED             NCI_STATUS_REJECTED             /* Command is rejected. */
#define NFC_STATUS_MSG_CORRUPTED        NCI_STATUS_MESSAGE_CORRUPTED    /* Message is corrupted */
#define NFC_STATUS_BUFFER_FULL          NCI_STATUS_BUFFER_FULL          /* buffer full          */
#define NFC_STATUS_FAILED               NCI_STATUS_FAILED               /* failed               */
#define NFC_STATUS_NOT_INITIALIZED      NCI_STATUS_NOT_INITIALIZED      /* not initialized      */
#define NFC_STATUS_SYNTAX_ERROR         NCI_STATUS_SYNTAX_ERROR         /* Syntax error         */
#define NFC_STATUS_SEMANTIC_ERROR       NCI_STATUS_SEMANTIC_ERROR       /* Semantic error       */
#define NFC_STATUS_UNKNOWN_GID          NCI_STATUS_UNKNOWN_GID          /* Unknown NCI Group ID */
#define NFC_STATUS_UNKNOWN_OID          NCI_STATUS_UNKNOWN_OID          /* Unknown NCI Opcode   */
#define NFC_STATUS_INVALID_PARAM        NCI_STATUS_INVALID_PARAM        /* Invalid Parameter    */
#define NFC_STATUS_MSG_SIZE_TOO_BIG     NCI_STATUS_MSG_SIZE_TOO_BIG     /* Message size too big */
#define NFC_STATUS_ALREADY_STARTED      NCI_STATUS_ALREADY_STARTED      /* Already started      */
#define NFC_STATUS_ACTIVATION_FAILED    NCI_STATUS_ACTIVATION_FAILED    /* Activation Failed    */
#define NFC_STATUS_TEAR_DOWN            NCI_STATUS_TEAR_DOWN            /* Tear Down Error      */
#define NFC_STATUS_RF_TRANSMISSION_ERR  NCI_STATUS_RF_TRANSMISSION_ERR  /* RF transmission error*/
#define NFC_STATUS_RF_PROTOCOL_ERR      NCI_STATUS_RF_PROTOCOL_ERR      /* RF protocol error    */
#define NFC_STATUS_TIMEOUT              NCI_STATUS_TIMEOUT              /* RF Timeout           */
#define NFC_STATUS_EE_INTF_ACTIVE_FAIL  NCI_STATUS_EE_INTF_ACTIVE_FAIL  /* EE Intf activate err */
#define NFC_STATUS_EE_TRANSMISSION_ERR  NCI_STATUS_EE_TRANSMISSION_ERR  /* EE transmission error*/
#define NFC_STATUS_EE_PROTOCOL_ERR      NCI_STATUS_EE_PROTOCOL_ERR      /* EE protocol error    */
#define NFC_STATUS_EE_TIMEOUT           NCI_STATUS_EE_TIMEOUT           /* EE Timeout           */

/* 0xE0 ~0xFF are proprietary status codes */
#define NFC_STATUS_CMD_STARTED          0xE3/* Command started successfully                     */
#define NFC_STATUS_HW_TIMEOUT           0xE4/* NFCC Timeout in responding to an NCI command     */
#define NFC_STATUS_CONTINUE             0xE5/* More (same) event to follow                      */
#define NFC_STATUS_REFUSED              0xE6/* API is called to perform illegal function        */
#define NFC_STATUS_BAD_RESP             0xE7/* Wrong format of R-APDU, CC file or NDEF file     */
#define NFC_STATUS_CMD_NOT_CMPLTD       0xE8/* 7816 Status Word is not command complete(0x9000) */
#define NFC_STATUS_NO_BUFFERS           0xE9/* Out of GKI buffers                               */
#define NFC_STATUS_WRONG_PROTOCOL       0xEA/* Protocol mismatch between API and activated one  */
#define NFC_STATUS_BUSY                 0xEB/* Another Tag command is already in progress       */

#define NFC_STATUS_LINK_LOSS            0xFC                      /* Link Loss                  */
#define NFC_STATUS_BAD_LENGTH           0xFD                      /* data len exceeds MIU       */
#define NFC_STATUS_BAD_HANDLE           0xFE                      /* invalid handle             */
#define NFC_STATUS_CONGESTED            0xFF                      /* congested                  */
typedef UINT8 tNFC_STATUS;



/**********************************************
 * NFC Config Parameter IDs defined by NCI
 **********************************************/
#define NFC_PMID_TOTAL_DURATION     NCI_PARAM_ID_TOTAL_DURATION
#define NFC_PMID_CON_DEVICES_LIMIT  NCI_PARAM_ID_CON_DEVICES_LIMIT
#define NFC_PMID_PA_BAILOUT         NCI_PARAM_ID_PA_BAILOUT
#define NFC_PMID_PB_AFI             NCI_PARAM_ID_PB_AFI
#define NFC_PMID_PB_BAILOUT         NCI_PARAM_ID_PB_BAILOUT
#define NFC_PMID_PB_ATTRIB_PARAM1   NCI_PARAM_ID_PB_ATTRIB_PARAM1
#define NFC_PMID_PF_BIT_RATE        NCI_PARAM_ID_PF_BIT_RATE
#define NFC_PMID_PF_RC              NCI_PARAM_ID_PF_RC
#define NFC_PMID_PB_H_INFO          NCI_PARAM_ID_PB_H_INFO
#define NFC_PMID_BITR_NFC_DEP       NCI_PARAM_ID_BITR_NFC_DEP
#define NFC_PMID_ATR_REQ_GEN_BYTES  NCI_PARAM_ID_ATR_REQ_GEN_BYTES
#define NFC_PMID_ATR_REQ_CONFIG     NCI_PARAM_ID_ATR_REQ_CONFIG
#define NFC_PMID_LA_HIST_BY         NCI_PARAM_ID_LA_HIST_BY
#define NFC_PMID_LA_NFCID1          NCI_PARAM_ID_LA_NFCID1
#define NFC_PMID_PI_BIT_RATE        NCI_PARAM_ID_PI_BIT_RATE
#define NFC_PMID_LA_BIT_FRAME_SDD   NCI_PARAM_ID_LA_BIT_FRAME_SDD
#define NFC_PMID_LA_PLATFORM_CONFIG NCI_PARAM_ID_LA_PLATFORM_CONFIG
#define NFC_PMID_LA_SEL_INFO        NCI_PARAM_ID_LA_SEL_INFO
#define NFC_PMID_LI_BIT_RATE        NCI_PARAM_ID_LI_BIT_RATE
#define NFC_PMID_LB_SENSB_INFO      NCI_PARAM_ID_LB_SENSB_INFO
#define NFC_PMID_LB_PROTOCOL        NCI_PARAM_ID_LB_PROTOCOL
#define NFC_PMID_LB_H_INFO          NCI_PARAM_ID_LB_H_INFO_RSP
#define NFC_PMID_LB_NFCID0          NCI_PARAM_ID_LB_NFCID0
#define NFC_PMID_LB_APPDATA         NCI_PARAM_ID_LB_APPDATA
#define NFC_PMID_LB_SFGI            NCI_PARAM_ID_LB_SFGI
#define NFC_PMID_LB_ADC_FO          NCI_PARAM_ID_LB_ADC_FO
#define NFC_PMID_LF_T3T_ID1         NCI_PARAM_ID_LF_T3T_ID1
#define NFC_PMID_LF_T3T_ID2         NCI_PARAM_ID_LF_T3T_ID2
#define NFC_PMID_LF_T3T_ID3         NCI_PARAM_ID_LF_T3T_ID3
#define NFC_PMID_LF_T3T_ID4         NCI_PARAM_ID_LF_T3T_ID4
#define NFC_PMID_LF_T3T_ID5         NCI_PARAM_ID_LF_T3T_ID5
#define NFC_PMID_LF_T3T_ID6         NCI_PARAM_ID_LF_T3T_ID6
#define NFC_PMID_LF_T3T_ID7         NCI_PARAM_ID_LF_T3T_ID7
#define NFC_PMID_LF_T3T_ID8         NCI_PARAM_ID_LF_T3T_ID8
#define NFC_PMID_LF_T3T_ID9         NCI_PARAM_ID_LF_T3T_ID9
#define NFC_PMID_LF_T3T_ID10        NCI_PARAM_ID_LF_T3T_ID10
#define NFC_PMID_LF_T3T_ID11        NCI_PARAM_ID_LF_T3T_ID11
#define NFC_PMID_LF_T3T_ID12        NCI_PARAM_ID_LF_T3T_ID12
#define NFC_PMID_LF_T3T_ID13        NCI_PARAM_ID_LF_T3T_ID13
#define NFC_PMID_LF_T3T_ID14        NCI_PARAM_ID_LF_T3T_ID14
#define NFC_PMID_LF_T3T_ID15        NCI_PARAM_ID_LF_T3T_ID15
#define NFC_PMID_LF_T3T_ID16        NCI_PARAM_ID_LF_T3T_ID16
#define NFC_PMID_LF_PROTOCOL        NCI_PARAM_ID_LF_PROTOCOL
#define NFC_PMID_LF_T3T_PMM         NCI_PARAM_ID_LF_T3T_PMM
#define NFC_PMID_LF_T3T_MAX         NCI_PARAM_ID_LF_T3T_MAX
#define NFC_PMID_LF_T3T_FLAGS2      NCI_PARAM_ID_LF_T3T_FLAGS2
#define NFC_PMID_FWI                NCI_PARAM_ID_FWI
#define NFC_PMID_LF_CON_BITR_F      NCI_PARAM_ID_LF_CON_BITR_F
#define NFC_PMID_WT                 NCI_PARAM_ID_WT
#define NFC_PMID_ATR_RES_GEN_BYTES  NCI_PARAM_ID_ATR_RES_GEN_BYTES
#define NFC_PMID_ATR_RSP_CONFIG     NCI_PARAM_ID_ATR_RSP_CONFIG
#define NFC_PMID_RF_FIELD_INFO      NCI_PARAM_ID_RF_FIELD_INFO
#define NFC_PMID_NFC_DEP_OP         NCI_PARAM_ID_NFC_DEP_OP
#define NFC_PARAM_ID_RF_EE_ACTION   NCI_PARAM_ID_RF_EE_ACTION
#define NFC_PARAM_ID_ISO_DEP_OP     NCI_PARAM_ID_ISO_DEP_OP

#define NFC_ROUTE_TAG_TECH           NCI_ROUTE_TAG_TECH      /* Technology based routing  */
#define NFC_ROUTE_TAG_PROTO          NCI_ROUTE_TAG_PROTO     /* Protocol based routing  */
#define NFC_ROUTE_TAG_AID            NCI_ROUTE_TAG_AID       /* AID routing */
#define NFC_ROUTE_TLV_ENTRY_SIZE     4 /* tag, len, 2 byte value for technology/protocol based routing */

/* For routing */
#define NFC_DH_ID                NCI_DH_ID   /* for DH */
/* To identify the loopback test */
#define NFC_TEST_ID              NCI_TEST_ID            /* use a proprietary range */

typedef UINT8 tNFC_PMID;
#define NFC_TL_SIZE                     2
#define NFC_SAVED_CMD_SIZE              2

typedef tNCI_DISCOVER_MAPS   tNFC_DISCOVER_MAPS;
typedef tNCI_DISCOVER_PARAMS tNFC_DISCOVER_PARAMS;

/* all NFC Manager Callback functions have prototype like void (cback) (UINT8 event, void *p_data)
 * tNFC_DATA_CBACK uses connection id as the first parameter; range 0x00-0x0F.
 * tNFC_DISCOVER_CBACK uses tNFC_DISCOVER_EVT; range  0x4000 ~
 * tNFC_RESPONSE_CBACK uses tNFC_RESPONSE_EVT; range  0x5000 ~
 */

#define NFC_FIRST_DEVT      0x4000
#define NFC_FIRST_REVT      0x5000
#define NFC_FIRST_CEVT      0x6000
#define NFC_FIRST_TEVT      0x8000

/* the events reported on tNFC_RESPONSE_CBACK */
enum
{
    NFC_ENABLE_REVT = NFC_FIRST_REVT,       /* 0  Enable event                  */
    NFC_DISABLE_REVT,                       /* 1  Disable event                 */
    NFC_SET_CONFIG_REVT,                    /* 2  Set Config Response           */
    NFC_GET_CONFIG_REVT,                    /* 3  Get Config Response           */
    NFC_NFCEE_DISCOVER_REVT,                /* 4  Discover NFCEE response       */
    NFC_NFCEE_INFO_REVT,                    /* 5  Discover NFCEE Notification   */
    NFC_NFCEE_MODE_SET_REVT,                /* 6  NFCEE Mode Set response       */
    NFC_RF_FIELD_REVT,                      /* 7  RF Field information          */
    NFC_EE_ACTION_REVT,                     /* 8  EE Action notification        */
    NFC_EE_DISCOVER_REQ_REVT,               /* 9  EE Discover Req notification  */
    NFC_SET_ROUTING_REVT,                   /* 10 Configure Routing response    */
    NFC_GET_ROUTING_REVT,                   /* 11 Retrieve Routing response     */
    NFC_RF_COMM_PARAMS_UPDATE_REVT,         /* 12 RF Communication Param Update */
    NFC_GEN_ERROR_REVT,                     /* 13 generic error notification    */
    NFC_NFCC_RESTART_REVT,                  /* 14 NFCC has been re-initialized  */
    NFC_NFCC_TIMEOUT_REVT,                  /* 15 NFCC is not responding        */
    NFC_NFCC_TRANSPORT_ERR_REVT,            /* 16 NCI Tranport error            */
    NFC_NFCC_POWER_OFF_REVT,                /* 17 NFCC turned off               */

    NFC_FIRST_VS_REVT                       /* First vendor-specific rsp event  */
};
typedef UINT16 tNFC_RESPONSE_EVT;

enum
{
    NFC_CONN_CREATE_CEVT = NFC_FIRST_CEVT,  /* 0  Conn Create Response          */
    NFC_CONN_CLOSE_CEVT,                    /* 1  Conn Close Response           */
    NFC_DEACTIVATE_CEVT,                    /* 2  Deactivate response/notificatn*/
    NFC_DATA_CEVT,                          /* 3  Data                          */
    NFC_ERROR_CEVT,                         /* 4  generic or interface error    */
    NFC_DATA_START_CEVT                     /* 5  received the first fragment on RF link */
};
typedef UINT16 tNFC_CONN_EVT;

#define NFC_NFCC_INFO_LEN       4
#ifndef NFC_NFCC_MAX_NUM_VS_INTERFACE
#define NFC_NFCC_MAX_NUM_VS_INTERFACE   5
#endif
typedef struct
{
    tNFC_STATUS             status;         /* The event status.                */
    UINT8                   nci_version;    /* the NCI version of NFCC          */
    UINT8                   max_conn;       /* max number of connections by NFCC*/
    UINT32                  nci_features;   /* the NCI features of NFCC         */
    UINT16                  nci_interfaces; /* the NCI interfaces of NFCC       */
    UINT16                  max_ce_table;   /* the max routing table size       */
    UINT16                  max_param_size; /* Max Size for Large Parameters    */
    UINT8                   manufacture_id; /* the Manufacture ID for NFCC      */
    UINT8                   nfcc_info[NFC_NFCC_INFO_LEN];/* the Manufacture Info for NFCC      */
    UINT8                   vs_interface[NFC_NFCC_MAX_NUM_VS_INTERFACE];  /* the NCI VS interfaces of NFCC    */
} tNFC_ENABLE_REVT;

#define NFC_MAX_NUM_IDS     125
/* the data type associated with NFC_SET_CONFIG_REVT */
typedef struct
{
    tNFC_STATUS             status;         /* The event status.                */
    UINT8                   num_param_id;   /* Number of rejected NCI Param ID  */
    UINT8                   param_ids[NFC_MAX_NUM_IDS];/* NCI Param ID          */
} tNFC_SET_CONFIG_REVT;

/* the data type associated with NFC_GET_CONFIG_REVT */
typedef struct
{
    tNFC_STATUS             status;         /* The event status.    */
    UINT16                  tlv_size;       /* The length of TLV    */
    UINT8                   *p_param_tlvs;  /* TLV                  */
} tNFC_GET_CONFIG_REVT;

/* the data type associated with NFC_NFCEE_DISCOVER_REVT */
typedef struct
{
    tNFC_STATUS             status;         /* The event status.    */
    UINT8                   num_nfcee;      /* The number of NFCEE  */
} tNFC_NFCEE_DISCOVER_REVT;

#define NFC_NFCEE_INTERFACE_APDU         NCI_NFCEE_INTERFACE_APDU
#define NFC_NFCEE_INTERFACE_HCI_ACCESS   NCI_NFCEE_INTERFACE_HCI_ACCESS
#define NFC_NFCEE_INTERFACE_T3T          NCI_NFCEE_INTERFACE_T3T
#define NFC_NFCEE_INTERFACE_TRANSPARENT  NCI_NFCEE_INTERFACE_TRANSPARENT
#define NFC_NFCEE_INTERFACE_PROPRIETARY  NCI_NFCEE_INTERFACE_PROPRIETARY
typedef UINT8 tNFC_NFCEE_INTERFACE;

#if 0
#define NFC_NFCEE_TAG_HW_ID             NCI_NFCEE_TAG_HW_ID
#define NFC_NFCEE_TAG_ATR_BYTES         NCI_NFCEE_TAG_ATR_BYTES
#define NFC_NFCEE_TAG_T3T_INFO          NCI_NFCEE_TAG_T3T_INFO
#define NFC_NFCEE_TAG_HCI_HOST_ID       NCI_NFCEE_TAG_HCI_HOST_ID
typedef UINT8 tNFC_NFCEE_TAG;
/* additional NFCEE Info */
typedef struct
{
    tNFC_NFCEE_TAG          tag;
    UINT8                   len;
    UINT8                   info[NFC_MAX_EE_INFO];
} tNFC_NFCEE_TLV;

#define NFC_NFCEE_STATUS_INACTIVE       NCI_NFCEE_STS_CONN_INACTIVE/* NFCEE connected and inactive */
#define NFC_NFCEE_STATUS_ACTIVE         NCI_NFCEE_STS_CONN_ACTIVE  /* NFCEE connected and active   */
#define NFC_NFCEE_STATUS_REMOVED        NCI_NFCEE_STS_REMOVED      /* NFCEE removed                */
/* the data type associated with NFC_NFCEE_INFO_REVT */
typedef struct
{
    tNFC_STATUS             status;                 /* The event status - place holder  */
    UINT8                   nfcee_id;               /* NFCEE ID                         */
    UINT8                   ee_status;              /* The NFCEE status.                */
    UINT8                   num_interface;          /* number of NFCEE interfaces       */
    UINT8                   ee_interface[NFC_MAX_EE_INTERFACE];/* NFCEE interface       */
    UINT8                   num_tlvs;               /* number of TLVs                   */
    tNFC_NFCEE_TLV          ee_tlv[NFC_MAX_EE_TLVS];/* The TLVs associated with NFCEE   */
} tNFC_NFCEE_INFO_REVT;

#define NFC_MODE_ACTIVATE          NCI_NFCEE_MD_ACTIVATE
#define NFC_MODE_DEACTIVATE        NCI_NFCEE_MD_DEACTIVATE
typedef UINT8 tNFC_NFCEE_MODE;
/* the data type associated with NFC_NFCEE_MODE_SET_REVT */
typedef struct
{
    tNFC_STATUS             status;                 /* The event status.*/
    UINT8                   nfcee_id;               /* NFCEE ID         */
    tNFC_NFCEE_MODE         mode;                   /* NFCEE mode       */
} tNFC_NFCEE_MODE_SET_REVT;
#endif
#define NFC_MAX_AID_LEN     NCI_MAX_AID_LEN     /* 16 */
/* the data type associated with NFC_CE_GET_ROUTING_REVT */
#if 0
typedef struct
{
    tNFC_STATUS             status;         /* The event status                 */
    UINT8                   nfcee_id;       /* NFCEE ID                         */
    UINT8                   num_tlvs;       /* number of TLVs                   */
    UINT8                   tlv_size;       /* the total len of all TLVs        */
    UINT8                   param_tlvs[NFC_MAX_EE_TLV_SIZE];/* the TLVs         */
} tNFC_GET_ROUTING_REVT;
#endif

/* the data type associated with NFC_CONN_CREATE_CEVT */
typedef struct
{
    tNFC_STATUS             status;         /* The event status                 */
    UINT8                   dest_type;      /* the destination type             */
    UINT8                   id;             /* NFCEE ID  or RF Discovery ID     */
    UINT8                   buff_size;      /* The max buffer size              */
    UINT8                   num_buffs;      /* The number of buffers            */
} tNFC_CONN_CREATE_CEVT;

/* the data type associated with NFC_CONN_CLOSE_CEVT */
typedef struct
{
    tNFC_STATUS             status;         /* The event status                 */
} tNFC_CONN_CLOSE_CEVT;

/* the data type associated with NFC_DATA_CEVT */
typedef struct
{
    tNFC_STATUS             status;         /* The event status                 */
    BT_HDR                  *p_data;        /* The received Data                */
} tNFC_DATA_CEVT;

/* RF Field Status */
#define NFC_RF_STS_NO_REMOTE    NCI_RF_STS_NO_REMOTE    /* No field generated by remote device  */
#define NFC_RF_STS_REMOTE       NCI_RF_STS_REMOTE       /* field generated by remote device     */
typedef UINT8 tNFC_RF_STS;

/* RF Field Technologies */
#define NFC_RF_TECHNOLOGY_A     NCI_RF_TECHNOLOGY_A
#define NFC_RF_TECHNOLOGY_B     NCI_RF_TECHNOLOGY_B
#define NFC_RF_TECHNOLOGY_F     NCI_RF_TECHNOLOGY_F
#define NFC_RF_TECHNOLOGY_15693 NCI_RF_TECHNOLOGY_15693
typedef UINT8 tNFC_RF_TECH;


/* Supported Protocols */
#define NFC_PROTOCOL_UNKNOWN    NCI_PROTOCOL_UNKNOWN  /* Unknown */
#define NFC_PROTOCOL_T1T        NCI_PROTOCOL_T1T      /* Type1Tag    - NFC-A            */
#define NFC_PROTOCOL_T2T        NCI_PROTOCOL_T2T      /* Type2Tag    - NFC-A            */
#define NFC_PROTOCOL_T3T        NCI_PROTOCOL_T3T      /* Type3Tag    - NFC-F            */
#define NFC_PROTOCOL_ISO_DEP    NCI_PROTOCOL_ISO_DEP  /* Type 4A,4B  - NFC-A or NFC-B   */
#define NFC_PROTOCOL_NFC_DEP    NCI_PROTOCOL_NFC_DEP  /* NFCDEP/LLCP - NFC-A or NFC-F       */
#define NFC_PROTOCOL_MIFARE     NCI_PROTOCOL_MIFARE
#define NFC_PROTOCOL_B_PRIME    NCI_PROTOCOL_B_PRIME
#define NFC_PROTOCOL_15693      NCI_PROTOCOL_15693
#define NFC_PROTOCOL_KOVIO      NCI_PROTOCOL_KOVIO
typedef UINT8 tNFC_PROTOCOL;

/* Discovery Types/Detected Technology and Mode */
#define NFC_DISCOVERY_TYPE_POLL_A           NCI_DISCOVERY_TYPE_POLL_A
#define NFC_DISCOVERY_TYPE_POLL_B           NCI_DISCOVERY_TYPE_POLL_B
#define NFC_DISCOVERY_TYPE_POLL_F           NCI_DISCOVERY_TYPE_POLL_F
#define NFC_DISCOVERY_TYPE_POLL_A_ACTIVE    NCI_DISCOVERY_TYPE_POLL_A_ACTIVE
#define NFC_DISCOVERY_TYPE_POLL_F_ACTIVE    NCI_DISCOVERY_TYPE_POLL_F_ACTIVE
#define NFC_DISCOVERY_TYPE_POLL_ISO15693    NCI_DISCOVERY_TYPE_POLL_ISO15693
#define NFC_DISCOVERY_TYPE_POLL_B_PRIME     NCI_DISCOVERY_TYPE_POLL_B_PRIME
#define NFC_DISCOVERY_TYPE_POLL_KOVIO       NCI_DISCOVERY_TYPE_POLL_KOVIO
#define NFC_DISCOVERY_TYPE_LISTEN_A         NCI_DISCOVERY_TYPE_LISTEN_A
#define NFC_DISCOVERY_TYPE_LISTEN_B         NCI_DISCOVERY_TYPE_LISTEN_B
#define NFC_DISCOVERY_TYPE_LISTEN_F         NCI_DISCOVERY_TYPE_LISTEN_F
#define NFC_DISCOVERY_TYPE_LISTEN_A_ACTIVE  NCI_DISCOVERY_TYPE_LISTEN_A_ACTIVE
#define NFC_DISCOVERY_TYPE_LISTEN_F_ACTIVE  NCI_DISCOVERY_TYPE_LISTEN_F_ACTIVE
#define NFC_DISCOVERY_TYPE_LISTEN_ISO15693  NCI_DISCOVERY_TYPE_LISTEN_ISO15693
#define NFC_DISCOVERY_TYPE_LISTEN_B_PRIME   NCI_DISCOVERY_TYPE_LISTEN_B_PRIME
typedef UINT8 tNFC_DISCOVERY_TYPE;
typedef UINT8 tNFC_RF_TECH_N_MODE;

/* Select Response codes */
#define NFC_SEL_RES_NFC_FORUM_T2T           0x00
#define NFC_SEL_RES_MF_CLASSIC              0x08

/* Bit Rates */
#define NFC_BIT_RATE_106        NCI_BIT_RATE_106    /* 106 kbit/s */
#define NFC_BIT_RATE_212        NCI_BIT_RATE_212    /* 212 kbit/s */
#define NFC_BIT_RATE_424        NCI_BIT_RATE_424    /* 424 kbit/s */
#define NFC_BIT_RATE_848        NCI_BIT_RATE_848    /* 848 Kbit/s */
#define NFC_BIT_RATE_1696       NCI_BIT_RATE_1696   /* 1696 Kbit/s*/
#define NFC_BIT_RATE_3392       NCI_BIT_RATE_3392   /* 3392 Kbit/s*/
#define NFC_BIT_RATE_6784       NCI_BIT_RATE_6784   /* 6784 Kbit/s*/
typedef UINT8 tNFC_BIT_RATE;

/**********************************************
 * Interface Types
 **********************************************/
#define NFC_INTERFACE_EE_DIRECT_RF  NCI_INTERFACE_EE_DIRECT_RF
#define NFC_INTERFACE_FRAME         NCI_INTERFACE_FRAME
#define NFC_INTERFACE_ISO_DEP       NCI_INTERFACE_ISO_DEP
#define NFC_INTERFACE_NDEF          NCI_INTERFACE_NDEF
#define NFC_INTERFACE_NFC_DEP       NCI_INTERFACE_NFC_DEP
#define NFC_INTERFACE_LLCP_LOW      NCI_INTERFACE_LLCP_LOW
#define NFC_INTERFACE_LLCP_HIGH     NCI_INTERFACE_LLCP_HIGH
#define NFC_INTERFACE_VS_T2T_CE     NCI_INTERFACE_VS_T2T_CE
#define NFC_INTERFACE_MIFARE        NCI_INTERFACE_VS_MIFARE
typedef tNCI_INTF_TYPE tNFC_INTF_TYPE;

/**********************************************
 *  Deactivation Type
 **********************************************/
#define NFC_DEACTIVATE_TYPE_IDLE        NCI_DEACTIVATE_TYPE_IDLE
#define NFC_DEACTIVATE_TYPE_SLEEP       NCI_DEACTIVATE_TYPE_SLEEP
#define NFC_DEACTIVATE_TYPE_SLEEP_AF    NCI_DEACTIVATE_TYPE_SLEEP_AF
#define NFC_DEACTIVATE_TYPE_DISCOVERY   NCI_DEACTIVATE_TYPE_DISCOVERY
typedef UINT8 tNFC_DEACT_TYPE;

/**********************************************
 *  Deactivation Reasons
 **********************************************/
#define NFC_DEACTIVATE_REASON_DH_REQ        NCI_DEACTIVATE_REASON_DH_REQ
#define NFC_DEACTIVATE_REASON_ENDPOINT_REQ  NCI_DEACTIVATE_REASON_ENDPOINT_REQ
#define NFC_DEACTIVATE_REASON_RF_LINK_LOSS  NCI_DEACTIVATE_REASON_RF_LINK_LOSS
#define NFC_DEACTIVATE_REASON_NFCB_BAD_AFI  NCI_DEACTIVATE_REASON_NFCB_BAD_AFI
typedef UINT8 tNFC_DEACT_REASON;

/* the data type associated with NFC_RF_FIELD_REVT */
typedef struct
{
    tNFC_STATUS             status;     /* The event status - place holder. */
    tNFC_RF_STS             rf_field;   /* RF Field Status                  */
} tNFC_RF_FIELD_REVT;

#define NFC_MAX_APP_DATA_LEN    40
typedef struct
{
    UINT8                   len_aid;                /* length of application id */
    UINT8                   aid[NFC_MAX_AID_LEN];   /* application id           */
} tNFC_AID;
typedef struct
{
    UINT8                   len_aid;                /* length of application id */
    UINT8                   aid[NFC_MAX_AID_LEN];   /* application id           */
    UINT8                   len_data;               /* len of application data  */
    UINT8                   data[NFC_MAX_APP_DATA_LEN];  /* application data    */
} tNFC_APP_INIT;

#define NFC_EE_TRIG_SELECT              NCI_EE_TRIG_7816_SELECT  /* ISO 7816-4 SELECT command */
#define NFC_EE_TRIG_RF_PROTOCOL         NCI_EE_TRIG_RF_PROTOCOL  /* RF Protocol changed       */
#define NFC_EE_TRIG_RF_TECHNOLOGY       NCI_EE_TRIG_RF_TECHNOLOGY/* RF Technology changed     */
#define NFC_EE_TRIG_APP_INIT            NCI_EE_TRIG_APP_INIT     /* Application initiation    */
typedef UINT8 tNFC_EE_TRIGGER;
typedef struct
{
    tNFC_EE_TRIGGER         trigger;        /* the trigger of this event        */
    union
    {
        tNFC_PROTOCOL       protocol;
        tNFC_RF_TECH        technology;
        tNFC_AID            aid;
        tNFC_APP_INIT       app_init;
    } param; /* Discovery Type specific parameters */
} tNFC_ACTION_DATA;

/* the data type associated with NFC_EE_ACTION_REVT */
typedef struct
{
    tNFC_STATUS             status;         /* The event status - place holder  */
    UINT8                   nfcee_id;       /* NFCEE ID                         */
    tNFC_ACTION_DATA        act_data;       /* data associated /w the action    */
} tNFC_EE_ACTION_REVT;

#define NFC_EE_DISC_OP_ADD      0
#define NFC_EE_DISC_OP_REMOVE   1
typedef UINT8 tNFC_EE_DISC_OP;
typedef struct
{
    tNFC_EE_DISC_OP         op;             /* add or remove this entry         */
    UINT8                   nfcee_id;       /* NFCEE ID                         */
    tNFC_RF_TECH_N_MODE     tech_n_mode;    /* Discovery Technology and Mode    */
    tNFC_PROTOCOL           protocol;       /* NFC protocol                     */
} tNFC_EE_DISCOVER_INFO;

#ifndef NFC_MAX_EE_DISC_ENTRIES
#define NFC_MAX_EE_DISC_ENTRIES     6
#endif
#define NFC_EE_DISCOVER_ENTRY_LEN   5 /* T, L, V(NFCEE ID, TechnMode, Protocol) */
#define NFC_EE_DISCOVER_INFO_LEN    3 /* NFCEE ID, TechnMode, Protocol */
/* the data type associated with NFC_EE_DISCOVER_REQ_REVT */
typedef struct
{
    tNFC_STATUS             status;         /* The event status - place holder  */
    UINT8                   num_info;       /* number of entries in info[]      */
    tNFC_EE_DISCOVER_INFO   info[NFC_MAX_EE_DISC_ENTRIES];  /* discovery request from NFCEE */
} tNFC_EE_DISCOVER_REQ_REVT;

#if 0
typedef union
{
    tNFC_STATUS                 status;     /* The event status. */
    tNFC_ENABLE_REVT            enable;
    tNFC_SET_CONFIG_REVT        set_config;
    tNFC_GET_CONFIG_REVT        get_config;
    tNFC_NFCEE_DISCOVER_REVT    nfcee_discover;
    tNFC_NFCEE_INFO_REVT        nfcee_info;
    tNFC_NFCEE_MODE_SET_REVT    mode_set;
    tNFC_RF_FIELD_REVT          rf_field;
    tNFC_STATUS                 cfg_routing;
    tNFC_GET_ROUTING_REVT       get_routing;
    tNFC_EE_ACTION_REVT         ee_action;
    tNFC_EE_DISCOVER_REQ_REVT   ee_discover_req;
    void                        *p_vs_evt_data;
} tNFC_RESPONSE;

/*************************************
**  RESPONSE Callback Functions
**************************************/
typedef void (tNFC_RESPONSE_CBACK) (tNFC_RESPONSE_EVT event, tNFC_RESPONSE *p_data);
#endif

/* The events reported on tNFC_VS_CBACK */
/* The event is (NCI_RSP_BIT|oid) for response and (NCI_NTF_BIT|oid) for notification*/

typedef UINT8 tNFC_VS_EVT;

/*************************************
**  Proprietary (Vendor Specific) Callback Functions
**************************************/
typedef void (tNFC_VS_CBACK) (tNFC_VS_EVT event, UINT16 data_len, UINT8 *p_data);

/* the events reported on tNFC_DISCOVER_CBACK */
enum
{
    NFC_START_DEVT = NFC_FIRST_DEVT,    /* Status of NFC_DiscoveryStart     */
    NFC_MAP_DEVT,                       /* Status of NFC_DiscoveryMap       */
    NFC_RESULT_DEVT,                    /* The responses from remote device */
    NFC_SELECT_DEVT,                    /* Status of NFC_DiscoverySelect    */
    NFC_ACTIVATE_DEVT,                  /* RF interface is activated        */
    NFC_DEACTIVATE_DEVT                 /* Status of RF deactivation        */
};
typedef UINT16 tNFC_DISCOVER_EVT;

/* the data type associated with NFC_START_DEVT */
typedef tNFC_STATUS tNFC_START_DEVT;

typedef tNCI_RF_PA_PARAMS tNFC_RF_PA_PARAMS;
#define NFC_MAX_SENSB_RES_LEN         NCI_MAX_SENSB_RES_LEN
#define NFC_NFCID0_MAX_LEN          4
typedef struct
{
    UINT8       sensb_res_len;/* Length of SENSB_RES Response (Byte 2 - Byte 12 or 13) Available after Technology Detection */
    UINT8       sensb_res[NFC_MAX_SENSB_RES_LEN]; /* SENSB_RES Response (ATQ) */
    UINT8       nfcid0[NFC_NFCID0_MAX_LEN];
} tNFC_RF_PB_PARAMS;

#define NFC_MAX_SENSF_RES_LEN       NCI_MAX_SENSF_RES_LEN
#define NFC_NFCID2_LEN              NCI_NFCID2_LEN
typedef struct
{
    UINT8       bit_rate;/* NFC_BIT_RATE_212 or NFC_BIT_RATE_424 */
    UINT8       sensf_res_len;/* Length of SENSF_RES Response (Byte 2 - Byte 17 or 19) Available after Technology Detection */
    UINT8       sensf_res[NFC_MAX_SENSF_RES_LEN]; /* SENSB_RES Response */
    UINT8       nfcid2[NFC_NFCID2_LEN];  /* NFCID2 generated by the Local NFCC for NFC-DEP Protocol.Available for Frame Interface  */
    UINT8       mrti_check;
    UINT8       mrti_update;
} tNFC_RF_PF_PARAMS;

typedef tNCI_RF_LF_PARAMS tNFC_RF_LF_PARAMS;

#define NFC_ISO15693_UID_LEN        8
typedef struct
{
    UINT8       flag;
    UINT8       dsfid;
    UINT8       uid[NFC_ISO15693_UID_LEN];
} tNFC_RF_PISO15693_PARAMS;

#ifndef NFC_KOVIO_MAX_LEN
#define NFC_KOVIO_MAX_LEN       32
#endif
typedef struct
{
    UINT8       uid_len;
    UINT8       uid[NFC_KOVIO_MAX_LEN];
} tNFC_RF_PKOVIO_PARAMS;

typedef union
{
    tNFC_RF_PA_PARAMS   pa;
    tNFC_RF_PB_PARAMS   pb;
    tNFC_RF_PF_PARAMS   pf;
    tNFC_RF_LF_PARAMS   lf;
    tNFC_RF_PISO15693_PARAMS pi93;
    tNFC_RF_PKOVIO_PARAMS pk;
} tNFC_RF_TECH_PARAMU;

typedef struct
{
    tNFC_DISCOVERY_TYPE     mode;
    tNFC_RF_TECH_PARAMU     param;
} tNFC_RF_TECH_PARAMS;

/* the data type associated with NFC_RESULT_DEVT */
typedef struct
{
    tNFC_STATUS             status;         /* The event status - place holder.  */
    UINT8                   rf_disc_id;     /* RF Discovery ID                   */
    UINT8                   protocol;       /* supported protocol                */
    tNFC_RF_TECH_PARAMS     rf_tech_param;  /* RF technology parameters          */
    UINT8                   more;           /* 0: last, 1: last (limit), 2: more */
} tNFC_RESULT_DEVT;

/* the data type associated with NFC_SELECT_DEVT */
typedef tNFC_STATUS tNFC_SELECT_DEVT;

/* the data type associated with NFC_STOP_DEVT */
typedef tNFC_STATUS tNFC_STOP_DEVT;

#define NFC_MAX_ATS_LEN             NCI_MAX_ATS_LEN
#define NFC_MAX_HIS_BYTES_LEN       NCI_MAX_HIS_BYTES_LEN
#define NFC_MAX_GEN_BYTES_LEN       NCI_MAX_GEN_BYTES_LEN


typedef struct
{
    UINT8       ats_res_len;                /* Length of ATS RES                */
    UINT8       ats_res[NFC_MAX_ATS_LEN];   /* ATS RES                          */
    BOOLEAN     nad_used;                   /* NAD is used or not               */
    UINT8       fwi;                        /* Frame Waiting time Integer       */
    UINT8       sfgi;                       /* Start-up Frame Guard time Integer*/
    UINT8       his_byte_len;               /* len of historical bytes          */
    UINT8       his_byte[NFC_MAX_HIS_BYTES_LEN];/* historical bytes             */
} tNFC_INTF_PA_ISO_DEP;

typedef struct
{
    UINT8       rats;  /* RATS */
} tNFC_INTF_LA_ISO_DEP;


typedef struct
{
    UINT8       atr_res_len;                /* Length of ATR_RES            */
    UINT8       atr_res[NFC_MAX_ATS_LEN];   /* ATR_RES (Byte 3 - Byte 17+n) */
    UINT8       max_payload_size;           /* 64, 128, 192 or 254          */
    UINT8       gen_bytes_len;              /* len of general bytes         */
    UINT8       gen_bytes[NFC_MAX_GEN_BYTES_LEN];/* general bytes           */
    UINT8       waiting_time;               /* WT -> Response Waiting Time RWT = (256 x 16/fC) x 2WT */
} tNFC_INTF_PA_NFC_DEP;

/* Note: keep tNFC_INTF_PA_NFC_DEP data member in the same order as tNFC_INTF_LA_NFC_DEP */
typedef struct
{
    UINT8       atr_req_len;                /* Length of ATR_REQ            */
    UINT8       atr_req[NFC_MAX_ATS_LEN];   /* ATR_REQ (Byte 3 - Byte 18+n) */
    UINT8       max_payload_size;           /* 64, 128, 192 or 254          */
    UINT8       gen_bytes_len;              /* len of general bytes         */
    UINT8       gen_bytes[NFC_MAX_GEN_BYTES_LEN];/* general bytes           */
} tNFC_INTF_LA_NFC_DEP;
typedef tNFC_INTF_LA_NFC_DEP tNFC_INTF_LF_NFC_DEP;
typedef tNFC_INTF_PA_NFC_DEP tNFC_INTF_PF_NFC_DEP;

#define NFC_MAX_ATTRIB_LEN      NCI_MAX_ATTRIB_LEN

typedef struct
{
    UINT8       attrib_res_len;                /* Length of ATTRIB RES      */
    UINT8       attrib_res[NFC_MAX_ATTRIB_LEN];/* ATTRIB RES                */
    UINT8       hi_info_len;                   /* len of Higher layer Info  */
    UINT8       hi_info[NFC_MAX_GEN_BYTES_LEN];/* Higher layer Info         */
    UINT8       mbli;                          /* Maximum buffer length.    */
} tNFC_INTF_PB_ISO_DEP;

typedef struct
{
    UINT8       attrib_req_len;                /* Length of ATTRIB REQ      */
    UINT8       attrib_req[NFC_MAX_ATTRIB_LEN];/* ATTRIB REQ (Byte 2 - 10+k)*/
    UINT8       hi_info_len;                   /* len of Higher layer Info  */
    UINT8       hi_info[NFC_MAX_GEN_BYTES_LEN];/* Higher layer Info         */
    UINT8       nfcid0[NFC_NFCID0_MAX_LEN];    /* NFCID0                    */
} tNFC_INTF_LB_ISO_DEP;


#ifndef NFC_MAX_RAW_PARAMS
#define NFC_MAX_RAW_PARAMS      16
#endif
#define NFC_MAX_RAW_PARAMS       16
typedef struct
{
    UINT8       param_len;
    UINT8       param[NFC_MAX_RAW_PARAMS];
} tNFC_INTF_FRAME;

typedef struct
{
    tNFC_INTF_TYPE      type;  /* Interface Type  1 Byte  See Table 67 */
    union
    {
        tNFC_INTF_LA_ISO_DEP    la_iso;
        tNFC_INTF_PA_ISO_DEP    pa_iso;
        tNFC_INTF_LB_ISO_DEP    lb_iso;
        tNFC_INTF_PB_ISO_DEP    pb_iso;
        tNFC_INTF_LA_NFC_DEP    la_nfc;
        tNFC_INTF_PA_NFC_DEP    pa_nfc;
        tNFC_INTF_LF_NFC_DEP    lf_nfc;
        tNFC_INTF_PF_NFC_DEP    pf_nfc;
        tNFC_INTF_FRAME         frame;
    } intf_param;       /* Activation Parameters   0 - n Bytes */
} tNFC_INTF_PARAMS;

/* the data type associated with NFC_ACTIVATE_DEVT */
typedef struct
{
    UINT8                   rf_disc_id;     /* RF Discovery ID          */
    tNFC_PROTOCOL           protocol;       /* supported protocol       */
    tNFC_RF_TECH_PARAMS     rf_tech_param;  /* RF technology parameters */
    tNFC_DISCOVERY_TYPE     data_mode;      /* for future Data Exchange */
    tNFC_BIT_RATE           tx_bitrate;     /* Data Exchange Tx Bitrate */
    tNFC_BIT_RATE           rx_bitrate;     /* Data Exchange Rx Bitrate */
    tNFC_INTF_PARAMS        intf_param;     /* interface type and params*/
} tNFC_ACTIVATE_DEVT;

/* the data type associated with NFC_DEACTIVATE_DEVT and NFC_DEACTIVATE_CEVT */
typedef struct
{
    tNFC_STATUS             status;         /* The event status.        */
    tNFC_DEACT_TYPE         type;           /* De-activate type         */
    BOOLEAN                 is_ntf;         /* TRUE, if deactivate notif*/
} tNFC_DEACTIVATE_DEVT;

typedef union
{
    tNFC_STATUS             status;         /* The event status.        */
    tNFC_START_DEVT         start;
    tNFC_RESULT_DEVT        result;
    tNFC_SELECT_DEVT        select;
    tNFC_STOP_DEVT          stop;
    tNFC_ACTIVATE_DEVT      activate;
    tNFC_DEACTIVATE_DEVT    deactivate;
} tNFC_DISCOVER;

/* Min TR0 indicates to tag the min delay before responding after the end of command */
#define NFC_RF_PARAM_MIN_TR0_DEFAULT    0x00
#define NFC_RF_PARAM_MIN_TR0_48X        0x01    /* 48 x 16/fc */
#define NFC_RF_PARAM_MIN_TR0_16X        0x02    /* 16 x 16/fc */

/* Min TR1 indicates to tag the min delay between subcarrier modulation and data transmission */
#define NFC_RF_PARAM_MIN_TR1_DEFAULT    0x00
#define NFC_RF_PARAM_MIN_TR1_64X        0x01    /* 64 x 16/fc */
#define NFC_RF_PARAM_MIN_TR1_16X        0x02    /* 16 x 16/fc */

/* Min TR2 indicates to RW the min delay between EoS of tag and SoS of RW */
#define NFC_RF_PARAM_MIN_TR2_1792       0x00    /* 1792/fc (10etu + 32/fc) */
#define NFC_RF_PARAM_MIN_TR2_3328       0x01    /* 3328/fc (10etu + 128/fc) */
#define NFC_RF_PARAM_MIN_TR2_5376       0x02    /* 5376/fc (10etu + 256/fc) */
#define NFC_RF_PARAM_MIN_TR2_9472       0x03    /* 9472/fc (10etu + 512/fc) */

#define NFC_RF_PARAM_EOS_REQUIRED       0x00    /* EoS required */
#define NFC_RF_PARAM_EOS_NOT_REQUIRED   0x01    /* EoS not required */

#define NFC_RF_PARAM_SOS_REQUIRED       0x00    /* SoS required */
#define NFC_RF_PARAM_SOS_NOT_REQUIRED   0x01    /* SoS not required */

typedef struct
{
    BOOLEAN                 include_rf_tech_mode;   /* TRUE if including RF Tech and Mode update    */
    tNFC_RF_TECH_N_MODE     rf_tech_n_mode;         /* RF tech and mode                             */
    BOOLEAN                 include_tx_bit_rate;    /* TRUE if including Tx bit rate update         */
    tNFC_BIT_RATE           tx_bit_rate;            /* Transmit Bit Rate                            */
    BOOLEAN                 include_rx_bit_rate;    /* TRUE if including Rx bit rate update         */
    tNFC_BIT_RATE           rx_bit_rate;            /* Receive Bit Rate                             */
    BOOLEAN                 include_nfc_b_config;   /* TRUE if including NFC-B data exchange config */
    UINT8                   min_tr0;                /* Minimun TR0                                  */
    UINT8                   min_tr1;                /* Minimun TR1                                  */
    UINT8                   suppression_eos;        /* Suppression of EoS                           */
    UINT8                   suppression_sos;        /* Suppression of SoS                           */
    UINT8                   min_tr2;                /* Minimun TR1                                  */
} tNFC_RF_COMM_PARAMS;

/*************************************
**  DISCOVER Callback Functions
**************************************/
typedef void (tNFC_DISCOVER_CBACK) (tNFC_DISCOVER_EVT event, tNFC_DISCOVER *p_data);

/* the events reported on tNFC_TEST_CBACK */
enum
{
    NFC_LOOPBACK_TEVT = NFC_FIRST_TEVT, /* 0  Loopback test             */
    NFC_RF_CONTROL_TEVT,                /* 1  RF control Test response  */
    NFC_RF_FIELD_DONE_TEVT              /* 1  RF control Test notificatn*/
};
typedef UINT16 tNFC_TEST_EVT;

#if 0
/* the data type associated with NFC_LOOPBACK_TEVT */
typedef struct
{
    tNFC_STATUS             status;     /* The event status.            */
    BT_HDR                  *p_data;    /* The loop back data from NFCC */
} tNFC_LOOPBACK_TEVT;
#endif

/* the data type associated with NFC_RF_CONTROL_TEVT */
typedef tNFC_STATUS tNFC_RF_CONTROL_TEVT;

#if 0
typedef union
{
    tNFC_STATUS             status;     /* The event status.            */
    tNFC_LOOPBACK_TEVT      loop_back;
    tNFC_RF_CONTROL_TEVT    rf_control;
} tNFC_TEST;
#endif

/*************************************
**  TEST Callback Functions
**************************************/
#if 0
typedef void (tNFC_TEST_CBACK) (tNFC_TEST_EVT event, tNFC_TEST *p_data);
#endif

typedef tNFC_DEACTIVATE_DEVT tNFC_DEACTIVATE_CEVT;
typedef union
{
    tNFC_STATUS             status;     /* The event status. */
    tNFC_CONN_CREATE_CEVT   conn_create;
    tNFC_CONN_CLOSE_CEVT    conn_close;
    tNFC_DEACTIVATE_CEVT    deactivate;
    tNFC_DATA_CEVT          data;
} tNFC_CONN;
/*************************************
**  Data Callback Functions
**************************************/
typedef void (tNFC_CONN_CBACK) (UINT8 conn_id, tNFC_CONN_EVT event, tNFC_CONN *p_data);
#define NFC_MAX_CONN_ID                15
#define NFC_ILLEGAL_CONN_ID            0xFF
#define NFC_RF_CONN_ID                 0    /* the static connection ID for RF traffic */



/*************************************
**  Status callback function
**************************************/
typedef void (tNFC_STATUS_CBACK) (tNFC_STATUS status);

/*****************************************************************************
**  EXTERNAL FUNCTION DECLARATIONS
*****************************************************************************/
#ifdef __cplusplus
extern "C" {
#endif

#if 0
/*******************************************************************************
**
** Function         NFC_Enable
**
** Description      This function enables NFC. Prior to calling NFC_Enable:
**                  - the NFCC must be powered up, and ready to receive commands.
**                  - GKI must be enabled
**                  - NFC_TASK must be started
**                  - NCIT_TASK must be started (if using dedicated NCI transport)
**
**                  This function opens the NCI transport (if applicable),
**                  resets the NFC controller, and initializes the NFC subsystems.
**
**                  When the NFC startup procedure is completed, an
**                  NFC_ENABLE_REVT is returned to the application using the
**                  tNFC_RESPONSE_CBACK.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_Enable (tNFC_RESPONSE_CBACK *p_cback);

/*******************************************************************************
**
** Function         NFC_Disable
**
** Description      This function performs clean up routines for shutting down
**                  NFC and closes the NCI transport (if using dedicated NCI
**                  transport).
**
**                  When the NFC shutdown procedure is completed, an
**                  NFC_DISABLED_REVT is returned to the application using the
**                  tNFC_RESPONSE_CBACK.
**
** Returns          nothing
**
*******************************************************************************/
NFC_API extern void NFC_Disable (void);

/*******************************************************************************
**
** Function         NFC_Init
**
** Description      This function initializes control blocks for NFC
**
** Returns          nothing
**
*******************************************************************************/
NFC_API extern void NFC_Init(tHAL_NFC_ENTRY *p_hal_entry_tbl);

/*******************************************************************************
**
** Function         NFC_GetLmrtSize
**
** Description      Called by application wto query the Listen Mode Routing
**                  Table size supported by NFCC
**
** Returns          Listen Mode Routing Table size
**
*******************************************************************************/
NFC_API extern UINT16 NFC_GetLmrtSize(void);

/*******************************************************************************
**
** Function         NFC_SetConfig
**
** Description      This function is called to send the configuration parameter
**                  TLV to NFCC. The response from NFCC is reported by
**                  tNFC_RESPONSE_CBACK as NFC_SET_CONFIG_REVT.
**
** Parameters       tlv_size - the length of p_param_tlvs.
**                  p_param_tlvs - the parameter ID/Len/Value list
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_SetConfig (UINT8     tlv_size,
                                          UINT8    *p_param_tlvs);

/*******************************************************************************
**
** Function         NFC_GetConfig
**
** Description      This function is called to retrieve the parameter TLV from NFCC.
**                  The response from NFCC is reported by tNFC_RESPONSE_CBACK
**                  as NFC_GET_CONFIG_REVT.
**
** Parameters       num_ids - the number of parameter IDs
**                  p_param_ids - the parameter ID list.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_GetConfig (UINT8     num_ids,
                                          UINT8    *p_param_ids);

/*******************************************************************************
**
** Function         NFC_NfceeDiscover
**
** Description      This function is called to enable or disable NFCEE Discovery.
**                  The response from NFCC is reported by tNFC_RESPONSE_CBACK
**                  as NFC_NFCEE_DISCOVER_REVT.
**                  The notification from NFCC is reported by tNFC_RESPONSE_CBACK
**                  as NFC_NFCEE_INFO_REVT.
**
** Parameters       discover - 1 to enable discover, 0 to disable.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_NfceeDiscover (BOOLEAN discover);

/*******************************************************************************
**
** Function         NFC_NfceeModeSet
**
** Description      This function is called to activate or de-activate an NFCEE
**                  connected to the NFCC.
**                  The response from NFCC is reported by tNFC_RESPONSE_CBACK
**                  as NFC_NFCEE_MODE_SET_REVT.
**
** Parameters       nfcee_id - the NFCEE to activate or de-activate.
**                  mode - 0 to activate NFCEE, 1 to de-activate.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_NfceeModeSet (UINT8              nfcee_id,
                                             tNFC_NFCEE_MODE    mode);
/*******************************************************************************
**
** Function         NFC_DiscoveryMap
**
** Description      This function is called to set the discovery interface mapping.
**                  The response from NFCC is reported by tNFC_DISCOVER_CBACK as.
**                  NFC_MAP_DEVT.
**
** Parameters       num - the number of items in p_params.
**                  p_maps - the discovery interface mappings
**                  p_cback - the discovery callback function
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_DiscoveryMap(UINT8 num, tNFC_DISCOVER_MAPS *p_maps,
                                        tNFC_DISCOVER_CBACK *p_cback);

/*******************************************************************************
**
** Function         NFC_DiscoveryStart
**
** Description      This function is called to start Polling and/or Listening.
**                  The response from NFCC is reported by tNFC_DISCOVER_CBACK as.
**                  NFC_START_DEVT. The notification from NFCC is reported by
**                  tNFC_DISCOVER_CBACK as NFC_RESULT_DEVT.
**
** Parameters       num_params - the number of items in p_params.
**                  p_params - the discovery parameters
**                  p_cback - the discovery callback function
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_DiscoveryStart(UINT8                 num_params,
                                              tNFC_DISCOVER_PARAMS *p_params,
                                              tNFC_DISCOVER_CBACK  *p_cback);

/*******************************************************************************
**
** Function         NFC_DiscoverySelect
**
** Description      If tNFC_DISCOVER_CBACK reports status=NFC_MULTIPLE_PROT,
**                  the application needs to use this function to select the
**                  the logical endpoint to continue. The response from NFCC is
**                  reported by tNFC_DISCOVER_CBACK as NFC_SELECT_DEVT.
**
** Parameters       rf_disc_id - The ID identifies the remote device.
**                  protocol - the logical endpoint on the remote devide
**                  rf_interface - the RF interface to communicate with NFCC
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_DiscoverySelect (UINT8    rf_disc_id,
                                                UINT8    protocol,
                                                UINT8    rf_interface);

/*******************************************************************************
**
** Function         NFC_ConnCreate
**
** Description      This function is called to create a logical connection with
**                  NFCC for data exchange.
**                  The response from NFCC is reported in tNFC_CONN_CBACK
**                  as NFC_CONN_CREATE_CEVT.
**
** Parameters       dest_type - the destination type
**                  id   - the NFCEE ID or RF Discovery ID .
**                  protocol - the protocol
**                  p_cback - the data callback function to receive data fron NFCC
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_ConnCreate(UINT8             dest_type,
                                          UINT8             id,
                                          UINT8             protocol,
                                          tNFC_CONN_CBACK  *p_cback);

/*******************************************************************************
**
** Function         NFC_ConnClose
**
** Description      This function is called to close a logical connection with
**                  NFCC.
**                  The response from NFCC is reported in tNFC_CONN_CBACK
**                  as NFC_CONN_CLOSE_CEVT.
**
** Parameters       conn_id - the connection id.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_ConnClose(UINT8 conn_id);

/*******************************************************************************
**
** Function         NFC_SetStaticRfCback
**
** Description      This function is called to update the data callback function
**                  to receive the data for the given connection id.
**
** Parameters       p_cback - the connection callback function
**
** Returns          Nothing
**
*******************************************************************************/
NFC_API extern void NFC_SetStaticRfCback(tNFC_CONN_CBACK    *p_cback);

/*******************************************************************************
**
** Function         NFC_SetReassemblyFlag
**
** Description      This function is called to set if nfc will reassemble
**                  nci packet as much as its buffer can hold or it should not
**                  reassemble but forward the fragmented nci packet to layer above.
**                  If nci data pkt is fragmented, nfc may send multiple
**                  NFC_DATA_CEVT with status NFC_STATUS_CONTINUE before sending
**                  NFC_DATA_CEVT with status NFC_STATUS_OK based on reassembly
**                  configuration and reassembly buffer size
**
** Parameters       reassembly - flag to indicate if nfc may reassemble or not
**
** Returns          Nothing
**
*******************************************************************************/
NFC_API extern void NFC_SetReassemblyFlag (BOOLEAN    reassembly);

/*******************************************************************************
**
** Function         NFC_SendData
**
** Description      This function is called to send the given data packet
**                  to the connection identified by the given connection id.
**
** Parameters       conn_id - the connection id.
**                  p_data - the data packet
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_SendData(UINT8       conn_id,
                                        BT_HDR     *p_data);

/*******************************************************************************
**
** Function         NFC_FlushData
**
** Description      This function is called to discard the tx data queue of
**                  the given connection id.
**
** Parameters       conn_id - the connection id.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_FlushData (UINT8       conn_id);

/*******************************************************************************
**
** Function         NFC_Deactivate
**
** Description      This function is called to stop the discovery process or
**                  put the listen device in sleep mode or terminate the NFC link.
**
**                  The response from NFCC is reported by tNFC_DISCOVER_CBACK
**                  as NFC_DEACTIVATE_DEVT.
**
** Parameters       deactivate_type - NFC_DEACTIVATE_TYPE_IDLE, to IDLE mode.
**                                    NFC_DEACTIVATE_TYPE_SLEEP to SLEEP mode.
**                                    NFC_DEACTIVATE_TYPE_SLEEP_AF to SLEEP_AF mode.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_Deactivate(tNFC_DEACT_TYPE deactivate_type);

/*******************************************************************************
**
** Function         NFC_UpdateRFCommParams
**
** Description      This function is called to update RF Communication parameters
**                  once the Frame RF Interface has been activated.
**
**                  The response from NFCC is reported by tNFC_RESPONSE_CBACK
**                  as NFC_RF_COMM_PARAMS_UPDATE_REVT.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_UpdateRFCommParams (tNFC_RF_COMM_PARAMS *p_params);

/*******************************************************************************
**
** Function         NFC_SetPowerOffSleep
**
** Description      This function closes/opens transport and turns off/on NFCC.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_SetPowerOffSleep (BOOLEAN enable);

/*******************************************************************************
**
** Function         NFC_PowerCycleNFCC
**
** Description      This function turns off and then on NFCC.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_PowerCycleNFCC (void);

/*******************************************************************************
**
** Function         NFC_SetRouting
**
** Description      This function is called to configure the CE routing table.
**                  The response from NFCC is reported by tNFC_RESPONSE_CBACK
**                  as NFC_SET_ROUTING_REVT.
**
** Parameters
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_SetRouting(BOOLEAN     more,
                                            UINT8       num_tlv,
                                            UINT8       tlv_size,
                                            UINT8      *p_param_tlvs);

/*******************************************************************************
**
** Function         NFC_GetRouting
**
** Description      This function is called to retrieve the CE routing table from
**                  NFCC. The response from NFCC is reported by tNFC_RESPONSE_CBACK
**                  as NFC_GET_ROUTING_REVT.
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_GetRouting(void);

/*******************************************************************************
**
** Function         NFC_RegVSCback
**
** Description      This function is called to register or de-register a callback
**                  function to receive Proprietary NCI response and notification
**                  events.
**                  The maximum number of callback functions allowed is NFC_NUM_VS_CBACKS
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_RegVSCback (BOOLEAN          is_register,
                                           tNFC_VS_CBACK   *p_cback);

/*******************************************************************************
**
** Function         NFC_SendVsCommand
**
** Description      This function is called to send the given vendor specific
**                  command to NFCC. The response from NFCC is reported to the
**                  given tNFC_VS_CBACK as (oid).
**
** Parameters       oid - The opcode of the VS command.
**                  p_data - The parameters for the VS command
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_SendVsCommand(UINT8          oid,
                                             BT_HDR        *p_data,
                                             tNFC_VS_CBACK *p_cback);

/*******************************************************************************
**
** Function         NFC_TestLoopback
**
** Description      This function is called to send the given data packet
**                  to NFCC for loopback test.
**                  When loopback data is received from NFCC, tNFC_TEST_CBACK .
**                  reports a NFC_LOOPBACK_TEVT.
**
** Parameters       p_data - the data packet
**
** Returns          tNFC_STATUS
**
*******************************************************************************/
NFC_API extern tNFC_STATUS NFC_TestLoopback(BT_HDR *p_data);


/*******************************************************************************
**
** Function         NFC_SetTraceLevel
**
** Description      This function sets the trace level for NFC.  If called with
**                  a value of 0xFF, it simply returns the current trace level.
**
** Returns          The new or current trace level
**
*******************************************************************************/
NFC_API extern UINT8 NFC_SetTraceLevel (UINT8 new_level);

#endif

#if (BT_TRACE_VERBOSE == TRUE)
#if 0
/*******************************************************************************
**
** Function         NFC_GetStatusName
**
** Description      This function returns the status name.
**
** NOTE             conditionally compiled to save memory.
**
** Returns          pointer to the name
**
*******************************************************************************/
NFC_API extern char * NFC_GetStatusName (tNFC_STATUS status);
#endif
#endif

#ifdef __cplusplus
}
#endif

#endif /* NFC_API_H */
