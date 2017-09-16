/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2012. All rights reserved.
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

package com.mediatek.mms.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.telephony.SubscriptionManager;

import com.android.internal.telephony.PhoneConstants;

import com.mediatek.common.PluginImpl;
import com.mediatek.mms.ext.DefaultMmsStatusBarSelectorExt;
import com.mediatek.widget.CustomAccountRemoteViews;
import com.mediatek.op01.plugin.R;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;

@PluginImpl(interfaceName="com.mediatek.mms.ext.IMmsStatusBarSelectorExt")
public class Op01MmsStatusBarSelectorExt extends DefaultMmsStatusBarSelectorExt{
    private static final String TAG = "Mms/Op01MmsStatusBarSelectorExt";


    public Op01MmsStatusBarSelectorExt(Context context) {
        super(context);
    }
    ///M: onfig statusbar selector to add op01 item "auto".
    public void config(ArrayList<CustomAccountRemoteViews.AccountInfo> list, long currentSubId,
                                                    String filterAction) {
        Xlog.d(TAG,"currentSubId = " + currentSubId + ", action = " + filterAction);
        // auto
        Intent autoIntent = new Intent(filterAction);
        autoIntent.putExtra(PhoneConstants.SUBSCRIPTION_KEY,
                Settings.System.SMS_SIM_SETTING_AUTO);
        Drawable icon = getResources().getDrawable(R.drawable.mms_notification_auto_select);
        String label = getResources().getString(R.string.mms_sim_select_auto);
        
        CustomAccountRemoteViews.AccountInfo autoInfo = new CustomAccountRemoteViews.AccountInfo(
                icon, label, null, autoIntent);

        //if not set or auto, set the auto item as active and set other item as inactive
        if (SubscriptionManager.INVALID_SUB_ID == currentSubId ||
            Settings.System.SMS_SIM_SETTING_AUTO == currentSubId) {
            autoInfo.setActiveStatus(true);
            int count = list.size();
            for (int index = 0; index < count; index++) {
                CustomAccountRemoteViews.AccountInfo info = list.get(index);
                if (info != null) {
                    info.setActiveStatus(false);
                }
            }
        } else {
            autoInfo.setActiveStatus(false);
        }
        list.add(autoInfo);
    }
}

