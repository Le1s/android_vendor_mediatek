package com.mediatek.bluetooth.sanitytest;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;

public class BluetoohRunner extends InstrumentationTestRunner {
    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(BluetoothTest.class);
        return suite;
    }
}
