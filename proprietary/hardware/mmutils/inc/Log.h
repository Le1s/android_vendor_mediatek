/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

#ifndef _MTK_HARDWARE_MMUTILS_INC_LOG_H_
#define _MTK_HARDWARE_MMUTILS_INC_LOG_H_

/******************************************************************************
 *
 ******************************************************************************/
#include <inttypes.h>


/******************************************************************************
 *
 ******************************************************************************/
#if 0
//
#include <cutils/xlog.h>
#define MM_LOGV(fmt, arg...)       XLOGV(fmt"\r\n", ##arg)
#define MM_LOGD(fmt, arg...)       XLOGD(fmt"\r\n", ##arg)
#define MM_LOGI(fmt, arg...)       XLOGI(fmt"\r\n", ##arg)
#define MM_LOGW(fmt, arg...)       XLOGW(fmt"\r\n", ##arg)
#define MM_LOGE(fmt, arg...)       XLOGE(fmt" (%s){#%d:%s}""\r\n", ##arg, __FUNCTION__, __LINE__, __FILE__)
//
#else
//
#include <cutils/log.h>
#define MM_LOGV(fmt, arg...)       ALOGV(fmt"\r\n", ##arg)
#define MM_LOGD(fmt, arg...)       ALOGD(fmt"\r\n", ##arg)
#define MM_LOGI(fmt, arg...)       ALOGI(fmt"\r\n", ##arg)
#define MM_LOGW(fmt, arg...)       ALOGW(fmt"\r\n", ##arg)
#define MM_LOGE(fmt, arg...)       ALOGE(fmt" (%s){#%d:%s}""\r\n", ##arg, __FUNCTION__, __LINE__, __FILE__)
//
#endif
//
//  ASSERT
#define MM_LOGA(...) \
    do { \
        MM_LOGE("[Assert] "__VA_ARGS__); \
        while(1) { ::usleep(500 * 1000); } \
    } while (0)
//
//
//  FATAL
#define MM_LOGF(...) \
    do { \
        MM_LOGE("[Fatal] "__VA_ARGS__); \
        LOG_ALWAYS_FATAL_IF(1, "(%s){#%d:%s}""\r\n", __FUNCTION__, __LINE__, __FILE__); \
    } while (0)


/******************************************************************************
 *
 ******************************************************************************/
#define MM_LOGV_IF(cond, ...)      do { if ( (cond) ) { MM_LOGV(__VA_ARGS__); } }while(0)
#define MM_LOGD_IF(cond, ...)      do { if ( (cond) ) { MM_LOGD(__VA_ARGS__); } }while(0)
#define MM_LOGI_IF(cond, ...)      do { if ( (cond) ) { MM_LOGI(__VA_ARGS__); } }while(0)
#define MM_LOGW_IF(cond, ...)      do { if ( (cond) ) { MM_LOGW(__VA_ARGS__); } }while(0)
#define MM_LOGE_IF(cond, ...)      do { if ( (cond) ) { MM_LOGE(__VA_ARGS__); } }while(0)
#define MM_LOGA_IF(cond, ...)      do { if ( (cond) ) { MM_LOGA(__VA_ARGS__); } }while(0)
#define MM_LOGF_IF(cond, ...)      do { if ( (cond) ) { MM_LOGF(__VA_ARGS__); } }while(0)


/******************************************************************************
 *
 ******************************************************************************/
#endif  //_MTK_HARDWARE_MMUTILS_INC_LOG_H_

