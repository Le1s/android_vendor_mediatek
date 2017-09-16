package com.mediatek.autosanity.networkmodeswitchtest;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;


public class NetworkModeSwitchTestRunner extends InstrumentationTestRunner {

    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(NetworkModeSwitchTest.class);
        return suite;
    }
}
