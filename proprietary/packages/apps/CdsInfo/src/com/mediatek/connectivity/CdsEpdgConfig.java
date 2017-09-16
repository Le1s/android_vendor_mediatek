package com.mediatek.connectivity;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.mediatek.epdg.EpdgConfig;
import com.mediatek.epdg.EpdgManager;

/**
  *
  * Class for configure EPDG parameters for each APN.
  *
  */
public class CdsEpdgConfig extends Fragment implements View.OnClickListener {
    private static final String TAG = "CdsEpdgConfig";

    private Button mSetAllBtnCmd = null;
    private Button mSetBtnCmd = null;
    private Button mSetApnCmd = null;
    private Context mContext;
    private EditText mEdpgServerAddress = null;
    private EditText mEpdgCertPath = null;
    private EditText mEpdgIkeAlgo = null;
    private EditText mEpdgEspAlgo = null;
    private Spinner mEdpgApnSpinner = null;
    private Spinner mEdpgAuthSpinner = null;
    private Spinner mEpdgSimSpinner = null;
    private Spinner mEpdgMpSpinner = null;
    private int mEpdgApnType = 1; //IMS
    private int mEpdgAuthType = 1; //EAP-AKA
    private int mEpdgSimIndex = 1; //SIM1
    private int mEpdgMobiltyProtcol = 1; //DSMIPv6
    private boolean mEpdgApplyAll = false;

    private EpdgManager mEpdgManager = null;

    private static final String[] APN_TYPE =
            new String[] {"FAST (MMS + XCAP + SUPL)", "IMS (IMS)", "NET"};
    private static final String[] AUTH_LIST =
            new String[] {"EAP-AKA", "EAP-SIM", "EAP-AKA'"};
    private static final String[] SIM_LIST = new String[] {"SIM1", "SIM2"};
    private static final String[] PROTOCOL_LIST = new String[] {"DSMIPv6", "NBM"};
    private static final String   OK_MSG = "OK";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cds_epdg_config, container, false);

        mContext = getActivity().getBaseContext();

        mEdpgApnSpinner = (Spinner) view.findViewById(R.id.apnSpinnner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, APN_TYPE);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEdpgApnSpinner.setAdapter(adapter);
        mEdpgApnSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View arg1,
            int position, long arg3) {
                mEpdgApnType = position;
                showEpdgConfig();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        mEdpgAuthSpinner = (Spinner) view.findViewById(R.id.authSpinnner);
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, AUTH_LIST);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEdpgAuthSpinner.setAdapter(adapter);
        mEdpgAuthSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View arg1,
            int position, long arg3) {
                mEpdgAuthType = position + 1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        mEpdgSimSpinner = (Spinner) view.findViewById(R.id.simSpinnner);
        adapter = new ArrayAdapter<String>(getActivity(),
                                           android.R.layout.simple_spinner_item, SIM_LIST);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEpdgSimSpinner.setAdapter(adapter);
        mEpdgSimSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View arg1,
            int position, long arg3) {
                mEpdgSimIndex = position + 1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        mEpdgMpSpinner = (Spinner) view.findViewById(R.id.protSpinnner);
        adapter = new ArrayAdapter<String>(getActivity(),
                                           android.R.layout.simple_spinner_item, PROTOCOL_LIST);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEpdgMpSpinner.setAdapter(adapter);
        mEpdgMpSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View arg1,
            int position, long arg3) {
                mEpdgMobiltyProtcol = position + 1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        mEdpgServerAddress = (EditText)  view.findViewById(R.id.Server);
        mEpdgCertPath = (EditText)  view.findViewById(R.id.CertPath);
        mEpdgIkeAlgo = (EditText)  view.findViewById(R.id.IkeAlgo);
        mEpdgEspAlgo = (EditText)  view.findViewById(R.id.EspAlgo);

        mSetBtnCmd = (Button) view.findViewById(R.id.Set);
        mSetBtnCmd.setOnClickListener(this);

        mSetAllBtnCmd = (Button) view.findViewById(R.id.SetAll);
        mSetAllBtnCmd.setOnClickListener(this);

        mSetApnCmd = (Button) view.findViewById(R.id.ApnActivity);
        mSetApnCmd.setOnClickListener(this);

        mEpdgManager = EpdgManager.getInstance(mContext);

        showEpdgConfig();
        Log.i(TAG, "CdsEpdgConfig in onCreateView");
        return view;
    }

    private void showEpdgConfig() {
        EpdgConfig epdgConfig = mEpdgManager.getConfiguration(mEpdgApnType);
        mEpdgAuthType = epdgConfig.authType;
        mEdpgAuthSpinner.setSelection(epdgConfig.authType - 1);
        mEpdgSimIndex = epdgConfig.simIndex;
        mEpdgSimSpinner.setSelection(epdgConfig.simIndex - 1);
        mEpdgMobiltyProtcol = epdgConfig.mobilityProtocol;
        mEpdgMpSpinner.setSelection(epdgConfig.mobilityProtocol - 1);
        mEdpgServerAddress.setText(epdgConfig.edpgServerAddress);
        mEpdgCertPath.setText(epdgConfig.certPath);
        mEpdgIkeAlgo.setText(epdgConfig.ikeaAlgo);
        mEpdgEspAlgo.setText(epdgConfig.espAlgo);
        Log.i(TAG, "epdgConfig:" + epdgConfig);
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        switch (buttonId) {
        case R.id.Set:
            handleEpdgConfig();
            Toast.makeText(mContext, OK_MSG, Toast.LENGTH_LONG).show();
            break;
        case R.id.SetAll:
            handleEpdgSetAll();
            Toast.makeText(mContext, OK_MSG, Toast.LENGTH_LONG).show();
            break;
        case R.id.ApnActivity:
            startApnActivity();
            break;
        default:
            break;
        }
    }

    private void startApnActivity() {
        try {
            Intent intent = new Intent("android.settings.APN_SETTINGS");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "ActivityNotFoundException");
        }
    }

    private void handleEpdgSetAll() {
        int i = 0;
        EpdgConfig[] configs = new EpdgConfig[EpdgManager.MAX_NETWORK_NUM];
        for (i = 0; i < EpdgManager.MAX_NETWORK_NUM; i++) {
            configs[i] = getEpdgConfigFromApp(i);
        }
        mEpdgManager.setConfiguration(configs);
    }

    private void handleEpdgConfig() {
        EpdgConfig epdgConfig = getEpdgConfigFromApp(mEpdgApnType);
        mEpdgManager.setConfiguration(mEpdgApnType, epdgConfig);
    }

    private EpdgConfig getEpdgConfigFromApp(int networkType) {
        EpdgConfig epdgConfig = mEpdgManager.getConfiguration(networkType);
        epdgConfig.authType = mEpdgAuthType;
        epdgConfig.simIndex = mEpdgSimIndex;
        epdgConfig.mobilityProtocol = mEpdgMobiltyProtcol;
        epdgConfig.edpgServerAddress = mEdpgServerAddress.getText().toString();
        epdgConfig.certPath = mEpdgCertPath.getText().toString();
        epdgConfig.ikeaAlgo = mEpdgIkeAlgo.getText().toString();
        epdgConfig.espAlgo = mEpdgEspAlgo.getText().toString();

        return epdgConfig;
    }

    private CheckBox.OnCheckedChangeListener mCbChkListener =
        new CheckBox.OnCheckedChangeListener()
    {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            mEpdgApplyAll = isChecked;
        }
    };
}
