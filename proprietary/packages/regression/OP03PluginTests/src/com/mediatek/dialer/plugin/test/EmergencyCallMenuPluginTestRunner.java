package com.mediatek.dialer.plugin.test;

import android.test.InstrumentationTestRunner;

import junit.framework.TestSuite;

public class EmergencyCallMenuPluginTestRunner extends InstrumentationTestRunner {

    @Override
    public TestSuite getTestSuite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(EmergencyCallMenuPluginTest.class);
        return suite;
    }

}
