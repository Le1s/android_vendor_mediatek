package com.mediatek.flightmode.sanitytest;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;

public class FlightModeRunner extends InstrumentationTestRunner {
    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(FlightModeTest.class);
        return suite;
    }
}
