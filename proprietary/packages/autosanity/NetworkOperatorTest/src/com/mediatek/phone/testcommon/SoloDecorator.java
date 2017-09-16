package com.mediatek.phone.testcommon;

import android.app.Activity;
import android.app.Instrumentation;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.jayway.android.robotium.solo.Solo;

import java.util.ArrayList;

/**
 * SoloDecorator to make it easier to do UI testing
 */
public class SoloDecorator extends Solo {

    private static final int RETRY_TIMES = 5;
    private static final int RETRY_WAITING_TIME = 1000; // wait 1s and retry again
    private static final long WAITING_TIME_OUT = 10 * 1000; // wait UI change for at most 10s
    private static final long WAITING_TEXT_APPEAR_TIME = 60 * 1000; // wait Text appear time, 1 min
    private String mTag;
    public SoloDecorator(String logTag, Instrumentation instrumentation, Activity activity) {
        super(instrumentation, activity);
        mTag = logTag;
    }

    public SoloDecorator(String logTag, Instrumentation instrumentation) {
        super(instrumentation);
        mTag = logTag;
    }

    private void log(String msg) {
        Log.i(mTag, "[solo]" + msg);
    }

    public void logCurrentTexts() {
        ArrayList<TextView> currentTvs = super.getCurrentViews(TextView.class);
        if (currentTvs == null || currentTvs.size() < 1) {
            log("no TextView found, current Activity is: " + super.getCurrentActivity());
            return;
        }
        ArrayList<String> texts = new ArrayList<String>();
        for (TextView tv : currentTvs) {
            if (!TextUtils.isEmpty(tv.getText()) && tv.getVisibility() == View.VISIBLE) {
                texts.add(tv.getText().toString());
            }
        }
        log("current " + texts.size() + " visible texts in " + currentTvs.size() + " TextViews:");

        if (texts.size() < 1) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String text : texts) {
            sb.append("<").append(text).append(">, ");
        }
        log("        " + sb.toString());
    }

    @Override
    public void clickOnText(String text) {
        if (searchText(text)) {
            log("[clickOnText]clicking on text: " + text);
            super.clickOnText(text, 1, true);
        } else {
            log("[clickOnText]clicking on text failed: " + text);
            throw new IllegalArgumentException("[solo][clickOnText]text not found, not able to click it: " + text);
        }
    }

    @Override
    public boolean searchText(String text) {
        for (int i = 0; i < RETRY_TIMES; ++i) {
            if (super.searchText(text, true)) {
                logCurrentTexts();
                log("[searchText]text found: " + text);
                return true;
            }
            sleep(RETRY_WAITING_TIME);
        }
        logCurrentTexts();
        log("[searchText]text not found: " + text);
        return false;
    }

    /**
     * wait for text to disappear, with enough log
     * wait at most 3 minutes
     * @param text
     */
    public void waitForTextDisappear(String text) {
        log("[waitForTextDisappear]make sure text exists: " + text);
        if (!super.waitForText(text, 1, WAITING_TIME_OUT, false, true)) {
            log("[waitForTextDisappear]text never appears");
            logCurrentTexts();
            return;
        }
        logCurrentTexts();
        for (int tryCount = 0; tryCount < 3 * 60; tryCount++) {
            if (!super.searchText(text, 1, false, true)) {
                log("[waitForTextDisappear]text disappears");
                logCurrentTexts();
                return;
            }
            sleep(RETRY_WAITING_TIME);
        }
        log("[waitForTextDisappear]wait a long time, text still show, failed");
        logCurrentTexts();
        throw new IllegalStateException("[solo][waitForTextDisappear]text not disappear: " + text);
    }

    /**
     * wait for text appears, case fail if not appear
     * @param text
     */
    public void waitForTextAppear(String text) {
        log("[waitForTextAppear]waiting text: " + text);
        logCurrentTexts();
        if (waitForText(text, 1, WAITING_TEXT_APPEAR_TIME, false, true)) {
            log("[waitForTextAppear]text appears");
            logCurrentTexts();
            return;
        }
        log("[waitForTextAppear]waiting timeout");
        logCurrentTexts();
        throw new IllegalStateException("[solo][waitFotTextAppear]text not appear after a long time: " + text);
    }
}
