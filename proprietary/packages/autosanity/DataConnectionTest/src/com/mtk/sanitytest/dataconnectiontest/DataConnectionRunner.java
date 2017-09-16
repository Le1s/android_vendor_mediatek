package com.mtk.sanitytest.dataconnectiontest;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;


public class DataConnectionRunner extends InstrumentationTestRunner {

    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(DataConnectionTest.class);
        return suite;
    }
}
