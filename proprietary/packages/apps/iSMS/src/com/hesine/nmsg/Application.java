package com.hesine.nmsg;


import android.content.Intent;

import com.hesine.hstat.SDK;
import com.hesine.nmsg.api.Activation;
import com.hesine.nmsg.config.Config;
import com.hesine.nmsg.db.LocalStore;
import com.hesine.nmsg.pn.PNControler;
import com.hesine.nmsg.service.NmsgService;
import com.hesine.nmsg.util.MLog;

public class Application extends android.app.Application {
	private static Application instance;
	public static final String TAG = "Application";
	
	public Application() {
		super();
		instance = this;
	}
	
	static public Application getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MLog.init(this);
		MLog.readIniFile();
		SDK.init(this);
		LocalStore store = LocalStore.instance();
		if (store.isDbUpgrade()) {
			store.dbDbUpgrade();
		}				
		
        if(!Config.getIsActivated()) {
        	Activation.instance().start();
        } else {
            PNControler.startPN(getApplicationContext());
        }
        
        if(!NmsgService.getInstance().isServiceStart()){
            MLog.error(TAG, "NmsgService is not running, so start it from application");
            Intent i = new Intent(Application.getInstance(), NmsgService.class);
            Application.getInstance().startService(i);
        }
        MLog.trace(TAG, "test application on create");
	}

}
