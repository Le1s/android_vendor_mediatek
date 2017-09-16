package com.mediatek.gallery3d.regressiontest;

import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import junit.framework.TestSuite;

public class Gallery2RegressionTestRunner extends InstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        InstrumentationTestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(Gallery2RegressionTestCase.class);
        return suite;
    }
    
    @Override
    public ClassLoader getLoader() {
        return Gallery2RegressionTestRunner.class.getClassLoader();
    }

}
