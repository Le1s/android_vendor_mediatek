package com.mediatek.apn.sanitytest;

import android.test.InstrumentationTestRunner;

import junit.framework.TestSuite;

/**
 * APN test case runner.
 * */
public class ApnTestRunner extends InstrumentationTestRunner {
    /**
     * getAllTests TestSuite.
     * */
    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ApnLteTests.class);
        return suite;
    }
}
