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
 *  This file contains the Near Field Communication (NFC) Tags related
 *  definitions from the specification.
 *
 ******************************************************************************/

#ifndef TAGS_DEFS_H
#define TAGS_DEFS_H

#if 0
#else
#include "data_types.h"
#endif

/* Manufacturer ID */
#define TAG_BRCM_MID            0x2E  /* BROADCOM CORPORATION                           */
#define TAG_MIFARE_MID          0x04  /* MIFARE                                         */
#define TAG_KOVIO_MID           0x37  /* KOVIO                                          */
#define TAG_INFINEON_MID        0x05  /* Infineon Technologies                          */

/* TLV types present in Type1 and Type 2 Tags */
#define TAG_NULL_TLV            0     /* May be used for padding. SHALL ignore this     */
#define TAG_LOCK_CTRL_TLV       1     /* Defines details of the lock bytes              */
#define TAG_MEM_CTRL_TLV        2     /* Identifies reserved memory areas               */
#define TAG_NDEF_TLV            3     /* Contains the NDEF message                      */
#define TAG_PROPRIETARY_TLV     0xFD  /* Tag proprietary information                    */
#define TAG_TERMINATOR_TLV      0xFE  /* Last TLV block in the data area                */
#define TAG_BITS_PER_BYTE       0x08  /* Number of bits in every tag byte               */
#define TAG_MAX_UID_LEN         0x0A  /* Max UID Len of type 1 and type 2 tag           */

#define TAG_LONG_NDEF_LEN_FIELD_BYTE0   0xFF  /* Byte 0 Length field to indicate LNDEF  */
#define TAG_DEFAULT_TLV_LEN             3     /* Tlv len for LOCK_CTRL/MEM TLV per spec */

/* Type 1 Tag related definitions */

#define T1T_STATIC_BLOCKS       0x0F  /* block 0 to Block E                             */
#define T1T_BLOCK_SIZE          0x08  /* T1T Block size in bytes                        */

#define T1T_STATIC_SIZE         T1T_STATIC_BLOCKS * T1T_BLOCK_SIZE /* Static Tag size   */

#define T1T_SEGMENT_SIZE        0x80  /* Size of Type 1 Tag segment in bytes            */
#define T1T_MAX_SEGMENTS        0x10  /* Maximum segment supported by Type 1 Tag        */
#define T1T_BLOCKS_PER_SEGMENT  0x10  /* Number of blocks present in a segment          */
#define T1T_OTP_LOCK_RES_BYTES  0x10  /* No.of default OTP,staticlocks,res bytes in tag */

#define T1T_STATIC_HR0          0x11  /* HRO value to indicate static Tag               */
#define T1T_DYNAMIC_HR0         0x12  /* 0x1y, as long as (y!=1)                        */
#define T1T_NDEF_SUPPORTED      0x10  /* HR0 value is 0x1y, indicates NDEF supported    */
#define T1T_HR1                 0x00  /* should be ignored                              */
#define T1T_UID_BLOCK           0x00  /* UID block                                      */
#define T1T_RES_BLOCK           0x0D  /* Reserved block                                 */
#define T1T_LOCK_BLOCK          0x0E  /* Static lock block                              */
#define T1T_MID_OFFSET          0x06  /* Manufacturer ID offset                         */
#define T1T_STATIC_RES_OFFSET   0x68  /* Reserved bytes offset                          */
#define T1T_LOCK_0_OFFSET       0x70  /* Static lock offset                             */
#define T1T_LOCK_1_OFFSET       0x71  /* Static lock offset                             */
#define T1T_DYNAMIC_LOCK_OFFSET 0x78  /* Block F - typically used for dynamic locks     */
#define T1T_DYNAMIC_LOCK_BYTES  0x08

#define T1T_RES_BYTE_LEN        1     /* the len of reserved byte in T1T block 0        */

/* Capability Container definitions */
#define T1T_CC_BLOCK            1     /* Capability container block                     */
#define T1T_CC_LEN              4     /* the len of CC used in T1T tag                  */
/* CC offset */
#define T1T_CC_NMN_OFFSET       0x00  /* Offset for NDEF magic number in CC             */
#define T1T_CC_VNO_OFFSET       0x01  /* Offset for Version number in CC                */
#define T1T_CC_TMS_OFFSET       0x02  /* Offset for Tag memory size in CC               */
#define T1T_CC_RWA_OFFSET       0x03  /* Offset for Read/Write access in CC             */
#define T1T_CC_NMN_BYTE         0x08  /* NDEF Magic Number byte number                  */
#define T1T_CC_VNO_BYTE         0x09  /* Version Number byte number                     */
#define T1T_CC_TMS_BYTE         0x0A  /* Tag Memory Size byte number                    */
#define T1T_CC_RWA_BYTE         0x0B  /* Read Write Access byte number                  */
#define T1T_CC_NMN              0xE1  /* NDEF Magic Number                              */
#define T1T_CC_LEGACY_VNO       0x10  /* Supported Legacy Version                       */
#define T1T_CC_VNO              0x11  /* Version Number                                 */
#define T1T_CC_TMS_STATIC       0x0E  /* TMS static memory - (8 * (n+1)).               */
#define T1T_CC_RWA_RW           0x00  /* RWA - Read/write allowed                       */
#define T1T_CC_RWA_RO           0x0F  /* RWA - Read only                                */

#define T1T_TAG_NULL            0     /* May be used for padding. SHALL ignore this     */
#define T1T_TAG_LOCK_CTRL       1     /* Defines details of the lock bytes              */
#define T1T_TAG_MEM_CTRL        2     /* Identifies reserved memory areas               */
#define T1T_TAG_NDEF            3     /* Contains the NDEF message                      */
#define T1T_TAG_PROPRIETARY     0xFD  /* Tag proprietary information                    */
#define T1T_TAG_TERMINATOR      0xFE  /* Last TLV block in the data area                */

#define T1T_DEFAULT_TLV_LEN     3     /* Tlv len for LOCK_CTRL/MEM TLV per spec         */
#define T1T_TLV_TYPE_LEN        1     /* Tlv type identifier len                        */
#define T1T_DEFAULT_TLV_LEN_FIELD_LEN   1     /* Length field size of  lock/mem tlv     */

#define T1T_HR_LEN              2     /* the len of HR used in Type 1 Tag               */
#define T1T_CMD_UID_LEN         4     /* the len of UID used in Type 1 Tag Commands     */
#define T1T_UID_LEN             7     /* the len of UID used in Type 1 Tag              */
#define T1T_ADD_LEN             1

#define T1T_SHORT_NDEF_LEN_FIELD_LEN    1 /* Length Field size of short NDEF Message    */
#define T1T_LONG_NDEF_LEN_FIELD_LEN     3 /* Length Field size of Long NDEF Message     */
#define T1T_LONG_NDEF_LEN_FIELD_BYTE0   0xFF /* Byte 0 in Length field to indicate LNDEF*/
#define T1T_LONG_NDEF_MIN_LEN           0x00FF /* Min. len of NDEF to qualify as LNDEF  */

/* Type 1 Tag Commands (7 bits) */
#define T1T_CMD_RID             0x78    /* read id                                      */
#define T1T_CMD_RALL            0x00    /* read all bytes                               */
#define T1T_CMD_READ            0x01    /* read (1 byte)                                */
#define T1T_CMD_WRITE_E         0x53    /* write with erase (1 byte)                    */
#define T1T_CMD_WRITE_NE        0x1A    /* write no erase (1 byte)                      */
/* dynamic memory only */
#define T1T_CMD_RSEG            0x10    /* read segment                                 */
#define T1T_CMD_READ8           0x02    /* read (8 byte)                                */
#define T1T_CMD_WRITE_E8        0x54    /* write with erase (8 byte)                    */
#define T1T_CMD_WRITE_NE8       0x1B    /* write no erase (8 byte)                      */

/* Lock */
#define T1T_NUM_STATIC_LOCK_BYTES           2   /* Number of static lock bytes in tag   */
#define T1T_BYTES_LOCKED_BY_STATIC_LOCK_BIT 4   /* Bytes locked by one static lock bit  */


/* Type 2 Tag related definitions */
#define T2T_STATIC_MEM_STR      0
#define T2T_DYNAMIC_MEM_STR     1
#define T2T_STATIC_SIZE         64
#define T2T_STATIC_BLOCKS       16      /* block 0 to Block 15 */
#define T2T_BLOCK_SIZE          4
#define T2T_HEADER_BLOCKS       4
#define T2T_HEADER_SIZE         16
#define T2T_SECTOR_SIZE         1024
#define T2T_BLOCKS_PER_SECTOR   0x100

#define T2T_UID_LEN             4     /* the len of UID used in T2T tag */
#define T2T_BLOCK0_UID_LEN      3     /* the len of UID in Block 0 of T2T tag */
#define T2T_BCC0_LEN            1     /* the len of BCC0 of T2T tag */
#define T2T_BLOCK1_UID_LEN      4     /* the len of UID in Block 1 of T2T tag */
#define T2T_BCC1_LEN            1     /* the len of BCC0 of T2T tag */
#define T2T_SNO_LEN             4     /* the len of Serial number used in T2T tag */
#define T2T_INTERNAL_BYTES_LEN  2     /* the len of internal used in T2T tag */
#define T2T_STATIC_LOCK_LEN     2     /* the len of static lock used in T2T tag */
/* Static Lock Bytes */
#define T2T_STATIC_LOCK0        0x0A  /* Static Lock 0 offset */
#define T2T_STATIC_LOCK1        0x0B  /* Static Lock 1 offset */

#define T2T_CC_LEN              4     /* the len of CC used in T2T tag                  */

/* Capability Container definitions */
#define T2T_CC_BLOCK		    0x03  /* Capability container block */
#define T2T_CC0_NMN_BYTE        0x0C  /* NDEF Magic Number byte number */
#define T2T_CC1_VNO_BYTE        0x0D  /* Version Number byte number*/
#define T2T_CC2_TMS_BYTE        0x0E  /* Tag Memory Size byte number */
#define T2T_CC3_RWA_BYTE        0x0F  /* Read Write Access byte number */
#define T2T_DATA_MEM            0x10  /* Data Memory */

#define T2T_CC0_NMN             0xE1  /* NDEF Magic Number */
#define T2T_CC1_VNO             0x11  /* Version Number */
#define T2T_CC1_LEGACY_VNO      0x10  /* Legacy Version Number */
#define T2T_CC1_NEW_VNO         0x12  /* Another supported Version Number */
#define T2T_CC2_TMS_STATIC      0x06  /* TMS static memory - (4 * (n+1)). */
#define T2T_CC3_RWA_RW          0x00  /* RWA - Read/write allowed */
#define T2T_CC3_RWA_RO          0x0F  /* RWA - Read only */

#define T2T_TMS_TAG_FACTOR      0x08  /* Factor to multiply to get tag data size from TMS */
#define T2T_DEFAULT_LOCK_BLPB   0x08  /* Bytes locked per lock bit of default locks */

/* Type 2 Tag Commands  */
#define T2T_CMD_READ            0x30    /* read  4 blocks (16 bytes) */
#define T2T_CMD_WRITE           0xA2    /* write 1 block  (4 bytes)  */
#define T2T_CMD_SEC_SEL         0xC2    /* Sector select             */
#define T2T_RSP_ACK			    0xA
#define T2T_RSP_NACK5		    0x5
#define T2T_RSP_NACK1           0x1     /* Nack can be either 1    */

#define T2T_FIRST_DATA_BLOCK    4
#define T2T_READ_BLOCKS         4
#define T2T_BLOCK_LEN           4
#define T2T_READ_DATA_LEN       (T2T_BLOCK_LEN * T2T_READ_BLOCKS)
#define T2T_WRITE_DATA_LEN      4


/* Type 2 TLV definitions */
#define T2T_TLV_TYPE_NULL         0     /* May be used for padding. SHALL ignore this */
#define T2T_TLV_TYPE_LOCK_CTRL    1     /* Defines details of the lock bytes */
#define T2T_TLV_TYPE_MEM_CTRL     2     /* Identifies reserved memory areas */
#define T2T_TLV_TYPE_NDEF         3     /* Contains the NDEF message */
#define T2T_TLV_TYPE_PROPRIETARY  0xFD  /* Tag proprietary information */
#define T2T_TLV_TYPE_TERMINATOR   0xFE  /* Last TLV block in the data area */


#define T2T_TLEN_LOCK_CTRL_TLV    3      /* Tag len for LOCK_CTRL TLV per spec */
#define T2T_TLEN_MEM_CTRL_TLV     3      /* Tag len for MEM_CTRL TLV per spec */

#define T2T_MAX_SECTOR            2      /* Maximum number of sectors supported */

#define T2T_TLV_TYPE_LEN                1     /* Tlv type identifier len                */

#define T2T_DEFAULT_TLV_LEN             3 /* Tlv len for LOCK_CTRL/MEM TLV per spec     */
#define T2T_SHORT_NDEF_LEN_FIELD_LEN    1 /* Length Field size of short NDEF Message    */
#define T2T_LONG_NDEF_LEN_FIELD_LEN     3 /* Length Field size of Long NDEF Message     */
#define T2T_LONG_NDEF_LEN_FIELD_BYTE0   0xFF /* Byte 0 in Length field to indicate LNDEF*/
#define T2T_LONG_NDEF_MIN_LEN           0x00FF /* Min. len of NDEF to qualify as LNDEF  */

/* Lock */
#define T2T_NUM_STATIC_LOCK_BYTES           2   /* Number of static lock bytes in tag   */
#define T2T_BYTES_LOCKED_BY_STATIC_LOCK_BIT 4   /* Bytes locked by one static lock bit  */

#define T2T_CC2_TMS_MUL           0x06
#define T2T_CC2_TMS_MULC          0x12
/*
**
**  Type 3 Tag Definitions
**
*/

#define T3T_SYSTEM_CODE_NDEF        0x12FC  /* System Code for NDEF tags */
#define T3T_SYSTEM_CODE_FELICA_LITE 0x88B4  /* System Code for felica-lite tags */
#define T3T_MAX_SYSTEM_CODES        16
#define T3T_FELICALITE_NMAXB        13      /* Maximum number of blocks for NDEF message for Felica Lite tags */

/* Block descriptor, used to describe a block to check/update */
typedef struct
{
    UINT16 service_code;    /* Block service code. Set to T3T_SERVICE_CODE_NDEF (0x000B) for NDEF data */
    UINT16 block_number;    /* Block number */
} tT3T_BLOCK_DESC;

/* Poll RC (request code) definitions */
#define T3T_POLL_RC_NONE    0   /* No RD requested in SENSF_RES */
#define T3T_POLL_RC_SC      1   /* System code requested in SENSF_RES */
#define T3T_POLL_RC_COMM    2   /* Avanced protocol features requested in SENSF_RES */
typedef UINT8 tT3T_POLL_RC;

/* Definitions for constructing t3t command messages */

/* NFC Forum / Felica commands */
#define T3T_MSG_OPC_CHECK_CMD   0x06
#define T3T_MSG_OPC_CHECK_RSP   0x07
#define T3T_MSG_OPC_UPDATE_CMD  0x08
#define T3T_MSG_OPC_UPDATE_RSP  0x09

/* Felica commands (not specified in NFC-Forum Type 3 tag specifications) */
#define T3T_MSG_OPC_POLL_CMD            0x00
#define T3T_MSG_OPC_POLL_RSP            0x01
#define T3T_MSG_OPC_REQ_SERVICE_CMD     0x02
#define T3T_MSG_OPC_REQ_SERVICE_RSP     0x03
#define T3T_MSG_OPC_REQ_RESPONSE_CMD    0x04
#define T3T_MSG_OPC_REQ_RESPONSE_RSP    0x05
#define T3T_MSG_OPC_REQ_SYSTEMCODE_CMD  0x0C
#define T3T_MSG_OPC_REQ_SYSTEMCODE_RSP  0x0D

#define T3T_MSG_NDEF_SC_RO          0x000B      /* Service code: read-only NDEF */
#define T3T_MSG_NDEF_SC_RW          0x0009      /* Service code: read/write NDEF */
#define T3T_MSG_NDEF_VERSION        0x10        /* NDEF Mapping Version 1.0 */
#define T3T_MSG_NDEF_WRITEF_OFF     0x00
#define T3T_MSG_NDEF_WRITEF_ON      0x0F
#define T3T_MSG_NDEF_RWFLAG_RO      0x00
#define T3T_MSG_NDEF_RWFLAG_RW      0x01
#define T3T_MSG_NDEF_ATTR_INFO_SIZE 14          /* Size of NDEF attribute info block (minus checksum) */

#define T3T_MSG_OFFSET_IDM                          1       /* offset of Manufacturer ID in UPDATE/CHECK messages */
#define T3T_MSG_OFFSET_NUM_SERVICES                 9       /* offset of Number of Services parameter in UPDATE/CHECK messages */
#define T3T_MSG_OFFSET_SERVICE_CODE_LIST            10      /* offset of Service Code List parameter in UPDATE/CHECK messages */
#define T3T_MSG_MASK_TWO_BYTE_BLOCK_DESC_FORMAT     0x80    /* len flag for Block List Element */
#define T3T_MSG_SERVICE_LIST_MASK                   0x0F    /* service code list mask */
#define T3T_MSG_SERVICE_LIST_MAX                    16

#define T3T_MSG_NUM_SERVICES_UPDATE_MAX             12      /* Max Number of Services per UPDATE command */
#define T3T_MSG_NUM_SERVICES_CHECK_MAX              15      /* Max Number of Services per CHECK command */
#define T3T_MSG_NUM_BLOCKS_UPDATE_MAX               13      /* Max Number of Blocks per UPDATE command */
#define T3T_MSG_NUM_BLOCKS_CHECK_MAX                15      /* Max Number of Blocks per CHECK command */

#define T3T_MSG_BLOCKSIZE                           16      /* Data block size for UPDATE and CHECK commands */

/* Common header definitions for T3t commands */
#define T3T_MSG_CMD_COMMON_HDR_LEN          11      /* Common header: SoD + cmdcode + NFCID2 + num_services */

/* Common header definition for T3t responses */
#define T3T_MSG_RSP_COMMON_HDR_LEN          11      /* Common header: rspcode + NFCID2 + StatusFlag1 + StatusFlag2  */
#define T3T_MSG_RSP_CHECK_HDR_LEN           (T3T_MSG_RSP_COMMON_HDR_LEN + 1)    /* Common header + NumBlocks */
#define T3T_MSG_RSP_OFFSET_RSPCODE          0       /* Offset for Response code */
#define T3T_MSG_RSP_OFFSET_IDM              1       /* Offset for Manufacturer ID */
#define T3T_MSG_RSP_OFFSET_STATUS1          9       /* Offset for Status Flag1 */
#define T3T_MSG_RSP_OFFSET_NUMBLOCKS        11      /* Offset for NumberOfBlocks (in CHECK response) */
#define T3T_MSG_RSP_OFFSET_CHECK_DATA       12      /* Offset for Block Data (in CHECK response) */
#define T3T_MSG_RSP_OFFSET_POLL_PMM         9       /* Offset for PMm (in POLL response) */
#define T3T_MSG_RSP_OFFSET_POLL_RD          17      /* Offset for RD (in POLL response) */
#define T3T_MSG_RSP_OFFSET_NUMSYS           9       /* Offset for Number of Systems */

#define T3T_MSG_RSP_STATUS_OK                       0x00
#define T3T_MSG_RSP_STATUS_ERROR                    0x01

#define T3T_MSG_RSP_STATUS2_ERROR_MEMORY            0x70
#define T3T_MSG_RSP_STATUS2_ERROR_EXCESSIVE_WRITES  0x71
#define T3T_MSG_RSP_STATUS2_ERROR_PROCESSING        0xFF

#define T3T_NFC_F_MAX_PAYLOAD_LEN                   0xFE    /* Maximum payload lenght for NFC-F messages (including SoD) */

/* Felica Lite defintions */
#define T3T_MSG_FELICALITE_BLOCK_ID_MC              0x88    /* Block ID for MC (memory configuration)                       */

#define T3T_MSG_FELICALITE_MC_OFFSET_MC_SP          0x00    /* Memory Configuration Block offset: MC_SP (Memory Configuration for scratch pad)   */
#define T3T_MSG_FELICALITE_MC_OFFSET_MC_ALL         0x02    /* Memory Configuration Block offset: MC_ALL (Memory Configuration for system block) */
#define T3T_MSG_FELICALITE_MC_OFFSET_SYS_OP         0x03    /* Memory Configuration Block offset: SYS_OP (System Option)                         */
#define T3T_MSG_FELICALITE_MC_OFFSET_RF_PRM         0x04    /* Memory Configuration Block offset: RF_PRM (Memory Configuration for RF Parameter) */



/*
**
**  Type 4 Tag Definitions
**
*/
#define T4T_CMD_MIN_HDR_SIZE            4       /* CLA, INS, P1, P2 */
#define T4T_CMD_MAX_HDR_SIZE            5       /* CLA, INS, P1, P2, Lc */

#define T4T_VERSION_2_0                 0x20    /* version 2.0 */
#define T4T_VERSION_1_0                 0x10    /* version 1.0 */
#define T4T_MY_VERSION                  T4T_VERSION_2_0
#define T4T_GET_MAJOR_VERSION(x)        ((x) >> 4)
#define T4T_GET_MINOR_VERSION(x)        ((x) & 0x0F )

#define T4T_CMD_CLASS                   0x00
#define T4T_CMD_INS_SELECT              0xA4
#define T4T_CMD_INS_READ_BINARY         0xB0
#define T4T_CMD_INS_UPDATE_BINARY       0xD6
#define T4T_CMD_DES_CLASS               0x90
#define T4T_CMD_INS_GET_HW_VERSION      0x60
#define T4T_CMD_CREATE_AID              0xCA
#define T4T_CMD_SELECT_APP              0x5A
#define T4T_CMD_CREATE_DATAFILE         0xCD
#define T4T_CMD_DES_WRITE               0x3D
#define T4T_CMD_P1_SELECT_BY_NAME       0x04
#define T4T_CMD_P1_SELECT_BY_FILE_ID    0x00
#define T4T_CMD_P2_FIRST_OR_ONLY_00H    0x00
#define T4T_CMD_P2_FIRST_OR_ONLY_0CH    0x0C

#define T4T_MAX_LENGTH_LE               0xFF    /* Max number of bytes to be read from file in ReadBinary Command */
#define T4T_MAX_LENGTH_LC               0xFF    /* Max number of bytes written to NDEF file in UpdateBinary Command */

#define T4T_RSP_STATUS_WORDS_SIZE       0x02

#define T4T_RSP_CMD_CMPLTED             0x9000
#define T4T_RSP_NOT_FOUND               0x6A82
#define T4T_RSP_WRONG_PARAMS            0x6B00
#define T4T_RSP_CLASS_NOT_SUPPORTED     0x6E00
#define T4T_RSP_WRONG_LENGTH            0x6700
#define T4T_RSP_INSTR_NOT_SUPPORTED     0x6D00
#define T4T_RSP_CMD_NOT_ALLOWED         0x6986

#define T4T_V10_NDEF_TAG_AID_LEN        0x07    /* V1.0 Type 4 Tag Applicaiton ID length */
#define T4T_V20_NDEF_TAG_AID_LEN        0x07    /* V2.0 Type 4 Tag Applicaiton ID length */

#define T4T_MIN_MLE                     0x000F  /* Min of Max R-APDU data size */

#define T4T_FILE_ID_SIZE                0x02
#define T4T_CC_FILE_ID                  0xE103
#define T4T_CC_FILE_MIN_LEN             0x000F

#define T4T_VERSION_OFFSET_IN_CC          0x02
#define T4T_FC_TLV_OFFSET_IN_CC           0x07
#define T4T_FC_WRITE_ACCESS_OFFSET_IN_TLV 0x07  /* Offset of Write access byte from type field in CC */

#define T4T_NDEF_FILE_CONTROL_TYPE      0x04    /* NDEF File Control Type */
#define T4T_PROP_FILE_CONTROL_TYPE      0x05    /* Proprietary File Control Type */

#define T4T_FILE_CONTROL_TLV_SIZE       0x08    /* size of T(1),L(1),V(6) for file control */
#define T4T_FILE_CONTROL_LENGTH         0x06    /* size of V(6) for file control */

#define T4T_FC_READ_ACCESS              0x00    /* read access granted without any security */
#define T4T_FC_WRITE_ACCESS             0x00    /* write access granted without any security */
#define T4T_FC_NO_WRITE_ACCESS          0xFF    /* no write access granted at all (read-only) */

#define T4T_FILE_LENGTH_SIZE            0x02
#define T4T_ADDI_FRAME_RESP             0xAFU
#define T4T_SIZE_IDENTIFIER_2K          0x16U
#define T4T_SIZE_IDENTIFIER_4K          0x18U
#define T4T_SIZE_IDENTIFIER_8K          0x1AU
#define T4T_DESEV1_MAJOR_VERSION        0x01U
#define T4T_TYPE_DESFIRE_EV1            0x01U
#define T4T_DESEV0_MAJOR_VERSION        0x00U
#define T4T_DESEV0_MINOR_VERSION        0x06U
#define T4T_DES_EV1_NFC_APP_ID          0x010000
#define T4T_DES_EV0_NFC_APP_ID          0x10EEEE

/*
**
**  ISO 15693 Tag Definitions
**
*/

/* Request flags 1 to 4 definition */
#define I93_FLAG_SUB_CARRIER_MASK           0x01    /* Sub_carrier_flag */
#define I93_FLAG_SUB_CARRIER_SINGLE         0x00    /* A single sub-carrier frequency shall be used by VICC */
#define I93_FLAG_SUB_CARRIER_DOUBLE         0x01    /* Two sub-carriers shall be used by VICC               */

#define I93_FLAG_DATA_RATE_MASK             0x02    /* Data_rate_flag */
#define I93_FLAG_DATA_RATE_LOW              0x00    /* Low data rate shall be used  */
#define I93_FLAG_DATA_RATE_HIGH             0x02    /* High data rate shall be used */

#define I93_FLAG_INVENTORY_MASK             0x04    /* Inventory_flag */
#define I93_FLAG_INVENTORY_UNSET            0x00    /* Flags 5 to 8 meaning is according to table 4 */
#define I93_FLAG_INVENTORY_SET              0x04    /* Flags 5 to 8 meaning is according to table 5 */

#define I93_FLAG_PROT_EXT_MASK              0x08    /* Protocol_Extension_flag */
#define I93_FLAG_PROT_EXT_NO                0x00    /* No protocol format extension                         */
#define I93_FLAG_PROT_EXT_YES               0x08    /* Protocol format is extended. Reserved for future use */

/* Request flags 5 to 6 definition when inventory flag is not set */
#define I93_FLAG_SELECT_MASK                0x10    /* Select_flag */
#define I93_FLAG_SELECT_UNSET               0x00    /* Request shall be executed by any VICC according to the setting of Address_flag */
#define I93_FLAG_SELECT_SET                 0x10    /* Request shall be executed only by VICC in selected state */
                                                    /* The Address_flag shall be set to 0 and the UID field shall bot be included in the request */

#define I93_FLAG_ADDRESS_MASK               0x20    /* Address_flag */
#define I93_FLAG_ADDRESS_UNSET              0x00    /* Request is not addressed. UID field is not included. It shall be executed by any VICC */
#define I93_FLAG_ADDRESS_SET                0x20    /* Request is addressed. UID field is included. It shall be executed only by VICC */
                                                    /* whose UID matches the UID specified in the request */

/* Request flags 5 to 6 definition when inventory flag is set */
#define I93_FLAG_AFI_MASK                   0x10    /* AFI_flag */
#define I93_FLAG_AFI_NOT_PRESENT            0x00    /* AFI field is not present */
#define I93_FLAG_AFI_PRESENT                0x10    /* AFI field is present     */

#define I93_FLAG_SLOT_MASK                  0x20    /* Nb_slots_flag */
#define I93_FLAG_SLOT_16                    0x00    /* 16 slots */
#define I93_FLAG_SLOT_ONE                   0x20    /* 1 slot   */

/* Request flags 6 to 8 definition when inventory flag is set or not set */

#define I93_FLAG_OPTION_MASK                0x40    /* Option_flag */
#define I93_FLAG_OPTION_UNSET               0x00    /* Meaning is defined by the command description. */
                                                    /* It shall be set to 0 if not otherwise defined by command */
#define I93_FLAG_OPTION_SET                 0x40    /* Meaning is defined by the command description. */

/* Response flags */
#define I93_FLAG_ERROR_MASK                 0x01    /* Error_flag */
#define I93_FLAG_ERORR_NOT_DETECTED         0x00    /* No error                                           */
#define I93_FLAG_ERROR_DETECTED             0x01    /* Error detected, Error code is in the "Error" field */

/* Response error code */
#define I93_ERROR_CODE_NOT_SUPPORTED        0x01    /* The command is not supported, i.e. the request code is not recognized */
#define I93_ERROR_CODE_NOT_RECOGNIZED       0x02    /* The command is not recognized, for example: a format error occured    */
#define I93_ERROR_CODE_OPTION_NOT_SUPPORTED 0x03    /* The command option is not supported                                   */
#define I93_ERROR_CODE_NO_INFO              0x0F    /* Error with no information given or a specific error code is not supported */
#define I93_ERROR_CODE_BLOCK_NOT_AVAILABLE  0x10    /* The specific block is not available (doesn't exist)                   */
#define I93_ERROR_CODE_BLOCK_ALREADY_LOCKED 0x11    /* The specific block is already locked and thus cannot be locked again  */
#define I93_ERROR_CODE_BLOCK_LOCKED         0x12    /* The specific block is locked and its content cannot be changed        */
#define I93_ERROR_CODE_BLOCK_FAIL_TO_WRITE  0x13    /* The specific block is was not successfully programmed                 */
#define I93_ERROR_CODE_BLOCK_FAIL_TO_LOCK   0x14    /* The specific block is was not successfully locked                     */

#define I93_UID_BYTE_LEN                    8       /* UID length in bytes                  */
#define I93_DFS_UNSUPPORTED                 0x00    /* Data Storage Format is not supported */
#define I93_BLOCK_UNLOCKED                  0x00    /* Block is not locked                  */
#define I93_BLOCK_LOCKED                    0x01    /* Block is locked                      */

/* ISO 15693 Mandatory commands */
#define I93_CMD_INVENTORY                   0x01    /* Inventory  */
#define I93_CMD_STAY_QUIET                  0x02    /* Stay Quiet */

/* ISO 15693 Optional commands */
#define I93_CMD_READ_SINGLE_BLOCK           0x20    /* Read single block     */
#define I93_CMD_WRITE_SINGLE_BLOCK          0x21    /* Write single block    */
#define I93_CMD_LOCK_BLOCK                  0x22    /* Lock block            */
#define I93_CMD_READ_MULTI_BLOCK            0x23    /* Read multiple blocks  */
#define I93_CMD_WRITE_MULTI_BLOCK           0x24    /* Write multiple blocks */
#define I93_CMD_SELECT                      0x25    /* Select                */
#define I93_CMD_RESET_TO_READY              0x26    /* Reset to ready        */
#define I93_CMD_WRITE_AFI                   0x27    /* Wreite AFI            */
#define I93_CMD_LOCK_AFI                    0x28    /* Lock AFI              */
#define I93_CMD_WRITE_DSFID                 0x29    /* Write DSFID           */
#define I93_CMD_LOCK_DSFID                  0x2A    /* Lock DSFID            */
#define I93_CMD_GET_SYS_INFO                0x2B    /* Get system information             */
#define I93_CMD_GET_MULTI_BLK_SEC           0x2C    /* Get multiple block security status */

/* Information flags definition */
#define I93_INFO_FLAG_DSFID                 0x01    /* DSFID is supported and DSFID field is present */
#define I93_INFO_FLAG_AFI                   0x02    /* AFI is supported and AFI field is present     */
#define I93_INFO_FLAG_MEM_SIZE              0x04    /* VICC memory size field is present             */
#define I93_INFO_FLAG_IC_REF                0x08    /* IC reference field is present                 */

#define I93_MAX_BLOCK_LENGH                 32      /* Max block size in bytes */
#define I93_MAX_NUM_BLOCK                   256     /* Max number of blocks    */

/* ICODE Capability Container(CC) definition */
#define I93_ICODE_CC_MAGIC_NUMER            0xE1    /* magic number in CC[0]  */
#define I93_ICODE_CC_MAJOR_VER_MASK         0xC0    /* major version in CC[1] */
#define I93_ICODE_CC_MINOR_VER_MASK         0x30    /* minor version in CC[1] */
#define I93_ICODE_CC_READ_ACCESS_MASK       0x0C    /* read access condition in CC[1]        */
#define I93_ICODE_CC_READ_ACCESS_GRANTED    0x00    /* read access granted without security  */
#define I93_ICODE_CC_WRITE_ACCESS_MASK      0x03    /* write access condition in CC[1]       */
#define I93_ICODE_CC_WRITE_ACCESS_GRANTED   0x00    /* write access granted without security */
#define I93_ICODE_CC_READ_ONLY              0x03    /* write access not granted at all       */
#define I93_ICODE_CC_MBREAD_MASK            0x01    /* read multi block supported in CC[3]   */
#define I93_ICODE_CC_IPREAD_MASK            0x02    /* inventory page read supported in CC[3] */
#define I93_STM_CC_OVERFLOW_MASK            0x04    /* More than 2040 bytes are supported in CC[3] */

/* ICODE TLV type */
#define I93_ICODE_TLV_TYPE_NULL             0x00    /* NULL TLV         */
#define I93_ICODE_TLV_TYPE_NDEF             0x03    /* NDEF message TLV */
#define I93_ICODE_TLV_TYPE_PROP             0xFD    /* Proprietary TLV  */
#define I93_ICODE_TLV_TYPE_TERM             0xFE    /* Terminator TLV   */

/* UID Coding (UID Bit 64-57), First byte of ISO 15693 UID */
#define I93_UID_FIRST_BYTE                      0xE0

/* UID Coding (UID Bit 56-49), IC manufacturer code */
#define I93_UID_IC_MFG_CODE_STM                 0x02
#define I93_UID_IC_MFG_CODE_NXP                 0x04
#define I93_UID_IC_MFG_CODE_TI                  0x07

/* NXP, UID Coding of ICODE type (UID Bit 48-41) */
#define I93_UID_ICODE_SLI                       0x01    /* ICODE SLI, SLIX     */
#define I93_UID_ICODE_SLI_S                     0x02    /* ICODE SLI-S, SLIX-S */
#define I93_UID_ICODE_SLI_L                     0x03    /* ICODE SLI-L, SLIX-L */

#define I93_IC_REF_ICODE_SLI_L                  0x03    /* IC Reference for ICODE SLI-L */
#define I93_ICODE_IC_REF_MBREAD_MASK            0x02    /* read multi block supported check bit */

/* TI, UID Coding of product version (UID Bit 48-42) */
#define I93_UID_TAG_IT_HF_I_PRODUCT_ID_MASK     0xFE    /* upper 7 bits                     */
#define I93_UID_TAG_IT_HF_I_PLUS_INLAY          0x00    /* Tag-it HF-I Plus Inlay           */
#define I93_UID_TAG_IT_HF_I_PLUS_CHIP           0x80    /* Tag-it HF-I Plus Chip            */
#define I93_UID_TAG_IT_HF_I_STD_CHIP_INLAY      0xC0    /* Tag-it HF-I Standard Chip/Inlyas */
#define I93_UID_TAG_IT_HF_I_PRO_CHIP_INLAY      0xC4    /* Tag-it HF-I Pro Chip/Inlyas      */

#define I93_TAG_IT_HF_I_STD_CHIP_INLAY_NUM_TOTAL_BLK   11
#define I93_TAG_IT_HF_I_PRO_CHIP_INLAY_NUM_TOTAL_BLK   12

#define I93_TAG_IT_HF_I_STD_PRO_CHIP_INLAY_BLK_SIZE         4
#define I93_TAG_IT_HF_I_STD_PRO_CHIP_INLAY_NUM_USER_BLK     8
#define I93_TAG_IT_HF_I_STD_PRO_CHIP_INLAY_AFI_LOCATION    40   /* LSB in Block 0x0A */

/* STM, product version (IC manufacturer code) */
#define I93_IC_REF_STM_MASK                     0xFC    /* IC Reference mask for STM */
#define I93_IC_REF_STM_LRI1K                    0x40    /* IC Reference for LRI1K:      010000xx(b), blockSize: 4, numberBlocks: 0x20 */
#define I93_IC_REF_STM_LRI2K                    0x20    /* IC Reference for LRI2K:      001000xx(b), blockSize: 4, numberBlocks: 0x40 */
#define I93_IC_REF_STM_LRIS2K                   0x28    /* IC Reference for LRIS2K:     001010xx(b), blockSize: 4, numberBlocks: 0x40 */
#define I93_IC_REF_STM_LRIS64K                  0x44    /* IC Reference for LRIS64K:    010001xx(b), blockSize: 4, numberBlocks: 0x800 */
#define I93_IC_REF_STM_M24LR64_R                0x2C    /* IC Reference for M24LR64-R:  001011xx(b), blockSize: 4, numberBlocks: 0x800 */
#define I93_IC_REF_STM_M24LR04E_R               0x5A    /* IC Reference for M24LR04E-R: 01011010(b), blockSize: 4, numberBlocks: 0x80 */
#define I93_IC_REF_STM_M24LR16E_R               0x4E    /* IC Reference for M24LR16E-R: 01001110(b), blockSize: 4, numberBlocks: 0x200 */
#define I93_IC_REF_STM_M24LR64E_R               0x5E    /* IC Reference for M24LR64E-R: 01011110(b), blockSize: 4, numberBlocks: 0x800 */

#define I93_STM_BLOCKS_PER_SECTOR               32
#define I93_STM_MAX_BLOCKS_PER_READ             32

#endif /* TAGS_DEFS_H */
