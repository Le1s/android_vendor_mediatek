/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.email.regression.tests;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.android.emailcommon.Logging;

import junit.framework.TestCase;

/**
 * Utility methods used only by tests.
 */
@LargeTest
public class TestUtils extends TestCase /* It tests itself */ {
    public interface Condition {
        public boolean isMet();
    }

    /**
     * Wait until a {@code Condition} is met.
     *
     * @param condition
     *            condition to sleep
     * @param timeoutSeconds
     *            sleep time
     */
    public static void waitUntil(Condition condition, int timeoutSeconds) {
        waitUntil("", condition, timeoutSeconds);
    }

    /**
     * Wait until a {@code Condition} is met.
     *
     * @param message
     *            assert meesage
     * @param condition
     *            condition to sleep
     * @param timeoutSeconds
     *            sleep time
     */
    public static void waitUntil(String message, Condition condition, int timeoutSeconds) {
        waitUntil(message, condition, timeoutSeconds, 500);
    }

    /**
     * Wait until a {@code Condition} is met.
     *
     * @param message
     *            assert meesage
     * @param condition
     *            condition to sleep
     * @param timeoutSeconds
     *            timeout time
     * @param sleeptime
     *            sleep time
     */
    public static void waitUntil(String message, Condition condition,
            int timeoutSeconds, int sleeptime) {
        Log.d(Logging.LOG_TAG, message + ": Waiting...");
        final long timeout = System.currentTimeMillis() + timeoutSeconds * 1000;
        while (System.currentTimeMillis() < timeout) {
            if (condition.isMet()) {
                return;
            }
            try {
                Thread.sleep(sleeptime);
            } catch (InterruptedException ignore) {
            }
        }
        fail(message + ": Timeout");
    }

    /**
     * check device unlock station.
     *
     * @param context
     *            context
     * @return true if the screen is on and not locked; false otherwise, in
     *         which case tests that send key events will fail.
     */
    public static boolean isScreenOnAndNotLocked(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
            return false;
        }
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            return false;
        }
        return true;
    }

    /**
     * Get string for given resource name.
     *
     * @param activity
     *            resouce activity
     * @param resName
     *            resource name
     * @return resouce string
     */
    public static String getString(Activity activity, String resName) {
        int resId = activity.getResources()
                .getIdentifier(resName, "string", "com.android.email");
        return activity.getString(resId);
    }

    /**
     * Get resouce id for given resource name.
     *
     * @param activity
     *            resouce activity
     * @param resName
     *            resource name
     * @return resouce id
     */
    public static int getResouceId(Activity activity, String resName) {
        return activity.getResources()
                .getIdentifier(resName, "id", "com.android.email");
    }
    /**
     * Drag the screen down with a quarter distance right at left quarter screen
     */
    public static void dragToLeft(InstrumentationTestCase test, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        final android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);

        final float fromX = size.x * 0.95f;
        final float toX = size.x * 0.05f;
        final float y = size.y * 0.5f;

        TouchUtils.drag(test, fromX, toX, y, y, 1);
    }

    /**
     * Drag the screen down with a quarter distance right at right quarter
     * screen
     */
    public static void dragToRight(InstrumentationTestCase test, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        final android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);

        final float fromX = size.x * 0.95f;
        final float toX = size.x * 0.05f;
        final float y = size.y * 0.5f;

        TouchUtils.drag(test, fromX, toX, y, y, 1);
    }

    public static void dragViewToLeft(InstrumentationTestCase test, View view) {
        int[] corners = new int[2];
        view.getLocationOnScreen(corners);
        int viewHeight = view.getHeight();
        int viewWidth = view.getWidth();
        float x = corners[0] + viewWidth * 0.95f;
        float y = corners[1] + viewHeight / 2.0f;
        TouchUtils.drag(test, corners[0], x, y, y, 2);
    }

    public static void dragViewToRight(InstrumentationTestCase test, View view) {
        int[] corners = new int[2];
        view.getLocationOnScreen(corners);
        int viewHeight = view.getHeight();
        int viewWidth = view.getWidth();
        float x = corners[0] + viewWidth * 0.95f;
        float y = corners[1] + viewHeight / 2.0f;
        TouchUtils.drag(test, x, corners[0], y, y, 2);
    }
}
