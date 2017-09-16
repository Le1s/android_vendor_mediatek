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

package com.mediatek.settings.regressiontest;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.MotionEvent;

import com.android.settings.Settings;
import com.jayway.android.robotium.solo.Solo;

public class SettingsAppsTest extends
        ActivityInstrumentationTestCase2<Settings.ManageApplicationsActivity> {

    private static final String TAG = "SettingsAppsTest";
    private Solo mSolo;
    private Activity mActivity;
    private Instrumentation mIns;

    public SettingsAppsTest() {
        super(Settings.ManageApplicationsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mIns = getInstrumentation();
        mActivity = getActivity();
        mSolo = new Solo(mIns, mActivity);
        assertNotNull(mIns);
        assertNotNull(mActivity);
        assertNotNull(mSolo);
    }

    /*
     * 1. High Check Application filter
     *
     * [Step]1. Enter Settings 2. Enter Apps
     *
     * [Expected result] 1. All the apps can be filtered by 4 category:
     * Downloaded, Phone storage, Running, All 2. Can use OFN to highlight them
     * one by one
     *
     * [Notes] OFN in this screen should be checked"
     */
    public void test01_Settings_048() {
        for (int i = 0; i < 3; i++) {
            scrollToSide(Solo.RIGHT);
        }
        for (int i = 0; i < 3; i++) {
            scrollToSide(Solo.LEFT);
        }
    }

    /*
     * 1. High View downloaded apps "[Initial condition]
     *
     * [Step]1. Enter Settings 2. Enter Apps 3. Choose DOWNLOADED tab
     *
     * [Expected result]1. The downloaded apps will be listed in the middle
     * region. If there is no downloaded app , it will show ""No apps""
     */
    public void test02_Settings_049() {
        String downloadLabel = TestUtils.getString(mActivity, "filter_apps_third_party");
        if (mSolo.searchText(downloadLabel)) {
            Log.d(TAG, "download_Label: " + downloadLabel);
            mSolo.clickOnText(downloadLabel);
        }
    }

    /*
     * 1. High View Phone Storage apps "[Initial condition]
     *
     * [Step] 1. Enter Settings 2. Enter Apps 3. Choose PHONE STORAGE tab
     *
     * [Expected result] 1. The apps on Phone storage will be listed in the
     * middle region. If there is no app on Phone storage , it will show ""No
     * apps"".
     */
    public void test03_Settings_050() {
        String extStoragePath = Environment.getLegacyExternalStorageDirectory().getPath();
        String sdcardDes = getSdDesc(extStoragePath);
        scrollToSide(Solo.RIGHT);
        if (mSolo.searchText(sdcardDes)) {
            Log.d(TAG, "sdcardDes: " + sdcardDes);
            mSolo.clickOnText(sdcardDes);
        }
    }

    /*
     * 1. High View All apps "[Initial condition]
     *
     * [Step] 1. Enter Settings 2. Enter Apps 3. Scroll the tabs to left and
     * click the ""ALL"" tab
     *
     * [Expected result] 1. All the installed apps will be listed in the middle
     * region."
     */
    public void test04_Settings_051() {
        scrollToSide(Solo.RIGHT);
        scrollToSide(Solo.RIGHT);
        if (!Environment.isExternalStorageEmulated()
                && !(TestUtils.MTK_2SDCARD_SWAP && !TestUtils.isExSdcardInserted())) {
            scrollToSide(Solo.RIGHT);
        }
        String allLabel = TestUtils.getString(mActivity, "filter_apps_all");
        if (mSolo.searchText(allLabel)) {
            Log.d(TAG, "all_Label: " + allLabel);
            mSolo.clickOnText(allLabel);
        }
    }

    /*
     * 1. High View all running processes&services "[Initial condition]
     *
     * [Step] 1. Settings & Apps & RUNNING tab
     *
     * [Expected result] 1. All the running processes&services wil be listed in
     * the middle region."
     */
    public void test05_Settings_052() {
        scrollToSide(Solo.RIGHT);
        if (!Environment.isExternalStorageEmulated()
                && !(TestUtils.MTK_2SDCARD_SWAP && !TestUtils.isExSdcardInserted())) {
            scrollToSide(Solo.RIGHT);
        }
        String runningLabel = TestUtils.getString(mActivity, "filter_apps_running");
        if (mSolo.searchText(runningLabel)) {
            Log.d(TAG, "running_Label: " + runningLabel);
            mSolo.clickOnText(runningLabel);
        }
    }

    /*
     * 1. High Check the current tab after rotation "[Initial condition]
     * Auto-rotate is enabled
     *
     * [Step] 1. Enter Settings--& Apps--& ""Download"" tab(other tab is OK) 2.
     * Rotate device
     *
     * [Expected result] 1. The selected tab is not changed"
     */
    public void test06_Settings_058() {
        String downloadLabel = TestUtils.getString(mActivity, "filter_apps_third_party");
        if (mSolo.searchText(downloadLabel)) {
            Log.d(TAG, "download_Label: " + downloadLabel);
            mSolo.clickOnText(downloadLabel);
        }
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        assertTrue(mSolo.searchText(downloadLabel));
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            mSolo.finishOpenedActivities();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.tearDown();
    }

    private String getSdDesc(String path) {
        StorageManager storageManager = (StorageManager) getActivity().getSystemService(
                Context.STORAGE_SERVICE);
        StorageVolume[] volumes = storageManager.getVolumeList();
        int len = volumes.length;
        String sdDesc = "";
        for (int i = 0; i < len; i++) {
            if (volumes[i].getPath().equals(path)) {
                sdDesc = volumes[i].getDescription(getActivity());
                break;
            } else {
                sdDesc = "error";
            }
        }
        return sdDesc;
    }

    // re-write solo's api: scrollToSide
    private void scrollToSide(int side) {
        Log.d(TAG, "scrollToSide " + side);
        int screenHeight = mSolo.getCurrentActivity().getWindowManager().getDefaultDisplay()
                .getHeight();
        int screenWidth = mSolo.getCurrentActivity().getWindowManager().getDefaultDisplay()
                .getWidth();
        float x = screenWidth * 0.55f;
        float y = screenHeight / 2.0f;
        if (side == Solo.LEFT)
            drag(0, x, y, y, 15);
        else if (side == Solo.RIGHT)
            drag(x, 0, y, y, 15);
    }

    private void drag(float fromX, float toX, float fromY, float toY, int stepCount) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        float y = fromY;
        float x = fromX;
        float yStep = (toY - fromY) / stepCount;
        float xStep = (toX - fromX) / stepCount;
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, fromX,
                fromY, 0);
        try {
            mIns.sendPointerSync(event);
        } catch (SecurityException ignored) {
        }
        for (int i = 0; i < stepCount; ++i) {
            y += yStep;
            x += xStep;
            eventTime = SystemClock.uptimeMillis();
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, 0);
            try {
                mIns.sendPointerSync(event);
            } catch (SecurityException ignored) {
            }
        }
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, toX, toY, 0);
        try {
            mIns.sendPointerSync(event);
        } catch (SecurityException ignored) {
        }
    }
}
