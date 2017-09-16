package com.hesine.nmsg.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hesine.nmsg.util.MLog;

public class AutoStartReceiver extends BroadcastReceiver {
    public static final String TAG = "AutoStartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        MLog.trace(TAG, "receive action:" + intent.getAction());
        if(!NmsgService.getInstance().isServiceStart()){
            Intent i = new Intent(context, NmsgService.class);
            context.startService(i);
        }
    }
}
