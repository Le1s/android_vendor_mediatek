package com.hesine.nmsg.api;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.hesine.nmsg.util.MLog;
public class NmsgApi {
    private static final String FUNC_ID_nmsgServiceIsReady = "1";
    private static final String FUNC_ID_isNmsgNumber = "2";
    private String AUTH = "com.hesine.remote.api.providers";
    public final Uri API_CONTENT_URI = Uri.parse("content://" + AUTH);
    private static NmsgApi mInstance = null;
    private Context mContext = null;
    private ContentResolver mApiProviders = null;
    private static final String TAG = "NmsgApi";

	private NmsgApi(Context context) {
		mContext = context;
		mApiProviders = mContext.getContentResolver();
	}

    public static synchronized NmsgApi getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NmsgApi(context);
        }
        return mInstance;
    }

    public boolean isNmsgServiceReady() {
        try {
            Bundle back = mApiProviders.call(API_CONTENT_URI, FUNC_ID_nmsgServiceIsReady, null,
                    null);
            if (back != null) {
                return back.getBoolean(FUNC_ID_nmsgServiceIsReady, false);
            } else {
                return false;
            }
        } catch (Exception e) {
            MLog.PrintStackTrace(e);
            return false;
        }
    }
    public void checkNmsgService(Context context){
        if(!isNmsgServiceReady()){
            MLog.trace(TAG, "checkNmsgService is not running so start it");
            Intent i = new Intent();
            i.setAction("com.hesine.nmsg.startservice");
            context.sendBroadcast(i);
            return;
        }
    }

    public boolean isNmsgNumber(String number) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(FUNC_ID_isNmsgNumber + 1, number);
            Bundle back = mApiProviders.call(API_CONTENT_URI, FUNC_ID_isNmsgNumber, null, bundle);
            if (back != null) {
                return back.getBoolean(FUNC_ID_isNmsgNumber, false);
            } else {
                return false;
            }
        } catch (Exception e) {
            MLog.PrintStackTrace(e);
            return false;
        }
    }

    public boolean startConversationActivity(Context context, long threadId, String number,
            String openType) {
        if (isNmsgNumber(number)) {
            try {
                Intent intent = new Intent();
                intent.setAction("com.hesine.nmsg.activity.ConversationActivity");
                intent.setClassName("com.hesine.nmsg",
                        "com.hesine.nmsg.activity.ConversationActivity");
                intent.putExtra("thread_id", threadId);
                intent.putExtra("phone_number", number);
                intent.putExtra("open_type", openType);
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                MLog.PrintStackTrace(e);
            }
        }
        return false;
    }

}