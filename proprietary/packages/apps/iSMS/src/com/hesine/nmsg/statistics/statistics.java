package com.hesine.nmsg.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hesine.hstat.SDK;
import com.hesine.nmsg.Application;
import com.hesine.nmsg.R;
import com.hesine.nmsg.bean.ServiceInfo;

public class statistics {
    private static final String EVENT_ID_RECEIVE_MSG_SUCCESS = "10000001";
    private static final String EVENT_ID_RECEIVE_PN_NOTIFICATION = "10000002";
    private static final String EVENT_ID_CLICK_APP = "10000003";
    private static final String EVENT_ID_MSG_READ = "10000004";
    private static final String EVENT_ID_MSG_SHARE = "10000005";
    private static final String EVENT_ID_SERVICE_ACCOUNT = "10000006";
    private static final String EVENT_ID_MSG_SEND = "10000007";
    private static final String EVENT_ID_DELETE_THREADS = "10000008";
    private static final String EVENT_ID_UUID = "10000009";
    private static final String EVENT_ID_ACCOUNT_STATUS = "10000010";

    private static final String EVENT_LABLE_RECEIVE_MSG_SUCCESS = Application.getInstance().getString(R.string.statistics_receive_msg_success);
    private static final String EVENT_LABLE_RECEIVE_PN_NOTIFICATION = Application.getInstance().getString(R.string.statistics_receive_pn_norification);
    private static final String EVENT_LABLE_CLICK_APP = Application.getInstance().getString(R.string.statistics_app_click);
    private static final String EVENT_LABLE_MSG_READ = Application.getInstance().getString(R.string.statistics_msg_read);
    private static final String EVENT_LABLE_MSG_SHARE = Application.getInstance().getString(R.string.statistics_msg_share);
    private static final String EVENT_LABLE_SERVICE_ACCOUNT = Application.getInstance().getString(R.string.statistics_service_account);
    private static final String EVENT_LABLE_MSG_SEND = Application.getInstance().getString(R.string.statistics_msg_read);
    private static final String EVENT_LABLE_DELETE_THREADS = Application.getInstance().getString(R.string.statistics_delete_threads);
    private static final String EVENT_LABLE_UUID = Application.getInstance().getString(R.string.statistics_uuid);
    private static final String EVENT_LABLE_ACCOUNT_STATUS = Application.getInstance().getString(R.string.statistics_account_status);

    public static final class openType {
        public static final String smsList = "1";
        public static final String notification = "2";
        public static final String searchList = "3";
        public static final String systemSetting = "4";
    }
    
    public static final class shareChannel {
        public static final String weixin = "1";
        public static final String facebook = "2";
        public static final String twitter = "3";
        public static final String weibo = "4";
    }
    
    public static final class threadDeleteType{
        public static final String cv = "conversationList";
        public static final String clearMsg = "clearMsg";
    }

    private static final String KEY_MSG_ID = "msgid";
    private static final String KEY_MSG_TYPE = "msgtype";
    private static final String KEY_UUID = "UUID";
    private static final String KEY_CONTACT_ID = "contactId";
    private static final String KEY_ENTRANCE_TYPE = "entranceType";
    private static final String KEY_MSG_SUB_ID = "msgSubId";
    private static final String KEY_MSG_SEND_TYPE = "sendMsgType";
    private static final String KEY_MSG_SHARE_CHANNEL = "shareChannel";
    private static final String KEY_THREAD_DELETE_TYPE = "threadDeleteType";

    public static statistics mInstance = null;

    public static statistics getInstance() {
        if (mInstance == null) {
            mInstance = new statistics();
        }
        return mInstance;
    }

    public void receiveMsgSuccess(String msgId, String MsgType) {
        Map<String, String> kv = new HashMap<String, String>();
        kv.put(KEY_MSG_ID, msgId);
        kv.put(KEY_MSG_TYPE, MsgType);
        SDK.onEvent(Application.getInstance(), EVENT_ID_RECEIVE_MSG_SUCCESS,
                EVENT_LABLE_RECEIVE_MSG_SUCCESS, kv);
    }

    public void receivePnNotification(List<String> msgIds) {
        for (String msgid : msgIds) {
            Map<String, String> kv = new HashMap<String, String>();
            kv.put(KEY_MSG_ID, msgid);
            SDK.onEvent(Application.getInstance(), EVENT_ID_RECEIVE_PN_NOTIFICATION,
                    EVENT_LABLE_RECEIVE_PN_NOTIFICATION, kv);
        }
    }

    public void appClick(String conactID, String entranceType) {
        Map<String, String> kv = new HashMap<String, String>();
        kv.put(KEY_CONTACT_ID, conactID);
        kv.put(KEY_ENTRANCE_TYPE, entranceType);
        SDK.onEvent(Application.getInstance(), EVENT_ID_CLICK_APP, EVENT_LABLE_CLICK_APP, kv);

    }

    public void msgRead(String msgId, String msgSubId) {
        Map<String, String> kv = new HashMap<String, String>();
        kv.put(KEY_MSG_ID, msgId);
        if (!msgSubId.isEmpty()) {
            kv.put(KEY_MSG_SUB_ID, msgSubId);
        }
        SDK.onEvent(Application.getInstance(), EVENT_ID_MSG_READ, EVENT_LABLE_MSG_READ, kv);
    }

    public void msgShare(String msgId, int msgSubId,String shareChannel) {
        if(msgId.isEmpty()){
            return;
        }
        Map<String, String> kv = new HashMap<String, String>();
        kv.put(KEY_MSG_ID, msgId);
        if(msgSubId >=0){
            kv.put(KEY_MSG_SUB_ID, String.valueOf(msgSubId));
        }
        kv.put(KEY_MSG_SHARE_CHANNEL, shareChannel);
        SDK.onEvent(Application.getInstance(), EVENT_ID_MSG_SHARE, EVENT_LABLE_MSG_SHARE, kv);
    }
    
    public void accountIsExistInPhoneBook(List<ServiceInfo> accountList) {
        Map<String, String> kv = new HashMap<String, String>();
        for(ServiceInfo account : accountList){
            kv.put(account.getAccount(), String.valueOf(account.getIsExist()));
        }
        SDK.onEvent(Application.getInstance(), EVENT_ID_SERVICE_ACCOUNT, EVENT_LABLE_SERVICE_ACCOUNT, kv);
    }
    
    public void msgSend(String msgId,String msgTye) {
        Map<String, String> kv = new HashMap<String, String>();
        kv.put(KEY_MSG_ID,msgId);
        kv.put(KEY_MSG_SEND_TYPE,msgTye);
        SDK.onEvent(Application.getInstance(), EVENT_ID_MSG_SEND, EVENT_LABLE_MSG_SEND, kv);
    }
    
    public void threadsDelete(String accountName,String type) {
        Map<String, String> kv = new HashMap<String, String>();
        kv.put(KEY_CONTACT_ID,accountName);
        kv.put(KEY_THREAD_DELETE_TYPE, type);
        SDK.onEvent(Application.getInstance(), EVENT_ID_DELETE_THREADS, EVENT_LABLE_DELETE_THREADS, kv);
    }
    
    public void uuid(String uuid) {
        Map<String, String> kv = new HashMap<String, String>();
        kv.put(KEY_UUID,uuid);
        SDK.onEvent(Application.getInstance(), EVENT_ID_UUID, EVENT_LABLE_UUID, kv);
    }
    
    public void accountStatus(String account, String status) {
        Map<String, String> kv = new HashMap<String, String>();
        kv.put(account,status);
        SDK.onEvent(Application.getInstance(), EVENT_ID_ACCOUNT_STATUS, EVENT_LABLE_ACCOUNT_STATUS, kv);
    }
}