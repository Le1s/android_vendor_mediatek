package com.mediatek.systemui.plugin;

import android.content.Context;

import com.mediatek.common.PluginImpl;
import com.mediatek.systemui.ext.BehaviorSet;
import com.mediatek.systemui.ext.DataType;
import com.mediatek.systemui.ext.DefaultStatusBarPlugin;
import com.mediatek.systemui.ext.ISignalClusterExt;
import com.mediatek.systemui.ext.IconIdWrapper;
import com.mediatek.systemui.ext.NetworkType;
import com.mediatek.systemui.statusbar.util.SIMHelper;
import com.mediatek.xlog.Xlog;

/**
 * M: OP02 implementation of Plug-in definition of Status bar.
 */
@PluginImpl(interfaceName = "com.mediatek.systemui.ext.IStatusBarPlugin")
public class Op02StatusBarPlugin extends DefaultStatusBarPlugin {

    private static final String TAG = "Op02StatusBarPlugin";
    private static final boolean DEBUG = true;

    private int mSlotCount = 0;

    /**
     * Constructs a new Op02StatusBarPlugin instance with Context.
     * @param context A Context object
     */
    public Op02StatusBarPlugin(Context context) {
        super(context);
        this.mSlotCount = SIMHelper.getSlotCount();
    }

    @Override
    public BehaviorSet customizeBehaviorSet() {
        return BehaviorSet.OP02_BS;
    }

    @Override
    public boolean customizeHspaDistinguishable(boolean distinguishable) {
        if (DEBUG) {
            Xlog.d(TAG, "customizeHspaDistinguishable, HspaDistinguishable = true");
        }

        return true;
    }

    @Override
    public void customizeSignalStrengthNullIcon(int slotId, IconIdWrapper icon) {
        if (DEBUG) {
            Xlog.d(TAG, "customizeSignalStrengthNullIcon, slotId = " + slotId
                    + ", mSlotCount = " + mSlotCount);
        }

        // Multi slot
        if (isMultiSlot()) {
            if (slotId >= 0 && slotId < TelephonyIcons.SIGNAL_STRENGTH_NULLS.length) {
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.SIGNAL_STRENGTH_NULLS[slotId]);
            }
        } else {
            icon.setResources(this.getResources());
            icon.setIconId(TelephonyIcons.SIGNAL_STRENGTH_NULL);
        }
    }

    @Override
    public void customizeSignalIndicatorIcon(int slotId, IconIdWrapper icon) {
        if (slotId >= 0 && slotId < TelephonyIcons.SIGNAL_INDICATOR.length) {
            if (isMultiSlot()) {
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.SIGNAL_INDICATOR[slotId]);
            }
        }
    }

    @Override
    public void customizeDataTypeIcon(IconIdWrapper icon, boolean roaming, DataType dataType) {
        if (DEBUG) {
            Xlog.d(TAG, "customizeDataTypeIcon, roaming = " + roaming + ", dataType = " + dataType);
        }
        switch (dataType) {
            case Type_1X:
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.DATA_TYPE_1X);
                break;
            case Type_3G:
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.DATA_TYPE_3G);
                break;
            case Type_4G:
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.DATA_TYPE_4G);
                break;
            case Type_E:
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.DATA_TYPE_E);
                break;
            case Type_G:
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.DATA_TYPE_G);
                break;
            case Type_H:
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.DATA_TYPE_H);
                break;
            case Type_H_PLUS:
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.DATA_TYPE_H_PLUS);
                break;
            default:
                break;
        }
    }

    @Override
    public void customizeDataNetworkTypeIcon(IconIdWrapper icon, NetworkType networkType) {
        if (DEBUG) {
            Xlog.d(TAG, "customizeDataNetworkTypeIcon, networkType = " + networkType);
        }

        switch (networkType) {
            case Type_G:
            case Type_E:
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.NETWORK_TYPE_G);
                break;
            case Type_3G:
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.NETWORK_TYPE_3G);
                break;
            case Type_4G:
                icon.setResources(this.getResources());
                icon.setIconId(TelephonyIcons.NETWORK_TYPE_4G);
                break;
            default:
                break;
        }
    }

    @Override
    public void customizeDataActivityIcon(IconIdWrapper icon, int dataActivity) {
        if (DEBUG) {
            Xlog.d(TAG, "customizeDataActivityIcon, dataActivity = " + dataActivity);
        }

        if (dataActivity >= 0 && dataActivity < TelephonyIcons.DATA_ACTIVITY.length) {
            icon.setResources(this.getResources());
            icon.setIconId(TelephonyIcons.DATA_ACTIVITY[dataActivity]);
        }
    }

    @Override
    public boolean customizeMobileGroupVisible(boolean isSimInserted) {
        return true;
    }

    @Override
    public ISignalClusterExt customizeSignalCluster() {
        if (DEBUG) {
            Xlog.d(TAG, "customizeSignalCluster, class = Op02SignalClusterExt");
        }

        return new Op02SignalClusterExt(this.getBaseContext());
    }

    private boolean isMultiSlot() {
        return mSlotCount > 1;
    }
}
