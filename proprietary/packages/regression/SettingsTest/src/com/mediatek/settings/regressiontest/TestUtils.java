package com.mediatek.settings.regressiontest;

import android.app.Activity;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.Log;

import com.mediatek.storage.StorageManagerEx;

public class TestUtils {
    public static final boolean MTK_2SDCARD_SWAP = getValue("ro.mtk_2sdcard_swap");

    public static String[] getStringArray(Activity activity, String resName) {
        Resources resources = activity.getResources();
        int resId = resources.getIdentifier(resName, "array", "com.android.settings");
        return resources.getStringArray(resId);
    }

    public static String getString(Activity activity, String resName) {
        int resId = activity.getResources()
                .getIdentifier(resName, "string", "com.android.settings");
        return activity.getString(resId);
    }

    public static String getString(Activity activity, String resName, String x) {
        int resId = activity.getResources()
                .getIdentifier(resName, "string", "com.android.settings");
        return activity.getString(resId, x);
    }

    private static boolean getValue(String key) {
        return SystemProperties.get(key).equals("1");
    }

    public static boolean isExSdcardInserted() {
        boolean isExSdcardInserted = StorageManagerEx.getSdSwapState();
        Log.d("TestUtils", "isExSdcardInserted : " + isExSdcardInserted);
        return isExSdcardInserted;
    }
}
