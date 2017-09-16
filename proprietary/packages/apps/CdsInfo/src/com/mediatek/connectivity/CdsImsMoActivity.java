package com.mediatek.connectivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.telephony.SubInfoRecord;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ims.ImsConfig;
import com.android.ims.ImsConfig.ConfigConstants;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;

import com.android.ims.mo.ImsIcsi;
import com.android.ims.mo.ImsLboPcscf;
import com.android.ims.mo.ImsPhoneCtx;

import com.mediatek.gba.GbaCredentials;
import com.mediatek.gba.GbaManager;
import com.mediatek.gba.NafSessionKey;

import com.mediatek.ims.ImsConfigStub;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.IOException;
import java.security.KeyStore;
import java.util.List;


/**
  * Class for handle IMS MO and GBA procedures.
  *
  *
  */
public class CdsImsMoActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "CDSINFO/CdsImsMoActivity";
    private Context mContext;
    private TextView mImsMoInfo = null;
    private Button   mSetButton = null;
    private Button   mGetButton = null;
    private Button   mTestButton = null;
    private Button   mRunGbaButton = null;
    private boolean  mToggle = true;
    private Toast mToast;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.cds_ims_mo);

        mContext = this.getBaseContext();
        mImsMoInfo = (TextView) findViewById(R.id.ims_mo_info);

        mGetButton = (Button) findViewById(R.id.get_btn);
        mGetButton.setOnClickListener(this);
        mTestButton = (Button) findViewById(R.id.test_btn);
        mTestButton.setOnClickListener(this);
        mRunGbaButton = (Button) findViewById(R.id.run_gba_btn);
        mRunGbaButton.setOnClickListener(this);

        mToast = Toast.makeText(this, null, Toast.LENGTH_SHORT);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick");

        switch (v.getId()) {
        case R.id.get_btn:
            try {
                getImsMoInfo();
            } catch (ImsException e) {
                e.printStackTrace();
            }
            break;
        case R.id.test_btn:
            try {
                testImsMo();
            } catch (ImsException e) {
                e.printStackTrace();
            }
            break;
        case R.id.run_gba_btn:
            int runType = SystemProperties.getInt("gba.run", -1);

            if (runType != -1) {
                runGbaProcedure();
            } else {
                runHttpClient();
            }
            break;
        default:
            break;
        }
    }

    private void showToast(String info) {
        mToast.setText(info);
        mToast.show();
    }

    private void testImsMo() throws ImsException {
        ImsConfigStub imsConfig = getImsConfigStub();


        if (imsConfig == null) {
            Log.e(TAG, "Null imsConfig");
            return;
        }

        StringBuilder builder = new StringBuilder();

        int ve = imsConfig.getMasterValue(ConfigConstants.IMS_MO_VOICE_E);
        int uv = imsConfig.getMasterValue(ConfigConstants.IMS_MO_VOICE_U);
        int mm = imsConfig.getMasterValue(ConfigConstants.IMS_MO_MOBILITY);

        builder.append("[before]Voice_Domain_Preference_E_UTRAN:")
            .append(ve).append("\r\n");
        builder.append("[before]Voice_Domain_Preference_UTRAN:")
            .append(uv).append("\r\n");
        builder.append("[before]Mobility_Management_IMS_Voice_Termination:")
            .append(mm).append("\r\n");

        Log.d(TAG, "testImsMo:" + mToggle);

        if (mToggle) {
            imsConfig.setProvisionedValue(ConfigConstants.SIP_T1_TIMER, 1000000);
            imsConfig.setProvisionedValue(ConfigConstants.SIP_T2_TIMER, 1000000);
            imsConfig.setProvisionedValue(ConfigConstants.SIP_TF_TIMER, 1000000);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_RESOURCE, 1);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_REG_BASE, 10);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_REG_MAX, 10);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_SMS, 1);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_KEEPALIVE, 1);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_VOICE_E, 3);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_VOICE_U, 3);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_MOBILITY, 1);

            testIcsList(true);
            testImsPhoneCtx(true);

        } else {
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_VOICE_E, 1);
            imsConfig.setProvisionedValue(ConfigConstants.SIP_T1_TIMER, 600000);
            imsConfig.setProvisionedValue(ConfigConstants.SIP_T2_TIMER, 600000);
            imsConfig.setProvisionedValue(ConfigConstants.SIP_TF_TIMER, 600000);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_RESOURCE, 0);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_REG_BASE, 30);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_REG_MAX, 30);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_SMS, 0);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_KEEPALIVE, 0);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_VOICE_E, 1);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_VOICE_U, 1);
            imsConfig.setProvisionedValue(ConfigConstants.IMS_MO_MOBILITY, 0);

            testIcsList(false);
            testImsPhoneCtx(false);
        }

        mToggle = !mToggle;

        getImsMoInfo();
    }

    private void runGbaProcedure() {
        final String nafAddress = "mms.msg.eng.t-mobile.com";

        final GbaManager gbaManager = GbaManager.getDefaultGbaManager(mContext);

        if (gbaManager == null) {
            Log.e(TAG, "gbaManager is null");
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("GBA support type:" + gbaManager.getGbaSupported() + "\r\n");

        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    Log.d(TAG, "Before runGbaProcedure ");

                    synchronized (CdsImsMoActivity.this) {
                        byte[] uaId = new byte[] {(byte) 0x01, (byte) 0x00,
                                                  (byte) 0x01, (byte) 0x00, (byte) 0x2F
                                                 };
                        String gbaStr0 = System.getProperty("gba_cipher_byte_0", "");
                        String gbaStr1 = System.getProperty("gba_cipher_byte_1", "");

                        if (gbaStr0.length() > 0 && gbaStr1.length() > 0) {
                            try {
                                byte byte0 = (byte) Integer.parseInt(gbaStr0);
                                byte byte1 = (byte) Integer.parseInt(gbaStr1);
                                uaId[3] = byte0;
                                uaId[4] = byte1;
                                Log.i(TAG, "New UA ID:" + byte0 + ":" + byte1);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }

                        NafSessionKey nafSessionKey = gbaManager.runGbaAuthentication(
                                                          nafAddress, uaId, false);

                        if (nafSessionKey != null) {
                            Log.i(TAG, "GBA Session Key:" + nafSessionKey);
                        }
                    }

                    Log.d(TAG, "After runGbaProcedure ");
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        } .start();

    }

    private void runHttpClient() {
        Log.i(TAG, "run http client");
        new Thread() {

            @Override
            @SuppressWarnings("illegalcatch")
            public void run() {
                super.run();

                try {
                    final String nafAddress = "mms.msg.eng.t-mobile.com";
                    final String nafUrl = "https://mms.msg.eng.t-mobile.com/mms/wapenc";

                    java.util.logging.Logger.getLogger("org.apache.http.wire")
                            .setLevel(java.util.logging.Level.ALL);
                    java.util.logging.Logger.getLogger("org.apache.http.headers")
                            .setLevel(java.util.logging.Level.ALL);

                    // Create and initialize HTTP parameters
                    HttpParams params = new BasicHttpParams();
                    ConnManagerParams.setMaxTotalConnections(params, 10);
                    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                    HttpProtocolParams.setContentCharset(params, "UTF-8");

                    CredentialsProvider credProvider = new BasicCredentialsProvider();
                    credProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,
                                    AuthScope.ANY_PORT),
                                    new GbaCredentials(mContext, nafAddress));

                    // Create and initialize scheme registry
                    SchemeRegistry schemeRegistry = new SchemeRegistry();
                    schemeRegistry.register(new Scheme("http",
                                PlainSocketFactory.getSocketFactory(), 80));

                    try {
                        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        trustStore.load(null, null);
                        SSLSocketFactory sf = new CdsSSLSocketFactory(trustStore);
                        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                        schemeRegistry.register(new Scheme("https", sf, 443));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Create an HttpClient with the ThreadSafeClientConnManager.
                    ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
                                                    schemeRegistry);
                    DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
                    httpClient.setCredentialsProvider(credProvider);

                    HttpRequest request = createHttpRequest(nafUrl);

                    HttpResponse response = httpClient.execute(
                                new HttpHost(nafAddress, 443, "https"), request);

                    Log.i(TAG, "StatusCode:" + response.getStatusLine().getStatusCode());

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        showToast("http is ok");
                    } else {
                        showToast("http status:" + response.getStatusLine().getStatusCode());
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        } .start();

    }

    private HttpRequest createHttpRequest(String host) {

        HttpRequest req = new HttpPost(host);

        req.setHeader("Accept",
                "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic");
        req.setHeader("x-wap-profile",
                "http://218.249.47.94/Xianghe/MTK_Athens15_UAProfile.xml");
        req.setHeader("Accept-Language", "zh-TW, en-US");
        req.setHeader("User-Agent", "MTK-MMS/2.0 3gpp-gba");

        String raw = "8c839846545850705f364d354d7444008d9295839181";
        byte[] pdu = hexStringToByteArray(raw);
        ByteArrayEntity entity = new ByteArrayEntity(pdu);
        entity.setContentType("application/vnd.wap.mms-message");
        ((HttpEntityEnclosingRequest) req).setEntity(entity);

        return req;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    private void testIcsList(boolean bValue) throws ImsException {
        ImsConfigStub imsConfig = getImsConfigStub();

        if (imsConfig == null) {
            Log.e(TAG, "Null imsConfig");
            return;
        }

        String icsi = "";
        int count = 4;
        int i = 0;

        ImsIcsi[] objs = new ImsIcsi[count];

        for (i = 0; i < count; i++) {
            if (bValue) {
                icsi = "urn:urn-7:3gpp-service.ims.icsi.mmtel";
            } else {
                icsi = "ICSI" + "*" + i;
            }

            objs[i] = new ImsIcsi(icsi, bValue);
        }

        imsConfig.setProvisionedIcsiValue(objs);
    }

    private void getImsMoInfo() throws ImsException {
        int i = 0;
        ImsConfigStub imsConfig = getImsConfigStub();

        if (imsConfig == null) {
            Log.e(TAG, "Null imsConfig");
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("P-CSCF_Address:").append(
            imsConfig.getMasterStringValue(ConfigConstants.IMS_MO_PCSCF)).append("\r\n");
        builder.append("Timer_T1:").append(
            imsConfig.getMasterValue(ConfigConstants.SIP_T1_TIMER)).append("\r\n");
        builder.append("Timer_T2:").append(
            imsConfig.getMasterValue(ConfigConstants.SIP_T2_TIMER)).append("\r\n");
        builder.append("Timer_T2:").append(
            imsConfig.getMasterValue(ConfigConstants.SIP_TF_TIMER)).append("\r\n");
        builder.append("Private_user_identity:").append(
            imsConfig.getMasterStringValue(ConfigConstants.IMS_MO_IMPI)).append("\r\n");
        String[] impus = imsConfig.getMasterStringArrayValue(ConfigConstants.IMS_MO_IMPU);

        if (impus == null) {
            builder.append("impus:\r\n");
        } else {
            for (i = 0; i < impus.length; i++) {
                builder.append("Public_user_identity[" + i + "]:")
                        .append(impus[i]).append("\r\n");
            }
        }

        builder.append("Home_network_domain_name:").append(
            imsConfig.getMasterStringValue(ConfigConstants.IMS_MO_PCSCF)).append("\r\n");

        ImsIcsi[] icsis = imsConfig.getMasterIcsiValue();
        builder.append(getObjectsInfo("ICSI", icsis));

        ImsLboPcscf[] pcscfs = imsConfig.getMasterLboPcscfValue();
        builder.append(getObjectsInfo("LBO_P-CSCF", pcscfs));

        builder.append("Resource_Allocation_Mode:").append(
            imsConfig.getMasterValue(ConfigConstants.IMS_MO_RESOURCE)).append("\r\n");
        builder.append("Voice_Domain_Preference_E_UTRAN:").append(
            imsConfig.getMasterValue(ConfigConstants.IMS_MO_VOICE_E)).append("\r\n");
        builder.append("SMS_Over_IP_Networks_Indication:").append(
            imsConfig.getMasterValue(ConfigConstants.IMS_MO_SMS)).append("\r\n");
        builder.append("Keep_Alive_Enabled:").append(
            imsConfig.getMasterValue(ConfigConstants.IMS_MO_KEEPALIVE)).append("\r\n");
        builder.append("Voice_Domain_Preference_UTRAN:").append(
            imsConfig.getMasterValue(ConfigConstants.IMS_MO_VOICE_U)).append("\r\n");
        builder.append("Mobility_Management_IMS_Voice_Termination:").append(
            imsConfig.getMasterValue(ConfigConstants.IMS_MO_MOBILITY)).append("\r\n");
        builder.append("RegRetryBaseTime:").append(
            imsConfig.getMasterValue(ConfigConstants.IMS_MO_REG_BASE)).append("\r\n");
        builder.append("RegRetryMaxTime:").append(
            imsConfig.getMasterValue(ConfigConstants.IMS_MO_REG_MAX)).append("\r\n");

        ImsPhoneCtx[] phoneCtxs = imsConfig.getMasterImsPhoneCtxValue();
        builder.append(getObjectsInfo("PhoneContext", phoneCtxs));

        mImsMoInfo.setText(builder.toString());
    }

    private String getSingleObjectInfo(String title, Object obj) {
        if (obj == null) {
            return title + ":null\r\n";
        }

        return title + ":" + obj.toString() + "\r\n";
    }

    private String getObjectsInfo(String title, Object[] objs) {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        if (objs == null) {
            return title + ":null\r\n";
        }

        for (Object obj: objs) {
            i++;
            builder.append(title + "-" + i + ":[").append(obj.toString() + "]\r\n");
        }

        return builder.toString();
    }

    private void testImsPhoneCtx(boolean flag) throws ImsException {
        ImsConfigStub imsConfig = getImsConfigStub();

        if (imsConfig == null) {
            Log.e(TAG, "Null imsConfig");
            return;
        }

        ImsPhoneCtx[] phoneCtxArray = imsConfig.getMasterImsPhoneCtxValue();

        if (phoneCtxArray == null) {
            Log.e(TAG, "[PhoneCtx][write] readImsPhoneCtxMo is NULL!!!");
            return;
        }

        ImsPhoneCtx phoneCtx = null;
        String phoneCtxStr = null;
        String [] userID = null;
        int index = 0;
        int pcLength = phoneCtxArray.length;

        Log.i(TAG, "[write]pcLength: " + pcLength);

        for (; index < pcLength; index++) {
            Log.i(TAG, "write PhoneCtx " + index);

            if (flag) {
                phoneCtxStr = "phoneCtx-" + index;
            } else {
                phoneCtxStr = index + "-phoneCtx";
            }

            userID = phoneCtxArray[index].getPhoneCtxIpuis();
            int userIdLength = userID.length;

            for (int j = 0; j < userIdLength; ++j) {
                Log.i(TAG, "write userID " + j);

                if (flag) {
                    userID[j] = index + ":" + j;
                } else {
                    userID[j] = j + "/" + index;
                }
            }

            phoneCtx = new ImsPhoneCtx(phoneCtxStr, userID);
            Log.i(TAG, "phoneCtx:" + phoneCtx);
            phoneCtxArray[index] = phoneCtx;
        }

        imsConfig.setProvisionedPhoneCtxValue(phoneCtxArray);
    }

    private long getSubId(int slotId) {
        List<SubInfoRecord> result = SubscriptionManager.getSubInfoUsingSlotId(slotId);

        if (result != null && result.size() > 0) {
            Log.i(TAG, "result.size " + result.size());
            return result.get(0).subId;
        }

        return -1;
    }

    private ImsConfig getImsConfig() {
        ImsConfig imsConfig = null;
        ImsManager imsManager = ImsManager.getInstance(mContext, getSubId(0));

        try {
            imsConfig = imsManager.getConfigInterface();
        } catch (ImsException e) {
            e.printStackTrace();
        }

        return imsConfig;
    }

    private ImsConfigStub getImsConfigStub() {
        return new ImsConfigStub(mContext);
    }
}
