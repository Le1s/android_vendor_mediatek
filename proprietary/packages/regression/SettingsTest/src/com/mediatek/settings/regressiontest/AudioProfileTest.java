package com.mediatek.settings.regressiontest;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.android.settings.Settings;
import com.jayway.android.robotium.solo.Solo;

public class AudioProfileTest extends
        ActivityInstrumentationTestCase2<Settings.AudioProfileSettingsActivity> {

    private final static String TAG = "AudioProfileTest";
    private final static int SLEEP_TIME = 1000;
    private Solo mSolo;
    private Activity mActivity;

    public AudioProfileTest() {
        super(com.android.settings.Settings.AudioProfileSettingsActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        mActivity = getActivity();
        mSolo = new Solo(getInstrumentation(), mActivity);
        assertNotNull(mActivity);
        assertNotNull(mSolo);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * AudioProfileSettings
     * @throws Exception
     */
    public void test01_AudioProfileSettings_001() throws Exception {
        Log.d(TAG, "test01_AudioProfileSettings_001");
        String audioProfileSettingsTitle = TestUtils.getString(mActivity, "notification_settings");
        String generalSettingsTitle = TestUtils.getString(mActivity, "audio_profile_category_predefine_title");
        if (mSolo.searchText(audioProfileSettingsTitle)) {
            mSolo.clickOnText(audioProfileSettingsTitle);
            mSolo.sleep(SLEEP_TIME);
            if (mSolo.searchText(audioProfileSettingsTitle)) {
                mSolo.clickOnText(audioProfileSettingsTitle);
                mSolo.sleep(SLEEP_TIME);
                assertTrue(mSolo.searchText(generalSettingsTitle));
            }

        }
    }

}
