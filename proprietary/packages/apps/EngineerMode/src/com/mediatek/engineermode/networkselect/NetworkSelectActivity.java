package com.mediatek.engineermode.networkselect;

import android.app.Activity;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;

import com.mediatek.engineermode.FeatureSupport;
import com.mediatek.engineermode.ModemCategory;
import com.mediatek.engineermode.R;
import com.mediatek.xlog.Xlog;

import java.util.Arrays;

/**
 *
 * For setting network mode.
 * @author mtk54043
 *
 */
public class NetworkSelectActivity extends Activity {
    private static final String TAG = "EM/NetworkMode";
    private static final int EVENT_QUERY_NETWORKMODE_DONE = 101;
    private static final int EVENT_SET_NETWORKMODE_DONE = 102;

    private static final int MODEM_FDD = 1;
    private static final int MODEM_TD = 2;
    private static final int MODEM_NO3G = 3;

    private static final int MODEM_MASK_WCDMA = 0x04;
    private static final int MODEM_MASK_TDSCDMA = 0x08;

    private static final int WCDMA_PREFERRED_INDEX = 0;
    private static final int GSM_ONLY_INDEX = 1;
    private static final int WCDMA_ONLY_INDEX = 2;
    private static final int GSM_WCDMA_AUTO_INDEX = 3;
    private static final int LTE_ONLY_INDEX = 4;
    private static final int LTE_GSM_WCDMA_INDEX = 5;
    private static final int LTE_WCDMA_INDEX = 6;

    private static final int GSM_ONLY_INDEX_TD = 0;
    private static final int WCDMA_ONLY_INDEX_TD = 1;
    private static final int GSM_WCDMA_AUTO_INDEX_TD = 2;
    private static final int LTE_ONLY_INDEX_TD = 3;
    private static final int LTE_GSM_WCDMA_INDEX_TD = 4;
    private static final int LTE_WCDMA_INDEX_TD = 5;

    private static final int WCDMA_PREFERRED = Phone.NT_MODE_WCDMA_PREF;
    private static final int GSM_ONLY = Phone.NT_MODE_GSM_ONLY;
    private static final int WCDMA_ONLY = Phone.NT_MODE_WCDMA_ONLY;
    private static final int GSM_WCDMA_AUTO = Phone.NT_MODE_GSM_UMTS;
    private static final int LTE_ONLY = Phone.NT_MODE_LTE_ONLY;
    private static final int LTE_GSM_WCDMA = Phone.NT_MODE_LTE_GSM_WCDMA;
    private static final int LTE_GSM_WCDMA_PREFERRED = 31;
    //RILConstants.NETWORK_MODE_LTE_GSM_WCDMA_PREF;
    private static final int LTE_WCDMA = Phone.NT_MODE_LTE_WCDMA;

    private Phone mPhone = null;

    private int mModemType;
    private int mSimType = PhoneConstants.SIM_ID_1;
    private int[] mNetworkTypeValues;
    private int mCurrentSelected = 0;

    private Spinner mPreferredNetworkSpinner = null;

    OnItemSelectedListener mPreferredNetworkHandler = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView parent, View v, int pos, long id) {
            Xlog.d(TAG, "onItemSelected " + pos);
            if (mCurrentSelected == pos) {
                return; // avoid listener being invoked by setSelection()
            }
            mCurrentSelected = pos;

            Message msg = mHandler.obtainMessage(EVENT_SET_NETWORKMODE_DONE);

            int settingsNetworkMode = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.PREFERRED_NETWORK_MODE, Phone.PREFERRED_NT_MODE);
            int selectNetworkMode = mNetworkTypeValues[pos];

            if (settingsNetworkMode != selectNetworkMode) {
                Xlog.d(TAG, "selectNetworkMode " + selectNetworkMode);
                Settings.Global.putInt(getContentResolver(),
                        Settings.Global.PREFERRED_NETWORK_MODE, selectNetworkMode);
                Settings.Global.putInt(getContentResolver(),
                        Settings.Global.USER_PREFERRED_NETWORK_MODE, selectNetworkMode);
                mPhone.setPreferredNetworkType(selectNetworkMode, msg);
            }
        }

        public void onNothingSelected(AdapterView parent) {

        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
            case EVENT_QUERY_NETWORKMODE_DONE:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    int type = ((int[]) ar.result)[0];
                    Xlog.d(TAG, "Get Preferred Type " + type);
                    int index = findSpinnerIndexByType(type);
                    if (index >= 0 && index < mPreferredNetworkSpinner.getCount()) {
                        mCurrentSelected = index;
                        mPreferredNetworkSpinner.setSelection(index, true);
                    }
                } else {
                    Toast.makeText(NetworkSelectActivity.this, R.string.query_preferred_fail,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case EVENT_SET_NETWORKMODE_DONE:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    mPhone.getPreferredNetworkType(obtainMessage(EVENT_QUERY_NETWORKMODE_DONE));
                }
                break;
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.networkmode_switching);
        mPreferredNetworkSpinner = (Spinner) findViewById(R.id.networkModeSwitching);
    }

    @Override
    protected void onResume() {
        mSimType = getIntent().getIntExtra("mSimType", ModemCategory.getCapabilitySim());

        if (TelephonyManager.getDefault().getPhoneCount() > 1) {
            mPhone = PhoneFactory.getPhone(mSimType);
        } else {
            mPhone = PhoneFactory.getDefaultPhone();
        }
        mModemType = getModemType();
        if (mModemType == MODEM_TD) {
            // No "CDMA preferred" for TD
            mNetworkTypeValues = new int[] {GSM_ONLY, WCDMA_ONLY, GSM_WCDMA_AUTO,
                        LTE_ONLY, LTE_GSM_WCDMA, LTE_WCDMA};
            String[] labels = getResources().getStringArray(R.array.mTddNetworkLabels);
            if (!FeatureSupport.isSupported(FeatureSupport.FK_LTE_SUPPORT)) {
                labels = Arrays.copyOf(labels, GSM_WCDMA_AUTO_INDEX_TD + 1);
            }
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mPreferredNetworkSpinner.setAdapter(adapter);
        } else if (mModemType == MODEM_FDD) {
            mNetworkTypeValues = new int[] {WCDMA_PREFERRED, GSM_ONLY, WCDMA_ONLY, GSM_WCDMA_AUTO,
                        LTE_ONLY, LTE_GSM_WCDMA, LTE_WCDMA};
            String[] labels = getResources().getStringArray(R.array.mWcdmaNetworkLabels);
            if (!FeatureSupport.isSupported(FeatureSupport.FK_LTE_SUPPORT)) {
                labels = Arrays.copyOf(labels, GSM_WCDMA_AUTO_INDEX + 1);
            }
            if (!FeatureSupport.isSupported(FeatureSupport.FK_WCDMA_PREFERRED)) {
                // remove WCDMA_PREFERRED
                labels = Arrays.copyOfRange(labels, 1, labels.length);
                mNetworkTypeValues =
                        Arrays.copyOfRange(mNetworkTypeValues, 1, mNetworkTypeValues.length);
            }
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mPreferredNetworkSpinner.setAdapter(adapter);
        } else {
//            mPreferredNetworkSpinner.setEnabled(false);
            Xlog.w(TAG, "Isn't TD/WCDMA modem: " + mModemType);
        }
        mPreferredNetworkSpinner.setOnItemSelectedListener(mPreferredNetworkHandler);
        mPhone.getPreferredNetworkType(mHandler.obtainMessage(EVENT_QUERY_NETWORKMODE_DONE));
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private int getModemType() {
        String mt = SystemProperties.get("gsm.baseband.capability");
        Xlog.i(TAG, "gsm.baseband.capability " + mt);
        if (FeatureSupport.isSupported(FeatureSupport.FK_DT_SUPPORT)
                && mSimType == PhoneConstants.SIM_ID_2) {
            mt = SystemProperties.get("gsm.baseband.capability.md2");
            Xlog.i(TAG, "gsm.baseband.capability.md2 " + mt);
        }
        int mode = MODEM_NO3G;

        if (mt == null) {
            mode = MODEM_NO3G;
        } else {
            try {
                int mask = Integer.valueOf(mt);
                if ((mask & MODEM_MASK_TDSCDMA) == MODEM_MASK_TDSCDMA) {
                    mode = MODEM_TD;
                } else if ((mask & MODEM_MASK_WCDMA) == MODEM_MASK_WCDMA) {
                    mode = MODEM_FDD;
                } else {
                    mode = MODEM_NO3G;
                }
            } catch (NumberFormatException e) {
                mode = MODEM_NO3G;
            }
        }
        return mode;
    }

    private int findSpinnerIndexByType(int type) {
        if (type == WCDMA_PREFERRED && mModemType == MODEM_TD) {
            type = GSM_WCDMA_AUTO;
        }
        if (type == WCDMA_PREFERRED
                && !FeatureSupport.isSupported(FeatureSupport.FK_WCDMA_PREFERRED)) {
            type = GSM_WCDMA_AUTO;
        }
        if (type == LTE_GSM_WCDMA_PREFERRED) {
            type = LTE_GSM_WCDMA;
        }
        for (int i = 0; i < mNetworkTypeValues.length; i++) {
            if (mNetworkTypeValues[i] == type) {
                return i;
            }
        }
        return -1;
    }
}
