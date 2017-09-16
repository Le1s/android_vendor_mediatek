package com.hesine.nmsg.observer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.hesine.nmsg.api.Activation;
import com.hesine.nmsg.common.GlobalData;
import com.hesine.nmsg.config.Config;
import com.hesine.nmsg.pn.PNControler;
import com.hesine.nmsg.util.DeviceInfo;

public class SimStateChangedReceiver extends BroadcastReceiver {
	public final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
	private final static int SIM_VALID = 0;
	private final static int SIM_INVALID = 1;
	private int simState = SIM_INVALID;
    public static SimStateChangedReceiver mInstance = null;

    public static SimStateChangedReceiver getInstance() {
        if (mInstance == null) {
            mInstance = new SimStateChangedReceiver();
        }
        return mInstance;
    }


	public int getSimState() {
		return simState;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Service.TELEPHONY_SERVICE);
			int state = tm.getSimState();
			switch (state) {
			case TelephonyManager.SIM_STATE_READY:
				simState = SIM_VALID;
				String imsi = DeviceInfo.getIMSI(context);
				GlobalData.instance().getSystemInfo().setImsi(imsi);
                if(!Config.getIsActivated()) {
                	Activation.instance().start();
                } else {
                	PNControler.startPN(context);
                }
				break;
			case TelephonyManager.SIM_STATE_UNKNOWN:
			case TelephonyManager.SIM_STATE_ABSENT:
			case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			default:
				simState = SIM_INVALID;
				break;
			}
		}
	}

}