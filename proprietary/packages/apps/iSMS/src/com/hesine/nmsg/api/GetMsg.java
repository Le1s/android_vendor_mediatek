	package com.hesine.nmsg.api;

import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;

import com.hesine.nmsg.Application;
import com.hesine.nmsg.bean.ActionInfo;
import com.hesine.nmsg.bean.LocalMessageInfo;
import com.hesine.nmsg.bean.MessageInfo;
import com.hesine.nmsg.common.EnumConstants;
import com.hesine.nmsg.db.DBUtils;
import com.hesine.nmsg.db.NmsSMSMMSManager;
import com.hesine.nmsg.interfacee.Pipe;
import com.hesine.nmsg.statistics.statistics;
import com.hesine.nmsg.ui.NmsgNotification;

public class GetMsg extends com.hesine.nmsg.api.Base<com.hesine.nmsg.bean.request.GetMsg, com.hesine.nmsg.bean.response.GetMsg>{
	
	private String serviceAccount;
	private List<String> messageIds;
	
	@Override
	public com.hesine.nmsg.bean.request.GetMsg contentObject() {
		com.hesine.nmsg.bean.request.GetMsg obj = new com.hesine.nmsg.bean.request.GetMsg();
		obj.getActionInfo().setActionid(ActionInfo.ACTION_ID_GET_MSG);
		obj.getActionInfo().setServiceAccount(serviceAccount);
		obj.setMessageIds(messageIds);
		return obj;
	}

	public void setServiceAccount(String serviceAccount) {
		this.serviceAccount = serviceAccount;
	}

	@Override
	public com.hesine.nmsg.bean.response.GetMsg parseObject() {
		return new com.hesine.nmsg.bean.response.GetMsg();
	}

	@Override
	public void procReplyDataStore(
			com.hesine.nmsg.bean.response.GetMsg parseData, int success) {
		if(success == Pipe.NET_SUCCESS) {
	        Iterator<MessageInfo> iter = parseData.getMessages().iterator();
	        while (iter.hasNext()) {
	        	MessageInfo info  = iter.next();
	        	statistics.getInstance().receiveMsgSuccess(info.getMsgid(), String.valueOf(info.getType()));
	        	String ret = NmsSMSMMSManager.saveSmsToDb(info.getSms(), info.getFrom(), info.getFrom(), System.currentTimeMillis(), info.getTime());
	        	long threadId = -1;
	        	long smsId = -1;
	        	if(null != ret) {
	        		String[] fields = ret.split(",");
	        		if(fields != null && fields.length == 2) {
		    			threadId = Long.parseLong(fields[0]);
		    			smsId = Long.parseLong(fields[1]);
	        		}
	        	}
	        	DBUtils.insertMessage(info.getFrom(), threadId, smsId, info);	              
                // send broadcast for conversationactivity
	        	Intent intent = new Intent();
                intent.setAction(EnumConstants.NMSG_INTENT_ACTION);
                intent.putExtra(EnumConstants.NMSG_INTENT_EXTRA_ACCOUNT, info.getFrom());
                Application.getInstance().sendBroadcast(intent);
                
				String from = info.getFrom();
				if(!DBUtils.ifServiceAccountExisted(from)) {
		            GetData getData = new GetData();
		            getData.setServiceAccount(from);
		            getData.setListener(new listener());
		            getData.start();
				}else{
					if(DBUtils.getMessageStatusViaMsgId(info.getMsgid()) == LocalMessageInfo.STATUS_FROM_UNREAD){
		        		NmsgNotification.getInstance(com.hesine.nmsg.Application.getInstance()).showNmsgNotification(threadId, info.getFrom(), info.getSms());
		        	}	
				}
				
				  
			}
		} else {
			;//Fail, maybe need
		}
	}
	
	class listener implements Pipe{

		@Override
		public void complete(Object owner, Object data, int success) {
			long threadId = 0;
			long smsId = 0;
			String smsContent = null;
			
			com.hesine.nmsg.bean.response.GetData getData = (com.hesine.nmsg.bean.response.GetData)data;
			String account = getData.getServiceInfo().getAccount();
			String uuid = DBUtils.getUuIdViaAccount(account);
			//long smsId = DBUtils.getSmsIdViaAccount(account);	
			
		
	        Cursor cr = DBUtils.getMessageCursorViaMsgUuid(uuid);
	        while (cr != null && cr.moveToNext()) {
	            if (cr.getCount() > 0) {
	                threadId = cr.getLong(cr.getColumnIndex("thread_id"));
	                smsId =  cr.getLong(cr.getColumnIndex("sms_id"));
	                smsContent = cr.getString(cr.getColumnIndex("sms"));
	            }
	        }
	       
	        if(cr != null){
	            cr.close();
	        }

			if(DBUtils.getMessageStatusViaSmsId(smsId) == LocalMessageInfo.STATUS_FROM_UNREAD){
        		NmsgNotification.getInstance(com.hesine.nmsg.Application.getInstance()).showNmsgNotification(threadId,
        				account,
        				smsContent);
        	}	
			
		}
		
	}

	public void setMessageIds(List<String> messageIds) {
		this.messageIds = messageIds;
	}
}
