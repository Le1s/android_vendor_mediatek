package com.mtk.fts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import com.mtk.fts.FTestService;

public class DevicesBootEndReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(FTestService.TAG, "DevicesBootEndReceiver"+intent.getAction());
		Intent intentService = new Intent();
		// TODO Auto-generated method stub
        //"com.mtk.FunctionalTestService"
		intentService.setClass(context, FTestService.class);
		intentService.setAction("com.mtk.FunctionalTestService");
		intentService.putExtra("startby", "BootReceiver");
		context.startService(intentService);
	}
}
