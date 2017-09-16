/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.stk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.cat.CatLog;
import com.android.internal.telephony.cat.CatService;
import android.provider.Settings.System;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

//import com.mediatek.common.telephony.ITelephonyEx;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.android.internal.telephony.ITelephony;

/**
 * Boot completed receiver. used to reset the app install state every time the
 * device boots.
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = new Object(){}.getClass().getEnclosingClass().getName();
    public static final String INTENT_KEY_DETECT_STATUS = "simDetectStatus";
    public static final String EXTRA_VALUE_REMOVE_SIM = "REMOVE";
    private static boolean mHasBootComplete = false;

    private boolean checkSimRadioState(Context context, int slotId) {
        int dualSimMode = -1;
        boolean result = false;

        /* dualSimMode: 0 => both are off, 1 => SIM1 is on, 2 => SIM2 is on, 3 => both is on */
        dualSimMode = Settings.System.getInt(context.getContentResolver(),
                Settings.System.MSIM_MODE_SETTING, -1);

        CatLog.d(LOG_TAG, "dualSimMode: " + dualSimMode + ", sim id: " + slotId);
        int curRadioOnSim = (dualSimMode & (0x01 << slotId));
        CatLog.d(LOG_TAG, "result: " + curRadioOnSim);
        if (curRadioOnSim != 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isStkApkInitGone(Context context, int simId) {
        int mode = 0;

        /*For OP02 spec v4.1 start*/
        String optr = SystemProperties.get("ro.operator.optr");
        //For OP02, the stk install state can not be changed by flight mode.
        if (optr != null && "OP02".equals(optr)) {   
            CatLog.d(this, "[isStkApkInitGone]working for OP02...");
            //Check if the icc card is absent or not.
            if (0 == StkAppService.getIccCardState(simId)) {
                return true;
            } else {
                return false;
            }
        }
        //In Normal case, if flight mode is on, we should uninstall stk apk.
        try {
            mode = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON);
        } catch(SettingNotFoundException e) {
            CatLog.d(LOG_TAG, "fail to get airlane mode");
            mode = 0;
        }
        CatLog.d(LOG_TAG, "airlane mode is " + mode);
        return (mode != 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        StkAppInstaller appInstaller = StkAppInstaller.getInstance();
        StkAppService appService = StkAppService.getInstance();

        // make sure the app icon is removed every time the device boots.
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Bundle args = new Bundle();
            args.putInt(StkAppService.OPCODE, StkAppService.OP_BOOT_COMPLETED);
            context.startService(new Intent(context, StkAppService.class)
                    .putExtras(args));
            /* TODO: Gemini+ begine */
            if (false == SystemProperties.get("ro.mtk_gemini_support").equals("1")) {
                /* Hide icon of SIM2-SIM4 */
                appInstaller.unInstall(context, PhoneConstants.SIM_ID_2);
                appInstaller.unInstall(context, PhoneConstants.SIM_ID_3);
                appInstaller.unInstall(context, PhoneConstants.SIM_ID_4);
            } else {
                if (SystemProperties.get("ro.mtk_gemini_3sim_support").equals("1") != true && SystemProperties.get("ro.mtk_gemini_4sim_support").equals("1") != true) {
                    appInstaller.unInstall(context, PhoneConstants.SIM_ID_3);
                }
                if (SystemProperties.get("ro.mtk_gemini_4sim_support").equals("1") != true) {
                    appInstaller.unInstall(context, PhoneConstants.SIM_ID_4);
                }
            }
            /* TODO: Gemini+ end */
            mHasBootComplete = true;
            CatLog.d(LOG_TAG, "[ACTION_BOOT_COMPLETED]");
        } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            CatLog.d(LOG_TAG, "get ACTION_SIM_STATE_CHANGED");

            int SIMID = intent.getIntExtra(PhoneConstants.SLOT_KEY,-1);
            String SIMStatus = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
            CatLog.d(LOG_TAG, "[ACTION_SIM_STATE_CHANGED][simId] : " + SIMID);
            CatLog.d(LOG_TAG, "[ACTION_SIM_STATE_CHANGED][SimStatus] : " + SIMStatus);
            if(SIMID >= PhoneConstants.SIM_ID_1 && SIMID <= PhoneConstants.SIM_ID_4){
                //deal with SIM1
                CatLog.d(LOG_TAG, "[ACTION_SIM_STATE_CHANGED][Feature GEMINI]");
                Bundle bundle = new Bundle();
                bundle.putString("affinity", "com.android.stk");
                final Intent it = new Intent();
                it.putExtras(bundle);

                CatLog.d(LOG_TAG, "isSetupMenuCalled[" + StkAppService.isSetupMenuCalled(SIMID) + "]");
                CatLog.d(LOG_TAG, "mHasBootComplete[" + mHasBootComplete + "]");

                boolean bUnInstall = true;
                if ((StkAppService.isSetupMenuCalled(SIMID)) && (((IccCardConstants.INTENT_VALUE_ICC_READY).equals(SIMStatus))||((IccCardConstants.INTENT_VALUE_ICC_IMSI).equals(SIMStatus)))) {
                    bUnInstall = false;
                }

                int app_state = appInstaller.getIsInstalled(SIMID);
                if (app_state == -1) {
                    CatLog.d(LOG_TAG, "Initialize the app state in launcher");
                    appInstaller.install(context, SIMID);
                    if (checkSimRadioState(context, SIMID) != true
                            || true == isStkApkInitGone(context, SIMID)) {
                        /* The SIM card is off so uninstall it */
                        SystemClock.sleep(100);
                        appInstaller.unInstall(context, SIMID);
                    }
                } else {//The bUnInstall init value is true, to sync the value of bUnInstall and app_state at LOADED state.
                    if ((IccCardConstants.INTENT_VALUE_ICC_LOADED).equals(SIMStatus)) {
                        /* kepp STK at the same state */
                        bUnInstall = (app_state == StkAppInstaller.STK_INSTALLED)? false : true;
                        CatLog.d(LOG_TAG, "loaded state,  app_state: " + app_state + ", bUnInstall: " + bUnInstall);
                    }
                }

                if ((((IccCardConstants.INTENT_VALUE_ICC_READY).equals(SIMStatus)) || ((IccCardConstants.INTENT_VALUE_ICC_LOADED).equals(SIMStatus)))
                    && mHasBootComplete && !CatService.getSaveNewSetUpMenuFlag(SIMID))
                {
                    /* 1. SIM ready and get intent boot_complete. It must be phone reboot. 
                       2. In the case, if we still not get SET_UP_MENU, the SIM card may not support SAT 
                    */
                    if (((IccCardConstants.INTENT_VALUE_ICC_LOADED).equals(SIMStatus))) {
                        CatLog.d(LOG_TAG, "Disable the STK of sim" + (SIMID + 1) + " because still not receive SET_UP_MENU after boot up");
                        appService.StkAvailable(SIMID, StkAppService.STK_AVAIL_NOT_AVAILABLE);
                        appInstaller.unInstall(context, SIMID);
                        appService.setUserAccessState(false, SIMID);
                        bUnInstall = true;
                    } else {
                        /* Remove that SET_UP_MENU command from DB */
                        Bundle args = new Bundle();
                        args.putInt(StkAppService.OPCODE, StkAppService.OP_REMOVE_STM);
                        args.putInt(StkAppService.SLOT_ID, SIMID);
                        context.startService(new Intent(context, StkAppService.class).putExtras(args));
                    }
                } else if ((IccCardConstants.INTENT_VALUE_ICC_ABSENT).equals(SIMStatus)) {
                    CatLog.d(LOG_TAG, "SIM_ABSENT, removed sim: " + SIMID);
                    Bundle args = new Bundle();
                    args.putInt(StkAppService.OPCODE, StkAppService.OP_REMOVE_STM);
                    args.putInt(StkAppService.SLOT_ID, SIMID);
                    context.startService(new Intent(context, StkAppService.class).putExtras(args));
                } else {
                    if(null == appService && ((IccCardConstants.INTENT_VALUE_ICC_NOT_READY).equals(SIMStatus))) {
                        CatLog.d(LOG_TAG, "null == appService && NOT_READY, start StkAppService.");                        
                        Bundle args = new Bundle();
                        args.putInt(StkAppService.OPCODE, StkAppService.OP_BOOT_COMPLETED);
                        context.startService(new Intent(context, StkAppService.class)
                                .putExtras(args));                        
                    }
                }

                if (appService != null) {
                    int currentState = appService.StkQueryAvailable(SIMID);
                    CatLog.d(LOG_TAG, "[ACTION_SIM_STATE_CHANGED][bUnInstall] : " + bUnInstall + ", currentState: " + currentState);
                    if (bUnInstall && app_state == StkAppInstaller.STK_INSTALLED) {
                        /*For OP02 spec v4.1 start*/
                        String optr = SystemProperties.get("ro.operator.optr");
                        if (optr != null && "OP02".equals(optr)) {
                            CatLog.d(this, "working for OP02...");                            
                            if((IccCardConstants.INTENT_VALUE_ICC_LOCKED).equals(SIMStatus) || (IccCardConstants.INTENT_VALUE_ICC_ABSENT).equals(SIMStatus) || checkSimRadioState(context, SIMID) == false){
                                CatLog.d(this, "unInstall it~~");                                                            
                                CatLog.d(LOG_TAG, "OP02, ADD_RECENET_IGNORE");
                                it.setAction("android.intent.action.ADD_RECENET_IGNORE");
                                context.sendBroadcast(it);
                                appService.StkAvailable(SIMID, StkAppService.STK_AVAIL_NOT_AVAILABLE);                                                                                    
                                appInstaller.unInstall(context, SIMID);
                                appService.setUserAccessState(false, SIMID);
                            }
                        }/*For OP02 spec v4.1 end*/
                        else {
                            CatLog.d(LOG_TAG, "Not OPO2, ADD_RECENET_IGNORE");
                            it.setAction("android.intent.action.ADD_RECENET_IGNORE");
                            context.sendBroadcast(it);
                            appService.StkAvailable(SIMID, StkAppService.STK_AVAIL_NOT_AVAILABLE);                                                                                                         	
                            appInstaller.unInstall(context, SIMID);
                            appService.setUserAccessState(false, SIMID);
                        }
                    } else if (!bUnInstall && app_state == StkAppInstaller.STK_NOT_INSTALLED){
                        CatLog.d(LOG_TAG, "REMOVE_RECENET_IGNORE");
                        it.setAction("android.intent.action.REMOVE_RECENET_IGNORE");
                        context.sendBroadcast(it);

                        CatLog.d(LOG_TAG, "get ACTION_SIM_STATE_CHANGED - install");
                        appInstaller.install(context, SIMID);
                        appService.StkAvailable(SIMID, StkAppService.STK_AVAIL_AVAILABLE);
                    }
                } else {
                    CatLog.d(LOG_TAG, "get ACTION_SIM_STATE_CHANGED - StkAppService instance is null");
                }
            }
            CatLog.d(LOG_TAG, "get ACTION_SIM_STATE_CHANGED  finish");
        } else if (action.equals(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED)) {
            int SIMID = intent.getIntExtra(PhoneConstants.SLOT_KEY,-1);
            String newType = intent.getStringExtra(PhoneConstants.PHONE_NAME_KEY);
            CatLog.d(LOG_TAG, "GSM/CDMA changes, sim id: " + SIMID + ", new type: " + newType);
            if (SIMID == PhoneConstants.SIM_ID_1 && newType.equals("CDMA")) {
                /* Disable SIM1 stk because SIM1 is CDMA now */
                appInstaller.unInstall(context, SIMID);
            }
        }
    }
}
