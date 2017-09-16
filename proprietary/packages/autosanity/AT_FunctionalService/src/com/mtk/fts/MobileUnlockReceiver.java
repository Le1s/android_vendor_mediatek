package com.mtk.fts;

import com.android.internal.widget.LockPatternUtils;

import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.os.PowerManager;

public class MobileUnlockReceiver extends BroadcastReceiver {
	public static final String iA = "com.mediatek.autounlock";
	private static final String TAG = FTestService.TAG;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
         Log.i(TAG, "enter mobile mtk autounlock receiver");
         Log.i(TAG, "Intent action = "+intent.getAction());
		if(intent.getAction().equals(iA)){
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!pm.isScreenOn()){
            	makeScreenOn();
            }
            //PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,iA);
            //wl.acquire();
            Log.i(TAG, "Screen will wake lock.");
			KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
			Log.i(TAG, "km "+km.toString());
			KeyguardLock kl = km.newKeyguardLock(iA);
			Log.i(TAG, "kl "+kl.toString());
			kl.reenableKeyguard();
			kl.disableKeyguard();
			LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
			lockPatternUtils.clearLock(false);
			lockPatternUtils.setLockScreenDisabled(true);
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
