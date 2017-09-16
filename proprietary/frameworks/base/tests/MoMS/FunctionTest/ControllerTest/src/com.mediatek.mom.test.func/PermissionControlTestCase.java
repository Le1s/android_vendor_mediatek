/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.mom.test.func;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.Manifest.permission;
import android.os.Process;
import android.util.Log;

import com.mediatek.common.mom.IMobileManager;
import static com.mediatek.common.mom.IMobileManager.PERMISSION_STATUS_GRANTED;
import static com.mediatek.common.mom.IMobileManager.PERMISSION_STATUS_DENIED;
import static com.mediatek.common.mom.IMobileManager.PERMISSION_STATUS_CHECK;
import com.mediatek.common.mom.Permission;
import com.mediatek.common.mom.SubPermissions;

import static com.mediatek.mom.test.func.EmulatorManagerOps.prepareIntent;

public class PermissionControlTestCase extends AndroidTestCase {
    private static final String TAG = "PermissionControlTestCase";

    private PackageManager mPackageManager = null;

    private ArrayList<PackageInfo> mInstalledPackages = null;
    private ArrayList<Permission> mPkgGrantedPermissions = null;

    // The test reuslt passed from Target Emulator
    private int mResult = EmulatorManagerOps.RESULT_FAILED;

    // Time break between each testcases
    private static final int TIME_BREAK = 1000;

    // Attach Related
    private final Object ATTACH_LOCK = new Object();
    private int mExpectConnStatus = -1;
    private int mExpectUid = -1;
    private boolean mConnIntentReceived = false;

    // Intent extra keys
    public static final String EXTRA_MGR_OP = "extra_operation";
    public static final String EXTRA_PACKAGE = "extra_package";
    public static final String EXTRA_UID = "extra_uid";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_SUBPERMISSION = "extra_sub_permission";
    public static final String EXTRA_RESULT = "extra_result";
    public static final String EXTRA_ENABLE = "extra_enable";
    public static final String EXTRA_PARAM_1 = "extra_param_1";

    // Permission Callback Parameters
    public static final int CB_PARAM_NULL = 0;
    public static final int CB_PARAM_GRANTED = 1;
    public static final int CB_PARAM_DENIED = 2;

    private MyBroadcastReceiver mReceiver = new MyBroadcastReceiver();

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (EmulatorManagerOps.RESULT_INTENT.equals(action)) {
                boolean received = false;

                int operation = intent.getIntExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_NONE);
                Log.v(TAG, "onReceive() operation: " + operation);

                switch (operation) {
                case EmulatorManagerOps.MGR_OP_PMC_GET_INSTALLED_PACKAGES:
                    mInstalledPackages = intent.getParcelableArrayListExtra(EXTRA_RESULT);
                    Log.d(TAG, "mInstalledPackages: " + mInstalledPackages);
                    received = true;
                    break;
                case EmulatorManagerOps.MGR_OP_PMC_GET_RECORD:
                    mPkgGrantedPermissions = intent.getParcelableArrayListExtra(EXTRA_RESULT);
                    Log.d(TAG, "mPkgGrantedPermissions: " + mPkgGrantedPermissions);
                    received = true;
                    break;
                case EmulatorManagerOps.MGR_OP_PMC_CHECK_PERMISSION:
                    mResult = intent.getIntExtra(EXTRA_RESULT, -1);
                    Log.d(TAG, "mResult: " + mResult);
                    received = true;
                    break;
                case EmulatorManagerOps.MGR_OP_PMC_CHECK_PERMISSION_ASYNC:
                    mResult = intent.getIntExtra(EXTRA_RESULT, -1);
                    Log.d(TAG, "mResult: " + mResult);
                    received = true;
                    break;
                case EmulatorManagerOps.MGR_OP_PMC_SET_RECORD:
                case EmulatorManagerOps.MGR_OP_PMC_REGISTER_CB:
                case EmulatorManagerOps.MGR_OP_PMC_ENABLE_CONTROLLER:
                    // do nothing
                    received = true;
                    break;
                case EmulatorManagerOps.MGR_OP_PMC_DETACH:
                    // do nothing
                    received = true;
                    break;
                case EmulatorManagerOps.MGR_OP_PMC_ATTACH:
                    mResult = intent.getIntExtra(EXTRA_RESULT, -1);
                    received = true;
                    break;
                case EmulatorManagerOps.MGR_OP_PMC_CB_CONNECTION_ENDED:
                case EmulatorManagerOps.MGR_OP_PMC_CB_CONNECTION_RESUME:
                    // Ignored
                    break;
                default:
                }
                synchronized (mContext) {
                    // Notify the testcase
                    if (received) {
                        mContext.notify();
                    }
                }
            } else if (IMobileManager.ACTION_MGR_CHANGE.equals(action)) {
                synchronized (ATTACH_LOCK) {
                    int uid = intent.getIntExtra(IMobileManager.ACTION_EXTRA_UID, -1);
                    int status = intent.getIntExtra(IMobileManager.ACTION_EXTRA_STATUS, -1);
                    if (uid == mExpectUid && mExpectConnStatus == status) {
                        mConnIntentReceived = true;
                        mExpectConnStatus = -1;
                        ATTACH_LOCK.notify();
                        Log.v(TAG, "onReceive() PREM_MGR_CHANGE with uid: " + uid + " status: " + status);
                    }
                }
            } else if (EmulatorManagerOps.ACTION_TIMEOUT.equals(action)) {
                Log.e(TAG, "onReceive() Operation Timeout!");
                synchronized (mContext) {
                    mContext.notify();
                }
                synchronized (ATTACH_LOCK) {
                    ATTACH_LOCK.notify();
                }
                fail("Operation Timeout!");
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResult = EmulatorManagerOps.RESULT_FAILED;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EmulatorManagerOps.RESULT_INTENT);
        intentFilter.addAction(IMobileManager.ACTION_MGR_CHANGE);
        mContext.registerReceiver(mReceiver, intentFilter);
        mPackageManager = mContext.getPackageManager();

        // Install CTA test apk
        EmulatorManagerOps.installApkWait(mContext, EmulatorManagerOps.CTA_APK);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mContext.unregisterReceiver(mReceiver);
        mPackageManager = null;
        Thread.sleep(TIME_BREAK);
    }

    /**
     * Install CTA test apk and check it from MoMS.
     *
     * Output:
     *     mInstalledPackages: Should found CTA test apk.
     */
    public void test01_GetInstalledPackages() throws Exception {
        Log.d(TAG, "test01_GetInstalledPackages()");
        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_PMC_GET_INSTALLED_PACKAGES);

        synchronized (mContext) {
            mContext.startService(intent);
            mContext.wait();
        }

        if (mInstalledPackages != null) {
            for (int i = 0; i < mInstalledPackages.size(); i++) {
                PackageInfo pkgInfo = mInstalledPackages.get(i);
                if (EmulatorManagerOps.CTA_PACKAGE.equals(pkgInfo.packageName)) {
                    Log.v(TAG, "Found package: " + pkgInfo.packageName);
                    mResult = EmulatorManagerOps.RESULT_SUCCESS;
                    break;
                }
            }
        } else {
            Log.e(TAG, "Null mInstalledPackages");
        }

        assertEquals("testGetInstalledPackages() failed! CTA_PACKAGE should be found",
                EmulatorManagerOps.RESULT_SUCCESS, mResult);
    }

    /**
     * Make sure cta test apk covers all the CATR requested permissions
     */
    //private static final int NUM_SUBPERMISSION = 16;
    public void test02_GetPackageGrantedPermissions() throws Exception {
        Log.d(TAG, "test02_GetPackageGrantedPermissions()");
        int count = 0;

        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_PMC_GET_RECORD);
        intent.putExtra(EXTRA_PACKAGE, EmulatorManagerOps.CTA_PACKAGE);
        synchronized (mContext) {
            mContext.startService(intent);
            mContext.wait();
        }
        if (mPkgGrantedPermissions != null) {
            for (int i = 0; i < mPkgGrantedPermissions.size(); i++) {
                Permission p = mPkgGrantedPermissions.get(i);
                if (p.mPermissionName.equals(permission.READ_SMS)) {
                    Log.d(TAG, "Found android.Manifest.permission.READ_SMS");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.QUERY_SMS);
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.QUERY_MMS);
                } else if (p.mPermissionName.equals(permission.WRITE_SMS)) {
                    Log.d(TAG, "Found android.Manifest.permission.WRITE_SMS");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.MODIFY_SMS);
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.MODIFY_MMS);
                } else if (p.mPermissionName.equals(permission.READ_CONTACTS)) {
                    Log.d(TAG, "Found android.Manifest.permission.READ_CONTACTS");
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.QUERY_CONTACTS);
                    count++;
                } else if (p.mPermissionName.equals(permission.WRITE_CONTACTS)) {
                    Log.d(TAG, "Found android.Manifest.permission.WRITE_CONTACTS");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.MODIFY_CONTACTS);
                } else if (p.mPermissionName.equals(permission.READ_CALL_LOG)) {
                    Log.d(TAG, "Found android.Manifest.permission.READ_CALL_LOG");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.QUERY_CALL_LOG);
                } else if (p.mPermissionName.equals(permission.WRITE_CALL_LOG)) {
                    Log.d(TAG, "Found android.Manifest.permission.WRITE_CALL_LOG");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.MODIFY_CALL_LOG);
                } else if (p.mPermissionName.equals(permission.SEND_SMS)) {
                    Log.d(TAG, "Found android.Manifest.permission.SEND_SMS");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.SEND_SMS);
                } else if (p.mPermissionName.equals(permission.INTERNET)) {
                    Log.d(TAG, "Found android.Manifest.permission.INTERNET");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.SEND_MMS);
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.SEND_EMAIL);
                } else if (p.mPermissionName.equals(permission.ACCESS_FINE_LOCATION)) {
                    Log.d(TAG, "Found android.Manifest.permission.ACCESS_FINE_LOCATION");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.ACCESS_LOCATION);
                } else if (p.mPermissionName.equals(permission.RECORD_AUDIO)) {
                    Log.d(TAG, "Found android.Manifest.permission.RECORD_AUDIO");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.RECORD_MIC);
                } else if (p.mPermissionName.equals(permission.CAMERA)) {
                    Log.d(TAG, "Found android.Manifest.permission.CAMERA");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.OPEN_CAMERA);
                } else if (p.mPermissionName.equals(permission.CALL_PHONE)) {
                    Log.d(TAG, "Found android.Manifest.permission.CALL_PHONE");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.MAKE_CALL);
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.MAKE_CONFERENCE_CALL);
                } else if (p.mPermissionName.equals(permission.CHANGE_NETWORK_STATE)) {
                    Log.d(TAG, "Found android.Manifest.permission.CHANGE_NETWORK_STATE");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.CHANGE_NETWORK_STATE_ON);
                } else if (p.mPermissionName.equals(permission.CHANGE_WIFI_STATE)) {
                    Log.d(TAG, "Found android.Manifest.permission.CHANGE_WIFI_STATE");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.CHANGE_WIFI_STATE_ON);
                } else if (p.mPermissionName.equals(permission.BLUETOOTH_ADMIN)) {
                    Log.d(TAG, "Found android.Manifest.permission.BLUETOOTH_ADMIM");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.CHANGE_BT_STATE_ON);
                } else if (p.mPermissionName.equals("android.permission.HOTKNOT")) {
                    Log.d(TAG, "Found android.Manifest.permission.HOTKNOT");
                    count++;
                    enforceCheckSubPermission(p.mSubPermissions, SubPermissions.ACCESS_HOTKNOT);
                }
            }
        } else {
            Log.e(TAG, "verifySetPermissionRecordWait() null mPkgGrantedPermissions!");
        }
        //assertEquals("Miss some permission data for CTA apk!", NUM_SUBPERMISSION, count);
    }

    /**
     * Set status to QUERY_CONTACTS sub-permission for cta test apk
     */
    public void test03_SetPermissionRecord() throws Exception {
        Log.d(TAG, "test03_SetPermissionRecord()");
        boolean result = false;
        // Test set DENIED to QUERY_CONTACTS
        result = verifySetPermissionRecordWait(SubPermissions.QUERY_CONTACTS,
                PERMISSION_STATUS_DENIED);
        assertTrue("Set DENIED to Cta QUERY_CONTACTS failed!", result);

        // Test set CHECK to QUERY_CONTACTS
        result = verifySetPermissionRecordWait(SubPermissions.QUERY_CONTACTS,
                PERMISSION_STATUS_CHECK);
        assertTrue("Set CHECK to Cta QUERY_CONTACTS failed!", result);

        // Test set GRANTED to QUERY_CONTACTS
        result = verifySetPermissionRecordWait(SubPermissions.QUERY_CONTACTS,
                PERMISSION_STATUS_GRANTED);
        assertTrue("Set GRANTED to Cta QUERY_CONTACTS failed!", result);
    }

    /**
     * PERMISSION_GRANTED should be always returned when permission controller is disabled.
     */
    public void test04_DisablePermissionControllerAndCheck() throws Exception {
        Log.d(TAG, "test04_DisablePermissionControllerAndCheck()");
        String subPermission = SubPermissions.QUERY_CONTACTS;

        enablePermissionControllerWait(false);
        // Check DENIED status
        Log.d(TAG, "Check(DENIED)");
        enforceSetAndCheckPermission(subPermission, PERMISSION_STATUS_DENIED,
                PackageManager.PERMISSION_GRANTED);

        // Check GRANTED status
        Log.d(TAG, "Check(GRANTED)");
        enforceSetAndCheckPermission(subPermission, PERMISSION_STATUS_GRANTED,
                PackageManager.PERMISSION_GRANTED);

        // Check CHECK status
        Log.d(TAG, "Check(CHECK)");
        enforceSetAndCheckPermission(subPermission, PERMISSION_STATUS_GRANTED,
                PackageManager.PERMISSION_GRANTED);
    }

    /**
     * CheckPermission when enabled
     */
    public void test05_EnablePermissionControllerAndCheck() throws Exception {
        Log.d(TAG, "test05_EnablePermissionControllerAndCheck()");

        enablePermissionControllerWait();

        String subPermission = SubPermissions.QUERY_CONTACTS;
        // Check DENIED status
        Log.d(TAG, "Check(DENIED)");
        enforceSetAndCheckPermission(subPermission, PERMISSION_STATUS_DENIED,
                PackageManager.PERMISSION_DENIED);

        // Check GRANTED status
        Log.d(TAG, "Check(GRANTED)");
        enforceSetAndCheckPermission(subPermission, PERMISSION_STATUS_GRANTED,
                PackageManager.PERMISSION_GRANTED);
    }

    /**
     * PERMISSION_GRANTED should be returned when checking an unknown permission
     */
    public void test06_CheckUnknownPermission() throws Exception {
        Log.d(TAG, "test06_CheckUnknownPermission()");

        enablePermissionControllerWait();
        enforceSetAndCheckPermission("WRITE_FOO_BAR", PERMISSION_STATUS_DENIED,
                PackageManager.PERMISSION_GRANTED);
    }

    /**
     * PERMISSION_GRANTED should be returned
     */
    public void test07_CheckPermissionWithoutCallback() throws Exception {
        Log.d(TAG, "test07_CheckPermissionWithoutCallback()");

        enablePermissionControllerWait();
        // Register a null callback
        registerPermissionCallbackWait(CB_PARAM_NULL);
        String subPermission = SubPermissions.QUERY_CONTACTS;
        enforceSetAndCheckPermission(subPermission, PERMISSION_STATUS_CHECK,
                PackageManager.PERMISSION_GRANTED);
    }

    /**
     * CheckPermission with callback registered
     */
    public void test08_CheckPermissionWithCallback() throws Exception {
        Log.d(TAG, "test08_CheckPermissionWithCallback()");
        String subPermission = SubPermissions.QUERY_CONTACTS;

        // Callback return GRANTED
        enablePermissionControllerWait();
        enforceSetAndCheckPermission(subPermission, PERMISSION_STATUS_CHECK,
                PackageManager.PERMISSION_GRANTED);
        // Callback return DENIED
        registerPermissionCallbackWait(CB_PARAM_DENIED);
        enforceSetAndCheckPermission(subPermission, PERMISSION_STATUS_CHECK,
                PackageManager.PERMISSION_DENIED);
    }


    public void test09_CheckPermissionAsync() throws Exception {
        Log.d(TAG, "test09_CheckPermissionAsync()");

        enablePermissionControllerWait();

        String subPermission = SubPermissions.QUERY_CONTACTS;
        // Check GRANTED & DENIED status, Async call back should return directly
        registerPermissionCallbackWait(CB_PARAM_NULL);
        enforceSetAndCheckPermissionAsync(subPermission, PERMISSION_STATUS_GRANTED,
                PackageManager.PERMISSION_GRANTED);
        enforceSetAndCheckPermissionAsync(subPermission, PERMISSION_STATUS_DENIED,
                PackageManager.PERMISSION_GRANTED); //!
        // Callback return GRANTED
        registerPermissionCallbackWait(CB_PARAM_GRANTED);
        enforceSetAndCheckPermissionAsync(subPermission, PERMISSION_STATUS_CHECK,
                PackageManager.PERMISSION_GRANTED);
        // Callback return DENIED
        registerPermissionCallbackWait(CB_PARAM_DENIED);
        enforceSetAndCheckPermissionAsync(subPermission, PERMISSION_STATUS_CHECK,
                PackageManager.PERMISSION_DENIED);
    }


    /**
     * Attach operation should fail with null callback.
     *
     * Input:
     *     EXTRA_PARAM false => null callback
     * Output:
     *     EXTRA_RESULT 0 => attach failed (Expected)
     *     EXTRA_RESULT 1 => attach successfully
     */

    /**
     * Full test of attachment mechanism
     */
    public void test10_AttachAndPriority() throws Exception {
        Log.d(TAG, "test10_AttachAndPriority()");

        int attachResult = -1;

        // 1. ManagerEmulator attachs at first time
        Log.d(TAG, "1. Attach at first time");
        attachResult = attachEmulatorWait(true);
        assertEquals("Should attach successfully", 1, attachResult);

        // 2. Install Tencent apk (Higher priority) and ManagerEmulator should receive onConnectionEnded callback
        Log.d(TAG, "2. Attach with higher priority");

        synchronized (ATTACH_LOCK) {
            mConnIntentReceived = false;
            mExpectUid = Process.SYSTEM_UID;
            mExpectConnStatus = IMobileManager.MGR_DETACHED;
            EmulatorManagerOps.installApkWait(mContext, EmulatorManagerOps.TENCENT_APK);
            if (mConnIntentReceived == false) {
                EmulatorManagerOps.setTimeout(mContext, mReceiver);
                Log.d(TAG, "Wait until connection detach intent...");
                ATTACH_LOCK.wait();
                EmulatorManagerOps.stopTimeout();
            }
        }
        assertEquals("Manager with lower priority should receive MGR_DETACHED intent", true, mConnIntentReceived);

        // Take a break for MoMS
        Thread.sleep(TIME_BREAK);

        // 3. ManagerEmulator try to attach again but should fail with lower priority
        Log.d(TAG, "3. Attach with lower priority");
        attachResult = attachEmulatorWait(true);
        assertEquals("Should attach failed with lower identity!", 0, attachResult);

        // 4. Delete Tencent apk and ManagerEmulator should receive onConnectionResume callback
        Log.d(TAG, "4. Detach");
        synchronized (ATTACH_LOCK) {
            mConnIntentReceived = false;
            mExpectUid = mPackageManager.getApplicationInfo(EmulatorManagerOps.TENCENT_PACKAGE,
                    PackageManager.GET_META_DATA).uid;
            mExpectConnStatus = IMobileManager.MGR_DETACHED;
            EmulatorManagerOps.uninstallApkWait(mContext, EmulatorManagerOps.TENCENT_PACKAGE);
            if (mConnIntentReceived == false) {
                Log.d(TAG, "Wait until connection attach intent...");
                EmulatorManagerOps.setTimeout(mContext, mReceiver);
                ATTACH_LOCK.wait();
                EmulatorManagerOps.stopTimeout();
            }
        }
        assertEquals("Manager candidates should receive MGR_ATTACHED intent", true, mConnIntentReceived);
    }

    /**
     * Internal Functions
     */
    private void detachEmulatorWait() throws Exception {
        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_PMC_DETACH);
        startServiceWait(intent);
    }

    private int attachEmulatorWait(boolean withCallBack) throws Exception {
        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_PMC_ATTACH);
        intent.putExtra(EXTRA_PARAM_1, withCallBack);
        startServiceWait(intent);
        Log.d(TAG, "attachEmulatorWait() return: " + mResult);
        return mResult;
    }

    private void enablePermissionControllerWait() throws Exception {
        // Register callback and enable controller
        registerPermissionCallbackWait(CB_PARAM_GRANTED);
        enablePermissionControllerWait(true);
    }

    private void enablePermissionControllerWait(boolean enabled) throws Exception {
        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_PMC_ENABLE_CONTROLLER);
        int data = (enabled) ? 1 : 0;
        intent.putExtra(EXTRA_ENABLE, data);
        startServiceWait(intent);
    }

    private void registerPermissionCallbackWait(int callbackParam) throws Exception {
        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_PMC_REGISTER_CB);
        intent.putExtra(EXTRA_PARAM_1, callbackParam);
        startServiceWait(intent);
    }

    private void enforceCheckSubPermission(List<Permission> sub_list, String subPermission) {
        if (getCtaSubPermission(sub_list, subPermission) == null) {
            fail("Can't find " + subPermission + " for CTA apk");
        }
    }

    private void enforceSetAndCheckPermission(String subPermission, int requestStatus, int expectResult) throws Exception {
        int finalStatus = -1;
        setCtaSubPermissionWait(requestStatus, subPermission);

        finalStatus = checkPermissionWait(EmulatorManagerOps.CTA_PACKAGE, subPermission, false);
        assertEquals("enforceSetAndCheckPermission() failed!", expectResult, finalStatus);
    }

    private void enforceSetAndCheckPermissionAsync(String subPermission, int requestStatus, int expectResult) throws Exception {
        int finalStatus = -1;
        setCtaSubPermissionWait(requestStatus, subPermission);
        // Wait until callback
        finalStatus = checkPermissionWait(EmulatorManagerOps.CTA_PACKAGE, subPermission, true);
        assertEquals("enforceSetAndCheckPermissionAsync() failed!", expectResult, finalStatus);
    }

    private int checkPermissionWait(String packageName, String subPermission, boolean isAsync) throws Exception {
        PackageInfo pkgInfo = mPackageManager.getPackageInfo(packageName, 0);
        int uid = pkgInfo.applicationInfo.uid;

        Intent intent = prepareIntent();
        if (isAsync) {
            intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_PMC_CHECK_PERMISSION_ASYNC);
        } else {
            intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_PMC_CHECK_PERMISSION);
        }
        intent.putExtra(EXTRA_UID, uid);
        intent.putExtra(EXTRA_SUBPERMISSION, subPermission);

        startServiceWait(intent);

        return mResult;
    }


    private boolean verifySetPermissionRecordWait(String subPermission, int requestStatus) throws Exception {
        int finalStatus = -1;
        // Set CTA test permission status
        setCtaSubPermissionWait(requestStatus, subPermission);

        // Check permission status is correctly set
        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_PMC_GET_RECORD);
        intent.putExtra(EXTRA_PACKAGE, EmulatorManagerOps.CTA_PACKAGE);
        startServiceWait(intent);

        if (mPkgGrantedPermissions != null) {
            Permission p = getCtaSubPermissionFromParent(mPkgGrantedPermissions, subPermission);
            if (p != null) {
                finalStatus = p.getStatus();
                Log.d(TAG, "verifySetPermissionRecordWait() Found " + subPermission + " with status: " + finalStatus);
                if (finalStatus == requestStatus) {
                    return true;
                } else {
                    Log.e(TAG, "verifySetPermissionRecordWait() Verify failed! requestStatus: " +
                            requestStatus + " finalStatus: " + finalStatus);
                }
            } else {
                Log.e(TAG, "verifySetPermissionRecordWait() Can't find " + subPermission);
            }
        } else {
            Log.e(TAG, "verifySetPermissionRecordWait() null mPkgGrantedPermissions!");
        }
        return false;
    }

    private Permission getCtaSubPermissionFromParent(List<Permission> list, String subPermission) {
        Permission result = null;
        for (int i = 0; i < mPkgGrantedPermissions.size(); i++) {
            Permission p = mPkgGrantedPermissions.get(i);
            List<Permission> sps = p.mSubPermissions;
            if (sps != null) {
                for (int j = 0; j < sps.size(); j++) {
                    Permission sp = sps.get(j);
                    if (sp.mPermissionName.equals(subPermission)) {
                        return sp;
                    }
                }
            }
        }
        return null;
    }

    private Permission getCtaSubPermission(List<Permission> sub_list, String subPermission) {
        Permission result = null;
        for (int j = 0; j < sub_list.size(); j++) {
            Permission sp = sub_list.get(j);
            if (sp.mPermissionName.equals(subPermission)) {
                return sp;
            }
        }
        return null;
    }

    private void setCtaSubPermissionWait(int status, String subPermission) throws Exception {
        // Set subPermission status
        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_PMC_SET_RECORD);
        intent.putExtra(EXTRA_PACKAGE, EmulatorManagerOps.CTA_PACKAGE);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_SUBPERMISSION, subPermission);
        startServiceWait(intent);
    }

    private void startServiceWait(Intent intent) throws Exception {
        synchronized (mContext) {
            EmulatorManagerOps.setTimeout(mContext, mReceiver);
            mContext.startService(intent);
            mContext.wait();
            EmulatorManagerOps.stopTimeout();
        }
    }
}
