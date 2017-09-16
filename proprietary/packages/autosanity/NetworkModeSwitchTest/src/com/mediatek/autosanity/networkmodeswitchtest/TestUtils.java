package com.mediatek.autosanity.networkmodeswitchtest;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.SystemClock;

import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TestUtils {
    private static final String TAG = "NetworkModeSwitchTest";

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Intent getIntent(String packageName, String className) {
        Intent intent = new Intent();
        intent.setClassName(packageName, className);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * disable wifi if wifi is open.
     * @param context
     * @return true for disable wifi successfully, wifi is under not enabled.
     */
    public static boolean disableWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        log("disableWifi()...");
        if (wifiManager.isWifiEnabled()) {
            log("Wifi is enable, so disable it.");
            wifiManager.setWifiEnabled(false);
            int i = 0;
            while (wifiManager.isWifiEnabled() && i < 10) {
                SystemClock.sleep(1000);
                i++;
            }
            log("isWifiEnabled / i: " + wifiManager.isWifiEnabled() + " / " + i);
        } else {
            log("Wifi is already disabled, so do nothing.");
        }
        return !wifiManager.isWifiEnabled();
    }

    public static boolean isClickRight(ArrayList<TextView> clickedViews, String targetString) {
        log("targetString to click is: " + targetString);
        boolean isRightClick = false;
        for (TextView tv : clickedViews) {
            log("tv.getText() " + tv.getText());
            String foundText = tv.getText().toString();
            if (TextUtils.equals(foundText, targetString)) {
                isRightClick = true;
                break;
            }
        }
        return isRightClick;
    }

    public static List<SimInfoRecord> getSimInfoList(Context context) {
        log("getSimInfoList()...");
        List<SimInfoRecord> simList = SimInfoManager.getInsertedSimInfoList(context);

        Collections.sort(simList, new Comparator<SimInfoRecord>() {
            @Override
            public int compare(SimInfoRecord arg0, SimInfoRecord arg1) {
                return (arg0.mSimSlotId - arg1.mSimSlotId);
            }
        });
        return simList;
    }

    public static int getslot3G() {
        int slot3G = -1;
        slot3G = GeminiUtils.get3GCapabilitySIM();
        log("slot3G = " + slot3G);
        return slot3G;
    }

    public static String getSimDisplayName(List<SimInfoRecord> simList, int slotId) {
        String displayName = null;
        for (int i = 0; i < simList.size(); i++) {
            if (simList.get(i).mSimSlotId == slotId) {
                displayName = simList.get(i).mDisplayName;
            }
        }
        log("slotId / displayName: " + slotId + " / " + displayName);
        return displayName;
    }

    public static String getStringResource(Context context, String msg) {
        String resultString = null;
        int id = context.getResources().getIdentifier(msg, null, null);
        if (id == 0) {
            log("the string " + msg + " do not exsit");
            return "";
        }
        resultString = context.getString(id);
        log("get str :" + resultString);
        return resultString;
    }

    private static void log(String msg) {
        Log.i(TAG, msg);
    }

}
