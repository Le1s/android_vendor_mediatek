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

/********************************************************************************************
 *     LEGAL DISCLAIMER
 *
 *     (Header of MediaTek Software/Firmware Release or Documentation)
 *
 *     BY OPENING OR USING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *     THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE") RECEIVED
 *     FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON AN "AS-IS" BASIS
 *     ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES, EXPRESS OR IMPLIED,
 *     INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 *     A PARTICULAR PURPOSE OR NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY
 *     WHATSOEVER WITH RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK
 *     ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
 *     NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S SPECIFICATION
 *     OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
 *
 *     BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE LIABILITY WITH
 *     RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION,
TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE
 *     FEES OR SERVICE CHARGE PAID BY BUYER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE WITH THE LAWS
 *     OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF LAWS PRINCIPLES.
 ************************************************************************************************/
#define LOG_TAG "CamExif"
//
#include <string.h>
#include <cutils/properties.h>
//
#include <mtkcam/Log.h>
#include <mtkcam/common.h>
//
#include <mtkcam/exif/StdExif.h>
//
#include <stdlib.h>
#include <IBaseExif.h>
#include <Exif.h>
//
#include <limits>
using namespace std;


/*******************************************************************************
*
********************************************************************************/
#define MY_LOGD(fmt, arg...)        CAM_LOGD("(%d)[%s] "fmt, ::gettid(), __FUNCTION__, ##arg)
#define MY_LOGI(fmt, arg...)        CAM_LOGI("(%d)[%s] "fmt, ::gettid(), __FUNCTION__, ##arg)
#define MY_LOGW(fmt, arg...)        CAM_LOGW("(%d)[%s] "fmt, ::gettid(), __FUNCTION__, ##arg)
#define MY_LOGE(fmt, arg...)        CAM_LOGE("(%d)[%s] "fmt, ::gettid(), __FUNCTION__, ##arg)
#define MY_LOGA(fmt, arg...)        CAM_LOGA("(%d)[%s] "fmt, ::gettid(), __FUNCTION__, ##arg)


/*******************************************************************************
*
********************************************************************************/
StdExif::
StdExif()
    : miDebugEnable(0)
    , mpThumbnail(NULL)
    , mThumbnailSize(0)
    , mMaxThumbSize(0)
    , mpOutputExifBuf(NULL)
    , mOutputExifBufSize(0)
    , mpExifParam(NULL)
    , mpBaseExif(NULL)
{
    char cEnable[PROPERTY_VALUE_MAX] = {'\0'};
    ::property_get("debug.exif.optimize.enable", cEnable, "0");
    miDebugEnable = ::atoi(cEnable);
    MY_LOGI("- this:%p, debug.exif.optimize.enable=%d", this, miDebugEnable);
}

/*******************************************************************************
*
********************************************************************************/
StdExif::
~StdExif()
{
    MY_LOGI("- this:%p", this);
}

/*******************************************************************************
*
********************************************************************************/
MBOOL
StdExif::
init()
{
    // Exif Utilitis
    mpBaseExif = new ExifUtils();
    if ( !(mpBaseExif->init()) )
    {
        MY_LOGE("mpBaseExif->init() fail");
        return MFALSE;
    }
    //
    return  MTRUE;
}


/*******************************************************************************
*
********************************************************************************/
MBOOL
StdExif::
uninit()
{
    //
    if ( mpBaseExif != NULL )
    {
        if ( !(mpBaseExif->uninit()) )
        {
            MY_LOGE("mpBaseExif->uninit() fail");
        }
        delete mpBaseExif;
        mpBaseExif = NULL;
    }
    //
    return  MTRUE;
}

/*******************************************************************************
*
********************************************************************************/
MBOOL
StdExif::
reset()
{
    uninit();
    init();

    return  MTRUE;
}

/*******************************************************************************
*
********************************************************************************/
void
StdExif::
setStdExifParams(
    StdExifParams const&    rStdExifParams
)
{
    mpExifParam = &rStdExifParams;
    MY_LOGI("mpExifParam(0x%x), ImageSize(%dx%d), FNumber(%d/10), FocalLegth(%d/100), AWBMode(%d), Strobe(%d)",
        mpExifParam, mpExifParam->u4ImageWidth, mpExifParam->u4ImageHeight, mpExifParam->u4FNumber,
        mpExifParam->u4FocalLength, mpExifParam->u4AWBMode, mpExifParam->u4FlashLightTimeus);
    MY_LOGI("AEMeterMode(%d), AEExpBias(%d), CapExposureTime(%d), AEISOSpeed(%d), LightSource(%d)",
        mpExifParam->u4AEMeterMode, mpExifParam->i4AEExpBias, mpExifParam->u4CapExposureTime,
        mpExifParam->u4AEISOSpeed, mpExifParam->u4LightSource);
    MY_LOGI("ExpProgram(%d), SceneCapType(%d), Orientation(%d), ZoomRatio(%d), Facing(%d)",
        mpExifParam->u4ExpProgram, mpExifParam->u4SceneCapType,
        mpExifParam->u4Orientation, mpExifParam->u4ZoomRatio, mpExifParam->u4Facing);
    MY_LOGI("GPS(%d), Altitude(%d), Latitude(%s), Longitude(%s), TimeStamp(%s), ProcessingMethod(%s)",
        mpExifParam->u4GpsIsOn, mpExifParam->u4GPSAltitude, mpExifParam->uGPSLatitude,
        mpExifParam->uGPSLongitude, mpExifParam->uGPSTimeStamp, mpExifParam->uGPSProcessingMethod);
}

/*******************************************************************************
*
********************************************************************************/
size_t
StdExif::
getStdExifOffset() const
{
    size_t offset = mpBaseExif->exifApp1SizeGet();
    return offset;
}

/*******************************************************************************
*
********************************************************************************/
size_t
StdExif::
getHeaderOffset() const
{
    size_t offset = getStdExifOffset() + mMaxThumbSize; //+ debug exif
    MY_LOGI("header offset(%d)", offset);
    return offset;
}

/*******************************************************************************
*
********************************************************************************/
void
StdExif::
setMaxThumbnail(
    size_t const        thumbnailSize
)
{
    mMaxThumbSize   = thumbnailSize;
    MY_LOGI("max thumbnail size(%d)", mMaxThumbSize);
}

/*******************************************************************************
*
********************************************************************************/
void
StdExif::
setThumbnail(
    MINT8 const * const thumbnail,
    size_t const        thumbnailSize
)
{
    mpThumbnail     = (MINT8*)thumbnail;
    mThumbnailSize  = thumbnailSize;
    mMaxThumbSize   = thumbnailSize;
    MY_LOGW("should be removed!! thumbnail(0x%x), size(%d)", mpThumbnail, mThumbnailSize);
}

/*******************************************************************************
*
********************************************************************************/
void
StdExif::
setOutputBuffer(
    MINT8 * const   outputExifBuf,
    size_t const    outputExifBufCapacity

)
{
    mpOutputExifBuf     = (MUINTPTR)outputExifBuf;
    mOutputExifBufSize  = outputExifBufCapacity;
    MY_LOGI("out buffer(0x%x), size(%d)", mpOutputExifBuf, mOutputExifBufSize);
}

/*******************************************************************************
*
********************************************************************************/
status_t
StdExif::
make(
    size_t& rOutputExifSize
)
{
    // set 0 first for error return
    rOutputExifSize = 0;

    exifAPP1Info_t exifApp1Info;
    exifImageInfo_t exifImgInfo;

    //  (1) Fill exifApp1Info
    ::memset(&exifApp1Info, 0, sizeof(exifAPP1Info_t));

    /*********************************************************************************
                                           GPS
    **********************************************************************************/
    if  (mpExifParam->u4GpsIsOn == 1) {
        float latitude = atof((char*)mpExifParam->uGPSLatitude);
        float longitude = atof((char*)mpExifParam->uGPSLongitude);
        long long timestamp = atol((char*)mpExifParam->uGPSTimeStamp);
        char const*pgpsProcessingMethod = (char*)mpExifParam->uGPSProcessingMethod;
        //
        // Set GPS Info
        if (latitude >= 0) {
            strcpy((char *)exifApp1Info.gpsLatitudeRef, "N");
        }
        else {
            strcpy((char *)exifApp1Info.gpsLatitudeRef, "S");
            latitude *= -1;     // make it positive
        }
        if (longitude >= 0) {
            strcpy((char *)exifApp1Info.gpsLongitudeRef, "E");
        }
        else {
            strcpy((char *)exifApp1Info.gpsLongitudeRef, "W");
            longitude *= -1;    // make it positive
        }
        exifApp1Info.gpsIsOn = 1;
        // Altitude
        exifApp1Info.gpsAltitude[0] = mpExifParam->u4GPSAltitude;
        exifApp1Info.gpsAltitude[1] = 1;
        // Latitude
        exifApp1Info.gpsLatitude[0] = (int) latitude;
        exifApp1Info.gpsLatitude[1] = 1;
        latitude -= exifApp1Info.gpsLatitude[0];
        latitude *= 60;
        exifApp1Info.gpsLatitude[2] = (int) latitude;
        exifApp1Info.gpsLatitude[3] = 1;
        latitude -= exifApp1Info.gpsLatitude[2];
        latitude *= 60;
        latitude *= 10000;
        exifApp1Info.gpsLatitude[4] = (int) latitude;
        exifApp1Info.gpsLatitude[5] = 10000;
        // Longtitude
        exifApp1Info.gpsLongitude[0] = (int) longitude;
        exifApp1Info.gpsLongitude[1] = 1;
        longitude -= exifApp1Info.gpsLongitude[0];
        longitude *= 60;
        exifApp1Info.gpsLongitude[2] = (int) longitude;
        exifApp1Info.gpsLongitude[3] = 1;
        longitude -= exifApp1Info.gpsLongitude[2];
        longitude *= 60;
        longitude *= 10000;
        exifApp1Info.gpsLongitude[4] = (int) longitude;
        exifApp1Info.gpsLongitude[5] = 10000;

        // Timestamp
        if ( (timestamp >= 0) && (timestamp <= numeric_limits<long long>::max()))
        {
            time_t tim = (time_t) timestamp;
            struct tm *ptime = gmtime(&tim);
            exifApp1Info.gpsTimeStamp[0] = ptime->tm_hour;
            exifApp1Info.gpsTimeStamp[1] = 1;
            exifApp1Info.gpsTimeStamp[2] = ptime->tm_min;
            exifApp1Info.gpsTimeStamp[3] = 1;
            exifApp1Info.gpsTimeStamp[4] = ptime->tm_sec;
            exifApp1Info.gpsTimeStamp[5] = 1;
            sprintf((char *)exifApp1Info.gpsDateStamp, "%04d:%02d:%02d", ptime->tm_year + 1900, ptime->tm_mon + 1, ptime->tm_mday);
        }
        else
        {
            MY_LOGE("wrong timestamp(%lld)", timestamp);
        }
        // ProcessingMethod
        const char exifAsciiPrefix[] = { 0x41, 0x53, 0x43, 0x49, 0x49, 0x0, 0x0, 0x0 }; // ASCII
        int len1, len2, maxLen;
        len1 = sizeof(exifAsciiPrefix);
        memcpy(exifApp1Info.gpsProcessingMethod, exifAsciiPrefix, len1);
        maxLen = sizeof(exifApp1Info.gpsProcessingMethod) - len1;
        len2 = strlen(pgpsProcessingMethod);
        if (len2 > maxLen) {
            len2 = maxLen;
        }
        memcpy(&exifApp1Info.gpsProcessingMethod[len1], pgpsProcessingMethod, len2);
    }

    /*********************************************************************************
                                           common
    **********************************************************************************/
    // software information
    memset(exifApp1Info.strSoftware, 0, 32);
    strcpy((char *)exifApp1Info.strSoftware, "MediaTek Camera Application");

    // get datetime
    struct tm *tm;
    struct timeval tv;
    gettimeofday(&tv, NULL);
    if((tm = localtime(&tv.tv_sec)) != NULL)
    {
        strftime((char *)exifApp1Info.strDateTime, 20, "%Y:%m:%d %H:%M:%S", tm);
        snprintf((char *)exifApp1Info.strSubSecTime, 3, "%02ld\n", tv.tv_usec);
    }

    // [digital zoom ratio]
    exifApp1Info.digitalZoomRatio[0] = (unsigned int)mpExifParam->u4ZoomRatio;
    exifApp1Info.digitalZoomRatio[1] = 100;
    // [orientation]
    exifApp1Info.orientation = (unsigned short)determineExifOrientation(
                                    mpExifParam->u4Orientation,
                                    mpExifParam->u4Facing
                                );

    /*********************************************************************************
                                           3A
    **********************************************************************************/
    // [f number]
    exifApp1Info.fnumber[0] = (unsigned int)mpExifParam->u4FNumber;
    exifApp1Info.fnumber[1] = 10;

    // [focal length]
    exifApp1Info.focalLength[0] = (unsigned int)mpExifParam->u4FocalLength;
    exifApp1Info.focalLength[1] = 100;

    // [iso speed]
    exifApp1Info.isoSpeedRatings = (unsigned short)mpExifParam->u4AEISOSpeed;

    // [exposure time]
    if(mpExifParam->u4CapExposureTime == 0){
        //YUV sensor
        exifApp1Info.exposureTime[0] = 0;
        exifApp1Info.exposureTime[1] = 0;
    }
    else{
        // RAW sensor
        if (mpExifParam->u4CapExposureTime > 1000000) { //1 sec
            exifApp1Info.exposureTime[0] = mpExifParam->u4CapExposureTime / 100000;
            exifApp1Info.exposureTime[1] = 10;
        }
        else{ // us
            exifApp1Info.exposureTime[0] = mpExifParam->u4CapExposureTime;
            exifApp1Info.exposureTime[1] = 1000000;
        }
    }

    // [flashlight]
    exifApp1Info.flash = (0 != mpExifParam->u4FlashLightTimeus) ? 1 : 0;

    // [white balance mode]
    exifApp1Info.whiteBalanceMode = mpExifParam->u4AWBMode;

    // [light source]
    exifApp1Info.lightSource = mpExifParam->u4LightSource;

    // [metering mode]
    exifApp1Info.meteringMode = mpExifParam->u4AEMeterMode;

    // [exposure program] , [scene mode]
    exifApp1Info.exposureProgram  = mpExifParam->u4ExpProgram;
    exifApp1Info.sceneCaptureType = mpExifParam->u4SceneCapType;

    // [Ev offset]
    exifApp1Info.exposureBiasValue[0] = (unsigned int)mpExifParam->i4AEExpBias;
    exifApp1Info.exposureBiasValue[1] = 10;

    /*********************************************************************************
                                           update customized exif
    **********************************************************************************/
    {
        char make[PROPERTY_VALUE_MAX] = {'\0'};
        char model[PROPERTY_VALUE_MAX] = {'\0'};
        property_get("ro.product.manufacturer", make, "0");
        property_get("ro.product.model", model, "0");
        MY_LOGI("property: make(%s), model(%s)", make, model);
        // [Make]
        if ( ::strcmp(make, "0") != 0 ) {
            ::memset(exifApp1Info.strMake, 0, 32);
            ::strcpy((char*)exifApp1Info.strMake, (const char*)make);
        }
        // [Model]
        if ( ::strcmp(model, "0") != 0 ) {
            ::memset(exifApp1Info.strModel, 0, 32);
            ::strcpy((char*)exifApp1Info.strModel, (const char*)model);
        }
    }

    /*********************************************************************************
                                           MISC
    **********************************************************************************/
    // [flashPixVer]
    memcpy(exifApp1Info.strFlashPixVer, "0100 ", 5);
    // [exposure mode]
    exifApp1Info.exposureMode = 0;  // 0 means Auto exposure


    //  (2) Fill exifImgInfo
    ::memset(&exifImgInfo, 0, sizeof(exifImageInfo_t));
    exifImgInfo.bufAddr     = mpOutputExifBuf;
    exifImgInfo.mainWidth   = mpExifParam->u4ImageWidth;
    exifImgInfo.mainHeight  = mpExifParam->u4ImageHeight;
    exifImgInfo.thumbSize   = mMaxThumbSize;

    int ret;
    unsigned int size = 0;
    ret = mpBaseExif->exifApp1Make(&exifImgInfo, &exifApp1Info, &size);
    rOutputExifSize = (size_t)size;

if ( miDebugEnable == 0 )
{
    //  (3) Append thumbnail
    if ((ret == 0) && (mpThumbnail != NULL) && mThumbnailSize)
    {
        // out of memory check for thumbnail
        if (rOutputExifSize + mThumbnailSize > mOutputExifBufSize) {
            return NO_MEMORY;
        }

        memcpy((MUINT8*)mpOutputExifBuf + rOutputExifSize,
                mpThumbnail,
                mThumbnailSize);

        rOutputExifSize += exifImgInfo.thumbSize;
    }
}
    return (status_t)ret;
}

/*******************************************************************************
*
********************************************************************************/
MINT32
StdExif::determineExifOrientation(
    MUINT32 const   u4DeviceOrientation,
    MBOOL const     bIsFacing,
    MBOOL const     bIsFacingFlip
)
{
    MINT32  result = -1;

    if  ( bIsFacing && bIsFacingFlip )
    {
        //  Front Camera with Flip
        switch  (u4DeviceOrientation)
        {
        case 0:
            result = 1;
            break;
        case 90:
            result = 8;
            break;
        case 180:
            result = 3;
            break;
        case 270:
            result = 6;
            break;
        default:
            result = 1;
            break;
        }
    }
    else
    {   //  Rear Camera or Front Camera without Flip
        switch  (u4DeviceOrientation)
        {
        case 0:
            result = 1;
            break;
        case 90:
            result = 6;
            break;
        case 180:
            result = 3;
            break;
        case 270:
            result = 8;
            break;
        default:
            result = 1;
            break;
        }
    }

    return  result;
}

