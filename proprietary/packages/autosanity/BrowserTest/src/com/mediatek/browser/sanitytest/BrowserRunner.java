package com.mediatek.browser.sanitytest;

import junit.framework.TestSuite;

import android.test.InstrumentationTestRunner;

public class BrowserRunner extends InstrumentationTestRunner {
    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestBrowser.class);
        return suite;
    }
}
