package com.mtk.fts;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UnlockScreenReceiver extends BroadcastReceiver{
	private static final String TAG = FTestService.TAG;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "enter system autounlock receiver");
        Log.i(TAG, "Intent action = "+intent.getAction());
		String ac = "NULL";
		if (intent!=null){
			ac = intent.getAction();
		}
		if( ac.equals(Intent.ACTION_SEND)){
			context.sendBroadcast(new Intent(MobileUnlockReceiver.iA));
		}
	}

}
