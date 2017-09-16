package com.mediatek.smsreg;

import android.app.Application;
import android.util.Log;

public class SmsRegApplication extends Application {
    private static final String TAG = "SmsReg/Application";
    private static PlatformManager sPlatformManager = null;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        sPlatformManager = new PlatformManager();
    }

    public static PlatformManager getPlatformManager() {
        return sPlatformManager;
    }
}
