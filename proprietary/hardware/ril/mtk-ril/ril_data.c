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

#include <telephony/mtk_ril.h>
#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <pthread.h>
#include <alloca.h>
#include "atchannels.h"
#include "at_tok.h"
#include "misc.h"
#include <getopt.h>
#include <sys/socket.h>
#include <cutils/sockets.h>
#include <termios.h>
#include "ril_callbacks.h"
#include <utils/Log.h>

#include <cutils/properties.h>
#include <netdb.h>
#include <sys/param.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <linux/if.h>
#include <linux/sockios.h>
#include <linux/route.h>

#define PROPERTY_RIL_SPECIFIC_SM_CAUSE "ril.specific.sm_cause"
#define PROPERTY_SIM_OPERATOR_NUMERIC "gsm.sim.operator.numeric"

#define DATA_CHANNEL_CTX getRILChannelCtxFromToken(t)

#define PPP_PPP_RUNNING_PROPERTY "init.svc.pppd_gprs"
#define PPP_DEF_RUNNING "running"
#define PPP_DEF_STOPPING "stopping"


#if defined(PURE_AP_USE_EXTERNAL_MODEM)
    const char *CCMNI_IFNAME = "usb";
    const char *LOG_TAG_STR = "RILEXTMD";
#else
    #ifdef MTK_RIL_MD2
        #if __MTK_ENABLE_MD5
            const char *CCMNI_IFNAME = "ccemni";
            const char *LOG_TAG_STR = "RIL";
        #else
            const char *CCMNI_IFNAME = "cc2mni";
            const char *LOG_TAG_STR = "RILMD2";
        #endif /* MTK_ENABLE_MD5 */
    #else /* MTK_RIL_MD2 */
        #if __MTK_ENABLE_MD1
            const char *CCMNI_IFNAME = "ccmni";
            const char *LOG_TAG_STR = "RIL"; 
        #elif __MTK_ENABLE_MD2
            const char *CCMNI_IFNAME = "cc2mni";
            const char *LOG_TAG_STR = "RILMD2";
        #else
            const char *CCMNI_IFNAME = "ccemni";
            const char *LOG_TAG_STR = "RIL";
        #endif
     #endif
#endif

#ifdef LOG_TAG
    #undef LOG_TAG
#endif
#define LOG_TAG LOG_TAG_STR

int sock_fd[MAX_CCMNI_NUMBER] = {0};
int sock6_fd[MAX_CCMNI_NUMBER] = {0};

extern int max_pdn_support;
extern int pdnInfoSize;
extern int isCid0Support;

/* Refer to system/core/libnetutils/ifc_utils.c */
extern int ifc_disable(const char *ifname);
extern int ifc_remove_default_route(const char *ifname);
extern int ifc_reset_connections(const char *ifname);

extern void initialCidTable();
extern void clearPdnInfo(PdnInfo* info);
extern int get_protocol_type(const char* protocol);
extern int getAvailableCid();
extern void configureNetworkInterface(int interfaceId, int isUp);
extern void requestSetFDMode(void * data, size_t datalen, RIL_Token t);
extern void onNetworkBearerUpdate(void* param);
extern void onPdnDeactResult(void* param);
extern int handleLastPdnDeactivation (const int isEmergency, const int lastPDNCid, RILChannelCtx* rilchnlctx);
extern int deactivateDataCall(int cid, RILChannelCtx* rilchnlctx);
extern int definePdnCtx(const RIL_Default_Bearer_VA_Config_Struct *pDefaultBearerVaConfig, const char *requestedApn,
                    int protocol, int availableCid, int authType, const char *username, const char* password, RILChannelCtx *pChannel);
extern int queryMatchedPdnWithSameApn(const char* apn, int* matchedCidList, RILChannelCtx* rilchnlctx);
extern int getAttachApnSupport();
void getIaCache(char* cache);
void setIaCache(const char* iccid, const char* protocol, const int authtype, const char* username, const int canHandleIms, const char* apn);
int doPsAttach(RILChannelCtx *pChannel);
int isAlwaysAttach();

void shutdownPPP(RIL_Token t);
int startPPPCall(RIL_Data_Call_Response_v6* response, char* port);

void onReAttachForSettingAttachApn(void* param);

extern RILChannelCtx *openPPPDataChannel(int isBlocking);
extern void closePPPDataChannel();
extern int at_send_command_to_ppp_data_channel(const char *command, ATResponse **pp_outResponse, 
                                        RILChannelCtx *p_channel);

extern void purge_data_channel(RILChannelCtx *p_channel);
extern int getMtkShareModemCurrent();
extern void freeDataResponse(RIL_Data_Call_Response_v6* response);
extern void RIL_onUnsolicitedResponseSocket(int unsolResponse, const void *data,
                                size_t datalen, RIL_SOCKET_ID socket_id);

#ifdef MTK_RIL
extern const char *proxyIdToString(int id);
#endif

//Fucntion prototype
void ril_data_ioctl_init(int index);
void ril_data_setflags(int s, struct ifreq *ifr, int set, int clr);
void ril_data_setaddr(int s, struct ifreq *ifr, const char *addr);

int receivedSCRI_RAU = 0;
int sendSCRI_RAU = 0;

//Global variables/strcuture
static int disableFlag = 1;
int gprs_failure_cause = 0;

extern int gcf_test_mode;
extern int s_md_off;

extern PdnInfo* pdn_info;

static const struct timeval TIMEVAL_0 = {0,0};

#define MAX_DUAL_APN_OPERATORS 20
#define MAX_PLMN_LEN 10
static char g_aDualApnPlmnList[MAX_DUAL_APN_OPERATORS][MAX_PLMN_LEN];
static int g_iNumDualApnPlmn;
#define PROPERTY_OPERATOR_NUMERIC "gsm.operator.numeric"
#define PROPERTY_DATA_NETWORK_TYPE "gsm.network.type"
#define DEFAULT_NODATA_APN "NODATA"

int disableIpv6Interface(char* filepath) {
    int fd = open(filepath, O_WRONLY);
    if (fd < 0) {
        LOGE("failed to open file (%s)", strerror(errno));
        return -1;
    }
    if (write(fd, "1", 1) != 1) {
        LOGE("failed to write property file (%s)",strerror(errno));
        close(fd);
        return -1;
    }
    close(fd);
    return 0;
}

void ril_data_ioctl_init(int index)
{
    disableFlag = 0;

    if(disableFlag || (gcf_test_mode > 0 )) {
        LOGD("Disable IPv6 inteface for CCNMI");
#ifdef MTK_RIL_MD1
        disableIpv6Interface("/proc/sys/net/ipv6/conf/ccmni0/disable_ipv6");
        disableIpv6Interface("/proc/sys/net/ipv6/conf/ccmni1/disable_ipv6");
        disableIpv6Interface("/proc/sys/net/ipv6/conf/ccmni2/disable_ipv6");
#else
        disableIpv6Interface("/proc/sys/net/ipv6/conf/cc2mni0/disable_ipv6");
        disableIpv6Interface("/proc/sys/net/ipv6/conf/cc2mni1/disable_ipv6");
        disableIpv6Interface("/proc/sys/net/ipv6/conf/cc2mni2/disable_ipv6");
#endif
    }

    if (sock_fd[index] > 0)
        close(sock_fd[index]);
    sock_fd[index] = socket(AF_INET, SOCK_DGRAM, 0);
    if (sock_fd[index] < 0)
        LOGD("Couldn't create IP socket: errno=%d", errno);
    else
        LOGD("Allocate sock_fd=%d, for cid=%d", sock_fd[index], index+1);

#ifdef INET6
    if (sock6_fd[index] > 0)
        close(sock6_fd[index]);
    sock6_fd[index] = socket(AF_INET6, SOCK_DGRAM, 0);
    if (sock6_fd[index] < 0) {
        sock6_fd[index] = -errno;    /* save errno for later */
        LOGD("Couldn't create IPv6 socket: errno=%d", errno);
    } else {
        LOGD("Allocate sock6_fd=%d, for cid=%d", sock6_fd[index], index+1);
    }
#endif
}

/* For setting IFF_UP: ril_data_setflags(s, &ifr, IFF_UP, 0) */
/* For setting IFF_DOWN: ril_data_setflags(s, &ifr, 0, IFF_UP) */
void ril_data_setflags(int s, struct ifreq *ifr, int set, int clr)
{
    if(ioctl(s, SIOCGIFFLAGS, ifr) < 0)
        goto terminate;
    ifr->ifr_flags = (ifr->ifr_flags & (~clr)) | set;
    if(ioctl(s, SIOCSIFFLAGS, ifr) < 0)
        goto terminate;
    return;
terminate:
    LOGD("Set SIOCSIFFLAGS Error!");
    return;
}

inline void ril_data_init_sockaddr_in(struct sockaddr_in *sin, const char *addr)
{
    sin->sin_family = AF_INET;
    sin->sin_port = 0;
    sin->sin_addr.s_addr = inet_addr(addr);
}

void ril_data_setaddr(int s, struct ifreq *ifr, const char *addr)
{
    LOGD("Configure IPv4 adress :%s", addr);
    ril_data_init_sockaddr_in((struct sockaddr_in *) &ifr->ifr_addr, addr);
    if(ioctl(s, SIOCSIFADDR, ifr) < 0)
        LOGD("Set SIOCSIFADDR Error");
}

void initialDataCallResponse(RIL_Data_Call_Response_v6* responses, int length) {
    int i = 0;
    for (i=0; i<length; i++) {
        memset(&responses[i], 0, sizeof(RIL_Data_Call_Response_v6));
        responses[i].status = PDP_FAIL_ERROR_UNSPECIFIED;
        responses[i].cid = INVALID_CID;
    }
}

int getAuthType(const char* authTypeStr) {
    int  authType = atoi(authTypeStr);
    //Application 0->none, 1->PAP, 2->CHAP, 3->PAP/CHAP;
    //Modem 0->PAP, 1->CHAP, 2->NONE, 3->PAP/CHAP;
    switch (authType) {
        case 0:
            return AUTHTYPE_NONE;
        case 1:
            return AUTHTYPE_PAP;
        case 2:
            return AUTHTYPE_CHAP;
        case 3:
            return AUTHTYPE_CHAP;
        default:
            return AUTHTYPE_NOT_SET;
    }
}

int getLastDataCallFailCause()
{
    if (gprs_failure_cause == SM_OPERATOR_BARRED ||
            gprs_failure_cause == SM_MBMS_CAPABILITIES_INSUFFICIENT ||
            gprs_failure_cause == SM_LLC_SNDCP_FAILURE ||
            gprs_failure_cause == SM_INSUFFICIENT_RESOURCES ||
            gprs_failure_cause == SM_MISSING_UNKNOWN_APN ||
            gprs_failure_cause == SM_UNKNOWN_PDP_ADDRESS_TYPE ||
            gprs_failure_cause == SM_USER_AUTHENTICATION_FAILED ||
            gprs_failure_cause == SM_ACTIVATION_REJECT_GGSN ||
            gprs_failure_cause == SM_ACTIVATION_REJECT_UNSPECIFIED ||
            gprs_failure_cause == SM_SERVICE_OPTION_NOT_SUPPORTED ||
            gprs_failure_cause == SM_SERVICE_OPTION_NOT_SUBSCRIBED ||
            gprs_failure_cause == SM_SERVICE_OPTION_OUT_OF_ORDER ||
            gprs_failure_cause == SM_NSAPI_IN_USE ||
            gprs_failure_cause == SM_REGULAR_DEACTIVATION ||
            gprs_failure_cause == SM_QOS_NOT_ACCEPTED ||
            gprs_failure_cause == SM_NETWORK_FAILURE ||
            gprs_failure_cause == SM_REACTIVATION_REQUESTED ||
            gprs_failure_cause == SM_FEATURE_NOT_SUPPORTED ||
            gprs_failure_cause == SM_SEMANTIC_ERROR_IN_TFT ||
            gprs_failure_cause == SM_SYNTACTICAL_ERROR_IN_TFT ||
            gprs_failure_cause == SM_UNKNOWN_PDP_CONTEXT ||
            gprs_failure_cause == SM_SEMANTIC_ERROR_IN_PACKET_FILTER ||
            gprs_failure_cause == SM_SYNTACTICAL_ERROR_IN_PACKET_FILTER ||
            gprs_failure_cause == SM_PDP_CONTEXT_WITHOU_TFT_ALREADY_ACTIVATED ||
            gprs_failure_cause == SM_MULTICAST_GROUP_MEMBERSHIP_TIMEOUT ||
            gprs_failure_cause == SM_BCM_VIOLATION ||
            gprs_failure_cause == SM_ONLY_IPV4_ALLOWED ||
            gprs_failure_cause == SM_ONLY_IPV6_ALLOWED ||
            gprs_failure_cause == SM_ONLY_SINGLE_BEARER_ALLOWED ||
            gprs_failure_cause == SM_COLLISION_WITH_NW_INITIATED_REQUEST ||
            gprs_failure_cause == SM_BEARER_HANDLING_NOT_SUPPORT ||
            gprs_failure_cause == SM_MAX_PDP_NUMBER_REACHED ||
            gprs_failure_cause == SM_APN_NOT_SUPPORT_IN_RAT_PLMN ||
            gprs_failure_cause == SM_INVALID_TRANSACTION_ID_VALUE ||
            gprs_failure_cause == SM_SEMENTICALLY_INCORRECT_MESSAGE ||
            gprs_failure_cause == SM_INVALID_MANDATORY_INFO ||
            gprs_failure_cause == SM_MESSAGE_TYPE_NONEXIST_NOT_IMPLEMENTED ||
            gprs_failure_cause == SM_MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE ||
            gprs_failure_cause == SM_INFO_ELEMENT_NONEXIST_NOT_IMPLEMENTED ||
            gprs_failure_cause == SM_CONDITIONAL_IE_ERROR ||
            gprs_failure_cause == SM_MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE ||
            gprs_failure_cause == SM_PROTOCOL_ERROR ||
            gprs_failure_cause == SM_APN_RESTRICTION_VALUE_INCOMPATIBLE_WITH_PDP_CONTEXT) {
        return gprs_failure_cause;
    }else if (gprs_failure_cause == 0x0D19){
        //SM_LOCAL_REJECT_ACT_REQ_DUE_TO_GPRS_ATTACH_FAILURE
        //Modem will retry attach
        LOGD("getLastDataCallFailCause(): GMM error %X", gprs_failure_cause);
    } else {
       gprs_failure_cause = 14;  //FailCause.Unknown
    }

    return gprs_failure_cause;
}

/* Change name from requestOrSendPDPContextList to requestOrSendDataCallList */
static void requestOrSendDataCallList(RIL_Token *t, RIL_SOCKET_ID rilid)
{
    /* Because the RIL_Token* t may be NULL passed due to receive URC: Only t is NULL, 2nd parameter rilid is used */
    RILChannelCtx* rilchnlctx = NULL;
    if (t != NULL)
        rilchnlctx = getRILChannelCtxFromToken(*t);
    else
        rilchnlctx = getChannelCtxbyProxy(rilid);

    requestOrSendDataCallListIpv6(rilchnlctx, t, rilid);
}

/* 27:RIL_REQUEST_SETUP_DATA_CALL/RIL_REQUEST_SETUP_DEFAULT_PDP */
/* ril_commands.h : {RIL_REQUEST_SETUP_DATA_CALL, dispatchStrings, responseStrings} */
/* ril_commands.h : {RIL_REQUEST_SETUP_DEFAULT_PDP, dispatchStrings, responseStrings, RIL_CMD_PROXY_3} */
/* Change name from requestSetupDefaultPDP to requestSetupDataCall */
void requestSetupDataCall(void * data, size_t datalen, RIL_Token t)
{
    /* In GSM with CDMA version: DOUNT - data[0] is radioType(GSM/UMTS or CDMA), data[1] is profile,
     * data[2] is apn, data[3] is username, data[4] is passwd, data[5] is authType (added by Android2.1)
     * data[6] is cid field added by mtk for Multiple PDP contexts setup support 2010-04
     */

    int requestParamNumber = (datalen/sizeof(char*));
    LOGD("requestSetupData with datalen=%d and parameter number=%d", datalen, requestParamNumber);

    const char* profile = ((const char **)data)[1];
    const char* apn = ((const char **)data)[2];
    const char* username = ((const char **)data)[3];
    const char* passwd = ((const char **)data)[4];
    const int authType = getAuthType(((const char **)data)[5]);
    const int protocol = get_protocol_type(((const char **)data)[6]);
    int interfaceId = requestParamNumber > 7 ? atoi(((const char **)data)[7]) - 1 : INVALID_CID;

    //VoLTE
    RIL_Default_Bearer_VA_Config_Struct defaultBearerVaConfig;
    memset(&defaultBearerVaConfig, 0, sizeof(RIL_Default_Bearer_VA_Config_Struct));

    LOGD("requestSetupData profile=%s, apn=%s, username=%s, password=xxxx, authType=%d, \
protocol=%d, interfaceId=%d, defaultBearerVaConfig=%d",
            profile, apn, username, authType, protocol, interfaceId, defaultBearerVaConfig.isValid);

    if (interfaceId < 0) {
        int i = 0, j = 0;
        for (i = 0; i < 10; i++) {
            if (interfaceId == INVALID_CID) {
                interfaceId = i;
                for (j = 0; j<max_pdn_support; j++) {
                    if (pdn_info[j].interfaceId == i) {
                        LOGD("requestSetupData interfaceId [%d] is already in use", i);
                        interfaceId = INVALID_CID;
                        break;
                    }
                }
            }
        }
        LOGD("requestSetupData selected ineterfaceId is %d", interfaceId);
    }

    if (requestParamNumber > 8) {
        defaultBearerVaConfig.isValid = atoi(((const char **)data)[8]);
    }

    if (defaultBearerVaConfig.isValid) {
        defaultBearerVaConfig.qos.qci = atoi(((const char **) data)[9]);
        defaultBearerVaConfig.qos.dlGbr = atoi(((const char **) data)[10]);
        defaultBearerVaConfig.qos.ulGbr = atoi(((const char **) data)[11]);
        defaultBearerVaConfig.qos.dlMbr = atoi(((const char **) data)[12]);
        defaultBearerVaConfig.qos.ulMbr = atoi(((const char **) data)[13]);
        defaultBearerVaConfig.emergency_ind = atoi(((const char **) data)[14]);
        defaultBearerVaConfig.pcscf_discovery_flag = atoi(((const char **) data)[15]);
        defaultBearerVaConfig.signalingFlag = atoi(((const char **) data)[16]);

        LOGD("requestSetupData defaultBearerVaConfig QOS[qci=%d, dlGbr=%d, ulGbr=%d, dlMbr=%d, \
ulMbr=%d] flag[emergency_ind=%d, pcscf_discovery_flag=%d, signalingFlag=%d]",
                defaultBearerVaConfig.qos.qci, defaultBearerVaConfig.qos.dlGbr,
                defaultBearerVaConfig.qos.ulGbr, defaultBearerVaConfig.qos.dlMbr,
                defaultBearerVaConfig.qos.ulMbr, defaultBearerVaConfig.emergency_ind,
                defaultBearerVaConfig.pcscf_discovery_flag, defaultBearerVaConfig.signalingFlag);
    }

#if MTK_IMS_SUPPORT
    if (requestParamNumber != 17) { // TODO: need to check this magic number
#else
    if (requestParamNumber != 8) {
#endif        /* Last parm is the authType instaed of cid */
        LOGD("requestSetupData with incorrect parameters");
        RIL_onRequestComplete(t, RIL_E_GENERIC_FAILURE, NULL, 0);
    }

#if defined(PURE_AP_USE_EXTERNAL_MODEM) && !defined(MT6280_SUPER_DONGLE)
    if (strcmp(profile, "1002") == 0) {
        // Always request for CID 1 for default connection
        requestSetupDataCallOverIPv6(apn, username, passwd, authType, protocol, 0, 1, profile, t);
    } else {
        requestSetupDataCallOverIPv6(apn, username, passwd, authType, protocol, 1, 2, profile, t);
    }
#else
    //genenal project
    if (defaultBearerVaConfig.emergency_ind == 1) { // Emergency
        requestSetupDataCallEmergency(apn, username, passwd, authType, protocol, interfaceId,
                profile, (void *) &defaultBearerVaConfig, t);
    } else {
        requestSetupDataCallOverIPv6(apn, username, passwd, authType, protocol, interfaceId,
                profile, (void *) &defaultBearerVaConfig, t);
    }
#endif
}

void requestDeactiveDataCall(void * data, size_t datalen, RIL_Token t)
{
    UNUSED(datalen);
#ifdef MTK_RIL_MD2
    if (getExternalModemSlot() >= 0) {
        LOGD("[%s] PPP shutdown", __FUNCTION__);
        shutdownPPP(t);
        int cidArray[1] = {1};
        RIL_onRequestComplete(t, RIL_E_SUCCESS, cidArray, sizeof(cidArray));
        return;
    }
#endif

    const char *cid;
    char *cmd;
    int err;
    ATResponse *p_response = NULL;
    int i = 0, lastPdnCid = INVALID_CID;
	int deactiveDataCallCid[pdnInfoSize];    //using [0] for primary cid, dedicate cid following index 0
    int responseLen = 0;
    int *pResponse = NULL;
    int interfaceId = atoi(((const char **)data)[0]);
    int responseDataSize = 0;
    LOGD("[%s] interfaceId=%d X", __FUNCTION__, interfaceId);

    int needHandleLastPdn = 0;
    int lastPdnState = DATA_STATE_INACTIVE;
	int isEmergency = 0;
    // AT+CGACT=<state>,<cid>;  <state>:0-deactivate;1-activate
    for(i = 0; i < pdnInfoSize; i++) {
        deactiveDataCallCid[i] = INVALID_CID;
        if (pdn_info[i].interfaceId == interfaceId) {
            if (!pdn_info[i].isDedicateBearer) {
                int err = deactivateDataCall(pdn_info[i].cid, DATA_CHANNEL_CTX);
                switch (err) {
                case CME_SUCCESS:
                    break;
                case CME_L4C_CONTEXT_CONFLICT_DEACT_ALREADY_DEACTIVATED:
                    LOGD("[%s] deactivateDataCall cid%d already deactivated", __FUNCTION__, i);
                    break;
                case CME_LAST_PDN_NOT_ALLOW:
                    if (needHandleLastPdn == 0) {
                        needHandleLastPdn = 1;
                        lastPdnState = pdn_info[i].active;
                        isEmergency = pdn_info[i].isEmergency;
                        lastPdnCid = pdn_info[i].cid;
                        LOGD("[%s] deactivateDataCall cid%d is the last PDN, state: %d", __FUNCTION__, i, lastPdnState);
                    }
                    break;
                case CME_LOCAL_REJECTED_DUE_TO_PS_SWITCH:
                    //in this case, we make the deactivation fail
                    //after switch, framework will check data call list and to deactivation again
                    LOGD("[%s] receive LOCAL_REJECTED_DUE_TO_PS_SWITCH", __FUNCTION__);
                    goto error;
                default:
                    goto error;
                };
                deactiveDataCallCid[responseLen++] = i; //primary cid
            }else { //dedicate cid
                deactiveDataCallCid[responseLen++] = i;
            }
        }
    }

    configureNetworkInterface(interfaceId, 0);
    for(i = 0; i < pdnInfoSize; i++) { //fixed network interface not disable issue while deactivating)
        if (pdn_info[i].interfaceId == interfaceId) {
            clearPdnInfo(&pdn_info[i]);
        }
    }

    if (responseLen > 0) {
        responseDataSize = sizeof(int) * responseLen;
        pResponse = alloca(responseDataSize);
        memcpy(pResponse, deactiveDataCallCid, responseDataSize);
    }

    if (needHandleLastPdn) {
        if (lastPdnState != DATA_STATE_LINKDOWN) {
            if(0 != handleLastPdnDeactivation(isEmergency, lastPdnCid, DATA_CHANNEL_CTX)) {
                goto error;
            }
        } else {
            LOGD("[%s] last pdn alread linkdown", __FUNCTION__);
        }
        pdn_info[lastPdnCid].active = DATA_STATE_LINKDOWN;
        pdn_info[lastPdnCid].cid = lastPdnCid;
        pdn_info[lastPdnCid].primaryCid = lastPdnCid;
    }

#if defined(PURE_AP_USE_EXTERNAL_MODEM)
    updateAvailablePdpNum();
#endif

    LOGD("[%s] response data size: %d, deactivate cid num: %d E", __FUNCTION__, responseDataSize,
            responseLen);
    //response deactivation result first then do re-attach
    RIL_onRequestComplete(t, RIL_E_SUCCESS, pResponse, responseDataSize);
    AT_RSP_FREE(p_response);
    return;
error:
    LOGE("[%s] error E", __FUNCTION__);
    RIL_onRequestComplete(t, RIL_E_GENERIC_FAILURE, NULL, 0);
    AT_RSP_FREE(p_response);
}

void requestLastDataCallFailCause(void * data, size_t datalen, RIL_Token t)
{
    UNUSED(data);
    UNUSED(datalen);
    int lastPdpFailCause = 14;
    lastPdpFailCause = getLastDataCallFailCause();
    RIL_onRequestComplete(t, RIL_E_SUCCESS, &lastPdpFailCause, sizeof(lastPdpFailCause));
}

void requestDataCallList(void * data, size_t datalen, RIL_Token t)
{
    UNUSED(data);
    UNUSED(datalen);
    requestOrSendDataCallList(&t, getRILIdByChannelCtx(getRILChannelCtxFromToken(t)));
}

void requestSetInitialAttachApn(void * data, size_t datalen, RIL_Token t)
{
    UNUSED(datalen);
    RIL_InitialAttachApn* param = data;
    int isDualApnSim = 0;
    LOGD("IA: requestSetInitialAttachApn [apn=%s, protocol=%s, auth_type=%d, username=%s, \
password=%s, operatorNumeric=%s, canHandleIms=%d], attachApnSupport=%d, isCid0Support=%d",
            param->apn, param->protocol, param->authtype, param->username,
            "XXXXX"/*param->password*/, param->operatorNumeric, param->canHandleIms,
            getAttachApnSupport(), isCid0Support);

    RIL_SOCKET_ID rid = getRILIdByChannelCtx(getRILChannelCtxFromToken(t));

    if (rid + 1 != (unsigned int) RIL_get3GSIM()) {
        LOGD("IA: requestSetInitialAttachApn but not main socket");
        goto error;
    } else if (getAttachApnSupport() == ATTACH_APN_NOT_SUPPORT) {
        LOGD("IA: requestSetInitialAttachApn but attach apn is not support");
        goto error;
    } else if (isDualTalkMode()) {
        #ifdef MTK_RIL_MD2
            LOGD("IA: requestSetInitialAttachApn but dual talk MD2 is not support LTE");
            goto error;
        #endif
    }

    char operatorNumeric[PROPERTY_VALUE_MAX] = {0};
    getMSimProperty(rid, PROPERTY_SIM_OPERATOR_NUMERIC, operatorNumeric);

#ifdef MTK_DTAG_DUAL_APN_SUPPORT
    int i = 0;
    char** dualApnPlmnList = param->dualApnPlmnList;
    int stringLength = datalen - sizeof(RIL_InitialAttachApn) + sizeof(param->dualApnPlmnList);
    stringLength = stringLength / sizeof(char *);

    if (stringLength > MAX_DUAL_APN_OPERATORS) {
        LOGE("IA: requestSetInitialAttachApn Need to extend the dual APN PLMN size %d !! ", stringLength);
        goto error;
    }

    g_iNumDualApnPlmn = stringLength;

    for (i = 0; i < stringLength; i++) {
        //Check if the HPLMN and registered PLMN is in dual APN list
        if (strcmp(operatorNumeric, dualApnPlmnList[i]) == 0) {
            isDualApnSim = 1;
            LOGD("IA: dualApnSim=%s", dualApnPlmnList[i]);
        }
        strncpy(g_aDualApnPlmnList[i], dualApnPlmnList[i], strlen(dualApnPlmnList[i]));
        LOGD("IA: dualApnPlmnList=%s", g_aDualApnPlmnList[i]);
    }
#endif //MTK_DTAG_DUAL_APN_SUPPORT

    if ((strcmp(operatorNumeric, param->operatorNumeric) != 0) && (isDualApnSim == 0)) {
        LOGE("IA: requestSetInitialAttachApn current SIM operator numeric is different from the \
parameter, skip setting attach APN [%s, %s]", operatorNumeric, param->operatorNumeric);
        goto error;
    } else if (isCid0Support) {
        int triggerPsAttach = 1;
        if (getMtkShareModemCurrent() > 1) {
            //This is for gemini load that attach apn should be set on data SIM
            char dataSettings[PROPERTY_VALUE_MAX] = {0};
            int simId3G = RIL_get3GSIM();
            property_get("persist.ril.gprs.setting", dataSettings, "0");

            if (atoi(dataSettings) == 0) {
                LOGD("IA: requestSetInitialAttachApn data disabled, trigger attach on 4G SIM");
            } else if (simId3G != atoi(dataSettings)) {
                LOGD("IA: requestSetInitialAttachApn data not enabled on 4G SIM, defind CID0 and \
not to trigger PS re-attach");
                triggerPsAttach = 0;
            }
        }

        char iccid[PROPERTY_VALUE_MAX] = {0};
        char iaProperty[PROPERTY_VALUE_MAX*2] = {0};

        property_get(PROPERTY_ICCID_SIM[rid], iccid, "");
        getIaCache(iaProperty);

        if (strlen(iaProperty) != 0) {
            //here we need to check if current IA property is different than what we want to set
            //if different, need to clear cache and do PS detach
            //this scenario happens when attach APN is changed
            char iaParameter[PROPERTY_VALUE_MAX*2] = {0};
            if (strlen(param->password) == 0) {
                sprintf(iaParameter, "%s,%s,%d,%s,%d,%s", iccid, param->protocol,
                        param->authtype, param->username, param->canHandleIms, param->apn);
            } else {
                //when password is set, iccid is not recorded
                //so we do not need to compare iccid
                sprintf(iaParameter, "%s,%s,%d,%s,%d,%s", "", param->protocol, param->authtype,
                        param->username, param->canHandleIms, param->apn);
            }

            if (strcmp(iaParameter, iaProperty) != 0) {
                LOGD("IA: requestSetInitialAttachApn the parameter is different than cache \
[%s][%s]", iaParameter, iaProperty);
                if (strlen(iccid) > 0) {
                    LOGD("IA: requestSetInitialAttachApn clear IA property");
                    memset(iaProperty, 0, sizeof(iaProperty));
                } else {
                    LOGD("IA: requestSetInitialAttachApn no SIM detected");
                }
            }
        } else {
            ATResponse *p_response = NULL;
            ATLine *p_cur = NULL;
            int err = at_send_command_multiline ("AT+CGDCONT?", "+CGDCONT:", &p_response,
                    DATA_CHANNEL_CTX);
            if (isATCmdRspErr(err, p_response)) {
                LOGD("IA: requestSetInitialAttachApn AT+CGDCONT? response error");
            } else {
                for (p_cur = p_response->p_intermediates; p_cur != NULL; p_cur = p_cur->p_next) {
                    char *line = p_cur->line;
                    int cid;
                    char* type = NULL;
                    char* apn = NULL;
                    err = at_tok_start(&line);
                    if (err < 0)
                        break;

                    err = at_tok_nextint(&line, &cid); //cid
                    if (err < 0)
                        break;

                    if (cid == 0) {
                        err = at_tok_nextstr(&line, &type); //type
                        if (err < 0)
                            break;

                        err = at_tok_nextstr(&line, &apn); //apn
                        if (err < 0)
                            break;

                        LOGD("IA: requestSetInitialAttachApn APN of CID0 is [%s], type=%s",
                                apn, type);
                        if (strcmp(INVALID_APN_VALUE, apn) == 0 || strcmp(param->apn, apn) == 0) {
                            LOGD("IA: requestSetInitialAttachApn CID0 is invalid APN or the same \
with current CID0 APN, not to do re-attach");
                            triggerPsAttach = 0;
                        }
                        break;
                    }
                }
            }
        }

        if (strlen(iaProperty) == 0) {
            RIL_Default_Bearer_VA_Config_Struct* defaultBearer = NULL;
            if (param->canHandleIms) {
                defaultBearer = malloc(sizeof(RIL_Default_Bearer_VA_Config_Struct));
                defaultBearer->signalingFlag = 1;
                defaultBearer->pcscf_discovery_flag  =1;
                defaultBearer->emergency_ind = 0;
            }
            if (!definePdnCtx(defaultBearer, param->apn, get_protocol_type(param->protocol), 0,
                    param->authtype, param->username, param->password, DATA_CHANNEL_CTX)) {
                LOGE("IA: requestSetInitialAttachApn define CID0 failed");
                at_send_command("AT+EGREA=0", NULL, DATA_CHANNEL_CTX);
                goto error;
            }

            if (defaultBearer != NULL) {
                free(defaultBearer);
                defaultBearer = NULL;
            }

            //update IA APN ICCID
            LOGD("IA: requestSetInitialAttachApn update IA ICCID [%s]", iccid);
            property_set(PROPERTY_IA_APN_SET_ICCID, iccid);

            if (strlen(param->password) == 0) {
                setIaCache(iccid, param->protocol, param->authtype, param->username,
                        param->canHandleIms, param->apn);
            } else {
                LOGD("IA: requestSetInitialAttachApn initial attach APN contain password, \
set patch to invalid value");
                setIaCache("", "", 0, "", 0, "");
            }

            if (triggerPsAttach) {
                LOGD("IA: requestSetInitialAttachApn relay re-attach to data channel");
                RILChannelCtx* pChannel = getRILChannelCtx(RIL_DATA, rid);
                RIL_requestProxyTimedCallback (onReAttachForSettingAttachApn, pChannel, &TIMEVAL_0,
                        pChannel->id, "onReAttachForSettingAttachApn");
            } else {
                LOGD("IA: requestSetInitialAttachApn not to do PS attach");
            }
        } else {
            LOGD("IA: requestSetInitialAttachApn valid IA property is set, \
not to do attach again");
        }
    } else {
        LOGD("IA: requestSetInitialAttachApn but CID0 is not support");
    }

    RIL_onRequestComplete(t, RIL_E_SUCCESS, NULL, 0);
    return;

error:
    RIL_onRequestComplete(t, RIL_E_GENERIC_FAILURE, NULL, 0);
}

void requestSetupPPPDataCall(void * data, size_t datalen, RIL_Token t) 
{
    UNUSED(datalen);
    const char *apn;
    //const char *username, *passwd, *authType, *protocol;
    char *req_dns1="", *req_dns2="";
    char *cmd;
    int err;
    int isPPPSucceed = 0;
    ATResponse *p_response = NULL;
    RIL_Data_Call_Response_v6 *responses = alloca(sizeof(RIL_Data_Call_Response_v6));
    initialDataCallResponse(responses, 1);

    LOGD("Shutdown PPP before setup data call");
    shutdownPPP(t);

    RILChannelCtx* p_channel = openPPPDataChannel(1);
    if (p_channel == NULL) {
        goto error;
    }

    int response_cid = 0;
    // response[0]:cid, response[1]:interface name, response[2]:ipaddr, response[3]:dns1,
    // response[4]:dns2 [5]: remote-ip

    //In GSM with CDMA version: DOUNT - data[0] is radioType(GSM/UMTS or CDMA), data[1] is profile,
    //data[2] is apn, data[3] is username, data[4] is passwd: modified 2009-0908 by mtk01411
    apn = ((const char **)data)[2];

    LOGD("requesting data connection to APN '%s'", apn);

    asprintf(&cmd, "AT+CGDCONT=1,\"IP\",\"%s\",,0,0", apn);
    //FIXME check for error here
    err = at_send_command_to_ppp_data_channel(cmd, NULL, p_channel);
    free(cmd);

    // packet-domain event reporting
    err = at_send_command_to_ppp_data_channel("AT+CGEREP=1,0", NULL, p_channel);

    // Hangup anything that's happening there now
    // 20120508: we shutdown ppp at the begining of this function, so remove this
    //err = at_send_command_to_ppp_data_channel("AT+CGACT=0,1", NULL, p_channel);

    // Start data on PDP context 1
    err = at_send_command_to_ppp_data_channel("ATD*99***1#", &p_response, p_channel);

    if (err < 0 || p_response->success == 0) {
        goto error;
    }

    closePPPDataChannel();

    p_channel = openPPPDataChannel(0);
    purge_data_channel(p_channel);
    closePPPDataChannel();

    //TODO: Check
    isPPPSucceed = startPPPCall(responses, "pppd_gprs");


    if (!isPPPSucceed)
        goto error;

    RIL_onRequestComplete(t, RIL_E_SUCCESS, responses, sizeof(RIL_Data_Call_Response_v6));
    at_response_free(p_response);
    if (responses != NULL)
        freeDataResponse(responses);
    return;
error:
    closePPPDataChannel();
    LOGD("Shutdown PPP before return fail");
    shutdownPPP(t);

    RIL_onRequestComplete(t, RIL_E_GENERIC_FAILURE, NULL, 0);
    at_response_free(p_response);
    if (responses != NULL)
        freeDataResponse(responses);
}

int isPPPRunning(char* runningProperty) {
    char runningCode[PROPERTY_VALUE_MAX] = {0};
    LOGD("isPPPRunning:%s", runningProperty);
    property_get(runningProperty, runningCode, "");
    if (strcmp(runningCode, PPP_DEF_RUNNING) == 0 || strcmp(runningCode, PPP_DEF_STOPPING) == 0)
        return 1;
    else
        return 0;
}

int startPPPCall(RIL_Data_Call_Response_v6* response, char* port) {

    property_set("net.gprs.ppp-exit", "");
    // Start pppd
    property_set("ctl.start", port);

    char *cmd;
    asprintf(&cmd, "init.svc.%s", port);

    int count = 0;
    while (count < 10) {
        if (isPPPRunning(cmd)) {
            LOGD("pppd started [%d]", count);
            break;
        } else {
            LOGD("Wait pppd started [%d]", count);
            sleep(1);
        }
        ++count;
    }
    free(cmd);

    int isPolling = 1;
    char* exitCode = malloc(PROPERTY_VALUE_MAX);
    char* runningCode = malloc(PROPERTY_VALUE_MAX);
    count = 0;
    int isPPPSucceed = 0;
    while (isPolling && count < 300) {
        memset(exitCode, 0, PROPERTY_VALUE_MAX);
        memset(runningCode, 0, PROPERTY_VALUE_MAX);
        property_get("net.gprs.ppp-running", runningCode, "");
        property_get("net.gprs.ppp-exit", exitCode, "");

        if (strcmp(exitCode, "") != 0) {
            LOGD("pppd exited [%s] during starting", exitCode);
            isPolling = 0;
        } else if (strcmp(runningCode, "yes") == 0) {
            //PPP Parameter
            char local[PROPERTY_VALUE_MAX] = {0};
            char remote[PROPERTY_VALUE_MAX] = {0};
            char dns1[PROPERTY_VALUE_MAX] = {0};
            char dns2[PROPERTY_VALUE_MAX] = {0};

            LOGD("polling pppd done");

            property_get("net.ppp0.local-ip", local, "0.0.0.0");
            property_get("net.ppp0.remote-ip", remote, "0.0.0.0");
            property_get("net.ppp0.dns1", dns1, "0.0.0.0");
            property_get("net.ppp0.dns2", dns2, "0.0.0.0");

            response->status = PDP_FAIL_NONE;
            response->suggestedRetryTime = 0;
            response->cid = 0;
            response->active = 2;
            asprintf(&response->type, "%s", "PPP");
            asprintf(&response->ifname, "%s", "ppp0");
            //address, local-ip
            asprintf(&response->addresses, "%s", local);
            //gateway, remote-ip
            asprintf(&response->gateways, "%s", remote);
            //dns
            asprintf(&response->dnses, "%s %s", dns1, dns2);

            LOGD("PPP Data call response: status=%d, suggestedRetryTime=%d, cid=%d, active=%d, \
                type=%s, ifname=%s, addresses=%s, dnses=%s, gateways=%s",
                response->status, response->suggestedRetryTime,
                response->cid, response->active, response->type,
                response->ifname, response->addresses,
                response->dnses, response->gateways);

            isPPPSucceed = 1;
            isPolling = 0;
        } else {
            LOGD("polling pppd not finished [%d]", count);
            sleep(1);
        }
        ++count;
    }
    free(exitCode);
    free(runningCode);
    return isPPPSucceed;

}

void shutdownPPP(RIL_Token t) {
    int isPPPOriginRunning = isPPPRunning(PPP_PPP_RUNNING_PROPERTY);
    int count = 0;

    if (isPPPOriginRunning) {
        do {
            LOGD("Shutdown ppp is retring [%d]", count);
            //property_set("ctl.stop", "pppd_gprs");
            at_send_command("AT+CGACT=0,1", NULL, DATA_CHANNEL_CTX);
            ++count;
            sleep(1);
        } while (count < 60 && isPPPRunning(PPP_PPP_RUNNING_PROPERTY));

        LOGD("Shutdown ppp is done [%d]", count);

        RILChannelCtx* p_channel = openPPPDataChannel(0);
        //waitForTargetPPPStopped(p_channel);
        closePPPDataChannel();
    }

    RILChannelCtx* p_channel = openPPPDataChannel(0);
    purge_data_channel(p_channel);
    closePPPDataChannel();
}

void onGPRSDeatch(char* urc, RIL_SOCKET_ID rid)
{
    LOGD("onGPRSDeatch:%s", urc);
    RIL_onUnsolicitedResponseSocket(RIL_UNSOL_GPRS_DETACH, NULL, 0, rid);
}

/* Change name from onPdpContextListChanged to onDataCallListChanged */
/* It can be called in onUnsolicited() mtk-ril\ril_callbacks.c */
void onDataCallListChanged(void* param)
{
    RIL_SOCKET_ID rilid = *((RIL_SOCKET_ID *) param);
    requestOrSendDataCallList(NULL, rilid);
}

void onDualApnCheckNetworkChange(void* param)
{
    int err = 0;
    ATResponse *p_response = NULL;
    RILChannelCtx* rilchnlctx = (RILChannelCtx*) param;
    ATLine *p_cur = NULL;
    char *out = NULL;
    char operatorNumeric[PROPERTY_VALUE_MAX] = {0};
    char networkType[PROPERTY_VALUE_MAX] = {0};
    int isNoDataApnExist = 0;
    int isDualApnPlmnNw = 0;
    int i;
    char *token;
    int activeSimFor4G = 0;

    int activatedPdnNum = 0;
    int concatenatedBearerNum = 0;
    static int isNoDataActive = 1;

    property_get(PROPERTY_DATA_NETWORK_TYPE, networkType, "");
    LOGD("onDualApnCheckNetworkChange: Network type [%s]", networkType);

    i = 0;
    token = strtok(networkType, ",");
    while (token != NULL) {
        if (0 == strcmp(token, "LTE")) {
            LOGD("onDualApnCheckNetworkChange: Find out LTE in SIM#%d\n", i+1);
            break;
        }
        token = strtok(NULL,",");
        i++;
    }
    activeSimFor4G = i; //keep the SIM ID for LTE service 0:SIM#1

    property_get(PROPERTY_OPERATOR_NUMERIC, operatorNumeric, "");
    LOGD("onDualApnCheckNetworkChange: Registered PLMN [%s]", operatorNumeric);

    token = strtok(operatorNumeric, ",");
    while(i > 0) {
        if (token != NULL) {
            token = strtok(NULL,",");
        }
        i--;
    }

    if (token == NULL) {
        LOGD("onDualApnCheckNetworkChange: Not register yet\n");
        return;
    } else {
        LOGD("onDualApnCheckNetworkChange: Find out LTE PLMN [%s]\n", token);
    }

    char simOperatorNumeric[PROPERTY_VALUE_MAX] = {0};
    RIL_SOCKET_ID rid = getRILIdByChannelCtx(rilchnlctx);
    //If the logic of getMSimProperty() is changed, then here should be also considered
    if ((rid - RIL_SOCKET_1) != activeSimFor4G) {
        LOGD("onDualApnCheckNetworkChange: Shouldn't be here since only 4G URC[id:%d]\n", activeSimFor4G);
        return;
    }
    getMSimProperty(rid, PROPERTY_SIM_OPERATOR_NUMERIC, simOperatorNumeric);

    for (i = 0; i < g_iNumDualApnPlmn; i++) {
        //Check the HPLMN
        if (0 == strncmp(simOperatorNumeric, g_aDualApnPlmnList[i], MAX_PLMN_LEN - 1)) {
            isDualApnPlmnNw = 1;
            break;
        }
    }

    if (0 == isDualApnPlmnNw) {
        LOGD("onDualApnCheckNetworkChange: Not a Dual APN SIM [%s]", simOperatorNumeric);
        return;
    }

    isDualApnPlmnNw = 0;
    for (i = 0; i < g_iNumDualApnPlmn; i++) {
        if (0 == strncmp(token, g_aDualApnPlmnList[i], MAX_PLMN_LEN - 1)) {
            isDualApnPlmnNw = 1;
            break;
        }
    }

    if (0 == isDualApnPlmnNw) {
        LOGD("onDualApnCheckNetworkChange: Not a Dual APN PLMN now [%s]", token);
        return;
    }

    int *activatedCidList = (int *) malloc(pdnInfoSize * sizeof(int));

    for (i = 0; i < pdnInfoSize; i++) {
        activatedCidList[i] = INVALID_CID;
    }

    err = at_send_command_multiline ("AT+CGCONTRDP", "+CGCONTRDP:", &p_response, rilchnlctx);
    if (isATCmdRspErr(err, p_response)) {
        LOGD("onDualApnCheckNetworkChange: AT+CGCONTRDP response error");
    } else {
        for (p_cur = p_response->p_intermediates; p_cur != NULL; p_cur = p_cur->p_next) {
            char *line = p_cur->line;
            int cid;
            int type;

            err = at_tok_start(&line);
            if (err < 0)
                break;

            err = at_tok_nextint(&line, &cid); //cid
            if (err < 0)
                break;

            err = at_tok_nextint(&line, &type); //type
            if (err < 0)
                break;

            err = at_tok_nextstr(&line, &out); //apn
            if (err < 0)
                break;

            if (out != NULL && strlen(out) > 0 && apnMatcher(out, DEFAULT_NODATA_APN) == 0)
            {
                //NODATA APN exists, do nothing
                LOGD("onDualApnCheckNetworkChange: APN exist [%s]", out);
                isNoDataApnExist = 1;
                break;
            }
        }
    }

    AT_RSP_FREE(p_response);

    if (0 == isNoDataApnExist) {
        //activate NODATA APN
        //set follow on flag for modem to re-use the connection
        //this helps to speed up PDN activation when we need to do PS attach
        err = at_send_command("AT+EGTYPE=3", &p_response, rilchnlctx);
        AT_RSP_FREE(p_response);

        //set to auto attach mode so that modem will do PS re-attach when network detach happened
        err = at_send_command("AT+EGTYPE=1", &p_response, rilchnlctx);
        if (isATCmdRspErr(err, p_response)) {
            LOGD("onDualApnCheckNetworkChange: fail to set connetion type");
            gprs_failure_cause = 14;
            goto error;
        }
        AT_RSP_FREE(p_response);

        //Use this command to make sure PS is attached before activate PDP
        err = at_send_command("AT+CGATT=1", &p_response, rilchnlctx);
        if (isATCmdRspErr(err, p_response)) {
            LOGD("onDualApnCheckNetworkChange: fail to do PS attach");
            gprs_failure_cause = 14;
            goto error;
        }
        AT_RSP_FREE(p_response);

        //after AT+CGATT=1, the CID0 would be activated, enable fallback again
        //not to check error since it is not critical
        //AT+EGFB=<fallback_enable>,<ipv4_first>
        err = at_send_command("AT+EGFB=1,1", &p_response, rilchnlctx);
        AT_RSP_FREE(p_response);

        int availableCid = getAvailableCid();
        if (availableCid == INVALID_CID) {
            LOGE("onDualApnCheckNetworkChange: no available CID to use");
            goto error;
        } else {
            LOGD("onDualApnCheckNetworkChange: available CID is [%d]", availableCid);
        }
        // define Pdn ctx
        if (0 == definePdnCtx(NULL, DEFAULT_NODATA_APN, IPV4, availableCid, 0, "", "", rilchnlctx)){
            goto error;
        }

        // packet-domain event reporting: +CGEREP=<mode>,<bfr>
        err = at_send_command("AT+CGEREP=1,0", NULL, rilchnlctx);

        // activate No Data PDN
        if (0 == activatePdn(availableCid, &activatedPdnNum, &concatenatedBearerNum,
                             activatedCidList, 0, isNoDataActive, rilchnlctx)) {
            goto error;
        }

        if (DATA_STATE_INACTIVE == isCidActive(activatedCidList, activatedPdnNum, rilchnlctx)) {
            goto error;
        }
    }

    if(activatedCidList != NULL) {
        free(activatedCidList);
    }

    LOGD("onDualApnCheckNetworkChange finish");
    return;

error:
    LOGE("onDualApnCheckNetworkChange ERROR, clear %d", activatedPdnNum);
    for (i = 0; i < activatedPdnNum; i++)
        clearPdnInfo(&pdn_info[activatedCidList[i]]);

    AT_RSP_FREE(p_response);
}

void onMePdnActive(void* param)
{
    MePdnActiveInfo* pInfo = (MePdnActiveInfo*)param;
    RILChannelCtx* rilchnlctx = pInfo->pDataChannel;
    int activatedCid = pInfo->activeCid;
    int err = 0;
    ATLine *p_cur = NULL;
    ATResponse *p_response = NULL;
    char *out = NULL;
    int i;
    int isFindNoDataPdn = 0;

    LOGD("onMePdnActive: CID%d is activated and current state is %d", activatedCid, pdn_info[activatedCid].active);
    if (pdn_info[activatedCid].active == DATA_STATE_INACTIVE) {
        pdn_info[activatedCid].active = DATA_STATE_LINKDOWN; // Update with link down state.
        pdn_info[activatedCid].cid = activatedCid;
        pdn_info[activatedCid].primaryCid = activatedCid;
    }

    //Check no data PDN
    for (i = 0; i < pdnInfoSize; i++) {
        if (1 == pdn_info[i].isNoDataPdn) {
            LOGD("onMePdnActive: CID%d NoData PDN %s exist", i, pdn_info[i].apn);
            isFindNoDataPdn = 1;
            break;
        }
    }

    if (0 == isFindNoDataPdn) {
        err = at_send_command_multiline ("AT+CGCONTRDP", "+CGCONTRDP:", &p_response, rilchnlctx);
        if (isATCmdRspErr(err, p_response)) {
            LOGD("onMePdnActive: AT+CGCONTRDP response error");
        } else {
            for (p_cur = p_response->p_intermediates; p_cur != NULL; p_cur = p_cur->p_next) {
                char *line = p_cur->line;
                int cid;
                int type;

                err = at_tok_start(&line);
                if (err < 0)
                    break;

                err = at_tok_nextint(&line, &cid); //cid
                if (err < 0)
                    break;

                err = at_tok_nextint(&line, &type); //type
                if (err < 0)
                    break;

                err = at_tok_nextstr(&line, &out); //apn
                if (err < 0)
                    break;

                if (out != NULL && strlen(out) > 0 && apnMatcher(out, DEFAULT_NODATA_APN) == 0)
                {
                    //NODATA APN is found
                    LOGD("onMePdnActive: NoData APN is found [%s] Cid:%d", out, cid);
                    pdn_info[cid].isNoDataPdn = 1;
                    isFindNoDataPdn = 1;
                    break;
                }
            }
        }

        if (0 == isFindNoDataPdn)
            LOGD("onMePdnActive: NoData APN is not found");
    }

    free(param);
}

void onReAttachForSettingAttachApn(void* param)
{
    LOGD("IA: onReAttachForSettingAttachApn start detach");
    RILChannelCtx* pChannel = (RILChannelCtx*)param;
    //when AT+EGREA=1 is set, EUTRAN would not be disabled after PS detached
    at_send_command("AT+EGREA=1", NULL, pChannel);
    detachPs(REQUEST_DATA_DETACH_NOT_RESPONSE, pChannel);

    LOGD("IA: onReAttachForSettingAttachApn detach done and start attach");
    //when AT+EGREA=0 is set, EUTRAN would be disabled after PS detached
    at_send_command("AT+EGREA=0", NULL, pChannel);
    if (!doPsAttach(pChannel)) {
        LOGE("IA: onReAttachForSettingAttachApn trigger attach failed");
        //we sill continue to update cache even if attach is failed
    }
    //Trigger query apn to update network assigned apn
    int matchCount = 0; //use to prevent queryMatchedPdnWithSameApn exception
    queryMatchedPdnWithSameApn("", &matchCount, pChannel);
    LOGD("IA: onReAttachForSettingAttachApn re-attach complete");
}

int isAlwaysAttach()
{
    return 1;
}

extern int rilDataMain(int request, void *data, size_t datalen, RIL_Token t)
{
    switch (request) {
        case RIL_REQUEST_SETUP_DATA_CALL:
            if (getExternalModemSlot() >= 0) {
#ifdef MTK_RIL_MD2
                MTK_REQUEST_SETUP_PPP_CALL(data, datalen, t);
#else
                MTK_REQUEST_SETUP_DATA_CALL(data, datalen, t);
#endif
            } else {
                MTK_REQUEST_SETUP_DATA_CALL(data, datalen, t);
            }
            break;
        case RIL_REQUEST_DATA_CALL_LIST:
            MTK_REQUEST_DATA_CALL_LIST(data, datalen, t);
            break;
        case RIL_REQUEST_DEACTIVATE_DATA_CALL:
            MTK_REQUEST_DEACTIVATE_DATA_CALL(data, datalen, t);
            break;
        case RIL_REQUEST_SET_INITIAL_ATTACH_APN:
            MTK_REQUEST_SET_INITIAL_ATTACH_APN(data, datalen, t);
            break;
        case RIL_REQUEST_LAST_DATA_CALL_FAIL_CAUSE:
            MTK_REQUEST_LAST_DATA_CALL_FAIL_CAUSE(data, datalen, t);
            break;
        case RIL_REQUEST_SET_SCRI:
            MTK_REQUEST_SET_SCRI(data, datalen, t);
            break;
        //[New R8 modem FD]
        case RIL_REQUEST_SET_FD_MODE:
            MTK_REQUEST_FD_MODE(data, datalen, t);
            break;
        case RIL_REQUEST_DETACH_PS:
            MTK_REQUEST_DATA_IDLE(data, datalen, t);
            break;
        case RIL_REQUEST_ALLOW_DATA:
            MTK_REQUEST_ALLOW_DATA(data, datalen, t);
            break;
        //VoLTE
        case RIL_REQUEST_SETUP_DEDICATE_DATA_CALL:
            requestSetupDedicateDataCall(data, datalen, t);
            break;
        case RIL_REQUEST_DEACTIVATE_DEDICATE_DATA_CALL:
            requestDeactivateDedicateDataCall(data, datalen, t);
            break;
        case RIL_REQUEST_MODIFY_DATA_CALL:
            requestModifyDataCall(data, datalen, t);
            break;
        case RIL_REQUEST_ABORT_SETUP_DATA_CALL:
            requestAbortSetupDataCall(data, datalen, t);
            break;
        case RIL_REQUEST_PCSCF_DISCOVERY_PCO:
            requestPcscfPco(data, datalen, t);
            break;
        case RIL_REQUEST_CLEAR_DATA_BEARER:
            requestClearDataBearer(data,datalen,t);
            break;
        case RIL_REQUEST_SET_DATA_ON_TO_MD:
            MTK_REQUEST_SET_DATA_ON_TO_MD(data, datalen, t);
            break;
        case RIL_REQUEST_SET_REMOVE_RESTRICT_EUTRAN_MODE:
            MTK_REQUEST_SET_REMOVE_RESTRICT_EUTRAN_MODE(data, datalen, t);
            break;
        default:
            return 0; /* no matched request */
    }

    return 1; /* request found and handled */

}

extern int rilDataUnsolicited(const char *s, const char *sms_pdu, RILChannelCtx* p_channel)
{
    UNUSED(sms_pdu);
    int rilid = getRILIdByChannelCtx(p_channel);
    RILChannelCtx* pDataChannel = getChannelCtxbyId(sCmdChannel4Id[rilid]);
    
    if (strStartsWith(s, "+CGEV: NW DEACT") || strStartsWith(s, "+CGEV: ME DEACT") ||
        strStartsWith(s, "+CGEV: NW REACT") ||
        strStartsWith(s, "+CGEV: NW PDN DEACT") || strStartsWith(s, "+CGEV: ME PDN DEACT"))
    {
        char* urc = NULL;
        asprintf(&urc, "%s", s);
        TimeCallbackParam* param = (TimeCallbackParam*)malloc(sizeof(TimeCallbackParam));
        param->rid = rilid;
        param->urc = urc;

        RIL_requestProxyTimedCallback (onPdnDeactResult,
               param,
               &TIMEVAL_0,
               pDataChannel->id,
               "onPdnDeactResult");
        return 1;
    } else if (strStartsWith(s, "+CGEV: NW ACT") || strStartsWith(s, "+CGEV: NW MODIFY")) {
        //+CGEV: NW ACT <p_cid>, <cid>, <event_type>
        //+CGEV: NW MODIFY <cid>, <change_reason>, <event_type>
        LOGD("rilDataUnsolicited receive bearer activation/modification by network [%s]", s);
        char* urc = NULL;
        asprintf(&urc, "%s", s);

        TimeCallbackParam* param = (TimeCallbackParam*)malloc(sizeof(TimeCallbackParam));
        param->rid = rilid;
        param->urc = urc;
        if (strStartsWith(s, "+CGEV: NW MODIFY"))
            param->isModification = 1;
        else
            param->isModification = 0;

        //be aware that the param->urc & param should be free in onNetworkBearerUpdate
        RIL_requestProxyTimedCallback (onNetworkBearerUpdate,
               param,
               &TIMEVAL_0,
               pDataChannel->id,
               "onNetworkBearerUpdate");
        return 1;
    } else if (strStartsWith(s, "+CGEV: ME")) {
        if (strStartsWith(s, "+CGEV: ME PDN ACT")) {
            char *p = strstr(s, ME_PDN_URC);
            if( p == NULL) {
                return 1;
            }
            p = p + strlen(ME_PDN_URC) + 1;

            MePdnActiveInfo* pInfo = malloc(sizeof(MePdnActiveInfo));

            if (NULL == pInfo) {
                LOGD("rilDataUnsolicited allocate pInfo fail!!");
                return 1;
            }

            pInfo->activeCid = atoi(p);
            pInfo->pDataChannel = pDataChannel;

            LOGD("rilDataUnsolicited CID%d is activated and current state is %d", pInfo->activeCid, pdn_info[pInfo->activeCid].active);

            RIL_requestProxyTimedCallback (onMePdnActive,
                (void *) pInfo,
                &TIMEVAL_0,
                pDataChannel->id,
                "onMePdnActive");
        } else {
            //+CGEV: ME related cases should be handled in setup data call request handler
            LOGD("rilDataUnsolicited ignore +CGEV: ME cases (%s)", s);
        }
        return 1;
    } else if (strStartsWith(s, "+CGEV:")) {
        if (s_md_off) {
            LOGD("rilDataUnsolicited(): modem off!");
            RIL_onUnsolicitedResponseSocket(RIL_UNSOL_DATA_CALL_LIST_CHANGED, NULL, 0, rilid);
         } else {
            RIL_requestProxyTimedCallback (onDataCallListChanged,
                &s_data_ril_cntx[rilid],
                &TIMEVAL_0,
                pDataChannel->id,
                "onDataCallListChanged");
         }

        return 1;

#ifdef WORKAROUND_FAKE_CGEV
    } else if (strStartsWith(s, "+CME ERROR: 150")) {

        if (s_md_off)    {
            LOGD("rilDataUnsolicited(): modem off!");
            RIL_onUnsolicitedResponseSocket(RIL_UNSOL_DATA_CALL_LIST_CHANGED, NULL, 0, rilid);
         } else {
            RIL_requestProxyTimedCallback (onDataCallListChanged,
                &s_data_ril_cntx[rilid],
                &TIMEVAL_0,
                pDataChannel->id,
                "onDataCallListChanged");
         }

        return 1;

#endif /* WORKAROUND_FAKE_CGEV */

    } else if(strStartsWith(s, "+ESCRI:")) {
        onScriResult((char*) s,(RIL_SOCKET_ID) rilid);
        return 1;
    } else if (strStartsWith(s, "+ECODE33")) {
        //M: CC33.
        RIL_onUnsolicitedResponse(RIL_UNSOL_REMOVE_RESTRICT_EUTRAN, NULL, 0, (RIL_SOCKET_ID) rilid);
        return 1;
    } else if (strStartsWith(s, "+EMOBD:")) {
        onMoDataBarring((char*) s,(RIL_SOCKET_ID) rilid);
        return 1;
    }

    return 0;
}


