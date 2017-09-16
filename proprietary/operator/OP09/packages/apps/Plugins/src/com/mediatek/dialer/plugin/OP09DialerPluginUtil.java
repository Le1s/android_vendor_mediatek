package com.mediatek.dialer.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;
import android.util.Log;

import com.mediatek.phone.SIMInfoWrapper;

import java.util.Date;
import java.util.TimeZone;

public class OP09DialerPluginUtil {

    private static final String TAG = "OP09DialerPluginUtil";

    public static final String BEIJING_TIME_ZONE_ID = "Asia/Shanghai";
    public static final String BROADCAST_TIME_DISPLAY_MODE = "com.mediatek.ct.TIME_DISPLAY_MODE";

    private Context mPluginContext;
    private int mTimezoneRawOffset = 0;

    public OP09DialerPluginUtil(Context context) {
        try {
            mPluginContext = context.createPackageContext("com.mediatek.op09.plugin", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            Log.d(TAG, "no com.mediatek.op09.plugin packages");
        }
        SIMInfoWrapper.getDefault().init(mPluginContext);
        updateTimezoneRawOffset();
        mPluginContext.registerReceiver(mTimeDisplayModeReceiver, new IntentFilter(BROADCAST_TIME_DISPLAY_MODE));
    }

    private BroadcastReceiver mTimeDisplayModeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_TIME_DISPLAY_MODE)) {
                log("received time display mode broadcast message");
                updateTimezoneRawOffset();
            }
        }
    };

    private void updateTimezoneRawOffset() {
        if (0 == Settings.System.getInt(mPluginContext.getContentResolver(), "Settings.System.CT_TIME_DISPLAY_MODE", 1)) {
            mTimezoneRawOffset = TimeZone.getTimeZone(OP09DialerPluginUtil.BEIJING_TIME_ZONE_ID).getRawOffset()
                - TimeZone.getDefault().getRawOffset();
        } else {
            mTimezoneRawOffset = 0;
        }
    }

    public int getTimezoneRawOffset() {
        return mTimezoneRawOffset;
    }

    public int getTimezoneOffset(long date) {
        if (0 == mTimezoneRawOffset) {
            return 0;
        } else {
            if (TimeZone.getDefault().inDaylightTime(new Date(date))) {
                return mTimezoneRawOffset - TimeZone.getDefault().getDSTSavings();
            } else {
                return mTimezoneRawOffset;
            }
        }
    }

    public Context getPluginContext() {
        return mPluginContext;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
