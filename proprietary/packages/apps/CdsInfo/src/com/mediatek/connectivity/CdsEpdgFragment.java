package com.mediatek.connectivity;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static android.net.ConnectivityManager.TYPE_NONE;

/**
  *
  * Class for trigger network reqeust by each network capability.
  *
  */

public class CdsEpdgFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "CDS_EPDG";
    private static final int MAX_APN_TYPE = 5;

    private static final String[] APN_LIST = new String[] {"MMS", "SUPL", "IMS", "XCAP", "RCS"};
    private static final int[] APN_CAP_LIST = new int[] {NetworkCapabilities.NET_CAPABILITY_MMS,
        NetworkCapabilities.NET_CAPABILITY_SUPL,
        NetworkCapabilities.NET_CAPABILITY_IMS,
        NetworkCapabilities.NET_CAPABILITY_XCAP,
        NetworkCapabilities.NET_CAPABILITY_RCS};

    private ConnectivityManager mConnMgr = null;
    private Context mContext;
    private Spinner mApnSpinner = null;
    private Toast mToast;
    private Button mRunBtnCmd = null;
    private Button mStopBtnCmd = null;
    private TextView mOutputScreen = null;

    private int mSelectApnPos = 0;

    private TestNetworkRequest mNetworkRequests[];

    /**
      *
      * Utiltiy Class for multiple network request.
      *
      */
    @SuppressWarnings("membername")
    private static class TestNetworkRequest {
        NetworkCapabilities networkCapabilities;
        NetworkRequest  networkRequest;
        Network         currentNetwork;

        NetworkCallback networkCallback = new NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                currentNetwork = network;
                Log.d(TAG, "onAvailable:" + network);
            }
            @Override
            public void onLost(Network network) {
                if (network.equals(currentNetwork)) {
                    currentNetwork = null;
                }
                Log.d(TAG, "onLost:" + network);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cds_epdg_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        mApnSpinner = (Spinner) view.findViewById(R.id.apnTypeSpinnner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_spinner_item, APN_LIST);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mApnSpinner.setAdapter(adapter);
        mApnSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View arg1,
            int position, long arg3) {
                // TODO Auto-generated method stub
                mSelectApnPos     = position;
                mApnSpinner.requestFocus();
                updateConnectButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        mRunBtnCmd = (Button) view.findViewById(R.id.Start);
        mRunBtnCmd.setOnClickListener(this);
        mStopBtnCmd = (Button) view.findViewById(R.id.Stop);
        mStopBtnCmd.setOnClickListener(this);

        mOutputScreen = (TextView) view.findViewById(R.id.outputText);
        mToast = Toast.makeText(getActivity(), null, Toast.LENGTH_SHORT);

        mConnMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        createNetworkRequest(MAX_APN_TYPE);

        Log.i(TAG, "CdsEpdgFragment in onCreateView");
        return view;
    }

    private void createNetworkRequest(int count) {
        mNetworkRequests = new TestNetworkRequest[count + 1];

        for (int i = 0; i < count; i++) {
            NetworkCapabilities netCap = new NetworkCapabilities();
            netCap.addCapability(APN_CAP_LIST[i]);
            NetworkRequest netRequest = new NetworkRequest(netCap, TYPE_NONE, i);
            mNetworkRequests[i] = new TestNetworkRequest();
            mConnMgr.registerNetworkCallback(netRequest, mNetworkRequests[i].networkCallback);
            mNetworkRequests[i].networkCapabilities = netCap;
            mNetworkRequests[i].networkRequest = netRequest;
        }
    }

    private void updateConnectButton() {
        NetworkInfo nwInfo = null;

        //nwInfo = mConnMgr.getNetworkInfo(APN_TYPE_LIST[mSelectApnPos]);

        if (nwInfo == null) {
            mRunBtnCmd.setEnabled(true);
            mStopBtnCmd.setEnabled(true);
            return;
        }

        if (nwInfo.getState() == NetworkInfo.State.CONNECTED) {
            mRunBtnCmd.setEnabled(false);
            mStopBtnCmd.setEnabled(true);
        } else {
            mRunBtnCmd.setEnabled(true);
            mStopBtnCmd.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        switch (buttonId) {
            case R.id.Start:
                runNetworkRequest();
                break;
            case R.id.Stop:
                stopNetworkRequest();
                break;
            default:
                break;
        }
    }

    private void runNetworkRequest() {
        mConnMgr.requestNetwork(mNetworkRequests[mSelectApnPos].networkRequest,
                mNetworkRequests[mSelectApnPos].networkCallback);
    }

    private void stopNetworkRequest() {
        mConnMgr.unregisterNetworkCallback(mNetworkRequests[mSelectApnPos].networkCallback);
    }
}
