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
 *  This file contains the Near Field Communication (NFC) Card Emulation
 *  mode related internal function / definitions.
 *
 ******************************************************************************/

#ifndef CE_INT_H_
#define CE_INT_H_

#include "ce_api.h"

#if 0
#else
#include "nfc_target.h"
#include "gki.h"
#endif

#if (CE_TEST_INCLUDED == FALSE)
#define CE_MIN_SUP_PROTO    NCI_PROTOCOL_FELICA
#define CE_MAX_SUP_PROTO    NCI_PROTOCOL_ISO4
#else
#define CE_MIN_SUP_PROTO    NCI_PROTOCOL_TYPE1
#define CE_MAX_SUP_PROTO    NCI_PROTOCOL_MIFARE
#endif

#define CE_MAX_BYTE_PER_PAGE    7   /* 2^8=256. CB use UINT8 for BytesPerPage, so max is 7 */

/* CE Type 3 Tag structures */

/* Type 3 Tag NDEF card-emulation */
typedef struct {
    BOOLEAN         initialized;
    UINT8           version;        /* Ver: peer version */
    UINT8           nbr;            /* NBr: number of blocks that can be read using one Check command */
    UINT8           nbw;            /* Nbw: number of blocks that can be written using one Update command */
    UINT16          nmaxb;          /* Nmaxb: maximum number of blocks available for NDEF data */
    UINT8           writef;         /* WriteFlag: 00h if writing data finished; 0Fh if writing data in progress */
    UINT8           rwflag;         /* RWFlag: 00h NDEF is read-only; 01h if read/write available */
    UINT32          ln;
    UINT8           *p_buf;         /* Current contents for READs */

    /* Scratch NDEF buffer (for update NDEF commands) */
    UINT8           scratch_writef;
    UINT32          scratch_ln;
    UINT8           *p_scratch_buf; /* Scratch buffer for WRITE/readback */
} tCE_T3T_NDEF_INFO;

/* Type 3 Tag current command processing */
typedef struct {
    UINT16          service_code_list[T3T_MSG_SERVICE_LIST_MAX];
    UINT8           *p_block_list_start;
    UINT8           *p_block_data_start;
    UINT8           num_services;
    UINT8           num_blocks;
} tCE_T3T_CUR_CMD;

/* Type 3 Tag control blcok */
typedef struct
{
    UINT8               state;
    UINT16              system_code;
    UINT8               local_nfcid2[NCI_RF_F_UID_LEN];
    UINT8               local_pmm[NCI_T3T_PMM_LEN];
    tCE_T3T_NDEF_INFO   ndef_info;
    tCE_T3T_CUR_CMD     cur_cmd;
} tCE_T3T_MEM;

/* CE Type 4 Tag control blocks */
typedef struct
{
    UINT8               aid_len;
    UINT8               aid[NFC_MAX_AID_LEN];
    tCE_CBACK          *p_cback;
} tCE_T4T_REG_AID;      /* registered AID table */

typedef struct
{
    TIMER_LIST_ENT      timer;              /* timeout for update file              */
    UINT8               cc_file[T4T_FC_TLV_OFFSET_IN_CC + T4T_FILE_CONTROL_TLV_SIZE];
    UINT8              *p_ndef_msg;         /* storage of NDEF message              */
    UINT16              nlen;               /* current size of NDEF message         */
    UINT16              max_file_size;      /* size of storage + 2 bytes for NLEN   */
    UINT8              *p_scratch_buf;      /* temp storage of NDEF message for update */

#define CE_T4T_STATUS_T4T_APP_SELECTED      0x01    /* T4T CE App is selected       */
#define CE_T4T_STATUS_REG_AID_SELECTED      0x02    /* Registered AID is selected   */
#define CE_T4T_STATUS_CC_FILE_SELECTED      0x04    /* CC file is selected          */
#define CE_T4T_STATUS_NDEF_SELECTED         0x08    /* NDEF file is selected        */
#define CE_T4T_STATUS_NDEF_FILE_READ_ONLY   0x10    /* NDEF is read-only            */
#define CE_T4T_STATUS_NDEF_FILE_UPDATING    0x20    /* NDEF is updating             */
#define CE_T4T_STATUS_WILDCARD_AID_SELECTED 0x40    /* Wildcard AID selected        */

    UINT8               status;

    tCE_CBACK          *p_wildcard_aid_cback;               /* registered wildcard AID callback */
    tCE_T4T_REG_AID     reg_aid[CE_T4T_MAX_REG_AID];        /* registered AID table             */
    UINT8               selected_aid_idx;
} tCE_T4T_MEM;


/* CE memory control blocks */
typedef struct
{
    tCE_T3T_MEM         t3t;
    tCE_T4T_MEM         t4t;
} tCE_MEM;

/* CE control blocks */
typedef struct
{
    tCE_MEM             mem;
    tCE_CBACK           *p_cback;
    UINT8               *p_ndef;     /* the memory starting from NDEF */
    UINT16              ndef_max;    /* max size of p_ndef */
    UINT16              ndef_cur;    /* current size of p_ndef */
    tNFC_RF_TECH        tech;
    UINT8               trace_level;

} tCE_CB;

/*
** CE Type 4 Tag Definition
*/

/* Max data size using a single ReadBinary. 2 bytes are for status bytes */
#define CE_T4T_MAX_LE           (NFC_CE_POOL_BUF_SIZE - BT_HDR_SIZE - NCI_MSG_OFFSET_SIZE - NCI_DATA_HDR_SIZE - T4T_RSP_STATUS_WORDS_SIZE)

/* Max data size using a single UpdateBinary. 6 bytes are for CLA, INS, P1, P2, Lc */
#define CE_T4T_MAX_LC           (NFC_CE_POOL_BUF_SIZE - BT_HDR_SIZE - NCI_DATA_HDR_SIZE - T4T_CMD_MAX_HDR_SIZE)

/*****************************************************************************
**  EXTERNAL FUNCTION DECLARATIONS
*****************************************************************************/
#ifdef __cplusplus
extern "C" {
#endif

#if 0
/* Global NFC data */
NFC_API extern tCE_CB  ce_cb;

extern void ce_init (void);

/* ce_t3t internal functions */
void ce_t3t_init (void);
tNFC_STATUS ce_select_t3t (UINT16 system_code, UINT8 nfcid2[NCI_RF_F_UID_LEN]);

/* ce_t4t internal functions */
extern tNFC_STATUS ce_select_t4t (void);
extern void ce_t4t_process_timeout (TIMER_LIST_ENT *p_tle);
#endif

#ifdef __cplusplus
}
#endif

#endif /* CE_INT_H_ */
