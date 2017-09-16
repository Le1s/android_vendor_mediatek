package com.mediatek.settings.plugin;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.RemoteException;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneRatFamily;
import android.telephony.SubInfoRecord;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.TelephonyIntents;
import com.mediatek.common.PluginImpl;
import com.mediatek.widget.AccountViewAdapter.AccountElements;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.settings.ext.DefaultSimManagementExt;
import com.mediatek.op01.plugin.R;
import com.mediatek.xlog.Xlog;

import java.util.List;

@PluginImpl(interfaceName="com.mediatek.settings.ext.ISimManagementExt")
public class Op01SimManagementExt extends DefaultSimManagementExt {

    private static final String TAG = "OP01SimManagementExt";

    private static final String KEY_3G_SERVICE_SETTING = "3g_service_settings";
    private static final String KEY_AUTO_WAP_PUSH = "wap_push_settings";
    private static final String KEY_SIM_STATUS = "status_info";

    private Context mContext;
    PreferenceFragment mPrefFragment;
    private AlertDialog mAlertDlg;
    private ProgressDialog mWaitDlg;
    private boolean mIsDataSwitchWaiting = false;
    private int mToCloseSlot = -1;

    private static final int DATA_SWITCH_TIME_OUT_MSG = 2000;
    private static final int DATA_SWITCH_TIME_OUT_TIME = 10000;
    private IntentFilter mIntentFilter;
    
    // Subinfo record change listener.
    private BroadcastReceiver mSubReceiver = new BroadcastReceiver() {
    
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Xlog.d(TAG, "mSubReceiver action = " + action);
                if (action.equals(TelephonyIntents.ACTION_SUBINFO_CONTENT_CHANGE)
                        || action.equals(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED)) {
                        long[] subids = SubscriptionManager.getActiveSubIdList();
                        if (subids == null || subids.length <= 1) {
                            if(mAlertDlg != null && mAlertDlg.isShowing()) {
                                Xlog.d(TAG, "onReceive dealWithDataConnChanged dismiss AlertDlg"); 
                                mAlertDlg.dismiss();
                                mToCloseSlot = -1;
                                if (mPrefFragment != null) {
                                    mPrefFragment.getActivity().unregisterReceiver(mSubReceiver);
                                }
                            }    
                        }
        
                }
            }
    };

    //Timeout handler
    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (DATA_SWITCH_TIME_OUT_MSG == msg.what) {

                Xlog.i(TAG, "reveive time out msg...");
                if (mIsDataSwitchWaiting) {

                    mTimerHandler.removeMessages(DATA_SWITCH_TIME_OUT_MSG);
                    if (mWaitDlg.isShowing()) {
                        mWaitDlg.dismiss();
                    }
                    mIsDataSwitchWaiting = false;
                }
            }
        }
    };

    /**
     * update the preference screen of sim management
     * @param parent parent preference
     */
    public Op01SimManagementExt(Context context) {
        super();
        mContext = context;
    }

    public void updateSimManagementPref(PreferenceGroup parent) {

        Xlog.d(TAG, "updateSimManagementPref()");
        PreferenceScreen pref3GService = null;
        PreferenceScreen prefWapPush = null;
        PreferenceScreen prefStatus = null;
        if (parent != null) {
            pref3GService = (PreferenceScreen) parent.findPreference(KEY_3G_SERVICE_SETTING);
            prefWapPush = (PreferenceScreen) parent.findPreference(KEY_AUTO_WAP_PUSH);
            prefStatus = (PreferenceScreen) parent.findPreference(KEY_SIM_STATUS);
        }
        if (pref3GService != null) {
            Xlog.d(TAG, "updateSimManagementPref()---remove pref3GService");
            parent.removePreference(pref3GService);
        }
        if (prefWapPush != null) {
            Xlog.d(TAG, "updateSimManagementPref()---remove prefWapPush");
            parent.removePreference(prefWapPush);
        }
        if (prefStatus != null) {
            Xlog.d(TAG, "updateSimManagementPref()---remove prefStatus");
            parent.removePreference(prefStatus);
        }
    }

    public void updateSimEditorPref(PreferenceFragment pref) {
        pref.getPreferenceScreen().removePreference(pref.findPreference("sim_color"));
    }

    public void dealWithDataConnChanged(Intent intent, boolean isResumed) {

        Xlog.d(TAG, "dealWithDataConnChanged: mToClosedSimCard is " + mToCloseSlot);
        //remove confrm dialog
        long curConSubId = SubscriptionManager.getDefaultDataSubId();

        if (mToCloseSlot >= 0) {

            long toCloseSubId = getSubIdBySlot(mToCloseSlot);
            Xlog.i(TAG, "dealWithDataConnChanged: toCloseSimId is " + toCloseSubId);
            Xlog.i(TAG, "dealWithDataConnChanged: curConSimId is " + curConSubId);

            if (toCloseSubId != curConSubId) {
                if (mAlertDlg != null && mAlertDlg.isShowing() && isResumed) {
                    Xlog.d(TAG, "dealWithDataConnChanged dismiss AlertDlg");
                    mAlertDlg.dismiss();
                    if (mPrefFragment != null) {
                        mPrefFragment.getActivity().unregisterReceiver(mSubReceiver);
                        mToCloseSlot = -1;
                    }
                }
                mToCloseSlot = -1;
            }
        }
        //remove waiting dialog
        if (intent != null) {
            String apnTypeList = intent.getStringExtra(PhoneConstants.DATA_APN_TYPE_KEY);
            PhoneConstants.DataState state = getMobileDataState(intent);

            if ((state == PhoneConstants.DataState.CONNECTED)
                    || (state == PhoneConstants.DataState.DISCONNECTED)) {

                if ((PhoneConstants.APN_TYPE_DEFAULT.equals(apnTypeList))) {

                    if (mIsDataSwitchWaiting) {

                        mTimerHandler.removeMessages(DATA_SWITCH_TIME_OUT_MSG);
                        if (mWaitDlg.isShowing()) {
                            mWaitDlg.dismiss();
                        }
                        mIsDataSwitchWaiting = false;
                    }

                }
            }
        }
    }

    public void updateDefaultSIMSummary(DialogPreference pref, Long simId) {
        if (simId == Settings.System.SMS_SIM_SETTING_AUTO) {
            pref.setSummary(mContext.getString(R.string.gemini_default_sim_auto));
        }
    }

    public void customizeSmsChoiceArray(List<AccountElements> listItem) {
        int size = listItem.size();
        if (size > 1) {
            listItem.add(
                     	 new AccountElements(
                             mContext.getDrawable(R.drawable.mms_notification_auto_select),
                             mContext.getString(R.string.gemini_default_sim_auto),
                             null));
        }
    }

    public void customizeSmsChoiceValue(List<Object> listItemValue) {
        int size = listItemValue.size();
        if (size > 1) {
            listItemValue.add(Settings.System.SMS_SIM_SETTING_AUTO);
        }
    }

    public void showChangeDataConnDialog(PreferenceFragment prefFragment, boolean isResumed) {

        Xlog.d(TAG, "showChangeDataConnDialog");
        mPrefFragment = prefFragment;

        if (mToCloseSlot >= 0 && SubscriptionManager.getActiveSubInfoList().size() > 1) {

            long curConSubId = SubscriptionManager.getDefaultDataSubId();
            long toCloseSubId = getSubIdBySlot(mToCloseSlot);
            Xlog.d(TAG, "toCloseSimId= " + toCloseSubId + "curConSimId= " + curConSubId);            
            if (mAlertDlg != null && mAlertDlg.isShowing()) {
                Xlog.d(TAG, "mAlertDlg.isShowing(), return");
                return;
            }

            if (toCloseSubId == curConSubId && isResumed) {

                // Add receiver to listen hot swap intent.
                /*mIntentFilter = new IntentFilter(TelephonyIntents.ACTION_SUBINFO_CONTENT_CHANGE);
                prefFragment.getActivity().registerReceiver(mSubReceiver, mIntentFilter);
                Builder builder = new AlertDialog.Builder(prefFragment.getActivity());
                mAlertDlg = getChangeDataConnDialog(builder);
                if (mAlertDlg != null) {
                    mAlertDlg.show();
                }*/
                Intent i = new Intent();
                i.setClassName(mContext.getPackageName(), SimMgrChangeConnDialog.class.getName());
                
                i.putExtra("slotId", mToCloseSlot);
                Xlog.d(TAG, "put intent slotId " + mToCloseSlot);
                prefFragment.getActivity().startActivity(i);    
                mToCloseSlot = -1;
            }
        }

    }

    public void setToClosedSimSlot(int simSlot) {
        Xlog.d(TAG, "setToClosedSimSlot = " + simSlot);
        mToCloseSlot = simSlot;
        if (mToCloseSlot >= 0 && SubscriptionManager.getActiveSubInfoList().size() > 1) {


            TelecomManager telecomMgr = TelecomManager.from(mContext);
            PhoneAccountHandle handle = telecomMgr.getUserSelectedOutgoingPhoneAccount();
            
            if (handle == null || handle.getId() == null || handle.getId().equals("")) {
                Xlog.d(TAG, "setToClosedSimSlot current not valid subid, return");
                return;
            }
            long curVoiceSubId = Long.parseLong(handle.getId());
            long toCloseSubId = getSubIdBySlot(mToCloseSlot);
            Xlog.d(TAG, "setToClosedSimSlot curVoiceSubId = " + curVoiceSubId);
            
            if (toCloseSubId == curVoiceSubId && toCloseSubId >= 0) {
                long subid = getSubIdBySlot(1 - mToCloseSlot);
                if (subid >= 0) {
                    switchVoiceCallDefaultSim(subid);
                }
            }
        }
    }

    private AlertDialog getChangeDataConnDialog(Builder builder) {

        Xlog.d(TAG, "getChangeDataConnDialog");

        SubInfoRecord currentSiminfo =
               SubscriptionManager.getSubInfoUsingSlotId(mToCloseSlot).get(0);

        SubInfoRecord anotherSiminfo =
               SubscriptionManager.getSubInfoUsingSlotId(1 - mToCloseSlot).get(0);

        String currentSimName = currentSiminfo.displayName;
        String anotherSimName = anotherSiminfo.displayName;

        if (currentSiminfo == null || anotherSiminfo == null) {
            return null;
        }
        if (currentSimName == null) {
            currentSimName = "SIM " + (currentSiminfo.slotId + 1);
        }
        if (anotherSimName == null) {
            anotherSimName = "SIM " + (anotherSiminfo.slotId + 1);
        }
        Xlog.d(TAG, "currentSimName:" + currentSimName + "\n anotherSimName:" + anotherSimName);

        int currSimResId = currentSiminfo.simIconRes[0];
        int otherSimResId = anotherSiminfo.simIconRes[0];
        int currentSimColor = getDrawableColorValue(currSimResId);
        int anotherSimColor = getDrawableColorValue(otherSimResId);

        Xlog.d(TAG, "mToClosedSimCard = " + mToCloseSlot);

        String message = String.format(currentSimName +
               mContext.getString(R.string.change_data_conn_message) +
               anotherSimName +
               "?");

        int currentSimStartIdx = message.indexOf(currentSimName);
        int currentSimEndIdx = currentSimStartIdx + currentSimName.length();

        int anotherSimStartIdx = currentSimEndIdx
                + (mContext.getString(R.string.change_data_conn_message)).length();
        int anotherSimEndIdx = anotherSimStartIdx + anotherSimName.length();

        SpannableStringBuilder style = new SpannableStringBuilder(message);

        style.setSpan(new BackgroundColorSpan(currentSimColor), currentSimStartIdx,
               currentSimEndIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        style.setSpan(new BackgroundColorSpan(anotherSimColor), anotherSimStartIdx,
               anotherSimEndIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setTitle(mContext.getString(R.string.change_data_conn_title));
        builder.setMessage(style);

        builder.setPositiveButton(android.R.string.yes,
               new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int whichButton) {
                           Xlog.d(TAG, "Perform On click ok");
                           long subid = getSubIdBySlot(1 - mToCloseSlot);
                           Xlog.d(TAG, "Auto Switch GPRS Sim id = " + subid);
                           mToCloseSlot = -1;
                           switchGprsDefautSIM(subid);
                           if (mPrefFragment != null) {
                               mPrefFragment.getActivity().unregisterReceiver(mSubReceiver);
                           }
                       }
                       });

        builder.setNegativeButton(android.R.string.cancel,
               new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int whichButton) {
                           Xlog.d(TAG, "Perform On click cancel");
                           mToCloseSlot = -1;
                           if (mPrefFragment != null) {
                               mPrefFragment.getActivity().unregisterReceiver(mSubReceiver);
                           }
                       }
                       });

        return builder.create();

    }

    private long getSubIdBySlot(int slotId) {
        Xlog.d(TAG, "SlotId = " + slotId);
        if (slotId < 0 || slotId > 1) {
            return -1;
        }
        long[] subids = SubscriptionManager.getSubId(slotId);
        long subid = -1;
        if (subids != null && subids.length >= 1) {
            subid = subids[0];
        }
        Xlog.d(TAG, "GetSimIdBySlot: sub id = " + subid 
                + "sim Slot = " + slotId);
        return subid;
    }

    private void switchGprsDefautSIM(long subid) {

        Xlog.d(TAG, "switchGprsDefautSIM() with simid=" + subid);

        if (subid < 0) {
            return;
        }
        long curConSubId = SubscriptionManager.getDefaultDataSubId();
        Xlog.d(TAG,"curConSimId=" + curConSubId);

        if (subid == curConSubId) {
            return;
        }
        SubscriptionManager.setDefaultDataSubId(subid);
        handleSimSwitch(subid);
        showDataConnWaitDialog();

    }

    private void switchVoiceCallDefaultSim(long subid) {
        Xlog.d(TAG, "switchVoiceCallDefaultSim() with subid=" + subid);
        if(subid < 0) {
            return;
        }
    
        TelecomManager telecomMgr = TelecomManager.from(mContext);
        List<PhoneAccountHandle> allHandles = telecomMgr.getAllPhoneAccountHandles();
       
        for (PhoneAccountHandle handle : allHandles) {
            Xlog.d(TAG, "switchVoiceCallDefaultSim() PhoneAccountHandle id=" + handle.getId());
            if (handle.getId() == null) {
                continue;
            } else if (handle.getId().equals(String.valueOf(subid))) {
                Xlog.d(TAG, "switch voice call to subid=" + subid);
                telecomMgr.setUserSelectedOutgoingPhoneAccount(handle);
                break;
            }

        }

        Xlog.d(TAG, "switchVoiceCallDefaultSim() ==>end");
    }

    private void showDataConnWaitDialog() {

        mTimerHandler.removeMessages(DATA_SWITCH_TIME_OUT_MSG);
        mTimerHandler.sendEmptyMessageDelayed(DATA_SWITCH_TIME_OUT_MSG,
                DATA_SWITCH_TIME_OUT_TIME);

        mWaitDlg = new ProgressDialog(mPrefFragment.getActivity());
        mWaitDlg.setMessage(mContext.getString(R.string.change_data_conn_progress_message));
        mWaitDlg.setIndeterminate(true);
        mWaitDlg.setCancelable(false);
        mWaitDlg.show();

        mIsDataSwitchWaiting = true;
    }

    private int getDrawableColorValue(int resId) {
        Xlog.i(TAG, "getDrawableColorValue");
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int color = bitmap.getPixel(width / 2, height / 2);
        return color;
    }

    private PhoneConstants.DataState getMobileDataState(Intent intent) {

        String str = intent.getStringExtra(PhoneConstants.STATE_KEY);

        if (str != null) {
            return Enum.valueOf(PhoneConstants.DataState.class, str);
        } else {
            return PhoneConstants.DataState.DISCONNECTED;
        }
    }

    private void handleSimSwitch(long subId) {
        TelephonyManager tm = TelephonyManager.from(mPrefFragment.getActivity());
        int count = tm.getPhoneCount();
        PhoneRatFamily[] rats = new PhoneRatFamily[count];
        Xlog.d(TAG, "handleSimSwitch()... " + count);
        for (int i = 0; i < rats.length; i++) {
            if (SubscriptionManager.getPhoneId(subId) == i) {
                if (SystemProperties.get("ro.mtk_lte_support").equals("1")) {
                    rats[i] = new PhoneRatFamily(i, PhoneRatFamily.PHONE_RAT_FAMILY_3G |
                            PhoneRatFamily.PHONE_RAT_FAMILY_4G);
                } else {
                    rats[i] = new PhoneRatFamily(i, PhoneRatFamily.PHONE_RAT_FAMILY_3G);
                }
            } else {
                rats[i] = new PhoneRatFamily(i, PhoneRatFamily.PHONE_RAT_FAMILY_2G);

            }
            Xlog.d(TAG, "handleSimSwitch()... rat[" + i + "]: " + rats[i]);
        }
        try {
            setStatusBarEnableStatus(false);
            ITelephony tel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
            tel.setPhoneRat(rats);
        } catch (RemoteException e) {
            Xlog.d(TAG, "handleSimSwitch fail");
            e.printStackTrace();
        }
    }

    private void setStatusBarEnableStatus(boolean enabled) {
        Xlog.i(TAG, "setStatusBarEnableStatus(" + enabled + ")");
        StatusBarManager statusMgr = (StatusBarManager) mPrefFragment.getActivity().getSystemService(Context.STATUS_BAR_SERVICE);
        if (statusMgr != null) {
            if (enabled) {
                statusMgr.disable(StatusBarManager.DISABLE_NONE);
            } else {
                statusMgr.disable(StatusBarManager.DISABLE_EXPAND |
                        StatusBarManager.DISABLE_RECENT |
                        StatusBarManager.DISABLE_HOME);
            }
        } else {
            Xlog.e(TAG, "Fail to get status bar instance");
        }
    }
    
}
