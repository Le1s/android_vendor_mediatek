package com.hesine.nmsg.api;

import com.hesine.nmsg.Application;
import com.hesine.nmsg.R;
import com.hesine.nmsg.bean.ActionInfo;
import com.hesine.nmsg.bean.ClientUpdateInfo;
import com.hesine.nmsg.config.Config;
import com.hesine.nmsg.interfacee.Pipe;
import com.hesine.nmsg.ui.NmsgNotification;

public class Upgrade
		extends
		Base<com.hesine.nmsg.bean.request.Base, com.hesine.nmsg.bean.response.Upgrade> {

	public static final long ONE_MONTH = 2592000000l;
	public static final long YEAR_ABOUT_2010 = 40*12*ONE_MONTH;
	
	@Override
	public com.hesine.nmsg.bean.request.Base contentObject() {
		com.hesine.nmsg.bean.request.Base obj = new com.hesine.nmsg.bean.request.Base();
		obj.getActionInfo().setActionid(ActionInfo.ACTION_ID_UPGRADE);
		return obj;
	}

	@Override
	public com.hesine.nmsg.bean.response.Upgrade parseObject() {
		return new com.hesine.nmsg.bean.response.Upgrade();
	}

	@Override
	public void procReplyDataStore(
			com.hesine.nmsg.bean.response.Upgrade parseData, int success) {
		if(success >= Pipe.NET_SUCCESS) {
			ClientUpdateInfo clientUpdateInfo = parseData.getClientUpdateInfo();
			if(null != clientUpdateInfo) {
				Config.saveLastUpgradeTime(System.currentTimeMillis());
				Config.saveNewVersion(clientUpdateInfo.getVersion());
				String url = clientUpdateInfo.getUrl();
				Config.saveNewClientUrl(url);
				downApp(url);
			}
		}
	}

    public static int compareVersion(String newVersion, String oldVersion) {
    	String[] osvs = oldVersion.substring(oldVersion.toUpperCase().indexOf('V')+1).split("[.]");
    	String[] nsvs = newVersion.substring(newVersion.toUpperCase().indexOf('V')+1).split("[.]");
    	for(int i=0; i<osvs.length; i++) {
    		int osv = Integer.parseInt(osvs[i]);
    		int nsv = Integer.parseInt(nsvs[i]);
    		if(nsv > osv) {
    			return 1;
    		} else if(nsv < osv) {
    			return -1;
    		}
    	}
    	
    	return 0;
    }

    public static void downApp(String url) {
    	String ticker = Application.getInstance().getString(R.string.update_title);
    	String title = Application.getInstance().getString(R.string.update_title);
    	String content = Application.getInstance().getString(R.string.update_content);
    	NmsgNotification.getInstance(Application.getInstance().getApplicationContext()).showApkDownloadNotification(url, ticker, title, content);
    }

}
