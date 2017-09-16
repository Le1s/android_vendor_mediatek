package com.hesine.nmsg.pn;

import java.util.ArrayList;
import java.util.List;

import com.hesine.nmsg.api.MsgGeter;
import com.hesine.nmsg.bean.ServiceInfo;
import com.hesine.nmsg.db.DBUtils;
import com.hesine.nmsg.statistics.statistics;

public class PNMessageHandler {
	private MsgGeter mNmsgRecvMsg = null;
	private static PNMessageHandler ins = null;

	public static PNMessageHandler instance() {
		if (null == ins) {
			ins = new PNMessageHandler();
		}

		return ins;
	}

	public PNMessageHandler() {
		mNmsgRecvMsg = new MsgGeter();
	}

	public void handlePNCommand(String message) {
		String[] strs = message.split("[-]");
		int command = 0;

		if (strs != null) {
			command = Integer.valueOf(strs[0]);
		}

		switch (command) {
		case PNControler.COMMAND_NEW_MSG:
			ServiceInfo sv = DBUtils.getServiceInfo(strs[1]);
			if(sv != null && sv.getStatus() == 0){
			    return;
			}
			String[] msgIdStrs = strs[2].split("[,]");

			List<String> msgIds = new ArrayList<String>();
			for(String msgId : msgIdStrs) {
				msgIds.add(msgId);
			}
			statistics.getInstance().receivePnNotification(msgIds);
			mNmsgRecvMsg.request(strs[1], msgIds);
			break;

//		case PNControler.COMMAND_VERSION_UPGRADE:
//			//	TODO
//			break;
//
//		case PNControler.COMMAND_UPLOAD_STATISTICS:
//			//	TODO
//			break;

		default:
			break;
		}
	}

}
