
package com.mediatek.mom.test.func;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;

public class EmulatorManagerOps {
    public static final String TAG = "EmulatorManagerOps";

    // Testcase Timout
    public static final int TIMEOUT_TIME = 60 * 1000;
    public static final String ACTION_TIMEOUT = "com.mediatek.mom.test.TIMEOUT";
    private static Thread sTimeoutThread = null;

    // Basic Information
    public static final String EMUMGR_CLASS = "com.mediatek.mom.test.app.mgremu.ManagerEmulator";
    public static final String EMUMGR_PACKAGE = "com.mediatek.mom.test.app.mgremu";
    public static final String APK_LOCATION = "/sdcard/MoMS";
    public static final String CTA_APK = "MTK_CtaTestAPK_KK.apk";
    public static final String TENCENT_APK = "Tencent_Permission_Manager_CTA.apk";
    public static final String TENCENT_PACKAGE = "com.tencent.tcuser";
    public static final String CTA_PACKAGE = "com.mediatek.cta";
    public static final String CTA_CLASS = "com.mediatek.cta.CtaActivity";
    public static final String RESULT_INTENT = "com.mediatek.mom.test.app.mgremu.operation.done";

    // Result code
    public static final int RESULT_NONE = -1;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAILED = 0;
    public static final int RESULT_WAIT = -999;
    public static final int RESULT_TIMEOUT = -998;

    // Emualted manager operations
    public static final int MGR_OP_NONE = 0;

    // Permission Controller (PMC)
    public static final int MGR_OP_PMC_ATTACH = 1;
    public static final int MGR_OP_PMC_ATTACH_NULL_CB = 2;
    public static final int MGR_OP_PMC_DETACH = 3;
    public static final int MGR_OP_PMC_REGISTER_CB = 4;
    public static final int MGR_OP_PMC_ENABLE_CONTROLLER = 5;
    public static final int MGR_OP_PMC_SET_RECORD = 6;
    public static final int MGR_OP_PMC_GET_RECORD = 7;
    public static final int MGR_OP_PMC_GET_INSTALLED_PACKAGES = 8;
    public static final int MGR_OP_PMC_CHECK_PERMISSION = 9;
    public static final int MGR_OP_PMC_CHECK_PERMISSION_ASYNC = 10;
    public static final int MGR_OP_PMC_CB_CONNECTION_ENDED = 11;
    public static final int MGR_OP_PMC_CB_CONNECTION_RESUME = 12;
    public static final int MGR_OP_PMC_CB_PERMISSION_CHECK = 13;
    // Permission Controller (REC)
    public static final int MGR_OP_REC_OFFSET = 100;
    public static final int MGR_OP_REC_GET_RECEIVER_LIST = MGR_OP_REC_OFFSET;
    public static final int MGR_OP_REC_SET_RECORD = MGR_OP_REC_OFFSET + 1;
    public static final int MGR_OP_REC_GET_RECORD = MGR_OP_REC_OFFSET + 2;
    public static final int MGR_OP_REC_FILTER_RECEIVER = MGR_OP_REC_OFFSET + 3;
    public static final int MGR_OP_REC_START_MONITOR = MGR_OP_REC_OFFSET + 4;
    public static final int MGR_OP_REC_STOP_MONITOR = MGR_OP_REC_OFFSET + 5;

    public static boolean isEngBuild() {
        String type = SystemProperties.get("ro.build.type");
        return ("eng".equals(type)) ? true : false;
    }

    public static Intent prepareIntent() {
        Intent intent = new Intent();
        intent.setClassName(EMUMGR_PACKAGE, EMUMGR_CLASS);
        return intent;
    }

    public static void setTimeout(final Context ctx, final BroadcastReceiver receiver) {
        stopTimeout();
        sTimeoutThread = new Thread(new Runnable() {
            public void run() {
                try {
                    sTimeoutThread.sleep(TIMEOUT_TIME);
                    Intent timeoutIntent = new Intent(ACTION_TIMEOUT);
                    receiver.onReceive(ctx, timeoutIntent);
                } catch (Exception e) { }
            }
        });
        sTimeoutThread.start();
    }

    public static void stopTimeout() {
        if (sTimeoutThread != null) {
            sTimeoutThread.interrupt();
            sTimeoutThread = null;
        }
    }

    static class InstallObserver extends IPackageInstallObserver.Stub {
        Context mContext = null;

        public InstallObserver(Context context) {
            mContext = context;
        }
        @Override
        public void packageInstalled(String packageName, int returnCode) {
            synchronized (mContext) {
                mContext.notify();
                Log.d(TAG, "Installed " + packageName + " with returnCode: " + returnCode);
            }
        }
    }

    static class DeleteObserver extends IPackageDeleteObserver.Stub {
        Context mContext = null;

        public DeleteObserver(Context context) {
            mContext = context;
        }
        @Override
        public void packageDeleted(String packageName, int returnCode) {
            synchronized (mContext) {
                mContext.notify();
                Log.d(TAG, "Uninstalled " + packageName + " with returnCode: " + returnCode);
            }
        }
    }

    public static void installApkWait(Context ctx, String apkFile) {
        try {
            synchronized (ctx) {
                File file = new File(EmulatorManagerOps.APK_LOCATION, apkFile);
                Uri packageUri = Uri.fromFile(file);
                ctx.getPackageManager().installPackage(packageUri, new InstallObserver(ctx),
                        PackageManager.INSTALL_ALL_USERS, ctx.getPackageName());
                ctx.wait();
            }
        } catch (InterruptedException e) { }
    }

    public static void uninstallApkWait(Context ctx, String packageName) {
        try {
            synchronized (ctx) {
                ctx.getPackageManager().deletePackage(packageName, new DeleteObserver(ctx),
                        PackageManager.DELETE_ALL_USERS);
                ctx.wait();
            }
        } catch (InterruptedException e) { }
    }
}
