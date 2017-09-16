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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.test.AndroidTestCase;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;

import com.android.internal.util.XmlUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import static com.mediatek.mom.test.func.EmulatorManagerOps.prepareIntent;
import com.mediatek.common.mom.ReceiverRecord;

public class ReceiverControlTestCase extends AndroidTestCase {
    private static final String TAG = "ReceiverControlTestCase";

    private PackageManager mPackageManager = null;
    private ArrayList<ReceiverRecord> mBootReceiverList = null;
    private ArrayList<ResolveInfo> mResolveInfoList = null;

    // The test reuslt passed from Target Emulator
    private int mResult = EmulatorManagerOps.RESULT_FAILED;

    // Install flag for CTA test apk
    private static boolean mCtaTestApkInstalled = false;

    // Time break between each testcases
    private static final int TIME_BREAK = 2000;

    // Intent extra keys
    public static final String EXTRA_MGR_OP = "extra_operation";
    public static final String EXTRA_PACKAGE = "extra_package";
    public static final String EXTRA_UID = "extra_uid";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_SUBPERMISSION = "extra_sub_permission";
    public static final String EXTRA_RESULT = "extra_result";
    public static final String EXTRA_ENABLE = "extra_enable";
    public static final String EXTRA_PARAM_1 = "extra_param_1";
    public static final String EXTRA_INTENT = "extra_intent";
    public static final String EXTRA_USERID = "extra_userid";

    private MyBroadcastReceiver mReceiver = new MyBroadcastReceiver();

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean received = false;

            String action = intent.getAction();

            if (EmulatorManagerOps.RESULT_INTENT.equals(action)) {
                int operation = intent.getIntExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_NONE);
                    Log.v(TAG, "onReceive() operation: " + operation);
                    switch (operation) {
                    case EmulatorManagerOps.MGR_OP_REC_GET_RECEIVER_LIST:
                        mBootReceiverList = intent.getParcelableArrayListExtra(EXTRA_RESULT);
                        Log.d(TAG, "mBootReceiverList: " + mBootReceiverList);
                        received = true;
                        break;
                    case EmulatorManagerOps.MGR_OP_REC_GET_RECORD:
                        mResult = intent.getIntExtra(EXTRA_RESULT, -1);
                        Log.d(TAG, "mResult: " + mResult);
                        received = true;
                        break;
                    case EmulatorManagerOps.MGR_OP_REC_SET_RECORD:
                        // Do nothing
                        received = true;
                        break;
                    case EmulatorManagerOps.MGR_OP_REC_FILTER_RECEIVER:
                        mResolveInfoList = intent.getParcelableArrayListExtra(EXTRA_RESULT);
                        Log.d(TAG, "mResolveInfoList: " + mResolveInfoList);
                        received = true;
                        break;
                    case EmulatorManagerOps.MGR_OP_REC_START_MONITOR:
                        // Do nothing
                        received = true;
                        break;
                    case EmulatorManagerOps.MGR_OP_REC_STOP_MONITOR:
                        // Do nothing
                        received = true;
                        break;
                    default:
                    }
                synchronized (mContext) {
                    // Notify the testcase
                    if (received) {
                        mContext.notify();
                    }
                }
            } else if (EmulatorManagerOps.ACTION_TIMEOUT.equals(action)) {
                Log.e(TAG, "onReceive() Operation Timeout!");
                synchronized (mContext) {
                    mContext.notify();
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
        mContext.registerReceiver(mReceiver, intentFilter);
        mPackageManager = mContext.getPackageManager();
        if (!mCtaTestApkInstalled) {
            EmulatorManagerOps.installApkWait(mContext, EmulatorManagerOps.CTA_APK);
            mCtaTestApkInstalled = true;
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mContext.unregisterReceiver(mReceiver);
        mPackageManager = null;
    }

    /**
     * Install CTA test apk and check it from MoMS.
     *
     * Output:
     *     mBootReceiverList: Should found CTA test apk.
     */
    public void test01_GetBootReceiverList() throws Exception {
        Log.d(TAG, "test01_GetBootReceiverList()");

        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_REC_GET_RECEIVER_LIST);

        startServiceWait(intent);

        if (mBootReceiverList != null) {
            for (int i = 0; i < mBootReceiverList.size(); i++) {
                ReceiverRecord record = mBootReceiverList.get(i);
                if (EmulatorManagerOps.CTA_PACKAGE.equals(record.packageName)) {
                    Log.v(TAG, "Found package: " + record.packageName);
                    mResult = EmulatorManagerOps.RESULT_SUCCESS;
                    break;
                }
            }
        } else {
            Log.e(TAG, "Null mBootReceiverList");
        }

        assertEquals("CTA_PACKAGE should be found", EmulatorManagerOps.RESULT_SUCCESS, mResult);
    }

    /**
     * Test set receiver record to CTA package.
     * Database cases are skipped in user build
     */
    public void test02_SetBootReceiverEnabledSetting() throws Exception {
        boolean getEnabled = false;
        boolean requestEnabled = false;
        boolean found = false;
        String testPackage = EmulatorManagerOps.CTA_PACKAGE;
        boolean isEngBuild = EmulatorManagerOps.isEngBuild();

        Log.d(TAG, "test02_SetBootReceiverEnabledSetting()");

        // Set to disabled
        requestEnabled = false;
        setReceiverRecordWait(testPackage, requestEnabled);
        System.out.println("getReceiverRecordWait start");
        getEnabled = getReceiverRecordWait(testPackage);
        assertEquals("Set record failed!", requestEnabled, getEnabled);

        System.out.println("getReceiverRecordWait end");
        // Find the record in database
        /*
        if (isEngBuild) {
            found = findRecordInDatabase(testPackage);
            assertEquals("Can't find CTA package in database!", true, found);
        }
        */

        // Set back to enabled
        requestEnabled = true;
        setReceiverRecordWait(testPackage, requestEnabled);
        getEnabled = getReceiverRecordWait(testPackage);
        assertEquals("Set record failed!", requestEnabled, getEnabled);

        // The record should be removed from database
        /*
        if (isEngBuild) {
            found = findRecordInDatabase(testPackage);
            assertEquals("CTA package should be removed from database!", false, found);
        }
        */
    }

    /**
     * Test set receiver record to an unknown CTA package. (Should return true)
     */
    public void test03_SetUnknownBootReceiverEnabledSetting() throws Exception {
        Log.d(TAG, "test03_SetUnknownBootReceiverEnabledSetting()");
        boolean getEnabled = false;

        // Set back to enabled
        getEnabled = getReceiverRecordWait("com.mediatek.foo.bar");
        assertEquals("Default value should be true", true, getEnabled);
    }

    /**
     * Remove CTA package and check the cache & database.
     */
    public void test04_FilterReceiver() throws Exception {
        Log.d(TAG, "test04_FilterReceiver()");

        int userId = ActivityManager.getCurrentUser();
        boolean found = false;
        String testPackage = EmulatorManagerOps.CTA_PACKAGE;
        Intent action = new Intent();
        action.setAction(Intent.ACTION_BOOT_COMPLETED);

        // 1. Set to disable
        setReceiverRecordWait(testPackage, false);

        // 2. Make sure CTA package should recevie BOOT_COMPLETED
        ArrayList<ResolveInfo> receivers = (ArrayList<ResolveInfo>)
                mPackageManager.queryBroadcastReceivers(action, PackageManager.GET_META_DATA, userId);
        if (receivers != null) {
            for (int j = 0; j < receivers.size(); j++) {
                ResolveInfo info = receivers.get(j);
                String packageName = (info.activityInfo != null) ? info.activityInfo.packageName : null;
                if (packageName != null && testPackage.equals(packageName)) {
                    found = true;
                    break;
                }
            }
        }
        assertEquals("CTA apk should receive BOOT_COMPLETED", true, found);

        // 3. Filter should not work before start monitoring
        monitorReceiverWait(false);
        filterReceiverWait(action, userId, receivers);
        found = findResolveList(EmulatorManagerOps.CTA_PACKAGE, mResolveInfoList);
        mResolveInfoList = null;
        assertEquals("CTA apk should be still found", true, found);

        // 4. Start monitor and filter again
        monitorReceiverWait(true);
        filterReceiverWait(action, userId, receivers);
        found = findResolveList(EmulatorManagerOps.CTA_PACKAGE, mResolveInfoList);
        assertEquals("CTA apk should be filtered out", false, found);

        // Stop
        monitorReceiverWait(false);
    }

    /**
     * Remove CTA package and check the cache & database.
     */
    public void test05_Remove_CTA_Package() throws Exception {
        Log.d(TAG, "test05_Remove_CTA_Package()");

        String testPackage = EmulatorManagerOps.CTA_PACKAGE;
        boolean found = false;
        boolean getEnabled = false;
        EmulatorManagerOps.uninstallApkWait(mContext, testPackage);

        // Need to wait until MoMs receives package removed intent.
        Thread.sleep(TIME_BREAK);

        getEnabled = getReceiverRecordWait(testPackage);
        assertEquals("The default value should be TRUE!", true, getEnabled);

        // The record should be removed from database
        found = findRecordInDatabase(testPackage);
        assertEquals("CTA package should be removed from database!", false, found);
    }

    /**
     * Internal Functions
     */
    private void monitorReceiverWait(boolean start) throws Exception {
        Intent intent = prepareIntent();
        if (start) {
            intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_REC_START_MONITOR);
        } else {
            intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_REC_STOP_MONITOR);
        }
        startServiceWait(intent);
    }

    private boolean findResolveList(String foundPkg, ArrayList<ResolveInfo> receivers) {
        boolean found = false;
        for (int j = 0; j < mResolveInfoList.size(); j++) {
            ResolveInfo info = mResolveInfoList.get(j);
            String packageName = (info.activityInfo != null) ? info.activityInfo.packageName : null;
            if (packageName != null && packageName.equals(foundPkg)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private void filterReceiverWait(Intent action, int userId, ArrayList<ResolveInfo> receivers) throws Exception {
        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_REC_FILTER_RECEIVER);
        intent.putExtra(EXTRA_INTENT, action);
        intent.putExtra(EXTRA_USERID, userId);
        intent.putParcelableArrayListExtra(EXTRA_PARAM_1, receivers);
        startServiceWait(intent);
    }

    private boolean findRecordInDatabase(String testPackage) throws Exception {
        // Wait a moment until the data is ready
        System.out.println("findRecordInDatabase 1" + testPackage);
        Thread.sleep(TIME_BREAK);
        System.out.println("findRecordInDatabase 2" + testPackage);
        List<ReceiverRecord> records = getReceiverRecordFromDatabase();

        boolean found = false;
        if (records != null) {
            System.out.println("findRecordInDatabase 3" + testPackage);
            for (int i = 0; i < records.size(); i++) {
                ReceiverRecord r = records.get(i);
               System.out.println("findRecordInDatabase 4" + r.packageName + r.enabled);
                if (testPackage.equals(r.packageName) && r.enabled == false) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    private boolean getReceiverRecordWait(String packageName) throws Exception {
        boolean enabled = false;
        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_REC_GET_RECORD);
        intent.putExtra(EXTRA_PACKAGE, packageName);

        startServiceWait(intent);
        enabled = (mResult > 0) ? true : false;
        return enabled;
    }

    private void setReceiverRecordWait(String packageName, boolean enabled) throws Exception {
        int data = (enabled) ? 1 : 0;
        Intent intent = prepareIntent();
        intent.putExtra(EXTRA_MGR_OP, EmulatorManagerOps.MGR_OP_REC_SET_RECORD);
        intent.putExtra(EXTRA_PACKAGE, packageName);
        intent.putExtra(EXTRA_ENABLE, data);

        startServiceWait(intent);
    }

    private List<ReceiverRecord> getReceiverRecordFromDatabase() {
        List<ReceiverRecord> recordList = new ArrayList<ReceiverRecord>();
        File dataDir = Environment.getDataDirectory();
        File systemDir = new File(dataDir, "system");
        File storeDir = new File(systemDir, "bootreceiver_1000.xml");
        AtomicFile mFile = new AtomicFile(storeDir);
        FileInputStream stream = null;
        try {
            stream = mFile.openRead();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No existing " + mFile.getBaseFile() + "; starting empty");
            return null;
        }
        boolean success = false;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
                ;
            }

            if (type != XmlPullParser.START_TAG) {
                throw new IllegalStateException("no start tag found");
            }

            int outerDepth = parser.getDepth();
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("pkg")) {
                    String pkgName = parser.getAttributeValue(null, "n");
                    int userId = Integer.parseInt(parser.getAttributeValue(null, "u"));
                    boolean enabled = Boolean.parseBoolean(parser.getAttributeValue(null, "e"));
                    Log.d(TAG, "Read package name: " + pkgName + " enabled: " + enabled + " at User(" + userId + ")");
                    ReceiverRecord record = new ReceiverRecord(pkgName, enabled);
                    recordList.add(record);
                } else {
                    Log.w(TAG, "Unknown element under <app-ops>: "
                            + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
            success = true;
        } catch (IllegalStateException e) {
            Log.w(TAG, "Failed parsing " + e);
        } catch (NullPointerException e) {
            Log.w(TAG, "Failed parsing " + e);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed parsing " + e);
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Failed parsing " + e);
        } catch (IOException e) {
            Log.w(TAG, "Failed parsing " + e);
        } catch (IndexOutOfBoundsException e) {
            Log.w(TAG, "Failed parsing " + e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }

        return recordList;
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
