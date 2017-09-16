package com.mtk.fts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.util.Log;

public class Log2ServerDialogDismissReceiver extends BroadcastReceiver {
	public static final String STR_Dismiss = "com.mtk.Log2serverreceiver.dismiss";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(STR_Dismiss)){
			Settings.System.putInt(context.getContentResolver(), 
				Settings.System.LOG2SERVER_DIALOG_SHOW, 0);
			final SharedPreferences preferences = context.getSharedPreferences(
					"taglog_switch", Context.MODE_PRIVATE);
			Log.d(FTestService.TAG, preferences.toString());
			int value = preferences.getInt("taglog_switch_key",10);
			Log.d(FTestService.TAG, ""+value);
			if (value!=10){
				final Editor edit = preferences.edit();
				Log.d(FTestService.TAG, edit.toString());
				edit.putInt("taglog_switch_key", 0);
	            edit.commit();
			}
		}
	}

}
