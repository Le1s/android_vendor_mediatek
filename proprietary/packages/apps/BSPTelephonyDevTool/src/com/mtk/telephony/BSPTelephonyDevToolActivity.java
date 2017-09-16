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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.PhoneRatFamily;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneProxy;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyIntents;

import java.util.ArrayList;

public class BSPTelephonyDevToolActivity extends Activity {
    private static final String LOG_TAG = "BSPTelephonyDev";

    private static final boolean MTK_SIM_HOT_SWAP_COMMON_SLOT = (SystemProperties.getInt("ro.mtk_sim_hot_swap_common_slot", 0) == 1);
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String AT_CMD_SIM_PLUG_OUT = "AT+ESIMTEST=17";
    private static final String AT_CMD_SIM_PLUG_IN  = "AT+ESIMTEST=18";
    private static final String AT_CMD_SIM_PLUG_IN_ALL = "AT+ESIMTEST=19";
    private static final String AT_CMD_SIM_MISSING  = "AT+ESIMTEST=65";
    private static final String AT_CMD_SIM_RECOVERY = "AT+ESIMTEST=66";

    private static boolean sIsStatusBarNotificationEnabled = false;
    private static int sDefaultPhoneId = SubscriptionManager.INVALID_PHONE_ID;
    private static int sDesiredDefaultPhoneId = SubscriptionManager.INVALID_PHONE_ID;
    private static int sDefaultVoicePhoneId = SubscriptionManager.INVALID_PHONE_ID;
    private static int sDefaultSmsPhoneId = SubscriptionManager.INVALID_PHONE_ID;
    private static int sDefaultDataPhoneId = SubscriptionManager.INVALID_PHONE_ID;
    private static long sDefaultSubId = SubscriptionManager.INVALID_SUB_ID;
    private static long sDefaultVoiceSubId = SubscriptionManager.INVALID_SUB_ID;
    private static long sDefaultSmsSubId = SubscriptionManager.INVALID_SUB_ID;
    private static long sDefaultDataSubId = SubscriptionManager.INVALID_SUB_ID;

    private static int[] sPhoneRat = new int[PROJECT_SIM_NUM];
    private static Phone[] sProxyPhones = null;
    private static Phone[] sActivePhones = new Phone[PROJECT_SIM_NUM];
    private static TelephonyManager sTelephonyManager = null;
    private static ITelephony sITelephony = null;
    private static SubscriptionController sSubscriptionController = null;
    private MultiSimPhoneStateListener[] mPhoneStateListener = new MultiSimPhoneStateListener[PROJECT_SIM_NUM];

    private static AlertDialog sAlertDialog;

    private static Button sBtnStatusbarNotification;
    private static Button sBtnTestButton;
    private static Button sBtnPlugOutAllSims;
    private static Button sBtnPlugInAllSims;
    private static Button[] sBtnDefaultPhone = new Button[PROJECT_SIM_NUM];
    private static Button[] sBtnPlugOutSim = new Button[PROJECT_SIM_NUM];
    private static Button[] sBtnPlugInSim = new Button[PROJECT_SIM_NUM];
    private static Button[] sBtnMissingSim = new Button[PROJECT_SIM_NUM];
    private static Button[] sBtnRecoverySim = new Button[PROJECT_SIM_NUM];
    private static Button[] sBtnSimSwitch = new Button[PROJECT_SIM_NUM];

    private static ProgressBar[] sBarSignalStrength = new ProgressBar[PROJECT_SIM_NUM];

    private static TextView sTextDefaultCommonPhone;
    private static TextView sTextDefaultVoicePhone;
    private static TextView sTextDefaultSmsPhone;
    private static TextView sTextDefaultDataPhone;
    private static TextView[] sTextDataActivity = new TextView[PROJECT_SIM_NUM];
    private static TextView[] sTextDataConnectionType = new TextView[PROJECT_SIM_NUM];
    private static TextView[] sTextNetworkType = new TextView[PROJECT_SIM_NUM];
    private static TextView[] sTextNwSpn = new TextView[PROJECT_SIM_NUM];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = getLayoutInflater().inflate(R.layout.main, null);
        setContentView(contentView);
        logd("[onCreate]+");
        logd("PROJECT_SIM_NUM: " + PROJECT_SIM_NUM);

        sProxyPhones = PhoneFactory.getPhones();
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            sActivePhones[i] = ((PhoneProxy) sProxyPhones[i]).getActivePhone();
        }
        sSubscriptionController = SubscriptionController.getInstance();
        sTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        sITelephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));

        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            sBtnDefaultPhone[i] = (Button) contentView.findViewWithTag("btn_default_phone_phone" + (i + 1));
            sTextNwSpn[i] = (TextView) contentView.findViewWithTag("operator_sim" + (i + 1));
            sTextNetworkType[i] = (TextView) contentView.findViewWithTag("network_type_sim" + (i + 1));
            sBarSignalStrength[i] = (ProgressBar) contentView.findViewWithTag("progress_signal_sim" + (i + 1));
            sTextDataConnectionType[i] = (TextView) contentView.findViewWithTag("data_connection_type_sim" + (i + 1));
            sTextDataActivity[i] = (TextView) contentView.findViewWithTag("data_activity_sim" + (i + 1));
            sBtnPlugOutSim[i] = (Button) contentView.findViewWithTag("btn_plug_out_sim" + (i + 1));
            sBtnPlugInSim[i] = (Button) contentView.findViewWithTag("btn_plug_in_sim" + (i + 1));
            sBtnMissingSim[i] = (Button) contentView.findViewWithTag("btn_missing_sim" + (i + 1));
            sBtnRecoverySim[i] = (Button) contentView.findViewWithTag("btn_recovery_sim" + (i + 1));
            sBtnSimSwitch[i] = (Button) contentView.findViewWithTag("btn_sim_switch_sim" + (i + 1));
        }
        for (int i = PROJECT_SIM_NUM; i <= PhoneConstants.SIM_ID_4; i++) {
            contentView.findViewWithTag("btn_default_phone_phone" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("operator_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("network_type_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("network_type_text_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("progress_signal_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("data_connection_type_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("data_connection_type_text_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("data_activity_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("btn_plug_out_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("btn_plug_in_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("btn_missing_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("btn_recovery_sim" + (i + 1)).setVisibility(View.GONE);
            contentView.findViewWithTag("btn_sim_switch_sim" + (i + 1)).setVisibility(View.GONE);
        }
        sBtnStatusbarNotification = (Button) findViewById(R.id.btn_status_bar_notification);
        sBtnStatusbarNotification.setOnClickListener(mStatusbarNotificationOnClickListener);
        sTextDefaultCommonPhone = (TextView) contentView.findViewWithTag("default_common_phone");
        sTextDefaultVoicePhone = (TextView) contentView.findViewWithTag("default_voice_phone");
        sTextDefaultSmsPhone = (TextView) contentView.findViewWithTag("default_sms_phone");
        sTextDefaultDataPhone = (TextView) contentView.findViewWithTag("default_data_phone");
        sBtnTestButton = (Button) findViewById(R.id.btn_test_button);
        sBtnTestButton.setText(R.string.test_button);
        sBtnTestButton.setOnClickListener(mTestButtonOnClickListener);
        sBtnPlugOutAllSims = (Button) findViewById(R.id.btn_plug_out_all_sims);
        sBtnPlugInAllSims = (Button) findViewById(R.id.btn_plug_in_all_sims);

        sAlertDialog = new AlertDialog.Builder(this).create();
        sAlertDialog.setTitle("Operation Executing...");

        if (MTK_SIM_HOT_SWAP_COMMON_SLOT) {
            sBtnPlugOutAllSims.setOnClickListener(mHotPlugOnClickListener);
            sBtnPlugInAllSims.setOnClickListener(mHotPlugOnClickListener);
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                sBtnPlugOutSim[i].setVisibility(View.GONE);
            }
        } else {
            sBtnPlugOutAllSims.setVisibility(View.GONE);
            sBtnPlugInAllSims.setVisibility(View.GONE);
        }

        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            sBtnDefaultPhone[i].setOnClickListener(mDefaultPhoneOnClickListener);
            sBtnPlugOutSim[i].setOnClickListener(mHotPlugOnClickListener);
            sBtnPlugInSim[i].setOnClickListener(mHotPlugOnClickListener);
            sBtnMissingSim[i].setOnClickListener(mHotPlugOnClickListener);
            sBtnRecoverySim[i].setOnClickListener(mHotPlugOnClickListener);
            sBtnSimSwitch[i].setOnClickListener(mSimSwitchClickListener);
            long[] subId = sSubscriptionController.getSubId(i);
            if (subId == null || subId.length == 0 || subId[0] <= 0) {
                logd("Phone" + i + ": Invalid subId to register for PhoneStateListener");
            } else {
                mPhoneStateListener[i] = new MultiSimPhoneStateListener(i, subId[0]);
                sTelephonyManager.listen(mPhoneStateListener[i],
                        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                        | PhoneStateListener.LISTEN_DATA_ACTIVITY);
            }
            try {
                sPhoneRat[i] = sITelephony.getPhoneRat(i);
            } catch(RemoteException e) {
                sPhoneRat[i] = PhoneRatFamily.PHONE_RAT_FAMILY_NONE;
                e.printStackTrace();
            }
            logd("Phone" + i + " rat: " + sPhoneRat[i]);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_SUBSCRIPTION_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_SET_PHONE_RAT_FAMILY_DONE);
        registerReceiver(mBroadcastReceiver, intentFilter);

        sIsStatusBarNotificationEnabled = BSPTelephonyDevToolService.isRunning();
        sDefaultSubId = sSubscriptionController.getDefaultSubId();
        sDefaultVoiceSubId = sSubscriptionController.getDefaultVoiceSubId();
        sDefaultSmsSubId = sSubscriptionController.getDefaultSmsSubId();
        sDefaultDataSubId = sSubscriptionController.getDefaultDataSubId();

        sDefaultPhoneId = sSubscriptionController.getPhoneId(sDefaultSubId);
        sDefaultVoicePhoneId = sSubscriptionController.getPhoneId(sDefaultVoiceSubId);
        sDefaultSmsPhoneId = sSubscriptionController.getPhoneId(sDefaultSmsSubId);
        sDefaultDataPhoneId = sSubscriptionController.getPhoneId(sDefaultDataSubId);
        logd("sDefaultSubId: " + sDefaultSubId + ", sDefaultPhoneId: " + sDefaultPhoneId);
        logd("sDefaultVoiceSubId: " + sDefaultVoiceSubId + ", sDefaultVoicePhoneId: " + sDefaultVoicePhoneId);
        logd("sDefaultSmsSubId: " + sDefaultSmsSubId + ", sDefaultSmsPhoneId: " + sDefaultSmsPhoneId);
        logd("sDefaultDataSubId: " + sDefaultDataSubId + ", sDefaultDataPhoneId: " + sDefaultDataPhoneId);
        updateUI();
        logd("[onCreate]-");
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logd("[Receiver]+");
            logd("Action: " + action);
            if (action.equals(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED)) {
                ServiceState ss = ServiceState.newFromBundle(intent.getExtras());
                logd("ServiceState: " + ss.getState());
            } else if (action.equals(TelephonyIntents.ACTION_DEFAULT_SUBSCRIPTION_CHANGED)) {
                sDefaultSubId = sSubscriptionController.getDefaultSubId();
                sDefaultPhoneId = sSubscriptionController.getPhoneId(sDefaultSubId);
                logd("sDefaultSubId: " + sDefaultSubId + ", sDefaultPhoneId: " + sDefaultPhoneId);
                updateUI();
            } else if (action.equals(TelephonyIntents.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED)) {
                sDefaultVoiceSubId = sSubscriptionController.getDefaultVoiceSubId();
                sDefaultVoicePhoneId = sSubscriptionController.getPhoneId(sDefaultVoiceSubId);
                logd("sDefaultVoiceSubId: " + sDefaultVoiceSubId + ", sDefaultVoicePhoneId: " + sDefaultVoicePhoneId);
                updateUI();
            } else if (action.equals(TelephonyIntents.ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED)) {
                sDefaultSmsSubId = sSubscriptionController.getDefaultSmsSubId();
                sDefaultSmsPhoneId = sSubscriptionController.getPhoneId(sDefaultSmsSubId);
                logd("sDefaultSmsSubId: " + sDefaultSmsSubId + ", sDefaultSmsPhoneId: " + sDefaultSmsPhoneId);
                updateUI();
            } else if (action.equals(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED)) {
                sDefaultDataSubId = sSubscriptionController.getDefaultDataSubId();
                sDefaultDataPhoneId = sSubscriptionController.getPhoneId(sDefaultDataSubId);
                logd("sDefaultDataSubId: " + sDefaultDataSubId + ", sDefaultDataPhoneId: " + sDefaultDataPhoneId);
                updateUI();
            } else if (action.equals(TelephonyIntents.ACTION_SET_PHONE_RAT_FAMILY_DONE)) {
                ArrayList<PhoneRatFamily> phoneRatList = intent.getParcelableArrayListExtra(
                        TelephonyIntents.EXTRA_PHONES_RAT_FAMILY);
                if (phoneRatList == null || phoneRatList.size() == 0) {
                    logd("EXTRA_PHONES_RAT_FAMILY not present!!!");
                } else {
                    int size = phoneRatList.size();
                    PhoneRatFamily phoneRatFamily = null;
                    for (int i = 0; i < size; i++) {
                        phoneRatFamily = phoneRatList.get(i);
                        sPhoneRat[phoneRatFamily.getPhoneId()] = phoneRatFamily.getRatFamily();
                    }
                    for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                        logd("Phone" + i + " rat: " + sPhoneRat[i]);
                    }
                }
                updateUI();
                Toast.makeText(getApplicationContext(), "SIM switch", Toast.LENGTH_LONG).show();
            }
            logd("[Receiver]-");
        }
    };

    private OnClickListener mStatusbarNotificationOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            sIsStatusBarNotificationEnabled = !sIsStatusBarNotificationEnabled;
            updateUI();
            if (sIsStatusBarNotificationEnabled) {
                startService(new Intent(BSPTelephonyDevToolActivity.this, BSPTelephonyDevToolService.class));
            } else {
                stopService(new Intent(BSPTelephonyDevToolActivity.this, BSPTelephonyDevToolService.class));
            }
        }
    };

    private OnClickListener mTestButtonOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "RD use only", Toast.LENGTH_LONG).show();
        }
    };

    private OnClickListener mDefaultPhoneOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_default_phone_phone1:
                    sDesiredDefaultPhoneId = PhoneConstants.SIM_ID_1;
                    break;
                case R.id.btn_default_phone_phone2:
                    sDesiredDefaultPhoneId = PhoneConstants.SIM_ID_2;
                    break;
                case R.id.btn_default_phone_phone3:
                    sDesiredDefaultPhoneId = PhoneConstants.SIM_ID_3;
                    break;
                case R.id.btn_default_phone_phone4:
                    sDesiredDefaultPhoneId = PhoneConstants.SIM_ID_4;
                    break;
                default:
                    break;
            }
            rebootAlert();
        }
    };

    private void rebootAlert() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        long[] subId = sSubscriptionController.getSubId(sDesiredDefaultPhoneId);
                        if (subId == null || subId.length == 0 || subId[0] <= 0) {
                            sDesiredDefaultPhoneId = SubscriptionManager.INVALID_PHONE_ID;
                            logd("Invalid phone to set default phone");
                            Toast.makeText(getApplicationContext(), "Invalid Phone", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            sDefaultPhoneId = sDesiredDefaultPhoneId;
                            sDefaultSubId = subId[0];
                            sSubscriptionController.setDefaultSubId(sDefaultSubId);
                            sSubscriptionController.setDefaultDataSubId(sDefaultSubId);
                            sSubscriptionController.setDefaultSmsSubId(sDefaultSubId);
                            sSubscriptionController.setDefaultVoiceSubId(sDefaultSubId);
                            logd("Setting Phone" + sDefaultPhoneId + " to default phone. sDefaultSubId: " + sDefaultSubId);
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                if (pm != null) {
                                    pm.reboot("");
                                } else {
                                    logd("Fail to get PowerManager");
                                }
                            }
                        }, 3000);
                        Toast.makeText(getApplicationContext(), R.string.restarting_device, Toast.LENGTH_LONG).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        sDesiredDefaultPhoneId = SubscriptionManager.INVALID_PHONE_ID;
                        break;
                    default:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.reboot_confirm_notice).setPositiveButton(R.string.yes, dialogClickListener)
            .setNegativeButton(R.string.no, dialogClickListener).show();
    }

    private void showWaitingDialog(long millisUntilFinished, long countDownInterval) {
        sAlertDialog.setMessage("Wait");
        sAlertDialog.setCanceledOnTouchOutside(false);
        sAlertDialog.show();

        new CountDownTimer(millisUntilFinished, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
               sAlertDialog.setMessage("Wait " + (millisUntilFinished / 1000) + " seconds");
            }

            @Override
            public void onFinish() {
                sAlertDialog.cancel();
            }
        }.start();
    }

    private OnClickListener mHotPlugOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (v == sBtnPlugOutAllSims) {
                    logd("Plug out all SIMs");
                    String cmdStr[] = {AT_CMD_SIM_PLUG_OUT, ""};
                    invokeOemRilRequest(cmdStr, PhoneConstants.SIM_ID_1);
                    break;
                } else if (v == sBtnPlugInAllSims) {
                    logd("Plug in all SIMs");
                    String cmdStr[] = {AT_CMD_SIM_PLUG_IN_ALL, ""};
                    invokeOemRilRequest(cmdStr, PhoneConstants.SIM_ID_1);
                    break;
                }
                if (v == sBtnPlugOutSim[i]) {
                    logd("Plug out SIM" + (i + 1));
                    String cmdStr[] = {AT_CMD_SIM_PLUG_OUT, ""};
                    invokeOemRilRequest(cmdStr, i);
                    break;
                }
                if (v == sBtnPlugInSim[i]) {
                    logd("Plug in SIM" + (i + 1));
                    String cmdStr[] = {AT_CMD_SIM_PLUG_IN, ""};
                    invokeOemRilRequest(cmdStr, i);
                    break;
                }
                if (v == sBtnMissingSim[i]) {
                    logd("Missing SIM" + (i + 1));
                    String cmdStr[] = {AT_CMD_SIM_MISSING, ""};
                    invokeOemRilRequest(cmdStr, i);
                    break;
                }
                if (v == sBtnRecoverySim[i]) {
                    logd("Recover SIM" + (i + 1));
                    String cmdStr[] = {AT_CMD_SIM_RECOVERY, ""};
                    invokeOemRilRequest(cmdStr, i);
                    break;
                }
            }
            Toast.makeText(getApplicationContext(), "Please wait", Toast.LENGTH_LONG).show();
        }
    };

    private OnClickListener mSimSwitchClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int[] phoneRat = new int[PROJECT_SIM_NUM];
            boolean isSwitchTriggered = true;
            PhoneRatFamily[] rat = new PhoneRatFamily[PROJECT_SIM_NUM];
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                if (v == sBtnSimSwitch[i]) {
                    logd("SIM switch to Phone" + i);
                    phoneRat[i] = PhoneRatFamily.PHONE_RAT_FAMILY_4G
                            | PhoneRatFamily.PHONE_RAT_FAMILY_3G
                            | PhoneRatFamily.PHONE_RAT_FAMILY_2G;
                } else {
                    phoneRat[i] = PhoneRatFamily.PHONE_RAT_FAMILY_2G;
                }
                rat[i] = new PhoneRatFamily(i, phoneRat[i]);
            }
            try {
                sITelephony.setPhoneRat(rat);
            } catch(RemoteException e) {
                logd("Set phone rat fail!!!");
                e.printStackTrace();
                isSwitchTriggered = false;
            }
            if (isSwitchTriggered) {
                for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                    sPhoneRat[i] = phoneRat[i];
                }
                updateUI();
                showWaitingDialog(10000, 1000);
            }
        }
    };

    private void invokeOemRilRequest(String[] cmdStr, int phoneId) {
        logd("[invokeOemRilRequest] " + cmdStr[0]);
        sActivePhones[phoneId].invokeOemRilRequestStrings(cmdStr, null);
    }

    private class MultiSimPhoneStateListener extends PhoneStateListener {
        private int mPhoneId;
        private String mPhoneString;

        public MultiSimPhoneStateListener(int phoneId, long subId) {
            super(subId);
            mPhoneId = phoneId;
            mPhoneString = "Phone" + (mPhoneId + 1);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            int gsmSignalStrength = signalStrength.getGsmSignalStrength();
            updateSignalStrengthUI(sBarSignalStrength[mPhoneId], gsmSignalStrength);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            logd("[onServiceStateChanged]+ " + mPhoneString);
            String plmn = serviceState.getOperatorNumeric();
            int voiceRegState = serviceState.getVoiceRegState();
            int rilVoiceRadioTechnology = serviceState.getRilVoiceRadioTechnology();
            int dataRegState = serviceState.getDataRegState();
            int rilDataRadioTechnology = serviceState.getRilDataRadioTechnology();
            logd("plmn: " + plmn);
            logd("voiceRegState: " + Utility.regStateToString(voiceRegState));
            logd("rilVoiceRadioTechnology: " + serviceState.rilRadioTechnologyToString(rilVoiceRadioTechnology));
            logd("dataRegState: " + Utility.regStateToString(dataRegState));
            logd("rilDataRadioTechnology: " + serviceState.rilRadioTechnologyToString(rilDataRadioTechnology));
            sTextNetworkType[mPhoneId].setText(Utility.getNetworkTypeString(serviceState.getNetworkType()));
            if (dataRegState == ServiceState.STATE_IN_SERVICE
                    || voiceRegState == ServiceState.STATE_IN_SERVICE) {
                sTextNwSpn[mPhoneId].setText(serviceState.getOperatorAlphaShort());
            } else {
                sTextNwSpn[mPhoneId].setText(Utility.regStateToString(voiceRegState));
            }
            logd("[onServiceStateChanged]-");
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            logd("[onDataConnectionStateChanged] " + mPhoneString + " data state: " + state + ", " + networkType);
            if (state > 0) {
                if (state == TelephonyManager.DATA_CONNECTED) {
                    sTextDataConnectionType[mPhoneId].setText(Utility.getNetworkTypeString(networkType));
                } else {
                    sTextDataConnectionType[mPhoneId].setText(Utility.getDataStateString(state));
                    sTextDataActivity[mPhoneId].setText("");
                }
            } else {
                sTextDataConnectionType[mPhoneId].setText("");
                sTextDataActivity[mPhoneId].setText("");
            }
        }

        @Override
        public void onDataActivity(int direction) {
            sTextDataActivity[mPhoneId].setText(Utility.getDataDirectionString(direction));
        }
    };

    private Runnable mUpdateUIRunnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
        }
    };

    private void updateUI() {
        if (sDefaultPhoneId == SubscriptionManager.INVALID_PHONE_ID) {
            sTextDefaultCommonPhone.setText("Unset");
        } else {
            sTextDefaultCommonPhone.setText("Phone" + (sDefaultPhoneId + 1));
        }
        if (sDefaultVoicePhoneId == SubscriptionManager.INVALID_PHONE_ID) {
            sTextDefaultVoicePhone.setText("Unset");
        } else {
            sTextDefaultVoicePhone.setText("Phone" + (sDefaultVoicePhoneId + 1));
        }
        if (sDefaultSmsPhoneId == SubscriptionManager.INVALID_PHONE_ID) {
            sTextDefaultSmsPhone.setText("Unset");
        } else {
            sTextDefaultSmsPhone.setText("Phone" + (sDefaultSmsPhoneId + 1));
        }
        if (sDefaultDataPhoneId == SubscriptionManager.INVALID_PHONE_ID) {
            sTextDefaultDataPhone.setText("Unset");
        } else {
            sTextDefaultDataPhone.setText("Phone" + (sDefaultDataPhoneId + 1));
        }
        if (sIsStatusBarNotificationEnabled) {
            sBtnStatusbarNotification.setText(R.string.stop_statusbar_notification);
        } else {
            sBtnStatusbarNotification.setText(R.string.start_status_bar_notification);
        }
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (sPhoneRat[i] == PhoneRatFamily.PHONE_RAT_FAMILY_2G) {
                sBtnSimSwitch[i].setEnabled(true);
            } else {
                sBtnSimSwitch[i].setEnabled(false);
            }
        }
    }

    private void updateSignalStrengthUI(ProgressBar progressBar, int signalStrength) {
        progressBar.setMax(31);
        if (signalStrength == 99) {
            progressBar.setProgress(0);
            progressBar.setEnabled(false);
        } else {
            progressBar.setProgress(signalStrength);
            progressBar.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            sTelephonyManager.listen(mPhoneStateListener[i], PhoneStateListener.LISTEN_NONE);
        }
        unregisterReceiver(mBroadcastReceiver);
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, "[BSPTelDevTool]" + msg);
    }
}
