package com.hesine.nmsg.api;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.hesine.nmsg.bean.ServiceInfo;
import com.hesine.nmsg.db.DBUtils;
import com.hesine.nmsg.service.NmsgService;
import com.hesine.nmsg.util.MLog;

public class NmsgApiProvider extends ContentProvider {
    private static final int FUNC_ID_nmsgServiceIsReady = 1;
    private static final int FUNC_ID_isNmsgNumber = 2;
    public static final String AUTH = "om.hesine.remote.api.providers";
    public final Uri uri = Uri.parse("content://" + AUTH);
    public static List<String> accounts = new ArrayList<String>();
    public static final String TAG = "NmsgApiProvider";

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        // TODO Auto-generated method stub
        if(!NmsgService.getInstance().isServiceStart()){
            MLog.error(TAG, "NmsgService is not running, so start it.");
            Intent i = new Intent(this.getContext(), NmsgService.class);
            this.getContext().startService(i);
        }
        Bundle retBundle = new Bundle();
        switch (Integer.parseInt(method)) {
        case FUNC_ID_nmsgServiceIsReady:
            retBundle.putBoolean(method, NmsgService.getInstance().isServiceStart());
            break;
        case FUNC_ID_isNmsgNumber:
            String number = extras.getString(method + 1);
            if (null!= accounts && accounts.contains(number)) {
                retBundle.putBoolean(method, true);
            } else {
                ServiceInfo si = DBUtils.getServiceInfo(number);
                if(null != si){
                    retBundle.putBoolean(method, true);
                    accounts.add(si.getAccount());
                }else{
                    retBundle.putBoolean(method, false);
                }
            }
            break;
        }
        return retBundle;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}