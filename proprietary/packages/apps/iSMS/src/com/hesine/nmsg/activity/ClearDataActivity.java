package com.hesine.nmsg.activity;

import java.util.HashSet;

import android.app.Activity;
import android.os.Bundle;

import com.hesine.nmsg.db.DBUtils;
import com.hesine.nmsg.db.NmsMtkBinderApi;
import com.hesine.nmsg.db.NmsSMSMMS;

public class ClearDataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HashSet<Long> threads = DBUtils.getMessageThreads();
        DBUtils.deleteAllData();

        for (Long thread : threads) {
            NmsMtkBinderApi.getInstance().delete(NmsSMSMMS.SMS_CONTENT_URI, "thread_id = ?",
                    new String[] { String.valueOf(thread) });
        }
        finish();
    }

}