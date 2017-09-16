package com.mediatek.settings.regressiontest;

import android.test.InstrumentationTestRunner;
import junit.framework.TestSuite;

public class RegressionTestRunner extends InstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        TestSuite tests = new TestSuite();
        tests.addTestSuite(DateTimeTest.class);
        tests.addTestSuite(DisplayTest.class);
        tests.addTestSuite(FactoryResetTest.class);
        tests.addTestSuite(SecurityTest.class);
        tests.addTestSuite(SettingsAppsTest.class);
        tests.addTestSuite(SettingsLanguageTest.class);
        tests.addTestSuite(WifiTest.class);
        tests.addTestSuite(AudioProfileTest.class);

        return tests;
    }
}
