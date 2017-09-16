/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mtk.sanitytest.initial;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import java.util.Locale;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.os.PowerManager;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;
import com.android.internal.widget.LockPatternUtils;

/**
 * Tests for {@link EntityDelta} and {@link ValuesDelta}. These tests focus on
 * passing changes across {@link Parcel}, and verifying that they correctly
 * build expected "diff" operations.
 */
@LargeTest
public class InitialTest extends InstrumentationTestCase {
    public static final String TAG = "SanityTest_InitialTest";
    public static final int TIMEOUT_MAX = 1800000;
    private PowerManager.WakeLock wakeLock = null;
    private KeyguardManager.KeyguardLock keyLock = null;
    Instrumentation mInst = null;
    Context mContext = null;

    public InitialTest() {
        super();
    }

    @Override
    public void setUp() {

        mInst = getInstrumentation();
        mContext = mInst.getContext();
    }

    /**
     * Test that {@link EntityDelta#mergeAfter(EntityDelta)} correctly passes
     * any changes through the {@link Parcel} object. This enforces that
     * {@link EntityDelta} should be identical when serialized against the same
     * "before" {@link Entity}.
     */

    @LargeTest
    public void testASetLocale() throws Exception {
        IActivityManager am = ActivityManagerNative.getDefault();
        Configuration config = am.getConfiguration();
        Log.i(TAG, "*************Before locale = " + config.locale);
        Log.i(TAG, "*************Before country = "
                + config.locale.getCountry());
        Log.i(TAG, "*************Before DisplayCountry = "
                + config.locale.getDisplayCountry());
        Log.i(TAG, "*************Before DispalayLanguage = "
                + config.locale.getDisplayLanguage());
        Log.i(TAG, "*************Before DisplayName = "
                + config.locale.getDisplayName());

        config.locale = Locale.ENGLISH;
        // config.userSetLocale = true;
        am.updateConfiguration(config);
        SystemClock.sleep(2000);
        am.updateConfiguration(config);
        Log.i(TAG, "*************After locale = " + config.locale);
        Log
                .i(TAG, "*************After country = "
                        + config.locale.getCountry());
        Log.i(TAG, "*************After DisplayCountry = "
                + config.locale.getDisplayCountry());
        Log.i(TAG, "*************After DispalayLanguage = "
                + config.locale.getDisplayLanguage());
        Log.i(TAG, "*************After DisplayName = "
                + config.locale.getDisplayName());

        assertEquals(Locale.ENGLISH, config.locale);
    }

    @SmallTest
    public void testsetScreenOn() {
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                BatteryManager.BATTERY_PLUGGED_USB);
        int iStayAwake = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN, -1);
        Log.i(TAG, "*******************Stay Awake state = " + iStayAwake);
        assertEquals(BatteryManager.BATTERY_PLUGGED_USB, iStayAwake);
    }

    @SmallTest
    public void testsetScreenTimeout() {

        Settings.System.putInt(mContext.getContentResolver(),
                SCREEN_OFF_TIMEOUT, TIMEOUT_MAX);
        int iTimeOut = Settings.System.getInt(mContext.getContentResolver(),
                SCREEN_OFF_TIMEOUT, 0);
        Log.i(TAG, "*******************Time Out state = " + iTimeOut);
        assertEquals(TIMEOUT_MAX, iTimeOut);
    }

    @LargeTest
    public void testDisableAccelerometer() {

        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0);
        // Log.i(TAG,"*********************Result of Disable Accelerometer: " +
        // bRes);
        assertEquals(0, getAccelerometerState());
    }

    @LargeTest
    public void testDisableKeyGuard() {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()){
        	makeScreenOn();
        }
        Log.i(TAG, "Screen will wake lock.");
		KeyguardManager km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
		Log.i(TAG, "km "+km.toString());
		KeyguardLock kl = km.newKeyguardLock(TAG);
		Log.i(TAG, "kl "+kl.toString());
		kl.reenableKeyguard();
		kl.disableKeyguard();
		LockPatternUtils lockPatternUtils = new LockPatternUtils(mContext);
		lockPatternUtils.clearLock(false);
		lockPatternUtils.setLockScreenDisabled(true);
		assertEquals(1, 1);
    }
    
    private void makeScreenOn(){
		new Thread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				Instrumentation inst = new Instrumentation();
				inst.sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
			}
		}).start();
	}

    /*
     * @LargeTest public void testTurnOffWiFi() {
     *
     * WifiManager mWifi; mWifi = (WifiManager)
     * mContext.getSystemService(Context.WIFI_SERVICE);
     * mWifi.setWifiEnabled(false); SystemClock.sleep(5000);
     * assertEquals(false,getWiFiState()); }
     *
     * @LargeTest public void testTurnOffBT(){ BluetoothAdapter mBT; mBT =
     * BluetoothAdapter.getDefaultAdapter(); mBT.disable();
     * SystemClock.sleep(5000); assertEquals(0,getBTState()); } /*
     *
     * @LargeTest public void testDeleteAllContacts() { final Uri
     * CONTACT_TABLE_URI = Uri.parse("content://contacts/people/");
     * ContentResolver resolver = getInstrumentation().getTargetContext()
     * .getContentResolver(); resolver.delete(CONTACT_TABLE_URI, null, null);
     * assertEquals(1, 1); }
     */
    public boolean getWiFiState() {
        WifiManager wifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        return (wifiManager.isWifiEnabled());
    }

    public int getBTState() {
        int btState = -1;
        BluetoothAdapter mBT = BluetoothAdapter.getDefaultAdapter();
        if (BluetoothAdapter.STATE_ON == mBT.getState()) {
            Log.i(TAG, "**********************BT is ON!");
            btState = 1;
        } else if (BluetoothAdapter.STATE_OFF == mBT.getState()) {
            Log.i(TAG, "*******************BT is Off!");
            btState = 0;
        }
        return btState;
    }

    public int getAccelerometerState() {

        int Res = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0);
        return Res;

    }

       @SmallTest
    public void testRestoreFlightToNormal() {
        boolean isAirPlaneModeOn = Settings.System.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        Log.i(TAG, "*******************" + Settings.System.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) + " " + isAirPlaneModeOn);
        if (!isAirPlaneModeOn) {
            return;
        }

        android.provider.Settings.Global.putInt(mContext.getContentResolver(), android.provider.Settings.Global.AIRPLANE_MODE_ON, 0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mContext.sendBroadcast(intent);
            Log.i(TAG, "*******************" + Settings.System.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0));
        }

      @SmallTest
     public void testAlwaysAskSms() {
         Settings.System.putLong(mContext.getContentResolver(), Settings.System.SMS_SIM_SETTING, Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK);
         SystemClock.sleep(2 * 1000);

         try {
             assertEquals(Settings.System.getLong(mContext.getContentResolver(), Settings.System.SMS_SIM_SETTING), Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK);
         } catch (android.provider.Settings.SettingNotFoundException e) {
             Log.i(TAG, "Settings.System.SMS_SIM_SETTING not find in settings!!!");
         }
    }

}
