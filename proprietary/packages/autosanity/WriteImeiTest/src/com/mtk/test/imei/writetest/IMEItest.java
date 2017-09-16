package com.mtk.test.imei.writetest;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;

public class IMEItest extends InstrumentationTestCase {
    private static final String TAG = "IMEIwrite";
    private static final int MSG_WRITE = 1;
    private static final int MSG_READ = 2;
    private static final int MAX_TIME = 7200 * 1000;
    private static final int PASS = 1;
    private static final int FAIL = 0;
    private static final int UNKNOWN = -1;

    private Object mObject = new Object();
    private int mResult = -1;
    private int mSlotId = 0;
    private int mPhoneCount = 1;

    private final Handler mCommandHander = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
            case MSG_WRITE:
                synchronized (mObject) {
                    AsyncResult asyncResult = (AsyncResult) msg.obj;
                    if (asyncResult != null && asyncResult.exception == null) {
                        mResult = PASS;
                    } else {
                        mResult = FAIL;
                    }
                    mObject.notifyAll();
                }
                break;
            case MSG_READ:
                synchronized (mObject) {
                    AsyncResult asyncResult = (AsyncResult) msg.obj;
                    if (asyncResult != null && asyncResult.exception == null) {
                        String[] data = (String[]) (asyncResult.result);
                        String expected = (String) asyncResult.userObj;
                        mResult = FAIL;
                        Log.d(TAG, "expected: " + expected);
                        for (int i = 0; i < data.length; i++) {
                            Log.d(TAG, "+CGSN response: " + data[i]);
                            if (data[i] != null && data[i].trim().equals(expected)) {
                                mResult = PASS;
                                break;
                            }
                        }
                    } else {
                        mResult = FAIL;
                    }
                    mObject.notifyAll();
                }
                break;
            default:
                break;
            }
        }
    };

    public void testWriteIMEI() throws Throwable {
        String imei1 = ConfigParser.getConfig()[0];
        String imei2 = ConfigParser.getConfig()[1];
        Log.d(TAG, "IMEI1:" + imei1);
        Log.d(TAG, "IMEI2:" + imei2);

        SystemClock.sleep(15000);

        runTestOnUiThread(new Runnable() {
            public void run() {
                mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
            }
        });
        if (mPhoneCount > 1) {
            Log.d(TAG, "Gemini");
            mSlotId = PhoneConstants.SIM_ID_1;
            doWriteImei("AT+EGMR=1,7,\"" + imei1 + "\"");
            doReadImei(imei1);
            mSlotId = PhoneConstants.SIM_ID_2;
            doWriteImei("AT+EGMR=1,10,\"" + imei2 + "\"");
            doReadImei(imei2);
        } else {
            Log.d(TAG, "Single");
            doWriteImei("AT+EGMR=1,7,\"" + imei1 + "\"");
            doReadImei(imei1);
        }
    }

    private void doWriteImei(final String command) throws Throwable {
        synchronized (mObject) {
            mResult = UNKNOWN;
        }
        sendAtCommand(new String[] {command, ""}, mCommandHander.obtainMessage(MSG_WRITE));
        waitForResult(command);
    }

    private void doReadImei(final String expectedIMEI) throws Throwable {
        synchronized (mObject) {
            mResult = UNKNOWN;
        }
        sendAtCommand(new String[] {"AT+CGSN", "+CGSN="},
                mCommandHander.obtainMessage(MSG_READ, expectedIMEI));
        waitForResult("AT+CGSN");
    }

    private void sendAtCommand(final String[] cmd, final Message msg) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                Phone phone = null;
                if (mPhoneCount > 1) {
                    phone = PhoneFactory.getPhone(mSlotId);
                } else {
                    phone = PhoneFactory.getDefaultPhone();
                }
                if (phone == null) {
                    Log.d(TAG, "phone is null");
                } else {
                    Log.d(TAG, "phone is not null");
                }
                Log.d(TAG, "invokeOemRilRequestStrings() " + cmd[0]);
                phone.invokeOemRilRequestStrings(cmd, msg);
            }
        });
    }

    private void waitForResult(String info) {
        Log.d(TAG, "waitForResult()");
        long startTime = System.currentTimeMillis();
        synchronized (mObject) {
            while (true) {
                if (System.currentTimeMillis() - startTime >= MAX_TIME) {
                    Log.e(TAG, info + ": time out");
                    assertTrue(info + ": time out", false);
                    break;
                } else if (mResult == PASS) {
                    Log.d(TAG, info + ": succeed");
                    break;
                } else if (mResult == FAIL) {
                    Log.e(TAG, info + ": fail");
                    assertTrue(info + ": fail", false);
                    break;
                }
                try {
                    mObject.wait(MAX_TIME);
                } catch (InterruptedException e) {
                    Log.d(TAG, "InterruptedException");
                }
            }
        }
    }
}
