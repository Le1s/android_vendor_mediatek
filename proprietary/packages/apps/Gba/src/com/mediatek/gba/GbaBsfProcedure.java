package com.mediatek.gba;

import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.mediatek.gba.auth.AkaDigestAuth;
import com.mediatek.gba.auth.KeyCalculator;

import com.mediatek.gba.element.AkaResponse;
import com.mediatek.gba.element.GbaBsfResponse;
import com.mediatek.gba.element.NafId;
import com.mediatek.gba.element.Nonce;
import com.mediatek.gba.element.SresResponse;
import com.mediatek.gba.header.WwwAuthHeader;
import com.mediatek.gba.telephony.TelephonyUtils;


import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AUTH;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * implementation for GbaBsfProcedure.
 *
 * @hide
 */
public class GbaBsfProcedure {
    private static final String TAG = "GbaBsfProcedure";
    private static final int MAX_BSF_SESSION_TIMES = 2;
    private static final int SOCKET_OPERATION_TIMEOUT = 30 * 1000;

    private static final String USER_AGENT = "Bootstrapping Client Agent ";
    private static final String USER_AGENT_GBA_ME   = " 3gpp-gba";
    private static final String USER_AGENT_GBA_UICC = " 3gpp-gba-uicc";
    private static final String USER_AGENT_TMPI_INDICATOR = "3gpp-gba-tmpi";
    private static final String ACCEPT_HDR = "Accept";
    private static final String X_IMEI_HEADER_NAME = "X-TMUS-IMEI";

    private final static String AUTHORIZATION_HEADER_VALUE_INITIAL = ""
            + "Digest username=\"%s\", realm=\"%s\", nonce=\"\", uri=\"/\", response=\"\"";

    private final static String ALL_NAMESPACES = "*";
    private final static String BOOTSTRAPPING_INFO_NODE = "BootstrappingInfo";
    private final static String BTID_NODE = "btid";
    private final static String LIFETIME_NODE = "lifetime";

    private static final String DEFAULT_BSF_PREFIX = "bsf.";
    private static final String DEFAULT_BSF_POSTFIX = ".pub.3gppnetwork.org";

    private static final String IMPI_DOMAIN = "@ims.";
    private static final String IMPI_POSTFIX = ".3gppnetwork.org";

    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";

    private URI mBsfUri = null;

    private int mGbaType;
    private long mSubId;
    private Context mContext;
    private String mUserName;
    private String mBsfHost;
    private String mImpi;

    /**
    * Bootstarp procedure.
    * @param gbaType the type of GBA mode.
    * @param cardType the type of card.
    * @param context the application context.
    *
    */
    protected GbaBsfProcedure(int gbaType, long subId, Context context) {
        mContext = context;
        mGbaType = gbaType;
        mSubId = subId;

        Log.i(TAG, "GbaBsfProcedure: gbaType:" + gbaType + " subId:" + subId);

        java.util.logging.Logger.getLogger("org.apache.http.wire")
                .setLevel(java.util.logging.Level.ALL);

        try {
            mImpi = TelephonyUtils.getPhoneImpi(subId);
            Log.i(TAG, "mImpi:" + mImpi);

            if (mImpi != null) {
                mUserName = mImpi;
            } else {
                mUserName = getLocalImpi(subId);
            }

            mBsfHost = resolveBsfAddress(mImpi, subId);

            if (mBsfHost != null) {
                Log.i(TAG, "use ssl:" + context.getResources().getBoolean(
                            R.bool.use_tls_transport));

                if (isOp01()) {
                    mBsfUri = new URI(HTTP_SCHEME, null, mBsfHost, 8080, "/", null, null);
                } else {
                    if (context.getResources().getBoolean(R.bool.use_tls_transport)) {
                        mBsfUri = new URI(HTTPS_SCHEME, mBsfHost, "/", null);
                    } else {
                        mBsfUri = new URI(HTTP_SCHEME, mBsfHost, "/", null);
                    }
                }
            }

            //CMMB: http://bsf.bj.mbbms.chinamobile.com:18600
            GbaDebugParam gbaDebugParam = GbaDebugParam.getInstance();
            String testUrl = gbaDebugParam.getBsfServerUrl();
            if (testUrl != null && testUrl.length() > 0) {
                mBsfUri = new URI(testUrl);
                Log.i(TAG, "Connect with testUrl:" + mBsfUri);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    protected NafSessionKey perform(NafId nafId) {
        NafSessionKey nafSessionKey = null;
        int runBsfRequest = 0;
        boolean isFirstRequest = true;
        GbaBsfResponse firstBsfResponse = null;
        GbaBsfResponse secondBsfResponse = null;
        WwwAuthHeader firstWwwAutHeader = null;
        int cardType = GbaConstant.GBA_CARD_UNKNOWN;

        if (mBsfUri == null) {
            Log.e(TAG, "Error No BSF URI");
            return null;
        }

        if (mGbaType == GbaConstant.GBA_NONE) {
            Log.e(TAG, "mGbaType is none");
            return null;
        }

        cardType = TelephonyUtils.getIccCardType(mSubId);

        if (cardType == GbaConstant.GBA_CARD_UNKNOWN) {
            Log.e(TAG, "cardType is unknown");
            return null;
        }

        do {
            runBsfRequest++;
            Log.i(TAG, "run GBA procedure with BSF:" + runBsfRequest);

            if (isFirstRequest) {
                firstBsfResponse = sendFirstRequest();

                if (firstBsfResponse != null && firstBsfResponse.getStatusCode() ==
                    HttpStatus.SC_UNAUTHORIZED) {
                    firstWwwAutHeader = firstBsfResponse.getWwwAuthenticateHeader();
                } else {
                    isFirstRequest = true;
                    Log.e(TAG, "Fail to get the first response from BSF");
                    continue;
                }
            }

            Log.i(TAG, "Process the GBA first response");

            Nonce nonce = null;
            AkaResponse akaResponse = null;
            SresResponse sresResponse = null;

            try {
                if (GbaConstant.GBA_U == mGbaType || GbaConstant.GBA_ME == mGbaType) {
                    nonce = Nonce.decodeNonce(firstWwwAutHeader.getNonce());
                    Log.d(TAG, "nonce dump:" + nonce);
                    akaResponse = runAkaAlgorithm(nonce);

                    isFirstRequest = false;
                    String auts = "";
                    String passwd = "";

                    Log.d(TAG, "response dump:" + akaResponse);

                    if (akaResponse != null && akaResponse.getAuts() != null) {
                        auts = new String(Base64.encode(akaResponse.getAuts(), Base64.DEFAULT));
                    }

                    if (auts == null || auts.length() == 0) {
                        if (akaResponse != null) {
                            passwd = TelephonyUtils.bytesToHex(akaResponse.getRes());
                        }
                    }

                    secondBsfResponse = sendSecondRequest(passwd, auts, firstWwwAutHeader,
                                            runBsfRequest);
                } else {
                    sresResponse = SresResponse.getSresResponse(
                            firstWwwAutHeader.getNonce(), mSubId);

                    if (sresResponse == null) {
                        isFirstRequest = true;
                        Log.e(TAG, "Fail to get 2G SIM response. Try again new");
                        continue;
                    }

                    isFirstRequest = false;
                    String passwd = "";
                    final KeyCalculator keyCalculator = KeyCalculator.getInstance();

                    try {
                        byte[] res = keyCalculator.calculateRes(sresResponse.getKc(),
                                sresResponse.getRand(), sresResponse.getSres());
                        passwd = TelephonyUtils.bytesToHex(res);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    secondBsfResponse = sendSecondRequest(passwd, "", firstWwwAutHeader,
                                                            runBsfRequest);
                }
            } catch (NullPointerException ee) {
                ee.printStackTrace();
            }

            if (secondBsfResponse.getStatusCode() == HttpStatus.SC_OK) {
                nafSessionKey = parseBsfXmlFromResponse(secondBsfResponse);

                if (mGbaType == GbaConstant.GBA_U) { //stores NAF key parameters into UICC
                    String dummy = "";
                    storeNafKeyParameters(cardType, nonce.getRand(),
                                nafSessionKey.getBtid(),
                                nafSessionKey.getKeylifetime(), mSubId);
                    nafSessionKey.setKey(TelephonyUtils.calculateNafExternalKey(
                                nafId.getNafIdBin(), null, mSubId));
                    nafSessionKey.setNafKeyName(GbaConstant.GBA_KS_EXT_NAF);
                } else if (cardType == GbaConstant.GBA_CARD_USIM) {
                    calculateNafKey(nafId, nonce.getRand(), nafSessionKey, akaResponse);
                } else if (cardType == GbaConstant.GBA_CARD_SIM) {
                    calculateNafKeyBySim(nafId, nafSessionKey, sresResponse);
                }

                break;
            } else if (secondBsfResponse.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                Log.e(TAG, "Response is 401. Retry");
                firstWwwAutHeader = secondBsfResponse.getWwwAuthenticateHeader();
            }
        } while (runBsfRequest < MAX_BSF_SESSION_TIMES);

        Log.i(TAG, "[Done]run GBA procedure with BSF");

        return nafSessionKey;
    }

    private AkaResponse runAkaAlgorithm(Nonce nonce) {
        byte[] rand = nonce.getRand();
        byte[] autn = nonce.getAutn();
        byte[] res  = null;
        AkaResponse response = null;

        if (mGbaType == GbaConstant.GBA_ME) {
            res = TelephonyUtils.calculateAkaAuthAndRes(rand, autn, mSubId);
        } else if (mGbaType == GbaConstant.GBA_U) {
            res = TelephonyUtils.calculateGbaAuthAndRes(rand, autn, mSubId);
        } else {
            Log.e(TAG, "Non-support type:" + mGbaType);
        }

        if (res != null) {
            response = new AkaResponse(res, mGbaType); //Todo: configure auts as emtpy string
        }

        return response;
    }

    private GbaBsfResponse sendFirstRequest() {
        HttpClient httpClient = createHttpClient();
        HttpRequest httpRequest = createHttpRequest(true);
        HttpResponse httpResponse = null;
        GbaBsfResponse bsfResponse = null;

        Log.i(TAG, "Connect to BSF:" + mBsfUri.toString());
        HttpHost httpHost = new HttpHost(mBsfUri.getHost(), mBsfUri.getPort(),
                mBsfUri.getScheme());

        try {
            httpResponse = httpClient.execute(httpHost, httpRequest);
            bsfResponse = GbaBsfResponse.parseResponse(httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }

        return bsfResponse;
    }

    private GbaBsfResponse sendSecondRequest(String passwd, String auts,
                            WwwAuthHeader header, int runCount) {
        HttpClient httpClient = createHttpClient();
        HttpRequest httpRequest = createHttpRequest(false);
        HttpResponse httpResponse = null;
        GbaBsfResponse bsfResponse = null;

        Log.i(TAG, "passwd:" + passwd + "  auts:" + auts);

        String nc = String.format("0000000%d", runCount);

        AkaDigestAuth akaDigestAuth = new AkaDigestAuth(header, mUserName, auts, passwd,
                                            mBsfUri.getPath(), nc);
        akaDigestAuth.calculateRequestDigest();

        httpRequest.setHeader(AUTH.WWW_AUTH_RESP, akaDigestAuth.createAuthorHeaderValue());

        HttpHost   httpHost = new HttpHost(mBsfUri.getHost(), mBsfUri.getPort(),
                                            mBsfUri.getScheme());

        try {
            httpResponse = httpClient.execute(httpHost, httpRequest);
            bsfResponse = GbaBsfResponse.parseResponse(httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }

        return bsfResponse;
    }

    private HttpRequest createHttpRequest(boolean isFirstRequest) {

        HttpRequest req = new HttpGet(mBsfUri);

        if (mBsfUri.getPort() != -1) {
            req.setHeader(HTTP.TARGET_HOST, mBsfUri.getHost() + ":" + mBsfUri.getPort());
        }

        //req.setHeader(HTTP.TARGET_HOST, "bsf.msg.lab.t-mobile.com:443");
        req.setHeader(AUTH.WWW_AUTH_RESP, getAuthorHeaderValue(isFirstRequest));
        req.setHeader(ACCEPT_HDR, "*/*");

        if (isOp08()) {
            req.setHeader(X_IMEI_HEADER_NAME, TelephonyUtils.getImei(mSubId));
        }

        final Date now = new Date();
        req.setHeader("Date", DateUtils.formatDate(now));
        //req.setHeader("Content-Length", "0");

        return req;
    }

    private String getAuthorHeaderValue(boolean isFristRequest) {
        String value = null;

        if (isFristRequest) {
            String realm = mBsfUri.getHost();
            realm = mUserName.substring(mUserName.indexOf("@") + 1);
            value = String.format(AUTHORIZATION_HEADER_VALUE_INITIAL, mUserName, realm,
                                mBsfUri.getPath());
        }

        return value;
    }

    private HttpClient createHttpClient() {
        boolean isTrustAll = true;
        HttpClient client = null;
        HttpParams params = new BasicHttpParams();

        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, SOCKET_OPERATION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT * 2);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpConnectionParams.setSoSndTimeout(params, SOCKET_OPERATION_TIMEOUT * 2);

        HttpClientParams.setRedirecting(params, false);
        HttpProtocolParams.setUseExpectContinue(params, true);

        if (GbaConstant.GBA_ME == mGbaType) {
            HttpProtocolParams.setUserAgent(params, USER_AGENT
                                + Build.MANUFACTURER + USER_AGENT_GBA_ME);
        } else if (GbaConstant.GBA_U == mGbaType) {
            HttpProtocolParams.setUserAgent(params, USER_AGENT
                                + Build.MANUFACTURER + USER_AGENT_GBA_UICC);
        } else {
            HttpProtocolParams.setUserAgent(params, USER_AGENT);
        }


        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http",
                        PlainSocketFactory.getSocketFactory(), 80));

        isTrustAll = GbaDebugParam.getInstance().getEnableGbaTrustAll();
        if (isTrustAll) {
            Log.i(TAG, "Trust all is enabled");
        }

        if (isTrustAll) {
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                SSLSocketFactory sf = new TrustAllSSLSocketFactory(trustStore);
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                schemeRegistry.register(new Scheme("https", sf, 443));
            } catch (KeyStoreException ke) {
                ke.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (NoSuchAlgorithmException ne) {
                ne.printStackTrace();
            } catch (CertificateException ce) {
                ce.printStackTrace();
            } catch (KeyManagementException kme) {
                kme.printStackTrace();
            } catch (UnrecoverableKeyException ue) {
                ue.printStackTrace();
            }
        } else {
            schemeRegistry.register(new Scheme("https",
                        SSLSocketFactory.getSocketFactory(), 443));
        }

        ClientConnectionManager connMgr =
            new ThreadSafeClientConnManager(params, schemeRegistry);

        DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(
            1, false);

        client = new DefaultHttpClient(connMgr, params);
        ((DefaultHttpClient) client).setHttpRequestRetryHandler(retryHandler);
        return client;
    }

    private NafSessionKey parseBsfXmlFromResponse(GbaBsfResponse bsfResponse) {
        NafSessionKey nafSessionKey = new NafSessionKey();
        String xmlFromResponse = bsfResponse.getXmlContent();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                                                        xmlFromResponse.getBytes());
            Document document = documentBuilder.parse(byteArrayInputStream);
            NodeList bootstrappingInfoNodeList = document.getElementsByTagNameNS(ALL_NAMESPACES,
                                                BOOTSTRAPPING_INFO_NODE);

            if (bootstrappingInfoNodeList == null || bootstrappingInfoNodeList.getLength() != 1) {
                Log.e(TAG, "Error content in " + BOOTSTRAPPING_INFO_NODE);
                return null;
            }

            Node bootstrappingInfoNode = bootstrappingInfoNodeList.item(0);

            if (bootstrappingInfoNode.getNodeType() == Node.ELEMENT_NODE) {
                Element bootstrappingInfoElement = (Element) bootstrappingInfoNode;
                nafSessionKey.setBtid(parseBtidNode(bootstrappingInfoElement));;
                nafSessionKey.setKeylifetime(parseKeylifetimeNode(bootstrappingInfoElement));
            } else {
                Log.e(TAG, "Error node in " + bootstrappingInfoNode.getNodeType());
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException ee) {
            ee.printStackTrace();
        } catch (SAXException eee) {
            eee.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }

        return nafSessionKey;
    }

    private String parseBtidNode(Element bootstrappingInfoElement) {
        NodeList btidNodeList = bootstrappingInfoElement.getElementsByTagNameNS(ALL_NAMESPACES,
                                                                                BTID_NODE);

        if (btidNodeList == null || btidNodeList.getLength() != 1) {
            Log.e(TAG, "Something wrong with parsing " + BTID_NODE);
            return null;
        }

        Node btidNode = btidNodeList.item(0);

        if (btidNode == null) {
            Log.e(TAG, "Something is null with parsing " + BTID_NODE);
            return null;
        }

        return btidNode.getFirstChild().getNodeValue();
    }

    private String parseKeylifetimeNode(Element bootstrappingInfoElement) {
        NodeList keylifetimeNodeList =
            bootstrappingInfoElement.getElementsByTagNameNS(ALL_NAMESPACES, LIFETIME_NODE);

        if (keylifetimeNodeList == null || keylifetimeNodeList.getLength() != 1) {
            Log.e(TAG, "Something wrong with parsing " + LIFETIME_NODE);
            return null;
        }

        Node keylifetimeNode = keylifetimeNodeList.item(0);

        if (keylifetimeNode == null) {
            Log.e(TAG, "Something is null with parsing " + LIFETIME_NODE);
            return null;
        }

        return keylifetimeNode.getFirstChild().getNodeValue();
    }

    private void storeNafKeyParameters(int cardType, byte[] rand,
            String btid, String keyLifetime, long subId) {
        Log.i(TAG, "storeNafKeyParameters()btid=" + btid + ";keyLifetime=" + keyLifetime);
        TelephonyUtils.setGbaBootstrappingParameters(
                cardType, rand, btid, keyLifetime, subId);
    }

    private void calculateNafKeyBySim(NafId nafId, NafSessionKey nafSessionKey,
                                        SresResponse sresResponse) {
        if (nafSessionKey == null) {
            throw new IllegalArgumentException("nafSessionKey must be not null.");
        }

        if (sresResponse == null) {
            throw new IllegalStateException(
                "Aka response is null. Please calculate if before using.");
        }

        final KeyCalculator keyCalculator = KeyCalculator.getInstance();

        try {
            //@User mUserName instead of impi
            final byte[] ks = keyCalculator.calculateKsBySres(
                    sresResponse.getKc(),  sresResponse.getRand(),
                    sresResponse.getKsInput(), sresResponse.getSres());
            final byte[] keyKsNaf = keyCalculator.calculateKsNaf(ks, sresResponse.getRand(),
                            mUserName, nafId);

            nafSessionKey.setKey(keyKsNaf);
            nafSessionKey.setNafKeyName(GbaConstant.GBA_KS_NAF);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void calculateNafKey(NafId nafId, byte[] rand, NafSessionKey nafSessionKey,
                            AkaResponse akaResponse) {

        if (nafSessionKey == null) {
            throw new IllegalArgumentException("nafSessionKey must be not null.");
        }

        if (akaResponse == null) {
            throw new IllegalStateException(
                "Aka response is null. Please calculate if before using.");
        }


        final byte[] ck = akaResponse.getCk();
        final byte[] ik = akaResponse.getIk();

        final KeyCalculator keyCalculator = KeyCalculator.getInstance();

        try {
                //@User mUserName instead of impi
                final byte[] ks = keyCalculator.calculateKsByGbaMe(ck, ik);
                final byte[] keyKsNaf = keyCalculator.calculateKsNaf(ks, rand, mUserName, nafId);

                nafSessionKey.setKey(keyKsNaf);
                nafSessionKey.setNafKeyName(GbaConstant.GBA_KS_NAF);
        } catch (NullPointerException e) {
                e.printStackTrace();
        } catch (IOException ioe) {
                ioe.printStackTrace();
        }
        Log.i(TAG, "[done]nafSessionKey=" + nafSessionKey);
    }

    //3GPP TS 23.003
    //16.2 BSF address
    //
    private String resolveBsfAddress(String impi, long subId) {
        String bsfHost = null;

        try {
            if (impi != null) { //ISIM support
                int offset = impi.indexOf("@");

                if (offset != -1) {
                    String domain = impi.substring(offset + 1);
                    bsfHost = DEFAULT_BSF_PREFIX + domain;
                }
            } else { //IMSI support
                String operator = TelephonyUtils.getSimOperator(mContext, subId);

                if (operator != null) {
                    String mcc = operator.substring(0, 3);
                    String mnc = operator.substring(3);

                    if (mnc.length() == 2) {
                        mnc = "0" + mnc;
                    }
                    bsfHost = DEFAULT_BSF_PREFIX + "mnc" + mnc + ".mcc"
                                    + mcc + DEFAULT_BSF_POSTFIX;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException ee) {
            ee.printStackTrace();
        }

        return bsfHost;
    }

    private String getLocalImpi(long subId) {
        String newImpi = "";
        String operator = TelephonyUtils.getSimOperator(mContext, subId);
        String imsi = TelephonyUtils.getImsi(subId);

        if (operator != null) {
            String mcc = operator.substring(0, 3);
            String mnc = operator.substring(3);

            if (mnc.length() == 2) {
                mnc = "0" + mnc;
            }

            newImpi = imsi + IMPI_DOMAIN + "mnc" + mnc + ".mcc" + mcc + IMPI_POSTFIX;
        }

        return newImpi;
    }

    private boolean isOp01() {
        String[] op01PlmnTable = {
            "46000", "46002", "46007", "46008", "46011",
        };

        String imsi = TelephonyUtils.getImsi(mSubId);

        if (imsi != null) {
            for (String plmn : op01PlmnTable) {
                if (plmn.equals(imsi.substring(0, 5))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isOp08() {
        String[] op08PlmnTable = {
            "310800", "310580", "310230", "310220", "310210", "310660", "310250", "310310",
            "310270", "310260", "310200",
        };

        String imsi = TelephonyUtils.getImsi(mSubId);

        if (imsi != null) {
            for (String plmn : op08PlmnTable) {
                if (plmn.equals(imsi.substring(0, 6))) {
                    return true;
                }
            }
        }
    }

    /**
      * The SSLSocketFactory class for trust all.
      *
      */
    private class TrustAllSSLSocketFactory extends SSLSocketFactory {
        SSLContext mSslContext = SSLContext.getInstance("TLS");

        TrustAllSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
                KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);
            TrustManager[] trustManagers = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                             return null;
                        }

                        public void checkClientTrusted(
                            X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                            X509Certificate[] certs, String authType) {
                        }
                    }
                };
            mSslContext.init(null, trustManagers, null);

        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            return mSslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return mSslContext.getSocketFactory().createSocket();
        }
    }
}
