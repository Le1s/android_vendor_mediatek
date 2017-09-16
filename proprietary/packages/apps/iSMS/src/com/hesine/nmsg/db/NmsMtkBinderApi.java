package com.hesine.nmsg.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.hesine.nmsg.Application;
import com.hesine.nmsg.util.MLog;

public class NmsMtkBinderApi {

    private static final String FUNC_ID_INSERT = "1";
    private static final String FUNC_ID_DELETE = "2";
    private static final String FUNC_ID_UPDATE = "3";
    private static final String FUNC_ID_GET_OR_CREATE_THREADID = "4";

    private String AUTH = "com.mediatek.nmsg.util.nmsg.providers";
    private final Uri API_CONTENT_URI = Uri.parse("content://" + AUTH);

    private ContentResolver mApiProviders = null;

    private static NmsMtkBinderApi mInstance = null;

    private NmsMtkBinderApi() {
        mApiProviders = Application.getInstance().getContentResolver();
    }

    public static NmsMtkBinderApi getInstance() {
        if (mInstance == null) {
            synchronized (NmsMtkBinderApi.class) {
                if (mInstance == null) {
                    try {
                        mInstance = new NmsMtkBinderApi();
                    } catch (Exception e) {
                    }
                }
            }
        }

        return mInstance;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // if (NmsConfig.isAndroidKKVersionOnward) {
        Cursor cursor = null;

        try {
            cursor = mApiProviders.query(uri, projection, selection, selectionArgs, sortOrder);
            return cursor;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
    }
    
    public static boolean versionAfterKitkat() {
    	final int version = android.os.Build.VERSION.SDK_INT; 
    	return version >= 19;
    }

    public Uri insert(Uri uri, ContentValues values) {
        try {
            if (versionAfterKitkat()) {
                Bundle param = new Bundle();
                param.putParcelable(FUNC_ID_INSERT + 1, uri);
                param.putParcelable(FUNC_ID_INSERT + 2, values);
                Bundle back = mApiProviders.call(API_CONTENT_URI, FUNC_ID_INSERT, null, param);
                return (Uri) back.getParcelable(FUNC_ID_INSERT);
            } else {
                return mApiProviders.insert(uri, values);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public int delete(Uri uri, String where, String[] selectionArgs) {

        try {
            if (versionAfterKitkat()) {
                Bundle param = new Bundle();
                param.putParcelable(FUNC_ID_DELETE + 1, uri);
                param.putString(FUNC_ID_DELETE + 2, where);
                param.putStringArray(FUNC_ID_DELETE + 3, selectionArgs);
                Bundle back = mApiProviders.call(API_CONTENT_URI, FUNC_ID_DELETE, null, param);
                return back.getInt(FUNC_ID_DELETE);
            } else {
                return mApiProviders.delete(uri, where, selectionArgs);
            }

        } catch (Exception e) {
            MLog.PrintStackTrace(e);
            return -1;
        }
    }

    public int update(Uri uri, ContentValues values, String where, String[] selectionArgs) {
        try {
            if (versionAfterKitkat()) {
                Bundle param = new Bundle();
                param.putParcelable(FUNC_ID_UPDATE + 1, uri);
                param.putParcelable(FUNC_ID_UPDATE + 2, values);
                param.putString(FUNC_ID_UPDATE + 3, where);
                param.putStringArray(FUNC_ID_UPDATE + 4, selectionArgs);
                Bundle back = mApiProviders.call(API_CONTENT_URI, FUNC_ID_UPDATE, null, param);
                return back.getInt(FUNC_ID_UPDATE);
            } else {
                return mApiProviders.update(uri, values, where, selectionArgs);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    public long getOrCreateThreadId(Uri uri) {
        if (versionAfterKitkat()) {
            try {
                Bundle param = new Bundle();
                param.putParcelable(FUNC_ID_GET_OR_CREATE_THREADID + 1, uri);
                Bundle back = mApiProviders.call(API_CONTENT_URI, FUNC_ID_GET_OR_CREATE_THREADID,
                        null, param);
                return back.getLong(FUNC_ID_GET_OR_CREATE_THREADID);
            } catch (Exception e) {
            }
        } else {
            String[] proj = { "_id" };
            Cursor cursor = NmsContentResolver.query(mApiProviders, uri, proj, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        Long ret = cursor.getLong(0);
                        cursor.close();
                        return ret;
                    }
                } catch (Exception e) {
                }

                if (cursor != null)
                    cursor.close();
            }
        }

        return -1;
    }
}
