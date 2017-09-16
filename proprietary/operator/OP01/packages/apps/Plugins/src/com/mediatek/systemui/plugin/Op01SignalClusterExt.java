package com.mediatek.systemui.plugin;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mediatek.systemui.ext.BaseSignalClusterExt;
import com.mediatek.systemui.statusbar.util.SIMHelper;

/**
 * M: OP01 ISignalClusterExt implements for SystemUI.
 */
public class Op01SignalClusterExt extends BaseSignalClusterExt {

    private static final String TAG = "Op01SignalClusterExt";

    /**
     * Constructs a new Op01SignalClusterExt instance.
     * @param context A Context object
     */
    public Op01SignalClusterExt(Context context) {
        super(context);
    }

    @Override
    public void onAttachedToWindow(ViewGroup[] signalClusterCombos,
            ImageView[] signalNetworkTypesImageViews, ViewGroup[] mobileViewGroups,
            ImageView[] mobileTypeImageViews, ImageView[] mobileSignalStrengthImageViews) {
        if (DEBUG) {
            Log.d(TAG, "onAttachedToWindow()");
        }

        for (int i = SIMHelper.SLOT_INDEX_DEFAULT; i < mSlotCount; i++) {
            Log.d(TAG, "onAttachedToWindow, mAdjustedLayout[i]=" + mAdjustedLayout[i]);
            if (!mAdjustedLayout[i]) {
                // NetworkType & DataType & DataActivity container
                final FrameLayout networkDataTypeActivityContainer = new FrameLayout(mContext);
                networkDataTypeActivityContainer.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                // 1.NetworkType
                signalClusterCombos[i].removeView(signalNetworkTypesImageViews[i]);
                networkDataTypeActivityContainer.addView(signalNetworkTypesImageViews[i]);

                // 2.DataType
                mobileViewGroups[i].removeView(mobileTypeImageViews[i]);
                networkDataTypeActivityContainer.addView(mobileTypeImageViews[i]);

                // 3.DataActivity
                mMobileDataActivity[i] = new ImageView(mContext);
                networkDataTypeActivityContainer.addView(mMobileDataActivity[i],
                        new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER | Gravity.BOTTOM));

                // Add to signalClusterCombo
                final int addViewIndex = (i == SIMHelper.SLOT_INDEX_DEFAULT) ? 1 : 0;
                signalClusterCombos[i].addView(networkDataTypeActivityContainer, addViewIndex);

                mNetworkDataTypeActivityCombo[i] = networkDataTypeActivityContainer;

                // Roaming
                mMobileRoamingIndicator[i] = new ImageView(mContext);
                mobileViewGroups[i].addView(mMobileRoamingIndicator[i]);

                mAdjustedLayout[i] = true;
            }

            mSignalClusterCombo[i] = signalClusterCombos[i];
            mSignalNetworkTypesImageViews[i] = signalNetworkTypesImageViews[i];
            mMobileDataType[i] = mobileTypeImageViews[i];
            mMobileSignalStrength[i] = mobileSignalStrengthImageViews[i];
        }
    }

    @Override
    protected void applyMobileRoamingIndicator(int slotId) {
        if (mMobileRoamingIndicator[slotId] != null) {
            Log.d(TAG, "applyMobileRoamingIndicator, mRoaming[" + slotId + "] = "
                    + mRoaming[slotId]);
            if (mRoaming[slotId] && mIsSimAvailable[slotId] && !isSignalStrengthNullIcon(slotId)) {
                setImage(mMobileRoamingIndicator[slotId], mDefaultRoamingIconId);

                mMobileRoamingIndicator[slotId].setPaddingRelative(mWideTypeIconStartPadding,
                        mMobileRoamingIndicator[slotId].getPaddingTop(),
                        mMobileRoamingIndicator[slotId].getPaddingRight(),
                        mMobileRoamingIndicator[slotId].getPaddingBottom());
                mMobileRoamingIndicator[slotId].setVisibility(View.VISIBLE);
            } else {
                mMobileRoamingIndicator[slotId].setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void applyMobileDataActivity(int slotId) {
        if (mMobileDataActivity[slotId] != null) {
            if (!mDataConnectioned[slotId] || !mIsSimAvailable[slotId]
                    || isSignalStrengthNullIcon(slotId)) {
                Log.d(TAG, "applyMobileDataActivity(), mMobileDataActivity is GONE");
                mMobileDataActivity[slotId].setVisibility(View.GONE);
            } else {
                Log.d(TAG, "applyMobileDataActivity(), mMobileDataActivity is VISIBLE");
                setImage(mMobileDataActivity[slotId], mMobileDataActivityIconId[slotId]);
                mMobileDataActivity[slotId].setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void applyNetworkDataSwitch(int slotId) {
        Log.d(TAG, "applyNetworkDataSwitch(), mDataConnectioned[" + slotId + "] = "
                + mDataConnectioned[slotId]);

        if (mSignalNetworkTypesImageViews[slotId] == null || mMobileDataType[slotId] == null) {
            return;
        }

        if (!mIsSimAvailable[slotId] || isSignalStrengthNullIcon(slotId)) {
            Log.d(TAG, "applyNetworkDataSwitch(), "
                    + "No SIM inserted/Service or Signal Strength Null: "
                    + "Hide network type icon and data icon");

            mSignalNetworkTypesImageViews[slotId].setVisibility(View.GONE);
            mMobileDataType[slotId].setVisibility(View.GONE);
        } else {
            if (mWifiVisible) {
                Log.d(TAG, "applyNetworkDataSwitch(), mWifiVisible = true,"
                        + " Show network type icon, Hide data type icon");

                mSignalNetworkTypesImageViews[slotId].setVisibility(View.VISIBLE);
                mMobileDataType[slotId].setVisibility(View.GONE);
            } else {
                if (mDataConnectioned[slotId] && mMobileDataTypeIconId[slotId].getIconId() > 0) {
                    mSignalNetworkTypesImageViews[slotId].setVisibility(View.GONE);

                    // Set data type icon id if DataTypeIconId is RoamingIconId.
                    if (mMobileDataTypeIconId[slotId].getIconId() == mDefaultRoamingIconId
                            .getIconId()) {
                        mMobileDataTypeIconId[slotId].setResources(mContext.getResources());
                        mMobileDataTypeIconId[slotId].setIconId(TelephonyIcons
                                .getDataTypeIconId(mNetworkControllerExt.getDataType(slotId)));
                        setImage(mMobileDataType[slotId], mMobileDataTypeIconId[slotId]);
                    }
                    mMobileDataType[slotId].setVisibility(View.VISIBLE);
                } else {
                    mSignalNetworkTypesImageViews[slotId].setVisibility(View.VISIBLE);
                    mMobileDataType[slotId].setVisibility(View.GONE);
                }
            }
        }

        Log.d(TAG, "applyNetworkDataSwitch(), "
                + "mSignalNetworkTypesImageViews isVisible: "
                + (mSignalNetworkTypesImageViews[slotId].getVisibility() == View.VISIBLE)
                + ", mMobileDataType isVisible: "
                + (mMobileDataType[slotId].getVisibility() == View.VISIBLE));
    }

    private boolean isSignalStrengthNullIcon(int slotId) {
        final boolean isSignalStrengthNullIcon = mMobileSignalStrengthIconId[slotId] != null
                && mMobileSignalStrengthIconId[slotId].getIconId()
                    == mDefaultSignalNullIconId.getIconId();
        Log.d(TAG, "isSignalStrengthNullIcon() = " + isSignalStrengthNullIcon);
        return isSignalStrengthNullIcon;
    }
}