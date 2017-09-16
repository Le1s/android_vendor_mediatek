package com.mtk.sanitytest.mocalltest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.content.pm.PackageManager;
import com.android.phone.PhoneUtils;

import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.test.InstrumentationTestCase;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;
import com.mediatek.telephony.TelephonyManagerEx;


/**
 * TODO: add test00_precondition
 */
public class MOCallTest extends InstrumentationTestCase {
    private static final String TAG = "MOCallTest";
    // private static final String CONFIG_FILE = "/sdcard/MOCallConfig.xml";
    private static final int SIM_CARD_1 = 0;
    private static final int SIM_CARD_2 = 1;
    private static final int MILLIS_IN_ONE_SECOND = 1000;
    private Instrumentation mInst = null;
    private NetworkModeSelectHelper networkModeSelectHelper = null;
    private PowerManager.WakeLock mWakeLock = null;

    private static final String CONFIG_FILE_NAME = "MOCallConfig.xml";
    private static final String NUMBER_SIM1_BEGIN = "<sim1>";
    private static final String NUMBER_SIM1_END = "</sim1>";
    private static final String NUMBER_SIM2_BEGIN = "<sim2>";
    private static final String NUMBER_SIM2_END = "</sim2>";
    private static final String MAX_TIMES_BEGIN = "<maxtimes>";
    private static final String MAX_TIMES_END = "</maxtimes>";
    private static final String PASS_TIMES_BEGIN = "<passtimes>";
    private static final String PASS_TIMES_END = "</passtimes>";
    private static final int TEST_COUNT = 1;
    private static final int CALL_DURATION_SECONDS = 10;

    private String mNumberSim1;
    private String mNumberSim2;
    private int mMaxTimes = 5;
    private int mPassTimes = 3;

    private static boolean sIsPreviousTestPass = true;
    private static String sVersion = "-1";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInst = getInstrumentation();
        networkModeSelectHelper = new NetworkModeSelectHelper(mInst);

        initConfig();
        lightTheScreen();
        networkModeSelectHelper.init();
    }

    private void lightTheScreen() {
        PowerManager pm = (PowerManager) mInst.getContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        KeyguardManager km = (KeyguardManager) mInst.getContext().getSystemService(Context.KEYGUARD_SERVICE);
        km.newKeyguardLock(TAG).disableKeyguard();
    }

    protected void tearDown() throws Exception {
        networkModeSelectHelper.clear();
        networkModeSelectHelper = null;
        if (mInst != null) {
            mInst = null;
        }
        mWakeLock.release();
        super.tearDown();
    }

    public void test01_Precondition() {
        log("---------test01_Precondition----------");

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

        log("waiting phone process restart and init for 1 min");
        SystemClock.sleep(60 * MILLIS_IN_ONE_SECOND);
        checkSimInsertStatus();

        log("test starts");
        sIsPreviousTestPass = true;
    }

    public void test02_MOCallWithCU3G() {
        log("---------test02_MOCallWithCU3G() begin-------- version: " + sVersion);

        assertTrue("[test02_MOCallWithCU3G]previous test failed, skip current test, version: " + sVersion, sIsPreviousTestPass);
        sIsPreviousTestPass = false;

        networkModeSelectHelper.changeNetworkModeForTest(1);
        SystemClock.sleep(500);
        doMOCall();
        mInst.waitForIdleSync();
        sendHomeKey();

        sIsPreviousTestPass = true;
    }

    private void sendHomeKey() {
        mInst.sendCharacterSync(KeyEvent.KEYCODE_HOME);
        SystemClock.sleep(500);
    }

    public void test03_MOCallWithCU2G() {
        log("---------test03_MOCallWithCU2G() begin-------- version: " + sVersion);

        assertTrue("[test03_MOCallWithCU2G]previous test failed, skip current test, version: " + sVersion, sIsPreviousTestPass);
        sIsPreviousTestPass = false;

        networkModeSelectHelper.changeNetworkModeForTest(2);
        SystemClock.sleep(500);
        doMOCall();
        mInst.waitForIdleSync();
        sendHomeKey();

        sIsPreviousTestPass = true;
    }

    private void doMOCall() {
        long sim1Id = -1;
        long sim2Id = -1;

        for (int i = 0; i < TEST_COUNT; i++) {
            int iSuceedCount = 0;

            SystemClock.sleep(5000);
            Intent intent_cu = new Intent(Intent.ACTION_CALL);
            intent_cu.setData(Uri.parse("tel://" + mNumberSim1));
            intent_cu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


            sim1Id = getSimIdBySlot(SIM_CARD_1);
            Settings.System.putLong(mInst.getTargetContext().getContentResolver(), Settings.System.VOICE_CALL_SIM_SETTING,
                    sim1Id);

            // call with sim1 for 3 times
            for (i = 0; i < mMaxTimes; i++) {
                log("Mo with sim1, time: " + i);
                if (callWithSpecifiedSIM(intent_cu, SIM_CARD_1, CALL_DURATION_SECONDS)) {

                    Log.e(TAG, "call succes from SIM1");
                    iSuceedCount++;
                }
                SystemClock.sleep(5000);
                if (iSuceedCount >= mPassTimes) {
                    break;
                }
            }
            assertTrue("call fail about sim1", iSuceedCount >= mPassTimes);
            if (GeminiUtils.isGeminiSupport()) {
                iSuceedCount = 0;
                Intent intent_td = new Intent(Intent.ACTION_CALL);
                intent_td.setData(Uri.parse("tel://" + mNumberSim2));
                intent_td.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                sim2Id = getSimIdBySlot(SIM_CARD_2);
                Settings.System.putLong(mInst.getTargetContext().getContentResolver(), Settings.System.VOICE_CALL_SIM_SETTING,
                        sim2Id);

                // call with sim2 for 3 times
                for (int j = 0; j < mMaxTimes; j++) {
                    log("Mo with sim2, time: " + j);
                    if (callWithSpecifiedSIM(intent_td, SIM_CARD_2, CALL_DURATION_SECONDS)) {
                        Log.e(TAG, "call success from SIM2");
                        iSuceedCount++;
                    }
                    if (iSuceedCount >= mPassTimes) {
                        break;
                    }
                    SystemClock.sleep(5000);
                }
                assertTrue("call fail about sim1", iSuceedCount >= mPassTimes);
            }
        }
        Log.i(TAG, "test end!");
    }

    private boolean callWithSpecifiedSIM(Intent intent, int slotId, int callTimeInSecond) {
        boolean result = false;
        //mInst.getContext().startActivity(i);
        if (!networkModeSelectHelper.isAllStateInService()) {
            fail(" wait " + NetworkModeSelectHelper.CONFIG_WAIT_SECONDS + " seconds, no service, cannot dial " +
                    "call from  slotId: " + slotId + ", " +
                    "PhoneInterfaceManager.getServiceState() != ServiceState.STATE_IN_SERVICE");
            return false;
        }
        mInst.getTargetContext().startActivity(intent);
        //SystemClock.sleep(time);
        log("[callWithSpecifiedSIM]call from slot: " + slotId);

        int callState_MOCall = TelephonyManager.CALL_STATE_IDLE;
        // wait till MO succeeded
        for (int j = 0; j < 10; ++j) {
            callState_MOCall = getCallStatus(slotId);
            log("[callWithSpecifiedSIM]start MO for " + j + " seconds, call status is " + callState_MOCall);
            if (TelephonyManager.CALL_STATE_OFFHOOK == callState_MOCall) {
                log("[callWithSpecifiedSIM]MO Call succeeded");
                break;
            }
            SystemClock.sleep(MILLIS_IN_ONE_SECOND);
        }

        if (TelephonyManager.CALL_STATE_OFFHOOK != callState_MOCall) {
            log("[callWithSpecifiedSIM]failed to MO");
            return false;
        }

        // keep call for several seconds.
        SystemClock.sleep(MILLIS_IN_ONE_SECOND * callTimeInSecond);

        Log.i(TAG, "Hang up!");

        boolean isCallEnd = endCall(slotId);
        Log.i(TAG, "isCallEnd = " + isCallEnd);
        if ((TelephonyManager.CALL_STATE_OFFHOOK == callState_MOCall) && isCallEnd) {
            result = true;
        }
        return result;
    }

    private int getCallStatus(int sim_card) {
        int callState_MOCall = TelephonyManager.CALL_STATE_IDLE;
        if (GeminiUtils.isGeminiSupport() && GeminiUtils.isValidSlot(sim_card)) {
            callState_MOCall = TelephonyManagerEx.getDefault().getCallState(sim_card);
        } else {
            callState_MOCall = TelephonyManager.getDefault().getCallState();
        }
        return callState_MOCall;
    }

    private long getSimIdBySlot(int slotId) {
        SimInfoRecord simInfoRecord = SimInfoManager.getSimInfoBySlot(mInst.getTargetContext(), slotId);
        assertNotNull("[getSimIdBySlot]get SimInfo failed for slot: " + slotId, simInfoRecord);
        long simId = simInfoRecord.mSimInfoId;
        log("getSimIdBySlot(). simId / slotId: " + simId + " / " + slotId);
        assertTrue("simId should not be -1.", simId != -1);
        return simId;
    }

    private boolean endCall(int simId) {
        boolean result = false;
        PhoneUtils.hangupAllCalls();
        SystemClock.sleep(5000);

        int callStatus = getCallStatus(simId);
        if (TelephonyManager.CALL_STATE_IDLE == callStatus) {
            Log.i(TAG, "callState is " + callStatus);
            result = true;
        }
        return result;
    }

    private void checkSimInsertStatus() {
        log("[checkSimInsertStatus]should insert " + GeminiUtils.getSlotCount() + " SIM(s)");
        for (int slotId : GeminiUtils.getSlots()) {
            boolean isSimInsert = TelephonyManagerEx.getDefault().hasIccCard(slotId);
            log("[checkSimInsertStatus]checking slot " + slotId + " insert? " + isSimInsert);
            assertTrue("[checkSimInsertStatus]sim not found in slot " + slotId, isSimInsert);
        }
    }

    private void initConfig() {
        File dir = Environment.getExternalStorageDirectory();
        log("dir = " + dir);
        if (dir == null) {
            return;
        }

        File file = findFileUnderCertainPath(dir);
        if (file == null) {
            log("Can't find MOCallConfig.xml directly under " + dir + ". Try to find it in all files.");
            file = findConfigFile(dir);
        }

        if (file == null) {
            log("Can't find config file (MOCallConfig.xml)");
            assertTrue("Can't find config file (MOCallConfig.xml).", false);
            return;
        }
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String str;
            int index1;
            int index2;
            while ((str = br.readLine()) != null) {
                if (str.contains(NUMBER_SIM1_BEGIN)) {
                    index1 = str.indexOf(NUMBER_SIM1_BEGIN);
                    index2 = str.indexOf(NUMBER_SIM1_END);
                    mNumberSim1 = str.substring(index1 + NUMBER_SIM1_BEGIN.length(), index2);
                    assertTrue("sim1 nubmer is null", !TextUtils.isEmpty(mNumberSim1));
                    mNumberSim1 = mNumberSim1.trim();
                }
                if (str.contains(NUMBER_SIM2_BEGIN)) {
                    index1 = str.indexOf(NUMBER_SIM2_BEGIN);
                    index2 = str.indexOf(NUMBER_SIM2_END);
                    mNumberSim2 = str.substring(index1 + NUMBER_SIM2_BEGIN.length(), index2);
                    assertTrue("sim1 nubmer is null", !TextUtils.isEmpty(mNumberSim2));
                    mNumberSim2 = mNumberSim2.trim();
                }
                if (str.contains(MAX_TIMES_BEGIN)) {
                    index1 = str.indexOf(MAX_TIMES_BEGIN);
                    index2 = str.indexOf(MAX_TIMES_END);
                    String maxTimes = str.substring(index1 + MAX_TIMES_BEGIN.length(), index2);
                    assertTrue("maxtimes tag is null", !TextUtils.isEmpty(maxTimes));
                    mMaxTimes = Integer.parseInt(maxTimes.trim());
                }
                if (str.contains(PASS_TIMES_BEGIN)) {
                    index1 = str.indexOf(PASS_TIMES_BEGIN);
                    index2 = str.indexOf(PASS_TIMES_END);
                    String passTimes = str.substring(index1 + PASS_TIMES_BEGIN.length(), index2);
                    assertTrue("maxtimes tag is null", !TextUtils.isEmpty(passTimes));
                    mPassTimes = Integer.parseInt(passTimes);
                }
            }

            fr.close();
            br.close();
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
        log("mNumberSim1/mNumberSim2/maxtimes/passtimes : " + mNumberSim1 + "/" + mNumberSim2 + "/" + mMaxTimes + "/" + mPassTimes);
        assertTrue("Can't find sim1 number from config file.", !TextUtils.isEmpty(mNumberSim1));
        if (GeminiUtils.isGeminiSupport()) {
            assertTrue("Can't find sim2 number from config file.", !TextUtils.isEmpty(mNumberSim2));
        }
    }

    private File findFileUnderCertainPath(File dir) {
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            log("[findFileUnderCertainPath]failed to get file list");
            return null;
        }
        log("dir: " + dir + "; number of files: " + files.length);
        for (File file : files) {
            if (!file.isDirectory() && file.getName().equals(CONFIG_FILE_NAME)) {
                return file;
            }
        }
        return null;
    }

    private File findConfigFile(File dir) {
        File resultFile = null;
        File[] files = dir.listFiles();
        if (files == null | files.length == 0) {
            return resultFile;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                resultFile = findConfigFile(file);
            } else if (file.getName().equals(CONFIG_FILE_NAME)) {
                resultFile = file;
            }
        }
        return resultFile;
    }

    private void log(String string) {
        Log.i(TAG, string);
    }

}
