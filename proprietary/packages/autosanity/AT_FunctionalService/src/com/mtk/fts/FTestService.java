package com.mtk.fts;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.io.Console;
import java.util.List;
import android.os.PowerManager;

import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.BroadcastReceiver;
import android.security.Credentials;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.hardware.usb.UsbManager;

public class FTestService extends Service {
	
	public static final String TAG = "FTestService";
	private MobileUnlockReceiver myR = new MobileUnlockReceiver();
	private MobileDataConnectReceiver mDataR = new MobileDataConnectReceiver();
	private WifiConnectReceiver mWifiR = new WifiConnectReceiver();
	private Log2ServerDialogDismissReceiver mL2S = new Log2ServerDialogDismissReceiver();
	private ADBDeviceResetReceiver mADBR = new ADBDeviceResetReceiver();
	private ScreenAlwaysOnReceiver screenOn = new ScreenAlwaysOnReceiver();
	private DatetimeUnupdateReceiver dtUnupdate = new DatetimeUnupdateReceiver();
	private AirplaneModeReceiver airplaneMode = new AirplaneModeReceiver();
	private ChangeInputMethodReceiver changeImputMethod = new ChangeInputMethodReceiver();
	private ChangeLanguageReceiver changeLanguage = new ChangeLanguageReceiver();
	private UnlockScreenReceiver mUnlockReceiver = new UnlockScreenReceiver();
	private AutoTestHeartSet mATH = new AutoTestHeartSet();
    private UsbOfflineReceiver mUsb = new UsbOfflineReceiver();
    private ListAllAppInforReceiver mListAllApp = new ListAllAppInforReceiver();
	
//	private ADBDeviceChecker mADBDC = null;
	
    private WifiManager mWifiManager;
    private WifiConfiguration mConfig;
    private int networkId;
    private PowerManager mPm;
    private PowerManager.WakeLock mWl;
    

    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_WPA_PSK = 3;
    static final int SECURITY_WPA2_PSK = 4;
    static final int SECURITY_EAP = 5;
    static final int SECURITY_WAPI_PSK = 6;
    static final int SECURITY_WAPI_CERT = 7;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.i(TAG, "Fontional Test Service is created");
		mPm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWl = mPm.newWakeLock(PowerManager.FULL_WAKE_LOCK,TAG);
        mWl.acquire();
		IntentFilter filter = new IntentFilter();
        filter.addAction(MobileUnlockReceiver.iA);
        this.registerReceiver(myR, filter);
        
        IntentFilter filterd = new IntentFilter();
        filterd.addAction(MobileDataConnectReceiver.STR_CONNECT);
        filterd.addAction(MobileDataConnectReceiver.STR_DISCONNECT);
        filterd.addAction(MobileDataConnectReceiver.STR_2G_CONNECT);
        this.registerReceiver(mDataR, filterd);
        
        IntentFilter filterw = new IntentFilter();
        filterw.addAction(WifiConnectReceiver.WIFI_CONNECT);
        filterw.addAction(WifiConnectReceiver.WIFI_DISCONNECT);
        this.registerReceiver(mWifiR, filterw);
        
        IntentFilter filterl = new IntentFilter();
        filterl.addAction(Log2ServerDialogDismissReceiver.STR_Dismiss);
        this.registerReceiver(mL2S, filterl);
        
        IntentFilter filterADB = new IntentFilter();
        filterADB.addAction(ADBDeviceResetReceiver.STR_ResetADB);
        this.registerReceiver(mADBR, filterADB);
        
        IntentFilter filterS = new IntentFilter();
        filterS.addAction(ScreenAlwaysOnReceiver.intentAction);
        this.registerReceiver(screenOn, filterS);
        
        IntentFilter filterDTU = new IntentFilter();
        filterDTU.addAction(DatetimeUnupdateReceiver.intentAction);
        this.registerReceiver(dtUnupdate, filterDTU);
        
        IntentFilter filterAir = new IntentFilter();
        filterAir.addAction(AirplaneModeReceiver.turnOn_intentAction);
        filterAir.addAction(AirplaneModeReceiver.turnOff_intentAction);
        this.registerReceiver(airplaneMode, filterAir);
        
        IntentFilter filterch = new IntentFilter();
        filterch.addAction(ChangeInputMethodReceiver.ChangeInputMethod_intentAction);
        this.registerReceiver(changeImputMethod, filterch); 
        
        IntentFilter filterLanguage = new IntentFilter();
        filterLanguage.addAction(ChangeLanguageReceiver.chinese_intentAction);
        filterLanguage.addAction(ChangeLanguageReceiver.english_intentAction);
        this.registerReceiver(changeLanguage, filterLanguage);
        
        IntentFilter filterAT = new IntentFilter();
        filterAT.addAction(AutoTestHeartSet.STARTHEARTSET);
        filterAT.addAction(AutoTestHeartSet.NORMALHEARTSET);
        filterAT.addAction(AutoTestHeartSet.STOPHEARTSET);
        filterAT.addAction(AutoTestHeartSet.REBOOTPHONE);
        this.registerReceiver(mATH, filterAT);

        IntentFilter filterUsb = new IntentFilter();
        filterUsb.addAction(UsbManager.ACTION_USB_STATE);
        filterUsb.addAction(Intent.ACTION_BATTERY_CHANGED);
		this.registerReceiver(mUsb, filterUsb);

        IntentFilter filterAppList = new IntentFilter();
        filterAppList.addAction(ListAllAppInforReceiver.ListReceiver);
        this.registerReceiver(mListAllApp, filterAppList);

        IntentFilter filterlock = new IntentFilter();
        filterlock.addAction(Intent.ACTION_SEND);
        Intent it = this.registerReceiver(mUnlockReceiver, filterlock, "com.android.internal.policy.impl.KeyguardViewMediator.DONE_DRAW", null);
        if ( it != null ){
        	Log.i(TAG, it.toString());
        }
        
//        mADBDC = new ADBDeviceChecker(this);
//        mADBDC.startcheck(); 
		final ContentResolver cr = this.getContentResolver();

		boolean result = Settings.Global
				.putInt(
						cr,
						Settings.Global.DEVICE_PROVISIONED,1);
		Log.i(TAG, "Settings.Global.putInt result == " + result);
//        this.sendBroadcast(new Intent(AutoTestHeartSet.STARTHEARTSET));
        super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		//Log.i(TAG, "Fontional Test Service is destroyed");
		sendBroadcast(new Intent(AutoTestHeartSet.STOPHEARTSET));
		this.unregisterReceiver(myR);
		this.unregisterReceiver(mDataR);
		this.unregisterReceiver(mWifiR);
		this.unregisterReceiver(mL2S);
		this.unregisterReceiver(mADBR);
		this.unregisterReceiver(screenOn);
        this.unregisterReceiver(dtUnupdate);
		this.unregisterReceiver(airplaneMode);
		this.unregisterReceiver(changeImputMethod);
		this.unregisterReceiver(changeLanguage);
        this.unregisterReceiver(mListAllApp);
        mWl.release();
		this.unregisterReceiver(mUnlockReceiver);
//		mADBDC.stopcheck();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.unregisterReceiver(mATH);
		this.unregisterReceiver(mUsb);
		super.onDestroy();
	}


	@Override
	public void onStart(Intent intent, int startId) {
		boolean enabled = Log.isLoggable("FunctionalServiceLog", Log.VERBOSE);
		Log.i(TAG, "Fontional Test Service is started, server log enabled: "+enabled);
//		Console cs = java.lang.System.console();
//		cs.printf("%s", "This is a test string.");
		//super.onStart(intent, startId)
//		this.sendBroadcast(new Intent(AutoTestHeartSet.NORMALHEARTSET));
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mConfig = new WifiConfiguration();
        //add usb connect
        String startby = "USB_ADB";
        if ( intent != null ){
        	startby = intent.getStringExtra("startby");
        }
//        if (startby =="BootReceiver"){
//        	this.sendBroadcast(new Intent(ADBDeviceResetReceiver.STR_ResetADB));
//        }
        
		//add wifi connnect
        String ext = "donothing";
        if ( intent != null ){
        	ext = intent.getStringExtra(WifiConnectReceiver.OPEN_OR_CLOSE);
        }
        if ( ext == null ){
        	Log.i(TAG, "WifiConnectReceiver.OPEN_OR_CLOSE is null");
        	return;
        }
   		if(WifiConnectReceiver.WIFI_CONNECT.equals(ext)){
    		if(!mWifiManager.isWifiEnabled()){ 
    			mWifiManager.setWifiEnabled(true);
    			Log.i(TAG,"wifi is disabled");
    			int i = 0;
    			while(!mWifiManager.isWifiEnabled()&&i<30){
        			sleep(1000);
        			i++;
    			}
    		}
    		mConfig.SSID = convertToQuotedString(intent.getStringExtra("SSID"));
    		if(!isConfigued(mConfig.SSID)){
    			setSecurityType(intent);
    			networkId = mWifiManager.addNetwork(mConfig);
                 Log.i(TAG,"mConfig = " + mConfig);
    			mWifiManager.enableNetwork(networkId, false);
    			connect(networkId);
    		}
     	}else if(WifiConnectReceiver.WIFI_DISCONNECT.equals(ext)){
		 Log.i(TAG,"service onDestroy");
             	disconnect();
    	}		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
    private void connect(int id) {
        Log.i(TAG,"current id : "+id);
        if(!mWifiManager.isWifiEnabled()){
            Log.i(TAG,"connected id : "+mWifiManager.getConnectionInfo().getNetworkId());
            return;
        }
        WifiConfiguration config = new WifiConfiguration();
        config.networkId = networkId;
        config.priority = getMaxPriority() + 1;
        mWifiManager.updateNetwork(config);
        mWifiManager.saveConfiguration();
        // Connect to network by disabling others.
        mWifiManager.enableNetwork(id, true);
        //mWifiManager.enableNetwork(id, false);
        mWifiManager.reconnect();
    }
    private boolean isConfigued(String ssid){
        int i=0;
        List<WifiConfiguration> mConfig = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : mConfig) {
            if (config != null && config.SSID.equals(ssid)) {
               Log.d(TAG,"configurd "+i);    
               connect(config.networkId);
               return true;
            }
        }
        return false;
    }
    private void disconnect() {
        Log.d(TAG,"disconnect networkId="+networkId);
        
        mWifiManager.removeNetwork(mWifiManager.getConnectionInfo().getNetworkId());
	mWifiManager.setWifiEnabled(false);
        
        /*i = 0;
        while(mWifiManager.isWifiEnabled() &&i<10){
            sleep(1000);
            i++;
        }*/

    }
    
    private int getMaxPriority() {
        int max = 0;
        List<WifiConfiguration> mConfigs = mWifiManager.getConfiguredNetworks();
        int mConfiguredApCount = mConfigs == null ? 0 : mConfigs.size();
        Log.d(TAG,"mConfiguredApCount="+mConfiguredApCount);
        for (WifiConfiguration config : mConfigs) {
            if (config != null) {
                if (config.priority > max) {
                    max = config.priority;
                }
                Log.d(TAG,config.SSID+" priority="+config.priority);
            }
        }
        return max;
    }

    private String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }
    private void setSecurityType(Intent intent) {
        int securityType = intent.getIntExtra("security", 0);
        String password = intent.getStringExtra("password");
        switch (securityType) {
        case SECURITY_NONE:
            mConfig.allowedKeyManagement.set(KeyMgmt.NONE);
            break;
        case SECURITY_WEP:
            mConfig.allowedKeyManagement.set(KeyMgmt.NONE);
            mConfig.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            mConfig.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
            int length = password.length();
            if (length != 0) {
                //get selected WEP key index
                int keyIndex = 0;//selected password index, 0~3
                // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                mConfig.wepKeys[keyIndex] = password;
                mConfig.wepTxKeyIndex = keyIndex;
            }
            break;
        case SECURITY_WPA_PSK:
        case SECURITY_WPA2_PSK:
        case SECURITY_PSK:
            mConfig.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            if (password.length() != 0) {
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    mConfig.preSharedKey = password;
                } else {
                    mConfig.preSharedKey = '"' + password + '"';
                }
            }
            if(securityType == SECURITY_WPA_PSK){
                mConfig.allowedPairwiseCiphers.set(PairwiseCipher.TKIP);
            }else if(securityType == SECURITY_WPA2_PSK){
                mConfig.allowedPairwiseCiphers.set(PairwiseCipher.CCMP);
            }
            break;
        case SECURITY_EAP:
            String identity = intent.getStringExtra("identity");
            mConfig.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
            mConfig.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
            mConfig.enterpriseConfig = new WifiEnterpriseConfig();
            mConfig.enterpriseConfig.setEapMethod(0);
            mConfig.enterpriseConfig.setPhase2Method(0);
            mConfig.enterpriseConfig.setCaCertificateAlias("");
            //mConfig.private_key.setValue("");
			mConfig.enterpriseConfig.setClientCertificateAlias("");
	        this.mConfig.enterpriseConfig.setIdentity((identity.length() == 0) ? "" :
                identity);
            mConfig.enterpriseConfig.setAnonymousIdentity("");
            if (password.length() != 0) {
                mConfig.enterpriseConfig.setPassword(password);
            }
            break;
        case SECURITY_WAPI_PSK:
            mConfig.allowedKeyManagement.set(KeyMgmt.WAPI_PSK);
            mConfig.allowedProtocols.set(Protocol.WAPI);
            mConfig.allowedPairwiseCiphers.set(PairwiseCipher.SMS4);
            mConfig.allowedGroupCiphers.set(GroupCipher.SMS4);
            if (password.length() != 0) {
                mConfig.preSharedKey = '"' + password + '"';
            }
            break;
        case SECURITY_WAPI_CERT:
            mConfig.allowedKeyManagement.set(KeyMgmt.WAPI_CERT);
            mConfig.allowedProtocols.set(Protocol.WAPI);
            mConfig.allowedPairwiseCiphers.set(PairwiseCipher.SMS4);
            mConfig.allowedGroupCiphers.set(GroupCipher.SMS4);
            mConfig.enterpriseConfig = new WifiEnterpriseConfig();
            mConfig.enterpriseConfig.setCaCertificateAlias("");
            mConfig.enterpriseConfig.setClientCertificateAlias("");
            break;
        default:
            break;
        }
    }

    public void sleep(int time){
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
