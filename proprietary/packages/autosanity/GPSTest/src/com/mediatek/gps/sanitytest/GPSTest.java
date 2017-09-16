package com.mediatek.gps.sanitytest;

import android.app.Activity;
import android.app.Instrumentation;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.provider.Settings;
import android.location.LocationManager;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;
import com.mediatek.ygps.R;
import com.mediatek.ygps.YgpsActivity;

public class GPSTest extends
        ActivityInstrumentationTestCase2<YgpsActivity> {

    private static final String TAG = "GPSTestTag";
    private static final int TIME_SHORT = 500;
    private static final int TIME_MID = 1000;
    private static final int TIME_LONG = 3000;

    private Solo mSolo = null;
    private Activity mActivity = null;
    private Context mContext = null;
    private Instrumentation mInst = null;

    public GPSTest() {
        super(YgpsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mInst = getInstrumentation();
        mContext = mInst.getTargetContext();
        mActivity = getActivity();
        mSolo = new Solo(mInst, mActivity);
        Settings.Secure.setLocationProviderEnabled(mActivity.getContentResolver(), LocationManager.GPS_PROVIDER, true);
        mSolo.sleep(TIME_MID);
        String version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        Log.i(TAG, "[GPSTest] version name:" + version);
    }

    public void test01() {  // check restart button
        mSolo.sleep(TIME_LONG);
        mSolo.clickOnText(mActivity.getString(R.string.information));

        mSolo.sleep(TIME_MID);

        String mProvider = String.format(mActivity.getString(
                            R.string.provider_status_enabled,
                            LocationManager.GPS_PROVIDER));
        Log.i(TAG, mProvider);
        if (mSolo.searchText(mProvider)) {
            Log.i(TAG, "[GPSTest]provider enabled");
        } else {
            assertTrue(false);
            return;
        }
        String started = mActivity.getString(R.string.gps_status_started);
        String unavailabled = mActivity.getString(R.string.gps_status_unavailable);
        String availabled = mActivity.getString(R.string.gps_status_available);
        Log.i(TAG, started);
        Log.i(TAG, unavailabled);
        Log.i(TAG, availabled);
        String strUnknown = mActivity.getString(R.string.gps_status_unknown);
        String targetStatus = getTextFromTextView(R.id.tv_status);
        if (strUnknown.equals(targetStatus)) {
            mSolo.sleep(TIME_MID * 5);
        }
        targetStatus = getTextFromTextView(R.id.tv_status);
        Log.i(TAG, "tv_status:" + targetStatus);
        if (targetStatus.equals(started)) {
            Log.i(TAG, "[GPSTest] status: started");
        } else if (targetStatus.equals(unavailabled)) {
            Log.i(TAG, "[GPSText] status: unavailabed");
        } else if (targetStatus.equals(availabled)) {
            Log.i(TAG, "[GSPText] status: availabled");
        } else {
            assertTrue(false);
            return;
        }
    }

    private String getTextFromTextView(int resId) {
            TextView tv = (TextView) mActivity.findViewById(resId);
            return tv.getText().toString();
    }
}
