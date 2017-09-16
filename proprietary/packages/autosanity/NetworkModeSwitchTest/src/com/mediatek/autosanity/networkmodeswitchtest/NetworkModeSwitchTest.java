package com.mediatek.autosanity.networkmodeswitchtest;

import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.telephony.ServiceState;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.CheckedTextView;
import android.widget.RadioButton;
import android.widget.Toast;
import com.android.internal.telephony.PhoneConstants;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;
import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.settings.MultipleSimActivity;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;
import com.mediatek.telephony.TelephonyManagerEx;
import testcommon.SoloDecorator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * TODO: waiting logic for all(based on receiver)
 * TODO: split 3G switch and other cases
 */
public class NetworkModeSwitchTest extends InstrumentationTestCase {

    private static final String TAG = "NetworkModeSwitchTest";
    private static final String PACKAGE_NAME = "com.android.phone";
    private static final String BAIDU = "http://www.baidu.com";
    private static final String GOOGLE = "http://www.google.com";

    private static final String NETWORK_MODE_CLASS = "com.android.phone.MobileNetworkSettings";
    public static final String NETWORK_MODE_KEY = "preferred_network_mode_key";

    private static final int SWITCH_MANUAL_ALLOWED_SLOT1 = 1;
    private static final int SWITCH_MANUAL_ALLOWED_SLOT2 = 2;
    private static final int SWITCH_MANUAL_ALLOWED_SLOT3 = 4;
    private static final int SWITCH_MANUAL_ALLOWED_SLOT4 = 8;

    private static final String NETWORK_MODE_STRING1 = "com.android.phone:string/gsm_umts_network_preferences_title";
    private static final String NETWORK_MODE_STRING2 = "com.android.phone:string/preferred_network_mode_title";
    private static final String DATA_CONNECTION = "com.android.phone:string/gemini_data_connection";
    private static final String ITEM_3G_SERVICE = "com.android.phone:string/setting_for_3G_service";

    private static final String CONFIRM_3G_SWITCH = "com.android.phone:string/confirm_3g_switch";
    private static final String CONFIRM_3G_SWITCH_TO_OFF = "com.android.phone:string/confirm_3g_switch_to_off";

    private static final int TIME_ONE_SECOND = 1000;
    /**
     * after phone process killed and restarted, fwk and native layer need some time
     * to recovery and sync state. so, we'll wait a few minutes to reduce the false alarm
     */
    private static final int CONFIG_WAIT_TIME_AFTER_RESET = 60 * TIME_ONE_SECOND; // 1 mins
    private static final int CONFIG_WAIT_TIME_AFTER_NETWORK_MODE_CHANGED = 60 * TIME_ONE_SECOND; // 1 mins
    private static final int CONFIG_WAIT_SECONDS = 60;

    private static boolean sIsPreviousTestPass = true;
    private static String sVersion = "NA";
    private static EventLogger sEventLogger = new EventLogger(TAG);

    private String[] SERVICE_3G = {
            CONFIRM_3G_SWITCH,
            CONFIRM_3G_SWITCH_TO_OFF};
    private Instrumentation mInst;
    private Context mContext;
    private PreferenceActivity mActivity;
    private SoloDecorator mSolo;
    private List<SimInfoRecord> mSimList = null;
    private PowerManager.WakeLock mWakeLock = null;
    private TestTimer mTimer;

    public NetworkModeSwitchTest() {
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

        Intent intent = null;
        intent = TestUtils.getIntent(PACKAGE_NAME, NETWORK_MODE_CLASS);
        mActivity = (PreferenceActivity) mInst.startActivitySync(intent);
        mSolo = new SoloDecorator(TAG, mInst, mActivity);
        mSimList = TestUtils.getSimInfoList(mContext);
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
        String versionName;
        Context context = getInstrumentation().getContext();
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "0.0";
        }
        log("version name: " + versionName);
        sEventLogger.logEvent(EventLogger.Event.PHONE_RESTART);
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

    public void test02_SwitchTo3GMode() {
        log("-------------test02_SwitchTo3GMode----------- version: " + sVersion);
        if (sIsPreviousTestPass) {
            sIsPreviousTestPass = false;
            doSwitchNetworkMode(1);
        } else {
            fail("[test02_SwitchTo3GMode]previous test failed, skip current test, version: " + sVersion);
        }
        sIsPreviousTestPass = true;
    }

    public void test03_DataConnWith3GMode() {
        log("-------------test03_DataConnWith3GMode------------ version: " + sVersion);
        if (sIsPreviousTestPass) {
            sIsPreviousTestPass = false;
            doConnectionTestForEachSlot();
        } else {
            fail("[test03_DataConnWith3GMode]previous test failed, skip current test, version: " + sVersion);
        }
        sIsPreviousTestPass = true;
    }

    public void test04_SwitchTo2GMode() {
        log("-------------test04_SwitchTo2GMode--------------- version: " + sVersion);
        if (sIsPreviousTestPass) {
            sIsPreviousTestPass = false;
            doSwitchNetworkMode(2);
        } else {
            fail("[test04_SwitchTo2GMode]previous test failed, skip current test, version: " + sVersion);
        }
        sIsPreviousTestPass = true;
    }

    public void test05_DataConnWith2GMode() {
        log("-------------test05_DataConnWith2GMode------------ version: " + sVersion);
        if (sIsPreviousTestPass) {
            sIsPreviousTestPass = false;
            doConnectionTestForEachSlot();
        } else {
            fail("[test05_DataConnWith2GMode]previous test failed, skip current test, version: " + sVersion);
        }
        sIsPreviousTestPass = true;
    }

    public boolean enterNetworkMode() {
        log("enterNetworkMode()...");
        String networkModeString = null;
        String networkModeString1 = TestUtils.getStringResource(mContext, NETWORK_MODE_STRING1);
        String networkModeString2 = TestUtils.getStringResource(mContext, NETWORK_MODE_STRING2);
        log("networkModeString1: " + networkModeString1);
        log("networkModeString2: " + networkModeString2);
        if (mSolo.searchText(networkModeString1)) {
            networkModeString = networkModeString1;
        } else if (mSolo.searchText(networkModeString2)) {
            networkModeString = networkModeString2;
        } else {
            log("[enterNetworkMode]Don't exist network mode Item");
        }

        log("[enterNetworkMode]the networkModeString to be click: " + networkModeString);
        if (networkModeString == null) {
            log("Network mode Item no exist");
            return false;
        }
        mSolo.clickOnText(networkModeString);
        mInst.waitForIdleSync();
        SystemClock.sleep(1000);

        android.app.Activity multiSimActivity = (android.app.Activity) mSolo.getCurrentActivity();
        log(" current activity: " + multiSimActivity);
        if (multiSimActivity instanceof MultipleSimActivity) {
            log("support Gemini ");
            int slot3G = TestUtils.getslot3G();
            int index = getLocationBySlotId(slot3G);
            mSolo.clickInList(index + 1);
            boolean isDialogShow = mSolo.waitForText(mActivity.getString(android.R.string.cancel), 1, 2 * TIME_ONE_SECOND, false);
            log("is Network Mode Dialog shown: " + isDialogShow);
        }
        return true;
    }

    /**
     * in network mode selection, the index item should be selected
     * @param index starts from 1
     */
    public void selectNetworkMode(int index) {
        boolean isIndexDefaultSelection = isNetworkModeItemAlreadyChecked(index);
        log("[selectNetworkMode], index = " + index + ", is default selection: " + isIndexDefaultSelection);
        mTimer.checkTimeout();
        mSolo.clickInList(index);
        sEventLogger.logEvent(EventLogger.Event.SELECT_NETWORK_MODE);

        if (isIndexDefaultSelection) {
            log("[selectNetworkMode]the index is already selected, skip waiting: " + index);
            return;
        }
        log("[selectNetworkMode]network mode changed, wait a few minutes for stability");
        Toast.makeText(mSolo.getCurrentActivity().getApplicationContext(), "Network Changed, wait for stability", Toast.LENGTH_LONG).show();
        mSolo.sleep(CONFIG_WAIT_TIME_AFTER_NETWORK_MODE_CHANGED);
    }

    public void sendBackKey() {
        log("sendBackKey()...");
        mInst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
        SystemClock.sleep(500);
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

    public boolean open3GSwitch() {
        if (!PhoneUtils.isSupportFeature("3G_SWITCH")) {
            log("Do not support 3g switch");
            return false;
        }
        int slot = GeminiUtils.get3GCapabilitySIM();
        log("3g capability slot: " + slot);
        if (slot >= 0) {
            log("3g switch has opened");
            updateNetworkModeEntriesFor3GSwitch();
            return true;
        }
        ListPreference mNetworkMode = (ListPreference) ((PreferenceActivity) mSolo.getCurrentActivity()).findPreference(NETWORK_MODE_KEY);
        if (mNetworkMode.isEnabled()) {
            log("opened 3G switch");
            updateNetworkModeEntriesFor3GSwitch();
            return true;
        }
        boolean mIs3GSwitchManualChangeAllowed = false;
        int mManualAllowedSlot = -1;
        int slotId = -1;
        mIs3GSwitchManualChangeAllowed = PhoneGlobals.getInstance().phoneMgrEx.is3GSwitchManualChange3GAllowed();
        log("mIs3GSwitchManualChangeAllowed: " + mIs3GSwitchManualChangeAllowed);
        if (!mIs3GSwitchManualChangeAllowed) {
            mManualAllowedSlot = PhoneGlobals.getInstance().phoneMgrEx.get3GSwitchAllowed3GSlots();
            slotId = query3GSwitchManualEnableSlotId(mManualAllowedSlot);
        } else {
            if (mSimList.size() > 0) {
                slotId = mSimList.get(0).mSimSlotId;
            }
        }
        log("mManualAllowedSlot: " + mManualAllowedSlot);

        int id = mActivity.getResources().getIdentifier("com.android.phone:string/enable_3G_service", null, null);
        log("get str :" + mActivity.getString(id));
        mSolo.clickOnText(mActivity.getString(id));
        mInst.waitForIdleSync();
        log("click slotId: " + slotId);
        int index = getLocationBySlotId(slotId);
        assertEquals(true, (index >= 0));
        mSolo.clickInList(index + 1);
        if (isExist3GServiceConfirmDialog()) {
            mSolo.clickOnButton(1);
        }
        mSolo.sleep(7000);
        int Capacity3GSlotId = GeminiUtils.get3GCapabilitySIM();
        updateNetworkModeEntriesFor3GSwitch();
        log("Capacity3GSlotId: " + Capacity3GSlotId);
        return Capacity3GSlotId == slotId;
    }

    public void updateNetworkModeEntriesFor3GSwitch() {
        if (!PhoneUtils.isSupportFeature("3G_SWITCH")) {
            log("Do not support 3g switch");
            return;
        }
        ListPreference mNetworkMode = (ListPreference) ((PreferenceActivity) mSolo.getCurrentActivity()).findPreference(NETWORK_MODE_KEY);
        if (!mNetworkMode.isEnabled()) {
            log("Network mode disable.");
            return;
        }
        String networkModeString = null;
        String networkModeString1 = TestUtils.getStringResource(mActivity.getApplicationContext(), NETWORK_MODE_STRING1);
        String networkModeString2 = TestUtils.getStringResource(mActivity.getApplicationContext(), NETWORK_MODE_STRING2);
        log("networkModeString1: " + networkModeString1);
        log("networkModeString2: " + networkModeString2);
        if (mSolo.searchText(networkModeString1)) {
            networkModeString = networkModeString1;
        } else if (mSolo.searchText(networkModeString2)) {
            networkModeString = networkModeString2;
        } else {
            log("Don't exist network mode related Item");
        }

        log("[updateNetworkModeEntriesFor3GSwitch]networkModeString to be click: " + networkModeString);
        if (networkModeString == null) {
            log("Network mode Item no exist");
            return;
        }
        mSolo.clickOnText(networkModeString);
        SystemClock.sleep(50);
        mNetworkMode.getEntries();
        String defaultString = (String) mNetworkMode.getEntry();
        log("defaultString: " + defaultString);
    }

    private int query3GSwitchManualEnableSlotId(int manualAllowedSlot) {
        int slotId = -1;
        if ((manualAllowedSlot & SWITCH_MANUAL_ALLOWED_SLOT1) > 0) {
            slotId = PhoneConstants.GEMINI_SIM_1;
        } else if ((manualAllowedSlot & SWITCH_MANUAL_ALLOWED_SLOT2) > 0) {
            slotId = PhoneConstants.GEMINI_SIM_2;
        } else if ((manualAllowedSlot & SWITCH_MANUAL_ALLOWED_SLOT3) > 0) {
            slotId = PhoneConstants.GEMINI_SIM_3;
        } else if ((manualAllowedSlot & SWITCH_MANUAL_ALLOWED_SLOT4) > 0) {
            slotId = PhoneConstants.GEMINI_SIM_4;
        }
        return slotId;
    }

    private int getLocationBySlotId(int slotId) {
        int index = -1;
        for (int i = 0; i < mSimList.size(); i++) {
            if (mSimList.get(i).mSimSlotId == slotId) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            index = 0;
        }
        log("click 3G Switch location: " + index);
        return index;
    }

    private boolean isExist3GServiceConfirmDialog() {
        ArrayList<String> dialogStrings = new ArrayList<String>();
        dialogStrings.clear();
        for (String string : SERVICE_3G) {
            String digMsg =  TestUtils.getStringResource(mInst.getTargetContext(), string);
            log("[isExist3GServiceConfirmDialog]String: " + digMsg);
            if (digMsg.length() >= 3) {
                digMsg = digMsg.substring(0, 4);
                dialogStrings.add(digMsg);
            }
        }

        for (String string : dialogStrings) {
            if (mSolo.searchText(string)) {
                log("[isExist3GServiceConfirmDialog]true");
                return true;
            }
        }
        return false;
    }

    private boolean isAllStateInService() {
        int waitTimesLeft = CONFIG_WAIT_SECONDS;
        while (waitTimesLeft >= 0) {
            waitTimesLeft--;
            mSolo.sleep(TIME_ONE_SECOND);
            log("[isAllStateInService]still out of service, waitTimesLeft = " + waitTimesLeft);
            if (isCurrentStateInService()) {
                log("[isAllStateInService]SERVICE_IN_STATE, continue testing");
                return true;
            }
        }

        log("[isAllStateInService]finally, out of service, following test might fail later");
        return false;
    }

    private boolean isCurrentStateInService() {
        boolean isInService = true;
        ServiceState serviceState;
        if (GeminiUtils.isGeminiSupport()) {
            int slots[] = GeminiUtils.getSlots();
            for (int i : slots) {
                log("[isCurrentStateInService], slotId = " + i);
                if (GeminiUtils.isValidSlot(i)) {
                    serviceState = ServiceState.newFromBundle(PhoneGlobals.getInstance().phoneMgrEx
                            .getServiceState(i));
                    int currentState = serviceState.getState();
                    if ((currentState != ServiceState.STATE_IN_SERVICE)) {
                        log("[isCurrentStateInService]slot " + i + " out of service, state: " + currentState);
                        isInService = false;
                    }
                }
            }
        } else {
            serviceState = ServiceState.newFromBundle(PhoneGlobals.getInstance().phoneMgr
                    .getServiceState());
            int currentState = serviceState.getState();
            if ((currentState != ServiceState.STATE_IN_SERVICE)) {
                log("[isCurrentStateInService]out of service, state: " + currentState);
                isInService = false;
            }
        }
        return isInService;
    }

    /**
     * switch network mode
     * @param testNo the test count, for the first test, should be 1
     */
    private void doSwitchNetworkMode(int testNo) {
        log("[doSwitchNetworkMode] for test " + testNo);
        if (!isAllStateInService()) {
            fail("wait for " + CONFIG_WAIT_SECONDS + " seconds, no service, version: " + sVersion + ", " +
                    "PhoneInterfaceManager.getServiceState() != ServiceState.STATE_IN_SERVICE");
        }

        if (PhoneUtils.isSupportFeature("3G_SWITCH")) {
            String clickString = TestUtils.getStringResource(mContext, ITEM_3G_SERVICE);
            if (mSolo.searchText(clickString)) {
                mSolo.clickOnText(clickString);
                mInst.waitForIdleSync();
                boolean isSuccessful = open3GSwitch();
                assertEquals(true, isSuccessful);
                SystemClock.sleep(1000);
                int clickItemIndex = getNetworkModeIndex(testNo);
                selectNetworkMode(clickItemIndex);
                mSolo.sleep(500);
                mSolo.goBack();
            }
        } else {
            if (enterNetworkMode()) {
                int clickItemIndex = getNetworkModeIndex(testNo);
                selectNetworkMode(clickItemIndex);
                if (GeminiUtils.isGeminiSupport()) {
                    sendBackKey();
                }
            }
        }
    }

    /**
     * check which item should be click during the specific test
     * @param testNo the test count, for the first test, should be 1
     * @return the index which could be clicked by solo directly
     */
    private int getNetworkModeIndex(int testNo) {
        ArrayList<CheckedTextView> networkModeItems = mSolo.getCurrentViews(CheckedTextView.class);
        int itemCount = networkModeItems.size();
        log("[getNetworkModeIndex]test number: " + testNo + ", all item count: " + itemCount);
        if (itemCount <= testNo) {
            log("[getNetworkModeIndex]only " + itemCount + " items, so click " + itemCount);
            return itemCount;
        }
        if (mSolo.searchText("4G")) {
            log("[getNetworkModeIndex]in 4G device, click the first 2 items");
            return testNo;
        }
        // we want to click the last 2 items during the 2 test number
        int indexFactor = itemCount - 2;
        int recommendIndex = testNo + indexFactor;
        log("[getNetworkModeIndex]recommend click item " + recommendIndex);
        return recommendIndex <= itemCount ? recommendIndex : itemCount;
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

    private boolean isNetworkModeItemAlreadyChecked(int index) {
        log("[isNetworkModeItemAlreadyChecked] index = " + index);
        mSolo.logCurrentTexts();
        ArrayList<CheckedTextView> networkModeItems = mSolo.getCurrentViews(CheckedTextView.class);
        return networkModeItems.get(index - 1).isChecked();
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
