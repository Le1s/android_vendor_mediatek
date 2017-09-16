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

package com.mediatek.csm;

import android.app.PendingIntent;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.IPhoneStateListener;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstants;

import com.mediatek.common.telephony.ITelephonyEx;
import com.mediatek.telephony.TelephonyManagerEx;

/**
 * Phone Manager Utils.
 */
public class PhoneManager {
    private static ITelephony sPhone;
    private static ITelephonyEx sPhoneEx;
    private static ITelephonyRegistry sPhoneRegistry;
    private static ITelephonyRegistry sPhoneRegistry2;

    /**
     * Private Construct to avoid new instance.
     */
    private PhoneManager() {
    }

    /**
     * Query if icc card is existed or not.
     * @param simId sim slot
     * @return true if exists an icc card in given slot.
     */
    public static boolean isSimInserted(int simId) {
        boolean ret = false;
        try {
            ITelephonyEx iTel = ITelephonyEx.Stub.asInterface(ServiceManager
                    .getService(Context.TELEPHONY_SERVICE_EX));
            ret = iTel.hasIccCard(simId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Check to see if the radio is on or not.
     * @param simId sim slot
     * @return returns true if the radio is on.
     */
    public static boolean isRadioOn(int simId) {
        boolean ret = false;
        try {
            if (isGeminiSupport()) {
                ret = getITelephonyEx().isRadioOn(simId);
            } else {
                ret = getITelephony().isRadioOn();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Returns a constant indicating the state of the device SIM card in a slot.
     * @param simId sim slot
     * @return Sim State
     */
    public static int getSimState(int simId) {
        int state = TelephonyManager.SIM_STATE_UNKNOWN;
        if (isGeminiSupport()) {
            state = TelephonyManagerEx.getDefault().getSimState(simId);
        } else {
            state = TelephonyManager.getDefault().getSimState();
        }
        return state;
    }

    /**
     * Returns the type of the phone.
     * @param simId sim slot
     * @return Phone Type
     */
    public static int getPhoneType(int simId) {
        int type = TelephonyManager.PHONE_TYPE_NONE;
        if (isGeminiSupport()) {
            type = TelephonyManagerEx.getDefault().getPhoneType(simId);
        } else {
            type = TelephonyManager.getDefault().getPhoneType();
        }
        return type;
    }

    /**
     * Returns a constant indicating the call state (cellular) on the device.
     * @param simId sim slot
     * @return Device call state
     */
    public static int getCallState(int simId) {
        int state = TelephonyManager.CALL_STATE_IDLE;
        if (isGeminiSupport()) {
            state = TelephonyManagerEx.getDefault().getCallState(simId);
        } else {
            state = TelephonyManager.getDefault().getCallState();
        }
        return state;
    }

    /**
     * End call.
     * @param simId sim slot
     */
    public static void endCall(int simId) {
        try {
            if (isGeminiSupport()) {
                /// M: For L.AOSP.EARLY.DEV
                getITelephony().endCallUsingSubId(simId);
            } else {
                getITelephony().endCall();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a text based SMS.
     * @param addr the address to send the message to
     * @param msg the body of the message to send
     * @param simId on which the SMS has to be sent
     * @param sentIntent sent Intent
     * @param deliveryIntent delivery Intent
     */
    public static void sendTextMessage(String addr, String msg, int simId,
            PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (isGeminiSupport()) {
            /// M: For L.AOSP.EARLY.DEV
            SmsManager.getDefault().sendTextMessage(simId, addr, null, msg,
                    sentIntent, deliveryIntent);
        } else {
            SmsManager.getDefault().sendTextMessage(addr, null, msg,
                    sentIntent, deliveryIntent);
        }
    }

    /**
     * Just only support master sim slot up to now.
     * @param turnOn Set the radio to on or off
     */
    public static void setRadioPower(boolean turnOn) {
        try {
            getITelephony().setRadio(turnOn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a listener object to receive notification of changes in
     * specified telephony states.
     * @param pkg package name
     * @param callback The {@link IPhoneStateListener} object to register (or unregister)
     * @param events The telephony state(s) of interest to the listener
     * @param notifyNow notify
     * @param simId sim slot
     */
    public static void listen(String pkg, IPhoneStateListener callback, int events,
            boolean notifyNow, int simId) {
        try {
            getITelephonyRegistry(simId).listen(pkg, callback, events, notifyNow);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a reference to a ITelephony instance.
     * @return ITelephony
     */
    public static ITelephony getITelephony() {
        if (sPhone == null) {
            sPhone = ITelephony.Stub.asInterface(ServiceManager
                    .getService(Context.TELEPHONY_SERVICE));
        }
        return sPhone;
    }

    /**
     * Returns a reference to a ITelephonyEx instance.
     * @return ITelephonyEx
     */
    public static ITelephonyEx getITelephonyEx() {
        if (sPhoneEx == null) {
            sPhoneEx = ITelephonyEx.Stub.asInterface(ServiceManager
                    .getService(Context.TELEPHONY_SERVICE_EX));
        }
        return sPhoneEx;
    }

    /**
     * Returns a reference to a ITelephonyRegistry instance in a slot.
     * @param slot simId
     * @return ITelephonyRegistry
     */
    public static ITelephonyRegistry getITelephonyRegistry(int slot) {
        ITelephonyRegistry registry = null;
        /// M: For L.AOSP.EARLY.DEV
        if (slot == PhoneConstants.SIM_ID_1) {
            if (sPhoneRegistry == null) {
                sPhoneRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager
                        .getService("telephony.registry"));
            }
            registry = sPhoneRegistry;
        } else if (slot == PhoneConstants.SIM_ID_2) {
            if (sPhoneRegistry2 == null) {
                sPhoneRegistry2 = ITelephonyRegistry.Stub.asInterface(ServiceManager
                        .getService("telephony.registry2"));
            }
            registry = sPhoneRegistry2;
        }

        return registry;
    }

    /**
     * Read system property and judge whether gemini is supported.
     * @return isGeminiSupport as a boolean
     */
    static boolean isGeminiSupport() {
        return "1".equals(SystemProperties.get("ro.mtk_gemini_support"));
    }
}
