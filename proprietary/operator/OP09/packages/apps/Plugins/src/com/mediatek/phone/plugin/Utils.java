package com.mediatek.phone.plugin;

import android.media.AudioSystem;
import android.os.SystemProperties;

/**
 * Tools for replace MTK_FEATURE_OPTION.
 */
public class Utils {
    private static final String TAG = "Utils";

    private static final String MTK_FEATURE_TTY_STATE = "MTK_FEATURE_TTY_SUPPORT";
    private static final String MTK_FEATURE_TTY_ON = "MTK_FEATURE_TTY_SUPPORT=true";
    private static final String MTK_FEATURE_DUAL_MIC_STATE = "MTK_FEATURE_DUAL_MIC_SUPPORT";
    private static final String MTK_FEATURE_DUAL_MIC_ON = "MTK_FEATURE_DUAL_MIC_SUPPORT=true";

    /**
     * GeminiSupport support or not.
     * @return true if support GeminiSupport
     */
    public static boolean isGeminiSupport() {
        return SystemProperties.getInt("ro.mtk_gemini_support", 0) == 1;
    }

    /**
     * EvdoIRSupport support or not.
     * @return true if support EvdoIRSupport
     */
    public static boolean isEvdoIRSupport() {
        return SystemProperties.getInt("ro.evdo_ir_support", 0) == 1;
    }

    /**
     * MTK_FEATURE_TTY_STATE support or not.
     * @return true if support MTK_FEATURE_TTY_STATE
     */
    public static boolean isTtySupport() {
        String state = AudioSystem.getParameters(MTK_FEATURE_TTY_STATE);
        if (null == state) {
            return false;
        }
        return state.equalsIgnoreCase(MTK_FEATURE_TTY_ON);
    }

    /**
     * MTK_FEATURE_DUAL_MIC_STATE support or not.
     * @return true if support MTK_FEATURE_DUAL_MIC_STATE
     */
    public static boolean isDualMic() {
        String state = AudioSystem.getParameters(MTK_FEATURE_DUAL_MIC_STATE);
        if (null == state) {
            return false;
        }
        return state.equalsIgnoreCase(MTK_FEATURE_DUAL_MIC_ON);
    }
}
