/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.mediatek.op09.plugin;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.mediatek.xlog.Xlog;


/**
 * <code>PrlService</code> is a service which will be sent a
 * SMS "PRL" to 10659465 in the background, and toast is shown
 * to alert the user.
 *
 * @author mtk54034
 */
public class PrlService extends IntentService {

    private static final String TAG = "PrlService";
    private static final String PRL_NUMBER = "10659165";
    private static final String PRL_CONTENT = "PRL";

    public PrlService() {
        super("PrlService");
        Xlog.d(TAG, "in PrlService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, getString(R.string.prl_sending), Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(), 0);
        smsManager.sendTextMessage(PRL_NUMBER, null, PRL_CONTENT, pendingIntent, null);
        Xlog.d(TAG, "in PrlService onHandleIntent! Send Successfully!");
    }

}
