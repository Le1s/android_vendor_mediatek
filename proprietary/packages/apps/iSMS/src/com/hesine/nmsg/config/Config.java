package com.hesine.nmsg.config;

import com.hesine.nmsg.Application;
import com.hesine.nmsg.common.EnumConstants;

import android.content.SharedPreferences;

public class Config extends BaseConfig {
    
    public Config(){}

    public Config(String prefsName) {
    }

    @Override
    protected SharedPreferences GetSharedPrefs() {
        return Application.getInstance().getSharedPreferences(EnumConstants.SHARED_PREFERENCE_NAME, 0);
    }
    
    public boolean isActivated;
    public String  uuid;
    public String  pnToken;
    public boolean	uploadPNTokenFlag;
    public long		lastUpgradeTime;
    public String  	newVersion;
    public String  	newClientUrl;
    public String	imsi;
	
	public static void saveIsActivated(boolean activate) {
		Config conf = new Config();
		conf.Get();
		conf.isActivated = activate;
		conf.Save();
	}
	
	public static boolean getIsActivated(){
		Config conf = new Config();
		conf.Get();
		return conf.isActivated;
	}
    
    public static void saveUuid(String uuid) {
        Config conf = new Config();
        conf.Get();
        conf.uuid = uuid;
        conf.Save();
    }
    
    public static String getUuid(){
        Config conf = new Config();
        conf.Get();
        return conf.uuid;
    }
    
    public static void savePnToken(String pnToken) {
        Config conf = new Config();
        conf.Get();
        conf.pnToken = pnToken;
        conf.Save();
    }
    
    public static String getPnToken(){
        Config conf = new Config();
        conf.Get();
        return conf.pnToken;
    }
    
    public static void saveUploadPNTokenFlag(boolean uploadPNTokenFlag) {
        Config conf = new Config();
        conf.Get();
        conf.uploadPNTokenFlag = uploadPNTokenFlag;
        conf.Save();
    }
    
    public static boolean getUploadPNTokenFlag(){
        Config conf = new Config();
        conf.Get();
        return conf.uploadPNTokenFlag;
    }
    
    public static void saveLastUpgradeTime(long time) {
        Config conf = new Config();
        conf.Get();
        conf.lastUpgradeTime = time;
        conf.Save();
    }
    
    public static long getLastUpgradeTime(){
        Config conf = new Config();
        conf.Get();
        return conf.lastUpgradeTime;
    }
    
    public static void saveNewVersion(String version) {
        Config conf = new Config();
        conf.Get();
        conf.newVersion = version;
        conf.Save();
    }
    
    public static String getNewVersion(){
        Config conf = new Config();
        conf.Get();
        return conf.newVersion;
    }
    
    public static void saveNewClientUrl(String url) {
        Config conf = new Config();
        conf.Get();
        conf.newClientUrl = url;
        conf.Save();
    }
    
    public static String getNewClientUrl(){
        Config conf = new Config();
        conf.Get();
        return conf.newClientUrl;
    }    
    
    public static void saveImsi(String imsi) {
        Config conf = new Config();
        conf.Get();
        conf.imsi = imsi;
        conf.Save();
    }
    
    public static String getImsi(){
        Config conf = new Config();
        conf.Get();
        return conf.imsi;
    }    
        
}
