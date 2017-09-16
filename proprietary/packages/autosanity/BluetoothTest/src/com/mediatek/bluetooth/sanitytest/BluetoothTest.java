package com.mediatek.bluetooth.sanitytest;

import android.app.Activity;
import android.app.Instrumentation;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;

import com.android.settings.Settings;
import com.jayway.android.robotium.solo.Solo;

import java.util.Locale;
import java.util.Set;

public class BluetoothTest extends
        ActivityInstrumentationTestCase2<Settings.BluetoothSettingsActivity> {

    private static final String TAG = "BluetoothTest";
    private static final int MAX_TRY_COUNT = 8;
    private static final int TIME_OUT = 1000;

    private static final int BT_STATUS_ON = 1;
    private static final int BT_STATUS_OFF = 2;

    private static final int MAX_SEARCH_COUNT_DURING_SCAN = 5;
    private static final int MAX_SCAN_COUNT_DURING_TEST = 3;

    private static final int MAX_WAIT_COUNT_DURING_RESPONSE = 5;
    private static final int MAX_REQUEST_COUNT_DURING_TEST = 3;

    private static final int MAX_COMMON_WAIT_COUNT = 5;

    private static final String REMOTE_DEVICE = "SanityPairingTarget";
    private static final String LOCAL_DEVICE = "SanityTestDevice";


    private Activity mActivity;
    private Context mContext;
    private Instrumentation mInstrumentation;
    private Solo mSolo;
    private Switch mBtEnabler;

    private BluetoothAdapter mAdapter;
    private int mBTState = BluetoothAdapter.ERROR;
    private boolean mIsSuccess = false;
    private boolean mIsScanFinished = true;
    private String mPairingKey = "TestTestTest";
    private boolean mIsReceivedResponse = false;
    private boolean mIsBonded = false;
    private int mResponseId = Integer.MAX_VALUE;
    private int mBondState = BluetoothDevice.BOND_NONE;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            debugLog("Received broadcast " + action);
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                mBTState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                debugLog("BluetoothAdapter state changed to " + mBTState);
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = btDevice.getName();
                mIsReceivedResponse = REMOTE_DEVICE.equals(deviceName);
                int passkey = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, BluetoothDevice.ERROR);
                mPairingKey = String.format(Locale.US, "%06d", passkey);
                debugLog("deviceName : " + deviceName + " mPairingKey : " + mPairingKey);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                mBondState = intent.getIntExtra(
                        BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                mIsBonded = (BluetoothDevice.BOND_BONDED == mBondState);
                mResponseId = intent.getIntExtra(BluetoothDevice.EXTRA_REASON, BluetoothDevice.ERROR);
                debugLog("mBondState : " + mBondState + " mResponseId : " + mResponseId);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mIsScanFinished = true;
            }
        }
    };

    public BluetoothTest() {
        super("com.android.settings", Settings.BluetoothSettingsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        mActivity = getActivity();
        mContext = mInstrumentation.getTargetContext();
        mSolo = new Solo(mInstrumentation, mActivity);

        int resId = mActivity.getResources().getIdentifier("switch_widget", "id", "com.android.settings");
        mBtEnabler = (Switch) mActivity.findViewById(resId);

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        verifyPreconditions();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
    }

    public void testCase01_setBluetoothOn() throws InterruptedException {
        debugLog("TestCase01_setBluetoothOn start.");
        initBTStatus(BT_STATUS_OFF);
        if (mAdapter.isEnabled()) {
            debugLog("Can not diable the bluetooth");
            assertTrue(false);
            return ;
        }

        debugLog("Enable bluetooth by user");
        mSolo.clickOnView(mBtEnabler);
        int tryCount = 0;
        do {
            mSolo.sleep(TIME_OUT);
            tryCount++;
            mIsSuccess = (mBTState == BluetoothAdapter.STATE_ON);
        } while (!mIsSuccess && (tryCount < MAX_TRY_COUNT));

        assertEquals(true, mAdapter.isEnabled());
    }

    public void testCase02_pairRemoteDevice() throws InterruptedException {
        debugLog("TestCase02_pairRemoteDevice start.");

        initBTStatus(BT_STATUS_ON);
        removeAllBondedDevices();

        pair01_SearchRemoteDevice();

        pair02_sendPairRequest();

        pair03_confirmRequest();
    }

    public void testCase03_setBluetoothOff() throws InterruptedException {
        debugLog("testCase03_setBluetoothOff start.");
        initBTStatus(BT_STATUS_ON);
        if (!mAdapter.isEnabled()) {
            debugLog("Can not enable the bluetooth");
            assertTrue(false);
            return ;
        }

        debugLog("Disable bluetooth by user");
        mSolo.clickOnView(mBtEnabler);
        int tryCount = 0;
        do {
            mSolo.sleep(TIME_OUT);
            tryCount++;
            mIsSuccess = (mBTState == BluetoothAdapter.STATE_OFF);
        } while (!mIsSuccess && (tryCount < MAX_TRY_COUNT));

        assertEquals(false, mAdapter.isEnabled());
    }

    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            mActivity.finish();
        }
        mContext.unregisterReceiver(mReceiver);
        super.tearDown();
    }

    private void initBTStatus(int requestStatus) {
        boolean needSkip = false;
        boolean isBTOn = mAdapter.isEnabled();
        if (isBTOn && (requestStatus == BT_STATUS_OFF)) {
            debugLog("Disable bluetooth via API");
            mAdapter.disable();
        } else if (!isBTOn && (requestStatus == BT_STATUS_ON)) {
            debugLog("Enable bluetooth via API");
            mAdapter.enable();
        } else {
            ///: Just skip when the status is ready.
            needSkip = true;
            debugLog("Just skip when the status is ready.");
        }

        if (!needSkip) {
            int tryCount = 0;
            do {
                mSolo.sleep(TIME_OUT);
                tryCount++;

                ///M: Enable BT success.
                mIsSuccess = (requestStatus == BT_STATUS_ON) && (mBTState == BluetoothAdapter.STATE_ON);
                ///M: Disable BT success.
                mIsSuccess |= (requestStatus == BT_STATUS_OFF) && (mBTState == BluetoothAdapter.STATE_OFF);
            } while (!mIsSuccess && (tryCount < MAX_TRY_COUNT));

            debugLog("initPreconditions() tryCount : " + tryCount + " mBTState : " + mBTState);
        }
    }

    private void pair01_SearchRemoteDevice()  {
        debugLog("pair01_SearchRemoteDevice()");
        /// M: 1>. Scan target device
        int scanCount = 0;
        boolean isRemoteDeviceFound = false;
        do {
            startScanning();
            int searchCount = 0;
            do {
                mSolo.sleep(TIME_OUT * 2);
                isRemoteDeviceFound = mSolo.searchText(REMOTE_DEVICE);
                searchCount ++;
                debugLog("searchCount " + searchCount + " --->  isRemoteDeviceFound : " + isRemoteDeviceFound);
                ///M: Search 5 times during a scan process.
            } while(!isRemoteDeviceFound && searchCount <= MAX_SEARCH_COUNT_DURING_SCAN);

            ///M: Make sure scan process is finished.
            if (mIsScanFinished) {
                scanCount ++;
            } else if (!isRemoteDeviceFound && !mIsScanFinished) {
                int waitCount = 0;
                do {
                    mSolo.sleep(TIME_OUT * 2);
                    waitCount ++;
                } while (!mIsScanFinished && waitCount <= MAX_COMMON_WAIT_COUNT);
            }

        } while (!isRemoteDeviceFound && scanCount <= MAX_SCAN_COUNT_DURING_TEST);
        ///M: Assert whether search the remote device.
        assertEquals(isRemoteDeviceFound, true);
    }

    private void pair02_sendPairRequest() {
        debugLog("pair02_sendPairRequest()");
        /// M: 2>. Send the pair request to remote device.
        int requestCount = 0;
        do {
            if (mBondState < BluetoothDevice.BOND_BONDING) {
                mSolo.clickOnText(REMOTE_DEVICE);
                requestCount ++;
                debugLog("Send the pairing request to " + REMOTE_DEVICE + " -- " + requestCount);
            }
            int waitCount = 0;
            do {
                if (!mIsReceivedResponse) {
                    mSolo.sleep(TIME_OUT * 2);
                    waitCount ++;
                }
            } while(!mIsReceivedResponse && waitCount <= MAX_WAIT_COUNT_DURING_RESPONSE);

            ///M: Target device is busy,press back to ignore the poup error dialog.
            if (mBondState == BluetoothDevice.BOND_NONE &&
                    BluetoothDevice.UNBOND_REASON_AUTH_FAILED <= mResponseId &&
                    mResponseId <= BluetoothDevice.UNBOND_REASON_REMOTE_AUTH_CANCELED) {
                if (mSolo.getCurrentViews(Button.class).size() == 3) {
                    mSolo.goBack();
                    mSolo.sleep(TIME_OUT);
                }
            }

        } while (!mIsReceivedResponse && requestCount <= MAX_REQUEST_COUNT_DURING_TEST);
        ///M: Assert whether receive the remote response.
        assertEquals(mIsReceivedResponse, true);
    }

    private void pair03_confirmRequest() {
        debugLog("pair03_confirmRequest()");
        /// M: 3>. Confirm the pairing request response
        int tryCount = 0;
        boolean isPopup = false;
        Button btnPair = null;
        do {
            isPopup = mSolo.searchText(mPairingKey);
            if (isPopup) {
                btnPair = mSolo.getButton(1);
                debugLog("Pairing confirm dialog pop up with PairingKey : " + mPairingKey);
            } else {
                mSolo.sleep(TIME_OUT * 2);
                tryCount ++;
            }
        } while (!isPopup && tryCount <= MAX_COMMON_WAIT_COUNT);
        ///M: Assert whether pairing confirm dialog poup up.
        assertEquals(isPopup, true);

        ///M: Once the confirm dialog pop up, confirm to pair.
        if (isPopup) {
            mSolo.clickOnView(btnPair);
            debugLog("Confirm to pair with the target");
            mSolo.sleep(TIME_OUT * 2);
            tryCount = 0;
            do {
                mSolo.sleep(TIME_OUT * 2);
                tryCount ++;
            } while (!mIsBonded && tryCount <= MAX_COMMON_WAIT_COUNT);
            ///M: Assert whether bond create success.
            assertEquals(mIsBonded, true);
        }
    }

    private void verifyPreconditions() {
        assertTrue(mActivity != null);
        assertTrue(mInstrumentation != null);
        assertTrue(mSolo != null);
        assertTrue(mBtEnabler != null);
        assertTrue(mAdapter != null);
        assertTrue(mContext != null);
        setWFDStateOff();
    }

    private void setWFDStateOff() {
        try {
            ContentResolver resolver = mContext.getContentResolver();
            String wfdKey = android.provider.Settings.Global.WIFI_DISPLAY_ON;
            int state = android.provider.Settings.Global.getInt(resolver, wfdKey);
            if (state != 0) {
                android.provider.Settings.Global.putInt(resolver, wfdKey, 0);
                mSolo.sleep(TIME_OUT);
                debugLog("WFD is enable, close it");
            }
        } catch (Exception e) {
        }
    }

    private void removeAllBondedDevices() {
        mAdapter.setName(LOCAL_DEVICE);
        Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();
        if (!bondedDevices.isEmpty()) {
           for (BluetoothDevice bondedDevice : bondedDevices) {
               bondedDevice.removeBond();
               debugLog("Remove bonded device : " + bondedDevice.getName());
           }
        }
        mSolo.sleep(TIME_OUT);
    }

    private void startScanning() {
        if (!mAdapter.isDiscovering()) {
            if (mAdapter.startDiscovery()) {
                mIsScanFinished = false;
                debugLog("Start scan device");
            }
        }
    }

    private void debugLog(String msg) {
        Log.d(TAG, msg);
    }
}
