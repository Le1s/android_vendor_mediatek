package com.mediatek.bluetooth.sanitytest.function;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mediatek.bluetooth.sanitytest.BluetoothTestTool;

import java.util.Set;

public class DaemonReceiver extends BroadcastReceiver {
    private static final String TAG = "DaemonReceiver";
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static int mPairingKey;
    private static int mType;
    private static BluetoothDevice mDevice;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent : " + intent.getAction());
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
            ///M: Remove previous bonded device to avoid reach the max bond limitation.
            removeAllBondedDevices();

            mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT,
                    BluetoothDevice.ERROR);
            if (mType == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION
                    || mType == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY
                    || mType == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PIN) {
                mPairingKey = intent.getIntExtra(
                        BluetoothDevice.EXTRA_PAIRING_KEY,
                        BluetoothDevice.ERROR);
            }
            Log.d(TAG, "mDevice : " + mDevice + " mType : " + mType
                    + " mPairingKey : " + mPairingKey);
            if (mPairingKey == BluetoothDevice.ERROR) {
                Log.e(TAG,
                        "Invalid Confirmation Passkey received, not showing any dialog");
                return;
            }
            mDevice.setPairingConfirmation(true);
            Log.d(TAG, "setPairingConfirmation()");

        } else if (ACTION_BOOT_COMPLETED.equals(action)) {
            Intent bootTarget = new Intent(context, BluetoothTestTool.class);
            bootTarget.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(bootTarget);
        }
    }

    private void removeAllBondedDevices() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (!bondedDevices.isEmpty()) {
           for (BluetoothDevice bondedDevice : bondedDevices) {
               bondedDevice.removeBond();
               Log.d(TAG, "Remove bonded device : " + bondedDevice.getName());
           }
        }
    }
}
