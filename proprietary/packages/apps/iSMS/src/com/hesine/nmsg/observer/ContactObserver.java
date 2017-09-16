package com.hesine.nmsg.observer;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;

import com.hesine.nmsg.Application;
import com.hesine.nmsg.bean.ServiceInfo;
import com.hesine.nmsg.db.DBUtils;
import com.hesine.nmsg.statistics.statistics;
import com.hesine.nmsg.util.MLog;

public class ContactObserver extends ContentObserver {
    public static final String TAG = "NmsgContactObserver";

    public ContactObserver() {
        super(null);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        MLog.trace(TAG, "receive contact onchange");
        new Thread(new Runnable() {

            @Override
            public void run() {
                processContactChange();
            }
        }).start();

    }

    public synchronized void processContactChange() {
        ArrayList<ServiceInfo> serviceInfoList = DBUtils.getServiceInfos();
        ContentResolver resolver = Application.getInstance().getContentResolver();

        for (ServiceInfo serviceInfo : serviceInfoList) {
            boolean isExist = false;
            final Uri uri = Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI,
                    Uri.encode(serviceInfo.getAccount()));
            final Cursor c = resolver.query(uri, null, null, null, null);
            while (c != null && c.moveToNext()) {
                if (c.getCount() > 0) {
                    String name = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    if (serviceInfo.getName().equals(name)) {
                        isExist = true;
                    }
                }
            }
            if (c != null) {
                c.close();
            }
            DBUtils.updateServiceInfo(serviceInfo.getAccount(), isExist);
        }
        serviceInfoList = DBUtils.getServiceInfos();
        statistics.getInstance().accountIsExistInPhoneBook(serviceInfoList);
        // TODO
    }
}