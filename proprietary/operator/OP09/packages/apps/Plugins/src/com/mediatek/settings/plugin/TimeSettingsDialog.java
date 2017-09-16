package com.mediatek.settings.plugin;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

import com.mediatek.op09.plugin.R;

/**
 * <code>TimeSettingsDialog</code> is a dialog for user to
 * select time display mode. In domestic network environment,
 * Time display mode is not available, so this menu item is
 * grayed out.
 * When in international roaming environment, time
 * display mode will be available.
 * The default time display mode is Beijing time, and this
 * item only affects the time display in Call, Messaging
 * and Calendar.
 * In international roaming status, if the devices is registered
 * to CDMA network after booting, "Time display mode" dialog
 * should pop up auto matically.
 */
public class TimeSettingsDialog extends DialogPreference implements OnClickListener {
    private static final String TAG = "TimeSettingsDialog";
    private Dialog mDialog;
    private RadioButton mRadioBtn1 = null;
    private RadioButton mRadioBtn2 = null;
    private Context mContext;

    //Test Current time settings:
    private int mCurrentTime = 0;
    private int mSelectTimeMode = 0;
    private static final int BEIJING_MODE = 0;
    private static final int LOCAL_MODE = 1;

    /**
     * Constructor method.
     * @param context context
     * @param attrs AttributeSet
     */
    public TimeSettingsDialog(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        Log.d(TAG, "TimeSettingsDialog ");
        setDialogLayoutResource(R.layout.time_display_mode);
        mDialog = getDialog();
        mContext = context;
    }

    @Override
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Log.d(TAG, "in getView!!!");
        mRadioBtn1 = (RadioButton) view.findViewById(R.id.beijing_radio);
        mRadioBtn2 = (RadioButton) view.findViewById(R.id.local_radio);
        mRadioBtn1.setOnClickListener(this);
        mRadioBtn2.setOnClickListener(this);
        // init radio button:
        mCurrentTime = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.CT_TIME_DISPLAY_MODE, BEIJING_MODE);
        Log.i(TAG, "get ct init time display mode:" + mCurrentTime);
        if (mCurrentTime == BEIJING_MODE) {
            mRadioBtn1.setChecked(true);
            mRadioBtn2.setChecked(false);
        } else if (mCurrentTime == LOCAL_MODE) {
            mRadioBtn1.setChecked(false);
            mRadioBtn2.setChecked(true);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            Log.d(TAG, "Pressed OK");
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.CT_TIME_DISPLAY_MODE, mCurrentTime);
            // Send broadcast that Call App can receive it.
            Intent intent = new Intent("com.mediatek.ct.TIME_DISPLAY_MODE");
            intent.putExtra("time_display_mode", mCurrentTime);
            mContext.sendBroadcast(intent);
        } else {
            Log.d(TAG, "Pressed Cancel");
        }
    }

    @Override
    public void onClick(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        mDialog = getDialog();
        switch (view.getId()) {
            case R.id.beijing_radio:
                if (checked) {
                    if (mRadioBtn2 != null) {
                        mRadioBtn2.setChecked(false);
                        mCurrentTime = BEIJING_MODE;
                    }
                }
                break;
            case R.id.local_radio:
                if (checked) {
                    if (mRadioBtn1 != null) {
                        mRadioBtn1.setChecked(false);
                        mCurrentTime = LOCAL_MODE;
                    }
                }
                break;
            default:
                break;
        }
    }
}