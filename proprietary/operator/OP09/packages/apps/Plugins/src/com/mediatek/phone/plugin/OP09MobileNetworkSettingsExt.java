package com.mediatek.phone.plugin;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.mediatek.op09.plugin.R;
import com.mediatek.phone.ext.DefaultMobileNetworkSettingsExt;

/**
 * REMOVE ALL,just only APN.
 */
public class OP09MobileNetworkSettingsExt extends DefaultMobileNetworkSettingsExt {

    private static final String TAG = "OP09MobileNetworkSettingsExt";
    private static final String CT_APN_KEY = "pref_ct_apn_key";
    private static final String APN_SETTINGS_PACKAGE = "com.android.settings";
    private static final String APN_SETTINGS_CLASS = "com.android.settings.ApnSettings";
    private static final String CT_PLUGIN_PACKAGE = "com.mediatek.op09.plugin";
    private static final String CT_PLUGIN_CLASS_MULTI_SIM =
        "com.mediatek.phone.plugin.MultipleSimActivity";

    private Context  mOP09Context;

    /**
     * Constructor method.
     * @param context Context
     */
    public OP09MobileNetworkSettingsExt(Context context) {
        mOP09Context = context;
    }

    @Override
    public void initPreference(PreferenceActivity activity, ICallback callback) {
        Log.d(TAG, "reloadPreference() OP09");
        PreferenceScreen prefScreen = activity.getPreferenceScreen();
        prefScreen.removeAll();

        PreferenceScreen prefAPN = new PreferenceScreen(prefScreen.getContext(), null);
        prefAPN.setKey(CT_APN_KEY);
        prefAPN.setTitle(mOP09Context.getResources().getText(R.string.apn_settings));
        prefAPN.setPersistent(false);
        Intent targetIntent = new Intent();
        targetIntent.setAction(Intent.ACTION_MAIN);
        targetIntent.setClassName(APN_SETTINGS_PACKAGE, APN_SETTINGS_CLASS);
        prefAPN.setIntent(targetIntent);
        prefScreen.addPreference(prefAPN);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (null != (PreferenceScreen) preferenceScreen.findPreference(CT_APN_KEY)
                && preference == (PreferenceScreen) preferenceScreen.findPreference(CT_APN_KEY)) {
            preference.getContext().startActivity(
                    getPreferenceClickIntent(preference, APN_SETTINGS_PACKAGE, APN_SETTINGS_CLASS));
            return true;
        }
        return false;
    }

    /**
     * When user click pref_ct_apn_key.
     * @param preference pref_ct_apn_key
     * @param packageName com.android.settings
     * @param className com.android.settings.ApnSettings
     * @return new intent
     */
    private Intent getPreferenceClickIntent(Preference preference,
            String packageName, String className) {
        Intent it = new Intent();
        it.setAction(Intent.ACTION_MAIN);
        it.setClassName(CT_PLUGIN_PACKAGE, CT_PLUGIN_CLASS_MULTI_SIM);
        it.putExtra(MultipleSimActivity.INIT_TITLE_NAME_STR, preference.getTitle());
        it.putExtra(MultipleSimActivity.TARGET_PACKAGE, packageName);
        it.putExtra(MultipleSimActivity.TARGET_CLASS, className);
        return it;
    }
}
