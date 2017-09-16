
package com.mediatek.systemui.plugin;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mediatek.systemui.ext.BaseSignalClusterExt;
import com.mediatek.systemui.ext.IconIdWrapper;
import com.mediatek.systemui.statusbar.util.SIMHelper;

/**
 * M: OP02 ISignalClusterExt implements for SystemUI.
 */
public class Op02SignalClusterExt extends BaseSignalClusterExt {

    private static final String TAG = "Op02SignalClusterExt";

    /**
     * Constructs a new Op02SignalClusterExt instance.
     * @param context A Context object
     */
    public Op02SignalClusterExt(Context context) {
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

                // Slot Indicator
                mMobileSlotIndicator[i] = new ImageView(mContext);
                if (isMultiSlot()) {
                    mMobileSlotIndicator[i].setImageResource(TelephonyIcons.SIGNAL_INDICATOR[i]);
                    mobileViewGroups[i].addView(mMobileSlotIndicator[i],
                            new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                mAdjustedLayout[i] = true;
            }

            mSignalClusterCombo[i] = signalClusterCombos[i];
            mSignalNetworkTypesImageViews[i] = signalNetworkTypesImageViews[i];
            mMobileDataType[i] = mobileTypeImageViews[i];
            mMobileSignalStrength[i] = mobileSignalStrengthImageViews[i];
        }
    }

    @Override
    protected void applyMobileSignalStrength(int slotId) {
        if (DEBUG) {
            Log.d(TAG, "applyMobileSignalStrength(), slotId = " + slotId
                    + ", mSlotCount = " + mSlotCount
                    + ", mMobileSignalStrengthIconId[slotId] = "
                    + toString(mMobileSignalStrengthIconId[slotId]));
        }

        if (mMobileSignalStrength[slotId] != null) {
            final boolean isSignalStrengthNull = isSignalStrengthNullIcon(slotId);
            if (!mIsSimAvailable[slotId] || isSignalStrengthNull) {
                Log.d(TAG, "No SIM inserted/Service or Signal Strength Null: " +
                        "Show empty signal icon");

                // signal strength null
                if (!isSignalStrengthNull) {
                    Log.d(TAG, "Set signal strength null icon.");
                    final IconIdWrapper signalStrengthNullIcon = new IconIdWrapper();
                    signalStrengthNullIcon.setResources(mContext.getResources());
                    signalStrengthNullIcon.setIconId(getSignalStrengthNullIcon(slotId));
                    setImage(mMobileSignalStrength[slotId], signalStrengthNullIcon);
                } else {
                    setImage(mMobileSignalStrength[slotId], mMobileSignalStrengthIconId[slotId]);
                }

                // Show signal icon
                mMobileSignalStrength[slotId].setVisibility(View.VISIBLE);

                // Show signal icon's parent
                final ViewParent mMobileGroup = mMobileSignalStrength[slotId].getParent();
                if (mMobileGroup != null) {
                    Log.d(TAG, "Show mMobileGroup");
                    ((ViewGroup) mMobileGroup).setVisibility(View.VISIBLE);

                    // Show mSignalClusterCombo
                    final ViewParent mSignalClusterCombo = mMobileGroup.getParent();
                    if (mSignalClusterCombo != null) {
                        Log.d(TAG, "Show mSignalClusterCombo");
                        ((ViewGroup) mSignalClusterCombo).setVisibility(View.VISIBLE);
                    }
                }
            } else {
                setImage(mMobileSignalStrength[slotId], mMobileSignalStrengthIconId[slotId]);
            }
        }
    }

    @Override
    protected void applyMobileSlotIndicator(int slotId) {
        if (mMobileSlotIndicator[slotId] != null) {
            mMobileSlotIndicator[slotId].setPaddingRelative(
                    mIsMobileTypeIconWide ? mWideTypeIconStartPadding : 0, 0, 0, 0);

            if (isMultiSlot() && mIsSimAvailable[slotId] && !isSignalStrengthNullIcon(slotId)) {
                mMobileSlotIndicator[slotId].setVisibility(View.VISIBLE);
            } else {
                mMobileSlotIndicator[slotId].setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void applyMobileRoamingIndicator(int slotId) {
        if (mMobileRoamingIndicator[slotId] != null) {
            Log.d(TAG, "applyMobileRoamingIndicator, mRoaming[" + slotId + "] = "
                    + mRoaming[slotId]);
            if (mRoaming[slotId] && mIsSimAvailable[slotId] && !isSignalStrengthNullIcon(slotId)) {

                mMobileRoamingIndicator[slotId].setImageResource(
                        TelephonyIcons.DATA_ROAMING_INDICATOR);

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
                    == getSignalStrengthNullIcon(slotId);
        Log.d(TAG, "isSignalStrengthNullIcon() = " + isSignalStrengthNullIcon);
        return isSignalStrengthNullIcon;
    }

    private int getSignalStrengthNullIcon(int slotId) {
        if (isMultiSlot()) {
            return TelephonyIcons.SIGNAL_STRENGTH_NULLS[slotId];
        } else {
            return TelephonyIcons.SIGNAL_STRENGTH_NULL;
        }
    }
}
