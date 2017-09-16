package com.mediatek.settings.plugin;

import android.util.Log;

import com.mediatek.settings.ext.DefaultStatusGeminiExt;

/**
 * Cusotmize network type name.
 */
public class OP09StatusGeminiImp extends DefaultStatusGeminiExt {

    private static final String TAG = "StatusGeminiImp";

    /**
     * Constructor method.
     */
    public OP09StatusGeminiImp() {
    }

    /**
     * Cusotmize network type name, it will be called when update Network Type.
     * @param netWorkTypeName which name will be replace
     * @return new netWorkTypeName
     */
    @Override
    public String customizeNetworkTypeName(String netWorkTypeName) {
        Log.d(TAG, "reNameNetworkTypeNameForCTSpec netWorkTypeName="
                + netWorkTypeName);
        if (null != netWorkTypeName) {
            return CurrentNetworkInfoStatus.renameNetworkTypeNameForCTSpec(netWorkTypeName);
        } else {
            return null;
        }
    }
}
