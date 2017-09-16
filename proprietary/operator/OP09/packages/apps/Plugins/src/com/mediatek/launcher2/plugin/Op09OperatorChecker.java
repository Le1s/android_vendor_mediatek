package com.mediatek.launcher2.plugin;

import android.util.Log;

import com.mediatek.common.PluginImpl;
import com.mediatek.launcher2.ext.DefaultOperatorChecker;

/**
 * OP09 IOperatorChecker implements for Launcher2.
 */
@PluginImpl(interfaceName = "com.mediatek.launcher2.ext.IOperatorChecker")
public class Op09OperatorChecker extends DefaultOperatorChecker {
    private static final String TAG = "Op09OperatorChecker";

    /**
     * Constructs a new Op09OperatorChecker instance.
     */
    public Op09OperatorChecker() {
    }

    @Override
    public boolean supportEditAndHideApps() {
        Log.d(TAG, "Op09OperatorChecker supportEditAndHideApps called.");
        return true;
    }

    @Override
    public boolean supportAppListCycleSliding() {
        Log.d(TAG, "Op09OperatorChecker supportAppListCycleSliding called.");
        return true;
    }
}
