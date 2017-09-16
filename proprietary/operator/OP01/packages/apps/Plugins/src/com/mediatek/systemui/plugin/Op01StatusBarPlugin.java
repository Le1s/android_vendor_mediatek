package com.mediatek.systemui.plugin;

import android.content.Context;
import android.util.Log;

import com.mediatek.common.PluginImpl;
import com.mediatek.systemui.ext.BehaviorSet;
import com.mediatek.systemui.ext.DataType;
import com.mediatek.systemui.ext.DefaultStatusBarPlugin;
import com.mediatek.systemui.ext.ISignalClusterExt;
import com.mediatek.systemui.ext.IconIdWrapper;
import com.mediatek.systemui.ext.NetworkType;

/**
 * M: OP01 implementation of Plug-in definition of Status bar.
 */
@PluginImpl(interfaceName = "com.mediatek.systemui.ext.IStatusBarPlugin")
public class Op01StatusBarPlugin extends DefaultStatusBarPlugin {

    private static final String TAG = "Op01StatusBarPlugin";
    private static final boolean DEBUG = true;

    /**
     * Constructs a new Op01StatusBarPlugin instance with Context.
     * @param context A Context object
     */
    public Op01StatusBarPlugin(Context context) {
        super(context);
    }

    @Override
    public boolean customizeAutoInSimChooser(boolean isEnabled) {
        return true;
    }

    @Override
    public boolean customizeEnableBluetoothtAirplaneMode(boolean isEnabled) {
        return true;
    }

    @Override
    public BehaviorSet customizeBehaviorSet() {
        return BehaviorSet.OP01_BS;
    }

    @Override
    public boolean customizeHspaDistinguishable(boolean distinguishable) {
        if (DEBUG) {
            Log.d(TAG, "customizeHspaDistinguishable, HspaDistinguishable = false");
        }

        return false;
    }

    @Override
    public void customizeSignalStrengthIcon(int level, boolean roaming, IconIdWrapper[] icons) {
        if (DEBUG) {
            Log.d(TAG, "customizeSignalStrengthIcon, level = " + level + ", roaming = " + roaming);
        }

        if (level >= 0 && level < TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH_FULL.length) {
            icons[0].setResources(this.getResources());
            icons[0].setIconId(TelephonyIcons.getSignalStrengthIcon(level, roaming));
        }
    }

    @Override
    public void customizeDataTypeIcon(IconIdWrapper icon, boolean roaming, DataType dataType) {
        if (DEBUG) {
            Log.d(TAG, "customizeDataTypeIcon, roaming = " + roaming + ", dataType = " + dataType);
        }

        icon.setResources(this.getResources());
        icon.setIconId(TelephonyIcons.getDataTypeIconId(dataType));
    }

    @Override
    public void customizeDataNetworkTypeIcon(IconIdWrapper icon, NetworkType networkType) {
        if (DEBUG) {
            Log.d(TAG, "customizeDataNetworkTypeIcon, networkType = " + networkType);
        }

        icon.setResources(this.getResources());
        icon.setIconId(TelephonyIcons.getNetworkTypeIconId(networkType));
    }

    @Override
    public void customizeDataActivityIcon(IconIdWrapper icon, int dataActivity) {
        if (DEBUG) {
            Log.d(TAG, "customizeDataActivityIcon, dataActivity = " + dataActivity);
        }

        if (dataActivity >= 0 && dataActivity < TelephonyIcons.DATA_ACTIVITY.length) {
            icon.setResources(this.getResources());
            icon.setIconId(TelephonyIcons.DATA_ACTIVITY[dataActivity]);
        }
    }

    @Override
    public ISignalClusterExt customizeSignalCluster() {
        if (DEBUG) {
            Log.d(TAG, "customizeSignalCluster, class = Op01SignalClusterExt");
        }

        return new Op01SignalClusterExt(this.getBaseContext());
    }
}
