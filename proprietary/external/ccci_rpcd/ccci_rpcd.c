/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

#include <stdio.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <sys/vfs.h>
#include <limits.h>
#include <errno.h>
#include <string.h>
#include <dirent.h>
#include <wchar.h>
#include <linux/ioctl.h>
#include <ctype.h>
#include "hardware/ccci_intf.h"
#include <hardware_legacy/power.h>
#include <assert.h>
#define RPC_WAKE_LOCK_NAME "ccci_rpc"
#define RPC_WAKE_LOCK() acquire_wake_lock(PARTIAL_WAKE_LOCK, RPC_WAKE_LOCK_NAME)
#define RPC_WAKE_UNLOCK() release_wake_lock(RPC_WAKE_LOCK_NAME)

#define LOG_TAG "ccci_rpcd"

#include <cutils/log.h>

#include "ccci_rpcd.h"

//#define dbg_printf LOGD

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "ccci_rpcd",__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "ccci_rpcd",__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "ccci_rpcd",__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "ccci_rpcd",__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "ccci_rpcd",__VA_ARGS__)

#ifdef STREAM_SUPPORT
static int stream_support = 0;
static int DeviceFd = 0;
static unsigned int RPC_MAX_BUF_SIZE = 2048;
static unsigned int RPC_MAX_ARG_NUM = 6;
#else

#endif
static int	   md_id = 0;
static RPC_INFO g_RpcInfo;

static bool RPC_GetPackInfo(RPC_PACKET_INFO* pPackInfo, unsigned char* pData)
{
	unsigned int PackNum = *((unsigned int*)pData);
	unsigned int Index = 0;
	unsigned int DataLength = 0;
	unsigned int i;

	if(PackNum > RPC_MAX_ARG_NUM)
		return false;

	Index = sizeof(unsigned int);
	for(i = 0; i < PackNum; i++)
	{
		pPackInfo[i].Length = *((unsigned int*)(pData + Index));
		Index += sizeof(unsigned int);
		pPackInfo[i].pData = (pData + Index);
		//4 byte alignment
		Index += ((pPackInfo[i].Length+3)>>2)<<2;
	}

	if(Index > RPC_MAX_BUF_SIZE)
		return false;
	
	return true;
}
#ifdef STREAM_SUPPORT
// 5 = CCCI header + Operation ID
unsigned int g_bak[5];

/*
 * @brief Prepare a packet buffer for sending to MD
 * @param
 *     pData [in] A pointer to argument data for sending
 *     data_to_send [in] Size in bytes of argument data to send
 *     ccci_src [in] The pointer to the CCCI header for every sub-packet
 *     op_id [in] Operation ID currently used
 *     again [in] A flag means if we need to set "send again indicator"
 * @return
 *     On success, a pointer to arguments data is returned.
 *     On error, NULL is returned.
 */
void *RPC_PreparePktEx(char *pData, unsigned int data_to_send, CCCI_BUFF_T *ccci_src, unsigned int op_id, unsigned int again)
{
    char *bak_ptr = NULL;
    STREAM_DATA *stream = NULL;

    assert(data_to_send <= MAX_RPC_PKT_BYTE);
    assert(pData != NULL && ccci_src != NULL);
    assert(pData - (sizeof(CCCI_BUFF_T) + sizeof(unsigned int)) >= ccci_src);
		assert(sizeof(g_bak) == (sizeof(CCCI_BUFF_T) + sizeof(unsigned int))); 
	// move pointer forward to fill in CCCI header, this will replace orignal data there, so we backup them first
    bak_ptr = (char *)(pData - (sizeof(CCCI_BUFF_T) + sizeof(unsigned int)));
    // backup partial data
    memcpy((void*)g_bak, bak_ptr, sizeof(g_bak));
    stream = (STREAM_DATA *)bak_ptr;
	// copy CCCI header from the very fist header of all sub-packets
    if (again)
        stream->header.data[0] = ccci_src->data[0] | CCCI_RPC_REQ_SEND_AGAIN;
    else
        stream->header.data[0] = ccci_src->data[0] & ~CCCI_RPC_REQ_SEND_AGAIN;
    stream->header.data[1] = data_to_send + sizeof(CCCI_BUFF_T) + sizeof(unsigned int);;
    stream->header.channel = ccci_src->channel;
    stream->header.reserved = ccci_src->reserved;
    stream->payload.rpc_ops_id = op_id;

    //LOGD("RPC_PreparePktEx() CCCI_H(0x%X)(0x%X)(0x%X)(0x%X), OP ID = 0x%X",
    //     stream->header.data[0], stream->header.data[1], stream->header.channel, stream->header.reserved,
    //     stream->payload.OperateID);
	
    return (void*)stream;
}

/*
 * @brief Determine the prepare data has done
 * @param
 *     pStream [in] A pointer returned from RPC_PreparePktEx()
 * @return
 *     None
 */
void RPC_PreparePktDone(void *pStream)
{
    assert(pStream != NULL);
    // Restore backuped data
    memcpy(pStream, (void*)g_bak, sizeof(g_bak));
}
#endif
static bool RPC_WriteToMD(int DeviceFd, int BufIndex, RPC_PACKET_INFO* pPacketSrc, unsigned int PacketNum)
{
	bool bRet = false;
	int ret;
	char* pData;
	unsigned int DataLength = 0, AlignLength;
	unsigned int i;
	rpc_stream_buffer_t *pRpcBuf;
	rpc_stream_msg_t stream_msg;		

#ifdef STREAM_SUPPORT
	int data_len = 0;
	int data_sent = 0;
	STREAM_DATA *buffer_slot;
	CCCI_BUFF_T *ccci_h = NULL;
	int sent = 0;
	void *pkt_ptr = NULL;
	int pkt_size = 0;
	int data_to_send = 0;		
	if(!stream_support) {
		pRpcBuf = (rpc_stream_msg_t *)((char *)g_RpcInfo.pRpcBuf + (RPC_MAX_BUF_SIZE + sizeof(rpc_stream_msg_t))*BufIndex);
	} else {
		buffer_slot = (STREAM_DATA *)((char *)g_RpcInfo.pRpcBuf + (RPC_MAX_BUF_SIZE + sizeof(STREAM_DATA))*BufIndex);
		pRpcBuf = &buffer_slot->payload;
		DataLength += sizeof(CCCI_BUFF_T);
		DataLength += sizeof(unsigned int); // size of operate ID field
	}
#else
	pRpcBuf = g_RpcInfo.pRpcBuf + BufIndex;
#endif
	pRpcBuf->rpc_ops_id = RPC_API_RESP_ID | pRpcBuf->rpc_ops_id;
	pData = pRpcBuf->buffer;

	*((unsigned int*)pData) = PacketNum;
		
	pData += sizeof(unsigned int);
	DataLength += sizeof(unsigned int);

	for(i = 0; i < PacketNum; i++)
	{
		if((DataLength + 2*sizeof(unsigned int) + pPacketSrc[i].Length) > RPC_MAX_BUF_SIZE)
		{
			LOGE("RPCD_WriteToMD: Stream buffer full!!\r\n");
			goto _Exit;
		}
		*((unsigned int*)pData) = pPacketSrc[i].Length;
		pData += sizeof(unsigned int);
		DataLength += sizeof(unsigned int);
			
		//4 byte aligned
		AlignLength = ((pPacketSrc[i].Length + 3) >> 2) << 2;
		DataLength += AlignLength;
			
		if(pData != pPacketSrc[i].pData)
			memcpy(pData, pPacketSrc[i].pData, pPacketSrc[i].Length);
						
		pData += AlignLength;
	}

	stream_msg.length = DataLength;
	stream_msg.index = BufIndex;
#ifdef STREAM_SUPPORT
	if(!stream_support) {
		msync(pRpcBuf, RPC_MAX_BUF_SIZE, MS_SYNC);
		ret = ioctl(DeviceFd, CCCI_RPC_IOCTL_SEND, &stream_msg);
		if(ret < 0) {
			LOGE("WriteToMD: [error]fail send RPC stream: %d \n", errno);
			return bRet;
		}
	} else {
		// data length excluding CCCI header and OP ID
        data_len = DataLength - sizeof(CCCI_BUFF_T) - sizeof(unsigned int);
        ccci_h = &buffer_slot->header;
				ccci_h->channel++; //Rx->Tx

        /* No fragment is needed */
        if (data_len <= MAX_RPC_PKT_BYTE) {
            pData = (char*)buffer_slot;
            // Clear "send again indicator"
            ccci_h->data[0] = ccci_h->data[0] & ~CCCI_RPC_REQ_SEND_AGAIN;
						ccci_h->data[1] = DataLength;
            ret = write(DeviceFd, pData, DataLength);
            if (ret != DataLength) {
                LOGE("Failed to write only one RPC packet(%d)!! (%d/%d)\n", DataLength, ret, errno);
                return bRet;
            }
            LOGD("Write %d bytes to slot %d, CCCI_H(0x%X)(0x%X)(0x%X)(0x%X)\n",
                 ret, BufIndex, ccci_h->data[0], ccci_h->data[1], ccci_h->channel, ccci_h->reserved);
        } else {
            /* Data fragment is needed */
            //LOGD("Big packet, need fragment.");
            pData = (char*)(&buffer_slot->payload.buffer);
            while ((int)(data_sent + sizeof(CCCI_BUFF_T) + sizeof(unsigned int)) < DataLength) {
                /* Moret than 2 packets to send */
                /* Each packet includes CCCI header, OP id, and data */
                if ((data_len - data_sent) > MAX_RPC_PKT_BYTE) {
                    data_to_send = MAX_RPC_PKT_BYTE;
                    pkt_ptr = RPC_PreparePktEx(pData, data_to_send, ccci_h, pRpcBuf->rpc_ops_id, 1);
                } else {
            		/* The last packet */
                    data_to_send = data_len - data_sent;
                    pkt_ptr = RPC_PreparePktEx(pData, data_to_send, ccci_h, pRpcBuf->rpc_ops_id, 0);
                }
                // Add CCCI header and operation ID size to packet size, be aware of that OP_ID is not cosindered as payload, so not counted in MAX_RPC_PKT_BYTE
                pkt_size = data_to_send + sizeof(CCCI_BUFF_T) + sizeof(unsigned int);
                // write size = data + CCCI header + OP ID
                ret = write(DeviceFd, pkt_ptr, pkt_size);
                if (ret != pkt_size) {
                    LOGE("Failed to write RPC packet !! (%d)\n", errno);
                    break;
                } else {
                    CCCI_BUFF_T *dst_ccci_h = (CCCI_BUFF_T *)pkt_ptr;
                    LOGD("Write %d bytes to slot %d, CCCI_H(0x%X)(0x%X)(0x%X)(0x%X)\n",
                         ret, BufIndex,
                         dst_ccci_h->data[0], dst_ccci_h->data[1], dst_ccci_h->channel, dst_ccci_h->reserved);
                }
                RPC_PreparePktDone(pkt_ptr);
                data_sent += data_to_send;
                pData += data_to_send;
            };
        }
		if (ret < 0) {
            LOGE("WriteToMD: [error]fail send RPC stream: %d \n", ret);
            return bRet;
        }
        //LOGD("write to MD %d\n", DataLength);
	}
#else
	msync(pRpcBuf, RPC_MAX_BUF_SIZE, MS_SYNC);
	bRet = ioctl(DeviceFd, CCCI_RPC_IOCTL_SEND, &stream_msg);
#endif
	bRet = true;
		
_Exit:
	return bRet;
}

/***************************************************************************
 * TC1 Section
 ***************************************************************************/

#include <dlfcn.h>
#include "tc1_partition.h"

// Function pointer ----------------------------------------------------------------------------
bool (*fp_RPC_TC1_FacWriteImei)(unsigned char imei_type, unsigned char *imei, bool needFlashProgram);
bool (*fp_RPC_TC1_FacReadImei)(unsigned char imei_type, unsigned char *imei);
bool (*fp_RPC_TC1_FacWriteSimLockType)(unsigned char simLockType, bool needFlashProgram);
bool (*fp_RPC_TC1_FacReadSimLockType)(unsigned char *simLockType);
bool (*fp_RPC_TC1_FacWriteNetworkCodeListNum)(unsigned short networkCodeListNum, bool needFlashProgram);
bool (*fp_RPC_TC1_FacReadNetworkCodeListNum)(unsigned short *networkCodeListNum);
bool (*fp_RPC_TC1_FacWriteUnlockCodeVerifyFailCount)(unsigned char failCount, bool needFlashProgram);
bool (*fp_RPC_TC1_FacReadUnlockCodeVerifyFailCount)(unsigned char *failCount);
bool (*fp_RPC_TC1_FacWriteUnlockFailCount)(unsigned char simLockType, unsigned char failCount, bool needFlashProgram);
bool (*fp_RPC_TC1_FacReadUnlockFailCount)(unsigned char simLockType, unsigned char *failCount);
bool (*fp_RPC_TC1_FacWriteUnlockCode)(FactoryUnlockCode *unlockCode, bool needFlashProgram);
bool (*fp_RPC_TC1_FacVerifyUnlockCode)(unsigned char simLockType, unsigned char *unlockCode, bool *isOk);
bool (*fp_RPC_TC1_FacCheckUnlockCodeValidness)(bool *isValid);
bool (*fp_RPC_TC1_FacWriteNetworkCode)(FactoryNetworkCode *networkCode, unsigned short networkCodeListNum, bool needFlashProgram);
bool (*fp_RPC_TC1_FacReadNetworkCode)(FactoryNetworkCode *networkCode, unsigned short networkCodeListNum);
bool (*fp_RPC_TC1_FacInitSimLockData)(void);
bool (*fp_RPC_TC1_FacReadFusgFlag)(unsigned char *fusgFlag);
bool (*fp_RPC_TC1_FacCheckNetworkCodeValidness)(unsigned char simLockType, bool *isValid);

// Load lib ---------------------------------------------------------------------------------------
void *tc1_lib = NULL;
int tc1_lib_ready = 0;
int init_tc1_func_pointer(void)
{
	char *error;
	tc1_lib = dlopen("/system/lib/libtc1part.so", RTLD_NOW);
	if(NULL == tc1_lib){
		do {
			LOGE("Load lib fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}
	/*bool (*fp_RPC_TC1_FacWriteImei)(bool isMaster, unsigned char *imei, bool needFlashProgram)*/
	fp_RPC_TC1_FacWriteImei = dlsym(tc1_lib, TC1_GET_NAME(FacWriteImei));
	error = dlerror();
	if(NULL != error){
		do {LOGE("Load TC1_FacWriteImei fail\n");
		    LOGE("Load %s\n", error);
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacReadImei)(bool isMaster, unsigned char *imei)*/
	fp_RPC_TC1_FacReadImei = dlsym(tc1_lib, TC1_GET_NAME(FacReadImei));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacReadImei fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacWriteSimLockType)(unsigned char simLockType, bool needFlashProgram)*/
	fp_RPC_TC1_FacWriteSimLockType = dlsym(tc1_lib, TC1_GET_NAME(FacWriteSimLockType));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacWriteSimLockType fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacReadSimLockType)(unsigned char *simLockType)*/
	fp_RPC_TC1_FacReadSimLockType = dlsym(tc1_lib, TC1_GET_NAME(FacReadSimLockType));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacReadSimLockType fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacWriteNetworkCodeListNum)(unsigned short networkCodeListNum, bool needFlashProgram)*/
	fp_RPC_TC1_FacWriteNetworkCodeListNum = dlsym(tc1_lib, TC1_GET_NAME(FacWriteNetworkCodeListNum));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacWriteNetworkCodeListNum fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacReadNetworkCodeListNum)(unsigned short *networkCodeListNum)*/
	fp_RPC_TC1_FacReadNetworkCodeListNum = dlsym(tc1_lib, TC1_GET_NAME(FacReadNetworkCodeListNum));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacReadNetworkCodeListNum fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacWriteUnlockCodeVerifyFailCount)(unsigned char failCount, bool needFlashProgram)*/
	fp_RPC_TC1_FacWriteUnlockCodeVerifyFailCount = dlsym(tc1_lib, TC1_GET_NAME(FacWriteUnlockCodeVerifyFailCount));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacWriteUnlockCodeVerifyFailCount fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacReadUnlockCodeVerifyFailCount)(unsigned char *failCount)*/
	fp_RPC_TC1_FacReadUnlockCodeVerifyFailCount = dlsym(tc1_lib, TC1_GET_NAME(FacReadUnlockCodeVerifyFailCount));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacReadUnlockCodeVerifyFailCount fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacWriteUnlockFailCount)(unsigned char simLockType, unsigned char failCount, bool needFlashProgram)*/
	fp_RPC_TC1_FacWriteUnlockFailCount = dlsym(tc1_lib, TC1_GET_NAME(FacWriteUnlockFailCount));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacWriteUnlockFailCount fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacReadUnlockFailCount)(unsigned char simLockType, unsigned char *failCount)*/
	fp_RPC_TC1_FacReadUnlockFailCount = dlsym(tc1_lib, TC1_GET_NAME(FacReadUnlockFailCount));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacReadUnlockFailCount fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacWriteUnlockCode)(FactoryUnlockCode *unlockCode, bool needFlashProgram)*/
	fp_RPC_TC1_FacWriteUnlockCode = dlsym(tc1_lib, TC1_GET_NAME(FacWriteUnlockCode));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacWriteUnlockCode fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacVerifyUnlockCode)(unsigned char simLockType, unsigned char *unlockCode, bool *isOk)*/
	fp_RPC_TC1_FacVerifyUnlockCode = dlsym(tc1_lib, TC1_GET_NAME(FacVerifyUnlockCode));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacVerifyUnlockCode fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacCheckUnlockCodeValidness)(bool *isValid)*/
	fp_RPC_TC1_FacCheckUnlockCodeValidness = dlsym(tc1_lib, TC1_GET_NAME(FacCheckUnlockCodeValidness));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacCheckUnlockCodeValidness fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacWriteNetworkCode)(FactoryNetworkCode *networkCode, unsigned short networkCodeListNum, bool needFlashProgram)*/
	fp_RPC_TC1_FacWriteNetworkCode = dlsym(tc1_lib, TC1_GET_NAME(FacWriteNetworkCode));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacWriteNetworkCode fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacReadNetworkCode)(FactoryNetworkCode *networkCode, unsigned short networkCodeListNum)*/
	fp_RPC_TC1_FacReadNetworkCode = dlsym(tc1_lib, TC1_GET_NAME(FacReadNetworkCode));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacReadNetworkCode fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacInitSimLockData)(void)*/
	fp_RPC_TC1_FacInitSimLockData = dlsym(tc1_lib, TC1_GET_NAME(FacInitSimLockData));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacInitSimLockData fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacReadFusgFlag(unsigned char *fusgFlag)*/
	fp_RPC_TC1_FacReadFusgFlag = dlsym(tc1_lib, TC1_GET_NAME(FacReadFusgFlag));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacReadFusgFlag fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	/*bool (*fp_RPC_TC1_FacCheckNetworkCodeValidness(unsigned char simLockType, bool  *isValid)*/
	fp_RPC_TC1_FacCheckNetworkCodeValidness = dlsym(tc1_lib, TC1_GET_NAME(FacCheckNetworkCodeValidness));
	error = dlerror();
	if(NULL != error){
		do{LOGE("Load TC1_FacCheckNetworkCodeValidness fail\n");
		 	sleep(2);
		} while(1);
		return -1;
	}

	return 0;
}

static bool RPC_TC1_FacWriteImei(unsigned char imei_type, unsigned char *imei, bool needFlashProgram)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacWriteImei(imei_type, imei, needFlashProgram);
}  
static bool RPC_TC1_FacReadImei(unsigned char imei_type, unsigned char *imei)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacReadImei(imei_type, imei);
}
static bool RPC_TC1_FacWriteSimLockType(unsigned char simLockType, bool needFlashProgram)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacWriteSimLockType(simLockType, needFlashProgram);
}
static bool RPC_TC1_FacReadSimLockType(unsigned char *simLockType)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacReadSimLockType(simLockType);
}
static bool RPC_TC1_FacWriteNetworkCodeListNum(unsigned short networkCodeListNum, bool needFlashProgram)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacWriteNetworkCodeListNum(networkCodeListNum, needFlashProgram);
}
static bool RPC_TC1_FacReadNetworkCodeListNum(unsigned short *networkCodeListNum)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacReadNetworkCodeListNum(networkCodeListNum);
}
static bool RPC_TC1_FacWriteUnlockCodeVerifyFailCount(unsigned char failCount, bool needFlashProgram)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacWriteUnlockCodeVerifyFailCount(failCount, needFlashProgram);
}
static bool RPC_TC1_FacReadUnlockCodeVerifyFailCount(unsigned char *failCount)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacReadUnlockCodeVerifyFailCount(failCount);
}
static bool RPC_TC1_FacWriteUnlockFailCount(unsigned char simLockType, unsigned char failCount, bool needFlashProgram)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacWriteUnlockFailCount(simLockType, failCount, needFlashProgram);
}
static bool RPC_TC1_FacReadUnlockFailCount(unsigned char simLockType, unsigned char *failCount)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacReadUnlockFailCount(simLockType, failCount);
}
static bool RPC_TC1_FacWriteUnlockCode(FactoryUnlockCode *unlockCode, bool needFlashProgram)
{
	if(!tc1_lib_ready){
		LOGD("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacWriteUnlockCode(unlockCode, needFlashProgram);
}
static bool RPC_TC1_FacVerifyUnlockCode(unsigned char simLockType, unsigned char *unlockCode, bool *isOK)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacVerifyUnlockCode(simLockType, unlockCode, isOK);
}
static bool RPC_TC1_FacCheckUnlockCodeValidness(bool *isValid)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacCheckUnlockCodeValidness(isValid);
}
static bool RPC_TC1_FacWriteNetworkCode(FactoryNetworkCode *networkCode, unsigned short networkCodeListNum, bool needFlashProgram)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacWriteNetworkCode(networkCode, networkCodeListNum, needFlashProgram);
}
static bool RPC_TC1_FacReadNetworkCode(FactoryNetworkCode *networkCode, unsigned short networkCodeListNum)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return -1;
	}
	return fp_RPC_TC1_FacReadNetworkCode(networkCode, networkCodeListNum);
}
static bool RPC_TC1_FacInitSimLockData(void)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return -1;
	}
	return fp_RPC_TC1_FacInitSimLockData();
}

static bool RPC_TC1_FacReadFusgFlag(unsigned char *fusgFlag)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacReadFusgFlag(fusgFlag);
}

static bool RPC_TC1_FacCheckNetworkCodeValidness(unsigned char simLockType, bool *isValid)
{
	if(!tc1_lib_ready){
		LOGE("TC1 Lib does not ready!!\n");
		return false;
	}
	return fp_RPC_TC1_FacCheckNetworkCodeValidness(simLockType, isValid);
}
static int exit_signal = 0;
void signal_treatment(int param)
{
	/*
	 * this signal catching design does NOT work...
	 * set property ctl.stop will send SIGKILL to ccci_rpcd(check service_stop_or_reset() in init.c),
	 * but SIGKILL is not catchable.
	 * kill pid will send SIGTERM to ccci_rpcd, we can catch this signal, but the process is just
	 * terminated, and no time for us to check exit_signal in main().
	 * per system team's comment, kernel will free all resource (memory get from malloc, etc.),
	 * so we do NOT need to take care of these.
	 */
	LOGD("signal number=%d\n", param);
	switch (param) {
	case SIGPIPE:
	case SIGHUP:
	case SIGINT:
	case SIGTERM:
	case SIGUSR1:
	case SIGUSR2:	
	case SIGALRM:
    case SIGKILL:
    default:
        exit_signal = param;
        break;
    }
}

int main(int argc, char *argv[])
{
#ifndef STREAM_SUPPORT
	int DeviceFd;
#endif
	int ReqBufIndex;
	rpc_stream_buffer_t *pRpcBuf;
	int PacketNum = 0;		
	int RetVal;
	char dev_node[32];
	int  using_old_ver = 0;
#ifdef STREAM_SUPPORT
	RPC_PACKET_INFO *PackInfo;
#else
	RPC_PACKET_INFO PackInfo[RPC_MAX_ARG_NUM];
#endif
	unsigned int Length;
#ifdef STREAM_SUPPORT
	CCCI_BUFF_T *ccci_h = NULL;
	char pkt_buff[MAX_RPC_BUF_BYTE] = {0};
	STREAM_DATA *stream = NULL; // data packet received from MD
	STREAM_DATA *buffer_slot = NULL; // local buffer slot
	char *p_rpc_buff = NULL;
#endif
	LOGD("ccci_rpcd Ver:v2.00, CCCI Ver:%d", ccci_get_version());
	//Check if input parameter is valid
	if(argc != 2) {
		md_id = 0;
		LOGE("[Warning]Parameter number not correct,use old version!\n");
		using_old_ver = 1;
		snprintf(dev_node, 32, "/dev/ccci_rpc");
	} else {
		if(strcmp(argv[1],"0")==0) {
			snprintf(dev_node, 32, "%s", ccci_get_node_name(USR_CCCI_RPC, MD_SYS1));
			md_id = 0;
		} else if(strcmp(argv[1],"1")==0) {
			snprintf(dev_node, 32, "%s", ccci_get_node_name(USR_CCCI_RPC, MD_SYS2));
			md_id =1;
#ifdef STREAM_SUPPORT
		} else if(strcmp(argv[1],"4")==0) {
			snprintf(dev_node, 32, "%s", ccci_get_node_name(USR_CCCI_RPC, MD_SYS5));
			md_id =4;
#endif
		} else {
			LOGD("Invalid md sys id(%d)!\n", md_id);
			return -1;
		}
	}
#ifdef STREAM_SUPPORT
	if(md_id==0 || md_id==1) {
		if(ccci_get_version() == ECCCI||ccci_get_version() == EDSDA)
			stream_support = 1;
		else
			stream_support = 0;
	} else if(md_id == 4) {
		stream_support = 1;
	}
#endif

	DeviceFd = open(dev_node, O_RDWR);
	if(DeviceFd == -1)
	{
		LOGE("Main: open ccci_rpc fail\r\n");
		return -1;
	}

#ifdef STREAM_SUPPORT
	if(!stream_support) {
		g_RpcInfo.pRpcBuf = mmap(NULL, sizeof(rpc_stream_buffer_t), PROT_READ | PROT_WRITE, MAP_SHARED, DeviceFd, 0);
	} else {
		int alloc_length = (sizeof(STREAM_DATA) + RPC_MAX_BUF_SIZE) * RPC_BUFFER_SLOT_NUM;
		g_RpcInfo.pRpcBuf = malloc(alloc_length);
		memset(g_RpcInfo.pRpcBuf, 0, alloc_length);
	}
	PackInfo = malloc(sizeof(RPC_PACKET_INFO) * RPC_MAX_ARG_NUM);
#else
	g_RpcInfo.pRpcBuf = mmap(NULL, sizeof(rpc_stream_buffer_t), PROT_READ | PROT_WRITE, MAP_SHARED, DeviceFd, 0);
#endif
	if(g_RpcInfo.pRpcBuf  == NULL)
	{
		LOGE("Main: mmap buffer fail\r\n");
		return -1;			
	}
	
	if(init_tc1_func_pointer() < 0)
		tc1_lib_ready = 0;
	else
		tc1_lib_ready = 1;
#ifdef STREAM_SUPPORT
	LOGD("register signal hadler\n");
	if(signal(SIGHUP, signal_treatment)==SIG_ERR)
		LOGE("can't catch SIGHUP\n");
	if(signal(SIGPIPE, signal_treatment)==SIG_ERR)
		LOGE("can't catch SIGPIPE\n");
	if(signal(SIGKILL, signal_treatment)==SIG_ERR)
		LOGE("can't catch SIGKILL\n");
	if(signal(SIGINT, signal_treatment)==SIG_ERR)
		LOGE("can't catch SIGINT\n");
	if(signal(SIGUSR1, signal_treatment)==SIG_ERR)
		LOGE("can't catch SIGUSR1\n");
	if(signal(SIGUSR2, signal_treatment)==SIG_ERR)
		LOGE("can't catch SIGUSR2\n");
	if(signal(SIGTERM, signal_treatment)==SIG_ERR)
		LOGE("can't catch SIGTERM\n");
	if(signal(SIGALRM, signal_treatment)==SIG_ERR)
		LOGE("can't catch SIGALRM\n");
	
	while(exit_signal == 0)
#else
	while(1)
#endif
	{
		PacketNum = 0;
retry:
#ifdef STREAM_SUPPORT
		if(!stream_support) {
			ReqBufIndex = ioctl(DeviceFd, CCCI_RPC_IOCTL_GET_INDEX, 0);
			RPC_WAKE_LOCK();

			if(ReqBufIndex < 0 || ReqBufIndex > RPC_REQ_BUFFER_MUN)
			{
				LOGE("Main: [error]fail get CCCI_RPC buffer index: %d \n", errno);
				RetVal = RPC_PARAM_ERROR;
				PackInfo[PacketNum].Length = sizeof(unsigned int);
				PackInfo[PacketNum++].pData = (void*) &RetVal;
				goto _Next;
			}
		
			pRpcBuf = (rpc_stream_buffer_t *)((char *)g_RpcInfo.pRpcBuf + (RPC_MAX_BUF_SIZE + sizeof(rpc_stream_buffer_t))*ReqBufIndex);
		} else {
			while (1) {
          memset(pkt_buff, 0, MAX_RPC_BUF_BYTE);
				// add an extra integer as MD consider OP_ID as not part of the "payload"
          RetVal = read(DeviceFd, pkt_buff, (MAX_RPC_PKT_BYTE+sizeof(CCCI_BUFF_T)+sizeof(unsigned int)));
          if (RetVal <= 0) {
              LOGE("Failed to read from RPC device (%d) !! errno = %d", RetVal, errno);
                  goto retry;
          } else {
              LOGD("Read %d bytes from RPC device", RetVal);
          }
					RPC_WAKE_LOCK();
          stream = (STREAM_DATA *)pkt_buff;
          ccci_h = (CCCI_BUFF_T *)&stream->header;
          ReqBufIndex = ccci_h->reserved;
          LOGD("Read %d bytes from slot %d, CCCI_H(0x%X)(0x%X)(0x%X)(0x%X)",
               RetVal, ReqBufIndex,
               ccci_h->data[0], ccci_h->data[1], ccci_h->channel, ccci_h->reserved);
	
					buffer_slot = (STREAM_DATA *)((char *)g_RpcInfo.pRpcBuf + (RPC_MAX_BUF_SIZE + sizeof(STREAM_DATA))*ReqBufIndex);
          p_rpc_buff = (char *)buffer_slot;
          /******************************************
           *
           *  FSM description for re-sent mechanism
           *   (ccci_rpc_buff_state == CCCI_RPC_BUFF_IDLE) ==> initial status & end status
           *   (ccci_rpc_buff_state == CCCI_RPC_BUFF_WAIT) ==> need to receive again
           *
           ******************************************/
          if (!CCCI_RPC_PEER_REQ_SEND_AGAIN(ccci_h)) {
              if (g_RpcInfo.rpc_buff_state[ReqBufIndex] == RPC_BUFF_IDLE) {
                  /* copy data memory and CCCI header */
                  memcpy(p_rpc_buff, ccci_h, ccci_h->data[1]);
                  /* don't need to update FS_Address */
              } else if (g_RpcInfo.rpc_buff_state[ReqBufIndex] == RPC_BUFF_WAIT) {
                  /* copy data memory and NULL, excluding CCCI header, OP id */
                  memcpy(p_rpc_buff + g_RpcInfo.rpc_buff_offset[ReqBufIndex],
                         stream->payload.buffer,
                         ccci_h->data[1] - sizeof(CCCI_BUFF_T) - sizeof(unsigned int));
                  /* update CCCI header info */
                  memcpy(p_rpc_buff, ccci_h, sizeof(CCCI_BUFF_T));
              } else {
                  /* No such rpc_buff_state state */
                  assert(0);
              }
              g_RpcInfo.rpc_buff_state[ReqBufIndex] = RPC_BUFF_IDLE;
              g_RpcInfo.rpc_buff_offset[ReqBufIndex] = 0;
          } else {
              if (g_RpcInfo.rpc_buff_state[ReqBufIndex] == RPC_BUFF_IDLE) {
                  /* only "OP id" and "data" size and "CCCI header" */
                  unsigned int length = ccci_h->data[1];
                  memcpy(p_rpc_buff, ccci_h, length);
                  g_RpcInfo.rpc_buff_offset[ReqBufIndex] += length;
              } else if (g_RpcInfo.rpc_buff_state[ReqBufIndex] == RPC_BUFF_WAIT) {
                  /* only "data" size, excluding CCCI header and OP id */
                  unsigned int length = ccci_h->data[1] - sizeof(CCCI_BUFF_T) - sizeof(unsigned int);
                  memcpy(p_rpc_buff + g_RpcInfo.rpc_buff_offset[ReqBufIndex],
                         stream->payload.buffer,
                         length);    /* CCCI_HEADER + RPC_OP_ID */
                  g_RpcInfo.rpc_buff_offset[ReqBufIndex] += length;
              } else {
                  /* No such ccci_rpc_buff_state state */
                  assert(0);
              }
              g_RpcInfo.rpc_buff_state[ReqBufIndex] = RPC_BUFF_WAIT;
          }
          if (g_RpcInfo.rpc_buff_state[ReqBufIndex] == RPC_BUFF_IDLE)
              break;
					RPC_WAKE_UNLOCK();
      }
			pRpcBuf = &buffer_slot->payload;
		}
		//LOGD("Main: operation ID = %x\n", pRpcBuf->OperateID);
#else		
		ReqBufIndex = ioctl(DeviceFd, CCCI_RPC_IOCTL_GET_INDEX, NULL);
		RPC_WAKE_LOCK();
		if(ReqBufIndex < 0 || ReqBufIndex >= CCCI_RPC_MAX_BUFFERS) // NOTE here!!!!!!!!!!!!!!!
		{
			LOGE("Main: Get CCCI_RPC buffer index fail\r\n");
			RetVal = RPC_PARAM_ERROR;
			PackInfo[PacketNum].Length = sizeof(unsigned int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;
			goto _Next;
		}

		pRpcBuf = g_RpcInfo.pRpcBuf  + ReqBufIndex;
#endif
		if(!RPC_GetPackInfo(PackInfo, pRpcBuf->buffer))
		{
			LOGE("Main: Fail to get packet info!! \r\n");
			RetVal = RPC_PARAM_ERROR;
			PackInfo[PacketNum].Length = sizeof(unsigned int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;
			goto _Next;
		}

		if(0==tc1_lib_ready)
		{
			LOGE("Main: Lib not ready!! \r\n");
			RetVal = RPC_PARAM_ERROR;
			PackInfo[PacketNum].Length = sizeof(unsigned int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;
			goto _Next;
		}
			
		switch(pRpcBuf->rpc_ops_id)
		{
		case RPC_CCCI_TC1_FAC_WRITE_IMEI:
			{
			bool		isMaster;
			unsigned char	*imeiStr;
			bool		needFlashProgram;
		
			isMaster = *(bool *)PackInfo[0].pData;
			imeiStr = (unsigned char *)PackInfo[1].pData;
			needFlashProgram = *(bool *)PackInfo[2].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_WRITE_IMEI <0x%x, %s, 0x%x>\n",
							isMaster, imeiStr, needFlashProgram);
		
			RetVal = RPC_TC1_FacWriteImei(isMaster, imeiStr, needFlashProgram);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	

			break;
			}
		case RPC_CCCI_TC1_FAC_READ_IMEI:
			{
			bool		isMaster;
			unsigned char	*imeiStr;
		
			isMaster = *(bool *)PackInfo[0].pData;
			imeiStr = (unsigned char *)(pRpcBuf->buffer + 4*sizeof(int));

			LOGD("Main: RPC_CCCI_TC1_FAC_READ_IMEI(in) <0x%x>\n", isMaster);
			RetVal = RPC_TC1_FacReadImei(isMaster, imeiStr);
			LOGD("Main: RPC_CCCI_TC1_FAC_READ_IMEI IMEI(out): %s\n", imeiStr);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	
			PackInfo[PacketNum].Length = TC1_FAC_IMEI_LEN;//IMEI_CODE_LENGTH;
			PackInfo[PacketNum++].pData = imeiStr;	
										
			break;
			}
		case RPC_CCCI_TC1_FAC_WRITE_SIM_LOCK_TYPE:
			{
			unsigned char	simLockType;
			bool		needFlashProgram;
		
			simLockType = *(unsigned char *)PackInfo[0].pData;
			needFlashProgram = *(bool *)PackInfo[1].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_WRITE_SIM_LOCK_TYPE <0x%x, 0x%x>\n",
							simLockType, needFlashProgram);
		
			RetVal = RPC_TC1_FacWriteSimLockType(simLockType, needFlashProgram);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	

			break;
			}
		case RPC_CCCI_TC1_FAC_READ_SIM_LOCK_TYPE:
			{
			unsigned char	*simLockType;
			simLockType = (unsigned char *)(pRpcBuf->buffer + 4*sizeof(int));

			RetVal = RPC_TC1_FacReadSimLockType(simLockType);
			LOGD("Main: RPC_CCCI_TC1_FAC_READ_SIM_LOCK_TYPE: 0x%x\n", *simLockType);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	
			PackInfo[PacketNum].Length = sizeof(unsigned char);
			PackInfo[PacketNum++].pData = simLockType;	
										
			break;
			}
		case RPC_CCCI_TC1_FAC_WRITE_NETWORK_CODE_LIST_NUM:
			{
			unsigned short	networkCodeListNum;
			bool		needFlashProgram;
		
			networkCodeListNum = *(unsigned short *)PackInfo[0].pData;
			needFlashProgram = *(bool *)PackInfo[1].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_WRITE_NETWORK_CODE_LIST_NUM <0x%x, 0x%x>\n",
							networkCodeListNum, needFlashProgram);
		
			RetVal = RPC_TC1_FacWriteNetworkCodeListNum(networkCodeListNum, needFlashProgram);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	

			break;
			}
		case RPC_CCCI_TC1_FAC_READ_NETWORK_CODE_LIST_NUM:
			{
			unsigned short	*networkCodeListNum;
			networkCodeListNum = (unsigned short *)(pRpcBuf->buffer + 4*sizeof(int));

			RetVal = RPC_TC1_FacReadNetworkCodeListNum(networkCodeListNum);
			LOGD("Main: RPC_CCCI_TC1_FAC_READ_NETWORK_CODE_LIST_NUM: %x\n", *(unsigned short*)networkCodeListNum);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	
			PackInfo[PacketNum].Length = sizeof(unsigned short);
			PackInfo[PacketNum++].pData = networkCodeListNum;	
										
			break;
			}
		case RPC_CCCI_TC1_FAC_WRITE_UNLOCK_CODE_VERIFY_FAIL_COUNT:
			{
			unsigned char	failCount;
			bool		needFlashProgram;
		
			failCount = *(unsigned char *)PackInfo[0].pData;
			needFlashProgram = *(bool *)PackInfo[1].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_WRITE_UNLOCK_CODE_VERIFY_FAIL_COUNT <0x%x, 0x%x>\n",
							failCount, needFlashProgram);
		
			RetVal = RPC_TC1_FacWriteUnlockCodeVerifyFailCount(failCount, needFlashProgram);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	

			break;
			}
		case RPC_CCCI_TC1_FAC_READ_UNLOCK_CODE_VERIFY_FAIL_COUNT:
			{
			unsigned char	*failCount;
			failCount = (unsigned char *)(pRpcBuf->buffer + 4*sizeof(int));

			RetVal = RPC_TC1_FacReadUnlockCodeVerifyFailCount(failCount);
			LOGD("Main: RPC_CCCI_TC1_FAC_READ_UNLOCK_CODE_VERIFY_FAIL_COUNT: %x\n",  *failCount);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	
			PackInfo[PacketNum].Length = sizeof(unsigned char);
			PackInfo[PacketNum++].pData = failCount;	
										
			break;
			}
		case RPC_CCCI_TC1_FAC_WRITE_UNLOCK_FAIL_COUNT:
			{
			unsigned char	simLockType;
			unsigned char	failCount;
			bool		needFlashProgram;

			simLockType = *(unsigned char *)PackInfo[0].pData;
			failCount = *(unsigned char *)PackInfo[1].pData;
			needFlashProgram = *(bool *)PackInfo[2].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_WRITE_UNLOCK_FAIL_COUNT <0x%x, 0x%x, 0x%x>\n",
							simLockType, failCount, needFlashProgram);
		
			RetVal = RPC_TC1_FacWriteUnlockFailCount(simLockType, failCount, needFlashProgram);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	

			break;
			}
		case RPC_CCCI_TC1_FAC_READ_UNLOCK_FAIL_COUNT:
			{
			unsigned char	simLockType;
			unsigned char	*failCount;
			simLockType = *(unsigned char *)PackInfo[0].pData;
			failCount = (unsigned char *)(pRpcBuf->buffer + 4*sizeof(int));

			LOGD("Main: RPC_CCCI_TC1_FAC_READ_UNLOCK_FAIL_COUNT(in): %x\n", simLockType);
			RetVal = RPC_TC1_FacReadUnlockFailCount(simLockType, failCount);
			LOGD("Main: RPC_CCCI_TC1_FAC_READ_UNLOCK_FAIL_COUNT(out): %x\n", *failCount);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	
			PackInfo[PacketNum].Length = sizeof(unsigned char);
			PackInfo[PacketNum++].pData = failCount;	
										
			break;
			}
		case RPC_CCCI_TC1_FAC_WRITE_UNLOCK_CODE:
			{
			FactoryUnlockCode *unlockCode;
			bool		needFlashProgram;

			unlockCode = (FactoryUnlockCode *)PackInfo[0].pData;
			needFlashProgram = *(bool *)PackInfo[1].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_WRITE_UNLOCK_CODE <%s, 0x%x>\n",
							(char*)unlockCode, needFlashProgram);
		
			RetVal = RPC_TC1_FacWriteUnlockCode(unlockCode, needFlashProgram);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	

			break;
			}
		case RPC_CCCI_TC1_FAC_VERIFY_UNLOCK_CODE:
			{
			unsigned char	simLockType;
			unsigned char	*unlockCode;
			bool		*isOK;

			simLockType = *(unsigned char *)PackInfo[0].pData;
			unlockCode = (unsigned char *)PackInfo[1].pData;
			isOK = *(bool *)PackInfo[2].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_VERIFY_UNLOCK_CODE <0x%x, %s>\n",
							simLockType, (char*)unlockCode);
		
			RetVal = RPC_TC1_FacVerifyUnlockCode(simLockType, unlockCode, isOK);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;
			PackInfo[PacketNum].Length = sizeof(bool);
			PackInfo[PacketNum++].pData = isOK;

			break;
			}
		case RPC_CCCI_TC1_FAC_CHECK_UNLOCK_CODE_VALIDNESS:
			{
			bool	*isValid;

			isValid = *(bool *)PackInfo[0].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_CHECK_UNLOCK_CODE_VALIDNESS <0x%x>\n", *isValid);
		
			RetVal = RPC_TC1_FacCheckUnlockCodeValidness(isValid);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;
			PackInfo[PacketNum].Length = sizeof(bool);
			PackInfo[PacketNum++].pData = isValid;

			break;
			}
		case RPC_CCCI_TC1_FAC_WRITE_NETWORK_CODE:
			{
			FactoryNetworkCode *networkCode;
			unsigned short	networkCodeListNum;
			bool		needFlashProgram;
		
			networkCode = (FactoryNetworkCode *)PackInfo[0].pData;
			networkCodeListNum = *(unsigned short *)PackInfo[1].pData;
			needFlashProgram = *(bool *)PackInfo[2].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_WRITE_NETWORK_CODE <%s, 0x%x, 0x%x>\n",
							(char*)networkCode, networkCodeListNum, needFlashProgram);
		
			RetVal = RPC_TC1_FacWriteNetworkCode(networkCode, networkCodeListNum, needFlashProgram);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	

			break;
			}
		case RPC_CCCI_TC1_FAC_READ_NETWORK_CODE:
			{
			FactoryNetworkCode *networkCode;
			unsigned short	networkCodeListNum;

			networkCode = (FactoryNetworkCode *)(pRpcBuf->buffer + 4*sizeof(int));
			networkCodeListNum = *(unsigned short*)PackInfo[0].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_READ_NETWORK_CODE <0x%x>\n", networkCodeListNum);
			RetVal = RPC_TC1_FacReadNetworkCode(networkCode, networkCodeListNum);
			LOGD("Main: RPC_CCCI_TC1_FAC_READ_NETWORK_CODE: %s\n", (char*)networkCode);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	
			PackInfo[PacketNum].Length = (TC1_FAC_NETWORK_CODE_LEN+8)*networkCodeListNum; //NET_WORK_CODE_LENGTH;
			PackInfo[PacketNum++].pData = networkCode;	
										
			break;
			}
		case RPC_CCCI_TC1_FAC_INIT_SIM_LOCK_DATA:
			{
			LOGD("Main: RPC_CCCI_TC1_FAC_INIT_SIM_LOCK_DATA\n");
			RetVal = RPC_TC1_FacInitSimLockData();

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	
										
			break;
			}
		case RPC_CCCI_TC1_FAC_CHECK_NETWORK_CODE_VALIDNESS:
			{
			unsigned char	simLockType;
			bool		*isValid;

			simLockType = *(unsigned char*)PackInfo[0].pData;
			isValid = (bool *)(pRpcBuf->buffer + 4*sizeof(int));

			LOGD("Main: RPC_TC1_FacCheckNetworkCodeValidness(in): %x\n", simLockType);
			RetVal = RPC_TC1_FacCheckNetworkCodeValidness(simLockType, isValid);
			LOGD("Main: RPC_TC1_FacCheckNetworkCodeValidness(out): %x\n", *isValid);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	
			PackInfo[PacketNum].Length = sizeof(bool);
			PackInfo[PacketNum++].pData = isValid;

			break;
			}
		case RPC_CCCI_TC1_FAC_READ_FUSG_FLAG:
			{
			unsigned char	*isValid;

			isValid = (unsigned char*)PackInfo[0].pData;

			LOGD("Main: RPC_CCCI_TC1_FAC_READ_FUSG_FLAG <0x%x>\n", *isValid);
		
			RetVal = RPC_TC1_FacReadFusgFlag(isValid);

			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;
			PackInfo[PacketNum].Length = sizeof(unsigned char);
			PackInfo[PacketNum++].pData = isValid;

			break;
			}
					
		default:
			LOGE("Main: Unknow RPC Operation ID (0x%x)\n", pRpcBuf->rpc_ops_id);			
			RetVal = RPC_PARAM_ERROR;
			PackInfo[PacketNum].Length = sizeof(int);
			PackInfo[PacketNum++].pData = (void*) &RetVal;	
			break;
		}
_Next:
		if(!RPC_WriteToMD(DeviceFd, ReqBufIndex, PackInfo, PacketNum))
		{
			LOGE("Main: fail to write packet!!\r\n");
			return -1;
		}
    RPC_WAKE_UNLOCK();
	}
#ifdef STREAM_SUPPORT
	LOGD("ccci_rpcd exit, free buffer\n");
	close(DeviceFd);
	free(PackInfo);
	if(stream_support)
		free(g_RpcInfo.pRpcBuf);
#endif
	dlclose(tc1_lib);
	return 0;
}
