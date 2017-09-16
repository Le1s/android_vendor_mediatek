package com.hesine.nmsg.observer;

import com.hesine.nmsg.api.Activation;
import com.hesine.nmsg.config.Config;
import com.hesine.nmsg.pn.PNControler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkChangeReceiver extends BroadcastReceiver {
    public static final String TAG = "NmsNetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetInfo = cm.getActiveNetworkInfo();
                if (activeNetInfo != null && activeNetInfo.isConnected()) {
                    if(!Config.getIsActivated()) {
                        Activation.instance().start();
                    } else {
                    	PNControler.startPN(context);
                    }
                } else {

                }
            } else {

            }
        } catch (Exception e) {
        }
    }
}
