/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2014. All rights reserved.
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
 */

package com.mediatek.videoplayback.sanitytest;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.android.gallery3d.R;
import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.CommonControllerOverlay;
import com.android.gallery3d.app.CommonControllerOverlay.State;
import com.android.gallery3d.app.GalleryActivity;
import com.android.gallery3d.app.MovieActivity;
import com.android.gallery3d.app.MovieControllerOverlay;
import com.android.gallery3d.app.MoviePlayer;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.mediatek.storage.StorageManagerEx;
import com.mediatek.videoplayback.sanitytest.Utils;

import com.jayway.android.robotium.solo.Solo;

/**
 * Test case for test videoplayback.
 */
public class VideoPlaybackTest extends ActivityInstrumentationTestCase2<GalleryActivity> {
    
    private static final String TAG = "VideoPlaybackSanityTest/VideoPlaybackTest";
    private static final String TEST_FILE = "SanityTest";
    private static final String VIDEO_MP4_SUFFIX = ".mp4";
    private static String STORAGE_PATH = "sdcard0";
    private static final String INSTANCE_PLAYER = "mPlayer";
    private static final String INSTANCE_CONTROLLER = "mController";
    private static final String INSTANCE_STATE = "mState";
    private static final String CHOOSE_ALWAYS = "Always";
    private static final int SLEEP_TIME = 2000;
    private static final int PLAY_TIME = 61000;
    private GalleryActivity mGalleryActivity;
    private Solo mSolo;
    private Instrumentation mInstrumentation;
    private UiAutomation mUiAutomation;
    
    public VideoPlaybackTest() {
        super(GalleryActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Log.v(TAG, "<setUp> entry");
        mGalleryActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mUiAutomation = mInstrumentation.getUiAutomation();
        mSolo = new Solo(mInstrumentation, mGalleryActivity);
        checkEnv();
        // set video player to be the default player
        Utils.setDefaultPlayer(mGalleryActivity.getPackageManager());
    }
    
    @Override
    protected void tearDown() throws Exception {
        Log.v(TAG, " <tearDown> entry");
        mGalleryActivity.finish();
        super.tearDown();
    }
    
    /**
     * Test the video can be played normally
     * 
     * @throws Exception
     */
    public void test_video_play() throws Throwable {
        Log.v(TAG, "<test_video_play> test start");
        mSolo.sleep(SLEEP_TIME);
        // into test album
        Utils.openAlbumWhenAlbumSetPage(STORAGE_PATH,
                (AlbumSetPage) (mGalleryActivity.getStateManager().getTopState()),
                mGalleryActivity, mInstrumentation, mSolo);
        mSolo.sleep(SLEEP_TIME);
        // if AlbumPage, into PhotoPage
        if (mGalleryActivity.getStateManager().getTopState() instanceof AlbumPage) {
            Log.v(TAG, "<test_video_play> open AlbumPage and select the video to play");
            Utils.openPhotoWhenAlbumPage(TEST_FILE,
                    (AlbumPage) (mGalleryActivity.getStateManager().getTopState()),
                    mGalleryActivity, mInstrumentation, mSolo);
        } else {
            Log.v(TAG, "<test_video_play> only one video so open it directly");
            int screenHeight = mSolo.getCurrentActivity().getWindowManager().getDefaultDisplay()
                    .getHeight();
            int screenWidth = mSolo.getCurrentActivity().getWindowManager().getDefaultDisplay()
                    .getWidth();
            mSolo.clickOnScreen(screenWidth / 2, screenHeight / 2);
        }
        mSolo.sleep(2 * SLEEP_TIME);
        List<AccessibilityNodeInfo> chooseInfo = mUiAutomation.getRootInActiveWindow()
                .findAccessibilityNodeInfosByText(CHOOSE_ALWAYS);
        Log.v(TAG, "<test_video_play> chooseInfo is " + chooseInfo);
        if (!chooseInfo.isEmpty()) {
            Log.v(TAG, "<test_video_play> ResolverActivity pops up ");
            AccessibilityNodeInfo alwaysInfo = chooseInfo.get(0);
            alwaysInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        mSolo.sleep(SLEEP_TIME);
        checkVideoPlaying();
        Log.v(TAG, "<test_video_play> test end");
    }
    
    private void checkVideoPlaying() throws Throwable {
        Log.v(TAG, "<checkVideoPlaying> entry");
        long startTime = System.currentTimeMillis();
        Activity activity = mSolo.getCurrentActivity();
        assertTrue("<checkVideoPlaying> the current activity is " + activity,
                activity instanceof MovieActivity);
        MoviePlayer player = Utils.getMemberInObject((MovieActivity) activity, MoviePlayer.class,
                INSTANCE_PLAYER);
        MovieControllerOverlay controller = Utils.getMemberInObject(player,
                MovieControllerOverlay.class, INSTANCE_CONTROLLER);
        State state = Utils.getMemberInObject((CommonControllerOverlay) controller,
                CommonControllerOverlay.State.class, INSTANCE_STATE);
        do {
            if (System.currentTimeMillis() - startTime > 5 * SLEEP_TIME) {
                fail("<checkVideoPlaying> video is not playing with state is " + state);
            }
            mSolo.sleep(SLEEP_TIME / 10);
        } while (state != State.PLAYING);
        assertTrue("<checkVideoPlaying> video is not playing with state is " + state,
                (state == state.PLAYING));
        mSolo.sleep(PLAY_TIME);
        Log.v(TAG, "<checkVideoPlaying> exit");
    }
    
    private void checkEnv() {
        // get default storage path
        String defaultStoragePath = null;
        try {
            defaultStoragePath = StorageManagerEx.getDefaultPath();
            Log.v(TAG, "<checkEnv> with defaultStoragePath = " + defaultStoragePath);
        } catch (RuntimeException e) {
            Log.i(TAG, "<checkEnv> RuntimeException when StorageManagerEx.getDefaultPath()", e);
            File file = Environment.getExternalStorageDirectory();
            if (file != null) {
                defaultStoragePath = file.getAbsolutePath();
                Log.i(TAG, "<checkEnv> from Environment, defaultStoragePath = " + defaultStoragePath);
            } else {
                Log.i(TAG, "<checkEnv> Environment.getExternalStorageDirectory() == null");
            }
        }
        assertTrue("<checkEnv> invalide defaultStoragePath = " + defaultStoragePath,
                defaultStoragePath != null && !defaultStoragePath.equals(""));
        // check storage state
        StorageManager sm = (StorageManager) mGalleryActivity
                .getSystemService(Context.STORAGE_SERVICE);
        assertTrue("<checkEnv>  StorageManager is null", sm != null);
        String volumeState = sm.getVolumeState(defaultStoragePath);
        assertTrue("<checkEnv>  volumeState is " + volumeState,
                Environment.MEDIA_MOUNTED.equalsIgnoreCase(volumeState));
        Log.i(TAG, "<checkEnv>  check storage state, pass");
        if (defaultStoragePath != null) {
            STORAGE_PATH = defaultStoragePath.substring(defaultStoragePath.lastIndexOf("/") + 1,
                    defaultStoragePath.length());
        }
        Log.v(TAG, "<checkEnv> with STORAGE_PATH = " + STORAGE_PATH);
        String videoPath = new StringBuilder().append(defaultStoragePath).append("/")
                .append(TEST_FILE).append(VIDEO_MP4_SUFFIX).toString();
        File file = new File(videoPath);
        // check the test video file is exist in the storage
        assertTrue("<checkEnv> video " + videoPath + " is not exist in the storage", file.exists());
    }
}
