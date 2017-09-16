package com.mediatek.bluetooth.sanitytest;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mediatek.bluetooth.sanitytest.function.SanityConfigure;
import com.mediatek.bluetooth.sanitytest.function.ScreenShot;

public class BluetoothTestTool extends Activity {
    private static final String TAG = "BluetoothTestTool";
    private TextView mConfigureInfo;
    private TextView mLatestRecordInfo;
    private TextView mStatusInfo;
    private SimpleDateFormat mFormat;
    private Resources mRes;
    private static String mTimeStamp = null;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mConfigureInfo = (TextView) findViewById(R.id.txtStatus);
        mLatestRecordInfo = (TextView) findViewById(R.id.txtLatestHistory);
        mStatusInfo = (TextView) findViewById(R.id.txtResult);
        mFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        mRes = getResources();
        mActivity = this;

        IntentFilter filter = new IntentFilter(
                BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        getApplicationContext().registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SanityConfigure.configureTestEnvironment(this);

        SanityConfigure.configureBluetooth(mConfigureInfo);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        getApplicationContext().unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, 0, R.string.pair_history)
                .setEnabled(true)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case Menu.FIRST:
            Intent intent = new Intent(
                    "com.mediatek.bluetooth.sanitytest.history.RecordGallery");
            this.startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String timeRecord = mFormat.format(new Date());
            if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String record = mRes.getString(R.string.latest_record);
                record = String.format(record, timeRecord, device.getAddress());
                mLatestRecordInfo.setText(record);
                mTimeStamp = String.valueOf(System.currentTimeMillis());
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                int bondState = intent
                        .getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                                BluetoothDevice.ERROR);
                String result = mRes.getString(R.string.result);
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    result = String.format(result,
                            mRes.getString(R.string.result_success));
                } else if (bondState == BluetoothDevice.BOND_BONDING) {
                    result = String.format(result,
                            mRes.getString(R.string.result_pairing));
                } else {
                    result = String.format(result,
                            mRes.getString(R.string.result_fail));
                }
                mStatusInfo.setText(result);
            }
            Log.d(TAG, "onReceive : " + action);
            if (mTimeStamp != null) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        ScreenShot.shoot(mActivity, mTimeStamp);
                        Log.d(TAG, "shoot " + mTimeStamp);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                    }
                } .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

    };
}
