package com.mtk.fts;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class AutoTestHeartSetThread extends Thread {
	
	private Date mLastDate = null;
	private Context mContext = null;
    private boolean mUserStop = false;
	
	public AutoTestHeartSetThread(Context context){
		mContext = context;
	}
	
	public void userstop(){
        mUserStop = true;
	}

	public void startL() {
		// TODO Auto-generated method stub
		mUserStop = false;
        try {
				this.start();
			} catch (IllegalThreadStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		mUserStop = false;
		super.start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		Date curDate = new Date(System.currentTimeMillis());
		boolean willTimeout = false;
		while (!mUserStop){
			if ( mLastDate == null ){
				mLastDate = new Date(System.currentTimeMillis());
			}
			curDate.setTime(System.currentTimeMillis());
			int lastMin = mLastDate.getMinutes();
			int curMin = curDate.getMinutes();
			int de = curMin - lastMin;
			Log.i(FTestService.TAG, "AutoTestHeartSetThread will reboot(m) = "+de);
			if (de<0){ de = de + 60;}
			if ( de > 10 && !willTimeout ){
				willTimeout = true;
			}else{
				if ( de < 10 && willTimeout ){
					willTimeout = false;
				}
				if ( de > 16 && willTimeout){
					PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
					Log.i(FTestService.TAG, "AutoTestHeartSetThread will reboot right now");
					//pm.reboot("");
					willTimeout = false;
				}
			}
			try {
				Thread.sleep(1000*60);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        mUserStop = false;
	}
	
	public void updateLastTime(){
		if ( mLastDate == null ){
			mLastDate = new Date(System.currentTimeMillis());
		}else{
			mLastDate.setTime(System.currentTimeMillis());
		}
	}
	
	public void reboot(){
		try {
			Thread.sleep(1000*20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		Log.i(FTestService.TAG, "AutoTestHeartSetThread will reboot right now");
		//pm.reboot("");
	}

}
