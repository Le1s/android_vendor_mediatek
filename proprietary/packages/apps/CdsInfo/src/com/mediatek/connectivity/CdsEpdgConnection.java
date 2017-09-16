package com.mediatek.connectivity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructTimeval;
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

import static android.system.OsConstants.AF_INET;
import static android.system.OsConstants.AF_INET6;
import static android.system.OsConstants.IPPROTO_ICMP;
import static android.system.OsConstants.IPPROTO_ICMPV6;
import static android.system.OsConstants.SOCK_DGRAM;
import static android.system.OsConstants.SOL_SOCKET;
import static android.system.OsConstants.SO_RCVTIMEO;

import com.android.internal.util.HexDump;

import com.mediatek.epdg.EpdgManager;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.FileDescriptor;

/**
  *
  * Class for trigger network reqeust by each network capability.
  *
  */

@SuppressWarnings("staticvariablename")
public class CdsEpdgConnection extends Fragment implements View.OnClickListener {
    private static final String TAG = "CDS_EPDG_CONNECTION";
    private static final int MAX_APN_TYPE = 5;

    private static final int MAX_CONNECTION_TIME = 3 * 60 * 1000;

    // Default connection and socket timeout of 60 seconds.  Tweak to taste.
    private static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;

    private static final int EVENT_MSG_INFO = 1001;

    private static final String[] APN_LIST = new String[] {"MMS", "SUPL", "IMS", "XCAP", "RCS"};
    private static final int[] APN_CAP_LIST = new int[] {NetworkCapabilities.NET_CAPABILITY_MMS,
        NetworkCapabilities.NET_CAPABILITY_SUPL,
        NetworkCapabilities.NET_CAPABILITY_IMS,
        NetworkCapabilities.NET_CAPABILITY_XCAP,
        NetworkCapabilities.NET_CAPABILITY_RCS};

    private static ConnectivityManager mConnMgr = null;
    private static EpdgManager mEpdgManager = null;
    private static Context mContext;
    private static Spinner mApnSpinner = null;

    private static Button mRunBtnCmd = null;
    private static Button mStopBtnCmd = null;
    private static Button mShowBtnCmd = null;
    private static TextView mOutputScreen = null;
    private static TextView mOutput2Screen = null;

    private static int mSelectApnPos = 0;

    private static Toast mToast;
    private static TestNetworkRequest mNetworkRequests[];

    /**
      *
      * Utiltiy Class for multiple network request.
      *
      */
    @SuppressWarnings("membername")
    private static class TestNetworkRequest {
        String          apnType;
        boolean         mIsRequested;
        NetworkCapabilities networkCapabilities;
        NetworkRequest  networkRequest;
        Network         currentNetwork;
        boolean         isRun;


        TestNetworkRequest(String apn) {
            apnType = apn;
            mIsRequested = false;
            isRun = false;
        }

        boolean getIsRequested() {
            return mIsRequested;
        }

        void setIsRequested(boolean isRequested) {
            mIsRequested = isRequested;
        }

        void setIsRun(boolean runFlag) {
            isRun = runFlag;
        }

        NetworkCallback networkCallback = new NetworkCallback() {

            @Override
            public void onPreCheck(Network network) {
                String info = "Connection is under pre-check (" + apnType + ":" + network + ")";
                Log.d(TAG, info);
                Message msg = mHandler.obtainMessage(EVENT_MSG_INFO, (Object) info);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                String info = "Connection is losing (" + apnType + ":" + network + ")";
                Log.d(TAG, info);
                Message msg = mHandler.obtainMessage(EVENT_MSG_INFO, (Object) info);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onUnavailable() {
                String info = "Connection is unavailable (" + apnType + ":" + networkRequest + ")";
                Log.d(TAG, info);
                mConnMgr.unregisterNetworkCallback(this);
                mIsRequested = false;
                Message msg = mHandler.obtainMessage(EVENT_MSG_INFO, (Object) info);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onAvailable(Network network) {
                currentNetwork = network;
                String info = "Connection is available (" + apnType + ":" + network + ")";
                Log.d(TAG, info);
                Message msg = mHandler.obtainMessage(EVENT_MSG_INFO, (Object) info);
                mHandler.sendMessage(msg);

                if (!isRun && apnType.equals("MMS")) {
                    mConnMgr.setProcessDefaultNetwork(network);
                    isRun = true;
                    runMmsTest();
                }
            }

            @Override
            public void onLost(Network network) {
                if (network.equals(currentNetwork)) {
                    currentNetwork = null;
                }
                int[] capTypes = networkCapabilities.getCapabilities();
                int reason = mEpdgManager.getReasonCode(capTypes[0]);
                String info = "Connection is lost (" + apnType + ":" + network
                                + " + reason:" + reason + " )";
                Log.d(TAG, info);
                mConnMgr.unregisterNetworkCallback(this);
                mIsRequested = false;
                Message msg = mHandler.obtainMessage(EVENT_MSG_INFO, (Object) info);
                mHandler.sendMessage(msg);
                isRun = false;
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
        mShowBtnCmd = (Button) view.findViewById(R.id.Show);
        mShowBtnCmd.setOnClickListener(this);

        mOutputScreen = (TextView) view.findViewById(R.id.outputText);
        mOutput2Screen = (TextView) view.findViewById(R.id.output2Text);
        mToast = Toast.makeText(getActivity(), null, Toast.LENGTH_SHORT);

        mConnMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mEpdgManager = EpdgManager.getInstance(mContext);

        createNetworkRequest(MAX_APN_TYPE);

        updateConnectStatus();
        //showAllNetworks();
        Log.i(TAG, "CdsEpdgFragment in onCreateView");
        return view;
    }

    private void createNetworkRequest(int count) {
        mNetworkRequests = new TestNetworkRequest[count + 1];

        for (int i = 0; i < count; i++) {
            NetworkCapabilities netCap = new NetworkCapabilities();
            netCap.addCapability(APN_CAP_LIST[i]);
            netCap.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
            netCap.addTransportType(NetworkCapabilities.TRANSPORT_EPDG);
            NetworkRequest netRequest = new NetworkRequest(netCap,
                    legacyTypeForNetworkCapabilities(APN_CAP_LIST[i]), i);
            mNetworkRequests[i] = new TestNetworkRequest(APN_LIST[i]);
            //mConnMgr.registerNetworkCallback(netRequest, mNetworkRequests[i].networkCallback);
            mNetworkRequests[i].networkCapabilities = netCap;
            mNetworkRequests[i].networkRequest = netRequest;
        }
    }

    private int legacyTypeForNetworkCapabilities(int capability) {
        if (capability == NetworkCapabilities.NET_CAPABILITY_IMS) {
            return ConnectivityManager.TYPE_MOBILE_IMS;
        }

        if (capability == NetworkCapabilities.NET_CAPABILITY_MMS) {
            return ConnectivityManager.TYPE_MOBILE_MMS;
        }

        if (capability == NetworkCapabilities.NET_CAPABILITY_SUPL) {
            return ConnectivityManager.TYPE_MOBILE_SUPL;
        }

        return ConnectivityManager.TYPE_NONE;
    }

    private static void updateConnectButton() {
        NetworkInfo nwInfo = null;

        Network network = mNetworkRequests[mSelectApnPos].currentNetwork;

        if (network != null) {
            nwInfo = mConnMgr.getNetworkInfo(network);
            Log.i(TAG, "updateConnectButton:" + nwInfo);
        }

        if (nwInfo == null) {
            if (mNetworkRequests[mSelectApnPos].getIsRequested()) {
                mRunBtnCmd.setEnabled(false);
                mStopBtnCmd.setEnabled(true);
            } else {
                mRunBtnCmd.setEnabled(true);
                mStopBtnCmd.setEnabled(false);
            }
            return;
        }

        if (nwInfo.isConnectedOrConnecting()) {
            mRunBtnCmd.setEnabled(false);
            mStopBtnCmd.setEnabled(true);
        } else {
            mRunBtnCmd.setEnabled(true);
            mStopBtnCmd.setEnabled(false);
        }

        updateConnectStatus();
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        switch (buttonId) {
            case R.id.Start:
                runNetworkRequest();
                updateConnectButton();
                break;
            case R.id.Stop:
                stopNetworkRequest();
                break;
            case R.id.Show:
                showAllNetworks();
                break;
            default:
                break;
        }
    }

    private static void showAllNetworks() {
        StringBuffer networkInfo = new StringBuffer();
        Network[] networks = mConnMgr.getAllNetworks();
        networkInfo.append("Total " + networks.length + " networks");
        for (int i = 0; i < networks.length; i++) {
            NetworkInfo nwInfo = mConnMgr.getNetworkInfo(networks[i]);
            networkInfo.append(networks[i] + ":\r\n" + nwInfo + "\r\n");
        }
        mOutput2Screen.setText(networkInfo.toString());

        Log.i(TAG, "Start epdg test service");
        Intent i = new Intent(mContext, CdsEpdgService.class);
        mContext.startService(i);
    }

    private static void runNetworkRequest() {
        mConnMgr.requestNetwork(mNetworkRequests[mSelectApnPos].networkRequest,
                mNetworkRequests[mSelectApnPos].networkCallback, MAX_CONNECTION_TIME);
        mNetworkRequests[mSelectApnPos].setIsRequested(true);
        mOutputScreen.setText("Start:" + mNetworkRequests[mSelectApnPos].networkRequest);
        Log.i(TAG, "runNetworkRequest");
    }

    private static void stopNetworkRequest() {
        Log.i(TAG, "stopNetworkRequest");
        try {
            mNetworkRequests[mSelectApnPos].setIsRequested(false);
            mNetworkRequests[mSelectApnPos].setIsRun(false);
            mConnMgr.unregisterNetworkCallback(mNetworkRequests[mSelectApnPos].networkCallback);
            mRunBtnCmd.setEnabled(true);
            mStopBtnCmd.setEnabled(false);
            mOutputScreen.setText("");
        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();
        }
    }

    private static void updateConnectStatus() {

        StringBuffer output = null;
        Network network = mNetworkRequests[mSelectApnPos].currentNetwork;
        if (network != null) {
            NetworkInfo nwInfo = mConnMgr.getNetworkInfo(network);
            LinkProperties link = mConnMgr.getLinkProperties(network);
            NetworkCapabilities cap = mConnMgr.getNetworkCapabilities(network);
            Log.i(TAG, "updateConnectStatus:" + nwInfo);
            if (nwInfo != null) {
                output = new StringBuffer(nwInfo + "\r\n" + link + "\r\n" + cap);
            }
        }

        if (output != null) {
            mOutputScreen.setText(output.toString());
        }
    }

    private static Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_MSG_INFO:
                    String info = (String) msg.obj;
                    if (info != null) {
                        mOutputScreen.setText(info);
                    }
                    mToast.setText(info);
                    mToast.show();
                    updateConnectButton();
                    break;
                default:
                    break;
            }
        }
    };

    private static void runMmsTest() {

        new Thread(new Runnable() {

            @Override
            @SuppressWarnings("illegalcatch")
            public void run() {

                try {
                    final String mmsAddress = "mms.msg.eng.t-mobile.com";
                    final String mmsUrl = "http://mms.msg.eng.t-mobile.com/mms/wapenc";
                    java.util.logging.Logger.getLogger("org.apache.http.wire")
                            .setLevel(java.util.logging.Level.ALL);
                    java.util.logging.Logger.getLogger("org.apache.http.headers")
                            .setLevel(java.util.logging.Level.ALL);

                    // Create and initialize HTTP parameters
                    HttpParams params = new BasicHttpParams();
                    ConnManagerParams.setMaxTotalConnections(params, 10);
                    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                    HttpProtocolParams.setContentCharset(params, "UTF-8");

                    // Create and initialize scheme registry
                    SchemeRegistry schemeRegistry = new SchemeRegistry();
                    schemeRegistry.register(new Scheme("http",
                                PlainSocketFactory.getSocketFactory(), 80));

                    schemeRegistry.register(new Scheme("https",
                                SSLSocketFactory.getSocketFactory(), 443));

                    // Create an HttpClient with the ThreadSafeClientConnManager.
                    ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
                                                    schemeRegistry);
                    DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
                    HttpRequest request = createHttpRequest(mmsUrl);

                    HttpResponse response = httpClient.execute(
                                new HttpHost(mmsAddress, 80, "http"), request);

                    Log.i(TAG, "StatusCode:" + response.getStatusLine().getStatusCode());
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        mToast.setText("http is ok");
                    } else {
                        mToast.setText("http status:" + response.getStatusLine().getStatusCode());
                    }
                    mToast.show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static HttpRequest createHttpRequest(String host) {
        HttpRequest req = new HttpPost(host);
        req.setHeader("Accept",
                "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic");
        req.setHeader("x-wap-profile",
                "http://www-ccpp.tcl-ta.com/files/ALCATEL_ONE_TOUCH_5020T.xml");
        req.setHeader("Accept-Language", "zh-TW, en-US");
        req.setHeader("User-Agent", "ALCATEL_ONE_TOUCH_5020T-MMS/2.0 3gpp-gba");

        String raw = "8c839846545850705f364d354d7444008d9295839181";
        byte[] pdu = HexDump.hexStringToByteArray(raw);
        ByteArrayEntity entity = new ByteArrayEntity(pdu);
        entity.setContentType("application/vnd.wap.mms-message");
        ((HttpEntityEnclosingRequest) req).setEntity(entity);

        return req;
    }

    /**
     * Creates an IPv6 ping socket and sets a receive timeout of 100ms.
     */
    private FileDescriptor createPingSocket(boolean isIpv6) {
        FileDescriptor s = null;

        try {
            if (isIpv6) {
                s = Os.socket(AF_INET, SOCK_DGRAM, IPPROTO_ICMP);
            } else {
                s = Os.socket(AF_INET6, SOCK_DGRAM, IPPROTO_ICMPV6);
            }
            Os.setsockoptTimeval(s, SOL_SOCKET, SO_RCVTIMEO, StructTimeval.fromMillis(1000 * 30));
        } catch (ErrnoException e) {
            e.printStackTrace();
        }

        return s;
    }

}
