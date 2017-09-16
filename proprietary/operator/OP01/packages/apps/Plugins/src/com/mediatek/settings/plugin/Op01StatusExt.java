package com.mediatek.settings.plugin;

import android.content.IntentFilter;
import android.content.Context;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.mediatek.common.PluginImpl;
import com.mediatek.op01.plugin.R;
import com.mediatek.settings.ext.DefaultStatusExt;

@PluginImpl(interfaceName="com.mediatek.settings.ext.IStatusExt")
public class Op01StatusExt extends DefaultStatusExt {
    private Context mContext;
    
    public Op01StatusExt(Context context) {
        super();
        mContext = context;
    }
   
    public void updateOpNameFromRec(Preference p, String name) {
        p.setSummary(name);
    }
    
    public void updateServiceState(Preference p, String name) {
        
    }
    public void addAction(IntentFilter intent, String action) {
        intent.addAction(action);
    }

    public void customizeGeminiImei(String imeiKey, String imeiSvKey, PreferenceScreen parent, int slotId) {
        if (SystemProperties.get("ro.mtk_single_imei").equals("1")) {
            Preference imei = parent.findPreference(imeiKey); 
            Preference imeiSv = parent.findPreference(imeiSvKey);
            if (slotId == 0) {
                imei.setTitle(mContext.getString(R.string.status_imei));
                imeiSv.setTitle(mContext.getString(R.string.status_imei_sv));
            } else {
                parent.removePreference(imei);
                parent.removePreference(imeiSv);
            }
        }
    }
}
