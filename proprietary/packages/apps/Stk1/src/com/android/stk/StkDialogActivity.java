/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.stk;

import com.android.internal.telephony.cat.TextMessage;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.telephony.cat.CatLog;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.IccCardConstants;

/**
 * AlretDialog used for DISPLAY TEXT commands.
 *
 */
public class StkDialogActivity extends Activity implements View.OnClickListener {
    // members
    private static final String className = new Object(){}.getClass().getEnclosingClass().getName();
    private static final String LOG_TAG = className.substring(className.lastIndexOf('.') + 1);
    //keys) for saving the state of the dialog in the icicle
    private static final String TEXT = "text";
    TextMessage mTextMsg = null;
    protected boolean mIsResponseSent = false;
    private int mSlotId = -1;
    private String mStkSource = null;
    private Context mContext = null;
    private StkAppService appService = StkAppService.getInstance();
    // message id for time out
    private static final int MSG_ID_TIMEOUT = 1;
    private boolean mIsRegisterReceiverDone = false;
    // buttons id
    public static final int OK_BUTTON = R.id.button_ok;
    public static final int CANCEL_BUTTON = R.id.button_cancel;
    protected static final int MIN_LENGTH = 6;
    protected static final int MIN_WIDTH = 170;

    private HandlerThread mTimeoutThread = null;
    private TimeoutHandler mTimeoutHandler = null;
    private AlarmManager mAlarmManager = null;
    private PendingIntent mTimeOutIntent = null;
    private TimeoutReceiver mTimeoutReceiver = null;
    private String sOperatorSpec = SystemProperties.get("ro.operator.optr", "OM");
    private static final String ACTION_DIALOG_TIMEOUT = "android.stkDialog.TIMEOUT";

    private final IntentFilter mSIMStateChangeFilter =
            new IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED);

    private final BroadcastReceiver mSIMStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())) {
                String simState = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
                int slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY, -1);

                CatLog.d(LOG_TAG, "mSIMStateChangeReceiver() - slotId[" + slotId +
                        "]  state[" + simState + "], mSlotId: " + mSlotId);
                if ((slotId == mSlotId) &&
                    ((IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(simState)) ||
                    (IccCardConstants.INTENT_VALUE_ICC_NOT_READY.equals(simState)))) {
                    if (IccCardConstants.INTENT_VALUE_ICC_NOT_READY.equals(simState)) {
                        showTextToast(getApplicationContext(),
                        getString(R.string.lable_sim_not_ready));
                    }
                    cancelTimeOut();
                    mIsResponseSent = true;
                    finish();
                }
            }
        }
    };

    private final IntentFilter mAirplaneModeFilter = new IntentFilter(
        Intent.ACTION_AIRPLANE_MODE_CHANGED);

    private BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean airplaneModeEnabled = isAirplaneModeOn(mContext);
            StkAppInstaller appInstaller = StkAppInstaller.getInstance();
            CatLog.d(LOG_TAG, "mAirplaneModeReceiver AIRPLANE_MODE_CHANGED: " +
            airplaneModeEnabled);
            if (airplaneModeEnabled) {
                mIsResponseSent = true;
                cancelTimeOut();
                finish();
            }
        }
    };

    private boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        CatLog.d(LOG_TAG, "onCreate");
        initFromIntent(getIntent());
        if (mTextMsg == null) {
            mIsRegisterReceiverDone = false;
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        Window window = getWindow();

        setContentView(R.layout.stk_msg_dialog);
        TextView mMessageView = (TextView) window
                .findViewById(R.id.dialog_message);

        Button okButton = (Button) findViewById(R.id.button_ok);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        mContext = getBaseContext();

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        setTitle(mTextMsg.title);
        if (!(mTextMsg.iconSelfExplanatory && mTextMsg.icon != null)) {
            if ((mTextMsg.text == null) || (mTextMsg.text.length() < MIN_LENGTH)) {
                mMessageView.setMinWidth(MIN_WIDTH);
            }
            mMessageView.setText(mTextMsg.text);
        }

        if (mTextMsg.icon == null) {
            CatLog.d(LOG_TAG, "onCreate icon is null");
            window.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                    com.android.internal.R.drawable.stat_notify_sim_toolkit);
        } else {
            window.setFeatureDrawable(Window.FEATURE_LEFT_ICON,
                    new BitmapDrawable(mTextMsg.icon));
        }
        //clear optionmenu in stkDialog activity
        window.clearFlags(WindowManager.LayoutParams.FLAG_NEEDS_MENU_KEY);
        registerReceiver(mSIMStateChangeReceiver, mSIMStateChangeFilter);
        registerReceiver(mAirplaneModeReceiver, mAirplaneModeFilter);

        if (sOperatorSpec.equals("OP01")) {
            CatLog.d(LOG_TAG, "OnCreate, OP01 package");
            //mCtx = this.parent.getApplicationContext();
            if (mAlarmManager == null) {
                if (mContext != null) {
                    CatLog.d(LOG_TAG, "get mAlarmManager");            
                    mAlarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
                } else {
                    CatLog.d(LOG_TAG, "mContext is null");
                }
            }
            if (mTimeoutReceiver == null) {
                CatLog.d(LOG_TAG, "new TimeoutReceiver"); 
                mTimeoutReceiver = new TimeoutReceiver();
            }
            IntentFilter filter = new IntentFilter(ACTION_DIALOG_TIMEOUT);
            CatLog.d(LOG_TAG, "registerReceiver"); 
            registerReceiver(mTimeoutReceiver, filter);
        } else {
            mTimeoutThread = new HandlerThread("timeoutThread");
            mTimeoutThread.start();
            mTimeoutHandler = new TimeoutHandler(mTimeoutThread.getLooper());
        }
        mIsRegisterReceiverDone = true;

        // Set a new task description to change icon
        if (sOperatorSpec.equals("OP02") && PhoneConstants.SIM_ID_1 < mSlotId) {
            setTaskDescription(new ActivityManager.TaskDescription(null,
            BitmapFactory.decodeResource(getResources(),
            R.drawable.ic_launcher_sim2_toolkit)));
        }
    }

    public void onClick(View v) {
        String input = null;

        switch (v.getId()) {
        case OK_BUTTON:
            CatLog.d(LOG_TAG, "OK Clicked! isCurCmdSetupCall[" +
                    appService.isCurCmdSetupCall(mSlotId) + "], mSlotId: " + mSlotId);
            if ((appService != null) && appService.isCurCmdSetupCall(mSlotId)) {
                CatLog.d(LOG_TAG, "dailStkCall");
                appService.dailStkCall(mSlotId);
            }
            sendResponse(StkAppService.RES_ID_CONFIRM, true);
            cancelTimeOut();
            finish();
            break;
        case CANCEL_BUTTON:
            CatLog.d(LOG_TAG, "Cancel Clicked!, mSlotId: " + mSlotId);
            sendResponse(StkAppService.RES_ID_CONFIRM, false);
            cancelTimeOut();
            finish();
            break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        CatLog.d(LOG_TAG, "onNewIntent - mIsResponseSent[" + mIsResponseSent + "]" + ", mSlotId: " + mSlotId);
        initFromIntent(intent);
        if (mTextMsg == null) {
            finish();
            return;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        CatLog.d(LOG_TAG, "onKeyDown - keyCode:" + keyCode);    
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            CatLog.d(LOG_TAG, "onKeyDown - KEYCODE_BACK");
            cancelTimeOut();
            sendResponse(StkAppService.RES_ID_BACKWARD);
            finish();
            break;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        CatLog.d(LOG_TAG, "onResume - mIsResponseSent[" + mIsResponseSent +
                "], sim id: " + mSlotId);
        //For performance auto test case, do not delete this log.
        CatLog.d(LOG_TAG, "Stk_Performance time: " + SystemClock.elapsedRealtime());
        if (appService != null) {
            appService.indicateDialogVisibility(true, mSlotId);
        } else {
           CatLog.d(LOG_TAG, "onPause, appService is null.");
           mIsResponseSent = true;//Skip TR since this is not a real activity triggered from sim.
           showTextToast(getApplicationContext(), getString(R.string.lable_not_available));
           finish();
           return;
        }
        startTimeOut(mTextMsg.userClear);
    }

    @Override
    public void onPause() {
        super.onPause();
        CatLog.d(LOG_TAG, "onPause, sim id: " + mSlotId);
        if (appService != null) {
            appService.indicateDialogVisibility(false, mSlotId);
        }
        /* For operator lab test, cancelTimeOut() should be removed.
           When HOME key pressed, the timer should be counted continually for sending TR.*/
        //cancelTimeOut();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsResponseSent = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        CatLog.d(LOG_TAG, "onStop - before Send CONFIRM false mIsResponseSent[" +
                mIsResponseSent + "], sim id: " + mSlotId);
        if (null == appService) {
            CatLog.d(LOG_TAG, "null appService");
            return;
        }
        if (null == appService.getStkContext(mSlotId)) {
            CatLog.d(LOG_TAG, "null stk context");
            return;
        }
        if (!mIsResponseSent) {
            appService.getStkContext(mSlotId).setPendingDialogInstance(this);
            //sendResponse(StkAppService.RES_ID_CONFIRM, false);
            //finish();
        } else {
            CatLog.d(LOG_TAG, "finish.");
            appService.getStkContext(mSlotId).setPendingDialogInstance(null);
            cancelTimeOut();
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CatLog.d(LOG_TAG, "onDestroy - before Send CONFIRM false mIsResponseSent[" + mIsResponseSent +
                "], sim id: " + mSlotId);
        if (!mIsResponseSent) {
            if (null == appService) {
                //To get instance again, if stkappservice has created before onDestroy.
                appService = StkAppService.getInstance();
            }
            if (null != appService) {
                if (!appService.isDialogPending(mSlotId)) {
                    CatLog.d(LOG_TAG, "handleDestroy - Send false confirm.");
                    sendResponse(StkAppService.RES_ID_CONFIRM, false);
                }
            }
        }
        if (appService != null) {
            appService.indicateDialogVisibility(false, mSlotId);
        }
        cancelTimeOut();
        if (sOperatorSpec.equals("OP01")) {
            if (mIsRegisterReceiverDone) {
                unregisterReceiver(mTimeoutReceiver);
            }
            mTimeoutReceiver = null;
            mAlarmManager = null;
        } else {
            if (null != mTimeoutThread) {
                mTimeoutThread.quit();
            }
        }
        if (mIsRegisterReceiverDone) {
            unregisterReceiver(mSIMStateChangeReceiver);
            unregisterReceiver(mAirplaneModeReceiver);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        CatLog.d(LOG_TAG, "onSaveInstanceState");

        super.onSaveInstanceState(outState);
        outState.putParcelable(TEXT, mTextMsg);
        outState.putBoolean("RESPONSE_SENT", mIsResponseSent);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTextMsg = savedInstanceState.getParcelable(TEXT);
        CatLog.d(LOG_TAG, "onRestoreInstanceState - [" + mTextMsg + "]");
        mIsResponseSent = savedInstanceState.getBoolean("RESPONSE_SENT");
    }

    private void sendResponse(int resId) {
        sendResponse(resId, true);
    }

    private void initFromIntent(Intent intent) {

        if (intent != null) {
            mTextMsg = intent.getParcelableExtra("TEXT");
            mSlotId = intent.getIntExtra(StkAppService.SLOT_ID, -1);
            mStkSource = intent.getStringExtra(StkAppService.STK_SOURCE_KEY);
            if (appService != null) {
                if (!appService.isValidStkSourceKey(mStkSource)) {
                    mIsResponseSent = true;
                    finish();
                    appService.restoreCurrentCmd(mSlotId);
                    return;
                }
            } else {
                 CatLog.d(LOG_TAG, "appService is null!");
                 mIsResponseSent = true;
                 finish();
                 return;
            }
        } else {
            finish();
        }

        CatLog.d(LOG_TAG, "initFromIntent - [" + mTextMsg + "], sim id: " + mSlotId);
    }
    private void cancelTimeOut() {
        CatLog.d(LOG_TAG, "cancelTimeOut: " + mSlotId);
        if (sOperatorSpec.equals("OP01")) {
            if (mTimeOutIntent != null) {
                CatLog.d(LOG_TAG, "mAlarmManager cancel");
                if (null !=  mAlarmManager) {
                    mAlarmManager.cancel(mTimeOutIntent);
                }
            }
        } else {
            if (null != mTimeoutHandler) {
                mTimeoutHandler.removeMessages(MSG_ID_TIMEOUT);
            }
        }
    }

    private void startTimeOut(boolean waitForUserToClear) {
        // Reset timeout.
        cancelTimeOut();
        int dialogDuration = StkApp.calculateDurationInMilis(mTextMsg.duration);
        // case 1  userClear = true & responseNeeded = false,
        // Dialog always exists.
        if (mTextMsg.userClear == true && mTextMsg.responseNeeded == false) {
            return;
        } else {
            // userClear = false. will dissapear after a while.
            if (dialogDuration == 0) {
                if (waitForUserToClear) {
                    dialogDuration = StkApp.DISP_TEXT_WAIT_FOR_USER_TIMEOUT;
                } else {
                    dialogDuration = StkApp.DISP_TEXT_CLEAR_AFTER_DELAY_TIMEOUT;
                }
            }
            if (sOperatorSpec.equals("OP01")) {
                Intent timeoutIntent = new Intent(ACTION_DIALOG_TIMEOUT, null);
                mTimeOutIntent = PendingIntent.getBroadcast(mContext, 0, timeoutIntent, 0);
                CatLog.d(LOG_TAG, "mAlarmManager.set");
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                        + dialogDuration, mTimeOutIntent);
            } else {
                mTimeoutHandler.sendMessageDelayed(mTimeoutHandler
                    .obtainMessage(MSG_ID_TIMEOUT), dialogDuration);
            }
            CatLog.d(LOG_TAG, "startTimeOut: " + mSlotId);
        }
    }
    private void sendResponse(int resId, boolean confirmed) {
        if (mSlotId == -1) {
            CatLog.d(LOG_TAG, "sim id is invalid");
            return;
        }

        if (StkAppService.getInstance() == null) {
            CatLog.d(LOG_TAG, "Ignore response: id is " + resId);
            return;
        }

        CatLog.d(LOG_TAG, "sendResponse resID[" + resId + "] confirmed[" + confirmed + "]");

        mIsResponseSent = true;
        Bundle args = new Bundle();
        args.putInt(StkAppService.OPCODE, StkAppService.OP_RESPONSE);
        args.putInt(StkAppService.SLOT_ID, mSlotId);
        args.putInt(StkAppService.RES_ID, resId);
        args.putBoolean(StkAppService.CONFIRMATION, confirmed);
        startService(new Intent(this, StkAppService.class).putExtras(args));
    }

    /*Handler base on timeoutThread Looper.*/
    private class TimeoutHandler extends Handler {
        public TimeoutHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_ID_TIMEOUT:
                CatLog.d(LOG_TAG, "MSG_ID_TIMEOUT finish.");
                sendResponse(StkAppService.RES_ID_TIMEOUT);
                finish();
                break;
            }
        }
    }
    private class TimeoutReceiver extends BroadcastReceiver {
    
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            CatLog.d(LOG_TAG, "TimeoutReceiver onReceive");
            if (action.equals(ACTION_DIALOG_TIMEOUT)) {
                CatLog.d(LOG_TAG, "TimeoutReceiver handle timeout");
                sendResponse(StkAppService.RES_ID_TIMEOUT);
                finish();
            }
        }
    }    
    void showTextToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }
}
