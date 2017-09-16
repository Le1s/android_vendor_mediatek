
package com.android.calculator2;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

public class RegressionLaucherTest extends ActivityUnitTestCase<Calculator> {

    public RegressionLaucherTest() {
        super(Calculator.class);
    }

    private Instrumentation mInst;
    private Activity mActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInst = getInstrumentation();
    }

    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            mActivity.finish();
        }
        super.tearDown();
    }

    public void testLauncherApp() {
        Intent intent = new Intent(mInst.getTargetContext(), Calculator.class);
        ActivityMonitor am = new ActivityMonitor(Calculator.class.getName(), null, false);
        try {
            mInst.addMonitor(am);
            startActivity(intent, null, null);
            mActivity = am.waitForActivityWithTimeout(1000);
            assertNotNull(mActivity);
        } finally {
            mInst.removeMonitor(am);
        }
    }

}
