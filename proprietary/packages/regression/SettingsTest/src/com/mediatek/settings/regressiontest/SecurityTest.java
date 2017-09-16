package com.mediatek.settings.regressiontest;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.android.settings.Settings;
import com.jayway.android.robotium.solo.Solo;

public class SecurityTest extends
        ActivityInstrumentationTestCase2<Settings.SecuritySettingsActivity> {

    private final static String TAG = "SecurityTest";
    private final static int SLEEP_TIME = 1000;
    private Solo mSolo;
    private Activity mActivity;
    private final static String[] LOCKMODE = {"unlock_set_unlock_off_title", "unlock_set_unlock_none_title",
            "unlock_set_unlock_pattern_title", "unlock_set_unlock_pin_title", "unlock_set_unlock_password_title"};

    public SecurityTest() {
        super(Settings.SecuritySettingsActivity.class);
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
     * There is 5 kind of screen lock in device
     * @throws Exception
     */
    public void test01_Settings_079() throws Exception {
        Log.d(TAG, "test01_Settings_079");
        String screenLockTitle = TestUtils.getString(mActivity, "unlock_set_unlock_launch_picker_title");
        Log.d(TAG, "test01_Settings_079, screenLockTitle = " + screenLockTitle);
        if (mSolo.searchText(screenLockTitle)) {
            mSolo.clickOnText(screenLockTitle);
            mSolo.sleep(SLEEP_TIME);
            boolean result = true;
            for (int i = 0; i < LOCKMODE.length; i++) {
                String lockmode = TestUtils.getString(mActivity, LOCKMODE[i]);
                Log.d(TAG, "test01_Settings_079, lockmode = " + lockmode);
                result &= mSolo.searchText(lockmode);
            }

            assertTrue(result);

        }
    }
}
