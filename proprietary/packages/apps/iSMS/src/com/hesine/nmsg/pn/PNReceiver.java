package com.hesine.nmsg.pn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.hesine.nmsg.Application;
import com.hesine.nmsg.api.Upgrade;
import com.hesine.nmsg.common.GlobalData;
import com.hesine.nmsg.config.Config;
import com.hesine.nmsg.util.DeviceInfo;
import com.hesine.nmsg.util.MLog;
import com.hissage.hpe.SDK;

public class PNReceiver extends BroadcastReceiver {

    public static final String action_registration = "com.hpns.android.intent.REGISTRATION";
    public static final String action_receive = "com.hpns.android.intent.RECEIVE";
    public static final String action_unregister = "com.hpns.android.intent.UNREGISTER";
    public static final String action_reconnect = "com.hpns.android.intent.RECONNECT";
    public static final String action_reg_change = "com.hpns.android.intent.REGIDCHANGED";

    private static final String TAG = "PNReceiver";

    public static final int HPNS_CODE_SUCCESS = 0;
    private static int NMSG_REGISTER_TIMES = 1;

    private Handler mHandler = new Handler();    

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            PNControler.startPN(Application.getInstance().getApplicationContext());
        }
    };
    
    public void onReceive(Context context, Intent intent) {
        String receiveAction = intent.getAction();
        MLog.trace(TAG, "onReceive action: " + receiveAction);
        if (receiveAction.equals(action_registration)) {
            handleRegistration(context, intent);
        } else if (receiveAction.equals(action_unregister)) {
            handleUnRegistration(context, intent);
        } else if (receiveAction.equals(action_receive)) {
            handleNewMessage(context, intent);
        } else if (receiveAction.equals(action_reconnect)) {
            handleReconnect(context, intent);
        } else if(receiveAction.equals(action_reg_change)){
            handleRegistration(context, intent); 
        }
    }
    
    private void handleRegistration(Context context, Intent intent) {
        String regId = intent.getStringExtra("registration_id");
        int code = intent.getIntExtra("code", 0);
        MLog.trace(TAG, "handleRegistration regId: " + regId + "code: " + code);
        if (HPNS_CODE_SUCCESS == code && regId != null && regId.length() > 0) {
            NMSG_REGISTER_TIMES = 1;
			String oldImsi = Config.getImsi();
			String nowImsi = GlobalData.instance().getSystemInfo().getImsi();
			boolean imsiChanged = (null != oldImsi && !oldImsi.equals(nowImsi));
            String oldRegId = Config.getPnToken();
            if(oldRegId == null || 
            	(oldRegId != null && (!oldRegId.equals(regId))) || !Config.getUploadPNTokenFlag() || imsiChanged) {
            	Config.savePnToken(regId);
            	GlobalData.instance().getSystemInfo().setPnToken(regId);
	            PNControler.postPNToken();
            }
            long lastUpdateTime = Config.getLastUpgradeTime();
            String cV = GlobalData.instance().getClientInfo().getVersion();
            String nv = Config.getNewVersion();
            if(nv != null && lastUpdateTime > Upgrade.YEAR_ABOUT_2010) {
            	long curTime = System.currentTimeMillis();
            	long offTime = curTime - lastUpdateTime;
            	if(Upgrade.compareVersion(nv, cV) > 0 && offTime > Upgrade.ONE_MONTH) {
            		String url = Config.getNewClientUrl();
    	        	Config.saveLastUpgradeTime(System.currentTimeMillis());
    	        	Upgrade.downApp(url);
            	}
            } else {
            	Upgrade upgrade = new Upgrade();
            	upgrade.start();
            }
        }else{
            MLog.error(TAG, "fatal error in HPNS for handleRegitration is failed, code:"
                    + code + " times:" + NMSG_REGISTER_TIMES);
            if(DeviceInfo.isNetworkReady(context)){
                mHandler.postDelayed(runnable, 60*1000*NMSG_REGISTER_TIMES);
                NMSG_REGISTER_TIMES = NMSG_REGISTER_TIMES * 2;
            }else{
                MLog.error(TAG, "Network Not Available");
            }
        }
    }

    private void handleUnRegistration(Context context, Intent intent) {
        Config.savePnToken("");
    }

    private void handleNewMessage(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        MLog.trace(TAG, "handleNewMessage message: " + message);
        try {
            PNControler.handlePNCommand(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleReconnect(Context context, Intent intent) {
        try {
            SDK.onRegister(context);
        } catch (Exception e) {
        }
    }
    
}