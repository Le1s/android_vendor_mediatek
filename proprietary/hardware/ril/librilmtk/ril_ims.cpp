/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2014. All rights reserved.
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
#include <hardware_legacy/power.h>

#include <telephony/mtk_ril.h>
//#include <telephony/ril_cdma_sms.h>
//#include <cutils/sockets.h>
//#include <cutils/jstring.h>
//#include <telephony/record_stream.h>
#include <utils/Log.h>
#include <utils/SystemClock.h>
#include <pthread.h>
#include <binder/Parcel.h>
#include <pwd.h>

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
//#include "atchannels.h"
//#include "at_tok.h"
//#include "misc.h"
//#include <getopt.h>
#include <sys/socket.h>
#include <cutils/sockets.h>
#include <termios.h>

//#include <ril_callbacks.h>
#include <utils/Log.h>
#include <rilc.h>

namespace android {

/************************************************************/
#define PHONE_PROCESS "radio"
#define SOCKET_NAME_IMS_RIL "rild-ims"
// match with constant in ImsRILAdapter.java
#define MAX_COMMAND_BYTES (8 * 1024)
/* Negative values for private RIL errno's */
#define RIL_ERRNO_INVALID_RESPONSE -1
#define PRINTBUF_SIZE 8096
#define NUM_ELEMS(a)     (sizeof (a) / sizeof (a)[0])
#define ANDROID_WAKE_LOCK_NAME "ims-radio-interface"
/* Constants for response types */
#define RESPONSE_SOLICITED 0
#define RESPONSE_UNSOLICITED 1
#define IMS_COUNT 1


static SocketListenParam s_ril_param_socket[IMS_COUNT];
static struct ril_event s_commands_event[IMS_COUNT];
static struct ril_event s_listen_event[IMS_COUNT];

static pthread_mutex_t s_pendingRequestsMutex[IMS_COUNT] = {PTHREAD_MUTEX_INITIALIZER};
static RequestInfo *s_pendingRequests[IMS_COUNT] = {NULL};

static UserCallbackInfo *s_last_wake_timeout_info = NULL;
static AtResponseList* pendedUrcList1 = NULL;

#if RILC_LOG
    static char printBuf[PRINTBUF_SIZE];

    #define startResponse           sprintf(printBuf, "%s {", printBuf)
    #define closeResponse           sprintf(printBuf, "%s}", printBuf)
    #define printResponse           RLOGD("%s", printBuf)

    #define clearPrintBuf           printBuf[0] = 0
    #define removeLastChar          printBuf[strlen(printBuf)-1] = 0
    #define appendPrintBuf(x...)    sprintf(printBuf, x)
#else
    #define startRequest
    #define closeRequest
    #define printRequest(token, req)
    #define startResponse
    #define closeResponse
    #define printResponse
    #define clearPrintBuf
    #define removeLastChar
    #define appendPrintBuf(x...)
#endif
/************************************************************/
static pthread_mutex_t s_ims_writeMutex = PTHREAD_MUTEX_INITIALIZER;
static int s_ims_fdCommand = -1;
static int s_ims_fdListen = -1;
static int s_ims_registerCalled = 0;
static const struct timeval TIMEVAL_WAKE_TIMEOUT = {1,0};
/**********************************************/
static int responseNotSupport(Parcel &p, void *response, size_t responselen);
static int responseRilSignalStrength(Parcel & p,void * response,size_t responselen);
static int responseDataCallList(Parcel & p,void * response,size_t responselen);
static int responseSsn(Parcel & p,void * response,size_t responselen);
static int responseSimRefresh(Parcel & p,void * response,size_t responselen);
static int responseCdmaSms(Parcel & p,void * response,size_t responselen);
static int responseCdmaCallWaiting(Parcel & p,void * response,size_t responselen);
static int responseCdmaInformationRecords(Parcel & p,void * response,size_t responselen);
static int responseCellInfoList(Parcel & p,void * response,size_t responselen);
static int responseHardwareConfig(Parcel & p,void * response,size_t responselen);
static int responseDcRtInfo(Parcel & p,void * response,size_t responselen);
static int responseEtwsNotification(Parcel & p,void * response,size_t responselen);
static int responseSetupDedicateDataCall(Parcel & p,void * response,size_t responselen);
static int responseCrssN(Parcel & p,void * response,size_t responselen);
static int responseEpcNetworkFeatureInfo(Parcel & p,void * response,size_t responselen);
static int responseInts(Parcel &p, void *response, size_t responselen);
static int responseString(Parcel &p, void *response, size_t responselen);
static int responseStrings(Parcel &p, void *response, size_t responselen);
static int responseVoid(Parcel &p, void *response, size_t responselen);
static int responseCallRing(Parcel &p, void *response, size_t responselen);
static int responseRaw(Parcel &p, void *response, size_t responselen);
static void writeStringToParcel(Parcel &p, const char *s);
static int sendResponse (Parcel &p, int socket_fd);
static int sendResponseRaw (const void *data, size_t dataSize, int sock_fd);
static void rilEventAddWakeup(struct ril_event *ev);
static void IMS_sendPendedUrcs(int socket_id, int fdCommand);

/**********************************************/
static UserCallbackInfo * internalRequestTimedCallback
    (RIL_TimedCallback callback, void *param,
        const struct timeval *relativeTime);
/**********************************************/
/** Index == requestNumber */
UnsolResponseInfo ims_s_unsolResponses[] = {
//#include "ims_ril_unsol_commands.h"
#include "ril_unsol_commands.h"
};
#ifdef MTK_RIL
UnsolResponseInfo ims_s_mtk_unsolResponses[] = {
//#include "ims_ril_unsol_commands_mtk.h"
#include "mtk_ril_unsol_commands.h"
};
#endif

static int responseNotSupport(Parcel &p, void *response, size_t responselen) {
    RLOGE("IMS: response URC not support in IMS !!");
    return RIL_ERRNO_INVALID_RESPONSE;
}

static int responseRilSignalStrength(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseDataCallList(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseSsn(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseSimRefresh(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseCdmaSms(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseCdmaCallWaiting(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseCdmaInformationRecords(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseCellInfoList(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseHardwareConfig(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseDcRtInfo(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseEtwsNotification(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseSetupDedicateDataCall(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseCrssN(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseEpcNetworkFeatureInfo(Parcel & p,void * response,size_t responselen) {
    return responseNotSupport(p, response, responselen);
}

static int responseInts(Parcel &p, void *response, size_t responselen) {
    int numInts;

    if (response == NULL && responselen != 0) {
        RLOGE("invalid response: NULL");
        return RIL_ERRNO_INVALID_RESPONSE;
    }
    if (responselen % sizeof(int) != 0) {
        RLOGE("invalid response length %d expected multiple of %d\n",
            (int)responselen, (int)sizeof(int));
        return RIL_ERRNO_INVALID_RESPONSE;
    }

    int *p_int = (int *) response;

    numInts = responselen / sizeof(int);
    p.writeInt32 (numInts);

    /* each int*/
    startResponse;
    for (int i = 0 ; i < numInts ; i++) {
        appendPrintBuf("%s%d,", printBuf, p_int[i]);
        p.writeInt32(p_int[i]);
    }
    removeLastChar;
    closeResponse;

    return 0;
}

static int responseString(Parcel &p, void *response, size_t responselen) {
    /* one string only */
    startResponse;
    appendPrintBuf("%s%s", printBuf, (char*)response);
    closeResponse;

    writeStringToParcel(p, (const char *)response);

    return 0;
}

static int responseStrings(Parcel &p, void *response, size_t responselen) {
    int numStrings;

    if (response == NULL && responselen != 0) {
        RLOGE("invalid response: NULL");
        return RIL_ERRNO_INVALID_RESPONSE;
    }
    if (responselen % sizeof(char *) != 0) {
        RLOGE("invalid response length %d expected multiple of %d\n",
            (int)responselen, (int)sizeof(char *));
        return RIL_ERRNO_INVALID_RESPONSE;
    }

    if (response == NULL) {
        p.writeInt32 (0);
    } else {
        char **p_cur = (char **) response;

        numStrings = responselen / sizeof(char *);
        p.writeInt32 (numStrings);

        /* each string*/
        startResponse;
        for (int i = 0 ; i < numStrings ; i++) {
            appendPrintBuf("%s%s,", printBuf, (char*)p_cur[i]);
            writeStringToParcel (p, p_cur[i]);
        }
        removeLastChar;
        closeResponse;
    }
    return 0;
}

static int responseVoid(Parcel &p, void *response, size_t responselen) {
    startResponse;
    removeLastChar;
    return 0;
}

static int responseCallRing(Parcel &p, void *response, size_t responselen) {
    if ((response == NULL) || (responselen == 0)) {
        return responseVoid(p, response, responselen);
    } else {
        RLOGE("responseCallRing : not implement !!");
        return 0;
        //return responseCdmaSignalInfoRecord(p, response, responselen);
    }
}

static int responseRaw(Parcel &p, void *response, size_t responselen) {
    if (response == NULL && responselen != 0) {
        RLOGE("invalid response: NULL with responselen != 0");
        return RIL_ERRNO_INVALID_RESPONSE;
    }

    // The java code reads -1 size as null byte array
    if (response == NULL) {
        p.writeInt32(-1);
    } else {
        p.writeInt32(responselen);
        p.write(response, responselen);
    }

    return 0;
}

static void writeStringToParcel(Parcel &p, const char *s) {
    char16_t *s16;
    size_t s16_len;
    s16 = strdup8to16(s, &s16_len);
    p.writeString16(s16, s16_len);
    free(s16);
}

static void grabPartialWakeLock() {
    acquire_wake_lock(PARTIAL_WAKE_LOCK, ANDROID_WAKE_LOCK_NAME);
}

static void releaseWakeLock() {
    release_wake_lock(ANDROID_WAKE_LOCK_NAME);
}

/**
 * Timer callback to put us back to sleep before the default timeout
 */
static void
wakeTimeoutCallback (void *param) {
	RLOGD("IMS: wakeTimeoutCallback and releaseWakeLock()");
    // We're using "param != NULL" as a cancellation mechanism
    if (param == NULL) {
        //RLOGD("wakeTimeout: releasing wake lock");

        releaseWakeLock();
    } else {
        //RLOGD("wakeTimeout: releasing wake lock CANCELLED");
    }
}

/*
static void onNewCommandConnect(RIL_SOCKET_ID socket_id) {
    // Inform we are connected and the ril version
    int rilVer = callForRilVersion();
    RIL_onUnsolicitedResponseSocket(RIL_UNSOL_RIL_CONNECTED,
                                    &rilVer, sizeof(rilVer), socket_id);

    // implicit radio state changed
    RIL_onUnsolicitedResponseSocket(RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED,
                                    NULL, 0, socket_id);

    // Send last NITZ time data, in case it was missed
    if (s_lastNITZTimeData != NULL) {
        sendResponseRaw(s_lastNITZTimeData, s_lastNITZTimeDataSize, socket_id);

        free(s_lastNITZTimeData);
        s_lastNITZTimeData = NULL;
    }

    // Get version string
    if (callForGetVersion() != NULL) {
        const char *version;
        version = callForGetVersion();
        RLOGI("RIL Daemon version: %s\n", version);

        property_set(PROPERTY_RIL_IMPL, version);
    } else {
        RLOGI("RIL Daemon version: unavailable\n");
        property_set(PROPERTY_RIL_IMPL, "unavailable");
    }

}
*/
static void onCommandsSocketClosed(int socket_id) {
    int ret;
    RequestInfo *p_cur;
    /* Hook for current context
       pendingRequestsMutextHook refer to &s_pendingRequestsMutex */
    pthread_mutex_t * pendingRequestsMutexHook = &s_pendingRequestsMutex[socket_id];
    /* pendingRequestsHook refer to &s_pendingRequests */
    RequestInfo **    pendingRequestsHook = &s_pendingRequests[socket_id];

    /* mark pending requests as "cancelled" so we dont report responses */
    ret = pthread_mutex_lock(pendingRequestsMutexHook);
    assert (ret == 0);

    p_cur = *pendingRequestsHook;

    for (p_cur = *pendingRequestsHook
            ; p_cur != NULL
            ; p_cur  = p_cur->p_next
    ) {
        p_cur->cancelled = 1;
    }

    ret = pthread_mutex_unlock(pendingRequestsMutexHook);
    assert (ret == 0);
}

static int processCommandBuffer(void *buffer, size_t buflen, RIL_SOCKET_ID socket_id) {
    RLOGE("IMS processCommandBuffer() !!");
    return 0;
}


static void processCommandsCallback(int fd, short flags, void *param) {
    RecordStream *p_rs;
    void *p_record;
    size_t recordlen;
    int ret;
    SocketListenParam *p_info = (SocketListenParam *)param;

    assert(fd == p_info->fdCommand);

    p_rs = p_info->p_rs;

    for (;;) {
        /* loop until EAGAIN/EINTR, end of stream, or other error */
        ret = record_stream_get_next(p_rs, &p_record, &recordlen);

        if (ret == 0 && p_record == NULL) {
            /* end-of-stream */
            break;
        } else if (ret < 0) {
            break;
        } else if (ret == 0) { /* && p_record != NULL */
            processCommandBuffer(p_record, recordlen, p_info->socket_id);
        }
    }

    if (ret == 0 || !(errno == EAGAIN || errno == EINTR)) {
        /* fatal error or end-of-stream */
        if (ret != 0) {
            RLOGE("IMS error on reading command socket errno:%d\n", errno);
        } else {
            RLOGW("IMS EOS.  Closing command socket.");
        }

        close(fd);
        p_info->fdCommand = -1;

        ril_event_del(p_info->commands_event);

        record_stream_free(p_rs);

        /* start listening for new connections again */
        rilEventAddWakeup(&s_listen_event[p_info->socket_id]);

        onCommandsSocketClosed(p_info->socket_id);
    }
}

static void listenCallback (int fd, short flags, void *param) {
    int ret;
    int err;
    int is_phone_socket;
    int fdCommand = -1;
    RecordStream *p_rs;
    SocketListenParam *p_info = (SocketListenParam *)param;

    struct sockaddr_un peeraddr;
    socklen_t socklen = sizeof (peeraddr);

    struct ucred creds;
    socklen_t szCreds = sizeof(creds);

    struct passwd *pwd = NULL;

    assert (*p_info->fdCommand < 0);
    assert (fd == *p_info->fdListen);

    fdCommand = accept(fd, (sockaddr *) &peeraddr, &socklen);

    if (fdCommand < 0 ) {
        RLOGE("IMS: Error on accept() errno:%d", errno);
        /* start listening for new connections again */
        rilEventAddWakeup(p_info->listen_event);
        return;
    }


    /* check the credential of the other side and only accept socket from
     * phone process
     */

    errno = 0;
    is_phone_socket = 0;

    err = getsockopt(fdCommand, SOL_SOCKET, SO_PEERCRED, &creds, &szCreds);

    if (err == 0 && szCreds > 0) {
        errno = 0;
        pwd = getpwuid(creds.uid);
        if (pwd != NULL) {
            if (strcmp(pwd->pw_name, p_info->processName) == 0) {
                is_phone_socket = 1;
            } else {
                RLOGE("IMS: RILD can't accept socket from process %s", pwd->pw_name);
            }
        } else {
            RLOGE("IMS: Error on getpwuid() errno: %d", errno);
        }
    } else {
        RLOGD("IMS: Error on getsockopt() errno: %d", errno);
    }

    if (!is_phone_socket) {
      RLOGE("IMS: RILD must accept socket from %s", p_info->processName);

      close(fdCommand);
      fdCommand = -1;

      onCommandsSocketClosed(p_info->socket_id);

      // start listening for new connections again
      rilEventAddWakeup(p_info->listen_event);

      return;
    }

    ret = fcntl(fdCommand, F_SETFL, O_NONBLOCK);

    if (ret < 0) {
        RLOGE ("Error setting O_NONBLOCK errno:%d", errno);
    }

    RLOGI("IMS: new connection to %s", SOCKET_NAME_IMS_RIL);

    p_info->fdCommand = fdCommand;

    p_rs = record_stream_new(p_info->fdCommand, MAX_COMMAND_BYTES);

    p_info->p_rs = p_rs;

    ril_event_set (p_info->commands_event, p_info->fdCommand, 1,
        p_info->processCommandsCallback, p_info);

    rilEventAddWakeup (p_info->commands_event);

    //onNewCommandConnect(p_info->socket_id);
    IMS_RIL_onUnsolicitedResponse(RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED, (void *)0, 0);

#ifdef MTK_RIL
    IMS_sendPendedUrcs(p_info->socket_id, p_info->fdCommand);
#endif
}

static void rilEventAddWakeup(struct ril_event *ev) {
RLOGI("IMS: Start to rilEventAddWakeup");
    ril_event_add(ev);
    triggerEvLoop();
}

static void startListen(SocketListenParam* socket_listen_p) {
    int fdListen = -1;
    int ret;
    char socket_name[10];

    memset(socket_name, 0, sizeof(char)*10);
    strncpy(socket_name, SOCKET_NAME_IMS_RIL, sizeof(SOCKET_NAME_IMS_RIL));
 

    RLOGI("IMS: Start to listen %s", SOCKET_NAME_IMS_RIL);

    fdListen = android_get_control_socket(socket_name);
    if (fdListen < 0) {
        RLOGE("IMS: Failed to get socket %s", socket_name);
        //prevent to exit, just print log for debugging
        //exit(-1);
        return;
    }

    ret = listen(fdListen, 4);

    if (ret < 0) {
        RLOGE("IMS: Failed to listen on control socket '%d': %s",
             fdListen, strerror(errno));
        //prevent to exit, just print log for debugging
        //exit(-1);
        return;
    }
    socket_listen_p->fdListen = fdListen;

    /* note: non-persistent so we can accept only one connection at a time */


    ril_event_set(socket_listen_p->listen_event, fdListen, false,
                listenCallback, socket_listen_p);

    rilEventAddWakeup(socket_listen_p->listen_event);
}

static int blockingWrite(int fd, const void *buffer, size_t len) {
    size_t writeOffset = 0;
    const uint8_t *toWrite;

    toWrite = (const uint8_t *)buffer;

    while (writeOffset < len) {
        ssize_t written;
        do {
            written = write (fd, toWrite + writeOffset,
                                len - writeOffset);
        } while (written < 0 && ((errno == EINTR) || (errno == EAGAIN)));

        if (written >= 0) {
            writeOffset += written;
        } else {   // written < 0
            RLOGE ("RIL Response: unexpected error on write errno:%d", errno);
            close(fd);
            return -1;
        }
    }

    return 0;
}

extern "C"
void IMS_RILA_register () {
    int ret;
    int flags;
    int i = 0;

    RLOGD("IMS: IMS_RILA_register begin");

    if (s_ims_registerCalled > 0) {
        RLOGE("IMS_RILA_register has been called more than once. "
                "Subsequent call ignored");
        return;
    }

    /* Initialize socket1 parameters */
    for (i = 0; i < IMS_COUNT; i++) {
        s_ril_param_socket[i] = {
                            (RIL_SOCKET_ID)(0+i),  /* ims socket_index => 0 */
                            -1,                       /* fdListen */
                            -1,                       /* fdCommand */
                            PHONE_PROCESS,            /* processName */
                            &s_commands_event[i],        /* commands_event */
                            &s_listen_event[i],          /* listen_event */
                            processCommandsCallback,  /* processCommandsCallback */
                            NULL                      /* p_rs */
                            };
    }

    s_ims_registerCalled = 1;
    RLOGI("IMS: s_ims_registerCalled flag set !!");

    startListen(&s_ril_param_socket[0]);
}

static int sendResponse (Parcel &p, int socket_id) {
    //printResponse;
    return sendResponseRaw(p.data(), p.dataSize(), socket_id);
}

static int sendResponseRaw (const void *data, size_t dataSize, int socket_id) {
    int fd = s_ril_param_socket[socket_id].fdCommand;
    int ret;
    uint32_t header;
    pthread_mutex_t * writeMutexHook = &s_ims_writeMutex;

    RLOGE("Send Response to %s", SOCKET_NAME_IMS_RIL);

    if (fd < 0) {
        RLOGE("Send Response, but fd is incorrect");
        return -1;
    }

    if (dataSize > MAX_COMMAND_BYTES) {
        RLOGE("RIL: packet larger than %u (%u)",
                MAX_COMMAND_BYTES, (unsigned int )dataSize);

        return -1;
    }

    pthread_mutex_lock(writeMutexHook);

    header = htonl(dataSize);

    ret = blockingWrite(fd, (void *)&header, sizeof(header));

    if (ret < 0) {
        pthread_mutex_unlock(writeMutexHook);
        return ret;
    }

    ret = blockingWrite(fd, data, dataSize);

    if (ret < 0) {
        pthread_mutex_unlock(writeMutexHook);
        return ret;
    }

    pthread_mutex_unlock(writeMutexHook);

    return 0;
}

static void IMS_cacheUrc(int unsolResponse, void *data, size_t datalen, int socket_id){
    //Only the URC list we wanted.
    if (unsolResponse != RIL_UNSOL_IMS_REGISTRATION_INFO) {
        RLOGI("Don't need to cache the request = %d ", unsolResponse);
        return;
    }
    AtResponseList* urcCur = NULL;
    AtResponseList* urcPrev = NULL;
    int pendedUrcCount = 0;

    switch(socket_id) {
        case 0:
            urcCur = pendedUrcList1;
            break;
        default:
            RLOGE("IMS: Socket id is wrong!!");
            return;
    }
    while (urcCur != NULL) {
        RLOGD("IMS: Pended URC:%d, IMS_RILD:%d, :%s",
            pendedUrcCount,
            0,
            requestToString(urcCur->id));
        urcPrev = urcCur;
        urcCur = urcCur->pNext;
        pendedUrcCount++;
    }
    urcCur = (AtResponseList*)malloc(sizeof(AtResponseList));
    if (urcPrev != NULL)
        urcPrev->pNext = urcCur;
    urcCur->pNext = NULL;
    urcCur->id = unsolResponse;
    urcCur->datalen = datalen;
    urcCur->data = (char*)malloc(datalen + 1);
    urcCur->data[datalen] = 0x0;
    memcpy(urcCur->data, data, datalen);
    if (pendedUrcCount == 0) {
        switch(socket_id) {
            case 0:
                pendedUrcList1 = urcCur;
                break;
            default:
                RLOGE("IMS: Socket id is wrong!!");
                return;
        }
    }
    RLOGD("IMS: Current pendedUrcCount = %d", pendedUrcCount + 1);
}

static void IMS_sendUrc(int socket_id, AtResponseList* urcCached) {
    AtResponseList* urc = urcCached;
    AtResponseList* urc_temp;
    while (urc != NULL) {
        RLOGD("IMS: sendPendedUrcs IMS_RIL:%d, %s",
        socket_id,
        requestToString(urc->id));
        IMS_RIL_onUnsolicitedResponseSocket (urc->id, urc->data, urc->datalen, socket_id);
        free(urc->data);
        urc_temp = urc;
        urc = urc->pNext;
        free(urc_temp);
    }
}

static void IMS_sendPendedUrcs(int socket_id, int fdCommand) {
    RLOGD("IMS: Ready to send pended URCs, socket:%d, fdCommand:%d", socket_id, fdCommand);
    if ((0 == socket_id) && (fdCommand != -1)) {
        IMS_sendUrc(socket_id, pendedUrcList1);
        pendedUrcList1 = NULL;
    }
}

extern "C" 
void IMS_RIL_onUnsolicitedResponse(int unsolResponse, void *data, size_t datalen)
{
    IMS_RIL_onUnsolicitedResponseSocket(unsolResponse, data, datalen, 0);
}

extern "C" 
void IMS_RIL_onUnsolicitedResponseSocket(int unsolResponse, void *data, size_t datalen, int socket_index)
{
    int unsolResponseIndex;
    int ret;
    int64_t timeReceived = 0;
    bool shouldScheduleTimeout = false;
    RIL_RadioState newState;
    //RIL_SOCKET_ID soc_id = socket_id;
    int soc_id = socket_index;
#ifdef MTK_RIL
    int fdCommand = -1;
    WakeType wakeType = WAKE_PARTIAL;
#endif

    if (s_ims_registerCalled == 0) {
        // Ignore RIL_onUnsolicitedResponse before RIL_register
        RLOGW("IMS_RIL_onUnsolicitedResponse called before IMS_RILA_register");
        return;
    }

    unsolResponseIndex = unsolResponse - RIL_UNSOL_RESPONSE_BASE;

    if ((unsolResponseIndex < 0)
        || (unsolResponseIndex >= (int32_t)NUM_ELEMS(ims_s_unsolResponses))) {
    #ifdef MTK_RIL
        if (unsolResponse > (RIL_UNSOL_VENDOR_BASE + (int32_t)NUM_ELEMS(ims_s_mtk_unsolResponses)))
    #endif /* MTK_RIL */
        {
            RLOGE("unsupported unsolicited response code %d", unsolResponse);
            return;
        }
    }

#ifdef MTK_RIL
    fdCommand = s_ril_param_socket[soc_id].fdCommand;

    if (fdCommand == -1) {
        RLOGD("IMS: Can't send URC because there is no ims connection yet.");
        IMS_cacheUrc(unsolResponse, data, datalen , 0);
        return;
    }
#endif

   // Grab a wake lock if needed for this reponse,
   // as we exit we'll either release it immediately
   // or set a timer to release it later.
#ifdef MTK_RIL
    if (unsolResponse >= RIL_UNSOL_VENDOR_BASE) {
        unsolResponseIndex = unsolResponse - RIL_UNSOL_VENDOR_BASE;
        wakeType = ims_s_mtk_unsolResponses[unsolResponseIndex].wakeType;
    }
    else
#endif /* MTK_RIL */
    {
        wakeType = ims_s_unsolResponses[unsolResponseIndex].wakeType;
    }

    // Grab a wake lock if needed for this reponse,
    // as we exit we'll either release it immediately
    // or set a timer to release it later.
    switch (wakeType) {
        case WAKE_PARTIAL:
            grabPartialWakeLock();
            shouldScheduleTimeout = true;
        break;

        case DONT_WAKE:
        default:
            // No wake lock is grabed so don't set timeout
            shouldScheduleTimeout = false;
            break;
    }

    // Mark the time this was received, doing this
    // after grabing the wakelock incase getting
    // the elapsedRealTime might cause us to goto
    // sleep.
    if (unsolResponse == RIL_UNSOL_NITZ_TIME_RECEIVED) {
        timeReceived = elapsedRealtime();
    }

    //appendPrintBuf("[UNSL]< %s", requestToString(unsolResponse));

    Parcel p;

    p.writeInt32 (RESPONSE_UNSOLICITED);
    p.writeInt32 (unsolResponse);

#ifdef MTK_RIL
    if (unsolResponse >= RIL_UNSOL_VENDOR_BASE) {
        ret = ims_s_mtk_unsolResponses[unsolResponseIndex]
              .responseFunction(p, const_cast<void*>(data), datalen);
    } else
#endif /* MTK_RIL */
    {
        ret = ims_s_unsolResponses[unsolResponseIndex]
              .responseFunction(p, const_cast<void*>(data), datalen);
    }

    if (ret != 0) {
        // Problem with the response. Don't continue;
        goto error_exit;
    }


    //RLOGI("%s UNSOLICITED: %s length:%d", rilSocketIdToString(soc_id), requestToString(unsolResponse), p.dataSize());
    ret = sendResponse(p, soc_id);

    // For now, we automatically go back to sleep after TIMEVAL_WAKE_TIMEOUT
    // FIXME The java code should handshake here to release wake lock

    if (shouldScheduleTimeout) {
        // Cancel the previous request
        if (s_last_wake_timeout_info != NULL) {
            s_last_wake_timeout_info->userParam = (void *)1;
        }
        s_last_wake_timeout_info = internalRequestTimedCallback(wakeTimeoutCallback, NULL, &TIMEVAL_WAKE_TIMEOUT);
    }

    // Normal exit
    return;

error_exit:
    if (shouldScheduleTimeout) {
        releaseWakeLock();
    }
}

/** FIXME generalize this if you track UserCAllbackInfo, clear it
    when the callback occurs
*/
static UserCallbackInfo *
internalRequestTimedCallback (RIL_TimedCallback callback, void *param,
                                const struct timeval *relativeTime)
{
    struct timeval myRelativeTime;
    UserCallbackInfo *p_info;

    p_info = (UserCallbackInfo *) malloc (sizeof(UserCallbackInfo));

    p_info->p_callback = callback;
    p_info->userParam = param;
#ifdef MTK_RIL
    p_info->cid = (RILChannelId)-1;
#endif

    if (relativeTime == NULL) {
        /* treat null parameter as a 0 relative time */
        memset (&myRelativeTime, 0, sizeof(myRelativeTime));
    } else {
        /* FIXME I think event_add's tv param is really const anyway */
        memcpy (&myRelativeTime, relativeTime, sizeof(myRelativeTime));
    }

    ril_event_set(&(p_info->event), -1, false, userTimerCallback, p_info);

    ril_timer_add(&(p_info->event), &myRelativeTime);

    triggerEvLoop();
    return p_info;
}

}/* namespace android */

