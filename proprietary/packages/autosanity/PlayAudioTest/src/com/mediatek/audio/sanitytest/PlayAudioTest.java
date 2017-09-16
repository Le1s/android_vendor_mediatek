/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.mediatek.audio.sanitytest;


import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;

import com.android.music.*;
import com.jayway.android.robotium.solo.Solo;

/**
 * Junit / Instrumentation test case for the TrackBrowserActivity
 */
public class PlayAudioTest extends ActivityInstrumentationTestCase2<MediaPlaybackActivity>
        implements ServiceConnection {

    private static final String TAG = "PlayAudioTest";

    private static final int WAIT_FOR_ACTIVITY_CREATED_TIME = 10000;
    private static final int WAIT_FOR_RESPOND_TIME = 500;
    private static final int WAIT_FOR_DELETED_TIME = 1500;
    private static final int WAIT_FOR_SERVICE_CONNECTED_TIME = 5000;
    private static final int WAIT_FOR_JUMPING_TO_PREV_TIME = 3000;
    private static final int MINIMUM_SONGS_COUNT_IN_MUSIC = 3;
    private static final int LONG_CLICK_TIME = 5000;

    private Instrumentation mInstrumentation = null;
    private MediaPlaybackActivity mMediaPlaybackActivity = null;
    private Solo mSolo = null;
    private Context mContext = null;
    private IMediaPlaybackService mService = null;
    private Object mLock = new Object();
    private boolean mIsMusicServiceConnected = false;

    public PlayAudioTest() {
        super(MediaPlaybackActivity.class);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected");
        mService = IMediaPlaybackService.Stub.asInterface(service);
        mIsMusicServiceConnected = true;
        synchronized (mLock) {
            mLock.notify();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected");
        mService = null;
        mIsMusicServiceConnected = false;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Log.d(TAG, "setUp>>>");
        setActivityInitialTouchMode(true);
        mInstrumentation = getInstrumentation();

        // Bind service
        Intent iService = new Intent(mInstrumentation.getTargetContext(),
                MediaPlaybackService.class);
        mInstrumentation.getContext().bindService(iService, this, Context.BIND_AUTO_CREATE);
        if (!mIsMusicServiceConnected) {
            synchronized (mLock) {
                while (!mIsMusicServiceConnected) {
                    mLock.wait(WAIT_FOR_SERVICE_CONNECTED_TIME);
                }
            }
        }

        // ready the service for playback and other functions
        if (mService != null) {
            long[] audioIds = MusicUtils.getAllSongs(mInstrumentation.getContext());
            mService.open(audioIds, 0);
        }
        mInstrumentation.waitForIdleSync();
        // start activity until the service has started
        // so that the activity will not be finished once it starts at
        // updateTrackInfo.
        mMediaPlaybackActivity = getActivity();
        mContext = mMediaPlaybackActivity.getApplicationContext();
        mSolo = new Solo(mInstrumentation, mMediaPlaybackActivity);

        // Assert all used to be not null
        assertNotNull(mInstrumentation);
        assertNotNull(mMediaPlaybackActivity);
        assertNotNull(mContext);
        assertNotNull(mSolo);
        assertNotNull(mService);
        Log.d(TAG, "setUp<<<");
    }

    @Override
    protected void tearDown() throws Exception {
        Log.d(TAG, "tearDown>>>");

        mSolo.finishOpenedActivities();
        try {
            mSolo.finalize();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Thread.sleep(MusicTestUtils.WAIT_TEAR_DONW_FINISH_TIME);
        Log.d(TAG, "tearDown<<<");
        super.tearDown();
    }

    public void test01_PlayPauseSwitching() throws Throwable {
        Log.d(TAG, ">> test01_PlayPauseSwitching");

        // Pause unnecessary playback
        mService.pause();
        mInstrumentation.waitForIdleSync();

        // Click play button and check the playback status
        final View buttonPlayPause = mSolo.getView(mMediaPlaybackActivity.getResources()
                .getIdentifier("pause", "id", "com.android.music"));
        mSolo.clickOnView(buttonPlayPause);
        assertTrue(MusicTestUtils.isPlaying(mService));

        // Click pause again to pause playback and check the playback status
        mSolo.clickOnView(buttonPlayPause);
        assertTrue(MusicTestUtils.isStopping(mService));

        Log.d(TAG, "<< test01_PlayPauseSwitching");
    }

    public void test02_ForwardAndRewind() throws Exception {
        Log.d(TAG, ">> test02_ForwardAndRewind");
        mService.play();
        assertTrue(MusicTestUtils.isPlaying(mService));

        // test forward and rewind button, the track duration must bigger than 100000ms
        if (mService.duration() < 100000) {
            return;
        }
        mService.seek(10000);
        mInstrumentation.waitForIdleSync();
        assertTrue(mService.position() >= 10000);
        assertTrue(MusicTestUtils.isPlaying(mService));

        mService.seek(50000);
        mInstrumentation.waitForIdleSync();
        assertTrue(mService.position() >= 50000); 
        assertTrue(MusicTestUtils.isPlaying(mService));

        mService.seek(10000);
        mInstrumentation.waitForIdleSync();
        assertTrue(mService.position() >= 10000);
        assertTrue(MusicTestUtils.isPlaying(mService));

        Log.d(TAG, "<< test02_ForwardAndRewind");
    }

}
