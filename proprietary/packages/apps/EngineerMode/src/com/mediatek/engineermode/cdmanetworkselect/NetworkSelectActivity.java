package com.mediatek.engineermode.cdmanetworkselect;

import android.app.Activity;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.mediatek.engineermode.R;
import com.mediatek.xlog.Xlog;
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

    private static final int HYBRID_INDEX = 0;
    private static final int CDMA_1X_ONLY_INDEX = 1;
    private static final int EVDO_ONLY_INDEX = 2;
    private static final int HYBRID = 4;
    private static final int CDMA_1X_ONLY  = 5;
    private static final int EVDO_ONLY = 6;

    private boolean mFirstEnter = true;
    private int mCurrentSettingsNetworkMode;

    private Phone mPhone = null;

    private Spinner mPreferredNetworkSpinner = null;

    private OnItemSelectedListener mPreferredNetworkHandler = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView parent, View v, int pos, long id) {
            Message msg = mHandler.obtainMessage(EVENT_SET_NETWORKMODE_DONE);

            int settingsNetworkMode = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.PREFERRED_NETWORK_MODE, Phone.PREFERRED_NT_MODE);
            mCurrentSettingsNetworkMode = settingsNetworkMode;
            int selectNetworkMode = Phone.PREFERRED_NT_MODE;
            switch (pos) {
            case HYBRID_INDEX: // 4
                selectNetworkMode = HYBRID;
                break;
            case CDMA_1X_ONLY_INDEX: // 5
                selectNetworkMode = CDMA_1X_ONLY;
                break;
            case EVDO_ONLY_INDEX: // 6
                selectNetworkMode = EVDO_ONLY;
                break;
            default:
                break;
            }
            if (mFirstEnter == true) {
                mFirstEnter = false;
            } else {
                Xlog.d(TAG, "selectNetworkMode " + selectNetworkMode);
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
                Xlog.d(TAG, "Get response EVENT_QUERY_NETWORKMODE_DONE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    int type = ((int[]) ar.result)[0];
                    Xlog.d(TAG, "Get Preferred Type " + type);
                    switch (type) {
                    case HYBRID:
                        mPreferredNetworkSpinner.setSelection(HYBRID_INDEX, true);
                        break;
                    case CDMA_1X_ONLY:
                        mPreferredNetworkSpinner.setSelection(CDMA_1X_ONLY_INDEX, true);
                        break;
                    case EVDO_ONLY:
                        mPreferredNetworkSpinner.setSelection(EVDO_ONLY_INDEX, true);
                        break;
                    default:
                        break;
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
        if (TelephonyManager.getDefault().getPhoneCount() > 1) {
            mPhone = PhoneFactory.getPhone(PhoneConstants.SIM_ID_1);
        } else {
            mPhone = PhoneFactory.getDefaultPhone();
        }
        mPreferredNetworkSpinner = (Spinner) findViewById(R.id.networkModeSwitching);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.mCdmaNetworkLabels));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPreferredNetworkSpinner.setAdapter(adapter);

        mPreferredNetworkSpinner.setOnItemSelectedListener(mPreferredNetworkHandler);
        mFirstEnter = true;
    }

    @Override
    protected void onResume() {
        mCurrentSettingsNetworkMode = android.provider.Settings.Global.getInt(getContentResolver(),
                android.provider.Settings.Global.PREFERRED_NETWORK_MODE, Phone.PREFERRED_NT_MODE);
        mPhone.getPreferredNetworkType(mHandler.obtainMessage(EVENT_QUERY_NETWORKMODE_DONE));
        Xlog.d(TAG, "Query EVENT_QUERY_NETWORKMODE_DONE");
        super.onResume();
    }
}
