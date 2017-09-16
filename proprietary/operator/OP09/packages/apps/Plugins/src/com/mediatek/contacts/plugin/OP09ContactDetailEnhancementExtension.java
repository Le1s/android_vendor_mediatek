package com.mediatek.contacts.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.mediatek.op09.plugin.R;

import com.mediatek.contacts.ext.DefaultContactDetailEnhancementExtension;
import com.mediatek.contacts.plugin.util.OP09ContactsDetailCallColor;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;

public class OP09ContactDetailEnhancementExtension extends DefaultContactDetailEnhancementExtension {
    private static final String TAG = "ContactDetailEnhancementExtension";

    private Context mPluginContext;

    public DetailUIController mDetailUIController;
    public int mVal;
    public int mLayoutMode;
    public ActionBar mActionBar;
    private LayoutInflater mLayoutInflater;
    private Fragment mHistoryFragment;

    private static final int TWO_COLUMN = 0;
    /**
     * Tall and narrow screen to allow swipe between the details and updates.
     */
    private static final int VIEW_PAGER_AND_TAB_CAROUSEL = 1;
    /**
     * Short and wide screen to allow part of the other page to show.
     */
    private static final int FRAGMENT_CAROUSEL = 2;
    /**
     * Same as FRAGMENT_CAROUSEL (allowing part of the other page to show) except the details
     * layout is similar to the details layout in TWO_COLUMN mode.
     */
    private static final int TWO_COLUMN_FRAGMENT_CAROUSEL = 3;

    private static final int TAB_INDEX_DETAIL = 0;
    private static final int DETAIL_HISTORY_INDEX = 1;

    private List<CallCorGView> callViews = new ArrayList<CallCorGView>();

    public OP09ContactDetailEnhancementExtension(Context context) {
        mPluginContext = context;
    }


    @Override
    public boolean isUseOperation() {
        return false;
    }

    @Override
    public Drawable getDrawableCorG(SimInfoRecord simInfo) {
        int cOrGId = OP09ContactsDetailCallColor.getInstance().getDrawableCorG(
                simInfo);
        return mPluginContext.getResources().getDrawable(cOrGId);
    }

    @Override
    public boolean bindDetailEnhancementViewForQuickContact(final Activity activity,
            final String number, ImageView firstActionViewButton,
            View firstDivider, ImageView btnVtCallAction, int mInsertedSimCount) {

        final ContentResolver contentResolver = activity.getContentResolver();
        SimInfoRecord defaultSimInfo = OP09ContactsDetailCallColor.getInstance()
                .getDefaultSiminfo(contentResolver);
        if (defaultSimInfo == null) {
            firstActionViewButton.setVisibility(View.GONE);
            firstDivider.setVisibility(View.GONE);
            btnVtCallAction.setVisibility(View.VISIBLE);
            btnVtCallAction.setImageDrawable(mPluginContext.getResources().getDrawable(R.drawable.ic_contact_detail_call_disable));
            return true;
        }
        int callDefault = OP09ContactsDetailCallColor.getInstance().getDrawableCorG(defaultSimInfo);
        firstActionViewButton.setImageDrawable(mPluginContext.getResources().getDrawable(callDefault));
        firstActionViewButton.setVisibility(View.VISIBLE);
        firstActionViewButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                        OP09ContactsDetailCallColor.getInstance().getCallUri(
                                number));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("com.android.phone.extra.slot", // Constants.EXTRA_SLOT_ID
                        OP09ContactsDetailCallColor.getInstance()
                                .getDefaultSlot(contentResolver));
                activity.startActivity(intent);
            }
        });

        firstDivider.setVisibility(View.VISIBLE);

        SimInfoRecord notDefaultSimInfo = OP09ContactsDetailCallColor.getInstance().getNotDefaultSiminfo(contentResolver);
        if (notDefaultSimInfo != null) {
            int callNotDefault = OP09ContactsDetailCallColor.getInstance().getDrawableCorG(notDefaultSimInfo);
            btnVtCallAction.setImageDrawable(mPluginContext.getResources().getDrawable(callNotDefault));
            btnVtCallAction.setVisibility(View.VISIBLE);
            btnVtCallAction.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    final Intent intent = new Intent(
                            Intent.ACTION_CALL_PRIVILEGED,
                            OP09ContactsDetailCallColor.getInstance()
                                    .getCallUri(number));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("com.android.phone.extra.slot", // Constants.EXTRA_SLOT_ID
                            OP09ContactsDetailCallColor.getInstance()
                                    .getNotDefaultSlot(contentResolver));
                    activity.startActivity(intent);
                    }
            });
        }

        if (mInsertedSimCount == 1) {
            firstActionViewButton.setVisibility(View.GONE);
            firstDivider.setVisibility(View.GONE);
            btnVtCallAction.setVisibility(View.VISIBLE);
            btnVtCallAction.setImageDrawable(mPluginContext.getResources().getDrawable(callDefault));
        }

        return true;
    }

    @Override
    public boolean bindDetailEnhancementView(final Activity activity,
            final String number, View firstActionViewContainer,
            final ImageView firstActionButtonView, View vewVtCallDivider,
            ImageView btnVtCallAction, View vtcallActionViewContainer,
            int visibility, String secondaryActionDescription,
            int mInsertedSimCount) {

        final ContentResolver contentResolver = activity.getContentResolver();
        SimInfoRecord defaultSimInfo = OP09ContactsDetailCallColor.getInstance()
                .getDefaultSiminfo(contentResolver);
        if (defaultSimInfo == null) {
            firstActionViewContainer.setVisibility(View.GONE);
            vewVtCallDivider.setVisibility(View.GONE);
            btnVtCallAction.setVisibility(visibility);
            vtcallActionViewContainer.setVisibility(visibility);
            btnVtCallAction.setImageDrawable(mPluginContext.getResources().getDrawable(R.drawable.ic_contact_detail_call_disable));
            return true;
        }
        int callDefault = OP09ContactsDetailCallColor.getInstance().getDrawableCorG(defaultSimInfo);
        Drawable firstActionIcon = mPluginContext.getResources().getDrawable(callDefault);
        firstActionButtonView.setImageDrawable(firstActionIcon);
        firstActionButtonView.setContentDescription(secondaryActionDescription);
//        firstActionViewContainer.setTag(entry);
        firstActionViewContainer.setVisibility(View.VISIBLE);
        firstActionViewContainer.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, OP09ContactsDetailCallColor
                        .getInstance().getCallUri(number));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("com.android.phone.extra.slot", // Constants.EXTRA_SLOT_ID
                        OP09ContactsDetailCallColor.getInstance()
                                .getDefaultSlot(contentResolver));
                activity.startActivity(intent);
            }
        });

        SimInfoRecord notDefaultSimInfo = OP09ContactsDetailCallColor.getInstance()
                .getNotDefaultSiminfo(contentResolver);
        if (notDefaultSimInfo != null) {
            int callNotDefault = OP09ContactsDetailCallColor.getInstance()
                    .getDrawableCorG(notDefaultSimInfo);
            btnVtCallAction.setImageDrawable(mPluginContext.getResources()
                    .getDrawable(callNotDefault));
            vewVtCallDivider.setVisibility(visibility);
            btnVtCallAction.setVisibility(visibility);
            vtcallActionViewContainer.setVisibility(visibility);
            vtcallActionViewContainer.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    final Intent intent = new Intent(
                            Intent.ACTION_CALL_PRIVILEGED,
                            OP09ContactsDetailCallColor.getInstance().getCallUri(
                                    number));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("com.android.phone.extra.slot", // Constants.EXTRA_SLOT_ID
                            OP09ContactsDetailCallColor.getInstance()
                                    .getNotDefaultSlot(contentResolver));
                    activity.startActivity(intent);
                }
            });
        }
        if (mInsertedSimCount == 1) {
            firstActionViewContainer.setVisibility(View.GONE);
            vewVtCallDivider.setVisibility(View.GONE);
            btnVtCallAction.setVisibility(visibility);
            vtcallActionViewContainer.setVisibility(visibility);
            btnVtCallAction.setImageDrawable(mPluginContext.getResources()
                    .getDrawable(callDefault));

        }

        return true;
    }

    @Override
    public Drawable getEnhancementAccountSimIndicator(int i, int slot) {
        return OP09ContactsDetailCallColor.getInstance()
                .getEnhancementAccountSimIndicator(i, slot, mPluginContext);
    }

    @Override
    public long getEnhancementPhotoId(int isSdnContact, int colorId, int slot) {
        return OP09ContactsDetailCallColor.getInstance().getEnhancementPhotoId(
                isSdnContact, colorId, slot);
    }

    @Override
    public String getEnhancementPhotoUri(int isSdnContact, int colorId, int slot) {
        return OP09ContactsDetailCallColor.getInstance().getEnhancementPhotoUri(
                isSdnContact, colorId, slot);
    }

    @Override
    public BroadcastReceiver registeChangeDefaultSim(Activity activity,
            BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
        activity.registerReceiver(broadcastReceiver, intentFilter);
        return broadcastReceiver;
    }

    public boolean changeDualCallButton(List<CallCorGView> callViews,
            Activity activity) {
        SimInfoRecord defaultSimInfo = OP09ContactsDetailCallColor.getInstance()
                .getDefaultSiminfo((activity.getContentResolver()));
        int callDefault = OP09ContactsDetailCallColor.getInstance()
                .getDrawableCorG(defaultSimInfo);
        SimInfoRecord notDefaultSimInfo = OP09ContactsDetailCallColor.getInstance()
                .getNotDefaultSiminfo((activity.getContentResolver()));
        int callNotDefault = OP09ContactsDetailCallColor.getInstance()
                .getDrawableCorG(notDefaultSimInfo);
        for (Iterator<CallCorGView> iterator = callViews.iterator(); iterator
                .hasNext(); ) {
            CallCorGView item = (CallCorGView) iterator.next();
            item.getFirstCallButton().setImageDrawable(
                    activity.getResources().getDrawable(callDefault));
            item.getSecondCallButton().setImageDrawable(
                    activity.getResources().getDrawable(callNotDefault));
        }

        return true;
    }

    private class CallCorGView {
        private ImageView firstCallButton;
        private View firstCallView;
        private View firstCallDivider;
        private ImageView secondCallButton;
        private View secondCallView;
        private View secondCallDivider;


        public ImageView getFirstCallButton() {
            return firstCallButton;
        }
        public void setFirstCallButton(ImageView firstCallButton) {
            this.firstCallButton = firstCallButton;
        }
        public ImageView getSecondCallButton() {
            return secondCallButton;
        }
        public void setSecondCallButton(ImageView secondCallButton) {
            this.secondCallButton = secondCallButton;
        }
        public View getFirstCallView() {
            return firstCallView;
        }
        public void setFirstCallView(View firstCallView) {
            this.firstCallView = firstCallView;
        }
        public View getFirstCallDivider() {
            return firstCallDivider;
        }
        public void setFirstCallDivider(View firstCallDivider) {
            this.firstCallDivider = firstCallDivider;
        }
        public View getSecondCallView() {
            return secondCallView;
        }
        public void setSecondCallView(View secondCallView) {
            this.secondCallView = secondCallView;
        }
        public View getSecondCallDivider() {
            return secondCallDivider;
        }
        public void setSecondCallDivider(View secondCallDivider) {
            this.secondCallDivider = secondCallDivider;
        }
    }

    @Override
    public void configActionBarExt(ActionBar bar) {

        int flag = ActionBar.DISPLAY_SHOW_HOME ^ ActionBar.DISPLAY_USE_LOGO;
        int change = bar.getDisplayOptions() ^ flag;
        bar.setDisplayOptions(change, flag);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    @Override
    public void initActionBarExt(ActionBar bar, boolean withUpdatePage) {
        Log.i(TAG, "==sDetailUIController : " + mDetailUIController);
        mActionBar = bar;
        int tabCount = bar.getTabCount();
        int expectTabCount = withUpdatePage ? 2 : 3;
        if (tabCount == expectTabCount) {
            return;
        }
        if (tabCount > 0) {
            bar.removeAllTabs();
        }
        ActionBar.Tab tab = bar.newTab();
        tab.setText(mPluginContext.getResources().getString(R.string.contact_detail_detail));
        tab.setTabListener(mTabListener);
        bar.addTab(tab, true);

        tab = bar.newTab();
        tab.setText(mPluginContext.getResources().getString(R.string.contact_detail_history));
        tab.setTabListener(mTabListener);
        bar.addTab(tab);

        if (withUpdatePage) {
            tab = bar.newTab();
            tab.setText(mPluginContext.getResources().getString(R.string.contact_detail_update));
            tab.setTabListener(mTabListener);
            bar.addTab(tab);
        }
    }

    private ActionBar.TabListener mTabListener = new ActionBar.TabListener() {

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            int position = tab.getPosition();
            switchTab(position);
            tab.select();
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {

        }
    };



    @Override
    public void onFragmentPageChange(int index) {

        mDetailUIController.onFragmentPageChange(mActionBar, index);
    }

    protected void switchTab(int position) {
        int updateIndex = getDetailUpdateIndex();

        switch (mLayoutMode) {
            case VIEW_PAGER_AND_TAB_CAROUSEL: {
                if (position == 0) {
                    mDetailUIController.requestScrollTab(0, 0);
                    mDetailUIController.requestSwitchViewPager(TAB_INDEX_DETAIL);
                } else if (position == updateIndex) {
                    mDetailUIController.requestScrollTab(mVal * updateIndex, 0);
                    mDetailUIController.requestSwitchViewPager(updateIndex);
                } else {
                    mDetailUIController.requestScrollTab(mVal * DETAIL_HISTORY_INDEX, 0);
                    mDetailUIController.requestSwitchViewPager(DETAIL_HISTORY_INDEX);
                }
                break;
            }
            case FRAGMENT_CAROUSEL: {
                if (position == 0) {
                    mDetailUIController.requestScrollFragment(0, 0);
                    mDetailUIController.requestSwitchFragment(TAB_INDEX_DETAIL);
                } else if (position == updateIndex) {
                    mDetailUIController.requestScrollFragment(mVal * updateIndex, 0);
                    mDetailUIController.requestSwitchFragment(updateIndex);
                } else {
                    mDetailUIController.requestScrollFragment(mVal * DETAIL_HISTORY_INDEX, 0);
                    mDetailUIController.requestSwitchFragment(DETAIL_HISTORY_INDEX);
                }
                break;
            }
        }
    }

    @Override
    public int getDetailUpdateIndex() {
        return 2;
    }

    @Override
    public void registerDetailUIController(DetailUIController detailUIController, int val,
            int mLayoutMode) {
        Log.i(TAG, "--registerDetailUIController");
        mDetailUIController = detailUIController;
        mVal = val;
        this.mLayoutMode = mLayoutMode;
        mLayoutInflater = (LayoutInflater) mPluginContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void setViewPagerCurrentItemEx(ViewPager viewPager, int mCurrentPageIndex,
            boolean smoothScroll) {
        viewPager.setCurrentItem(mCurrentPageIndex, false /* smooth transition */);

    }

    @Override
    public int getMaxFragmentViewCountEx(int maxFragmentViewCount) {

        return 3;
    }

    @Override
    public int getVisibleFragmentViewCountEx(boolean enableSwipe) {

        return enableSwipe ? 3 : 2;

    }

    @Override
    public boolean onTouchEx() {

        return false;
    }

    @Override
    public boolean onScrollChangedEx() {

        return false;
    }

    @Override
    public MeasureInfo onMeasureEx(int screenWidth, int heightMeasureSpec,
            int fragmentViewCount, float fragmentWidthScreenWidthFraction) {

        MeasureInfo measureInfo = new MeasureInfo();
        measureInfo.mMinFragmentWidth = (int) (fragmentWidthScreenWidthFraction * screenWidth);
        measureInfo.mAllowedHorizontalScrollLength = (fragmentViewCount * measureInfo.mMinFragmentWidth)
                -
                screenWidth * (fragmentViewCount * measureInfo.mMinFragmentWidth / screenWidth - 1);
        measureInfo.mLowerThreshold = (screenWidth - measureInfo.mMinFragmentWidth)
                / fragmentViewCount;
        measureInfo.mUpperThreshold = measureInfo.mAllowedHorizontalScrollLength
                - measureInfo.mLowerThreshold;

        Log.i(TAG, "mMinFragmentWidth:" + measureInfo.mMinFragmentWidth
                + "|mAllowedHorizontalScrollLength:" + measureInfo.mAllowedHorizontalScrollLength
                + "|mLowerThreshold:" + measureInfo.mLowerThreshold
                + "|mUpperThreshold:" + measureInfo.mUpperThreshold);

        return measureInfo;

    }

    @Override
    public void childMeasureEx(boolean mEnableSwipe, View child, int mMinFragmentWidth,
            int visibleFragmentViewCount, int screenHeight, int screenWidth) {
        /**
        * M: replace MAX_FRAGMENT_VIEW_COUNT to variable fragmentViewCount
        *  // If we enable swipe, then the {@link LinearLayout} child width
        *  // must be the sum of the width of all its children fragments.
        */
        child.measure(MeasureSpec.makeMeasureSpec(
                screenWidth * visibleFragmentViewCount, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.EXACTLY));

    }

    @Override
    public int getDesiredPageUpdatesEx(int mLastScrollPosition, int mUpperThreshold, boolean enableSwipe) {

        return (mLastScrollPosition < mUpperThreshold) ? ABOUT_PAGE
                : (enableSwipe ? (mLastScrollPosition > mUpperThreshold ? HISTORY_PAGE
                        : UPDATES_PAGE) : UPDATES_PAGE);
    }

    @Override
    public int getDesiredPageHistoryEx(int mLastScrollPosition, int mUpperThreshold,
            int updatePageIndex) {

        return (mLastScrollPosition < mUpperThreshold * updatePageIndex) ? UPDATES_PAGE
                : HISTORY_PAGE;
    }

    @Override
    public View getViewPagerViewEx(ViewPager mViewPager) {
        return mLayoutInflater.inflate(
                R.layout.contact_detail_history_fragment_container, mViewPager, false);

    }

    @Override
    public int getCurrentPageIndexEx(ViewPager mViewPager,
            int currentPage) {

        if (mViewPager != null) {
            return mViewPager.getCurrentItem();
        } else if (currentPage != -1) {
            return currentPage;
        }

        return -1;
    }

    @Override
    public Fragment initContactDetailHistoryFragment(FragmentManager mFragmentManager) {

        mHistoryFragment = mFragmentManager.findFragmentByTag(
                "view-pager-history-fragment");
        Log.i(TAG, "initContactDetailHistoryFragment == " + mHistoryFragment);
        Log.i(TAG, "  mPluginContext == " + mPluginContext);
        if (mHistoryFragment == null) {
            mHistoryFragment = new ContactDetailHistoryFragment(mPluginContext);
//            ((ContactDetailHistoryFragment)mHistoryFragment).setPluginContext(mPluginContext);
        }

        return mHistoryFragment;
    }

    @Override
    public void addHistoryTransaction(FragmentManager mFragmentManager,
            FragmentTransaction transaction) {

        transaction.add(R.id.history_fragment_container, mHistoryFragment,
                "view-pager-history-fragment");
        transaction.commitAllowingStateLoss();
        mFragmentManager.executePendingTransactions();
    }

    @Override
    public void setPhoneNumbersToFragmentEx(Uri mContactUri, List<String> mPhoneNumbers, long contactId) {

        if (mHistoryFragment != null) {
            ((ContactDetailHistoryFragment) mHistoryFragment).setData(mContactUri, contactId, mPhoneNumbers);
        }

    }


}
