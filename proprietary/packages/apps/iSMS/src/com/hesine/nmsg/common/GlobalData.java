package com.hesine.nmsg.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.hesine.nmsg.Application;
import com.hesine.nmsg.bean.ClientInfo;
import com.hesine.nmsg.bean.SystemInfo;
import com.hesine.nmsg.config.Config;
import com.hesine.nmsg.util.DeviceInfo;

public class GlobalData {
	
	private static GlobalData ins = null;
	public static GlobalData instance() {
		if(null == ins) {
			ins = new GlobalData();
		}
		
		return ins;
	}
	
	GlobalData() {
		initSystemInfo();
		initClientInfo();
	}
	
	
	private SystemInfo 	systemInfo 	= null;
	private ClientInfo 	clientInfo 	= null;
	private String		id			= null;

	private void initSystemInfo() {
		Context context = Application.getInstance().getApplicationContext();
		systemInfo = new SystemInfo();
		systemInfo.setPhoneNum(DeviceInfo.getPhonenum(context));
		systemInfo.setImsi(DeviceInfo.getIMSI(context));
		systemInfo.setImei(DeviceInfo.getIMEI(context));
		systemInfo.setDevice(DeviceInfo.getDeviceModel());
		systemInfo.setBrand(DeviceInfo.getDeviceBrand());
		systemInfo.setLanguage(DeviceInfo.getLanuage(context));        
		systemInfo.setPnToken(Config.getPnToken());
		systemInfo.setPnType("HPNS");
		systemInfo.setUuid(Config.getUuid());
    }

	private void initClientInfo() {
    	clientInfo = new ClientInfo();
    	clientInfo.setVersion(getVersionName()/*EnumConstants.version*/);
    	clientInfo.setChannelId(EnumConstants.channelID);
    	clientInfo.setAppName("nmsg");
    }
	
	public static String getVersionName() {
		try {
		    Context context = Application.getInstance();
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "V1.00.000.000.00";
		}
	}
	
	public SystemInfo getSystemInfo() {
		return systemInfo;
	}

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
