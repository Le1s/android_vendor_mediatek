package com.mediatek.phone.plugin.callsetting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.mediatek.op09.plugin.R;
import com.mediatek.phone.ext.DefaultCallSettingsExt;

/**
 * CT sim card call setting.
 */
public class OP09CallSettingsExt extends DefaultCallSettingsExt {

    private static final String TAG = "OP09CallSettingsExt";
    private static final String CDMA_CALL_OPTION_CLASS_NAME =
        "com.mediatek.phone.plugin.CdmaAdditionalCallOptions";

    private static final String BUTTON_CFU_KEY = "button_cfu_key";
    private static final String BUTTON_CFB_KEY = "button_cfb_key";
    private static final String BUTTON_CFNRY_KEY = "button_cfnry_key";
    private static final String BUTTON_CFNRC_KEY = "button_cfnrc_key";
    private static final String BUTTON_CFC_KEY = "button_cfc_key";

    private Context mContext;

    /**
     *
     * @param context get current context.
     */
    public OP09CallSettingsExt(Context context) {
        Log.i(TAG, "OP09CallSettingsExt: " + context.getPackageName());
        mContext = context;
    }

    @Override
    public void replaceCallSettingsActivity(Activity activity) {
        if (activity != null) {
            Intent intent = new Intent();
            intent.setClassName("com.mediatek.op09.plugin",
                    "com.mediatek.phone.plugin.callsetting.CallSettings");
            activity.startActivity(intent);
            activity.finish();
            Log.v(TAG, "OP09CallSettingsExt  activity current finish");
        }
    }

    @Override
    public void initCdmaCallForwardOptionsActivity(PreferenceActivity prefActivity) {
        Log.d(TAG, "OP09CallSettingsExt initPreferenceActivity");
        PreferenceScreen prefScreen = prefActivity.getPreferenceScreen();
        Preference buttonCFU = prefScreen.findPreference(BUTTON_CFU_KEY);
        Preference buttonCFB = prefScreen.findPreference(BUTTON_CFB_KEY);
        Preference buttonCFNRy = prefScreen.findPreference(BUTTON_CFNRY_KEY);
        Preference buttonCFNRc = prefScreen.findPreference(BUTTON_CFNRC_KEY);
        Preference buttonCFC = prefScreen.findPreference(BUTTON_CFC_KEY);

        if (buttonCFU != null) {
            buttonCFU.setTitle(mContext.getString(R.string.ct_labelCFU));
        }
        if (buttonCFB != null) {
            buttonCFB.setTitle(mContext.getString(R.string.ct_labelCFB));
        }
        if (buttonCFNRy != null) {
            buttonCFNRy.setTitle(mContext.getString(R.string.ct_labelCFNRy));
        }
        if (buttonCFNRc != null) {
            buttonCFNRc.setTitle(mContext.getString(R.string.ct_labelCFNRc));
        }
        if (buttonCFC != null) {
            buttonCFC.setTitle(mContext.getString(R.string.ct_labelCFC));
        }
    }
}
