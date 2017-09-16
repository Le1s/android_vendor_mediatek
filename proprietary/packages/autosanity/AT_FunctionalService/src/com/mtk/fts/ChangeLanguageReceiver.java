package com.mtk.fts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import java.util.Locale;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.app.ActivityManagerNative;
import android.app.backup.BackupManager;
import android.app.IActivityManager;


public class ChangeLanguageReceiver extends BroadcastReceiver {
	public static final String chinese_intentAction = "com.mediatek.chinese";
	public static final String english_intentAction = "com.mediatek.english";
	private static final String TAG = FTestService.TAG;
	@Override
	public void onReceive(Context context, Intent intent) {
         Log.i(TAG, "enter mtk ChangeLanguage receiver");
         Log.i(TAG, "Intent action = "+intent.getAction());
         Configuration config = null;
         IActivityManager am = null;
         try{
        	 am = ActivityManagerNative.getDefault();
             config = am.getConfiguration();
         }catch(RemoteException e){
        	 Log.i(TAG, "get config failure"+e.getStackTrace());
        	 return;
         }

final String[] locales = Resources.getSystem().getAssets().getLocales();
final int origSize = locales.length;
for (int i = 0 ; i < origSize; i++ ) {
            final String s = locales[i];
            final int len = s.length();
            if (len == 5) {
                String language = s.substring(0, 2);
                String country = s.substring(3, 5);
                Log.i(TAG, "Language "+language+" Country: "+country);
                final Locale l = new Locale(language, country);
}
}
         try{
	     if(intent.getAction().equals(chinese_intentAction)){

	    	 Log.i(TAG, "begin to change to chinese ");
          	config.locale = new Locale("zh", "CN");
          	config.userSetLocale = true;
          	am.updateConfiguration(config);
          
          	Log.i(TAG,  "after change to chinese ");
	      
	      }else if(intent.getAction().equals(english_intentAction)){
	      	
	    	  config.locale = new Locale("en", "US");
	    	  config.userSetLocale = true;
	    	  am.updateConfiguration(config);
	      	}
         }catch(RemoteException e){
        	 Log.i(TAG, "set config failure"+e.getStackTrace());
        	 return;
         }
         BackupManager.dataChanged("com.android.providers.settings");
	}
}
