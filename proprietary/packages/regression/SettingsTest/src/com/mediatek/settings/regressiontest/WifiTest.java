package com.mediatek.settings.regressiontest;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.widget.Button;
import android.widget.Switch;

import com.android.settings.Settings;
import com.jayway.android.robotium.solo.Solo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WifiTest extends
        ActivityInstrumentationTestCase2<Settings.WifiSettingsActivity> {

    private Solo solo;
    /*wait 2 min for wifi conectting*/
    private static final int SLEEP_TIMES_NUM = 22;
    private static final int SLEEP_TIME = 10000;
    private static final int FIVE_THOUSANDS_MILLISECOND = 5000;
    private static final int FIVE_HUNDREDS_MILLISECOND = 500;
    private static String TAG = "WifiTest";
    private static final String FILE_PATH = "/data/wifi_accounts.xml";
    private static final String DEFAULT_SSID = "mtklab";
    private static final String DEFAULT_PASSWORD = "1234567890";
    private static final String NOT_MAC_ADDRESS = "NVRAM WARNING:";
    public static final String MTKGUEST = "mtkguest";
    private static final int TWO_SECONDS = 2000;
    private static final int FIVE_SECONDS = 5000;

    private WifiManager mWifiManager;
    private Activity mActivity;
    private String mSsid = null;
    private String mPassword = null;

    public WifiTest() {
        super(Settings.WifiSettingsActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        mActivity = getActivity();
        assertNotNull(mActivity);
        solo = new Solo(getInstrumentation(), mActivity);
    }

    @Override
    // need over write
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // Get wiifi status
    private boolean getWifiMode() {
        mWifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        return mWifiManager.isWifiEnabled();
    }

    /**
     * turn on/off wifi switch
     *
     * @param onOff
     *            true: turn on
     *            false: turn off
     */
    private void switchWifi(boolean onOff) {
        //Switch switchView = (Switch) mActivity.getSwitchBar();
        int resId = mActivity.getResources().getIdentifier("switch_widget", "id", "com.android.settings");
        Switch switchView = (Switch) mActivity.findViewById(resId);
        assertNotNull(switchView);
        // Click on wifi switch
        if (switchView.isChecked() != onOff) {
            solo.clickOnView(switchView);
        }
    }

    /**
     * Turn on Wi-Fi
     * @throws Exception
     */
    public void test01_Wifi_001() throws Exception {
        Log.d(TAG, "test01_Wifi_001");
        // turn on wifi
        switchWifi(true);
        solo.sleep(FIVE_SECONDS);
        // check if wifi is enable
        assertTrue(getWifiMode());
    }

    /**
     * Turn off Wi-Fi
     * @throws Exception
     */
    public void test02_Wifi_002() throws Exception {
        Log.d(TAG, "test02_Wifi_002");

        // turn off wifi
        switchWifi(false);
        solo.sleep(FIVE_SECONDS);
        // check if wifi is disable
        assertFalse(getWifiMode());
    }

    /**
     * Check Ap List
     * @throws Exception
     */
    public void test03_Wifi_003() throws Exception {
        Log.d(TAG, "test03_WifiCheckApList()");
        parseConfig();
        // turn on wifi
        switchWifi(true);
        solo.sleep(FIVE_SECONDS);
        assertTrue(solo.searchText(mSsid));

        //turn off wifi
        switchWifi(false);
        solo.sleep(FIVE_SECONDS);
        //wifi_empty_list_wifi_off
        String wifiEmptyText = TestUtils.getString(mActivity, "wifi_empty_list_wifi_off");
        Log.d(TAG, "wifiEmptyText = " + wifiEmptyText);
        assertTrue(solo.searchText(wifiEmptyText));
    }

    private void parseConfig() {
        String filePath = FILE_PATH;
        Log.d(TAG, "filePath = " + filePath);
        domParse(filePath);
        if (mSsid == null || mPassword == null) {
            mSsid = DEFAULT_SSID;
            mPassword = DEFAULT_PASSWORD;
        }
        Log.d(TAG, "mSsid = " + mSsid);
        Log.d(TAG, "mPassword = " + mPassword);
    }

    /**
     * Wifi can be turned off when enable Airplane mode
     * @throws Exception
     */
    public void test04_Wifi_004() throws Exception {
        Log.d(TAG, "test04_Wifi_004()");
        // turn on wifi
        switchWifi(true);
        solo.sleep(FIVE_SECONDS);

        setAirplaneModeOn(true);
        solo.sleep(FIVE_SECONDS);

        // turn off wifi
        switchWifi(false);
        solo.sleep(FIVE_SECONDS);
        // check if wifi is disable
        assertFalse(getWifiMode());
    }

    /**
     * Wifi can be resumed when disable Airplane mode
     * @throws Exception
     */
    public void test05_Wifi_005() throws Exception {
        Log.d(TAG, "test05_Wifi_005()");
        // turn on wifi
        switchWifi(true);
        solo.sleep(FIVE_SECONDS);

        // turn on airplane mode
        setAirplaneModeOn(true);
        solo.sleep(FIVE_SECONDS);

        // turn off airplane mode
        setAirplaneModeOn(false);
        solo.sleep(FIVE_SECONDS);
        // check if wifi is enable
        assertTrue(getWifiMode());
    }

    /**
     * Forget Ap
     * @throws Exception
     */
    public void test06_Wifi_038() throws Exception {
        Log.d(TAG, "test06_Wifi_038()");
        // make sure wifi switch is checked
        switchWifi(true);
        // forget AP
        int networkid = wifiSaved(MTKGUEST);
        if (networkid != -1) {
            mWifiManager.forget(networkid, null);
            solo.sleep(FIVE_THOUSANDS_MILLISECOND);
        }
        scrollTop();
        // connect mtkguest
        if (solo.searchText(MTKGUEST)) {
            solo.clickOnText(MTKGUEST);
            solo.sleep(FIVE_SECONDS);
        }

        scrollTop();
        if (solo.searchText(MTKGUEST)) {
            solo.clickOnText(MTKGUEST);
            solo.sleep(FIVE_SECONDS);
            if (solo.searchButton(TestUtils.getString(mActivity, "wifi_forget"), true)) {
                // if mtkguest is connected,forget it.
                solo.clickOnButton(TestUtils.getString(mActivity, "wifi_forget"));
                solo.sleep(FIVE_SECONDS);
            }


            assertTrue((wifiSaved(MTKGUEST) == -1));
        }
    }

    /**
     * Connected Ap's info can be shown
     * @throws Exception
     */
    public void test07_Wifi_039() throws Exception {
        Log.d(TAG, "test03_WifiConnect()");

        String filePath = FILE_PATH;
        Log.d(TAG, "filePath = " + filePath);
        domParse(filePath);
        if (mSsid == null || mPassword == null) {
            mSsid = DEFAULT_SSID;
            mPassword = DEFAULT_PASSWORD;
        }
        Log.d(TAG, "mSsid = " + mSsid);
        Log.d(TAG, "mPassword = " + mPassword);
        // turn on wifi
        switchWifi(true);
        solo.sleep(FIVE_SECONDS);

        // forget AP
        int networkid = wifiSaved(mSsid);
        if (networkid != -1) {
            mWifiManager.forget(networkid, null);
            solo.sleep(FIVE_THOUSANDS_MILLISECOND);
        }

        scrollTop();
        //connect AP
        if (solo.searchText(mSsid)) {
            solo.clickOnText(mSsid);
            solo.sleep(FIVE_THOUSANDS_MILLISECOND);
            if (isRightView()) {
                solo.enterText(0, mPassword);
                solo.sleep(FIVE_HUNDREDS_MILLISECOND);
                Button connButton = solo.getButton(3);
                if (connButton != null) {
                    solo.clickOnView(connButton);
                }
            }
        }
        solo.sleep(FIVE_SECONDS);

        // check if wifi is connected
        int step = 0;
        while (!wifiConnected(mSsid) && step++ < SLEEP_TIMES_NUM) {
            solo.sleep(FIVE_THOUSANDS_MILLISECOND);
        }
        assertTrue(wifiConnected(mSsid));

        //check connect info

        // forget AP
        networkid = wifiSaved(mSsid);
        if (networkid != -1) {
            scrollTop();
            //connect AP
            if (solo.searchText(mSsid)) {
                solo.clickOnText(mSsid);
                //wifi_status, wifi_signal, wifi_speed, wifi_frequency
                String wifiStatus = TestUtils.getString(mActivity, "wifi_status");
                String wifiSignal = TestUtils.getString(mActivity, "wifi_signal");
                String wifiSpeed = TestUtils.getString(mActivity, "wifi_speed");
                String wifiFrequency = TestUtils.getString(mActivity, "wifi_frequency");

                boolean result = false;
                result |= solo.searchText(wifiStatus);
                result |= solo.searchText(wifiSignal);
                result |= solo.searchText(wifiSpeed);
                result |= solo.searchText(wifiFrequency);

                assertTrue(result);

                if (solo.searchButton(TestUtils.getString(mActivity, "wifi_forget"), true)) {
                    solo.clickOnButton(TestUtils.getString(mActivity, "wifi_forget"));
                    solo.sleep(FIVE_SECONDS);
                }
            }
        }
        solo.goBack();

    }

    public void test08_Wifi_047() throws Exception {
        Log.d(TAG, "test08_Wifi_047()");
        test03_Wifi_003();

    }

    public void test09_Wifi_061() throws Exception {
        Log.d(TAG, "test09_Wifi_061()");
        // turn on wifi
        switchWifi(true);
        solo.sleep(FIVE_SECONDS);
        solo.sendKey(KeyEvent.KEYCODE_MENU);
        solo.sleep(TWO_SECONDS);
        String wifiAdvanced = TestUtils.getString(mActivity, "wifi_menu_advanced");
        if (solo.searchText(wifiAdvanced)) {
            solo.clickOnText(wifiAdvanced);
            solo.sleep(TWO_SECONDS);
            String wifiMacAddress = TestUtils.getString(mActivity, "wifi_advanced_mac_address_title");
            assertTrue(solo.searchText(wifiMacAddress));
        }

    }

//    public void test10_Wifi_062() throws Exception {
//        Log.d(TAG, "test10_Wifi_062()");
//        test07_Wifi_039();
//
//    }
//
//    public void test11_Wifi_065() throws Exception {
//        Log.d(TAG, "test11_Wifi_065()");
//        test06_Wifi_038();
//    }

    private void scrollTop() {
        while (solo.scrollUp()) {
            solo.scrollUp();
        }
        solo.sleep(2000);
    }

    private boolean wifiConnected(String ssid) {
        boolean res = false;
        ssid = "\"" + ssid + "\"";
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        }

        final ConnectivityManager connectivity = (ConnectivityManager) mActivity
                                                           .getSystemService(Context.CONNECTIVITY_SERVICE);
        //if (connectivity != null && connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
            WifiInfo currentConnecdInfo = mWifiManager.getConnectionInfo();
            if (currentConnecdInfo != null && ssid.equals(currentConnecdInfo.getSSID())) {
                Log.d(TAG, "currentConnectedInfo.getSSID() =  " + currentConnecdInfo.getSSID());
                res = true;
            }
        //}

        return res;
    }

    private int wifiSaved(String ssid) {
        int res = -1;
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        }
        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if ((config.SSID).equals("\"" + ssid + "\"")) {
                    res = config.networkId;
                    break;
                }
            }
        }
        return res;
    }

    private void domParse(String filePath) {
        DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dombuilder = domfac.newDocumentBuilder();
            InputStream is = new FileInputStream(filePath);
            Document doc = (Document) dombuilder.parse(is);
            Element root = (Element) doc.getDocumentElement();
            NodeList accessPoints = ((Node) root).getChildNodes();
            if (accessPoints != null) {
                Log.d(TAG, "accessPoints.getLength() = " + accessPoints.getLength());
                for (int i = 0; i < accessPoints.getLength(); i++) {
                    Log.d(TAG, "i = " + i);
                    Node accessPoint = accessPoints.item(i);
                    Log.d(TAG, "accessPoint = " + accessPoint);
                    if (accessPoint.getNodeType() == Node.ELEMENT_NODE) {
                        for (Node node = accessPoint.getFirstChild(); node != null;
                                node = node.getNextSibling()) {
                            Log.d(TAG, "node = " + node);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                if (node.getNodeName().equals("ssid")) {
                                    mSsid = node.getFirstChild().getNodeValue();

                                } else if (node.getNodeName().equals("password")) {
                                    mPassword = node.getFirstChild().getNodeValue();
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
     * M: check the current view is the right view,
     * if the curent is  mtklab's view, it will return true, else return false
     */
    private boolean isRightView() {
        int i = 0;
        boolean result = false;
        while (i++ < 5) {
            if (solo.searchButton(mActivity.getString(android.R.string.cancel)) && solo.searchText(mSsid)) {
                Log.d(TAG, "isRightView = true");
                return true;
            } else if (solo.searchButton(mActivity.getString(android.R.string.cancel))) {
                solo.clickOnButton(mActivity.getString(android.R.string.cancel));
                solo.sleep(500);
            }
            scrollTop();
            if (solo.searchText(mSsid)) {
                solo.clickOnText(mSsid);
                solo.sleep(2000);
            }
         }
        Log.d(TAG, "isRightView, result = " + result);
        return result;
    }

    private void setAirplaneModeOn(boolean enabling) {
        // Change the system setting
        android.provider.Settings.Global.putInt(mActivity.getContentResolver(), android.provider.Settings.Global.AIRPLANE_MODE_ON,
                                enabling ? 1 : 0);

        // Post the intent
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabling);
        mActivity.sendBroadcastAsUser(intent, UserHandle.ALL);
    }
}
