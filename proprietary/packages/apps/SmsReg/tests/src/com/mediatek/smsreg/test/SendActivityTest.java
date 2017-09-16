package com.mediatek.smsreg.test;

import android.app.NotificationManager;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.jayway.android.robotium.solo.Solo;
import com.mediatek.smsreg.test.util.MockSmsRegUtil;
import com.mediatek.smsreg.ui.SendMessageAlertActivity;

public class SendActivityTest extends ActivityInstrumentationTestCase2<SendMessageAlertActivity> {
    private static final String TAG = "SmsReg/SendActivityTest";

    private Solo mSolo;
    private SendMessageAlertActivity mActivity;

    public SendActivityTest() {
        super(SendMessageAlertActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mActivity = getActivity();
        mSolo = new Solo(getInstrumentation(), mActivity);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            mSolo.finishOpenedActivities();
            clearNotification();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.tearDown();
    }

    /**
     * Operation: rotate Check: NA
     */
    public void test00() {
        MockSmsRegUtil.formatLog(TAG, "test00");
        Log.i(TAG, "Rotate.");
        mSolo.setActivityOrientation(Solo.LANDSCAPE);
    }

    /**
     * Operation: click cancel -> cancel Check: NA
     */
    public void test01() {
        MockSmsRegUtil.formatLog(TAG, "test01");
        Log.i(TAG, "Click cancle button.");
        mSolo.clickOnButton(mSolo.getString(android.R.string.cancel));

        Log.i(TAG, "Click cancel button of 2nd dialogue");
        mSolo.clickOnButton(mSolo.getString(android.R.string.cancel));
    }

    private void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) mActivity
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }
}
