package com.mtk.fts;

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.content.Context;

public class WifiConnectReceiver extends BroadcastReceiver {
    public static final String WIFI_CONNECT = "com.mtk.wificonnectreceiver.wificonnect";
    public static final String WIFI_DISCONNECT = "com.mtk.wificonnectreceiver.wifidisconnect";
    private static final String WIFI_ENABLE_SERVICE = "com.mtk.FunctionalTestService";
    private static final String SSID = "SSID";
    private static final String SECURITY = "security";
    private static final String IDENTITY = "identity";
    private static final String PASSWORD = "password";
    private static final String TAG = "WifiConnectReceiver";
    public static final String OPEN_OR_CLOSE = "wifiOpenOrClose";


    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "enter wifi connect receiver");
        String intentAction = intent.getAction();
        Intent aIntent = new Intent();
        aIntent.setClass(context, FTestService.class);
        aIntent.setAction(WIFI_ENABLE_SERVICE);
        if (WIFI_CONNECT.equals(intentAction)) {
            Log.i(TAG, "receiver; start service!");
            aIntent.putExtra(OPEN_OR_CLOSE,WIFI_CONNECT);
            aIntent.putExtra(SSID,intent.getStringExtra(SSID));
            aIntent.putExtra(SECURITY,intent.getIntExtra(SECURITY,0));
            aIntent.putExtra(IDENTITY, intent.getStringExtra(IDENTITY));
            aIntent.putExtra(PASSWORD, intent.getStringExtra(PASSWORD));
            context.startService(aIntent);
        } else if (WIFI_DISCONNECT.equals(intentAction)) {
            Log.i(TAG, "receiver; stop service!");
            aIntent.putExtra(OPEN_OR_CLOSE,WIFI_DISCONNECT);
            context.startService(aIntent);
        } 
    }
    

}
