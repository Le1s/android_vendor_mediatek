/*
**
** Copyright 2008, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/


#include <cutils/sockets.h>
#include <cutils/jstring.h>
#include <cutils/record_stream.h>
#include <utils/Log.h>
#include <utils/SystemClock.h>
#include <pthread.h>
#include <binder/Parcel.h>
#include <cutils/jstring.h>
#include <ril_event.h>
#include <sys/types.h>
#include <sys/un.h>
#include <telephony/mtk_ril.h>

#ifdef MTK_RIL

#define RIL_SUPPORT_PROXYS  RIL_SUPPORT_CHANNELS

#define RIL_CMD_PROXY_5     RIL_CMD_4
#define RIL_CMD_PROXY_1     RIL_CMD_3
#define RIL_CMD_PROXY_2     RIL_CMD_2
#define RIL_CMD_PROXY_3     RIL_CMD_1
#define RIL_CMD_PROXY_4     RIL_URC
#define RIL_CMD_PROXY_6     RIL_ATCI

#if (SIM_COUNT >= 2)
#define RIL_PROXY_OFFSET    RIL_CHANNEL_OFFSET
#define RIL_CMD2_PROXY_5    RIL_CMD2_4
#define RIL_CMD2_PROXY_1    RIL_CMD2_3
#define RIL_CMD2_PROXY_2    RIL_CMD2_2
#define RIL_CMD2_PROXY_3    RIL_CMD2_1
#define RIL_CMD2_PROXY_4    RIL_URC2
#define RIL_CMD2_PROXY_6    RIL_ATCI2
#endif

#if (SIM_COUNT >= 3)
#define RIL_PROXY_OFFSET    RIL_CHANNEL_OFFSET
#define RIL_CMD3_PROXY_5    RIL_CMD3_4
#define RIL_CMD3_PROXY_1    RIL_CMD3_3
#define RIL_CMD3_PROXY_2    RIL_CMD3_2
#define RIL_CMD3_PROXY_3    RIL_CMD3_1
#define RIL_CMD3_PROXY_4    RIL_URC3
#define RIL_CMD3_PROXY_6    RIL_ATCI3
#endif

#if (SIM_COUNT >= 4)
#define RIL_PROXY_OFFSET    RIL_CHANNEL_OFFSET
#define RIL_CMD4_PROXY_5    RIL_CMD4_4
#define RIL_CMD4_PROXY_1    RIL_CMD4_3
#define RIL_CMD4_PROXY_2    RIL_CMD4_2
#define RIL_CMD4_PROXY_3    RIL_CMD4_1
#define RIL_CMD4_PROXY_4    RIL_URC4
#define RIL_CMD4_PROXY_6    RIL_ATCI4
#endif

#endif /* MTK_RIL */

extern "C" const char *proxyIdToString(int id);

namespace android {

extern "C" int getTelephonyMode();
extern "C" int isDualTalkMode();
extern "C" int isGeminiMode();
extern "C" int isSingleMode();

extern "C" int getExternalModemSlot();
extern "C" int isInternationalRoamingEnabled();
extern "C" int isEVDODTSupport();
extern "C" int getExternalModemSlotTelephonyMode();
extern "C" int isBootupWith3GCapability();
extern "C" int getRilProxysNum();

enum WakeType {DONT_WAKE, WAKE_PARTIAL};

struct CommandInfo {
    int requestNumber;
    void (*dispatchFunction) (Parcel &p, struct RequestInfo *pRI);
    int(*responseFunction) (Parcel &p, void *response, size_t responselen);
#ifdef MTK_RIL
    RILChannelId proxyId;
#endif /* MTK_RIL */
} ;

typedef struct {
    int requestNumber;
    int (*responseFunction) (Parcel &p, void *response, size_t responselen);
    WakeType wakeType;
} UnsolResponseInfo;

typedef struct RequestInfo {
    int32_t token;      //this is not RIL_Token
    CommandInfo *pCI;
    struct RequestInfo *p_next;
    char cancelled;
    char local;         // responses to local commands do not go back to command process
    RIL_SOCKET_ID socket_id;
#ifdef MTK_RIL
    RILChannelId cid;    // For command dispatch after onRequest()
#endif /* MTK_RIL */
} RequestInfo;

typedef struct UserCallbackInfo {
    RIL_TimedCallback p_callback;
    void *userParam;
    struct ril_event event;
    struct UserCallbackInfo *p_next;
#ifdef MTK_RIL
    RILChannelId cid;    // For command dispatch after onRequest()
#endif /* MTK_RIL */
} UserCallbackInfo;

typedef struct SocketListenParam {
    RIL_SOCKET_ID socket_id;
    int fdListen;
    int fdCommand;
    char* processName;
    struct ril_event* commands_event;
    struct ril_event* listen_event;
    void (*processCommandsCallback)(int fd, short flags, void *param);
    RecordStream *p_rs;
} SocketListenParam;

extern "C" const char * requestToString(int request);
extern "C" const char * failCauseToString(RIL_Errno);
extern "C" const char * callStateToString(RIL_CallState);
extern "C" const char * radioStateToString(RIL_RadioState);
extern "C" const char * rilSocketIdToString(RIL_SOCKET_ID socket_id);

#ifdef MTK_RIL

typedef struct RequestInfoProxy {
    struct RequestInfoProxy *p_next;
    RequestInfo * pRI;
    UserCallbackInfo *pUCI;
    Parcel* p;
} RequestInfoProxy;

void triggerEvLoop(void);
void userTimerCallback (int fd, short flags, void *param);

void enqueueLocalRequestResponse(RequestInfo* pRI, void *buffer, size_t buflen, UserCallbackInfo* pUCI, RIL_SOCKET_ID socket_id);
void enqueueAtciRequest(RequestInfo* pRI, void *buffer, size_t buflen, UserCallbackInfo* pUCI, int forceSendToUrc);
void enqueue(RequestInfo* pRI, void *buffer, size_t buflen, UserCallbackInfo* pUCI, RIL_SOCKET_ID socket_id);
void RIL_startRILProxys(void);

typedef struct AtResponseList {
    int id;
    char* data;
    size_t datalen;
    AtResponseList *pNext;
} AtResponseList;

void cacheUrc(int unsolResponse, void *data, size_t datalen, RIL_SOCKET_ID id);
void sendPendedUrcs(RIL_SOCKET_ID socket_id, int fdCommand);

extern "C" CommandInfo s_commands[];
extern "C" CommandInfo s_mtk_commands[];
extern const int s_commands_size;
extern const int s_mtk_commands_size;

#endif /* MTK_RIL */

}
