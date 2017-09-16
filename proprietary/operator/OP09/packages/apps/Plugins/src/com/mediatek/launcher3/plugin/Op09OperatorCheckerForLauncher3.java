package com.mediatek.launcher3.plugin;

import com.mediatek.common.PluginImpl;
import com.mediatek.launcher3.ext.DefaultOperatorChecker;

/**
 * OP09 IOperatorChecker implements for Launcher3.
 */
@PluginImpl(interfaceName = "com.mediatek.launcher3.ext.IOperatorChecker")
public class Op09OperatorCheckerForLauncher3 extends DefaultOperatorChecker {

    /**
     * Constructs a new Op09OperatorCheckerForLauncher3 instance.
     */
    public Op09OperatorCheckerForLauncher3() {
    }

    @Override
    public boolean supportEditAndHideApps() {
        return true;
    }

    @Override
    public boolean supportAppListCycleSliding() {
        return true;
    }
}
