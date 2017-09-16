package com.mediatek.contacts.sanitytest;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;


public class AutoSanityTestRunner extends InstrumentationTestRunner {

    @Override
    public TestSuite getAllTests() {
        TestSuite suit = new TestSuite();
        suit.addTestSuite(ContactsTest.class);
        return suit;
    }

}
