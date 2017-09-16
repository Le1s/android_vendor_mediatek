package com.mediatek.mediatekdm.test;

import android.app.Service;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.PhoneConstants;
import com.mediatek.mediatekdm.PlatformManager;

import java.lang.reflect.Field;

public class MockPlatformManager extends PlatformManager {
    public static final String TAG = "MDMTest/MockPlatformManager";

    public MockPlatformManager() {
        super();
    }

    public static void setUp() {
        PlatformManager pm = new MockPlatformManager();
        try {
            Field field = PlatformManager.class.getDeclaredField("sInstance");
            field.setAccessible(true);
            field.set(PlatformManager.class, pm);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static void tearDown() {
        try {
            Field field = PlatformManager.class.getDeclaredField("sInstance");
            field.setAccessible(true);
            field.set(PlatformManager.class, null);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    // ********************************************
    // Telephony & Subscription
    // ********************************************
    @Override
    public String getDeviceImei() {
        return TelephonyManager.getDefault().getDeviceId(PhoneConstants.SIM_ID_1);
    }

    @Override
    public String getSubImsi(long subId) {
        if (subId == TestEnvironment.REGISTER_SUB_ID) {
            return TestEnvironment.TEST_IMSI;
        } else {
            return null;
        }
    }

    @Override
    public String getSubOperator(long subId) {
        return TestEnvironment.TEST_MCCMNC;
    }

    @Override
    public long[] getSubIdList() {
        return new long[] { TestEnvironment.REGISTER_SUB_ID };
    }

    // ********************************************
    // Service priority
    // ********************************************
    @Override
    public void stayForeground(Service service) {
        Log.i(TAG, "Bring service to foreground");
    }

    @Override
    public void leaveForeground(Service service) {
        Log.d(TAG, "Exec stopForeground with para true.");
    }

    // ********************************************
    // DmAgent related
    // ********************************************
    @Override
    public void clearFileWhenSwitch(Context context) {
        Log.i(TAG, "clearFileWhenSwitch");
    }

    @Override
    public long getRegisteredSubId() {
        return TestEnvironment.REGISTER_SUB_ID;
    }
}
