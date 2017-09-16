package com.mtk.fts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class ADBDeviceResetReceiver extends BroadcastReceiver {
	
	public static final String STR_ResetADB = "com.mtk.FunctionalTestService.ResetADB";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i(FTestService.TAG, "ADBDeviceResetReceiver onReceived action = "+intent.getAction());
		if ( intent!=null && intent.getAction().equals(STR_ResetADB)){
			if ( ! ( intent.getBooleanExtra("OnlyEnable", false))){
				Settings.Secure.putInt(context.getContentResolver(),
						Settings.Secure.ADB_ENABLED, 0);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.ADB_ENABLED, 1);
		}
	}

}
