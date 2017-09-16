
package com.android.calculator2;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.View;


public class RegressionCalculatorTest extends ActivityInstrumentationTestCase2<Calculator> {

    private Activity mActivity;
    private Instrumentation mInst;
    private CalculatorEditText mFormulaEditText;
    private CalculatorEditText mResultEditText;
    private View mClearButton;
    private View mDeleteButton;

    public RegressionCalculatorTest() {
        super("com.android.calculator2", Calculator.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInst = getInstrumentation();
        mInst.waitForIdleSync();
    }

    @Override
    protected void tearDown() throws Exception {
        mActivity.finish();
        super.tearDown();
    }

    public void testUserInput() {
        mFormulaEditText = (CalculatorEditText) mActivity.findViewById(R.id.formula);
        mDeleteButton = (View) mActivity.findViewById(R.id.del);
        // input 1234567890
        assertTrue(
                "Tap 1,2,3 failed",
                tap(R.id.digit_1, R.id.digit_2, R.id.digit_3, R.id.digit_4, R.id.digit_5,
                        R.id.digit_6, R.id.digit_7, R.id.digit_8, R.id.digit_9, R.id.digit_0,
                        R.id.dec_point));
        mInst.waitForIdleSync();
        assertEquals("UserInput not 1234567890.", "1234567890.", mFormulaEditText.getText()
                .toString());
        // test delete
        TouchUtils.clickView(this, mDeleteButton);
        mInst.waitForIdleSync();
        assertEquals("UserInput not 1234567890", "1234567890", mFormulaEditText.getText()
                .toString());
        tap(R.id.op_mul, R.id.digit_9, R.id.eq);
        mClearButton = mActivity.findViewById(R.id.clr);
        assertTrue(mClearButton != null && mClearButton.getVisibility() == View.VISIBLE);
        TouchUtils.clickView(this, mClearButton);
        mInst.waitForIdleSync();
        assertEquals("UserInput not empty", "", mFormulaEditText.getText().toString());
    }

    public void testCommonOperation() {
        mFormulaEditText = (CalculatorEditText) mActivity.findViewById(R.id.formula);
        mResultEditText = (CalculatorEditText) mActivity.findViewById(R.id.result);
        tap(R.id.digit_1, R.id.digit_4, R.id.digit_4);
        tap(R.id.op_div);
        tap(R.id.digit_1, R.id.digit_2);
        tap(R.id.eq);
        assertEquals("div result failed", "12", mFormulaEditText.getText().toString());
        tap(R.id.op_sub);
        tap(R.id.digit_6);
        tap(R.id.eq);
        assertEquals("sub result failed", "6", mFormulaEditText.getText().toString());
        tap(R.id.op_add);
        tap(R.id.digit_7, R.id.digit_8);
        tap(R.id.eq);
        assertEquals("add result failed", "84", mFormulaEditText.getText().toString());
        tap(R.id.op_mul);
        tap(R.id.digit_9);
        tap(R.id.eq);
        assertEquals("mul result failed", "756", mFormulaEditText.getText().toString());
    }

    private boolean tap(int... ids) {
        boolean ret = true;
        for (int id : ids) {
            View view = mActivity.findViewById(id);
            if (view != null) {
                TouchUtils.clickView(this, view);
            } else {
                ret = false;
            }
        }
        return ret;
    }
}
