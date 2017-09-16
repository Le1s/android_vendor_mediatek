package com.mediatek.backuprestore;

import com.mediatek.backuprestore.utils.Constants.ContactType;

public class ContactItemData {

    private int mSimId;
    private boolean mIsChecked;
    private String mContactName;
    private int msimIconRes;
    private boolean mIsShow;

    public ContactItemData(int simId, boolean isChecked, String contactName, int simIconRes) {
        mSimId = simId;
        mIsChecked = isChecked;
        mContactName = contactName;
        msimIconRes = simIconRes;
    }

    public ContactItemData(int simId, boolean isChecked, String contactName, int simIconRes, boolean show) {
        mSimId = simId;
        mIsChecked = isChecked;
        mContactName = contactName;
        msimIconRes = simIconRes;
        mIsShow = show;
    }

    public int getSimId() {
        return mSimId;
    }

    public String getmContactName() {
        return mContactName;
    }

    public boolean getIsShow() {
        return mIsShow;
    }

    public int getIconId() {
        return msimIconRes;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
    }
}
