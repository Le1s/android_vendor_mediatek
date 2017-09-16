package com.mediatek.settings.regressiontest;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.android.settings.Settings;
import com.jayway.android.robotium.solo.Solo;

public class FactoryResetTest extends
        ActivityInstrumentationTestCase2<Settings.PrivacySettingsActivity> {

    private final static String TAG = "FactoryResetTest";
    private final static int SLEEP_TIME = 1000;
    private Solo mSolo;
    private Activity mActivity;

    public FactoryResetTest() {
        super(Settings.PrivacySettingsActivity.class);
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
     * DRM reset
     * @throws Exception
     */
    public void test01_Settings_136() throws Exception {
        Log.d(TAG, "test01_Settings_136");
        String drmSettingsTitle = TestUtils.getString(mActivity, "drm_settings_title");
        if (mSolo.searchText(drmSettingsTitle)) {
            mSolo.clickOnText(drmSettingsTitle);
            mSolo.sleep(SLEEP_TIME);
            if (mSolo.searchText(drmSettingsTitle)) {
                mSolo.clickOnText(drmSettingsTitle);
                mSolo.sleep(SLEEP_TIME);
                if (mSolo.searchButton(mActivity.getString(android.R.string.ok))) {
                    mSolo.clickOnButton(mActivity.getString(android.R.string.ok));
                    String drmResetToast = TestUtils.getString(mActivity, "drm_reset_toast_msg");
                    assertTrue(mSolo.searchText(drmResetToast));
                }
            }

        }
    }

}
