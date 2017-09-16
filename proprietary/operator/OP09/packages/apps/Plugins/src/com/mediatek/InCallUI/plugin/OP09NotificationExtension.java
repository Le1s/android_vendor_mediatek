/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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

package com.mediatek.incallui.plugin;

import android.os.SystemProperties;
import android.util.Log;

import com.android.internal.telephony.PhoneConstants;
import com.android.services.telephony.common.Call;

import com.mediatek.calloption.plugin.OP09CallOptionUtils;
import com.mediatek.incallui.ext.DefaultNotificationExt;


public class OP09NotificationExtension extends DefaultNotificationExt {

    private static final String LOG_TAG = "OP09NotificationMgrExtension";

    private static final int MO_RES = 0;
    private static final int MT_RES = 1;
    private static final int HOLD_RES = 2;
    private static final int PV_MO_RES = 3;
    private static final int PV_HOLD_RES = 4;

    /**
     * @param cm
     * @param voicePrivacy "voice privacy" mode is active for always show notification
     * @param defResId
     * @param pluginResIds
     * @return
     */
    public int getInCallResId(Call call, boolean voicePrivacy, int defResId, int[][] simResId) {
        if (!SystemProperties.getBoolean(OP09CallOptionUtils.MTK_GEMINI_SUPPORT, false) || null == call ||
                      Call.CALL_TYPE_SIP == call.getCallType()) {
            return -1;
        }

        //check resource from host for sim indicatro res.
        if (simResId.length < 2 || simResId[0].length < 5) {
            return -1;
        }

        int pluginResId = defResId;
        int slotId = call.getSlotId();
        if (call.isIncoming()) {
            // There's for current incoming ringing call.
            if (PhoneConstants.GEMINI_SIM_1 == slotId ||
                        PhoneConstants.GEMINI_SIM_2 == slotId) {
                pluginResId = simResId[slotId][MT_RES];
            }
        }  else if (call.getState() == Call.State.ONHOLD) {
            // There's only one call, and it's on hold.
            if (PhoneConstants.GEMINI_SIM_1 == slotId ||
                        PhoneConstants.GEMINI_SIM_2 == slotId) {
                if (voicePrivacy) {
                    pluginResId = simResId[slotId][PV_HOLD_RES];
                } else {
                    pluginResId = simResId[slotId][HOLD_RES];
                }
            }
        } else {
            //There's for current activie call
            if (PhoneConstants.GEMINI_SIM_1 == call.getSlotId() ||
                        PhoneConstants.GEMINI_SIM_2 == call.getSlotId()) {
                if (voicePrivacy) {
                    pluginResId = simResId[slotId][PV_MO_RES];
                } else {
                    pluginResId = simResId[slotId][MO_RES];
                }
            }
        }
        log("pluginResId : " + pluginResId);
        return pluginResId;
    }

    /**
     * @param hostDefaultValue
     * @return boolean
     */
    public boolean shouldSuppressNotification(boolean hostDefaultValue) {
        // CT reuires not suppress notification
        return false;
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}

