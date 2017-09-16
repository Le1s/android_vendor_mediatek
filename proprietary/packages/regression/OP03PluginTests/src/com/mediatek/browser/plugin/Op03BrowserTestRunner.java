package com.mediatek.browser.plugin.test;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;

public class Op03BrowserTestRunner extends InstrumentationTestRunner {
    
    @Override
    public TestSuite getTestSuite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(Op03BrowserBookmarkExTest.class);
        return suite;
    }
    
}