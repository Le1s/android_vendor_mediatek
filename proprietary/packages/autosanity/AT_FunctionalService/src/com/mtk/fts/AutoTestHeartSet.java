package com.mtk.fts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoTestHeartSet extends BroadcastReceiver {

	public final static String TAG = FTestService.TAG;
	public final static String STARTHEARTSET = "com.mtk.autotest.heartset.start";
	public final static String NORMALHEARTSET = "com.mtk.autotest.heartset.normal";
	public final static String STOPHEARTSET = "com.mtk.autotest.heartset.stop";
	public final static String REBOOTPHONE = "com.mtk.autotest.heartset.reboot";
	private AutoTestHeartSetThread mAT = null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (mAT==null){
			mAT = new AutoTestHeartSetThread(context);
		}
		if ( intent != null ){
			String action = intent.getAction();
			if(STARTHEARTSET.equals(action)){
				if (!mAT.isAlive()){
					mAT.startL();
				}
			}
			if(NORMALHEARTSET.equals(action)){
				if (!mAT.isAlive()){
					mAT.startL();
				}
				mAT.updateLastTime();
			}
			if(STOPHEARTSET.equals(action)){
				if (mAT.isAlive()){
					mAT.userstop();
				}
			}
			if(REBOOTPHONE.equals(action)){
				mAT.reboot();
			}
		}
	}

}
