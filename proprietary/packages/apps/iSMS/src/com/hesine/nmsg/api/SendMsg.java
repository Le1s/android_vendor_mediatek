package com.hesine.nmsg.api;

import com.hesine.nmsg.bean.ActionInfo;
import com.hesine.nmsg.bean.LocalMessageInfo;
import com.hesine.nmsg.bean.MessageInfo;
import com.hesine.nmsg.common.GlobalData;
import com.hesine.nmsg.db.DBUtils;
import com.hesine.nmsg.interfacee.Pipe;

public class SendMsg extends
        Base<com.hesine.nmsg.bean.request.SendMsg, com.hesine.nmsg.bean.response.Base> {

    private MessageInfo mMessageInfo = null;
    private Long mThreadId;
    private String mAccount = null;

    // if need be called when launcher send msg
    public void setMessageInfo(MessageInfo mMessageInfo) {
        this.mMessageInfo = mMessageInfo;
    }

    @Override
    public com.hesine.nmsg.bean.request.SendMsg contentObject() {
        com.hesine.nmsg.bean.request.SendMsg obj = new com.hesine.nmsg.bean.request.SendMsg();
        obj.getActionInfo().setActionid(ActionInfo.ACTION_ID_SEND_DATA);
        obj.setMessageInfo(mMessageInfo);
        return obj;
    }

    @Override
    public void procRequestDataStore(com.hesine.nmsg.bean.request.SendMsg submitData) {
        String imsi = GlobalData.instance().getSystemInfo().getImsi();
        String imei = GlobalData.instance().getSystemInfo().getImei();
        String msgId = null;
        if (null == imsi) {
            msgId = ((imei == null) ? "" : imei) + System.currentTimeMillis();
        } else {
            msgId = imsi + System.currentTimeMillis();
        }
        mMessageInfo.setMsgid(msgId);
        int status = LocalMessageInfo.STATUS_TO_OUTBOX;
        DBUtils.insertMessage(mAccount, mThreadId, null, mMessageInfo, status);
    }

    @Override
    public com.hesine.nmsg.bean.response.Base parseObject() {
        return new com.hesine.nmsg.bean.response.Base();
    }

    @Override
    public void procReplyDataStore(com.hesine.nmsg.bean.response.Base parseData, int success) {
        int status_code = LocalMessageInfo.STATUS_TO_FAILED;
        if (success == Pipe.NET_SUCCESS) {
            status_code = LocalMessageInfo.STATUS_TO_SENT;
        }
        DBUtils.updateMessageStatus(mMessageInfo.getMsgid(), status_code);
        // BroadCastUtils.sendUpdateMessagesBroadcast();
    }

    public void setAccountAndThreadId(String account, Long threadId) {
        this.mAccount = account;
        this.mThreadId = threadId;
    }

}
