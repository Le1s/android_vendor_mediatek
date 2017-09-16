
package com.mtk.sanitytest.launchstktest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.mtk.autotest.Telephony.STK;
import android.util.Log;


public class launchSTK extends Activity {
    /** Called when the activity is first created. */
    private static final String TAG = "launchSTK";
    private STK stk = null;
    private Context mContext = null;
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "launchSTK onCreate");
        super.onCreate(savedInstanceState);
        mContext = this;
        stk = new STK(mContext);
        //stk.launchSTK2();
        //SystemClock.sleep(5000);
        //stk.launchSTK();
        /*
        String strTargetLoc = "com.android.stk";
        String strTargetClass = "com.android.stk.StkLauncherActivity";
        Intent intent = new Intent();
        intent.setClassName(strTargetLoc, strTargetClass);
        try {
            startActivity(intent);
        } catch(ActivityNotFoundException e) {
            Log.i(TAG, "ActivityNotFoundException happened");
        }
        */
   }
}

