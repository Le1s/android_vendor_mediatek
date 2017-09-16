package com.mediatek.keyguard.test;

import com.jayway.android.robotium.solo.Solo;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.test.ActivityInstrumentationTestCase2;
import android.content.BroadcastReceiver;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;

//import android.telephony.TelephonyManager;

import android.provider.Settings;

public class KeyguardSanityTest extends ActivityInstrumentationTestCase2<KeyguardTestActivity2> {
    private static final String TAG = "KeyguardSanityTest";

    private KeyguardTestActivity2 mActivity;
    private Solo mSolo;
    private int mTriggered;
    private int mTriggerResId;

    private CharSequence mTelephonyPlmn[];

    public KeyguardSanityTest() {
        super(KeyguardTestActivity2.class);
        Log.v(TAG, "constructed this = " + this);

    }

    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        mActivity = getActivity();
        Log.v(TAG, "get activity=" + mActivity);
        mSolo = new Solo(getInstrumentation(), mActivity);
        Log.v(TAG, "get mSolo=" + mSolo);

        mTelephonyPlmn = new CharSequence[getNumOfPhone()];
        Log.v(TAG, "get getNumOfPhone =" + getNumOfPhone());

        for (int i = 0; i < getNumOfPhone(); i++) {
            mTelephonyPlmn[i] = getDefaultPlmn();
        }

        // Watch for interesting updates
        final IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.SPN_STRINGS_UPDATED_ACTION);
        mActivity.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        try {
            mActivity.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
        }
        super.tearDown();

    }

    private void resetAirplaneMode() {
        for (int i = 1; i >= 0; i--) {
            Settings.Global.putInt(
                    mActivity.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON,
                    i);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.putExtra("state", (i == 1) ? true : false);
            mActivity.sendBroadcast(intent);
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
            }
        }
        try {
            Thread.sleep(90000);
        } catch (Exception e) {
        }
    }

   private CharSequence getTelephonyPlmnFrom(Intent intent) {
       if (intent.getBooleanExtra(TelephonyIntents.EXTRA_SHOW_PLMN, false)) {
           Log.d(TAG, "EXTRA_SHOW_PLMN =  TRUE ");
           final String plmn = intent.getStringExtra(TelephonyIntents.EXTRA_PLMN);
           return (plmn != null) ? plmn : getDefaultPlmn();
       }
       if (intent.getBooleanExtra(TelephonyIntents.EXTRA_SHOW_SPN, false)) {
           Log.d(TAG, "EXTRA_SHOW_SPN =  TRUE ");
           final String spn = intent.getStringExtra(TelephonyIntents.EXTRA_SPN);
           if (spn != null) {
               return spn;
           }
       }
       Log.d(TAG, "Neither plmn nor spn are shown");
       return getDefaultPlmn();
   }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TelephonyIntents.SPN_STRINGS_UPDATED_ACTION.equals(action)) {
                int slotId = 0;
                // fixMe: here should get subId then change to phoneId
                //long subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, 0);
                slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY, 0);

                mTelephonyPlmn[slotId] = getTelephonyPlmnFrom(intent);
                Log.d(TAG, "SPN_STRINGS_UPDATED_ACTION, update slotId = " + slotId + " , plmn=" + mTelephonyPlmn[slotId]);
            }
        }
    };

    static int getNumOfPhone() {
        int phoneCount = 1;
            /// fixMe: here should to get phone count
        phoneCount = TelephonyManager.getDefault().getPhoneCount();

        return phoneCount;
    }

    private CharSequence getDefaultPlmn() {
        return mActivity.getResources().getText(com.android.internal.R.string.lockscreen_carrier_default);
    }

    public void test00CarrierText() {
        /// M: this test case verify Carrier text1 and carrier text2 both not equals to default plmen "no service"
        /// Before this test, please verify SIM1 and SIM2 are both inserted, enabled and not in airplane mode.
        /// This test cases may fail if it's run immediately after phone rebooted, because plmn may still get null.

        Log.v(TAG, "test00CarrierText - test KeyguardSanityTest CarrierText");

        resetAirplaneMode();

        String defaultPlmn = getDefaultPlmn().toString().toUpperCase();
        Log.v(TAG, "defaultPlmn =" + defaultPlmn);

        boolean bText1ContainDefault = mTelephonyPlmn[0].toString().toUpperCase().contains(defaultPlmn);
        Log.v(TAG, "CarrierText1 =" + mTelephonyPlmn[0]);
        assertFalse("CarrierText1 contains " + defaultPlmn, bText1ContainDefault);

/*  todo: temporary only test sim card 1
       if (getNumOfPhone()>=2) {
           boolean bText2ContainDefault = mTelephonyPlmn[1].toString().toUpperCase().contains(defaultPlmn);
           Log.v(TAG, "CarrierText2 =" + mTelephonyPlmn[1]);
           assertFalse("CarrierText2 contains "+defaultPlmn, bText2ContainDefault );
        }
        */

    }

}
