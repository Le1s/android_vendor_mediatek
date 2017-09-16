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
#ifndef _STDEXIF_H_
#define _STDEXIF_H_
//

#include <mtkcam/common.h>

using namespace android;
//
class IBaseExif;
//
/*******************************************************************************
*
********************************************************************************/
struct StdExifParams {
    MUINT32     u4ImageWidth;       // Image width
    MUINT32     u4ImageHeight;      // Image height
    //
    MUINT32     u4FNumber;          // Format: F2.8 = 28
    MUINT32     u4FocalLength;      // Format: FL 3.5 = 350
    MUINT32     u4AWBMode;          // White balance mode
    MUINT32     u4LightSource;      // Light Source mode
    MUINT32     u4ExpProgram;       // Exposure Program
    MUINT32     u4SceneCapType;     // Scene Capture Type
    MUINT32     u4FlashLightTimeus; // Strobe on/off
    MUINT32     u4AEMeterMode;      // Exposure metering mode
    MINT32      i4AEExpBias;        // Exposure index*10
    MUINT32     u4CapExposureTime;  //
    MUINT32     u4AEISOSpeed;       // AE ISO value
    //
    MUINT32     u4GpsIsOn;
    MUINT32     u4GPSAltitude;
    MUINT8      uGPSLatitude[32];
    MUINT8      uGPSLongitude[32];
    MUINT8      uGPSTimeStamp[32];
    MUINT8      uGPSProcessingMethod[64];   //(values of "GPS", "CELLID", "WLAN" or "MANUAL" by the EXIF spec.)
    //
    MUINT32     u4Orientation;      // 0, 90, 180, 270
    MUINT32     u4ZoomRatio;        // Digital zoom ratio (x100) For example, 100, 114, and 132 refer to 1.00, 1.14, and 1.32 respectively.
    //
    MUINT32     u4Facing;           // 1: front camera, 0: not front
    //
public:     ////    Operations.
    StdExifParams()  { ::memset(this, 0, sizeof(StdExifParams)); }

};

/*******************************************************************************
*
********************************************************************************/

enum ECapTypeId
{
    eCapTypeId_Standard   = 0,
    eCapTypeId_Landscape  = 1,
    eCapTypeId_Portrait   = 2,
    eCapTypeId_Night      = 3
};

enum EExpProgramId
{
    eExpProgramId_NotDefined    = 0,
    eExpProgramId_Manual        = 1,
    eExpProgramId_Normal        = 2,
    eExpProgramId_Portrait      = 7,
    eExpProgramId_Landscape     = 8
};

enum ELightSourceId
{
    eLightSourceId_Daylight     = 1,
    eLightSourceId_Fluorescent  = 2,
    eLightSourceId_Tungsten     = 3,
    eLightSourceId_Cloudy       = 10,
    eLightSourceId_Shade        = 11,
    eLightSourceId_Other        = 255
};

enum EMeteringModeId
{
    eMeteringMode_Average   = 1,
    eMeteringMode_Center    = 2,
    eMeteringMode_Spot      = 3,
    eMeteringMode_Other     = 255
};

/******************************************************************************
 *
 ******************************************************************************/
class StdExif
{
public:     ////    Constructor/Destructor
    StdExif();
    ~StdExif();

public:
    MBOOL           init();
    MBOOL           uninit();
    MBOOL           reset();

    size_t          getStdExifOffset() const;

    size_t          getHeaderOffset() const;

    void            setMaxThumbnail(size_t const thumbnailSize);

    void            setThumbnail(
                        MINT8 const * const thumbnail,
                        size_t const        thumbnailSize
                    );

    void            setStdExifParams(
                        StdExifParams const&    rStdExifParams
                    );

    void            setOutputBuffer(
                        MINT8 * const   outputExifBuf,
                        size_t const    outputExifBufCapacity
                    );

    status_t        make(
                        size_t& rOutputExifSize
                    );

private:

    MINT32          determineExifOrientation(
                        MUINT32 const   u4DeviceOrientation,
                        MBOOL const     bIsFacing,
                        MBOOL const     bIsFacingFlip = MFALSE
                    );


private:
    //// to be removed
    MINT32                  miDebugEnable;
    MINT8*                  mpThumbnail;
    size_t                  mThumbnailSize;
    ////
    size_t                  mMaxThumbSize;

    MUINTPTR                mpOutputExifBuf;
    size_t                  mOutputExifBufSize;

    StdExifParams const*    mpExifParam;
    IBaseExif*              mpBaseExif;
};


#endif // _STDEXIF_H_

