package com.hesine.nmsg.bean.response;

import java.util.List;

import com.hesine.nmsg.bean.ClientUpdateInfo;
import com.hesine.nmsg.bean.MessageInfo;

public class GetMsg extends Base {
	
	private List<MessageInfo> messages;
	private ClientUpdateInfo clientUpdateInfo;

	public List<MessageInfo> getMessages() {
		return messages;
	}

	public void setMessages(List<MessageInfo> messages) {
		this.messages = messages;
	}

	public ClientUpdateInfo getClientUpdateInfo() {
		return clientUpdateInfo;
	}

	public void setClientUpdateInfo(ClientUpdateInfo clientUpdateInfo) {
		this.clientUpdateInfo = clientUpdateInfo;
	}

}
