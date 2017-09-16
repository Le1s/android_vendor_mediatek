package com.mediatek.smsreg.test;

import android.test.AndroidTestCase;

import com.android.internal.telephony.IccCardConstants;
import com.mediatek.smsreg.SmsRegConst;
import com.mediatek.smsreg.test.util.MockSmsRegUtil;

public class SmsRegConstTest extends AndroidTestCase {
    public static final String TAG = "SmsReg/ConstTest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test00() {
        MockSmsRegUtil.formatLog(TAG, "test00");
        new SmsRegConst();

        assertEquals(SmsRegConst.GEMINI_SIM_1, 0);
        assertEquals(SmsRegConst.GEMINI_SIM_2, 1);
        assertEquals(SmsRegConst.GEMINI_SIM_3, 2);
        assertEquals(SmsRegConst.GEMINI_SIM_4, 3);
        assertEquals(SmsRegConst.GEMSIM[0], 0);
        assertEquals(SmsRegConst.GEMSIM[1], 1);
        assertEquals(SmsRegConst.GEMSIM[2], 2);
        assertEquals(SmsRegConst.GEMSIM[3], 3);

        assertEquals(SmsRegConst.SIM_STATE_READY, 5);
        assertEquals(SmsRegConst.ACTION_BOOT_COMPLETED, "android.intent.action.BOOT_COMPLETED");
        assertEquals(SmsRegConst.ACTION_SIM_STATE_CHANGED,
                "android.intent.action.SIM_STATE_CHANGED");

        assertEquals(SmsRegConst.KEY_ICC_STATE, IccCardConstants.INTENT_KEY_ICC_STATE);
        assertEquals(SmsRegConst.VALUE_ICC_LOADED, IccCardConstants.INTENT_VALUE_ICC_LOADED);

        assertEquals(SmsRegConst.ACTION_RETRY_SEND_SMS, "com.mediatek.smsreg.RETRY_SEND_SMS");
        assertEquals(SmsRegConst.ACTION_FINISH_SEND_SMS, "com.mediatek.smsreg.FINISH_SEND_SMS");
        assertEquals(SmsRegConst.ACTION_DISPLAY_DIALOG,
                "com.mediatek.smsreg.DISPLAY_CONFIRM_DIALOG");
        assertEquals(SmsRegConst.ACTION_RESPONSE_DIALOG,
                "com.mediatek.smsreg.RESPONSE_CONFIRM_DIALOG");

        assertEquals(SmsRegConst.EXTRA_IMSI, "extra_imsi");
        assertEquals(SmsRegConst.EXTRA_RESULT_CODE, "extra_result_code");
        assertEquals(SmsRegConst.EXTRA_IS_NEED_SEND, "extra_is_need_send");
        assertEquals(SmsRegConst.DM_SMSREG_MESSAGE_NEW, "com.mediatek.mediatekdm.smsreg.new");
    }
}
