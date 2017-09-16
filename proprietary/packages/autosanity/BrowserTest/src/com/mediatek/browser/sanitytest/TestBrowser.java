package com.mediatek.browser.sanitytest;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.jayway.android.robotium.solo.Solo;
import com.android.browser.BrowserActivity;

import java.util.Timer;
import java.util.TimerTask;

public class TestBrowser extends InstrumentationTestCase {

    private static final String TAG = "TestBrowser";
    private static final String BAIDU = "http://www.baidu.com";
    private static final String PACKAGE_NAME = "com.android.browser";
    private static final String CLASS_NAME = "com.android.browser.BrowserActivity";
    private static final long TIMEOUT_VALUE = 1000 * 60 * 10L;
    private BrowserActivity mActivity;
    private Instrumentation mInst;
    private Solo msolo;
    private Timer mTimer = null;
    private boolean mFinish = false;
    private boolean mError = false;
    private boolean mTimeout = false;
    private WebView mWebView = null;

    public TestBrowser() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();
        mInst = getInstrumentation();
        msolo = new Solo(mInst, null);
        mFinish = false;
        mError = false;
        mTimeout = false;
    }

    public void testBrowserStart() throws Throwable {
        Log.i(TAG, "Launcher browser " + BAIDU);
        Uri uri = Uri.parse(BAIDU);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mActivity = (BrowserActivity) mInst.startActivitySync(intent);
        assertTrue(mActivity != null);
        assertTrue(mInst != null);
        assertTrue(msolo != null);

        mWebView = mActivity.getCurrentWebView();
        if (mWebView == null) {
            return;
        }

        mTimer = new Timer();
        mTimer.schedule(new BrowserLoadingTimerTask(), TIMEOUT_VALUE);

        mActivity.runOnUiThread(new Runnable() {

            public void run() {
                mWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        Log.d(TAG, "onPageFinished url = " + url);
                        mFinish = true;
                        if (mTimer != null) {
                            mTimer.cancel();
                        }
                    }
                    @Override
                    public void onReceivedError(WebView view, int errorCode,
                        String description, String failingUrl) {
                        Log.d(TAG, "onReceivedError " + errorCode + " " + failingUrl + " " + description);
                        mError = true;
                    }
                });
                mWebView.loadUrl(BAIDU);
            }

        });

        while (!mFinish && !mTimeout) {

        }
        assertTrue(mError == false);
    }

    protected void tearDown() throws Exception {
        if (mActivity != null) {
            mActivity.finish();
        }
        super.tearDown();
    }

    class BrowserLoadingTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Browser loading timeout!");
            mTimeout = true;
            mError = true;
        }
    }
}
