package com.hesine.nmsg.api;

import java.util.ArrayList;
import java.util.List;

import com.hesine.nmsg.interfacee.Pipe;

public class MsgGeter implements Pipe {

	private List<GetMsg>		mSendQueue 	= new ArrayList<GetMsg>();
    
	private Pipe listener = null;
	
	public void setListener(Pipe listener) {
		this.listener = listener;
	}

    public void request(String serviceAccount, List<String> messageIds) {
	    GetMsg api = new GetMsg();
		api.setMessageIds(messageIds);
		api.setServiceAccount(serviceAccount);
		api.setListener(this);
		if(mSendQueue.size() <= 0) {
			api.request();
		}
		mSendQueue.add(api);
	}
	
	@Override
	public void complete(Object owner, Object data, int success) {
		if(null != listener) {
			listener.complete(this, data, success);
		}
		mSendQueue.remove(0);
		if(mSendQueue.size() > 0) {
			mSendQueue.get(0).request();
		}
	}
}
