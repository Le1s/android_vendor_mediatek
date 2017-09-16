package com.mediatek.dialer.plugin.speeddial;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.op01.plugin.R;
    
public class SpeedDialController {
    private static final String TAG = "SpeedDialController";
    private static SpeedDialController sMe;
    private Context mfContext;
    private Context mContext;
    Uri uri = Uri.parse("content://speed_dial/numbers");
    
    private void SpeedDialController() {
    }

    public static SpeedDialController getInstance() {
        if (sMe == null) {
            sMe = new SpeedDialController();
        }
        return sMe;
    }

    public void handleKeyLongProcess(Activity activity, Context cnx, int key) {
        mfContext = activity;
        mContext = cnx;
        Cursor cursor = mContext.getContentResolver().query(uri, new String[] {"_id", "number"}, "_id" + " = " + key, null, null);

        String number = "";
        if (cursor!= null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("number");
                if (columnIndex != -1) {
                    number = cursor.getString(columnIndex);
                }
            }
            cursor.close();
        }
        Log.i(TAG, "handleKeyLongProcess, key = " + key);
        Log.i(TAG, "handleKeyLongProcess, number = " + number);
        if (TextUtils.isEmpty(number)) {
            showSpeedDialConfirmDialog();
        } else {
            final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri.fromParts("tel",
                    number, null));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mfContext.startActivity(intent);
        }
    }

    public void enterSpeedDial(Context fcnx) {
        Log.i(TAG, "enterSpeedDial");
        final Intent intent = new Intent();
        intent.setClassName("com.mediatek.op01.plugin", "com.mediatek.dialer.plugin.speeddial.SpeedDialActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        fcnx.startActivity(intent);
    }

    public void showSpeedDialConfirmDialog() {
        Log.i(TAG, "showSpeedDialConfirmDialog");
        AlertDialog confirmDialog = new AlertDialog.Builder(mfContext)
            .setTitle(mContext.getString(R.string.call_speed_dial))
            .setMessage(mContext.getString(R.string.dialog_no_speed_dial_number_message))
            .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            enterSpeedDial(mfContext);
                        }
                }).setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
            }).create();
        confirmDialog.show();
    }
}


