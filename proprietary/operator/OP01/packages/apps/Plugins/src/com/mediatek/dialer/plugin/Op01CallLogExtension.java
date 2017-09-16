package com.mediatek.dialer.plugin;

import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;

import com.mediatek.common.PluginImpl;
import com.mediatek.dialer.ext.DefaultCallLogExtension;
import com.mediatek.dialer.ext.ICallLogExtension.ICallLogAction;
import com.mediatek.op01.plugin.R;

@PluginImpl(interfaceName="com.mediatek.dialer.ext.ICallLogExtension")
public class Op01CallLogExtension extends DefaultCallLogExtension {
    private static final String TAG = "Op01CallLogExtension";

    public static final int CALL_TYPE_ALL = -1;
    private boolean mAutoRejectedFilterMode = false;
    private OnMenuItemClickListener mAutoRejectMenuClickListener ;
    private int mAutoRejectMenuId = -1;
    private Activity mCallLogActivity;

    /**
     * for op01
     * @param context
     * @param fragment
     */
    @Override
    public void onCreateForCallLogFragment(Context context, ListFragment fragment) {
       mAutoRejectedFilterMode = false;
       Log.d(TAG, "onCreateForCallLogFragment set mAutoRejectedFilterMode false");
    }

    /**
     * for op01
     * called when host create menu, to add plug-in own menu here
     * @param menu
     * @param callLogAction callback plug-in need if things need to be done by host
     */
    @Override
    public void createCallLogMenu(Activity activity, Menu menu, HorizontalScrollView viewPagerTabs, ICallLogAction callLogAction) {
        Log.d(TAG, "createCallLogMenu");
        mCallLogActivity = activity;
        final Activity fCallLogActivity = activity;
        final ICallLogAction fCallLogAction = callLogAction;
        final HorizontalScrollView fViewPagerTabs = viewPagerTabs;
        try {
            final Context cont = mCallLogActivity.getApplicationContext().createPackageContext("com.mediatek.op01.plugin", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            //MenuItem autoRejectMenu = menu.add(cont.getText(R.string.call_log_auto_rejected_label));
            int index = menu.size();
            MenuItem autoRejectMenu = menu.add(Menu.NONE, index, index, cont.getText(R.string.call_log_auto_rejected_label));
            mAutoRejectMenuId = autoRejectMenu.getItemId();
            autoRejectMenu.setOnMenuItemClickListener(mAutoRejectMenuClickListener = new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Log.d(TAG, "Auto reject onMenuItemClick");
                    if (mCallLogActivity != null) {
                        mAutoRejectedFilterMode = true;
                    }
                    fCallLogAction.updateCallLogScreen();
                    fViewPagerTabs.removeAllViews();
                    Resources resources = cont.getResources();
                    int backgroundColor = resources.getColor(R.color.actionbar_background_color);
                    fViewPagerTabs.setBackgroundColor(backgroundColor);
                    TextView textView = new TextView(cont);
                    textView.setBackgroundColor(backgroundColor);
                    final String autoRejectTitle = cont.getString(R.string.call_log_auto_rejected_label);
                    textView.setText(autoRejectTitle);
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextColor(Color.WHITE);
                    fViewPagerTabs.addView(textView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                    return true;
                }
            });
        } catch (NameNotFoundException e) {
            Log.d(TAG, "no com.mediatek.op01.plugin packages");
        }
    }

    /**
     * for op01
     * @param context
     * @param menu
     */
    @Override
    public void prepareCallLogMenu(Menu menu) {
        Log.d(TAG, "prepareCallLogMenu");
        if (mCallLogActivity == null) {
            mAutoRejectedFilterMode = false;
        }
        Log.d(TAG, "isAutoRejectedFilterMode: " + mAutoRejectedFilterMode);
        if (mAutoRejectMenuId >= 0) {
            menu.findItem(mAutoRejectMenuId).setVisible(!mAutoRejectedFilterMode);
        }
    }

    /**
     * for op01
     * @param item
     */
    public boolean onHomeButtonClick(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        if (mCallLogActivity != null) {
            if (mAutoRejectedFilterMode && (item.getItemId() == android.R.id.home)) {
                mAutoRejectedFilterMode = false;
                final Intent intent = new Intent();
                intent.setClassName("com.android.dialer", "com.android.dialer.calllog.CallLogActivity");
                mCallLogActivity.startActivity(intent);
                mCallLogActivity.finish();
                return true;
            }
        } else {
            mAutoRejectedFilterMode = false;
        }
        return false;
    }

    /**
     * for op01
     * called when host press back key
     */
    @Override
    public void onBackPressed(ICallLogAction callLogAction) {
        Log.d(TAG, "onBackHandled");
        if (mCallLogActivity != null) {
            if (mAutoRejectedFilterMode) {
                mAutoRejectedFilterMode = false;
                final Intent intent = new Intent();
                intent.setClassName("com.android.dialer", "com.android.dialer.calllog.CallLogActivity");
                mCallLogActivity.startActivity(intent);
                mCallLogActivity.finish();
            } else {
                callLogAction.processBackPressed();
            }
        } else {
            mAutoRejectedFilterMode = false;
            callLogAction.processBackPressed();
        }
    }

    /**
     * for op01
     * @param typeFiler current query type
     * @param builder the query selection Stringbuilder
     */
    @Override
    public void appendQuerySelection(int typeFiler, StringBuilder builder, List<String> selectionArgs) {
        Log.d(TAG, "appendQuerySelection");
        if (mCallLogActivity == null) {
            mAutoRejectedFilterMode = false;
        }
        String strbuilder = null;
        if (CALL_TYPE_ALL == typeFiler && !mAutoRejectedFilterMode) {
            if (builder.length() > 0) {
                builder.append(" AND ");
            }
            strbuilder = Calls.TYPE + "!=" + Calls.AUTO_REJECT_TYPE;
            builder.append(strbuilder);
        }
        if (mAutoRejectedFilterMode) {
            Log.d(TAG, "selectionArgs1: " + selectionArgs);
            if (typeFiler > CALL_TYPE_ALL) {
                if (!selectionArgs.isEmpty()) {
                    selectionArgs.set(0, Integer.toString(Calls.AUTO_REJECT_TYPE));
                }
            } else if (typeFiler == CALL_TYPE_ALL) {
                selectionArgs.add(0, Integer.toString(Calls.AUTO_REJECT_TYPE));
            }
            Log.d(TAG, "selectionArgs2: " + selectionArgs);
            if (builder.length() > 0 && (builder.indexOf("(type = ?)") == -1)) {
                String strbuild = "(type = ?) AND ";
                builder.insert(0, strbuild);
            } else if (builder.length() == 0) {
                builder.append("(type = ?)");
            }
        }
        Log.d(TAG, "builder: " + builder);
    }
}
