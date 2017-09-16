package com.mtk.fts;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.test.LaunchPerformanceBase;

public class FSLaunchPerformance extends LaunchPerformanceBase {
	
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        mIntent.setAction("com.mtk.FunctionalTestService");
        start();
    }

    /**
     * Calls LaunchApp and finish.
     */
    @Override
    public void onStart() {
        super.onStart();
       // LaunchApp();
		getTargetContext().startService(mIntent);
        waitForIdleSync();
        finish(0, mResults);
    }
}
