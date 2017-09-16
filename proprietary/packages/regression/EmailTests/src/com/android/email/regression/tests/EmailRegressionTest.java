package com.android.email.regression.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.android.email.activity.setup.AccountSetupFinal;
import com.android.email.regression.tests.TestUtils.Condition;
import com.android.email2.ui.MailActivityEmail;
import com.android.emailcommon.Configuration;
import com.android.mail.browse.SwipeableConversationItemView;
import com.android.mail.ui.SwipeableListView;
import com.jayway.android.robotium.solo.Solo;

/**
 * Regression testcase for Email basic function. adb shell am instrument -w
 * com.android.email.regression.tests/com.android.email.regression.tests.
 * EmailRegressionTestRunner
 */
public class EmailRegressionTest extends
        ActivityInstrumentationTestCase2<MailActivityEmail> {

    private static final String TAG = "EmailRegressionTest";
    private Instrumentation mInst;
    private Activity mActivity;
    private String mMimeType = "application/email-ls";
    private EmailDataUtlities mDataUtlities = null;
    private Solo mSolo;
    private static final int MAX_WAIT_SECOUND = 10;

    /**
     * EmailRegressionTest.
     */
    public EmailRegressionTest() {
        super(MailActivityEmail.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Configuration.openTest();
        mInst = getInstrumentation();
        mDataUtlities = new EmailDataUtlities(getInstrumentation()
                .getTargetContext());
        mSolo = new Solo(getInstrumentation(), mActivity);

    }

    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            mActivity.finish();
        }
        Configuration.shutDownTest();
        super.tearDown();
    }

    /**
     * No account config, mock launch form Home without any account. adb shell
     * am start -a android.intent.action.VIEW -t application/email-ls -d
     * content://ui.email.android.com/?ACCOUNT_ID=5
     *
     */
    public void testReg001LauncherEmailApp() {
        if (!TestUtils.isScreenOnAndNotLocked(getInstrumentation()
                .getTargetContext())) {
            Log.d(TAG,
                    "stop testLauncherEmailApp, for screen is not on or unlocked");
            return;
        }
        Log.d(TAG, "start testLauncherEmailApp");
        // 1. clear all account.
        mDataUtlities.clearTempAccount();

        // 2. no account config, shoudl popup setup account.
        ActivityMonitor am = new ActivityMonitor(
                AccountSetupFinal.class.getName(), null, false);
        try {
            mInst.addMonitor(am);
            // 2. lauch MailEmailActivity.
            mInst.startActivitySync(getOpenConversationListIntent());
            mActivity = am.waitForActivityWithTimeout(5000);
            assertNotNull(mActivity);
        } finally {
            if (mActivity != null) {
                mActivity.finish();
            }
            mDataUtlities.clearTempAccount();
            Log.d(TAG, "end testLauncherEmailApp");
        }
    }

    /**
     * Launch mail list, with pre set account.
     */
    public void testReg002ShowList() {
        if (!TestUtils.isScreenOnAndNotLocked(getInstrumentation()
                .getTargetContext())) {
            Log.d(TAG, "stop testShowList, for screen is not on or unlocked");
            return;
        }
        Log.d(TAG, "start testShowList");
        mDataUtlities.setupTempAccount();
        try {
            openConversationList(null);
        } finally {
            if (mActivity != null) {
                mActivity.finish();
            }
            mDataUtlities.clearTempAccount();
            Log.d(TAG, "end testShowList");
        }
    }

    /**
     * Launch mail list, with pre set account. swip item to delete a mail.
     */
    public void testReg003SwipDelete() {
        if (!TestUtils.isScreenOnAndNotLocked(getInstrumentation()
                .getTargetContext())) {
            Log.d(TAG,
                    "stop test003_SwipDelete, for screen is not on or unlocked");
            return;
        }
        Log.d(TAG, "start testSwapDelete");
        mDataUtlities.setupTempAccount();
        try {
            openConversationList(new InnerAction() {

                @Override
                public void doAction() {
                    SwipeableListView listContent = (SwipeableListView) mSolo
                            .getView(SwipeableListView.class, 0);
                    if (listContent != null && listContent.getCount() > 0) {
                        int messageCount = listContent.getCount();
                        Log.d(TAG, " find listContent " + messageCount
                                + " child " + listContent.getChildCount());
                        // mesageCount should more than @MESSAGE_COUNT
                        assertTrue("message count not right",
                                messageCount >= EmailDataUtlities.MESSAGE_COUNT);

                        int firstMessgeIndex = -1;
                        SwipeableConversationItemView firstItem = null;
                        // only find in the first screen.
                        for (int i = 0; i < listContent.getChildCount(); i++) {
                            View item = listContent.getChildAt(i);
                            if (item instanceof SwipeableConversationItemView) {
                                firstMessgeIndex = i;
                                firstItem = (SwipeableConversationItemView) item;
                                break;
                            }
                        }
                        Log.d(TAG, " find first message index "
                                + firstMessgeIndex);
                        if (firstMessgeIndex == -1) {
                            return;
                        }
                        assertTrue("find first message " + firstMessgeIndex,
                                firstMessgeIndex >= 0);
                        Log.d(TAG, " start swip message ");

                        // some time solo scroll not work, we just swip two
                        // message in another way.
                        mSolo.scrollViewToSide(firstItem, Solo.RIGHT);
                        TestUtils.dragViewToRight(EmailRegressionTest.this,
                                listContent.getChildAt(firstMessgeIndex + 1));
                        final int undoTextId = TestUtils.getResouceId(
                                mActivity, "undo_text");
                        TestUtils.waitUntil("wait undo button",
                                new Condition() {
                                    @Override
                                    public boolean isMet() {
                                        return mActivity
                                                .findViewById(undoTextId) != null;
                                    }
                                }, MAX_WAIT_SECOUND, 500);
                    }
                }
            });

        } finally {
            if (mActivity != null) {
                mActivity.finish();
            }
            mDataUtlities.clearTempAccount();
            Log.d(TAG, "end testSwapDelete");
        }
    }

    /**
     * Launch mail list, with pre set account. Drag and open different message.
     */
    public void testReg004OpenConversation() {
        if (!TestUtils.isScreenOnAndNotLocked(getInstrumentation()
                .getTargetContext())) {
            Log.d(TAG,
                    "stop testOpenConversation, for screen is not on or unlocked");
            return;
        }
        Log.d(TAG, "start testOpenConversation");
        mDataUtlities.setupTempAccount();
        try {
            openConversationList(new InnerAction() {

                @Override
                public void doAction() {
                    SwipeableListView listContent = (SwipeableListView) mSolo
                            .getView(SwipeableListView.class, 0);
                    if (listContent != null && listContent.getCount() > 0) {
                        int messageCount = listContent.getCount();
                        Log.d(TAG, " find listContent " + messageCount
                                + " child " + listContent.getChildCount());
                        // mesageCount should more than @MESSAGE_COUNT
                        assertTrue("message count not right",
                                messageCount >= EmailDataUtlities.MESSAGE_COUNT);

                        int firstMessgeIndex = -1;
                        SwipeableConversationItemView firstItem = null;
                        // only find in the first screen.
                        for (int i = 0; i < listContent.getChildCount(); i++) {
                            View item = listContent.getChildAt(i);
                            if (item instanceof SwipeableConversationItemView) {
                                firstItem = (SwipeableConversationItemView) item;
                                firstMessgeIndex = i;
                                break;
                            }
                        }

                        Log.d(TAG, " find first message index "
                                + firstMessgeIndex);
                        if (firstMessgeIndex == -1) {
                            return;
                        }
                        assertTrue("find conversaton item",
                                firstMessgeIndex != -1);
                        Log.d(TAG, " open first conversation with index "
                                + firstMessgeIndex);
                        mSolo.clickOnView(firstItem);
                        /*
                         * TouchUtils.clickView(EmailRegressionTest.this,
                         * firstItem);
                         */
                        dragAndShowMessge();
                    } else {
                        Log.d(TAG, " No message ");
                    }
                }
            });
        } finally {
            mInst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            if (mActivity != null) {
                mActivity.finish();
            }
            mDataUtlities.clearTempAccount();
            Log.d(TAG, "end testOpenConversation");
        }
    }

    /**
     * This case use to clean tmp data.
     * It is ok event not run it.
     */
    public void testReg009CleanUp() {
        Log.d(TAG, "cleanAll email data");
        mDataUtlities.cleanAll();
    }

    private void dragAndShowMessge() {
        TestUtils.dragToRight(EmailRegressionTest.this, mActivity);

        TestUtils.waitUntil("search message " + EmailDataUtlities.MESSAGE_PREX
                + (EmailDataUtlities.MESSAGE_COUNT - 1), new Condition() {

            @Override
            public boolean isMet() {
                // TODO: some time drag event, not work...
                return mSolo.searchText(EmailDataUtlities.MESSAGE_PREX
                        + (EmailDataUtlities.MESSAGE_COUNT - 1))
                        || mSolo.searchText(EmailDataUtlities.MESSAGE_PREX
                                + EmailDataUtlities.MESSAGE_COUNT);
            }
        }, MAX_WAIT_SECOUND, 500);

        TestUtils.dragToLeft(EmailRegressionTest.this, mActivity);
    }

    /**
     * Compose open conversation list intent.
     *
     * @return open conversation intent.
     */
    private Intent getOpenConversationListIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.setDataAndType(
                Uri.parse("content://ui.email.android.com/?ACCOUNT_ID=1"),
                mMimeType);
        return intent;
    }

    /**
     * 1. Set conversation list intent. 2. open MailActivityEmail. 3. check
     * wether list show.
     * @param action
     */
    private void openConversationList(InnerAction action) {
        // wait list show.
        ActivityMonitor am = new ActivityMonitor(
                MailActivityEmail.class.getName(), null, false);
        try {
            mInst.addMonitor(am);
            mActivity = (MailActivityEmail) mInst
                    .startActivitySync(getOpenConversationListIntent());
            am.waitForActivityWithTimeout(5000);
            assertNotNull(mActivity);

            final String inboxName = TestUtils.getString(mActivity,
                    "mailbox_name_display_inbox");
            // Inbox folder show success
            TestUtils.waitUntil("wait show " + inboxName, new Condition() {
                @Override
                public boolean isMet() {
                    return mSolo.searchText(inboxName);
                }
            }, MAX_WAIT_SECOUND, 500);

            Log.d(TAG, "find mailbox " + inboxName + " do action");
            // Next action,do some validation after list show.
            if (action != null) {
                action.doAction();
            }
        } finally {
            mInst.removeMonitor(am);
        }
    }

    private interface InnerAction {
        public void doAction();
    }
}
