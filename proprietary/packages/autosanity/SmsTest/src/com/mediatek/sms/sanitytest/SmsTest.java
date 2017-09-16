package com.mediatek.sms.sanitytest;

import java.util.ArrayList;

import com.jayway.android.robotium.solo.Solo; //import android.test.ActivityInstrumentationTestCase2;

import android.test.InstrumentationTestCase;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.SystemClock;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.telephony.PhoneNumberUtils;

import org.w3c.dom.Node;

import com.android.mms.MmsApp;

public class SmsTest extends InstrumentationTestCase {

    private Solo solo;
    private static final String TAG = "SmsTest";
    private static final String CONFIG_FILE = "/sdcard/sanity_configure.xml";
    private static final String SMS_SEND_TABLE_URI = "content://sms/sent";
    private static final String SMS_INBOX_TABLE_URI = "content://sms/inbox";
    private static String sim1_number = null;
    private static String sim2_number = null;
    private Context mContext = null;
    private Activity mActivity = null;
    private Instrumentation mInst = null;
    private static boolean hasSetDefaultMms = false;
    private static boolean IS_GEMINI = true;
    private static boolean IS_AOSP = false;
    /*
     * For gemini: if it's strict, it should send and receive two messages successfully.
     * Else it's success if send and receive one message.
     */
    private boolean mIsStrict = true;
    // wait for 8 minutes to send and recevie two messages
    private static int GEMINI_WAIT_MINUTE = 8;
    // wait for 8 minutes to send and recevie one message
    private static int SINGLE_WAIT_MINUTE = 5;
    private static int WAIT_QUERY_ONE_TIME = 2000;
    // need mutiple minute
    private int mTime = 60 * 1000;

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation());
        mContext = getInstrumentation().getTargetContext();
        SystemClock.sleep(3000);

        mInst = getInstrumentation();
        xmlParser mParser = new xmlParser(CONFIG_FILE);
        Node node = mParser.getRootNode();
        Node SIM1 = mParser.getNodeByName(node, "sim1");
        Node SIM2 = mParser.getNodeByName(node, "sim2");
        Node ndStrict = mParser.getNodeByName(node, "strict");
        sim1_number = mParser.getNodeValue(SIM1);
        if (SIM2 == null) {
            IS_GEMINI = false;
            Log.i(TAG, "Is Gemini: " + IS_GEMINI + ", sim1: " + sim1_number);
        } else {
            sim2_number = mParser.getNodeValue(SIM2);
            Log.i(TAG, "Is Gemini: " + IS_GEMINI + "sim1: " + sim1_number + " sim2:" + sim2_number);
        }
        if (ndStrict != null) {
            mIsStrict = mParser.getNodeValue(ndStrict).equals("yes") ? true : false;
            Log.i(TAG, "Is Strict: " + mIsStrict);
        }

        // set default mms
        setDefaultSms();
    }

    @Override
    public void tearDown() throws Exception {

        try {
            // Robotium will finish all the activities that have been opened
            solo.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.tearDown();
    }

    // If the DialogModeActivity is showing, then finish it to go to home screen.
    private void goToHome() {
        for (int i = 0; i < 10; i++) {
            SystemClock.sleep(2000);
            Activity activity = solo.getCurrentActivity();
            String className = activity.getComponentName().getClassName();
            if (className.equals("com.android.mms.ui.DialogModeActivity")) {
                activity.finish();
                break;
            }
        }
    }

    public void test_sms_send() throws Exception {
        if (IS_GEMINI && !IS_AOSP) {
            mTime *= GEMINI_WAIT_MINUTE;
            gemini_sms_send();
        } else {
            mTime *= SINGLE_WAIT_MINUTE;
            single_sms_send();
        }
    }

    /*
     * SingleCard: Send one message to self.
     */
    private void single_sms_send() throws Exception {
        final String TEST_NAME = "test_sms_send";
        final String MESSAGE = "[" + TEST_NAME + "] ";
        int mSendCnt = 0;
        int mReceiveCnt = 0;

        // test start
        Log.v(TAG, MESSAGE + "test start");
        Log.i(TAG, "Send message from sim to self");

        mSendCnt = getCnt(Uri.parse(SMS_SEND_TABLE_URI), sim1_number);
        mReceiveCnt = getCnt(Uri.parse(SMS_INBOX_TABLE_URI), sim1_number);
        Log.i(TAG, "mSendCnt: " + mSendCnt + " mReceiveCnt: " + mReceiveCnt);
        sendSMS("this is a test message send to self ", sim1_number, -1);

        assertTrue("Send message failed from sim to self",
                isSuccessfully(mSendCnt, Uri.parse(SMS_SEND_TABLE_URI), sim1_number));

        assertTrue("Receive message failed from sim to self",
                isSuccessfully(mReceiveCnt, Uri.parse(SMS_INBOX_TABLE_URI), sim1_number));

        SystemClock.sleep(2000);
        if (!IS_AOSP) {
            goToHome();
        }

        Log.v(TAG, MESSAGE + "test end");
        assertTrue(TEST_NAME, true);
    }

    private void gemini_sms_send() throws Exception {
        final String TEST_NAME = "gemini_sms_send";
        final String MESSAGE = "[" + TEST_NAME + "] ";
        int sendCnt1 = 0;
        int receiveCnt1 = 0;
        int sendCnt2 = 0;
        int receiveCnt2 = 0;

        Log.v(TAG, MESSAGE + "test start");

        // Send sms from sim1 to sim2
        Log.i(TAG, "Send message from sim1 to sim2");

        sendCnt1 = getCnt(Uri.parse(SMS_SEND_TABLE_URI), sim2_number);
        receiveCnt1 = getCnt(Uri.parse(SMS_INBOX_TABLE_URI), sim1_number);
        Log.i(TAG, "sendCnt1: " + sendCnt1 + " receiveCnt1: " + receiveCnt1);
        sendSMS("this is a test message send to SIM2 ", sim2_number, 1);

        // Send sms from sim2 to sim1
        Log.i(TAG, "Send message from sim2 to sim1");

        sendCnt2 = getCnt(Uri.parse(SMS_SEND_TABLE_URI), sim1_number);
        receiveCnt2 = getCnt(Uri.parse(SMS_INBOX_TABLE_URI), sim2_number);
        Log.i(TAG, "sendCnt2: " + sendCnt2 + " receiveCnt2: " + receiveCnt2);
        sendSMS("this is a test message send to SIM1 ", sim1_number, 2);

        if (mIsStrict) {
            assertTrue("Send message failed from sim1 to sim2", isSuccessfully(
                    sendCnt1, Uri.parse(SMS_SEND_TABLE_URI), sim2_number));
            assertTrue("Send message failed from sim2 to sim1", isSuccessfully(
                    sendCnt2, Uri.parse(SMS_SEND_TABLE_URI), sim1_number));
            assertTrue("Receive message failed from sim1 to sim2", isSuccessfully(
                    receiveCnt1, Uri.parse(SMS_INBOX_TABLE_URI), sim1_number));
            assertTrue("Receive message failed from sim2 to sim1", isSuccessfully(
                    receiveCnt2, Uri.parse(SMS_INBOX_TABLE_URI), sim2_number));
        } else {
            assertTrue("Send one message failed", isOneSuccessfully(
                    sendCnt1, Uri.parse(SMS_SEND_TABLE_URI), sim2_number,
                    sendCnt2, Uri.parse(SMS_SEND_TABLE_URI), sim1_number));
            assertTrue("Send one message failed", isOneSuccessfully(
                    receiveCnt1, Uri.parse(SMS_SEND_TABLE_URI), sim1_number,
                    receiveCnt2, Uri.parse(SMS_SEND_TABLE_URI), sim2_number));
        }

        SystemClock.sleep(2000);
        if (!IS_AOSP) {
            goToHome();
        }

        Log.v(TAG, MESSAGE + "test end");
        assertTrue(TEST_NAME, true);
    }

    // Send sms
    public void sendSMS(String message, String simNumber, int ListIndex) {

        // Start SMS Activity
        sendMMSIntent(simNumber, message);
        SystemClock.sleep(4000);
        // Get current Activity

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.d(TAG, "top activity pkg: " + cn.getPackageName() + ", cls: " + cn.getClassName());

        mActivity = solo.getCurrentActivity();
        SystemClock.sleep(2000);
        Log.i(TAG, "Current Activity class: " + mActivity.getClass().toString());

        // Click sim button
        int Send_id = mActivity.getResources().getIdentifier("send_button_sms",
                "id", "com.android.mms");

        View Send = solo.getView(Send_id);
        Log.i(TAG, "Send button is: " + Send + ", Enable: " + Send.isEnabled());

        solo.clickOnView(Send);
        SystemClock.sleep(2000);
        // Click sim button
        Log.v(TAG, "sendSMS: ListIndex =" + ListIndex + " IS_GEMINI = " + IS_GEMINI + " IS_AOSP = "
                + IS_AOSP);
        ArrayList<ListView> listViews = solo.getCurrentViews(ListView.class);
        Log.v(TAG, "sendSMS: listViews size =" + listViews.size());
        if (listViews.size() > 0) {
            if (ListIndex >= 0) {
                solo.clickInList(ListIndex);
            } else {
                solo.clickInList(1);
            }
        }
        mActivity.finish();
        SystemClock.sleep(2000);
    }

    public void sendMMSIntent(String number, String text) {

        // Attach text message and file by intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setClassName("com.android.mms",
                "com.android.mms.ui.ComposeMessageActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("address", number);
        intent.putExtra("sms_body", text);
        intent.putExtra("compose_mode", false);
        //intent.putExtra("exit_on_sent", true);
        mInst.getContext().startActivity(intent);

    }

    // get sent/receive count
    public int getCnt(Uri uri, String telNo) {
        String telNo2 = "";
        String telNo3 = PhoneNumberUtils.formatNumber(telNo, telNo,
                            MmsApp.getApplication().getCurrentCountryIso());

        if (telNo.startsWith("+86")) {
            telNo2 = telNo.substring(3);
        } else {
            telNo2 = "+86" + telNo;
        }
        int cnt = -1;
        ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(uri, null, "address = ? or address = ? or address = ?", new String[]{telNo, telNo2, telNo3}, null);
        if (c != null) {
            cnt = c.getCount();
            Log.i(TAG, telNo + " Get cursor successfully cnt: " + cnt);
            c.close();
        } else {
            Log.i(TAG, "cursor is null");
        }
        return cnt;
    }

    public boolean isSuccessfully(int count_old, Uri uri, String telNo) {
        while (mTime > 0) {
            SystemClock.sleep(WAIT_QUERY_ONE_TIME);
            mTime -= WAIT_QUERY_ONE_TIME;
            if ((count_old + 1) <= getCnt(uri, telNo)) {
                Log.i(TAG, telNo + " Send/Receive message successfully");
                return true;
            }
        }
        return false;
    }

    /*
     * Whether send or receive one message successfully or not.
     */
    public boolean isOneSuccessfully(int count1, Uri uri1, String telNo1, int count2, Uri uri2, String telNo2) {
        while (mTime > 0) {
            SystemClock.sleep(WAIT_QUERY_ONE_TIME);
            mTime -= WAIT_QUERY_ONE_TIME;
            if ((count1 + 1) <= getCnt(uri1, telNo1)) {
                Log.i(TAG, telNo1 + " Send/Receive one message successfully");
                return true;
            }
            if ((count2 + 1) <= getCnt(uri2, telNo2)) {
                Log.i(TAG, telNo2 + " Send/Receive one message successfully");
                return true;
            }
        }
        return false;
    }

    public void setDefaultSms() {
        if (hasSetDefaultMms) {
            return;
        } else {
            hasSetDefaultMms = true;
            Log.d(TAG, "hasSetDefaultMms: " + hasSetDefaultMms);
        }

        // start SettingListActivity
        Intent intent = new Intent();
        if (IS_AOSP) {
            intent.setClassName("com.android.mms",
                    "com.android.mms.ui.MessagingPreferenceActivity");
        } else {
            intent.setClassName("com.android.mms",
                    "com.android.mms.ui.SettingListActivity");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mInst.getContext().startActivity(intent);
        mInst.waitForIdleSync();
        Activity activity = solo.getCurrentActivity();
        int disableMmsId = activity.getResources().getIdentifier("pref_title_sms_disabled",
                "string", "com.android.mms");
        String disableMms = activity.getResources().getString(disableMmsId);

        if (solo.searchText(disableMms)) {
            Log.d(TAG, "Mms is not default app");
            solo.clickOnText(disableMms);
            mInst.waitForIdleSync();
        } else {
            Log.d(TAG, "Mms is default app");
        }

        activity.finish();
        mInst.waitForIdleSync();
        SystemClock.sleep(4000);
    }
}
