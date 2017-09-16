package com.mediatek.bluetooth.sanitytest.function;

import java.io.File;

import com.mediatek.bluetooth.sanitytest.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.widget.LockPatternUtils;

public class SanityConfigure {
    private static final String TAG = "SanityConfigure";
    private static final int ACTION_WAIT_DURNATION = 5000;

    // /M: Please do not modify it.
    private static final String TARGET_NAME = "SanityPairingTarget";
    private static long record = 1;

    public static void configureTestEnvironment(Activity activity) {
        // /M: Set Orientation.
        int orientation = activity.getResources().getConfiguration().orientation;
        int winOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            winOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }
        activity.setRequestedOrientation(winOrientation);

        // /M : Remove the lock screen pattern.
        LockPatternUtils utils = new LockPatternUtils(activity);
        try {
            utils.clearLock(false);
            utils.setLockScreenDisabled(true);
        } catch (Exception e) {
            Toast.makeText(activity, R.string.lock_screen_off_failed_msg,
                    Toast.LENGTH_SHORT).show();
        }

        // /M : Keep the screen always on once the activity launched.
        activity.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static void configureBluetooth(final TextView configureInfo) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        // M : Do the test environment configuration
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // M : If bluetooth is disabled, enable it first
                do {
                    if (!adapter.isEnabled()) {
                        adapter.enable();
                        try {
                            Thread.sleep(ACTION_WAIT_DURNATION);
                        } catch (InterruptedException e) {
                        }
                    }
                } while (!adapter.isEnabled());
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // /M : Keep the bluetooth device discoverable
                setDeviceDiscoverable(adapter);
                // /M : Rename the device
                adapter.setName(TARGET_NAME);
                configureInfo.setText(R.string.bt_configure_done);
            }
        } .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static boolean setDeviceDiscoverable(BluetoothAdapter adapter) {
        if (adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            boolean result = adapter
                    .setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
            Log.d(TAG, "setScanMode success : " + result);
            try {
                Thread.sleep(ACTION_WAIT_DURNATION);
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    public static void takeScreenShot(String timeStame) {
        String path = Environment.getExternalStorageDirectory()
                + File.separator + "SanityRecord" + File.separator + timeStame
                + File.separator;
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
            try {
                Thread.sleep(ACTION_WAIT_DURNATION);
            } catch (InterruptedException e) {
            }
        }
        String fileName = String.valueOf(++record) + ".png";
        path += fileName;
        Log.d(TAG, "folder : " + path + " " + dir.exists());
        try {
            Runtime.getRuntime().exec("screencap -p " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
