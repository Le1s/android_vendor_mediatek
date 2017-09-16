package com.mediatek.wifi.sanitytest;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.app.Activity;
import android.app.ActionBar;
import android.widget.Button;
import android.widget.Switch;

import com.android.settings.Settings;
import com.jayway.android.robotium.solo.Solo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WifiTest extends
        ActivityInstrumentationTestCase2<Settings.WifiSettingsActivity> {

    private static String TAG = "WifiTest";

    private static final int SLEEP_TIMES_NUM = 22; // wait 2 min for wifi conectting
    private static final int SLEEP_TIME = 1000;

    private static final String FILE_PATH = "/data/wifi_accounts.xml";
    private static final String DEFAULT_SSID = "mtklab";
    private static final String DEFAULT_PASSWORD = "1234567890";

    private static final boolean REQUEST_WIFI_ON = true;
    private static final boolean REQUEST_WIFI_OFF = false;
    private static int mIsWiFiOff = 1;

    private Solo mSolo;
    private Activity mActivity;

    private WifiManager mWifiManager;
    private String mSSId;
    private String mPassword;

    public WifiTest() {
        super(Settings.WifiSettingsActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        mActivity = getActivity();
        mSolo = new Solo(getInstrumentation(), mActivity);

        assertNotNull(mActivity);
        assertNotNull(mActivity);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void test01_setWiFiOn() throws Exception {
        Log.d(TAG, "test01_setWiFiOn");
        // /M: Turn on WiFi
        setWiFiState(REQUEST_WIFI_ON);

        // /M: Check whether WiFi is enable
        boolean isEnable = getWifiMode();
        assertEquals(isEnable, true);
    }

    public void test02_connectAp() throws Exception {
        String filePath = FILE_PATH;
        Log.d(TAG, "test02_connectAp()");
        parseAPConfig(filePath);
        if (mSSId == null || mPassword == null) {
            mSSId = DEFAULT_SSID;
            mPassword = DEFAULT_PASSWORD;
        }
        Log.d(TAG, "mSSId : " + mSSId + " mPassword : " + mPassword);

        setWiFiState(REQUEST_WIFI_ON);

        // /M: Forget saved AP.
        int savedNetworkId = getSavedNetworkId(mSSId);
        if (savedNetworkId != -1) {
            mWifiManager.forget(savedNetworkId, null);
            mSolo.sleep(SLEEP_TIME * 5);
            Log.d(TAG, "Forget saved AP : " + mSSId);
        }

        scrollTop();
        // /M: Connect test AP
        if (mSolo.searchText(mSSId)) {
            mSolo.clickOnText(mSSId);
            mSolo.sleep(SLEEP_TIME * 5);
            Log.d(TAG, "Click on AP :" + mSSId);
            if (isRightView()) {
                mSolo.enterText(0, mPassword);
                mSolo.sleep(SLEEP_TIME);
                Button connButton = mSolo.getButton(3);
                if (connButton != null) {
                    mSolo.clickOnView(connButton);
                    Log.d(TAG, "Confirm connect AP :" + mSSId);
                }
            }
        }
        mSolo.sleep(SLEEP_TIME);

        // /M: Check whether WiFi is connected
        int step = 0;
        do {
            mSolo.sleep(SLEEP_TIME * 5);
            step++;
        } while (!getWifiConnectedState(mSSId) && step < SLEEP_TIMES_NUM);
        boolean isConnedted = getWifiConnectedState(mSSId);
        assertEquals(isConnedted, true);

        // /M: Forget test AP
        Log.d(TAG, "mIsWiFiOff = " + mIsWiFiOff);
        if (mIsWiFiOff == 0) {
            return;
        }
        savedNetworkId = getSavedNetworkId(mSSId);
        if (savedNetworkId != -1) {
            mWifiManager.forget(savedNetworkId, null);
            mSolo.sleep(SLEEP_TIME * 5);
        }
        mSolo.goBack();
    }

    public void test03_setWiFiOff() throws Exception {
        Log.d(TAG, "test03_setWiFiOff()");
        if (mIsWiFiOff == 0) {
            Log.d(TAG, "Wifi is off, just return");
            return;
        }

        // /M: Turn off WiFi
        setWiFiState(REQUEST_WIFI_OFF);

        // /M: Check whether WiFi is disable
        boolean isEnable = getWifiMode();
        assertEquals(isEnable, false);
    }

    private boolean getWifiMode() {
        mWifiManager = (WifiManager) mActivity
                .getSystemService(Context.WIFI_SERVICE);
        return mWifiManager.isWifiEnabled();
    }

    private void setWiFiState(boolean isOn) {
        int resId = mActivity.getResources().getIdentifier("switch_widget",
                "id", "com.android.settings");
        Switch enabler = (Switch) mActivity.findViewById(resId);
        assertNotNull(enabler);

        // /M: Click on WiFi switch
        if (enabler.isChecked() != isOn) {
            mSolo.clickOnView(enabler);
            Log.d(TAG, "setWiFiState() " + isOn);
            mSolo.sleep(SLEEP_TIME * 10);
        }
    }

    private void scrollTop() {
        while (mSolo.scrollUp()) {
            mSolo.scrollUp();
        }
        mSolo.sleep(2000);
    }

    private boolean getWifiConnectedState(String ssid) {
        boolean res = false;
        ssid = "\"" + ssid + "\"";
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        }

        final ConnectivityManager connectivity = (ConnectivityManager) mActivity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiInfo currentConnecdInfo = mWifiManager.getConnectionInfo();
        if (currentConnecdInfo != null
                && ssid.equals(currentConnecdInfo.getSSID())) {
            Log.d(TAG, "getSSID() : " + currentConnecdInfo.getSSID());
            res = true;
        }
        return res;
    }

    private int getSavedNetworkId(String ssid) {
        int savedId = -1;
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        }
        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if ((config.SSID).equals("\"" + ssid + "\"")) {
                    savedId = config.networkId;
                    break;
                }
            }
        }
        return savedId;
    }

    private void parseAPConfig(String configPath) {
        DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dombuilder = domfac.newDocumentBuilder();
            InputStream is = new FileInputStream(configPath);
            Document doc = (Document) dombuilder.parse(is);
            Element root = (Element) doc.getDocumentElement();
            NodeList accessPoints = ((Node) root).getChildNodes();
            if (accessPoints != null) {
                Log.d(TAG, "accessPoints.getLength() = " + accessPoints.getLength());
                for (int i = 0; i < accessPoints.getLength(); i++) {
                    Node accessPoint = accessPoints.item(i);
                    Log.d(TAG, "i : " + i + " accessPoint : " + accessPoint);
                    if (accessPoint.getNodeType() == Node.ELEMENT_NODE) {
                        for (Node node = accessPoint.getFirstChild(); node != null; node = node.getNextSibling()) {
                            Log.d(TAG, "node : " + node);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                if (node.getNodeName().equals("ssid")) {
                                    mSSId = node.getFirstChild().getNodeValue();
                                } else if (node.getNodeName().equals("password")) {
                                    mPassword = node.getFirstChild().getNodeValue();
                                } else if (node.getNodeName().equals("turnoff")) {
                                    mIsWiFiOff = Integer.parseInt(node.getFirstChild().getNodeValue());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * M: check the current view is the right view, if the curent is mtklab's
     * view, it will return true, else return false
     */
    private boolean isRightView() {
        int i = 0;
        boolean result = false;
        while (i++ < 5) {
            if (mSolo.searchButton(mActivity.getString(android.R.string.cancel))
                    && mSolo.searchText(mSSId)) {
                Log.d(TAG, "isRightView = true");
                return true;
            } else if (mSolo.searchButton(mActivity.getString(android.R.string.cancel))) {
                mSolo.clickOnButton(mActivity.getString(android.R.string.cancel));
                mSolo.sleep(500);
            }
            scrollTop();
            if (mSolo.searchText(mSSId)) {
                mSolo.clickOnText(mSSId);
                mSolo.sleep(2000);
            }
        }
        Log.d(TAG, "isRightView, result = " + result);
        return result;
    }
}
