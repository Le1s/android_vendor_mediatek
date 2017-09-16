package com.mediatek.settings.regressiontest;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.app.Activity;
import android.provider.Settings.SettingNotFoundException;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.android.settings.Settings;
import com.jayway.android.robotium.solo.Solo;

public class DisplayTest extends
        ActivityInstrumentationTestCase2<Settings.DisplaySettingsActivity> {

    private final static String TAG = "DisplayTest";
    private final static int SLEEP_TIME = 1000;
    private Solo mSolo;
    private Activity mActivity;

    public DisplayTest() {
        super(Settings.DisplaySettingsActivity.class);
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
     * Set Brightness as Auto
     * @throws Exception
     */
    public void test01_Settings_036() throws Exception {
        Log.d(TAG, "test01_Settings_036");
        String autoBrightnessTitle = TestUtils.getString(mActivity, "auto_brightness_title");
        int manual = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        int automatic = android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        if (getBrightnessMode() == manual) {
            if (mSolo.searchText(autoBrightnessTitle)) {
                mSolo.clickOnText(autoBrightnessTitle);
                mSolo.sleep(SLEEP_TIME * 2);
            }
        }

        assertTrue((getBrightnessMode() == automatic));
    }

    /**
     * Check font Settings
     * @throws Exception
     */
    public void test02_Settings_042() throws Exception {
        Log.d(TAG, "test02_Settings_042");
        String fontSizeTitle = TestUtils.getString(mActivity, "dialog_title_font_size");
        String[] fontSizeEntryValues = TestUtils.getStringArray(mActivity, "entries_font_size");
        Log.d(TAG, "test02_Settings_042, fontSizeTitle = " + fontSizeTitle);
        Log.d(TAG, "test02_Settings_042, fontSizeEntryValues = " + fontSizeEntryValues);
        if (mSolo.searchText(fontSizeTitle)) {
            mSolo.clickOnText(fontSizeTitle);
            mSolo.sleep(SLEEP_TIME);
            boolean result = true;
            for (int i = 0; i < fontSizeEntryValues.length; i++) {
                result &= mSolo.searchText(fontSizeEntryValues[i]);
            }

            assertTrue(result);

        }
    }

    /**
     * Change font Settings
     * @throws Exception
     */
    public void test03_Settings_043() throws Exception {
        Log.d(TAG, "test03_Settings_043");
        String fontSizeTitle = TestUtils.getString(mActivity, "dialog_title_font_size");
        String[] fontSizeEntryValues = TestUtils.getStringArray(mActivity, "entries_font_size");
        Log.d(TAG, "test03_Settings_043, fontSizeTitle = " + fontSizeTitle);
        Log.d(TAG, "test03_Settings_043, fontSizeEntryValues = " + fontSizeEntryValues);
        if (mSolo.searchText(fontSizeTitle)) {
            mSolo.clickOnText(fontSizeTitle);
            mSolo.sleep(SLEEP_TIME);
            if (mSolo.searchText(fontSizeEntryValues[0])) {
                mSolo.clickOnText(fontSizeEntryValues[0]);
                mSolo.sleep(SLEEP_TIME * 2);
            }

        }
    }

    /**
     * Check sleep settings
     * @throws Exception
     */
    public void test04_Settings_045() throws Exception {
        Log.d(TAG, "test04_Settings_045");
        String timeoutTitle = TestUtils.getString(mActivity, "screen_timeout");
        String[] timeoutEntryValues = TestUtils.getStringArray(mActivity, "screen_timeout_entries");
        Log.d(TAG, "test04_Settings_045, timeoutTitle = " + timeoutTitle);
        Log.d(TAG, "test04_Settings_045, timeoutEntryValues = " + timeoutEntryValues);

        if ("OP02".equals(android.os.SystemProperties.get("ro.operator.optr"))) {
            Log.d(TAG , "op02 load, we should return");
            return;
        }

        if (mSolo.searchText(timeoutTitle)) {
            mSolo.clickOnText(timeoutTitle);
            mSolo.sleep(SLEEP_TIME);
            boolean result = true;
            for (int i = 0; i < timeoutEntryValues.length; i++) {
                result &= mSolo.searchText(timeoutEntryValues[i]);
            }

            assertTrue(result);

        }
    }

    /**
     * Set sleep
     * @throws Exception
     */
    public void test05_Settings_046() throws Exception {
        Log.d(TAG, "test05_Settings_046");
        String timeoutTitle = TestUtils.getString(mActivity, "screen_timeout");
        String[] timeoutEntryValues = TestUtils.getStringArray(mActivity, "screen_timeout_entries");
        String[] timeoutValues = TestUtils.getStringArray(mActivity, "screen_timeout_values");
        Log.d(TAG, "test05_Settings_046, timeoutTitle = " + timeoutTitle);
        Log.d(TAG, "test05_Settings_046, timeoutEntryValues = " + timeoutEntryValues);
        Log.d(TAG, "test05_Settings_046, timeoutValues = " + timeoutValues);

        if ("OP02".equals(android.os.SystemProperties.get("ro.operator.optr"))) {
            Log.d(TAG , "op02 load, we should return");
            return;
        }

        if (mSolo.searchText(timeoutTitle)) {
            mSolo.clickOnText(timeoutTitle);
            mSolo.sleep(SLEEP_TIME);
            if (mSolo.searchText(timeoutEntryValues[0])) {
                mSolo.clickOnText(timeoutEntryValues[0]);
                mSolo.sleep(SLEEP_TIME * 2);
                Integer value = android.provider.Settings.System.getInt(mActivity.getContentResolver(),
                        SCREEN_OFF_TIMEOUT, 30000);
                Log.d(TAG, "test05_Settings_046, value = " + value);

                assertEquals(Integer.valueOf(timeoutValues[0]), value);
            }


        }
    }

    private int getBrightnessMode() {
        int mode = 0;
        try {
            mode = android.provider.Settings.System.getInt(mActivity.getContentResolver(),
                                        android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getBrightnessMode, mode = " + mode);
        return mode;
    }

    private void setBrightnessMode(int mode) {
        Log.d(TAG, "setBrightnessMode, mode = " + mode);
        android.provider.Settings.System.putInt(mActivity.getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, mode);

    }

}
