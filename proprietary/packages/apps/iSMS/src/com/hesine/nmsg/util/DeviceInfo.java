package com.hesine.nmsg.util;

import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.hesine.nmsg.Application;

public class DeviceInfo {
    private static final String TAG = "SystemInfo";
    private static final int DAY_OF_MILLIS = 24 * 60 * 60 * 1000;
    
    public static final String INVALID_CODE = "000000000000000";
    public static String getIMEI(Context context) {
        String imei = "";

        TelephonyManager mTelephonyMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        if (mTelephonyMgr != null) {
            imei = mTelephonyMgr.getDeviceId();
        }
        
        imei = imei+"";
        MLog.trace(TAG, "imei: " + imei);
        return imei;
    }

    public static String getIMSI(Context context) {
        String imsi = "";

        TelephonyManager mTelephonyMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        if (mTelephonyMgr != null) {
            imsi = mTelephonyMgr.getSubscriberId();
        }

        if (TextUtils.isEmpty(imsi)) {
            imsi = INVALID_CODE;
        }
        MLog.trace(TAG, "imsi: " + imsi);
        return imsi;
    }

    public static String getMac(Context context) {
        String macAddress = null;
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        if (info != null) {
            macAddress = info.getMacAddress();
        }
        MLog.trace(TAG, "macAddrss: " + macAddress);
        return macAddress;
    }

    public static String getDeviceId(Context context) {
        String deviceId = null;

        deviceId = getIMEI(context);
        if (TextUtils.isEmpty(deviceId) || INVALID_CODE.equals(deviceId)) {
            deviceId = getMac(context);
        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = INVALID_CODE;
        }
        return deviceId;
    }
    
    public static String getPhonenum(Context context) {
        String number = "";

        TelephonyManager mTelephonyMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        if (mTelephonyMgr != null) {
            number = mTelephonyMgr.getLine1Number();
        }

        MLog.trace(TAG, "number: " + number);
        return number;
    }

    public static String getNetworkName(Context context) {
        String apn = "Unknown";
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        if (info != null) {
            if (ConnectivityManager.TYPE_WIFI == info.getType()) {
                apn = info.getTypeName();
                if (apn == null) {
                    apn = "wifi";
                }
            } else {
                apn = info.getExtraInfo();
                if (apn == null) {
                    apn = "mobile";
                }
            }
        }

        return apn;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE + "";
    }

    public static String getCellId(Context context) {
        TelephonyManager mTelephoneManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        int cellId = 0;

        if (mTelephoneManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA
                || mTelephoneManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
            CdmaCellLocation location = (CdmaCellLocation) mTelephoneManager.getCellLocation();
            if (location != null) {
                cellId = location.getBaseStationId();
            }
        } else {
            GsmCellLocation location = (GsmCellLocation) mTelephoneManager.getCellLocation();
            if (location != null) {
                cellId = location.getCid();
            }
        }

        return cellId + "";
    }

    public static String getLanuage(Context context){
        return Locale.getDefault().getLanguage();
    }
    public static boolean isNetworkReady(Context context) {

        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null == connManager) {
            MLog.error(TAG, "connectivity manager is null when checking active network");
            return false;
        }

        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info == null) {
            MLog.error(TAG, "no active network when checking active network");
            return false;
        }

        if (!info.isConnected()) {
            MLog.error(TAG, "current network is not connected when checking active network");
            return false;
        }

        if (!info.isAvailable()) {
            MLog.error(TAG, "current network is not available when checking active network");
            return false;
        }
        return true;
    }
    
    public static long getCurrentDay() {
        return  (System.currentTimeMillis() / DAY_OF_MILLIS);
    }
    
    public static String getDisplayInfo() {
        int width = -1;
        int height = -1;
        try {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wmg = (WindowManager) Application.getInstance().getSystemService(
                    Context.WINDOW_SERVICE);
            wmg.getDefaultDisplay().getMetrics(dm);
            width = dm.widthPixels;
            height = dm.heightPixels;
        } catch (Exception e) {
            MLog.error(TAG, "getNetworkStatus get Execption: " + e.toString());
            width = -1;
            height = -1;
        }

        return width + "|" + height;
    }
    
    public static String getSmsServiceCenter() {

        try {
            final String smsUri = "content://sms/";
            final String serviceCenter = "service_center";
            Cursor cur = Application.getInstance()
                    .getContentResolver()
                    .query(Uri.parse(smsUri), new String[] { serviceCenter },
                            "type = ? AND service_center IS NOT NULL", new String[] { "1" },
                            "date DESC LIMIT 1");

            if (cur == null) {
                MLog.error(TAG, "get cursor is null");
                return "";
            }
            if (!cur.moveToFirst()) {
                cur.close();
                MLog.error(TAG, "not inbox msg with service center in sms db");
                return "";
            }

            String ret = cur.getString(cur.getColumnIndex(serviceCenter));
            cur.close();

            if (ret == null)
                ret = "";
            return ret;

        } catch (Exception e) {
            MLog.error(TAG, "getSmsServiceCenter get Execption: " + e.toString());
        }
        return "";
    }
}
