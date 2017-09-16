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

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.settings.plugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneRatFamily;
import android.telephony.SubInfoRecord;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.TelephonyIntents;
import com.mediatek.common.PluginImpl;
import com.mediatek.widget.AccountViewAdapter.AccountElements;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.settings.ext.DefaultSimManagementExt;
import com.mediatek.op01.plugin.R;
import com.mediatek.xlog.Xlog;


public class SimMgrChangeConnDialog extends Activity {
    private static final String TAG = "SimMgrChangeConnDialog";

    private Context mContext;
    private IntentFilter mIntentFilter;
    private ProgressDialog mWaitDlg;
    private boolean mIsDataSwitchWaiting = false;
    private int mToCloseSlot = -1;
    private static final int DIALOG_ID_CHANGE_DATA_CONNECTION = 1000;
    private static final int DIALOG_ID_PROGRESS = 1001;
    
    private static final int DATA_SWITCH_TIME_OUT_MSG = 2000;
    private static final int DATA_SWITCH_TIME_OUT_TIME = 6000;
    
    private BroadcastReceiver mSubReceiver = new BroadcastReceiver() {
        
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Xlog.d(TAG, "mSubReceiver action = " + action);
                if (action.equals(TelephonyIntents.ACTION_SUBINFO_CONTENT_CHANGE)
                        || action.equals(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED)) {
                        long[] subids = SubscriptionManager.getActiveSubIdList();
                        if (subids == null || subids.length <= 1) {
                                Xlog.d(TAG, "onReceive dealWithDataConnChanged dismiss AlertDlg"); 
                                finish();
                        }
        
                } else if (TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED.equals(action)) {
                    long subId = intent.getLongExtra(PhoneConstants.SUBSCRIPTION_KEY,
                            SubscriptionManager.INVALID_SUB_ID);
                    dealDataConnectionChanged(subId);
                    Xlog.d(TAG, "changed default data subId: " + subId);
                } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                    finish();
                }
            }
    };
    
    //Timeout handler
    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (DATA_SWITCH_TIME_OUT_MSG == msg.what) {

                Xlog.i(TAG, "reveive time out msg...");
                if (mIsDataSwitchWaiting) {

                    mTimerHandler.removeMessages(DATA_SWITCH_TIME_OUT_MSG);
                    if (mWaitDlg !=null && mWaitDlg.isShowing()) {
                        mWaitDlg.dismiss();
                        finish();
                    }
                    mIsDataSwitchWaiting = false;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setFinishOnTouchOutside(false);
        mContext = this;
        
        mToCloseSlot = getIntent().getIntExtra("slotId", -1);
        Xlog.d(TAG, "Slot id =" + mToCloseSlot);
        mIntentFilter = new IntentFilter(TelephonyIntents.ACTION_SUBINFO_CONTENT_CHANGE);
        mIntentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mSubReceiver, mIntentFilter);

        showDialog(DIALOG_ID_CHANGE_DATA_CONNECTION);
    }


    @Override
    public Dialog onCreateDialog(int id, Bundle data) {
        switch (id) {
            case DIALOG_ID_CHANGE_DATA_CONNECTION: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog dialog = getChangeDataConnDialog(builder);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                            finish();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                return dialog;
            }
                        
            default:
                return null;
        }
    }

    @Override
    protected void onDestroy() {
        Xlog.d(TAG, "onDestroy()");
        unregisterReceiver(mSubReceiver);
        mTimerHandler.removeMessages(DATA_SWITCH_TIME_OUT_MSG);
        mToCloseSlot = -1;
        super.onDestroy();
    }

    private AlertDialog getChangeDataConnDialog(Builder builder) {

        Xlog.d(TAG, "getChangeDataConnDialog");
        final int toCloseSlot = mToCloseSlot;
        SubInfoRecord currentSiminfo =
               SubscriptionManager.getSubInfoUsingSlotId(toCloseSlot).get(0);

        SubInfoRecord anotherSiminfo =
               SubscriptionManager.getSubInfoUsingSlotId(1 - toCloseSlot).get(0);

        String currentSimName = currentSiminfo.displayName;
        String anotherSimName = anotherSiminfo.displayName;

        if (currentSiminfo == null || anotherSiminfo == null) {
            return null;
        }
        if (currentSimName == null) {
            currentSimName = "SIM " + (currentSiminfo.slotId + 1);
        }
        if (anotherSimName == null) {
            anotherSimName = "SIM " + (anotherSiminfo.slotId + 1);
        }
        Xlog.d(TAG, "currentSimName:" + currentSimName + "\n anotherSimName:" + anotherSimName);

        int currSimResId = currentSiminfo.simIconRes[0];
        int otherSimResId = anotherSiminfo.simIconRes[0];
        int currentSimColor = getDrawableColorValue(currSimResId);
        int anotherSimColor = getDrawableColorValue(otherSimResId);

        Xlog.d(TAG, "mToClosedSimCard = " + toCloseSlot);

        String message = String.format(currentSimName +
               mContext.getString(R.string.change_data_conn_message) +
               anotherSimName +
               "?");

        int currentSimStartIdx = message.indexOf(currentSimName);
        int currentSimEndIdx = currentSimStartIdx + currentSimName.length();

        int anotherSimStartIdx = currentSimEndIdx
                + (mContext.getString(R.string.change_data_conn_message)).length();
        int anotherSimEndIdx = anotherSimStartIdx + anotherSimName.length();

        SpannableStringBuilder style = new SpannableStringBuilder(message);

        style.setSpan(new BackgroundColorSpan(currentSimColor), currentSimStartIdx,
               currentSimEndIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        style.setSpan(new BackgroundColorSpan(anotherSimColor), anotherSimStartIdx,
               anotherSimEndIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setTitle(mContext.getString(R.string.change_data_conn_title));
        builder.setMessage(style);

        builder.setPositiveButton(android.R.string.yes,
               new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int whichButton) {
                           Xlog.d(TAG, "Perform On click ok");
                           removeDialog(DIALOG_ID_CHANGE_DATA_CONNECTION);
                           long subid = getSubIdBySlot(1 - toCloseSlot);
                           Xlog.d(TAG, "Auto Switch GPRS Sim id = " + subid);
                           switchGprsDefautSIM(subid);
                       }
                       });

        builder.setNegativeButton(android.R.string.cancel,
               new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int whichButton) {
                           Xlog.d(TAG, "Perform On click cancel");
                           finish();
                       }
                       });
        

        return builder.create();

    }
    
    private long getSubIdBySlot(int slotId) {
            Xlog.d(TAG, "SlotId = " + slotId);
            if (slotId < 0 || slotId > 1) {
                return -1;
            }
            long[] subids = SubscriptionManager.getSubId(slotId);
            long subid = -1;
            if (subids != null && subids.length >= 1) {
                subid = subids[0];
            }
            Xlog.d(TAG, "GetSimIdBySlot: sub id = " + subid 
                    + "sim Slot = " + slotId);
            return subid;
        }

    private void switchGprsDefautSIM(long subid) {

        Xlog.d(TAG, "switchGprsDefautSIM() with simid=" + subid);

        if (subid < 0) {
            finish();
            return;
        }
        long curConSubId = SubscriptionManager.getDefaultDataSubId();
        Xlog.d(TAG,"curConSimId=" + curConSubId);
        
        if (subid == curConSubId) {
            finish();
            return;
        }
        showDataConnWaitDialog();
        SubscriptionManager.setDefaultDataSubId(subid);
        handleSimSwitch(subid);

    }

    private void showDataConnWaitDialog() {

        mTimerHandler.removeMessages(DATA_SWITCH_TIME_OUT_MSG);
        mTimerHandler.sendEmptyMessageDelayed(DATA_SWITCH_TIME_OUT_MSG,
                DATA_SWITCH_TIME_OUT_TIME);

        mWaitDlg = new ProgressDialog(this);
        mWaitDlg.setMessage(mContext.getString(R.string.change_data_conn_progress_message));
        mWaitDlg.setIndeterminate(true);
        mWaitDlg.setCancelable(false);
        mWaitDlg.show();

        mIsDataSwitchWaiting = true;
    }

    private int getDrawableColorValue(int resId) {
        Xlog.i(TAG, "getDrawableColorValue");
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int color = bitmap.getPixel(width / 2, height / 2);
        return color;
    }

    private PhoneConstants.DataState getMobileDataState(Intent intent) {

        String str = intent.getStringExtra(PhoneConstants.STATE_KEY);

        if (str != null) {
            return Enum.valueOf(PhoneConstants.DataState.class, str);
        } else {
            return PhoneConstants.DataState.DISCONNECTED;
        }
    }

    private void handleSimSwitch(long subId) {
        TelephonyManager tm = TelephonyManager.from(this);
        int count = tm.getPhoneCount();
        PhoneRatFamily[] rats = new PhoneRatFamily[count];
        Xlog.d(TAG, "handleSimSwitch()... " + count);
        for (int i = 0; i < rats.length; i++) {
            if (SubscriptionManager.getPhoneId(subId) == i) {
                if (SystemProperties.get("ro.mtk_lte_support").equals("1")) {
                    rats[i] = new PhoneRatFamily(i, PhoneRatFamily.PHONE_RAT_FAMILY_3G |
                            PhoneRatFamily.PHONE_RAT_FAMILY_4G);
                } else {
                    rats[i] = new PhoneRatFamily(i, PhoneRatFamily.PHONE_RAT_FAMILY_3G);
                }
            } else {
                rats[i] = new PhoneRatFamily(i, PhoneRatFamily.PHONE_RAT_FAMILY_2G);

            }
            Xlog.d(TAG, "handleSimSwitch()... rat[" + i + "]: " + rats[i]);
        }
        try {
            setStatusBarEnableStatus(true);
            ITelephony tel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
            tel.setPhoneRat(rats);
        } catch (RemoteException e) {
            Xlog.d(TAG, "handleSimSwitch fail");
            e.printStackTrace();
        }
    }

    private void setStatusBarEnableStatus(boolean enabled) {
        Xlog.i(TAG, "setStatusBarEnableStatus(" + enabled + ")");
        StatusBarManager statusMgr = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
        if (statusMgr != null) {
            if (enabled) {
                statusMgr.disable(StatusBarManager.DISABLE_NONE);
            } else {
                statusMgr.disable(StatusBarManager.DISABLE_EXPAND |
                        StatusBarManager.DISABLE_RECENT |
                        StatusBarManager.DISABLE_HOME);
            }
        } else {
            Xlog.e(TAG, "Fail to get status bar instance");
        }
    }

    
    private void dealDataConnectionChanged(long subId) {
        Xlog.i(TAG, "dealDataConnectionChanged: mToCloseSlot is " + mToCloseSlot);
        if (mToCloseSlot >= 0) {
                
            long toCloseSubId = getSubIdBySlot(mToCloseSlot);
            Xlog.i(TAG, "dealDataConnectionChanged: toCloseSimId is " + toCloseSubId);
        
            if (toCloseSubId != subId) {
                Xlog.d(TAG, "dealWithDataConnChanged dismiss progressDialog");
                mTimerHandler.removeMessages(DATA_SWITCH_TIME_OUT_MSG);
                if (mWaitDlg != null && mWaitDlg.isShowing()) {
                    mWaitDlg.cancel();
                }
                finish();
            } 
        }
    }


}
