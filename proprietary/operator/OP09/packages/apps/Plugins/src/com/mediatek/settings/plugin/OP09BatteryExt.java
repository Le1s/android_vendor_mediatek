package com.mediatek.settings.plugin;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.mediatek.settings.ext.DefaultBatteryExt;

/**
 * Phase out.
 * Beijing Tianyi power saving mode feature has conflict with mtk common power saving feture.
 * We phase out this OP09 feature,
 * and if customer want to enable it,
 * just let BatteryExt extend DefaultBatteryExt.
 */
public class OP09BatteryExt extends DefaultBatteryExt {

    private static final String TAG = "OP09BatteryExt";
    private Context mContext;
    private static final String KEY_CPU_SAVING = "cpu_dtm";

    /**
     * Constructor method.
     * @param context is Settings's context.
     */
    public OP09BatteryExt(Context context) {
        Log.e(TAG, "OP09BatteryExt - new BatteryExt(context)" + context.getPackageName());
        mContext = context;
    }

    @Override
    public void loadPreference(Context context, PreferenceGroup listGroup) {
        Log.e(TAG, "loadPreference()");
        PowerSavingPreference preference = new PowerSavingPreference(mContext);
        /** Remove common feature CPU Saving mode */
        Preference cpuSaving = listGroup.findPreference(KEY_CPU_SAVING);
        if (cpuSaving != null) {
            listGroup.removePreference(cpuSaving);
        }
        preference.setOrder(-4);
        listGroup.addPreference(preference);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return preference instanceof PowerSavingPreference;
    }
}
