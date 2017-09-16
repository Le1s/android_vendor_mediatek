package com.mediatek.settings.plugin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mediatek.op09.plugin.R;
import com.mediatek.settings.ext.DefaultFactoryExt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * for factory reset CT feature.
 */
public class OP09FactoryExt extends DefaultFactoryExt {

    private static final String TAG = "OP09FactoryExt";

    private static final int NOT_CHECKED = 0;
    private static final int PERSONAL_DATA_CHECKED = 1;
    private static final int INSTALL_APP_CHECKED = 2;
    private static final int MEDIA_DATA_CHECKED = 4;
    // Not encrypted view container is put in list's first position, save its index here
    private static final int NOT_ENCRYPTED_VIEW = 0;
    private static final String ACCOUNT_DB_PATH = "/data/system/users/0/accounts.db";
    private static final String ACCOUNT_XML_PATH = "/data/system/sync/accounts.xml";
    private static final String CONTACTS_PATH =
        "/data/data/com.android.providers.contacts/databases/contacts2.db";
    private static final String MMS_PATH =
        "/data/data/com.android.providers.telephony/databases/mmssms.db";
    private static final String CONTACTS_PROFILE_PATH =
        "/data/data/com.android.providers.contacts/databases/profile.db";

    private static final String DATA_APP = "/data/app/";
    private static final String DATA_APP_LIB = "/data/app-lib/";
    private static final String DATA_ASEC = "/data/asec/";
    private static final String DATA_DATA = "/data/data/";
    private static final String PACKAGES_XML = "/data/system/packages.xml";
    private static final String DATA_USER = "/data/user/";

    private static final String DATA_MEDIA = "/data/media/";

    private static final String KEEP_LIST_FILE = "/data/app/.keep_list";
    private static final String DELETE_LIST_FILE = "/data/app/.del_list";

    private Context mContext;
    private LinearLayout mEncryptedContainer;
    private LinearLayout mInternalEraseContainer;

    private View mEraseInternalPersonalData;
    private CheckBox mPersonalDataCheckbox;
    private View mEraseInternalInstallApp;
    private CheckBox mInstallAppCheckbox;
    private View mMediaData;
    private CheckBox mMediaDataCheckbox;
    private boolean mIsEncrypted = false;

    /**
     * Constructor method.
     * @param context is Settings's context.
     */
    public OP09FactoryExt(Context context) {
        super(context);
        mContext = context;
        Log.d(TAG, "OP09FactoryExt");
    }

    private View getInternalStorageView() {
        View view = null;
        LayoutInflater inflater =
            (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DevicePolicyManager dpm =
            (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        switch (dpm.getStorageEncryptionStatus()) {
            case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE:
                // The device is currently encrypted.
                Log.d(TAG, "The device is currently encrypted");
                mIsEncrypted = true;
                view = inflater.inflate(R.layout.encrypted_factory, null);
                break;
            case DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE:
                // This device supports encryption but isn't encrypted.
                Log.d(TAG, "This device supports encryption but isn't encrypted");
                mIsEncrypted = false;
                view = inflater.inflate(R.layout.factory, null);
                mEraseInternalPersonalData = view.findViewById(R.id.erase_internal_personal_data);
                mPersonalDataCheckbox = (CheckBox) view.findViewById(R.id.personal_data_checkbox);
                mEraseInternalInstallApp = view.findViewById(R.id.erase_internal_install_app);
                mInstallAppCheckbox = (CheckBox) view.findViewById(R.id.install_app_checkbox);
                mMediaData = view.findViewById(R.id.ct_erase_external_container);
                mMediaDataCheckbox = (CheckBox) view.findViewById(R.id.ct_erase_external);
                mEraseInternalInstallApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mInstallAppCheckbox.toggle();
                    }
                });
                mMediaData.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMediaDataCheckbox.toggle();
                    }
                });
                break;
            default:
                break;
        }
        return view;
    }

    private boolean isEncrypted() {
        return mIsEncrypted;
    }

    @Override
    public int getCheckBoxStatus() {
        int status = 0;
        if (mPersonalDataCheckbox != null &&
                mInstallAppCheckbox != null &&
                mMediaDataCheckbox != null) {
            int data = (mPersonalDataCheckbox.isChecked() ? PERSONAL_DATA_CHECKED : NOT_CHECKED);
            int app = (mInstallAppCheckbox.isChecked() ? INSTALL_APP_CHECKED : NOT_CHECKED);
            int media = (mMediaDataCheckbox.isChecked() ? MEDIA_DATA_CHECKED : NOT_CHECKED);
            status = data | app | media;
            Log.d(TAG, "data = " + data);
            Log.d(TAG, "app = " + app);
            Log.d(TAG, "media = " + media);
            Log.d(TAG, "(data | app | media) " + status);
        }
        if (mIsEncrypted) {
            Log.d(TAG, "The phone has encrypted: pre status" + status);
            status = PERSONAL_DATA_CHECKED | INSTALL_APP_CHECKED ;
            Log.d(TAG, "The phone has encrypted: reset status" + status);
        }
        return status;
    }

    @Override
    public boolean onClick(int eraseInternalData) {
        if (!writePathFile(eraseInternalData)) {
            Toast.makeText(mContext, R.string.factory_failed, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean writePathFile(int eraseInternalData) {
        Log.d(TAG, "(data | app | media), eraseInternalData = " + eraseInternalData);
        File keepListFile = null;
        FileOutputStream out = null;
        File deleteListFile = null;
        FileOutputStream deleOut = null;
        keepListFile = new File(KEEP_LIST_FILE);
        deleteListFile = new File(DELETE_LIST_FILE);
        if (keepListFile.exists()) {
            Log.d(TAG, "delete keep file");
            if (!keepListFile.delete()) {
                Log.d(TAG, "delete keep file failed");
                return false;
            }
        }
        if (deleteListFile.exists()) {
            Log.d(TAG, "delete delete file");
            if (!deleteListFile.delete()) {
                Log.d(TAG, "delete delete file failed");
                return false;
            }
        }
        try {
            if (eraseInternalData != (PERSONAL_DATA_CHECKED |
                                        INSTALL_APP_CHECKED |
                                        MEDIA_DATA_CHECKED)) {
                if (!keepListFile.exists()) {
                    if (!keepListFile.createNewFile()) {
                        Log.d(TAG, "create keepFile failed");
                        return false;
                    }
                }
                out = new FileOutputStream(keepListFile, false);
                if ((eraseInternalData & PERSONAL_DATA_CHECKED) != PERSONAL_DATA_CHECKED) {
                    StringBuffer persistPath = new StringBuffer();
                    persistPath.append(ACCOUNT_DB_PATH + "\n");
                    persistPath.append(ACCOUNT_XML_PATH + "\n");
                    persistPath.append(CONTACTS_PATH + "\n");
                    persistPath.append(CONTACTS_PROFILE_PATH + "\n");
                    persistPath.append(MMS_PATH + "\n");
                    Log.d(TAG, "personalData = " + persistPath.toString());
                    out.write(persistPath.toString().getBytes());
                }
                if ((eraseInternalData & INSTALL_APP_CHECKED) != INSTALL_APP_CHECKED) {
                    StringBuffer persistPath = new StringBuffer();
                    boolean isThreeAppExists = false;
                    final PackageManager pm = mContext.getPackageManager();
                    final List<PackageInfo> infos = pm.getInstalledPackages(0);
                    List<String> thirdPartyPackages = new ArrayList<String>();
                    for (PackageInfo info: infos) {
                        if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                                && (info.applicationInfo.flags &
                                    ApplicationInfo.FLAG_OPERATOR) == 0) {
                            Log.d(TAG, "Three APP: = " + DATA_DATA +
                                    info.applicationInfo.packageName);
                            persistPath.append(DATA_DATA +
                                    info.applicationInfo.packageName + "/" + "\n");
                            thirdPartyPackages.add(info.packageName);
                            isThreeAppExists = true;
                        }
                    }
                    if (isThreeAppExists) {
                        Log.d(TAG, "Three APP is exists : = " + isThreeAppExists);
                        persistPath.append(DATA_APP + "\n");
                        persistPath.append(DATA_APP_LIB + "\n");
                        persistPath.append(DATA_ASEC + "\n");
                        persistPath.append(PACKAGES_XML + "\n");
                        persistPath.append(DATA_USER + "\n");
                        Log.d(TAG, "installAPP = " + persistPath.toString());
                        if ((eraseInternalData & PERSONAL_DATA_CHECKED) == PERSONAL_DATA_CHECKED) {
                            persistPath.append(ACCOUNT_DB_PATH + "\n");
                            persistPath.append(ACCOUNT_XML_PATH + "\n");
                            Log.d(TAG, "Account, installAPP = " + persistPath.toString());
                        }
                        out.write(persistPath.toString().getBytes());
                    }
                    if ((eraseInternalData & PERSONAL_DATA_CHECKED) == PERSONAL_DATA_CHECKED) {
                        if (!deleteListFile.exists()) {
                            if (!deleteListFile.createNewFile()) {
                                Log.d(TAG, "create deleFile failed");
                                return false;
                            }
                        }
                        deleOut = new FileOutputStream(deleteListFile, false);
                        StringBuffer delePath = new StringBuffer();
                        //delePath.append(ACCOUNT_DB_PATH + "\n");
                        //delePath.append(ACCOUNT_XML_PATH + "\n");
                        delePath.append(CONTACTS_PATH + "\n");
                        delePath.append(CONTACTS_PROFILE_PATH + "\n");
                        delePath.append(MMS_PATH + "\n");
                        Log.d(TAG, "delePath = " + delePath.toString());
                        deleOut.write(delePath.toString().getBytes());
                        // remove system account
                        removeAccounts(thirdPartyPackages);
                    }
                }
                // If sd share feature option is on,
                // /data/media and /storage/sdcard0 share the same storage path in sdcard,
                //so we should avoid format data/media if user did not select to format media files
                if (Utils.isMtkSharedSdcardSupport() &&
                        ((eraseInternalData & MEDIA_DATA_CHECKED) != MEDIA_DATA_CHECKED)) {
                    String persistPath = new String(DATA_MEDIA + "\n");
                    Log.d(TAG, "media Data = " + persistPath.toString());
                    out.write(persistPath.toString().getBytes());
                }
            } else {
                Log.d(TAG, "eraseInternalData = 0x11, eraseInternalData =" + eraseInternalData);
                Log.d(TAG, "write a null .keep_list file");
                if (!keepListFile.exists()) {
                    if (!keepListFile.createNewFile()) {
                        Log.d(TAG, "create keepFile failed");
                        return false;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (deleOut != null) {
                    deleOut.close();
                }
            } catch (IOException exception) {
                Log.d(TAG, "IOException");
                exception.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public void setLayout(List<View> lists) {
        LinearLayout eraseInternalLayout = null;
        View internalStorageView = getInternalStorageView();
        if (internalStorageView != null) {
            Log.d(TAG, "The device is currently encrypted: " + isEncrypted());
            eraseInternalLayout = isEncrypted() ? mEncryptedContainer : mInternalEraseContainer;
            for (int i = 0; i < lists.size(); i++) {
                lists.get(i).setVisibility(View.GONE);
            }
            lists.get(NOT_ENCRYPTED_VIEW).setVisibility((isEncrypted() ? View.GONE : View.VISIBLE));
            eraseInternalLayout.setVisibility(View.VISIBLE);
            eraseInternalLayout.addView(internalStorageView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }
    @Override
    public void updateContentViewLayout(ViewGroup container, int siberViewId) {
        // Add encrypted container view
        if (container.findViewById(R.id.encrypted_erase) == null) {
            if (mEncryptedContainer == null) {
                mEncryptedContainer = new LinearLayout(container.getContext());
                LinearLayout.LayoutParams layoutParam =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                mEncryptedContainer.setLayoutParams(layoutParam);
                mEncryptedContainer.setId(R.id.encrypted_erase);
            }
            container.addView(mEncryptedContainer, 0);
        }

        // Add internal erase container view
        if (container.findViewById(R.id.erase_internal_field) == null) {
            View siberView = container.findViewById(siberViewId);
            ViewGroup parent = (ViewGroup) siberView.getParent();
            int siberViewIndex = -1;
            int count = parent.getChildCount();
            for (int index = 0; index < count; index++) {
                if (parent.getChildAt(index) == siberView) {
                    siberViewIndex = index;
                    break;
                }
            }
            if (mInternalEraseContainer == null) {
                if (siberViewIndex > -1) {
                    mInternalEraseContainer = new LinearLayout(container.getContext());
                    LinearLayout.LayoutParams layoutParam =
                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                    mInternalEraseContainer.setLayoutParams(layoutParam);
                    mInternalEraseContainer.setId(R.id.erase_internal_field);
                }
            }
            parent.addView(mInternalEraseContainer, siberViewIndex);
        }
    }

    private void removeAccounts(List<String> thirdPartyPackages) {
        List<String> thirdPartyPackageTypes = new ArrayList<String>();
        AccountManager accountManager = AccountManager.get(mContext);
        AuthenticatorDescription[] accountDescriptions = accountManager.getAuthenticatorTypes();
        for (AuthenticatorDescription desc : accountDescriptions) {
            if (thirdPartyPackages != null && thirdPartyPackages.size() != 0
                    && thirdPartyPackages.contains(desc.packageName)) {
                thirdPartyPackageTypes.add(desc.type);
                Log.d(TAG, "package name is :" + desc.packageName + "type is" + desc.type);
            }
        }

        Account[] accounts = AccountManager.get(mContext).getAccounts();
        for (Account account : accounts) {
            if (thirdPartyPackageTypes == null || thirdPartyPackageTypes.size() == 0
                    || !thirdPartyPackageTypes.contains(account.type)) {
                Log.d(TAG, "remove account is : " + account.toString());
                accountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                    @Override
                    public void run(AccountManagerFuture<Boolean> future) {
                        try {
                            if (!future.getResult()) {
                                Log.d(TAG, "remove account is failed");
                            }
                        } catch (OperationCanceledException e) {
                            e.printStackTrace();
                        } catch (AuthenticatorException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
            }
        }
    }

    /**
     * CT Factory reset feature refactory.
     * @param activity which will startService or sendBroadcast
     * @param eraseInternalData data | app | media
     * @param eraseSdCard media has check or use in DefaultFactoryExt.java
     */
    @Override
    public void doMasterClear(Activity activity, int eraseInternalData, boolean eraseSdCard) {
        if (!onClick(eraseInternalData)) {
            return;
        }
        if ((eraseInternalData & MEDIA_DATA_CHECKED) == MEDIA_DATA_CHECKED) {
            Log.d(TAG, "mEraseInternalData = " +
                    eraseInternalData + ", mEraseSdCard = " + eraseSdCard);
            eraseSdCard = true;
        }
        super.doMasterClear(activity, eraseInternalData, eraseSdCard);
    }
}
