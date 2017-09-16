package com.mediatek.dialer.plugin.calllog;

import android.content.Intent;

import com.android.dialer.calllog.ContactInfo;

public final class CallLogInfo {

    public ContactInfo mContactInfo;
    public Intent mCallDetailIntent;

    public CallLogInfo(ContactInfo contactInfo, Intent callDetailIntent) {
        mContactInfo = contactInfo;
        mCallDetailIntent = callDetailIntent;
    }
}
