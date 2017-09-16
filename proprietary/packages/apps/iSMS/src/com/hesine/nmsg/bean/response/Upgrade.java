package com.hesine.nmsg.bean.response;

import com.hesine.nmsg.bean.ClientUpdateInfo;

public class Upgrade extends Base {
	
	private ClientUpdateInfo clientUpdateInfo 	= null;

	public ClientUpdateInfo getClientUpdateInfo() {
		return clientUpdateInfo;
	}

	public void setClientUpdateInfo(ClientUpdateInfo versionInfo) {
		this.clientUpdateInfo = versionInfo;
	}

}
