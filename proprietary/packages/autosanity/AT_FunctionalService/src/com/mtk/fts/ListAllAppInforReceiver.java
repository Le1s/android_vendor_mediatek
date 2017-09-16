package com.mtk.fts;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class ListAllAppInforReceiver extends BroadcastReceiver {
    public static final String ListReceiver = "com.mtk.autotest.listAllAPP";
    private static final String TAG = FTestService.TAG;
	@Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "enter List All APPInfor Receiver");
        String intentAction = intent.getAction();
        if (ListReceiver.equals(intentAction)) {
            Log.i(TAG, "receiver; start service!");
            ListAllApp(context);
        } 
    }
	
	private void ListAllApp(Context context){
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);  
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER); 
        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(mainIntent, 0);  
	    //List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
	    for(int i=0;i<apps.size();i++) { 
	    	ResolveInfo reInfo = apps.get(i);
	        String appName = reInfo.activityInfo.loadLabel(context.getPackageManager()).toString(); 
	        String packageName = reInfo.activityInfo.packageName; 
	        String activyName = reInfo.activityInfo.name;
	        Log.v("ListAllAppInforReceiver", "{appname:"+appName+" package:"+packageName+" activity:"+activyName);
            Log.v(TAG, "{appname:"+appName+" package:"+packageName+" activity:"+activyName);
	    }
	    Log.v("ListAllAppInforReceiver", "ListAllAppDone");
	}

}
