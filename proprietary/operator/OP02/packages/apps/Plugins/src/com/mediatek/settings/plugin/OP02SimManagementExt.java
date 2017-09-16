package com.mediatek.settings.plugin;

import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.mediatek.common.PluginImpl;
import com.mediatek.settings.ext.DefaultSimManagementExt;

/**
 * remove 3GService feature.
 */
@PluginImpl(interfaceName = "com.mediatek.settings.ext.ISimManagementExt")
public class OP02SimManagementExt extends DefaultSimManagementExt {

    private static final String TAG = "OP02SimManagementExt";

    private static final String KEY_3G_SERVICE_SETTING = "3g_service_settings";

    /**
     * For CU feature,remove 3g switch feature.
     * @param parent 3g switch PreferenceScreen's parent
     */
    public void updateSimManagementPref(PreferenceGroup parent) {
        Log.d(TAG, "SimManagementExt---updateSimManagementPref()");
        PreferenceScreen pref3GService = null;
        if (parent != null) {
            pref3GService = (PreferenceScreen) parent.findPreference(KEY_3G_SERVICE_SETTING);
        }
        if (pref3GService != null) {
            boolean gemini3gSwitchSupport = Utils.is3GSwitchSupport();
            Log.d(TAG, "3g switch support=" + gemini3gSwitchSupport);
            if (!gemini3gSwitchSupport) {
                parent.removePreference(pref3GService);
            }
        }
    }
}
