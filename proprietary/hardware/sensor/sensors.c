/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2012. All rights reserved.
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


#include <cutils/log.h>

#include <hardware/sensors.h>
#include <linux/hwmsensor.h>
//#include <hwmsen_chip_info.h>
#include "nusensors.h"

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG "Sensors_Init"
#endif

/*****************************************************************************/

/*
 * The SENSORS Module
 */

/*
 * the AK8973 has a 8-bit ADC but the firmware seems to average 16 samples,
 * or at least makes its calibration on 12-bits values. This increases the
 * resolution by 4 bits.
 */
typedef enum SENSOR_NUM_DEF
{
     SONSER_UNSUPPORTED = -1,

    #ifdef CUSTOM_KERNEL_ACCELEROMETER
        ACCELEROMETER_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_MAGNETOMETER
        MAGNETOMETER_NUM,
        ORIENTATION_NUM ,
    #endif

    #if defined(CUSTOM_KERNEL_ALSPS) || defined(CUSTOM_KERNEL_ALS)
        ALS_NUM,
    #endif
    #if defined(CUSTOM_KERNEL_ALSPS) || defined(CUSTOM_KERNEL_PS)
        PS_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_GYROSCOPE
        GYROSCOPE_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_BAROMETER
        PRESSURE_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_TEMPURATURE
        TEMPURATURE_NUM,
    #endif
    #ifdef CUSTOM_KERNEL_STEP_COUNTER
        STEP_COUNTER_NUM,
        STEP_DETECTOR_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_SIGNIFICANT_MOTION_SENSOR
        STEP_SIGNIFICANT_MOTION_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_PEDOMETER
        PEDOMETER_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_IN_POCKET_SENSOR
        IN_POCKET_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_ACTIVITY_SENSOR
        ACTIVITY_NUM,
    #endif
	
    #ifdef CUSTOM_KERNEL_PICK_UP_SENSOR
        PICK_UP_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_FACE_DOWN_SENSOR
        FACE_DOWN_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_SHAKE_SENSOR
        SHAKE_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_HEART_RATE_SENSOR
        HEART_RATE_NUM,
    #endif
    
    #ifdef CUSTOM_KERNEL_TILT_DETECTOR_SENSOR
        TILT_DETECTOR_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_WAKE_GESTURE_SENSOR
        WAKE_GESTURE_NUM,
    #endif

    #ifdef CUSTOM_KERNEL_GLANCE_GESTURE_SENSOR
        GLANCE_GESTURE_NUM,
    #endif

    SENSORS_NUM

}SENSOR_NUM_DEF;

#define MAX_NUM_SENSOR      (SENSORS_NUM)

extern  struct sensor_t sSensorList[MAX_NUM_SENSOR];



static int open_sensors(const struct hw_module_t* module, const char* name,
        struct hw_device_t** device);

static int sensors__get_sensors_list(struct sensors_module_t* module,
        struct sensor_t const** list)
{
    ALOGD(" sSensorList addr =%p, module addr =%p\r\n",sSensorList,module);
    ALOGD(" ARRAY_SIZE(sSensorList) =%d SENSORS_NUM=%d MAX_NUM_SENSOR=%d \r\n",ARRAY_SIZE(sSensorList), SENSORS_NUM, MAX_NUM_SENSOR);
    *list = sSensorList;
    return ARRAY_SIZE(sSensorList);

}


static struct hw_module_methods_t sensors_module_methods = {
    .open = open_sensors
};

struct sensors_module_t HAL_MODULE_INFO_SYM = {
    .common = {
        .tag = HARDWARE_MODULE_TAG,
        .version_major = 1,
        .version_minor = 0,
        .id = SENSORS_HARDWARE_MODULE_ID,
        .name = "MTK SENSORS Module",
        .author = "Mediatek",
        .methods = &sensors_module_methods,
    },
    .get_sensors_list = sensors__get_sensors_list,
};

/*****************************************************************************/

static int open_sensors(const struct hw_module_t* module, const char* name,
        struct hw_device_t** device)
{
   ALOGD("%s: name: %s! fwq debug\r\n", __func__, name);

   return init_nusensors(module, device);
}
