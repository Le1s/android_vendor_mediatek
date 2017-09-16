package com.hesine.nmsg.db;

import java.util.Set;

import android.text.TextUtils;

public class NmsSendMessage {

    public final static int UNSAVE_SMS = 0x00;
    public final static int SAVE_SMS = 0x01;
    public final static int MAX_SMS_LENGTH = 70;
    public final static String SMS_ID = "id";
    public final static String SMS_SENT_ACTION = "com.hissage.nmssendmessage.sent_action";
    public final static String SMS_DELIVER_ACTION = "com.hissage.nmssendmessage.deliver_action";

    public static NmsSendMessage mInstance = null;

    class SmsCont {
        SmsCont(String strAddrIn, String strMsgIn, long lThreadId, long lSmsIdIn, long simId) {
            strAddr = strAddrIn;
            strMsg = strMsgIn;
            lSmsId = lSmsIdIn;
            threadId = lThreadId ;
            this.simId = simId;
        }

        public String strAddr;
        public String strMsg;
        public long lSmsId;
        public long threadId ;
        public long simId;
    }
    
    public static NmsSendMessage getInstance() {
        if (null == mInstance) {
            mInstance = new NmsSendMessage();
        }
        return mInstance;
    }

    public boolean isAddressLegal(String strAddrIn, Set<String> setAddrIn) {
        if (TextUtils.isEmpty(strAddrIn)) {
            return false;
        }
        String[] strArrayAddr = strAddrIn.split(",");
        for (String strAddrTemp : strArrayAddr) {
             setAddrIn.add(strAddrTemp);
        }
        if (setAddrIn.size() > 0) {
            return true;
        }
        return false;
    }

}
