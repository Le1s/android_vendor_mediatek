package com.mediatek.calendar.regressiontest;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.util.Log;
import android.view.Display;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController.ViewType;

import java.lang.reflect.Field;

/**
 * Calendar regression test case class. In order to test launch calendar.
 */
public class CalendarTest extends ActivityInstrumentationTestCase2<AllInOneActivity> {
    private static final String TAG = "CalendarTest";
    private AllInOneActivity mAllInOneActivity = null;
    private Instrumentation mInst;

    public CalendarTest() {
        super(AllInOneActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInst = getInstrumentation();
        setActivityInitialTouchMode(false);
    }

    @Override
    protected void tearDown() throws Exception {
        if (mAllInOneActivity != null) {
            mAllInOneActivity.finish();
            mAllInOneActivity = null;
        }
        super.tearDown();
    }

    /**
     * test launch calendar.
     */
    public void testStart() {
        Log.v(TAG, "test start calendar....start");
        setActivityIntent(createWeekViewIntent());
        mAllInOneActivity = getActivity();
        assertNotNull(mAllInOneActivity);

        mInst.waitForIdleSync();
        int currentView = getIntVariable("mCurrentView");
        if (currentView < 0) {
            Log.v(TAG, "currentView=" + currentView);
        } else {
            assertEquals(ViewType.WEEK, getIntVariable("mCurrentView"));
        }

        mInst.waitForIdleSync();
        dragToLeft(this, mAllInOneActivity);

        mInst.waitForIdleSync();
        dragToRight(this, mAllInOneActivity);
        Log.v(TAG, "test start calendar....end");
    }

    /**
     * Drag the screen down with a quarter distance right at left quarter screen.
     */
    private void dragToLeft(InstrumentationTestCase test, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        final android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);

        final float fromX = size.x * 0.25f;
        final float toX = size.x * 0.75f;
        final float y = size.y * 0.5f;

        TouchUtils.drag(test, fromX, toX, y, y, 4);
    }

    /**
     * Drag the screen down with a quarter distance right at right quarter screen.
     */
    private void dragToRight(InstrumentationTestCase test, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        final android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);

        final float fromX = size.x * 0.75f;
        final float toX = size.x * 0.25f;
        final float y = size.y * 0.5f;

        TouchUtils.drag(test, fromX, toX, y, y, 4);
    }

    private int getIntVariable(String name) {
        int result = -1;
        try {
            Field field = mAllInOneActivity.getClass().getDeclaredField(name);
            field.setAccessible(true);
            result = field.getInt(mAllInOneActivity);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Intent createWeekViewIntent() {
        Editor editor = mInst
                .getTargetContext()
                .getSharedPreferences("com.android.calendar_preferences",
                        Context.MODE_WORLD_WRITEABLE).edit();
        editor.putInt("preferred_startView", ViewType.WEEK);
        editor.commit();

        Intent intent = new Intent();
        intent.setClass(getInstrumentation().getContext(), AllInOneActivity.class);
        return intent;
    }
}
