package com.mediatek.settings.plugin;

import android.os.SystemProperties;

/**
 * replace MTK_FEATION_OPTION.
 */
public class Utils {
    private static final String TAG = "Utils";

    // Check if mtk gemini feature is enabled
    public static boolean isGeminiSupport() {
        return SystemProperties.getInt("ro.mtk_gemini_support", 0) == 1;
    }

    public static boolean isMtkSharedSdcardSupport() {
        return SystemProperties.get("ro.mtk_shared_sdcard").equals("1");
    }

}