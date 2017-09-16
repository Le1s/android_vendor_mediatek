package com.mtk.fts;

import android.content.BroadcastReceiver;
import android.os.ServiceManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.util.Log;
/*
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;
*/
import java.util.List;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.ITelephony;
import android.os.RemoteException;


public class MobileDataConnectReceiver extends BroadcastReceiver {
    public static final String STR_CONNECT = "com.mtk.mobiledataconnectreceiver.dataconnect";
    public static final String STR_DISCONNECT = "com.mtk.mobiledataconnectreceiver.datadisconnect";
    public static final String STR_2G_CONNECT = "com.mtk.mobiledataconnectreceiver.2gdataconnect";
    
    private static final String TAG = FTestService.TAG;
    public Context mContext;

    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Not Ready , please take care of it later!");
    } 

/*
    public void onReceive(Context context, Intent intent) {
        mContext = context;
    	SimInfoRecord simInfo;   
    	//boolean geminisupport = com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT;
    	List<SimInfoRecord> simList = SimInfoManager.getInsertedSimInfoList(mContext);
    	Log.i(TAG, "enter mobile data connect receiver2 + "+simList.size());
    	if(simList == null || simList.size() == 0) {
    		return;
    	}
    	
		ITelephony telphony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));

    	int sim_status_1 = TelephonyManager.SIM_STATE_UNKNOWN;
        int sim_status_2 = TelephonyManager.SIM_STATE_UNKNOWN;
        try{
            sim_status_1 = telphony.getSimState(0);
            sim_status_2 = telphony.getSimState(1);
        }catch(RemoteException ex){
            ex.printStackTrace();
        }
        Log.i(TAG, "enter mobile data connect status 1: + "+sim_status_1);
        Log.i(TAG, "enter mobile data connect status 2: + "+sim_status_2);
        
        boolean geminisupport = sim_status_1 == TelephonyManager.SIM_STATE_READY && sim_status_2 == TelephonyManager.SIM_STATE_READY;
        
    	simInfo = simList.get(0);
        String intentAction = intent.getAction();
        if (intentAction.equals(STR_2G_CONNECT) && simList.size() > 1 ){
        	intentAction = STR_CONNECT;
        	simInfo = simList.get(1); 
        }
        long simid = simInfo.mSimInfoId;
        Log.i(TAG, "SIMID = "+simid);
        if (STR_CONNECT.equals(intentAction)) {
        	Log.i(TAG, "action = "+STR_CONNECT);
        	Intent intent1 = new Intent();
        	intent1.setAction(Intent.ACTION_DATA_DEFAULT_SIM_CHANGED);
	    	intent1.putExtra(PhoneConstants.MULTI_SIM_ID_KEY, simid);	    	
            mContext.sendBroadcast(intent1);
            if (!geminisupport){
            	Log.i(TAG, "Gemini is not support");
                ConnectivityManager cm =  //single card use this method
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    		    cm.setMobileDataEnabled(true);
            }
          
        } else if (STR_DISCONNECT.equals(intentAction)) {
        	Log.i(TAG, "action = "+STR_DISCONNECT);
        	Intent intent1 = new Intent();	    	
	    	intent1.setAction(Intent.ACTION_DATA_DEFAULT_SIM_CHANGED);
	    	intent1.putExtra(PhoneConstants.MULTI_SIM_ID_KEY, 0);
            mContext.sendBroadcast(intent1);
            if (!geminisupport){
            	Log.i(TAG, "Gemini is not support");
            	ConnectivityManager cm =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    		cm.setMobileDataEnabled(false);
            }
        }     
    }*/
}
