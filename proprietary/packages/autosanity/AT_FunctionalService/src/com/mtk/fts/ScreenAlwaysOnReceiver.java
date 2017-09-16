package com.mtk.fts;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.provider.Settings;

import android.content.ContentResolver;
import android.os.BatteryManager;
import android.os.PowerManager;

public class ScreenAlwaysOnReceiver extends BroadcastReceiver {
	public static final String intentAction = "com.mediatek.ScreenAlwaysOnReceiver";
	private static final String TAG = FTestService.TAG;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "enter mtk ScreenAlwaysOn receiver");
		Log.i(TAG, "Intent action = " + intent.getAction());
		if (intent.getAction().equals(intentAction)) {
			final ContentResolver cr = context.getContentResolver();

			boolean result = Settings.Global.putInt(cr,
							Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
							(BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB));
			Log.i(TAG, "Settings.System.putInt STAY_ON_WHILE_PLUGGED result == " + result);
            result = Settings.System.putInt(cr, SCREEN_OFF_TIMEOUT, 30*60*1000);
            Log.i(TAG, "Settings.System.putInt SCREEN_OFF_TIMEOUT result == " + result);
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!pm.isScreenOn()){
            	makeScreenOn();
            }
            //PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,intentAction);
            //wl.acquire();
		}
	}
	private void makeScreenOn(){
		new Thread(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				Instrumentation inst = new Instrumentation();
				inst.sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
			}
		}).start();
		
	}
}
