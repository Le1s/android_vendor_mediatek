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

package com.mediatek.phone.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.content.pm.PackageManager.NameNotFoundException;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneProxy;
import com.android.internal.telephony.cdma.CDMAPhone;
import com.android.internal.telephony.gemini.GeminiPhone;

import com.mediatek.phone.ext.DefaultPhoneMiscExt;
import com.mediatek.xlog.Xlog;

public class OP09PhoneGlobalsExtension extends DefaultPhoneMiscExt {
    private static final String TAG = "OP09PhoneGlobalsExtension";
    private final String PHONE_PACKAGE = "com.mediatek.op09.plugin";
    private final String CDMA_INFO_SPECIFICATION_ACTION = "com.mediatek.phone.plugin.CdmaInfoSpecification";
    private Context mPluginContext;
    private PoneGlobalsExtensionReceiver mReceiver;
    private Phone mPhone;

    /**
     * This method is called when PhoneGlobals#onPhoneGlobalsCreate() is called. Dispatch
     * context and phone for Plug-in.
     *
     * @param context
     * @param phone
     */
    public void onPhoneGlobalsCreate(Context context, Phone phone) {
        Xlog.d(TAG, "onCreate :" + phone);
        try {
            mPluginContext = context.createPackageContext("com.mediatek.op09.plugin", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            Xlog.d(TAG, "no com.mediatek.op09.plugin packages");
        }
        mReceiver = new PoneGlobalsExtensionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CDMA_INFO_SPECIFICATION_ACTION);
        mPluginContext.registerReceiver(mReceiver, intentFilter);
        mPhone = phone;
    }

    private class PoneGlobalsExtensionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Xlog.d(TAG, "PoneGlobalsExtensionReceiver, onReceive action = " + action + " mPhone " + mPhone);
            if (CDMA_INFO_SPECIFICATION_ACTION.equals(action)) {
                Phone phoneCdma;
                boolean isCdma = true;
                boolean meidValid = false;
                int slot = intent.getIntExtra(PhoneConstants.GEMINI_SIM_ID_KEY, -1);
                Intent intentCdma = new Intent(Intent.ACTION_MAIN);
                intentCdma.setClassName(PHONE_PACKAGE, CDMA_INFO_SPECIFICATION_ACTION);
                intentCdma.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (-1 == slot) {
                    Xlog.d(TAG, "slotId = -1, just return");
                    isCdma = false;
                    intentCdma.putExtra(CdmaInfoSpecification.IS_CDMA_PHONE, isCdma);
                    context.startActivity(intentCdma);
                    return;
                }

                if (SystemProperties.getBoolean("ro.mtk_gemini_support", false) && mPhone instanceof GeminiPhone) {
                    phoneCdma = ((GeminiPhone) mPhone).getPhonebyId(slot);
                } else {
                    phoneCdma = mPhone;
                }
                if (PhoneConstants.PHONE_TYPE_CDMA != phoneCdma.getPhoneType()) {
                    Xlog.d(TAG, "phone type is not CDMA, just return");
                    isCdma = false;
                    intentCdma.putExtra(CdmaInfoSpecification.IS_CDMA_PHONE, isCdma);
                    context.startActivity(intentCdma);
                    return;
                }
                if (!(phoneCdma instanceof PhoneProxy)) {
                    Xlog.d(TAG, "phone is not instance of PhoneProxy");
                    isCdma = false;
                    intentCdma.putExtra(CdmaInfoSpecification.IS_CDMA_PHONE, isCdma);
                    context.startActivity(intentCdma);
                    return;
                }
                PhoneProxy cdmaPhoneProxy = (PhoneProxy) phoneCdma;
                if (!(cdmaPhoneProxy.getActivePhone() instanceof CDMAPhone)) {
                    Xlog.d(TAG, "active phone intance type is not CDMAPhone, just return");
                    isCdma = false;
                    intentCdma.putExtra(CdmaInfoSpecification.IS_CDMA_PHONE, isCdma);
                    context.startActivity(intentCdma);
                    return;
                }

                if (isCdma) {
                    CDMAPhone cdmaPhone = (CDMAPhone) cdmaPhoneProxy.getActivePhone();
                    if (cdmaPhone.isMeidValid()) {
                        meidValid = true;
                        intentCdma.putExtra(CdmaInfoSpecification.KEY_MEID, cdmaPhone.getMeid());
                    } else {
                        intentCdma.putExtra(CdmaInfoSpecification.KEY_ESN, cdmaPhone.getEsn());
                    }

                    intentCdma.putExtra(PhoneConstants.GEMINI_SIM_ID_KEY, slot);
                    intentCdma.putExtra(CdmaInfoSpecification.KEY_PRL_VERSION, cdmaPhone.getPrl());
                    intentCdma.putExtra(CdmaInfoSpecification.KEY_SID, cdmaPhone.getSid());
                    intentCdma.putExtra(CdmaInfoSpecification.KEY_NID, cdmaPhone.getNid());
                    intentCdma.putExtra(CdmaInfoSpecification.KEY_UIM_ID, cdmaPhone.getUimid());
                    intentCdma.putExtra(CdmaInfoSpecification.IS_MEID, meidValid);
                }
                intentCdma.putExtra(CdmaInfoSpecification.IS_CDMA_PHONE, isCdma);
                context.startActivity(intentCdma);
            }
        }
    }

}


