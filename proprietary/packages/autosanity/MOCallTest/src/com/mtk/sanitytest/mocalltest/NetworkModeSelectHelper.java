package com.mtk.sanitytest.mocalltest;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.telephony.ServiceState;
import android.util.Log;

import android.widget.CheckedTextView;
import com.android.internal.telephony.PhoneConstants;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;
import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.settings.MultipleSimActivity;
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NetworkModeSelectHelper {
    private static final String TAG = "MOCallTest";
    private static final String PACKAGE = "com.android.phone";
    private static final String NETWORK_MODE_CLASS = "com.android.phone.MobileNetworkSettings";
    private static final String NETWORK_MODE_KEY = "preferred_network_mode_key";
    private static final String ITEM_3G_SERVICE = "com.android.phone:string/setting_for_3G_service";
    private static final String NETWORK_MODE_STRING1 = "com.android.phone:string/gsm_umts_network_preferences_title";

    private static final String NETWORK_MODE_STRING2 = "com.android.phone:string/preferred_network_mode_title";
    private static final String CONFIRM_3G_SWITCH = "com.android.phone:string/confirm_3g_switch";
    private static final String CONFIRM_3G_SWITCH_TO_OFF = "com.android.phone:string/confirm_3g_switch_to_off";
    private String[] SERVICE_3G = {
            CONFIRM_3G_SWITCH,
            CONFIRM_3G_SWITCH_TO_OFF};

    private static final int SWITCH_MANUAL_ALLOWED_SLOT1 = 1;

    private static final int SWITCH_MANUAL_ALLOWED_SLOT2 = 2;
    private static final int SWITCH_MANUAL_ALLOWED_SLOT3 = 4;
    private static final int SWITCH_MANUAL_ALLOWED_SLOT4 = 8;

    public static final int CONFIG_WAIT_SECONDS = 60;
    private static final int TIME_ONE_SECOND = 1000;
    private static final int CONFIG_WAIT_TIME_AFTER_NETWORK_MODE_CHANGED = 60 * TIME_ONE_SECOND; // 1 min

    private Instrumentation mInst = null;
    private android.app.Activity mMultiSimActivity;
    private SoloDecorator mSolo;
    private List<SimInfoRecord> mSimList = null;

    public NetworkModeSelectHelper(Instrumentation ins) {
        mInst = ins;
    }

    public void init() {
        initSimMap();
        Intent intent = new Intent();
        intent.setClassName(PACKAGE, NETWORK_MODE_CLASS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Activity activity = mInst.startActivitySync(intent);
        mSolo = new SoloDecorator(TAG, mInst, activity);
    }

    private boolean getNetworkModeSelections() {
        // click "Network Mode" to "Network Mode"(sim card select)
        if (!clickOnNetworkModePreference()) {
            log("[getNetworkModeSelections]click NetworkMode failed");
            return false;
        }

        mMultiSimActivity = mSolo.getCurrentActivity();
        log(" current activity: " + mMultiSimActivity);
        if (mMultiSimActivity instanceof MultipleSimActivity) {
            log("support Gemini ");
            int index = get3GSimDisplayLocation();
            mSolo.clickInList(index + 1);
            boolean isDialogShow = mSolo.waitForText(mSolo.getString(android.R.string.cancel), 1, 2000, false);
            log("is Network Mode Dialog shown: " + isDialogShow);
            SystemClock.sleep(500);
        }
        return true;
    }

    private void selectNetworkMode(int index) {
        boolean isIndexDefaultSelection = isNetworkModeItemAlreadyChecked(index);
        log("[selectNetworkMode], index = " + index + ", is default selection: " + isIndexDefaultSelection);
        mSolo.clickInList(index);

        mInst.waitForIdleSync();
        if (isIndexDefaultSelection) {
            log("[selectNetworkMode]the index is already selected, skip waiting: " + index);
            return;
        }
        log("[selectNetworkMode]network mode changed, wait a few minutes for stability");
        mSolo.sleep(CONFIG_WAIT_TIME_AFTER_NETWORK_MODE_CHANGED);
    }

    public void clear() {
        mSolo.finishOpenedActivities();
        if (mSimList != null) {
            mSimList.clear();
        }
    }

    private void initSimMap() {
//      List<SimInfoRecord> simList = SimInfoManager.getInsertedSimInfoList(this);
        mSimList = SimInfoManager.getInsertedSimInfoList(mInst.getTargetContext());
      Collections.sort(mSimList, new Comparator<SimInfoRecord>() {
          @Override
          public int compare(SimInfoRecord arg0, SimInfoRecord arg1) {
              return (arg0.mSimSlotId - arg1.mSimSlotId);
          }
      });

    }

    private int get3GSimDisplayLocation() {
        int slot3G = -1;
        slot3G = GeminiUtils.get3GCapabilitySIM();
        log("slot3G: " + slot3G);

        int index = -1;
        for (int i = 0; i < mSimList.size(); i++) {
            if (mSimList.get(i).mSimSlotId == slot3G) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            index = 0;
        }
        log("slot3G / displayName: " + slot3G + " / " + index);
        return index;
    }

    private void log(String string) {
        Log.i(TAG, "<Helper>" + string);
    }

    private boolean close3GSwitch() {
        if (!PhoneUtils.isSupportFeature("3G_SWITCH")) {
            log("Do not support 3g switch");
            return false;
        }
        int slot = GeminiUtils.get3GCapabilitySIM();
        log("3g capability slot: " + slot);
        if (slot < 0) {
            log("3G service is off");
            return true;
        }
        PreferenceActivity currentActivity = (PreferenceActivity) mSolo.getCurrentActivity();
        int id = currentActivity.getResources().getIdentifier("com.android.phone:string/enable_3G_service", null, null);
        log("get str :" + currentActivity.getString(id));
        mSolo.clickOnText(currentActivity.getString(id));
        int offItemID = currentActivity.getResources().getIdentifier("com.android.phone:string/service_3g_off", null, null);
        mInst.waitForIdleSync();
        log("get str :" + currentActivity.getString(offItemID));
        mSolo.clickOnText(currentActivity.getString(offItemID));
        if (isExist3GServiceConfirmDialog()) {
            mSolo.clickOnButton(1);
        }
        mSolo.sleep(7000);
        return true;
    }

    private boolean open3GSwitch() {
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
        PreferenceActivity currentActivity = (PreferenceActivity) mSolo.getCurrentActivity();
        boolean mIs3GSwitchManualChangeAllowed = false;
        int slotId = -1;
        mIs3GSwitchManualChangeAllowed = PhoneGlobals.getInstance().phoneMgrEx.is3GSwitchManualChange3GAllowed();
        log("mIs3GSwitchManualChangeAllowed: " + mIs3GSwitchManualChangeAllowed);
        if (!mIs3GSwitchManualChangeAllowed) {
            int mManualAllowedSlot = PhoneGlobals.getInstance().phoneMgrEx.get3GSwitchAllowed3GSlots();
            slotId = query3GSwitchManualEnableSlotId(mManualAllowedSlot);
        } else {
            if (mSimList.size() > 0) {
                slotId = mSimList.get(0).mSimSlotId;
            }
        }
        log("mManualAllowedSlot: " + slotId);
        int id = currentActivity.getResources().getIdentifier("com.android.phone:string/enable_3G_service", null, null);
        log("get str :" + currentActivity.getString(id));
        mSolo.clickOnText(currentActivity.getString(id));
        mInst.waitForIdleSync();
        log("click slotid: " + slotId);
        SimInfoRecord simInfoRecord = SimInfoManager.getSimInfoBySlot(currentActivity, slotId);
        String sim3GDisplayName = simInfoRecord.mDisplayName;
        log("displayName: " + sim3GDisplayName);
        mSolo.clickOnText(sim3GDisplayName);
        if (isExist3GServiceConfirmDialog()) {
            mSolo.clickOnButton(1);
        }
        mSolo.sleep(7000);
        int Capicity3GSlotId = GeminiUtils.get3GCapabilitySIM();
        updateNetworkModeEntriesFor3GSwitch();
        log("Capicity3GSlotId: " + Capicity3GSlotId);
        return Capicity3GSlotId == slotId;
    }

    private void updateNetworkModeEntriesFor3GSwitch() {
        if (!PhoneUtils.isSupportFeature("3G_SWITCH")) {
            log("Do not support 3g switch");
            return;
        }
        mSolo.sleep(1000);
        PreferenceActivity currentActivity = (PreferenceActivity) mSolo.getCurrentActivity();
        ListPreference mNetworkMode = (ListPreference) currentActivity.findPreference(NETWORK_MODE_KEY);
        if (!mNetworkMode.isEnabled()) {
            log("Network mode diaable.");
            return;
        }

        clickOnNetworkModePreference();

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

    private String getStringResource(String msg) {
        String resultString = mSolo.getString(msg);
        log("getString: " + msg + " --> " + resultString);
        return resultString;
    }

    private boolean isExist3GServiceString() {
        String clickString = getStringResource(ITEM_3G_SERVICE);
        if (mSolo.searchText(clickString)) {
            mSolo.clickOnText(clickString);
            return true;
        }
        return false;
    }

    private boolean isExist3GServiceConfirmDialog() {
        ArrayList<String> dialogStrings = new ArrayList<String>();
        dialogStrings.clear();
        for (String string : SERVICE_3G) {
            String digMsg = getStringResource(string);
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

    private boolean isNetworkModeItemAlreadyChecked(int index) {
        log("[isNetworkModeItemAlreadyChecked] index = " + index);
        mSolo.logCurrentTexts();
        ArrayList<CheckedTextView> networkModeItems = mSolo.getCurrentViews(CheckedTextView.class);
        return networkModeItems.get(index - 1).isChecked();
    }

    /**
     *
     * @param testNo the test count, for the first test, should be 1
     */
    public void changeNetworkModeForTest(int testNo) {
        if (PhoneUtils.isSupportFeature("3G_SWITCH")) {
            if (isExist3GServiceString()) {
                // the 2nd test is to test 2G only, the test step is different
                if (testNo == 2) {
                    close3GSwitch();
                    return;
                }
                boolean isSuccessful = open3GSwitch();
                Assert.assertEquals("[changeNetworkModeForTest]open 3G Switch preference failed", true, isSuccessful);
            }
        } else {
            if (!getNetworkModeSelections()) {
                log("[changeNetworkModeForTest]enter NetworkMode failed");
                mSolo.logCurrentTexts();
                return;
            }
        }
        int clickItemIndex = getNetworkModeIndex(testNo);
        selectNetworkMode(clickItemIndex);
    }

    private boolean clickOnNetworkModePreference() {
        // click "Network Mode" to "Network Mode"(sim card select)
        String networkModeString = null;
        String networkModeString1 = getStringResource(NETWORK_MODE_STRING1);
        String networkModeString2 = getStringResource(NETWORK_MODE_STRING2);
        log("networkModeString1: " + networkModeString1);
        log("networkModeString2: " + networkModeString2);
        if (mSolo.searchText(networkModeString1)) {
            networkModeString = networkModeString1;
        } else {
            log("Don't exist " + networkModeString1 + " Item");
        }

        if (mSolo.searchText(networkModeString2)) {
            networkModeString = networkModeString2;
        } else {
            log("Don't exist " + networkModeString2 + " Item");
        }

        log("the last chose networkModeString: " + networkModeString);
        if (networkModeString == null) {
            log("Network mode Item no exsit");
            return false;
        }

        mSolo.clickOnText(networkModeString);
        mInst.waitForIdleSync();
        SystemClock.sleep(1000);
        return true;
    }
}
