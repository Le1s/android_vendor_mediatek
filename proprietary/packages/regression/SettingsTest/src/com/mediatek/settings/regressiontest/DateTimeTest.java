package com.mediatek.settings.regressiontest;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.provider.Settings;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.jayway.android.robotium.solo.Solo;

public class DateTimeTest extends
        ActivityInstrumentationTestCase2<com.android.settings.Settings.DateTimeSettingsActivity> {

    private final static String TAG = "DateTimeTest";
    private final static int SLEEP_TIME = 1000;
    private final static int TEST_YEAR = 2012;
    private static int TEST_MONTH = 01;
    private final static int TEST_DAY = 10;
    private final static int TEST_HOUR = 1;
    private final static int TEST_MIN = 1;
    private Solo mSolo;
    private Activity mActivity;

    public DateTimeTest() {
        super(com.android.settings.Settings.DateTimeSettingsActivity.class);
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
     * Check automatic date & time
     * @throws Exception
     */
    public void test01_Settings_143() throws Exception {
        Log.d(TAG, "test01_Settings_143");
        String autoDateTimeTitle = TestUtils.getString(mActivity, "date_time_auto");
        String[] autoDateTimeEntry = TestUtils.getStringArray(mActivity, "auto_time_entries");
        Log.d(TAG, "test01_Settings_143, autoDateTimeTitle = " + autoDateTimeTitle);
        Log.d(TAG, "test01_Settings_143, autoDateTimeEntry = " + autoDateTimeEntry);
        if (mSolo.searchText(autoDateTimeTitle)) {
            mSolo.clickOnText(autoDateTimeTitle);
            mSolo.sleep(SLEEP_TIME);
            boolean result = true;
            for (int i = 0; i < autoDateTimeEntry.length; i++) {
                result &= mSolo.searchText(autoDateTimeEntry[i]);
            }

            assertTrue(result);

        }
    }

    /**
     * Enable Use set work-provided time
     * @throws Exception
     */
    public void test02_Settings_144() throws Exception {
        Log.d(TAG, "test02_Settings_144");
        String autoDateTimeTitle = TestUtils.getString(mActivity, "date_time_auto");
        String[] autoDateTimeEntry = TestUtils.getStringArray(mActivity, "auto_time_entries");
        Log.d(TAG, "test02_Settings_144, autoDateTimeTitle = " + autoDateTimeTitle);
        Log.d(TAG, "test02_Settings_144, autoDateTimeEntry = " + autoDateTimeEntry);
        if (mSolo.searchText(autoDateTimeTitle)) {
            mSolo.clickOnText(autoDateTimeTitle);
            mSolo.sleep(SLEEP_TIME);
            if (mSolo.searchText(autoDateTimeEntry[0])) {
                mSolo.clickOnText(autoDateTimeEntry[0]);
                mSolo.sleep(SLEEP_TIME * 2);

                assertEquals(1, Settings.Global.getInt(mActivity.getContentResolver(), Settings.Global.AUTO_TIME, 0));
            }
        }

    }

    /**
     * Set automatic date & time as off
     * @throws Exception
     */
    public void test03_Settings_145() throws Exception {
        Log.d(TAG, "test03_Settings_145");
        disableAutoDateTime();
        assertEquals(0, Settings.Global.getInt(mActivity.getContentResolver(), Settings.Global.AUTO_TIME, 0));
        assertEquals(0, Settings.Global.getInt(mActivity.getContentResolver(), Settings.System.AUTO_TIME_GPS, 0));
    }

    /**
     * Enable/Disable automatic time zone
     * @throws Exception
     */
    public void test04_Settings_146() throws Exception {
        Log.d(TAG, "test04_Settings_146");
        setAutoTimeZone(true);
        assertEquals(1, Settings.Global.getInt(mActivity.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0));
        setAutoTimeZone(false);
        assertEquals(0, Settings.Global.getInt(mActivity.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0));
    }

    /**
     * Set date manually
     * @throws Exception
     */
    public void test05_Settings_147() throws Exception {
        Log.d(TAG, "test05_Settings_147");
        disableAutoDateTime();
        String dateTimeSetTitle = TestUtils.getString(mActivity, "date_time_set_date");
        Log.d(TAG, "test05_Settings_147, dateTimeSetTitle = " + dateTimeSetTitle);
        if (mSolo.searchText(dateTimeSetTitle)) {
            mSolo.clickOnText(dateTimeSetTitle);
            mSolo.sleep(SLEEP_TIME);
            ArrayList<DatePicker> list = mSolo.getCurrentViews(DatePicker.class);
            if (list.size() != 1) return;
            DatePicker picker = list.get(0);
            Calendar dummyDate = Calendar.getInstance();
            TEST_MONTH = dummyDate.get(Calendar.MONDAY);
            Log.d(TAG, "test05_Settings_147, TEST_MONTH = " + TEST_MONTH);
            mSolo.setDatePicker(picker, TEST_YEAR, TEST_MONTH, TEST_DAY);

            if (mSolo.searchText(mActivity.getString(android.R.string.ok))) {
                mSolo.clickOnButton(mActivity.getString(android.R.string.ok));
                mSolo.sleep(SLEEP_TIME * 2);
                assertTrue(isRightDate(TEST_YEAR, TEST_MONTH, TEST_DAY));
            }
        }

    }

    /**
     * Set time manually
     * @throws Exception
     */
    public void test06_Settings_148() throws Exception {
        Log.d(TAG, "test06_Settings_148");
        disableAutoDateTime();
        String dateTimeSetTitle = TestUtils.getString(mActivity, "date_time_set_time");
        Log.d(TAG, "test06_Settings_148, dateTimeSetTitle = " + dateTimeSetTitle);
        if (mSolo.searchText(dateTimeSetTitle)) {
            mSolo.clickOnText(dateTimeSetTitle);
            mSolo.sleep(SLEEP_TIME);
            ArrayList<TimePicker> list = mSolo.getCurrentViews(TimePicker.class);
            if (list.size() != 1) return;
            TimePicker picker = list.get(0);
            mSolo.setTimePicker(picker, TEST_HOUR, TEST_MIN);

            if (mSolo.searchText(mActivity.getString(android.R.string.ok))) {
                mSolo.clickOnButton(mActivity.getString(android.R.string.ok));
                mSolo.sleep(SLEEP_TIME * 2);
                assertTrue(isRightTime(TEST_HOUR, TEST_MIN));
            }
        }
    }

    /**
     * Set time zone manually
     * @throws Exception
     */
    public void test07_Settings_149() throws Exception {
        Log.d(TAG, "test07_Settings_149");
        setAutoTimeZone(false);
        String setTimeZoneTitle = TestUtils.getString(mActivity, "date_time_set_timezone");
        if (mSolo.searchText(setTimeZoneTitle)) {
            mSolo.clickOnText(setTimeZoneTitle);
            mSolo.sleep(SLEEP_TIME);
            selectItem(2);
        }
    }

    private void selectItem(int i) {

        int num = -1;
        while (num++ < i) {
            Log.d(TAG, "selectItem, DOWN");
            mSolo.sendKey(Solo.DOWN);
            mSolo.sleep(SLEEP_TIME);
        }
        mSolo.sendKey(Solo.ENTER);
    }

    private void disableAutoDateTime() {
        Log.d(TAG, "disableAutoDateTime");
        String autoDateTimeTitle = TestUtils.getString(mActivity, "date_time_auto");
        String[] autoDateTimeEntry = TestUtils.getStringArray(mActivity, "auto_time_entries");
        Log.d(TAG, "disableAutoDateTime, autoDateTimeTitle = " + autoDateTimeTitle);
        Log.d(TAG, "disableAutoDateTime, autoDateTimeEntry = " + autoDateTimeEntry);
        if (mSolo.searchText(autoDateTimeTitle)) {
            mSolo.clickOnText(autoDateTimeTitle);
            mSolo.sleep(SLEEP_TIME);
            if (autoDateTimeEntry.length >= 3 && mSolo.searchText(autoDateTimeEntry[2])) {
                mSolo.clickOnText(autoDateTimeEntry[2]);
                mSolo.sleep(SLEEP_TIME * 2);
            }
        }

    }

    private void setAutoTimeZone(boolean enable) {
        int autoTimeZone = Settings.Global.getInt(mActivity.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
        Log.d(TAG, "setAutoTimeZone, enable = " + enable);
        Log.d(TAG, "setAutoTimeZone, autoTimeZone = " + autoTimeZone);
        if (enable ^ (autoTimeZone == 1)) {
            String autoTimeZoneTitle = TestUtils.getString(mActivity, "zone_auto");
            Log.d(TAG, "setAutoTimeZone, autoTimeZoneTitle = " + autoTimeZoneTitle);
            if (mSolo.searchText(autoTimeZoneTitle)) {
                mSolo.clickOnText(autoTimeZoneTitle);
                mSolo.sleep(SLEEP_TIME * 2);
            }

        }

    }

    private boolean isRightDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) == month &&
                calendar.get(Calendar.DAY_OF_MONTH) == day)
            return true;
            else
                return false;
    }

    private boolean isRightTime(int hour, int min) {
        Calendar c = Calendar.getInstance();
        if (hour == c.get(Calendar.HOUR_OF_DAY) && min == c.get(Calendar.MINUTE)) {
            return true;
        }

        return false;
    }

}
