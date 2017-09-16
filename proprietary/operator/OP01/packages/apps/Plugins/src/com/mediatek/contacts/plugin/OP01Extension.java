package com.mediatek.contacts.plugin;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Context;
import android.telephony.SubInfoRecord;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.text.method.DialerKeyListener;
import android.widget.EditText;

import com.mediatek.op01.plugin.R;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.contacts.ext.DefaultOp01Extension;
import com.mediatek.common.PluginImpl;

@PluginImpl(interfaceName="com.mediatek.contacts.ext.IOp01Extension")
public class OP01Extension extends DefaultOp01Extension {
    private static final String TAG = "OP01Extension";
    private Context mContext = null;
    private static Context mContextHost = null;
    private static final int MENU_SIM_STORAGE = 9999;

    //@Override
    //public void setMenuItem(MenuItem blockVoiceCallmenu, boolean mOptionsMenuOptions) {
    //    Log.i(TAG, "[setMenuItem]");
    //    blockVoiceCallmenu.setVisible(false);
    //}

    @Override
    public void registerHostContext(Context context) {
        mContextHost = context;
        try {
            mContext = context.createPackageContext("com.mediatek.op01.plugin", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            Log.d(TAG, "no com.mediatek.op01.plugin packages");
        }
    }

    @Override
    public void addOptionsMenu(Menu menu) {
        Log.i(TAG, "addOptionsMenu");
        MenuItem item = menu.findItem(MENU_SIM_STORAGE);
        List<SubInfoRecord> simInfos = SubscriptionManager.getActiveSubInfoList();
        if (item == null && simInfos != null && simInfos.size() > 0) {
            String string = mContext.getResources().getString(R.string.look_simstorage);
            menu.add(0, MENU_SIM_STORAGE, 0, string).setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        ShowSimCardStorageInfoTask.showSimCardStorageInfo(mContext);
                        return true;
                    }
            });
        }
    }

    public static class ShowSimCardStorageInfoTask extends AsyncTask<Void, Void, Void> {
        private static ShowSimCardStorageInfoTask sInstance = null;
        private boolean mIsCancelled = false;
        private boolean mIsException = false;
        private String mDlgContent = null;
        private Context mContext = null;

        public static void showSimCardStorageInfo(Context context) {
            Log.i(TAG, "[ShowSimCardStorageInfoTask]_beg");
            if (sInstance != null) {
                sInstance.cancel();
                sInstance = null;
            }
            sInstance = new ShowSimCardStorageInfoTask(context);
            sInstance.execute();
            Log.i(TAG, "[ShowSimCardStorageInfoTask]_end");
        }

        public ShowSimCardStorageInfoTask(Context context) {
            mContext = context;
            Log.i(TAG, "[ShowSimCardStorageInfoTask] onCreate()");
        }

        @Override
        protected Void doInBackground(Void... args) {
            Log.i(TAG, "[ShowSimCardStorageInfoTask]: doInBackground_beg");
            List<SubInfoRecord> simInfos = getSortedInsertedSimInfoList(SubscriptionManager.getActiveSubInfoList());
            Log.i(TAG, "[ShowSimCardStorageInfoTask]: simInfos.size = " + simInfos.size());
            if (!mIsCancelled && (simInfos != null) && simInfos.size() > 0) {
                StringBuilder build = new StringBuilder();
                int simId = 0;
                for (SubInfoRecord simInfo : simInfos) {
                    if (simId > 0) {
                        build.append("\n\n");
                    }
                    simId++;
                    int[] storageInfos = null;
                    Log.i(TAG, "[ShowSimCardStorageInfoTask] simName = " + simInfo.displayName
                            + "; simSlot = " + simInfo.slotId + "; simId = " + simInfo.subId);
                    build.append(simInfo.displayName);
                    build.append(":\n");
                    try {
                        ITelephonyEx phoneEx = ITelephonyEx.Stub.asInterface(ServiceManager
                              .checkService("phoneEx"));
                        if (!mIsCancelled && phoneEx != null) {
                            storageInfos = phoneEx.getAdnStorageInfo(simInfo.subId);
                            if (storageInfos == null) {
                                mIsException = true;
                                Log.i(TAG, " storageInfos is null");
                                return null;
                            }
                            Log.i(TAG, "[ShowSimCardStorageInfoTask] infos: "
                                    + storageInfos.toString());
                        } else {
                            Log.i(TAG, "[ShowSimCardStorageInfoTask]: phone = null");
                            mIsException = true;
                            return null;
                        }
                    } catch (RemoteException ex) {
                        Log.i(TAG, "[ShowSimCardStorageInfoTask]_exception: " + ex);
                        mIsException = true;
                        return null;
                    }
                    Log.i(TAG, "slotId:" + simInfo.slotId + "||storage:"
                            + (storageInfos == null ? "NULL" : storageInfos[1]) + "||used:"
                            + (storageInfos == null ? "NULL" : storageInfos[0]));
                    build.append(mContext.getResources().getString(R.string.dlg_simstorage_content,
                            storageInfos[1], storageInfos[0]));
                    if (mIsCancelled) {
                        return null;
                    }
                }
                mDlgContent = build.toString();
            }
            Log.i(TAG, "[ShowSimCardStorageInfoTask]: doInBackground_end");
            return null;
        }

        public void cancel() {
            super.cancel(true);
            mIsCancelled = true;
            Log.i(TAG, "[ShowSimCardStorageInfoTask]: mIsCancelled = true");
        }

        @Override
        protected void onPostExecute(Void v) {
            if (mContextHost instanceof Activity) {
                Log.i(TAG, "[onPostExecute]: activity find");
                Activity activity = (Activity) mContextHost;
                if (activity.isFinishing()) {
                    Log.i(TAG, "[onPostExecute]: activity finish");
                    mIsCancelled = false;
                    mIsException = false;
                    sInstance = null;
                    return;
                }
            }

            Drawable icon = mContext.getResources().getDrawable(R.drawable.ic_menu_look_simstorage_holo_light);
            String string = mContext.getResources().getString(R.string.look_simstorage);
            sInstance = null;
            if (!mIsCancelled && !mIsException) {
                new AlertDialog.Builder(mContextHost).setIcon(icon).setTitle(string).setMessage(mDlgContent).setPositiveButton(
                       android.R.string.ok, null).setCancelable(true).create().show();
            }
            mIsCancelled = false;
            mIsException = false;
        }

        public List<SubInfoRecord> getSortedInsertedSimInfoList(List<SubInfoRecord> ls) {
            Collections.sort(ls, new Comparator<SubInfoRecord>() {
                @Override
                public int compare(SubInfoRecord arg0, SubInfoRecord arg1) {
                    return (arg0.slotId - arg1.slotId);
                }
            });
            return ls;
        }
    }

    @Override
    public int getMultiChoiceLimitCount(int defaultCount) {
        Log.i(TAG, "[getMultiChoiceLimitCount]");
        return 5000;
    }

    @Override
    public void setViewKeyListener(EditText fieldView) {
        Log.i(TAG, "[setViewKeyListener] fieldView : " + fieldView);
        if (fieldView != null) {
            fieldView.setKeyListener(SIMKeyListener.getInstance());
        } else {
            Log.e(TAG, "[setViewKeyListener]fieldView is null");
        }
    }

    public static class SIMKeyListener extends DialerKeyListener {
        private static SIMKeyListener sKeyListener;
        /**
         * The characters that are used.
         *
         * @see KeyEvent#getMatch
         * @see #getAcceptedChars
         */
        public static final char[] CHARACTERS = new char[] { '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', '+', '*', '#', 'P', 'W', 'p', 'w', ',', ';'};

        @Override
        protected char[] getAcceptedChars() {
            return CHARACTERS;
        }

        public static SIMKeyListener getInstance() {
            if (sKeyListener == null) {
                sKeyListener = new SIMKeyListener();
            }
            return sKeyListener;
        }

    }
}
