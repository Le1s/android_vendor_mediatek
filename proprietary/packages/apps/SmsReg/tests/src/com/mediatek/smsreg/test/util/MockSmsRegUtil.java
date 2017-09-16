package com.mediatek.smsreg.test.util;

import android.util.Log;

import com.mediatek.smsreg.PlatformManager;
import com.mediatek.smsreg.SmsRegApplication;

import java.lang.reflect.Field;

public class MockSmsRegUtil {
    private static final String TAG = "SmsReg/MockDataUtil";

    /**
     * Format log
     */
    public static void formatLog(String tag, String content) {
        Log.i(tag, "----- " + content + "-----");
    }

    /**
     * Inject a customized platform manager
     */
    public static void preparePlatformManager(SmsRegApplication application) {
        Log.i(TAG, "preparePlatformManager");
        PlatformManager platformManager = new MockPlatformManager();
        preparePlatformManager(application, platformManager);
    }

    /**
     * Inject a customized platform manager
     */
    private static void preparePlatformManager(SmsRegApplication application,
            PlatformManager platformManager) {
        try {
            Field field = SmsRegApplication.class.getDeclaredField("sPlatformManager");
            field.setAccessible(true);
            field.set(application, platformManager);
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        } catch (IllegalArgumentException e) {
            throw new Error(e);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }
}
