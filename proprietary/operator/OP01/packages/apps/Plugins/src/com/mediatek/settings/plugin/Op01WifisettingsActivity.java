package com.mediatek.op01.plugin;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import com.mediatek.xlog.Xlog;


public class Op01WifisettingsActivity  extends Activity {
    private static final String TAG = "Op01WifisettingsActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Xlog.d(TAG, "Op01WifisettingsActivity screen onCreate and try to open wifisettings");
        Intent intent = new Intent("android.settings.WIFI_SETTINGS");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
