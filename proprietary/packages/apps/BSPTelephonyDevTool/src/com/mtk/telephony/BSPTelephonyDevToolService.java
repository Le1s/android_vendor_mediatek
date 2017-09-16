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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ServiceManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;

import android.telephony.SubscriptionManager;

public class BSPTelephonyDevToolService extends Service {
    private static final String LOG_TAG = "BSPTelephonyDev";
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final int[] NOTIFICATION_ID_SIM = {0x500, 0x520, 0x540, 0x560};

    private static boolean sIsRunning = false;

    private static TelephonyManager sTelephonyManager;
    private static NotificationManager sNotificationManager;
    private static Notification[] sNotification = new Notification[PROJECT_SIM_NUM];
    private static RemoteViews[] sRemoteViews = new RemoteViews[PROJECT_SIM_NUM];
    private static SignalStrength[] sSignalStrength = new SignalStrength[PROJECT_SIM_NUM];
    private MultiSimPhoneStateListener[] mPhoneStateListener = new MultiSimPhoneStateListener[PROJECT_SIM_NUM];

    @Override
    public void onCreate() {
        super.onCreate();
        logd("BSP package telephony dev service started");

        sTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        sNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, BSPTelephonyDevToolActivity.class), 0);

        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            String phoneIdText = getString(R.string.phone) + (i + 1);
            sNotification[i] = new Notification();
            sNotification[i].flags = Notification.FLAG_NO_CLEAR;
            sNotification[i].contentIntent = contentIntent;
            sNotification[i].icon = R.drawable.ic_launcher;

            long[] subId = SubscriptionManager.getSubId(i);
            if (subId == null || subId.length == 0 || subId[0] <= 0) {
                logd("Phone" + i + ": Invalid subId to register for PhoneStateListener");
                continue;
            } else {
                mPhoneStateListener[i] = new MultiSimPhoneStateListener(i, subId[0]);
                sTelephonyManager.listen(mPhoneStateListener[i],
                        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                        | PhoneStateListener.LISTEN_DATA_ACTIVITY);
            }
            sRemoteViews[i] = new RemoteViews(getPackageName(), R.layout.notification);
            sRemoteViews[i].setTextViewText(R.id.notification_phone_id, phoneIdText);
            updateNotifications(i);
        }
        sIsRunning = true;
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
            int strengthValue = 0;
            if (signalStrength != null) {
                strengthValue = signalStrength.getGsmSignalStrength();
            }
            if (strengthValue == 99) {
                strengthValue = 0;
            }
            sRemoteViews[mPhoneId].setProgressBar(R.id.notification_progress_signal,
                    31, strengthValue, false);
            updateNotifications(mPhoneId);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            String networkType = Utility.getNetworkTypeString(serviceState.getNetworkType());
            logd("[onServiceStateChanged] " + mPhoneString + ": " + networkType);
            sRemoteViews[mPhoneId].setTextViewText(R.id.notification_network_type, networkType);
            updateNotifications(mPhoneId);
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            logd("[onDataConnectionStateChanged] " + mPhoneString + ": state=" + state
                    + " networkType=" + networkType);
            if (state == TelephonyManager.DATA_CONNECTED) {
                sRemoteViews[mPhoneId].setTextViewText(R.id.notification_data_connection_type,
                        Utility.getNetworkTypeString(networkType));
            } else {
                sRemoteViews[mPhoneId].setTextViewText(R.id.notification_data_connection_type, "");
            }
            updateNotifications(mPhoneId);
        }

        @Override
        public void onDataActivity(int direction) {
            String dataDirection = Utility.getDataDirectionString(direction);
            logd("[onDataActivity] " + mPhoneString + ": " + dataDirection);
            sRemoteViews[mPhoneId].setTextViewText(R.id.notification_data_activity, dataDirection);
            updateNotifications(mPhoneId);
        }
    }

    public static boolean isRunning() {
        return sIsRunning;
    }

    private void updateNotifications(int phoneId) {
        sNotification[phoneId].contentView = sRemoteViews[phoneId];
        sNotificationManager.notify(NOTIFICATION_ID_SIM[phoneId], sNotification[phoneId]);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            sTelephonyManager.listen(mPhoneStateListener[i], PhoneStateListener.LISTEN_NONE);
            sNotificationManager.cancel(NOTIFICATION_ID_SIM[i]);
        }
        sIsRunning = false;

        logd("BSP package telephony dev service stopped");
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, "[BSPTelDevToolService]" + msg);
    }
}
