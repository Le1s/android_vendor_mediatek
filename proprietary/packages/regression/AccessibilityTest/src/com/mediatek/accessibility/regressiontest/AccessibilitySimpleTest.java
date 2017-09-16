/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2011. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.accessibility.regressiontest;

import com.android.settings.R;
import com.android.settings.Settings.AccessibilitySettingsActivity;
import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Instrumentation;
import android.app.Activity;
import android.provider.Settings;
import android.widget.Switch;
import com.android.settings.widget.SwitchBar;
import com.android.settings.SettingsActivity;

import java.util.List;

public class AccessibilitySimpleTest extends ActivityInstrumentationTestCase2<AccessibilitySettingsActivity> {

    private static final String TAG = "AccessibilitySimpleTest";
    private static final int OPERATION_DURATION = 3000;
    private static final int TOUCH_EXPLORE_TUTORIAL_WAIT_DURATION = 15000;
    private Solo mSolo;
    private Activity mActivity;
    private Context mContext;
    private Instrumentation mIns;
    private ContentResolver mCr;

    public AccessibilitySimpleTest() {
        super("com.android.settings", AccessibilitySettingsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mIns = getInstrumentation();
        mContext = mIns.getTargetContext();
        mActivity = getActivity();
        mSolo = new Solo(mIns, mActivity);
        mCr = mContext.getContentResolver();
    }

    // test the precondition
    public void test01_Precondition() {
        assertNotNull(mIns);
        assertNotNull(mContext);
        assertNotNull(mActivity);
        assertNotNull(mSolo);
        assertNotNull(mCr);
    }

    // Test enable/disable for magnifications gestures
    public void test04_magnification_gesture() {
        //*-----------------Define variables--------------------*//
        String title = mActivity.getString(R.string.accessibility_screen_magnification_title);
        boolean isEnabled = false;
        String switchStatus, shortcutStatus;
        //*-----------------------------------------------------*//
        Log.d(TAG, "Wait for 'Magnification gesture' text to appear+");
        mSolo.waitForText(title);
        Log.d(TAG, "Wait for 'Magnification gesture' text to appear-");
        mSolo.clickOnText(title);
        mSolo.sleep(OPERATION_DURATION);
        Log.d(TAG, "Wait for SwitchBar+");
        mSolo.waitForView(SwitchBar.class);
        Log.d(TAG, "Wait for SwitchBar-");
        SettingsActivity curActivity = (SettingsActivity) mSolo.getCurrentActivity();
        Switch switchView = curActivity.getSwitchBar().getSwitch();
        Log.d(TAG, "Magnification get the switch from switchbar, is " + switchView);
        //*-----------------Test enable start--------------------*//
        // Test for enable magnification gesture
        if (switchView != null) {
            mSolo.clickOnView(switchView);
            mSolo.sleep(OPERATION_DURATION);
            isEnabled = switchView.isChecked();
            switchStatus = isEnabled ? "on" : "off";
            Log.d(TAG, "Magnification switch is " + switchStatus);
        }
        boolean actual = (Settings.Secure.getInt(mCr, Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, 0) == 1);
        shortcutStatus = actual ? "on" : "off";
        Log.d(TAG, "Magnification actual is " + shortcutStatus);
        assertEquals(isEnabled, actual);
        //*-----------------Test enable end---------------------*//
        mSolo.sleep(OPERATION_DURATION);
        //*----------------Test disable start-------------------*//
        // Test for disable magnification gesture
        if (switchView != null) {
            mSolo.clickOnView(switchView);
            mSolo.sleep(OPERATION_DURATION);
            isEnabled = switchView.isChecked();
            switchStatus = isEnabled ? "on" : "off";
            Log.d(TAG, "Magnification switch is " + switchStatus);
            mSolo.goBack();
        }
        actual = (Settings.Secure.getInt(mCr, Settings.Secure.ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, 0) == 1);
        shortcutStatus = actual ? "on" : "off";
        Log.d(TAG, "Magnification actual is " + shortcutStatus);
        assertEquals(isEnabled, actual);
        //*-----------------Test disable end--------------------*//
    }

    // Test enable/disable for accessibility shortcut
    public void test03_accessibility_shortcut() {
        //*-----------------Define variables--------------------*//
        String title = mActivity.getString(R.string.accessibility_global_gesture_preference_title);
        boolean isEnabled = false;
        String switchStatus, shortcutStatus;
        //*-----------------------------------------------------*//
        Log.d(TAG, "Wait for 'Accessibility shotcut' text to appear+");
        mSolo.waitForText(title);
        Log.d(TAG, "Wait for 'Accessibility shotcut' text to appear-");
        mSolo.clickOnText(title);
        mSolo.sleep(OPERATION_DURATION);
        Log.d(TAG, "Wait for SwitchBar+");
        mSolo.waitForView(SwitchBar.class);
        Log.d(TAG, "Wait for SwitchBar-");
        SettingsActivity curActivity = (SettingsActivity) mSolo.getCurrentActivity();
        Switch switchView = curActivity.getSwitchBar().getSwitch();
        Log.d(TAG, "Shortcut get the switch from switchbar, is " + switchView);
        //*-----------------Test enable start--------------------*//
        // Test for enable accessibility shortcut
        if (switchView != null) {
            mSolo.clickOnView(switchView);
            mSolo.sleep(OPERATION_DURATION);
            isEnabled = switchView.isChecked();
            switchStatus = isEnabled ? "on" : "off";
            Log.d(TAG, "Shortcut feature is " + switchStatus);
        }
        boolean actual = (Settings.Global.getInt(mCr, Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0) == 1);
        shortcutStatus = actual ? "on" : "off";
        Log.d(TAG, "Shortcut actual is " + shortcutStatus);
        assertEquals(isEnabled, actual);
        //*-----------------Test enable end---------------------*//
        mSolo.sleep(OPERATION_DURATION);
        //*----------------Test disable start-------------------*//
        // Test for disable accessibility shortcut
        if (switchView != null) {
            mSolo.clickOnView(switchView);
            mSolo.sleep(OPERATION_DURATION);
            isEnabled = switchView.isChecked();
            switchStatus = isEnabled ? "on" : "off";
            Log.d(TAG, "Shortcut feature is " + switchStatus);
            mSolo.goBack();
        }
        actual = (Settings.Global.getInt(mCr, Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0) == 1);
        shortcutStatus = actual ? "on" : "off";
        Log.d(TAG, "Shortcut actual is " + shortcutStatus);
        assertEquals(isEnabled, actual);
        //*-----------------Test disable end--------------------*//
    }

    // Test enable/disable for talkback
    public void test02_talkback() {
        // Ensure the activity is on screen top
        mSolo.scrollToTop();
        //*-----------------Define variables--------------------*//
        String title = "TalkBack";
        String switchStatus;
        boolean isEnabled = false;
        boolean isTalkBackEnable = false;
        SettingsActivity curActivity = null;
        List<AccessibilityServiceInfo> enableServices = null;
        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(mActivity);
        //*-----------------------------------------------------*//
        mSolo.sleep(OPERATION_DURATION);
        Log.d(TAG, "Wait for 'Talkback' text to appear+");
        mSolo.waitForText(title);
        Log.d(TAG, "Wait for 'Talkback' text to appear-");
        // Click "TalkBack" to enter talkback settings page
        mSolo.clickOnText(title);
        mSolo.sleep(OPERATION_DURATION);
        Log.d(TAG, "Wait for SwitchBar+");
        mSolo.waitForView(SwitchBar.class);
        Log.d(TAG, "Wait for SwitchBar-");
        // Get switch from SettingActivity
        curActivity = (SettingsActivity) mSolo.getCurrentActivity();
        Switch switchView = curActivity.getSwitchBar().getSwitch();
        Log.d(TAG, "Talkback get the switch from switchbar, is " + switchView);
        // Start testing if switchView is not null
        if (switchView != null) {
            //*-----------------Test enable start--------------------*//
            mSolo.clickOnView(switchView);
            mSolo.sleep(OPERATION_DURATION);
            // Confirm dialog of using talkback
            Log.d(TAG, "Expect that now is in the talkback setting dialog");
            Log.d(TAG, "Wait for dialog to open+");
            mSolo.waitForDialogToOpen();
            Log.d(TAG, "Wait for dialog to open-");
            mSolo.clickOnButton("OK");
            mSolo.sleep(OPERATION_DURATION);
            isEnabled = switchView.isChecked();
            switchStatus = isEnabled ? "on" : "off";
            Log.d(TAG, "Talkback feature is " + switchStatus);
            mSolo.sleep(OPERATION_DURATION);
            // Here we should make sure the status bwtween switch and database
            // are equalize or not.
            enableServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
            for (int i = 0; i < enableServices.size(); i++) {
                String enableService = enableServices.get(i).getResolveInfo().serviceInfo.name;
                Log.d(TAG, "Enable service = " + enableService);
                if (!isTalkBackEnable && enableService.contentEquals("com.google.android.marvin.talkback.TalkBackService")) {
                    isTalkBackEnable = true;
                    Log.d(TAG, "Talkback now is enable!!!!!!!!!!!!!!!!!!!!!");
                    break;
                }
            }
            assertEquals(isEnabled, isTalkBackEnable);
            Log.d(TAG, "PASS talkback enable case");
            //*-----------------Test enable end---------------------*//
            mSolo.sleep(TOUCH_EXPLORE_TUTORIAL_WAIT_DURATION);
            // Assume that the current screen shows explore by touch tutorial
            // So we need to dismiss the dialog
            Log.d(TAG, "Wait for Exit text to appear+");
            mSolo.waitForText("Exit");
            Log.d(TAG, "Wait for Exit text to appear-");
            mSolo.goBack();
            mSolo.sleep(OPERATION_DURATION);
            Log.d(TAG, "Wait for SwitchBar+");
            mSolo.waitForView(SwitchBar.class);
            Log.d(TAG, "Wait for SwitchBar-");
            //*----------------Test disable start-------------------*//
            mSolo.clickOnView(switchView);
            mSolo.sleep(OPERATION_DURATION);
            mSolo.clickOnButton("OK");
            // Check the status of switch button
            mSolo.sleep(OPERATION_DURATION);
            isEnabled = switchView.isChecked();
            switchStatus = isEnabled ? "on" : "off";
            Log.d(TAG, "Talkback feature is " + switchStatus);
            mSolo.sleep(OPERATION_DURATION);
            // Here we should make sure the status bwtween switch and database
            // are equalize or not.
            enableServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
            for (int i = 0; i < enableServices.size(); i++) {
                String enableService = enableServices.get(i).getResolveInfo().serviceInfo.name;
                Log.d(TAG, "Enable service = " + enableService);
                if (enableService.contentEquals("com.google.android.marvin.talkback.TalkBackService")) {
                    isTalkBackEnable = true;
                    Log.e(TAG, "WARNING: Talkback now should be disable");
                    break;
                }
            }
            if (enableServices.isEmpty()) {
                isTalkBackEnable = false;
                Log.d(TAG, "Talkback now is disable!!!!!!!!!!!!!!!!!!!");
            }
            assertEquals(isEnabled, isTalkBackEnable);
            Log.d(TAG, "PASS talkback disable case");
            //*-----------------Test disable end--------------------*//
            mSolo.goBack();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            mSolo.finishOpenedActivities();
            mActivity.finish();
        } catch (Exception e) {
            // ignore
        }
        super.tearDown();
    }
}
