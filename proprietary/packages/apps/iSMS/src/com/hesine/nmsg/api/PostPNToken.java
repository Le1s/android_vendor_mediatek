package com.hesine.nmsg.api;

import com.hesine.nmsg.bean.ActionInfo;
import com.hesine.nmsg.common.GlobalData;
import com.hesine.nmsg.config.Config;
import com.hesine.nmsg.interfacee.Pipe;


public class PostPNToken extends com.hesine.nmsg.api.Base<com.hesine.nmsg.bean.request.Base, com.hesine.nmsg.bean.response.Base>{
	
	@Override
	public com.hesine.nmsg.bean.request.Base contentObject() {
		com.hesine.nmsg.bean.request.Base obj = new com.hesine.nmsg.bean.request.Base();
		ActionInfo actionInfo = new ActionInfo();
		actionInfo.setActionid(ActionInfo.ACTION_ID_SEND_PNTOKEN);
		obj.setActionInfo(actionInfo);
		return obj;
	}

	@Override
	public com.hesine.nmsg.bean.response.Base parseObject() {
		return new com.hesine.nmsg.bean.response.Base();
	}

	@Override
	public void procReplyDataStore(com.hesine.nmsg.bean.response.Base parseData, int success) {
		if(success >= Pipe.NET_SUCCESS) {
			Config.saveImsi(GlobalData.instance().getSystemInfo().getImsi());
			Config.saveUploadPNTokenFlag(true);            
		} else {
			Config.saveUploadPNTokenFlag(false);
		}
	}
}
