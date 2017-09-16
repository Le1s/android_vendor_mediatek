package com.mediatek.smsreg.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.mediatek.smsreg.R;
import com.mediatek.smsreg.SmsRegConst;
import com.mediatek.smsreg.SmsRegService;

public class SendMessageAlertActivity extends Activity {
    private static final String TAG = "SmsReg/SendMessageAlertActivity";
    private static final int ID_SEND_MSG = 1;
    private static final String ON_SECOND_DIALOG = "on_second_dialog";

    private NotificationManager mNotificationManager = null;
    private Dialog mDialog = null;
    private boolean mIsOnSecondDiag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate.");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        mDialog = buildConfirmDialog();
        mDialog.show();
        showNotification();
    }

    /**
     * Show first dialogue to ask user whether to send registered message
     */
    private Dialog buildConfirmDialog() {
        Log.d(TAG, "buildConfirmDialog.");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.notify_dialog_customview, null);

        return new AlertDialog.Builder(this).setView(layout).setCancelable(false)
                .setTitle(R.string.send_message_dlg_title)
                .setPositiveButton(R.string.alert_dlg_ok_button, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Click positive button.");
                        clear();
                        dialog.dismiss();

                        responseSendMsg(true);
                    }
                }).setNegativeButton(R.string.alert_dlg_cancel_button, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Click negative button.");
                        dialog.dismiss();
                        showSecondDialog();
                    }
                }).create();
    }

    /**
     * Show second dialogue if cancel on 1st dialogue, to let user double check
     */
    private void showSecondDialog() {
        Log.i(TAG, "Current is on second dialog.");
        mIsOnSecondDiag = true;

        Dialog dialog = new AlertDialog.Builder(SendMessageAlertActivity.this).setCancelable(false)
                .setTitle(R.string.confirm_dlg_title).setMessage(R.string.confirm_dlg__msg)
                .setPositiveButton(R.string.alert_dlg_ok_button, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Click positive button.");

                        dialog.dismiss();
                        clear();

                        responseSendMsg(false);
                    }
                }).setNegativeButton(R.string.alert_dlg_cancel_button, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "Click negative button.");

                        dialog.dismiss();
                        clear();

                        mIsOnSecondDiag = false;

                        Intent intent = new Intent(SmsRegConst.ACTION_DISPLAY_DIALOG);
                        startActivity(intent);
                    }
                }).create();
        dialog.show();
    }

    /**
     * Close dialogue and clear notification
     */
    private void clear() {
        this.finish();
        mNotificationManager.cancel(ID_SEND_MSG);
    }

    /**
     * Show notification which is cleared after user have a response
     */
    private void showNotification() {
        Intent intent = new Intent();
        intent.setClass(this, SendMessageAlertActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.send_message_notification_title))
                .setContentText(getResources().getString(R.string.send_message_notification_msg))
                .setTicker(getResources().getString(R.string.send_message_notification_tickerText))
                .setSmallIcon(R.drawable.perm_sent_mms)
                .setContentIntent(pendingIntent)
                .build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(ID_SEND_MSG, notification);
    }

    /**
     * Start service with user response
     */
    private void responseSendMsg(boolean result) {
        Log.d(TAG, "responseSendMsg with " + result);
        Intent intent = new Intent(SmsRegConst.ACTION_RESPONSE_DIALOG);
        intent.setClass(this, SmsRegService.class);
        intent.putExtra(SmsRegConst.EXTRA_IS_NEED_SEND, result);
        startService(intent);
    }

    /**
     * If it's on 2nd dialogue before restore, dismiss 1st and show 2nd
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState");

        if (savedInstanceState != null) {
            Log.i(TAG, "mIsOnSecondDiag is " + savedInstanceState.getBoolean(ON_SECOND_DIALOG));

            if (savedInstanceState.getBoolean(ON_SECOND_DIALOG)) {
                mDialog.dismiss();
                showSecondDialog();
            }
        }
    }

    /**
     * Save the state whether current is on 2nd dialogue
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState mIsOnSecondDiag is " + mIsOnSecondDiag);
        outState.putBoolean(ON_SECOND_DIALOG, mIsOnSecondDiag);
    }
}
