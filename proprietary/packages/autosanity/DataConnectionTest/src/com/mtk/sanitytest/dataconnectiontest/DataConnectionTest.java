package com.mtk.sanitytest.dataconnectiontest;

import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RadioButton;
import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.telephony.TelephonyManagerEx;
import testcommon.SoloDecorator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * TODO: waiting logic for all(based on receiver)
 * TODO: split 3G switch and other cases
 */
public class DataConnectionTest extends InstrumentationTestCase {

    private static final String TAG = "DataConnectionTest";
    private static final String PACKAGE_NAME = "com.android.phone";
    private static final String BAIDU = "http://www.baidu.com";
    private static final String GOOGLE = "http://www.google.com";

    private static final String NETWORK_MODE_CLASS = "com.android.phone.MobileNetworkSettings";

    private static final String DATA_CONNECTION = "com.android.phone:string/gemini_data_connection";

    private static final int TIME_ONE_SECOND = 1000;
    /**
     * after phone process killed and restarted, fwk and native layer need some time
     * to recovery and sync state. so, we'll wait a few minutes to reduce the false alarm
     */
    private static final int CONFIG_WAIT_TIME_AFTER_RESET = 60 * TIME_ONE_SECOND; // 1 mins

    private static boolean sIsPreviousTestPass = true;
    private static String sVersion = "NA";
    private static EventLogger sEventLogger = new EventLogger(TAG);

    private Instrumentation mInst;
    private Context mContext;
    private PreferenceActivity mActivity;
    private SoloDecorator mSolo;
    private PowerManager.WakeLock mWakeLock = null;
    private TestTimer mTimer;

    public DataConnectionTest() {
    }

    /**
     * There is a timeout in NATA, allow each test case run
     * at most 5 min
     */
    private static final long NATA_TIME_OUT_MILLIS = 5 * 60 * TIME_ONE_SECOND;
    /**
     * We should kill self before NATA timeout.
     * so our timeout would be shorter than NATA.
     */
    private static final long TEST_TIME_OUT_MILLIS = NATA_TIME_OUT_MILLIS - 30 * TIME_ONE_SECOND;

    /**
     * a simple timer which would check whether the test case
     * running too long. if so, it will assertion failed.
     */
    private class TestTimer {
        private long mTimeoutMillis;
        public TestTimer() {
            mTimeoutMillis = System.currentTimeMillis() + TEST_TIME_OUT_MILLIS;
        }

        /**
         * if current system time millis is larger than predefined one,
         * assertion fail. in order to get the call stack, and some more
         * error information
         */
        public void checkTimeout() {
            assertTrue("[checkTimeout]timeout" + ", version = " + sVersion, mTimeoutMillis >= System.currentTimeMillis());
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        log("---------- start setUp--------------");
        mInst = getInstrumentation();
        mContext = mInst.getTargetContext();

        screenOn();
        boolean isWifiDisabled = TestUtils.disableWifi(mContext);
        assertTrue("disable wifi failed." + ", version = " + sVersion, isWifiDisabled);

        Intent intent;
        intent = TestUtils.getIntent(PACKAGE_NAME, NETWORK_MODE_CLASS);
        mActivity = (PreferenceActivity) mInst.startActivitySync(intent);
        mSolo = new SoloDecorator(TAG, mInst, mActivity);
        mTimer = new TestTimer();
        log("----------- end setUp --------------");
    }

    protected void tearDown() throws Exception {
        log("------------ tearDown ---------------");
        if (mSolo != null) {
            mSolo.finishOpenedActivities();
        }
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        super.tearDown();
    }

    public void screenOn() {
        PowerManager pm = (PowerManager) mInst.getContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        KeyguardManager km = (KeyguardManager) mInst.getContext().getSystemService(Context.KEYGUARD_SERVICE);
        km.newKeyguardLock(TAG).disableKeyguard();
    }

    public void test01_Precondition() {
        log("--------test01_Precondition() begin--------");
        sEventLogger.logEvent(EventLogger.Event.PHONE_RESTART);
        String versionName;
        Context context = getInstrumentation().getContext();
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "0.0";
        }
        log("version name: " + versionName);
        sVersion = versionName;
        sIsPreviousTestPass = false;
        assertTrue("mInst should not be null." + ", version = " + sVersion, mInst != null);
        assertTrue("mActivity should not be null." + ", version = " + sVersion, mActivity != null);

        // wait a long time till fwk and native sync state done after reset phone process
        log("[test01_Precondition]waiting for stability");
        mSolo.sleep(CONFIG_WAIT_TIME_AFTER_RESET);

        checkSimInsertStatus();

        sendHomeKey();
        sIsPreviousTestPass = true;
    }

    public void test02_DataConnection() {
        log("-------------test02_DataConnection------------ version: " + sVersion);
        assertTrue("[test02_DataConnection]previous test failed, skip current test, version: " + sVersion, sIsPreviousTestPass);
        sIsPreviousTestPass = false;
        doConnectionTestForEachSlot();
        sIsPreviousTestPass = true;
    }

    public void sendHomeKey() {
        log("sendHomeKey()...");
        mInst.sendCharacterSync(KeyEvent.KEYCODE_HOME);
        // mSolo.sendKey(KeyEvent.KEYCODE_HOME);
        SystemClock.sleep(500);
    }

    public void enterDataConnection() {
        log("enterDataConnection()...");
        String dataConnectionString = TestUtils.getStringResource(mContext, DATA_CONNECTION);
        boolean isDialogShow = false;
        // try 5 times, till dialog shown
        for (int i = 0; i < 5; ++i) {
            boolean isTextFound = mSolo.searchText(dataConnectionString);
            // the dialog might disappear automatically, we have to avoid it.
            if (!isDialogShow && isTextFound) {
                mSolo.clickOnText(dataConnectionString);
                isDialogShow = mSolo.waitForText(mActivity.getString(android.R.string.cancel), 1, 2 * TIME_ONE_SECOND, false);
            } else {
                mSolo.sleep(2 * TIME_ONE_SECOND);
            }
            if (isDialogShow) {
                break;
            }
        }
        assertTrue("[enterDataConnection]the dialog not appear " + sVersion, isDialogShow);
    }

    public void selectDataConnection(int slotId) {
        enterDataConnection();
        log("selectDataConnection()..., index: " + slotId);
        // if the index is the default selection, no need to register the receiver
        ArrayList<RadioButton> checkItems = mSolo.getCurrentViews(RadioButton.class);
        for (int i = 0; i < 5; i++) {
            if (checkItems.size() < GeminiUtils.getSlotCount() + 1) {
                mSolo.sleep(TIME_ONE_SECOND);
                checkItems = mSolo.getCurrentViews(RadioButton.class);
            } else {
                break;
            }
        }
        mSolo.logCurrentTexts();
        assertTrue("[selectDataConnection]checkItem size not correct: " + checkItems.size() + ", version = " + sVersion, checkItems.size() == GeminiUtils.getSlotCount() + 1);
        boolean isSlotDefaultSelected;
        if (checkItems.get(slotId).isChecked()) {
            isSlotDefaultSelected = true;
        } else {
            isSlotDefaultSelected = false;
        }

        mSolo.clickInList(slotId + 1);
        sEventLogger.logEvent(EventLogger.Event.SELECT_DATA_CONNECT_SIM);
        if (isSlotDefaultSelected) {
            log("[selectDataConnection]the data connection keep default selection, no need waiting");
            return;
        }
        String confirmText = mActivity.getString(com.android.internal.R.string.yes);
        // searchText would wait at most 5s
        if (mSolo.searchText(confirmText, 1, false, true)) {
            mSolo.clickOnButton(confirmText);
            mSolo.sleep(10 * TIME_ONE_SECOND);
        } else {
            mSolo.sleep(5 * TIME_ONE_SECOND);
        }
    }

    public void doDataConnWithURL() {
        log("doDataConnWithURL()...");

        SystemClock.sleep(3000);
        boolean isConnected = false;
        sEventLogger.logEvent(EventLogger.Event.START_CONNECT);
        for (int i = 0; i < 2; i++) { // try at most 2 times
            mTimer.checkTimeout();
            waitForNetworkInfoReady();

            sEventLogger.logEvent(EventLogger.Event.START_CONNECT);
            if (openURL(GOOGLE) || openURL(BAIDU)) {
                isConnected = true;
                break;
            } else {
                log("connect failed for both baidu & google, try again");
            }

            mSolo.sleep(10 * TIME_ONE_SECOND); // wait a few seconds, and try again
            mTimer.checkTimeout();
        }

        sEventLogger.logEvent(EventLogger.Event.FINISH_CONNECT);
        assertTrue("can't open www.baidu.com and www.google.com" + ", version = " + sVersion, isConnected);
    }

    private boolean openURL(String website) {
        ConnectThreadPool pool = new ConnectThreadPool(website);
        for (int i = 0; i < 3; i++) {
            log("[openURL]open " + website + ", try count: " + i);
            pool.addNewConnectThread();
            for (int j = 0; j < 20; j++) {
                mSolo.sleep(TIME_ONE_SECOND);
                // check whether any thread connect success every second
                if (pool.isConnectSuccess()) {
                    return true;
                }
                mTimer.checkTimeout();
            }
        }
        while (!pool.isAllThreadFinished()) {
            mSolo.sleep(2 * TIME_ONE_SECOND);
            if (pool.isConnectSuccess()) {
                return true;
            }
            mTimer.checkTimeout();
        }
        return pool.isConnectSuccess();
    }

    public void log(String string) {
        Log.i(TAG, string);
    }

    /**
     * try to make data connection for all slots
     */
    private void doConnectionTestForEachSlot() {
        log("[doConnectionTestForEachSlot]total slot count: " + GeminiUtils.getSlotCount());
        for (int slotId : GeminiUtils.getSlots()) {
            SystemClock.sleep(1000);
            log("[doConnectionTestForEachSlot]switch DataConnection to slot: " + slotId);
            selectDataConnection(slotId);
            SystemClock.sleep(1000);
            log("[doConnectionTestForEachSlot]try data connection on slot " + slotId);
            doDataConnWithURL();
        }
    }

    private void checkSimInsertStatus() {
        log("[checkSimInsertStatus]should insert " + GeminiUtils.getSlotCount() + " SIM(s)");
        for (int slotId : GeminiUtils.getSlots()) {
            boolean isSimInsert = TelephonyManagerEx.getDefault().hasIccCard(slotId);
            log("[checkSimInsertStatus]checking slot " + slotId + " insert? " + isSimInsert);
            assertTrue("[checkSimInsertStatus]sim not found in slot " + slotId + ", version = " + sVersion, isSimInsert);
        }
    }

    /**
     * wait for network info ready, if it not ready in a few seconds,
     * we'll consider it as failed
     */
    private void waitForNetworkInfoReady() {
        ConnectivityManager manager =
                (ConnectivityManager) mContext
                        .getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        assertTrue("[doDataConnWithURL]manager is null" + ", version = " + sVersion, manager != null);
        boolean isNetworkInfoReady = false;
        for (int i = 0; i < 6; i++) {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            log("[waitForNetworkInfoReady]networkInfo = " + networkInfo);
            isNetworkInfoReady = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            if (isNetworkInfoReady) {
                break;
            }
            mTimer.checkTimeout();
            mSolo.sleep(10 * TIME_ONE_SECOND);
        }
        log("[waitForNetworkInfoReady]ready? " + isNetworkInfoReady);
    }

    private class ConnectThread extends Thread {
        private boolean mIsFinished = false;
        private boolean mIsConnected = false;
        private String mWebsite;

        private Boolean doInBackground(String website) {
            long threadId = Thread.currentThread().getId();
            boolean isConnected = false;
            HttpURLConnection conn = null;
            InputStream in;
            InputStreamReader isr;
            try {
                log("[openURL]before openURL: " + website + ", tid: " + threadId);
                URL url = new URL(website);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(20 * TIME_ONE_SECOND); // 10 s
                conn.setReadTimeout(20 * TIME_ONE_SECOND); // 20 s
                in = conn.getInputStream();
                log("[openURL]got input stream from connection, tid: " + threadId);
                isr = new InputStreamReader(in);
                int data = isr.read();
                if (data != -1) {
                    isConnected = true;
                }
                isr.close();
                in.close();
            } catch (Exception e) {
                Log.e(TAG, "[openURL]failed to openConnection or read stream. current connection might fail. " + e + ", tid: " + threadId, e);
            } finally {
                log("[openURL]finally disconnect the connection, tid: " + threadId);
                conn.disconnect();
            }
            log("[openURL]connect " + website + ": " + isConnected + ", tid: " + threadId);
            return isConnected;
        }

        private void onPostExecute(Boolean isConnected) {
            mIsFinished = true;
            mIsConnected = isConnected;
        }

        public boolean isFinished() {
            return mIsFinished;
        }

        public boolean isConnected() {
            return mIsConnected;
        }

        public ConnectThread(String website) {
            mWebsite = website;
        }

        @Override
        public void run() {
            boolean isConnected = doInBackground(mWebsite);
            onPostExecute(isConnected);
        }
    }

    private class ConnectThreadPool {
        private ArrayList<ConnectThread> mThreads = new ArrayList<ConnectThread>();
        private String mWebsite;

        public ConnectThreadPool(String website) {
            mWebsite = website;
        }

        public void addNewConnectThread() {
            ConnectThread thread = new ConnectThread(mWebsite);
            thread.start();
            mThreads.add(thread);
        }

        public boolean isAllThreadFinished() {
            for (ConnectThread thread : mThreads) {
                if (!thread.isFinished()) {
                    return false;
                }
            }
            return true;
        }

        public boolean isConnectSuccess() {
            for (ConnectThread thread : mThreads) {
                if (thread.isConnected()) {
                    return true;
                }
            }
            return false;
        }
    }
}
