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

package com.mtk.telephony;

import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

public class Utility {
    public static String getNetworkTypeString(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "Unknown";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO_0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO_A";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDen";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO_B";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_GSM:
                return "GSM";
            default:
                return "Type:" + networkType;
        }
    }

    public static String getDataStateString(int dataState) {
        switch (dataState) {
            case TelephonyManager.DATA_CONNECTED:
                return "CONNECTED";
            case TelephonyManager.DATA_CONNECTING:
                return "CONNECTING";
            case TelephonyManager.DATA_DISCONNECTED:
                return "DISCONNECTED";
            case TelephonyManager.DATA_SUSPENDED:
                return "SUSPENDED";
            default:
                return "State:" + dataState;
        }
    }

    public static String regStateToString(int regState) {
        String stateString;
        switch (regState) {
            case ServiceState.STATE_POWER_OFF:
                stateString = "STATE_POWER_OFF";
                break;
            case ServiceState.STATE_IN_SERVICE:
                stateString = "STATE_IN_SERVICE";
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
                stateString = "STATE_OUT_OF_SERVICE";
                break;
            case ServiceState.STATE_EMERGENCY_ONLY:
                stateString = "STATE_EMERGENCY_ONLY";
                break;
            default:
                stateString = "Invalid State";
                break;
        }

        return stateString;
    }

    public static String rilRegStateToString(int rilRegState) {
        String rsString;
        switch (rilRegState) {
            case ServiceState.REGISTRATION_STATE_NOT_REGISTERED_AND_NOT_SEARCHING:
                rsString = "REGISTRATION_STATE_NOT_REGISTERED_AND_NOT_SEARCHING";
                break;
            case ServiceState.REGISTRATION_STATE_HOME_NETWORK:
                rsString = "REGISTRATION_STATE_HOME_NETWORK";
                break;
            case ServiceState.REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING:
                rsString = "REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING";
                break;
            case ServiceState.REGISTRATION_STATE_REGISTRATION_DENIED:
                rsString = "REGISTRATION_STATE_REGISTRATION_DENIED";
                break;
            case ServiceState.REGISTRATION_STATE_UNKNOWN:
                rsString = "REGISTRATION_STATE_UNKNOWN";
                break;
            case ServiceState.REGISTRATION_STATE_ROAMING:
                rsString = "REGISTRATION_STATE_ROAMING";
                break;
            default:
                rsString = "Invalid RegState";
                break;
        }

        return rsString;
    }

    public static String getDataDirectionString(int direction) {
        switch (direction) {
            case TelephonyManager.DATA_ACTIVITY_IN:
                return "D";
            case TelephonyManager.DATA_ACTIVITY_OUT:
                return "U";
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                return "DU";
            case TelephonyManager.DATA_ACTIVITY_NONE:
                return "";
            default:
                return "UNKNOWN";
        }
    }
}
