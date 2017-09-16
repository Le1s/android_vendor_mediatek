package com.hesine.nmsg.bean.request;

import com.hesine.nmsg.bean.PicInfo;
import com.hesine.nmsg.bean.UserInfo;

public class SendData extends Base {
	
	private UserInfo userInfo 	= null;
	private PicInfo picInfo 	= null;
	
	public UserInfo getUserInfo() {
		return userInfo;
	}
	
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
	
	public PicInfo getPicInfo() {
		return picInfo;
	}
	
	public void setPicInfo(PicInfo picInfo) {
		this.picInfo = picInfo;
	}
	
}
