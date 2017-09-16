	package com.hesine.nmsg.api;

import com.hesine.nmsg.bean.ActionInfo;
import com.hesine.nmsg.bean.ImageInfo;
import com.hesine.nmsg.bean.PicInfo;
import com.hesine.nmsg.bean.ServiceInfo;
import com.hesine.nmsg.db.DBUtils;
import com.hesine.nmsg.interfacee.Pipe;
import com.hesine.nmsg.util.CommonUtils;
import com.hesine.nmsg.util.FileEx;

public class GetData extends com.hesine.nmsg.api.Base<com.hesine.nmsg.bean.request.Base, com.hesine.nmsg.bean.response.GetData>{
	
	private String serviceAccount = null;
	
	@Override
	public com.hesine.nmsg.bean.request.Base contentObject() {
		com.hesine.nmsg.bean.request.Base obj = new com.hesine.nmsg.bean.request.Base();
		obj.getActionInfo().setActionid(ActionInfo.ACTION_ID_GET_DATA);
		obj.getActionInfo().setServiceAccount(serviceAccount);
		return obj;
	}

	@Override
	public com.hesine.nmsg.bean.response.GetData parseObject() {
		return new com.hesine.nmsg.bean.response.GetData();
	}

	@Override
	public void procReplyDataStore(
			com.hesine.nmsg.bean.response.GetData parseData, int success) {
		if(success == Pipe.NET_SUCCESS) {
			ServiceInfo serviceInfo = parseData.getServiceInfo();
			PicInfo picInfo = parseData.getPicInfo();
			serviceInfo.setAccount(serviceAccount);
			getAccountAvatar(picInfo);
			DBUtils.insertServiceInfo(serviceInfo, picInfo);
		} else {
			;//Fail, maybe need
		}
	}

    public void getAccountAvatar(PicInfo pi) {
        if (FileEx.ifFileExisted(PicInfo.getPicPath(pi))) {
        	FileEx.DeleteFile(PicInfo.getPicPath(pi));
        }
        ImageWorker mImageWorker = new ImageWorker();
        mImageWorker.setListener(new Pipe() {
            @Override
            public void complete(Object owner, Object data, int success) {
                CommonUtils.updateContactInPhonebook(DBUtils.getServiceInfo(serviceAccount));
            }
        });
        ImageInfo ii = new ImageInfo();
        ii.setPath(PicInfo.getPicPath(pi));
        ii.setUrl(pi.getUrl());
        mImageWorker.setImageInfo(ii);
        mImageWorker.request();
    }
	
	public String getServiceAccount() {
		return serviceAccount;
	}

	public void setServiceAccount(String serviceAccount) {
		this.serviceAccount = serviceAccount;
	}
}
