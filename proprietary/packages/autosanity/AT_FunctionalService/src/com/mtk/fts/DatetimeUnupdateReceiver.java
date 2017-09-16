package com.mtk.fts;

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

public class DatetimeUnupdateReceiver extends BroadcastReceiver {
	public static final String intentAction = "com.mediatek.DatetimeUnupdateReceiver";
	private static final String TAG = FTestService.TAG;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "enter mtk Datetime unupdate receiver");
		Log.i(TAG, "Intent action = " + intent.getAction());
		if (intent.getAction().equals(intentAction)) {
			final ContentResolver cr = context.getContentResolver();
            boolean result = Settings.Global.putInt(cr, Settings.Global.AUTO_TIME,0);
			Log.i(TAG, "Settings.System.putInt result == " + result);
            result = Settings.Global.putInt(cr, Settings.System.AUTO_TIME_GPS,0);
			Log.i(TAG, "Settings.System.putInt result == " + result);
		}
	}
}
