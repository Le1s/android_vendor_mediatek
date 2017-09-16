package com.hesine.nmsg.api;

import com.hesine.nmsg.Application;
import com.hesine.nmsg.bean.ActionInfo;
import com.hesine.nmsg.common.GlobalData;
import com.hesine.nmsg.config.Config;
import com.hesine.nmsg.interfacee.Pipe;
import com.hesine.nmsg.pn.PNControler;
import com.hesine.nmsg.statistics.statistics;


public class Activation extends com.hesine.nmsg.api.Base<com.hesine.nmsg.bean.request.Base, com.hesine.nmsg.bean.response.Activation>{
	
	private static Activation ins = null;
	public static Activation instance() {
		if(null == ins) {
			ins = new Activation();
		}
		
		return ins;
	}
	
	@Override
	public com.hesine.nmsg.bean.request.Base contentObject() {
		com.hesine.nmsg.bean.request.Base obj = new com.hesine.nmsg.bean.request.Base();
		obj.getActionInfo().setActionid(ActionInfo.ACTION_ID_ACTIVATE);
		return obj;
	}

	@Override
	public com.hesine.nmsg.bean.response.Activation parseObject() {
		return new com.hesine.nmsg.bean.response.Activation();
	}

	@Override
	public void procReplyDataStore(com.hesine.nmsg.bean.response.Activation parseData, int success) {
	    if(success >= Pipe.NET_SUCCESS) {
	        Config.saveUuid(parseData.getUuid());
	        Config.saveIsActivated(true);
	        GlobalData.instance().getSystemInfo().setUuid(parseData.getUuid());
	        statistics.getInstance().uuid(parseData.getUuid());
	        PNControler.startPN(Application.getInstance().getApplicationContext());
	    }
	}
}
