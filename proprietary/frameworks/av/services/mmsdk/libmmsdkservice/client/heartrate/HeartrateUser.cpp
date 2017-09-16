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

//#define LOG_NDEBUG 0
#define LOG_TAG "mmsdk/HeartrateUser"

#include <utils/Log.h>
#include <stdint.h>
#include <sys/types.h>
#include <binder/Parcel.h>
#include <binder/IPCThreadState.h>

#include <utils/log.h>
#include <mtkcam/common.h>

using namespace android; 
//
#include "MyUtils.h"
using namespace MtkCamUtils;
#include <mtkcam/v1/IParamsManager.h>
#include <mtkcam/v1/ICamClient.h>
#include <mtkcam/device/Cam1Device.h>
#include <mmsdk/heartrate_device.h>
#include <camera/MtkCamera.h>


// 
#include <mmsdk/IHeartrateUser.h>
#include <mmsdk/IHeartrateDetectionListener.h>
#include "../include/HeartrateUser.h"



static int getCallingPid() {
    return android::IPCThreadState::self()->getCallingPid();
}

#include <cutils/xlog.h>
#define MY_LOGV(fmt, arg...)       XLOGV(fmt"\r\n", ##arg)
#define MY_LOGD(fmt, arg...)       XLOGD(fmt"\r\n", ##arg)
#define MY_LOGI(fmt, arg...)       XLOGI(fmt"\r\n", ##arg)
#define MY_LOGW(fmt, arg...)       XLOGW(fmt"\r\n", ##arg)
#define MY_LOGE(fmt, arg...)       XLOGE(fmt" (%s){#%d:%s}""\r\n", ##arg, __FUNCTION__, __LINE__, __FILE__)

#define FUNCTION_LOG_START    MY_LOGD("[%s]+, (pid=%d, tid=%d)", __FUNCTION__, getCallingPid(), gettid()); 
#define FUNCTION_LOG_END      MY_LOGD("[%s]-", __FUNCTION__); 

namespace android {
namespace NSMMSdk {
namespace NSHeartrate {

HeartrateUser::
HeartrateUser()
    :mListenerListMap()
    ,mpCamDev(NULL)
    ,mLock()
    ,mClientEnabled(false)
{

}


/******************************************************************************
 *
 ******************************************************************************/
void
HeartrateUser::
disconnect(
) 
{
    FUNCTION_LOG_START
    MY_LOGD("disconnect"); 
    Mutex::Autolock _l(mLock);    
    mListenerListMap.clear();

    FUNCTION_LOG_END
    return ;
}

/******************************************************************************
 *
 ******************************************************************************/
void
HeartrateUser::
start(
) 
{
    FUNCTION_LOG_START 
    Mutex::Autolock _l(mLock);    
    //
    if ((mpCamDev != 0) && (mClientEnabled == false))
    {
        MUINTPTR ptr;
        MUINT HiPtr, LoPtr;
        MY_LOGD("enable heartrate client");
        MY_LOGD("set Heartrate callback : CB = %p, User = %p", fgHeartrateCb, this);
        ptr = (MUINTPTR)fgHeartrateCb;
        HiPtr = (ptr >> 32);
        LoPtr = ptr & 0xFFFFFFFF;
        mpCamDev->sendCommand(CAMERA_CMD_SETCB_HR_PREVIEW, LoPtr, HiPtr); // set heartrate callback
        //MY_LOGD("fgHeartrateCb : 0x%X, 0x%X", HiPtr, LoPtr);
        ptr = (MUINTPTR)this;
        HiPtr = (ptr >> 32);
        LoPtr = ptr & 0xFFFFFFFF;
        //MY_LOGD("HeartrateUser : 0x%X, 0x%X", HiPtr, LoPtr);
        mpCamDev->sendCommand(CAMERA_CMD_SETUSER_HR_PREVIEW, LoPtr, HiPtr); // set heartrate callback
        mpCamDev->sendCommand(CAMERA_CMD_START_HR_PREVIEW, 0, 0); // set camera to do heart rate detection(start face detection)
        mClientEnabled = true; 
    }

    FUNCTION_LOG_END
    return ;
}

/******************************************************************************
 *
 ******************************************************************************/
void
HeartrateUser::
stop(
) 
{
    FUNCTION_LOG_START
    MY_LOGD("stop"); 
    Mutex::Autolock _l(mLock);    

    if (mClientEnabled && (mpCamDev != 0))
    {
        MY_LOGD("disable heartrate client"); 
        mpCamDev->sendCommand(CAMERA_CMD_STOP_HR_PREVIEW,0,0); //stop heart rate detection
        mClientEnabled = false; 
    } else if (mClientEnabled) {
        mClientEnabled = false;
    }

    FUNCTION_LOG_END
    return ;
}

/******************************************************************************
 *
 ******************************************************************************/
void
HeartrateUser::
query(int *HasCamDev, int *IsHRStart)
{
    FUNCTION_LOG_START
    Mutex::Autolock _l(mLock);

    *IsHRStart = mClientEnabled;

    if(mpCamDev != 0) {
        *HasCamDev = 1;
    } else {
        *HasCamDev = 0;
    }

    FUNCTION_LOG_END
    return ;
}


/******************************************************************************
 *
 ******************************************************************************/
void
HeartrateUser::
RegisterCamDev(
    NSCam::Cam1Device *dev
) 
{
    FUNCTION_LOG_START
    MY_LOGD("Register Camera device");
    HR_detection_result_t result;
    {
        Mutex::Autolock _l(mLock);
        mpCamDev = dev;
    }

    result.ReplyType = HEARTRATE_DEVICE_REPLYTYPE_CAMCONNECT;

    onHeartrateCb(result);

    FUNCTION_LOG_END
    return ;
}

/******************************************************************************
 *
 ******************************************************************************/
void
HeartrateUser::
unRegisterCamDev(
    NSCam::Cam1Device *dev
) 
{
    FUNCTION_LOG_START
    bool ReleaseCam = false;
    MY_LOGD("unregister");
    {
        Mutex::Autolock _l(mLock);
        if(mpCamDev == dev) {
            mpCamDev = NULL;
            ReleaseCam = true;
        }
    }
    if(ReleaseCam) {
        HR_detection_result_t result;
        result.ReplyType = HEARTRATE_DEVICE_REPLYTYPE_CAMDISCONNECT;
        onHeartrateCb(result);
    }

    FUNCTION_LOG_END
    return ;
}


/******************************************************************************
 *
 ******************************************************************************/

bool 
HeartrateUser::
fgHeartrateCb(HR_detection_result_t const &result, void* user)
{
    FUNCTION_LOG_START
    HeartrateUser *pHeartrateUser = reinterpret_cast <HeartrateUser *>(user); 
    if (NULL != pHeartrateUser) 
    {
        pHeartrateUser->onHeartrateCb(result); 
    }
    FUNCTION_LOG_END
    return MTRUE; 
}


void 
HeartrateUser::
onHeartrateCb(HR_detection_result_t const &result)
{
     FUNCTION_LOG_START
    for (int i = 0; i < mListenerListMap.size(); i++)
    {
          sp<IHeartrateDetectionListener> pListener = (mListenerListMap.keyAt(i));   

          HeartrateDetectionEvent event; 
          event.heartbeats =  result.heartbeats; 
          event.id = result.id;
          event.confidence = result.confidence; 
          event.boundBox = Rect(result.rect.leftTop().x,
          	                           result.rect.leftTop().y,
          	                           result.rect.rightBottom().x, 
          	                           result.rect.rightBottom().y);                	                           

          MY_LOGD("callback to listener"); 
          pListener->onHeartrateDetected(event); 
     }
     FUNCTION_LOG_END
}

/******************************************************************************
 *
 ******************************************************************************/
bool
HeartrateUser::
addHeartrateDetectionListener(
    const sp<IHeartrateDetectionListener> &listener, 
    int arg
)
{
    FUNCTION_LOG_START 
    MY_LOGD("listener:%p, arg:%d", listener.get(), arg);
    
    Mutex::Autolock _l(mLock);    
    for (int i = 0; i < mListenerListMap.size(); i++)
    {
          sp<IHeartrateDetectionListener> pListener = (mListenerListMap.keyAt(i));   
         if (pListener->asBinder() == listener->asBinder())
         {
             MY_LOGE("listener already added");
             return false;
         }
    }
#if 0
    //        
    //if ((mpCamDev != 0) && (mClientEnabled == false))
    if ((mpCamDev != 0))
    {
        MY_LOGD("set Heartrate callback");
        mpCamDev->sendCommand(CAMERA_CMD_SETCB_HR_PREVIEW, (int)fgHeartrateCb, 0); // set heartrate callback
        mpCamDev->sendCommand(CAMERA_CMD_SETUSER_HR_PREVIEW, (int)this, 0); // set heartrate callback
        //mpCamDev->sendCommand(CAMERA_CMD_START_HR_PREVIEW, 0, 0); // set camera to do heart rate detection(start face detection)
        //mClientEnabled = true; 
    }
#endif

    mListenerListMap.add(listener, arg);

    FUNCTION_LOG_END    
    return true; 
} 
 

/******************************************************************************
 *
 ******************************************************************************/
bool
HeartrateUser::
removeHeartrateDetectionListener(
    const sp<IHeartrateDetectionListener> &listener, 
    int arg
)
{
    FUNCTION_LOG_START 
    MY_LOGD("listener:%p, arg:%d", listener.get(), arg);    	 
    Mutex::Autolock _l(mLock);        	
    //
    int listenerFound = 0; 
    for (int i = 0; i < mListenerListMap.size(); i++)
    {
         sp<IHeartrateDetectionListener> pListener = (mListenerListMap.keyAt(i));   
         if (pListener->asBinder() == listener->asBinder())
         {
             MY_LOGD("listen found");
             mListenerListMap.removeItemsAt(i); 
             listenerFound = 1; 
             break; 
         }
    }
    if (listenerFound == 0)
    {
        MY_LOGE("no listener add before"); 
        return false; 
    }
    MY_LOGD("num of listener:%d, clientEnable:%d", mListenerListMap.size(), mClientEnabled);
#if 0
    if (0 == mListenerListMap.size() && mClientEnabled)
    {
        MY_LOGD("last listener, disable gesture client"); 
        mpCamDev->sendCommand(CAMERA_CMD_STOP_HR_PREVIEW,0,0); //stop heart rate detection
        mClientEnabled = false; 
    }
#endif

    FUNCTION_LOG_END    
    return true; 
} 



// ----------------------------------------------------------------------------
}; // namespace NSHeartrate
}; // namespace NSMMSdk
}; // namespace android
