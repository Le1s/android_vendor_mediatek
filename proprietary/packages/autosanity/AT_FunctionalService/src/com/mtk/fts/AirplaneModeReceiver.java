package com.mtk.fts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.provider.Settings;
import android.content.ContentResolver;
import android.os.BatteryManager;
import android.os.UserHandle;

public class AirplaneModeReceiver extends BroadcastReceiver {
	public static final String turnOn_intentAction = "com.mediatek.turnOnAirplaneMode";
	public static final String turnOff_intentAction = "com.mediatek.turnOffAirplaneMode";
	private static final String TAG = FTestService.TAG;
	@Override
	public void onReceive(Context context, Intent intent) {
         Log.i(TAG, "enter mtk AirplaneMode receiver");
         Log.i(TAG, "Intent action = "+intent.getAction());
         final ContentResolver cr = context.getContentResolver();
	     if(intent.getAction().equals(turnOn_intentAction)){
          	Settings.Global.putInt(cr, Settings.System.AIRPLANE_MODE_ON, 1);
			Intent intent1 = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        	intent1.putExtra("state", 1);
        	context.sendBroadcastAsUser(intent1, UserHandle.ALL);	      
	      }else if(intent.getAction().equals(turnOff_intentAction)){	      	
	      	Settings.Global.putInt(cr, Settings.System.AIRPLANE_MODE_ON, 0);
        	Intent intent2 = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        	intent2.putExtra("state", 0);
        	context.sendBroadcastAsUser(intent2, UserHandle.ALL);
 		}
	}
}
