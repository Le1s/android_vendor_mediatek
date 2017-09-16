package com.mtk.test.imei.writetest;

import android.test.InstrumentationTestRunner;

import junit.framework.TestSuite;

public class IMEItestRunner extends InstrumentationTestRunner {
    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(IMEItest.class);
        return suite;
    }
}

