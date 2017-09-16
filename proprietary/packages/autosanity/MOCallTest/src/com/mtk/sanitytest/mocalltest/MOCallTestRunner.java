package com.mtk.sanitytest.mocalltest;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;


public class MOCallTestRunner extends InstrumentationTestRunner {

    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(MOCallTest.class);
        return suite;
    }
}

