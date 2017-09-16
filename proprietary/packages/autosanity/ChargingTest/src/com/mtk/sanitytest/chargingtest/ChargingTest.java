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

package com.mtk.sanitytest.chargingtest;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;


/**
 * Tests for {@link EntityDelta} and {@link ValuesDelta}. These tests
 * focus on passing changes across {@link Parcel}, and verifying that they
 * correctly build expected "diff" operations.
 */
@LargeTest
public class ChargingTest extends InstrumentationTestCase {
    public static final String TAG = "SanityTest_ChargingTest";
    Instrumentation mInst = null;
    Context mContext = null;
    public boolean mRes = false;
    public ChargingTest() {
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
     * @throws Throwable
     *
     */

    @LargeTest
    public void testChargingStatus() throws Throwable {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                 Log.i(TAG, "***************Intent = " + intent);
                 int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                 mRes = (BatteryManager.BATTERY_STATUS_CHARGING == status) || (BatteryManager.BATTERY_STATUS_FULL == status);
                 Log.i(TAG, "***************status in if = " + status);
                 assertTrue("***************status in if = " + status, mRes);
/*
                 if (BatteryManager.BATTERY_STATUS_CHARGING == status || BatteryManager.BATTERY_STATUS_FULL == status ){
                     Log.i(TAG, "***************status in if = " +status);
                     mRes = true;
                    assertEquals(1,1);
                 }
                 else{
                     Log.i(TAG, "***************status in else = " +status);
                     mRes = false;
                     assertEquals(0,1);
                 }*/
            }
        }, filter);

    }





}
