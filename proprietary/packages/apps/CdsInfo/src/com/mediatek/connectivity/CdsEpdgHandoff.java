package com.mediatek.connectivity;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.epdg.EpdgManager;
import com.mediatek.rns.RnsManager;

/**
  *
  * Class for configure EPDG handoff criteria.
  *
  */
public class CdsEpdgHandoff extends Fragment implements View.OnClickListener {
    private static final String TAG = "CdsEpdgHandoff";

    private Button mSetProfileBtnCmd = null;
    private Button mSetRoveBtnCmd = null;
    private Context mContext;
    private EditText mRoveIn = null;
    private EditText mRoveOut = null;
    private RnsManager mRnsManager = null;
    private Spinner mEpdgUserProfileSpinner = null;
    private Spinner mEpdgWfcSpinner = null;
    private TextView mEpdgPdnPath = null;
    private TextView mEpdgRetryChoose = null;
    private TextView mEpdgWifiRssi = null;
    private Toast   mToast = null;

    private int mEpdgUserProfile = 1;
    private int mEpdgWfcSetting = 1;

    private static final String[] USER_PROFILE_SETTING =
            new String[] {"None", "Wi-Fi Only", "Wi-Fi Preferred",
                        "Celluar Only", "Celluar Preferred"};
    private static final String[] USER_WFC_SETTING =
            new String[] {"Always Use", "Ask Every Time", "Never Use"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cds_epdg_handoff, container, false);

        mContext = getActivity().getBaseContext();

        mEpdgUserProfileSpinner = (Spinner) view.findViewById(R.id.epdgProfile);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, USER_PROFILE_SETTING);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEpdgUserProfileSpinner.setAdapter(adapter);
        mEpdgUserProfileSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View arg1,
            int position, long arg3) {
                mEpdgUserProfile = position - 1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        int userProfile = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.RNS_USER_PREFERENCE, -1);
        mEpdgUserProfileSpinner.setSelection(userProfile + 1);
        Log.d(TAG, "userProfile:" + userProfile);

        mEpdgWfcSpinner = (Spinner) view.findViewById(R.id.epdgWfcSetting);
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, USER_WFC_SETTING);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEpdgWfcSpinner.setAdapter(adapter);
        mEpdgWfcSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View arg1,
            int position, long arg3) {
                mEpdgWfcSetting = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        int wfcSetting = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.WHEN_TO_MAKE_WIFI_CALLS,
                TelephonyManager.WifiCallingChoices.ALWAYS_USE);
        mEpdgWfcSpinner.setSelection(wfcSetting);
        Log.d(TAG, "wfcSetting:" + wfcSetting);

        mSetProfileBtnCmd = (Button) view.findViewById(R.id.SetProfile);
        mSetProfileBtnCmd.setOnClickListener(this);
        mSetRoveBtnCmd = (Button) view.findViewById(R.id.SetRove);
        mSetRoveBtnCmd.setOnClickListener(this);

        mRoveIn = (EditText) view.findViewById(R.id.WifiRoveIn);
        mRoveOut = (EditText) view.findViewById(R.id.WifiRoveOut);

        try {
            int roveIn = Settings.Global.getInt(mContext.getContentResolver(),
                    Settings.Global.RNS_WIFI_ROVE_IN_RSSI);
            int roveOut = Settings.Global.getInt(mContext.getContentResolver(),
                    Settings.Global.RNS_WIFI_ROVE_OUT_RSSI);

            mRoveIn.setText(Integer.toString(roveIn));
            mRoveOut.setText(Integer.toString(roveOut));
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        mToast = Toast.makeText(getActivity(), null, Toast.LENGTH_SHORT);

        mEpdgPdnPath = (TextView) view.findViewById(R.id.pdn_path);
        mEpdgRetryChoose = (TextView) view.findViewById(R.id.pdn_retry_choose);
        mEpdgWifiRssi = (TextView) view.findViewById(R.id.wifi_rssi);

        mRnsManager = (RnsManager) mContext.getSystemService(Context.RNS_SERVICE);
        Log.i(TAG, "CdsEpdgHandoff in onCreateView");

        updatePdnPath();
        updateRetryChoose();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext.registerReceiver(mWifiStateReceiver,
            new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        mContext.unregisterReceiver(mWifiStateReceiver);
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        switch(buttonId) {
            case R.id.SetProfile:
                handleEpdgProfile();
                break;
            case R.id.SetRove:
                handleRoveValue();
                break;
            default:
                break;
        }
        updatePdnPath();
    }

    private void updatePdnPath() {
        String pdnPath = "Unknown";
        int path = mRnsManager.getAllowedRadioList(EpdgManager.TYPE_FAST);

        Log.i(TAG, "RNS radio path:" + path);

        if (path == RnsManager.ALLOWED_RADIO_WIFI) {
            pdnPath = "EPDG";
        } else if (path == RnsManager.ALLOWED_RADIO_MOBILE) {
            pdnPath = "MOBILE";
        } else if (path == RnsManager.ALLOWED_RADIO_MAX) {
            pdnPath = "EPDG/MOBILE";
        }

        mEpdgPdnPath.setText("RNS Path Policy:" + pdnPath);
    }

    private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                    return;
            }

            if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
                handleSignalChanged(intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0));
            }
        }
    };

    private void handleSignalChanged(int rssi) {
        mEpdgWifiRssi.setText("RSSI: " + rssi);
    }

    private void updateRetryChoose() {
        StringBuffer msg = null;
        int isRetry = ConnectivityManager.TYPE_NONE;
        isRetry = mRnsManager.getTryAnotherRadioType(ConnectivityManager.TYPE_WIFI);
        msg = new StringBuffer("Wi-Fi is failed. Try Mobile: " + isRetry + "\r\n");
        isRetry = mRnsManager.getTryAnotherRadioType(ConnectivityManager.TYPE_MOBILE);
        msg.append("Mobile is failed. Try Wi-Fi: " + isRetry);
        mEpdgRetryChoose.setText(msg.toString());
    }

    private void handleEpdgProfile() {
        Log.i(TAG, "handleEpdgProfile:" +  mEpdgWfcSetting + "/" + mEpdgUserProfile);
        Settings.System.putInt(mContext.getContentResolver(),
            Settings.System.WHEN_TO_MAKE_WIFI_CALLS, mEpdgWfcSetting);
        Settings.System.putInt(mContext.getContentResolver(),
            Settings.System.RNS_USER_PREFERENCE, mEpdgUserProfile);
    }

    private void handleRoveValue() {
        String roveIn = mRoveIn.getText().toString();
        String roveOut = mRoveOut.getText().toString();
        Log.i(TAG, "handleRoveValue:" +  roveIn + "/" + roveOut);

        try {
            int inRssi = Integer.parseInt(roveIn);
            int outRssi = Integer.parseInt(roveOut);

            if (inRssi  < outRssi + 5) {
                mToast.setText("Roll in value should be large than Roll out value at least 5 dbm");
                mToast.show();
                return;
            } else if (inRssi > -45 || inRssi < -85) {
                mToast.setText("The range of roll-in is from -45 to -85 (dbm)");
                mToast.show();
                return;
            } else if (outRssi > -50 || outRssi < -90) {
                mToast.setText("The range of roll-out is from -50 dbm to -90 (dbm)");
                mToast.show();
                return;
            }

            Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.RNS_WIFI_ROVE_IN_RSSI, inRssi);
            Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.RNS_WIFI_ROVE_OUT_RSSI, outRssi);
        } catch (NumberFormatException ne) {
            ne.printStackTrace();
        }
    }
}
