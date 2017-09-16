package com.mediatek.phone.plugin;

import android.app.Notification;
import android.app.PendingIntent;
import android.util.Log;

import com.mediatek.phone.ext.DefaultPhoneMiscExt;

/**
 * CT OP09 Phone misc feature.
 */
public class OP09PhoneMiscExt extends DefaultPhoneMiscExt {

    private static final String TAG = "OP09PhoneMiscExt";

    @Override
    public String changeTextContainingSim(String originalText, int slotId) {
        if (slotId == 0) {
            return replaceSimToUim(originalText);
        }
        Log.d(TAG, "op09 replace string: " + originalText);
        return originalText;
    }
    @Override
    public String replaceSimToUim(String simString) {
        if (simString.contains("SIM")) {
            simString = simString.replaceAll("SIM", "UIM");
        }

        if (simString.contains("sim")) {
            simString = simString.replaceAll("sim", "uim");
        }
        Log.d(TAG, "op09 replace string: " + simString);
        return simString;
    }

    @Override
    public boolean publishBinderDirectly() {
        return true;
    }

    @Override
    public void customizeNetworkSelectionNotification(
            Notification notification, String titleText, String expandedText, PendingIntent pi) {
        // Set to null to avoid dierect user to default network selection activity
        notification.contentIntent = null;
    }
}
