package com.mediatek.qsb.regressiontest;

import android.test.ActivityInstrumentationTestCase2;

import com.android.quicksearchbox.SearchActivity;

public class QuickSearchBoxTest extends ActivityInstrumentationTestCase2<SearchActivity> {
    private SearchActivity mActivity;

    public QuickSearchBoxTest() {
        super(SearchActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mActivity = getActivity();
    }

    public void testValidation() {
        assertNotNull(mActivity);
    }

    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            mActivity.finish();
        }

        super.tearDown();
    }

}
