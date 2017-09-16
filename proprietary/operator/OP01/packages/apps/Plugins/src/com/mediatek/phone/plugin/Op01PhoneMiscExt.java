package com.mediatek.phone.plugin;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.provider.CallLog.Calls;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Telephony;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyCapabilities;
import com.mediatek.common.PluginImpl;
import com.mediatek.phone.ext.DefaultPhoneMiscExt;

@PluginImpl(interfaceName="com.mediatek.phone.ext.IPhoneMiscExt")
public class Op01PhoneMiscExt extends DefaultPhoneMiscExt {
    private static final String LOG_TAG = "Op01PhoneMiscExt";
    private static final boolean DBG = true;

    public static final int VOICE_CALL_REJECT_MODE = TelephonyManager.PHONE_TYPE_GSM;
    //public static final int VIDEO_CALL_REJECT_MODE = 2;
    private static final String VOICE_CALL_REJECT_MODE_KEY = Settings.System.VOICE_CALL_REJECT_MODE;
    //private static final String VIDEO_CALL_REJECT_MODE_KEY = Settings.System.VT_CALL_REJECT_MODE;
    private static final int OFF = 0;
    private static final int ALL_NUMBERS = 1;
    private static final String BLACK_LIST_URI = "content://reject/list";
    protected Context mContext;
    private Connection mConnection;

    private static final String[] BLACK_LIST_PROJECTION = {
        "Number",
        "type"
    };

    /**
     * Log the message
     * @param msg the message will be printed
     */
    void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    /**
     * called when an incoming call, need to check whether it is a black number
     * @param context
     * @param connection
     * @return true if it is a incoming black number.
     */
     public boolean shouldBlockNumber(Context context, Connection connection) {
        String address = connection.getAddress();
        int phoneType = connection.getCall().getPhone().getPhoneType();
        mConnection = connection;
        try {
            mContext = context.createPackageContext("com.mediatek.op01.plugin", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            log("catch no found com.mediatek.op01.plugin");
            return false;
        }
        int mode = getBlockMode(phoneType);
        if (DBG) {
            log("shouldBlock, number = " + address + " phoneType = " + phoneType + " mode = " + mode);
        }
        if (mode == OFF) {
            return false;
        }
        if (mode == ALL_NUMBERS) {
            return true;
        }
        return autoReject(address, phoneType);
    }

    /**
     * called when an incoming call, add reject CallLog to db
     * @param ci
     * @param phoneAccountHandle
     */
    public void addRejectCallLog(CallerInfo ci, PhoneAccountHandle phoneAccountHandle) {
        int callLogType = Calls.AUTO_REJECT_TYPE;
        int features = 0;
        final String number = mConnection.getAddress();
        final long start = mConnection.getCreateTime();
        final int duration = (int)mConnection.getDurationMillis();
        final Phone phone = mConnection.getCall().getPhone();
        // For international calls, 011 needs to be logged as +
        final int presentation = getPresentation(mConnection, ci);
        final boolean isOtaspNumber = TelephonyCapabilities.supportsOtasp(phone)
            && phone.isOtaSpNumber(number);
       // Don't log OTASP calls.
        if (!isOtaspNumber) {
            try {
                Calls.addCall(ci, mContext, number, presentation,
                        callLogType, features, phoneAccountHandle, 
                        start, duration, null, true /* addForAllUsers */);
            } catch (Exception e) {
                // This is very rare but may happen in legitimate cases.
                // E.g. If the phone is encrypted and thus write request fails, it may cause
                // some kind of Exception (right now it is IllegalArgumentException, but this
                // might change).
                //
                // We don't want to crash the whole process just because of that, so just log
                // it instead.
                log("logCall-addCall:" + e + "Exception raised during adding CallLog entry.");
            }
        }
    }

    /**
     * get the block mode
     * @param type reject type(all reject or voice call reject or video call reject)
     * @return the mode that current setting
     */
    public int getBlockMode(int type) {
        //final String key = type == VOICE_CALL_REJECT_MODE ? VOICE_CALL_REJECT_MODE_KEY : VIDEO_CALL_REJECT_MODE_KEY;
        final String key = VOICE_CALL_REJECT_MODE_KEY;
        try {
            final int mode =  Settings.System.getInt(mContext.getContentResolver(), key);
            return mode;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return OFF;
    }

    /**
     * check if the call should be rejected
     * @param number the incoming call number
     * @param type reject type
     * @return the result that the current number should be auto reject
     */
    public boolean autoReject(String number, int type) {
        Cursor cursor = mContext.getContentResolver().query(Uri.parse(BLACK_LIST_URI),
                BLACK_LIST_PROJECTION, null, null, null);
        if (cursor == null) {
            if (DBG) {
                log("cursor is null...");
            }
            return false;
        }
        String blockNumber;
        int blockType;
        boolean result = false;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            blockNumber = cursor.getString(0);
            if (PhoneNumberUtils.compare(number, blockNumber)) {
               // get it
                blockType = Integer.parseInt(cursor.getString(1));
                if (DBG) {
                    log("blockType = " + blockType);
                }
                /*if (blockType == BLOCK_VOICE_AND_VIDEO_CALL) {
                    result = true;
                    break;
                }
                if (type == VIDEO_CALL_REJECT_MODE && blockType == VIDEO_CALL_REJECT_MODE) {
                    result = true;
                    break;
                }*/
                if (type == VOICE_CALL_REJECT_MODE && blockType == VOICE_CALL_REJECT_MODE) {
                    result = true;
                    break;
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    /**
     * Get the presentation from the callerinfo if not null otherwise,
     * get it from the connection.
     *
     * @param conn The phone connection.
     * @param callerInfo The CallerInfo. Maybe null.
     * @return The presentation to use in the logs.
     */
    public int getPresentation(Connection conn, CallerInfo callerInfo) {
        int presentation;

        if (null == callerInfo) {
            presentation = conn.getNumberPresentation();
        } else {
            presentation = callerInfo.numberPresentation;
            log("- getPresentation(): ignoring connection's presentation: " +
                         conn.getNumberPresentation());
        }
        log("- getPresentation: presentation: " + presentation);
        return presentation;
    }
}
