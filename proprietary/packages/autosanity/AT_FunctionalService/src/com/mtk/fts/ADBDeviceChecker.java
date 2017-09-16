package com.mtk.fts;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.os.UEventObserver;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;

public class ADBDeviceChecker extends Thread {
	private Context mcx = null;
	private UsbManager mUm = null;
	private boolean mStopCheck = false;
	private static final String TAG = FTestService.TAG;
	private String mLastState = "CONFIGURED";
	private static final String USB_STATE_MATCH = "DEVPATH=/devices/virtual/android_usb/android0"; 
	private static final String ACCESSORY_START_MATCH = "DEVPATH=/devices/virtual/misc/usb_accessory"; 
	private final UEventObserver mUEventObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEventObserver.UEvent event) {
             Log.i(TAG, "USB UEVENT: " + event.toString());
             String state = event.get("USB_STATE");
             Log.i(TAG, "USB new state: " + state);
             mLastState = state;
        }
    };
    
	ADBDeviceChecker(Context context){
		mcx = context;
		mUm = (UsbManager) mcx.getSystemService(Context.USB_SERVICE);
	}
	
	public void startcheck(){
		this.start();
		mUEventObserver.startObserving(USB_STATE_MATCH);
		mUEventObserver.startObserving(ACCESSORY_START_MATCH);
	}
	
	public void stopcheck(){
		mStopCheck = true;
		this.stop();
		mUEventObserver.stopObserving();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
//		String state="CONFIGURED";
//		while(!mStopCheck){
////			Log.i(TAG, "USB last state: " + state);
////			Log.i(TAG, "USB state: " + mLastState);	
//			Intent intent = new Intent(ADBDeviceResetReceiver.STR_ResetADB);
//			intent.putExtra("OnlyEnable", true);
//			if (!mLastState.equals("CONFIGURED")){
//				mcx.sendBroadcast(intent);
//			}else{
//				boolean enable = (Settings.Secure.getInt(mcx.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
//				if (!enable){
//					mcx.sendBroadcast(intent);
//				}
//			}
//			state = mLastState;
//			try {
//				Thread.sleep(60000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	
}
