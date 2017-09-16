package com.mediatek.op.telephony;

import android.content.Context;
import android.text.TextUtils;

import com.android.internal.telephony.PhoneConstants;

import com.mediatek.common.PluginImpl;

/**
 * Interface that defines methos which are implemented in IGsmDCTExt
 */

 /** {@hide} */
@PluginImpl(interfaceName="com.mediatek.common.telephony.IGsmDCTExt")
public class GsmDCTExtOP01 extends GsmDCTExt {
    private Context mContext;

    public GsmDCTExtOP01(Context context) {
    }

    public boolean isDataAllowedAsOff(String apnType) {
        if (TextUtils.equals(apnType, PhoneConstants.APN_TYPE_MMS)
                || TextUtils.equals(apnType, PhoneConstants.APN_TYPE_SUPL)
                || TextUtils.equals(apnType, PhoneConstants.APN_TYPE_IMS)
                || TextUtils.equals(apnType, PhoneConstants.APN_TYPE_EMERGENCY)) {
            return true;
        }
        return false;
    }

    public boolean getFDForceFlag(boolean force_flag) {
        return force_flag;
    }
}

