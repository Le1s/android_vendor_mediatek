package com.android.phone.networkoperator.test;

import java.util.List;

import android.content.pm.PackageManager;
import android.preference.Preference;

import android.widget.ListView;
import com.android.internal.telephony.PhoneFactory;
import com.android.phone.PhoneGlobals;
import com.mediatek.phone.GeminiConstants;
import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.phone.testcommon.SoloDecorator;
import com.mediatek.settings.NetworkSettingList;
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;
import com.mediatek.telephony.TelephonyManagerEx;

import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.os.PowerManager;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.test.InstrumentationTestCase;
import android.view.KeyEvent;
import android.util.Log;


public class NetworkOperatorTestcase extends InstrumentationTestCase {

    private static final String TAG = "NetworkOperatorTestcase";
    private static final String PACKAGE_NAME = "com.android.phone";
    private static final String LIST_NETWORKS_KEY = "list_networks_key";
    private static final String NETWORK_MODE_CLASS = "com.android.phone.MobileNetworkSettings";
    private static final String NETWORK_SETTING = "com.android.phone.NetworkSetting";
    private static final String BUTTON_SELECT_MANUAL = "button_manual_select_key";

    private static final String NETWORK_OPERATOR = "com.android.phone:string/networks";
    private static final String DIALOG_NETWORK_LIST_LOAD = "com.android.phone:string/load_networks_progress";

    private static final int TIME_ONE_SECOND = 1000;
    private static final int CONFIG_WAIT_SECONDS = 60;
    private static boolean sIsPreviousTestPass = true;
    private static String sVersion = "NA";
    private static EventLogger sEventLogger = new EventLogger(TAG);

    private Instrumentation mInst;
    private Context mContext;
    private PreferenceActivity mActivity;
    private SoloDecorator mSolo;
    private List<SimInfoRecord> mSimList = null;
    private PowerManager.WakeLock mWakeLock = null;


    protected void setUp() throws Exception {
        super.setUp();
        log("---------------- setUp ------------------");
        mInst = getInstrumentation();
        mContext = mInst.getTargetContext();

        screenOn();

        Intent intent = null;
        intent = NetWorkOperatorUtils.getIntent(PACKAGE_NAME, NETWORK_MODE_CLASS);
        mActivity = (PreferenceActivity) mInst.startActivitySync(intent);
        mSolo = new SoloDecorator(TAG, mInst, mActivity);
        mSimList = NetWorkOperatorUtils.getSimInfoList(mContext);
    }

    protected void tearDown() throws Exception {
        log("------------------ testDown ----------------------");
        try {
            if (mActivity != null) {
                log("[tearDown]finish launched activity");
                mActivity.finish();
            }
        } catch (Throwable e) {
            log("[tearDown]catch exception: " + e.toString());
            e.printStackTrace();
        }
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
        if (mSolo != null) {
            mSolo.finishOpenedActivities();
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
        assertTrue("mInst should not be null.", mInst != null);
        assertTrue("mActivity should not be null.", mActivity != null);

        log("waiting phone process restart and init for 2 min");
        SystemClock.sleep(60 * TIME_ONE_SECOND);

        checkSimInsertStatus();

        sendHomeKey();
        sIsPreviousTestPass = true;
    }

    public void test02_SelectNetworkMode() {
        log("--------test02_SelectNetworkMode begin--------, version: " + sVersion);
        if (sIsPreviousTestPass) {
            sIsPreviousTestPass = false;
            setTestNetworkMode();
        } else {
            fail("[test02_SelectNetworkMode]previous test failed, skip current test, version: " + sVersion);
        }
        sIsPreviousTestPass = true;
    }

    public void test03_CheckNetworkOperators() {
        log("--------test03_CheckNetworkOperators begin--------, version: " + sVersion);
        if (sIsPreviousTestPass) {
            sIsPreviousTestPass = false;
            enterNetworkOperator();
        } else {
            fail("[test03_CheckNetworkOperators]previous test failed, skip current test, version: " + sVersion);
        }
        sIsPreviousTestPass = true;
    }

    private void setTestNetworkMode() {
        if (!isAllStateInService()) {
            fail(" wait " + CONFIG_WAIT_SECONDS + " seconds, no service, version: " + sVersion + ", " +
                    "PhoneInterfaceManager.getServiceState() != ServiceState.STATE_IN_SERVICE");
        }
        NetworkModeSelectHelper helper = new NetworkModeSelectHelper(mInst, mSolo);
        sEventLogger.logEvent(EventLogger.Event.SELECT_NETWORK_MODE);
        helper.changeNetworkModeForTest(1);
    }

    private void enterNetworkOperator() {
        assertTrue(mSimList.size() > 0);

        log("enterNetworkOperator()...");
        if (!GeminiUtils.isGeminiSupport()) {
            String networkString = NetWorkOperatorUtils.getStringResource(mContext, NETWORK_OPERATOR);
            mSolo.clickOnText(networkString);
            SystemClock.sleep(TIME_ONE_SECOND);
        }


        int slot3G = GeminiUtils.get3GCapabilitySIM();
        assertTrue("There should be one 3G sim at least, version: " + sVersion, slot3G != -1);
        if (GeminiUtils.isGeminiSupport()) {
            mSolo.sendKey(KeyEvent.KEYCODE_HOME);
            mSolo.finishOpenedActivities();
            int index = -1;
            if (slot3G == -1) {
                index = getLocationBySlotId(0);
            } else {
                index = getLocationBySlotId(slot3G);
            }
            log("index : " + index);
            Intent intent = new Intent();
            intent.setClassName(PACKAGE_NAME, NETWORK_SETTING);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(GeminiConstants.SLOT_ID_KEY, index);
            mActivity = (PreferenceActivity) mInst.startActivitySync(intent);
            mSolo = new SoloDecorator(TAG, getInstrumentation(), mActivity);
            mInst.waitForIdleSync();

        }

        PreferenceActivity currentActivity = (PreferenceActivity) mSolo.getCurrentActivity();
        Preference manuSelect = currentActivity.getPreferenceScreen().findPreference(BUTTON_SELECT_MANUAL);
        assertEquals(true, manuSelect != null);
        mSolo.sleep(500);
        mSolo.clickOnView(currentActivity.getListView().getChildAt(0));
        if (GeminiUtils.isGeminiSupport() && !PhoneFactory.isDualTalkMode()) {
            long dataConnectionId = Settings.System.getLong(mSolo.getCurrentActivity().getContentResolver(),
                Settings.System.GPRS_CONNECTION_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
            log("dataConnectionId = " + dataConnectionId);
            if (dataConnectionId != Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER) {
                int slot = -1;
                SimInfoRecord simInfoRecord = SimInfoManager.getSimInfoById(mSolo.getCurrentActivity(), dataConnectionId);
                if (simInfoRecord != null) {
                    slot = simInfoRecord.mSimSlotId;
                }
                log("slot = " + slot3G);
                if (slot != slot3G) {
                    mSolo.clickOnButton(1);
                }
            }
        }
        mSolo.clickOnButton(1);
        sEventLogger.logEvent(EventLogger.Event.START_SEARCH_OPERATORS);
        mInst.waitForIdleSync();
        NetworkSettingList list = (NetworkSettingList) mSolo.getCurrentActivity();
        mSolo.assertCurrentActivity("NetworkSettingList", list.getClass());
        String msg = NetWorkOperatorUtils.getStringResource(list, DIALOG_NETWORK_LIST_LOAD);
        log("Dialog msg: " + msg);

        mSolo.waitForTextDisappear(msg);

        sEventLogger.logEvent(EventLogger.Event.FINISH_SEARCH_OPERATORS);
        mSolo.sleep(5 * TIME_ONE_SECOND);
        log("[enterNetworkOperator]search network operators done");
        mSolo.logCurrentTexts();
        ListView searchResultList = mSolo.getCurrentViews(ListView.class).get(0);
        int searchResultCount = searchResultList.getCount();
        log("[enterNetworkOperator]total " + searchResultCount + " operators found");
        assertEquals(true, (searchResultCount > 0));
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

    public void log(String string) {
        Log.i(TAG, string);
    }

    private int getLocationBySlotId(int slotId) {
        int index = -1;
        for (int i = 0; i < mSimList.size(); i++) {
            if (mSimList.get(i).mSimSlotId == slotId) {
                index = i;
                break;
            }
        }

        log("click 3G Switch location: " + index);
        return index;
    }

    public boolean isAllStateInService() {
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

    private void checkSimInsertStatus() {
        log("[checkSimInsertStatus]should insert " + GeminiUtils.getSlotCount() + " SIM(s)");
        for (int slotId : GeminiUtils.getSlots()) {
            boolean isSimInsert = TelephonyManagerEx.getDefault().hasIccCard(slotId);
            log("[checkSimInsertStatus]checking slot " + slotId + " insert? " + isSimInsert);
            assertTrue("[checkSimInsertStatus]sim not found in slot " + slotId, isSimInsert);
        }
    }
}
