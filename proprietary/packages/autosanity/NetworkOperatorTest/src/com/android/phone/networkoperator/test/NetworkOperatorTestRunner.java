package com.android.phone.networkoperator.test;

import junit.framework.TestSuite;

import android.test.InstrumentationTestRunner;

public class NetworkOperatorTestRunner extends InstrumentationTestRunner {

    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(NetworkOperatorTestcase.class);
        return suite;
    }
}
