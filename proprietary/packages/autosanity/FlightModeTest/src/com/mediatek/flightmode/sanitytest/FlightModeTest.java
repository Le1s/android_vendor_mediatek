package com.mediatek.flightmode.sanitytest;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.Settings;
import com.android.internal.telephony.ITelephony;
import com.jayway.android.robotium.solo.Solo;

public class FlightModeTest extends
        ActivityInstrumentationTestCase2<Settings.WirelessSettingsActivity> {

    private static final String TAG = "FlightModeTest";
    private static final int SIM_CARD_1 = 0;
    private static final int SIM_CARD_2 = 1;
    private static final int ONE_THOUSAND_MILLISECOND = 1000;
    private static final int TWO_THOUSANDS_MILLISECOND = 2000;
    private static final int FIVE_THOUSANDS_MILLISECOND = 5000;
    private Instrumentation mIns;
    private Activity mActivity;
    private Solo mSolo;
    private Context mContext;
    private static boolean mOriginalMode;
    private Switch mEnabler;
    private boolean mIsNeedSkip;

    private static final String FLIGHT_MODE_ON = android.provider.Settings.Global.AIRPLANE_MODE_ON;

    public FlightModeTest() {
        super("com.android.settings", Settings.WirelessSettingsActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mIns = getInstrumentation();
        mContext = mIns.getTargetContext();
        mActivity = getActivity();
        mSolo = new Solo(mIns, mActivity);

        assertNotNull(mIns);
        assertNotNull(mContext);
        assertNotNull(mActivity);
        assertNotNull(mSolo);

        mEnabler = getEnablerSwitch("airplane_mode");

        mIsNeedSkip = (mEnabler == null);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * test whether airplane mode can be enabled
     */
    public void test01_AirplaneModeEnable() {
        Log.d(TAG, "test01_AirplaneModeEnable()");
        if (mIsNeedSkip) {
            Log.d(TAG, "Can not find switch, solo exit.");
            return ;
        }
        // /M: Record the previous state
        mOriginalMode = isAirplaneModeOn();
        Log.d(TAG, "mOriginalMode = " + mOriginalMode);

        // /M: Set a new mode
        setNewMode(true);

        // /M: Check the set result.
        checkModeState();
    }

    /**
     * test whether airplane mode can be disabled
     */
    public void test02_AirplaneModeDisable() {
        Log.d(TAG, "test02_AirplaneModeDisable()");
        if (mIsNeedSkip) {
            Log.d(TAG, "Can not find switch, solo exit.");
            return ;
        }

        setNewMode(false);

        checkSimCardState();

        restoreOriginalMode();
    }

    private void checkSimCardState() {
        Log.d(TAG, "checkSimCardState()");

        ITelephony phoneMgr = ITelephony.Stub.asInterface(
                ServiceManager.getService(Context.TELEPHONY_SERVICE));
        int tempCount = 0;
        try {
            while (!phoneMgr.isRadioOn() && tempCount++ < 10) {
                mSolo.sleep(FIVE_THOUSANDS_MILLISECOND);
            }
            boolean isRadioOn = phoneMgr.isRadioOn();
            assertTrue("single card has ready  ", isRadioOn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void restoreOriginalMode() {
        boolean currentMode = isAirplaneModeOn();
        Log.d(TAG, "mOriginalMode : " + mOriginalMode + " currentMode : " + currentMode);
        if (currentMode != mOriginalMode) {
            Log.d(TAG, "Try to restore  original mode ");
            android.provider.Settings.Global.putInt(
                    mContext.getContentResolver(), FLIGHT_MODE_ON,
                    mOriginalMode ? 1 : 0);

            // Post the intent
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", mOriginalMode);
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void checkModeState() {
        Log.d(TAG, "checkModeState()");
        int dbMode = android.provider.Settings.Global.getInt(
                mContext.getContentResolver(), FLIGHT_MODE_ON, 0);

        boolean uiMode = mEnabler.isChecked();

        assertEquals("airplane mode is not compatiant between database and UI",
                dbMode, uiMode ? 1 : 0);
    }

    private void setNewMode(boolean onOff) {
        if (mEnabler.isChecked() != onOff) {
            mSolo.clickOnView(mEnabler);
            mSolo.sleep(ONE_THOUSAND_MILLISECOND);
            dealWithOp01Customization();
        }

        int tryCount = 0;
        while (!mEnabler.isEnabled() && tryCount++ < 10) {
            Log.d(TAG, " isEnabled() : " + mEnabler.isEnabled() + " tryCount : " + tryCount);
            mSolo.sleep(TWO_THOUSANDS_MILLISECOND);
        }
        assertTrue("After 10s wait, airplaneMode switch success ?  ",
                mEnabler.isEnabled());
    }

    private void dealWithOp01Customization() {
        // /M: fix op01 popup dialog
        if ("OP01".equals(android.os.SystemProperties.get("ro.operator.optr"))) {
            Log.d(TAG, "op01 load, we should dismiss popup dialog");
            mSolo.sleep(ONE_THOUSAND_MILLISECOND);
            if (!mActivity.hasWindowFocus()) {
                Log.d(TAG, "dismiss op01 dialog, go back");
                mSolo.goBack();
            }
        }
    }

    private boolean isAirplaneModeOn() {
        return android.provider.Settings.Global.getInt(
                mContext.getContentResolver(),
                android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private Switch getEnablerSwitch(String resName) {
        Switch enabler;
        int resId = mActivity.getResources().getIdentifier(resName, "string",
                "com.android.settings");
        String message = mSolo.getString(resId);
        mSolo.sleep(TWO_THOUSANDS_MILLISECOND);
        if (mSolo.searchText(message)) {
            TextView msg = (TextView) mSolo.getText(message);
            RelativeLayout p1 = (RelativeLayout) msg.getParent();
            LinearLayout root = (LinearLayout) p1.getParent();
            LinearLayout c1 = (LinearLayout) root.getChildAt(2);
            enabler = (Switch) c1.getChildAt(0);
        } else {
            enabler = null;
        }
        return enabler;
    }
}
