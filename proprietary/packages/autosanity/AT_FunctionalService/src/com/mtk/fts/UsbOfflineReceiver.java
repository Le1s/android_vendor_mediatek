package com.mtk.fts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.hardware.usb.UsbManager;

public class UsbOfflineReceiver extends BroadcastReceiver {
 private boolean mUsbAccessoryMode;
private boolean mIsHwUsbConnected = false;
private boolean mIsPcKnowMe = false;
private int mPlugType = 0;

	private String getCurrentFunction() {
        String functions = android.os.SystemProperties.get("sys.usb.config",
                "none");
        Log.d(FTestService.TAG, "current function: " + functions);
        int commandIndex = functions.indexOf(',');
        if (commandIndex > 0) {
            return functions.substring(0, commandIndex);
        } else {
            return functions;
        }
    }

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(FTestService.TAG, "UsbOfflineReceiver"+intent.getAction());
		String action = intent.getAction();
        String currentFunction = getCurrentFunction();
        if (action.equals(UsbManager.ACTION_USB_STATE)) {
           mUsbAccessoryMode = intent.getBooleanExtra(UsbManager.USB_FUNCTION_ACCESSORY, false);
           Log.e(FTestService.TAG, "UsbAccessoryMode " + mUsbAccessoryMode);                
           mIsHwUsbConnected = !intent.getBooleanExtra("USB_HW_DISCONNECTED", false);
           if (!mIsHwUsbConnected){
               Intent intentService = new Intent(AutoTestHeartSet.STOPHEARTSET);
		       context.sendBroadcast(intentService);
		   }
           mIsPcKnowMe = intent.getBooleanExtra("USB_IS_PC_KNOW_ME", true);

           Log.d(FTestService.TAG, "[ACTION_USB_STATE]" + ", mIsHwUsbConnected :"
                        + mIsHwUsbConnected + ", mIsPcKnowMe :" + mIsPcKnowMe); 
           }

        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            mPlugType = intent.getIntExtra("plugged", 0);
            Log.d(FTestService.TAG, "[ACTION_BATTERY_CHANGED]" + ", mPlugType :"
                    + mPlugType);
            if (mPlugType == 0){
				Intent intentService = new Intent(AutoTestHeartSet.STOPHEARTSET);
				context.sendBroadcast(intentService);
			}
        }
		
	}
}
