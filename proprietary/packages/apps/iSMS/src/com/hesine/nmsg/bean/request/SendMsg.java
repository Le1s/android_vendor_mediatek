package com.hesine.nmsg.bean.request;

import com.hesine.nmsg.bean.MessageInfo;

public class SendMsg extends Base {
	
	private MessageInfo messageInfo 	= null;

	public MessageInfo getMessageInfo() {
		return messageInfo;
	}

	public void setMessageInfo(MessageInfo messageInfo) {
		this.messageInfo = messageInfo;
	}
}
