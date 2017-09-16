package com.android.email.regression.tests;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import com.android.email.regression.tests.TestUtils.Condition;
import com.android.email.service.EmailServiceUtils;
import com.android.emailcommon.mail.Address;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.provider.EmailContent.Attachment;
import com.android.emailcommon.provider.EmailContent.Body;
import com.android.emailcommon.provider.EmailContent.Message;
import com.android.emailcommon.provider.HostAuth;
import com.android.emailcommon.provider.Mailbox;
import com.android.emailcommon.utility.Utility;
import com.android.mail.utils.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import junit.framework.Assert;

/**
 * M: Utilities to support do UI Test. 1. create system account and message. 2.
 * rotate activity.
 */
public class EmailDataUtlities extends Assert {

    private static final String TAG = "EmailDataUtlities";
    private Context mContext;
    protected static final String ACCOUNT_EMAIL_TYPE = "com.android.email";
    protected static final String ACCOUNT_EXCHANGE_TYPE = "com.android.exchange";

    private static final String TEST_ACCOUNT_PREFIX = "RT_";
    public static final long WAIT_FOR_LONG_LOADING = 2500;
    public static final long WAIT_FOR_DB_OPERATION = 1000;
    public static final long WAIT_FOR_ACTIVITY_START = 1500;
    public static final long WAIT_FOR_OPERATION_START = 500;
    public static final int TYPE_EAS = 1;
    public static final int TYPE_IMAP = 2;
    public static final int TYPE_POP = 3;
    private static Random sRandom = new Random();

    public static final int MESSAGE_COUNT = 25;
    public static final String MESSAGE_PREX = "Test_Message_";

    private static final String[] DEMO_ADDRESS = { "123@service.netease.com",
            "yx163@service.netease.com网易《实况俱乐部》",
            "trip163@service.netease.com网易火车票", "1@service.netease.com一元夺宝",
            "webinar@mobileiron.comMobileIron", "admin@codoon.comAdmin",
            "noreply@noreply.xiaoenai.com小恩爱", "passport@baidu.combaidu",
            "dashi@service.netease.com", "no-reply@yinxiang.com印象笔记",
            "no-reply@mail.qiushibaike.com", "info@newsletter.gaopeng.com高朋",
            "dyf3285@hotmail.comDaiJack",
            "update+zrdpfrcecc=z@facebookmail.comFacebook" };

    private static final String[] DEMO_SNAP = {
            "To view this email as a web page, click here",
            "如邮件无法正常显示，请点击这里如果你不想再收到该产品的推荐邮件，请点击 这里退订",
            "亲爱的朋友： 恭喜您成为一元夺宝的特邀用户，获赠1元开门红包！ →_→不要以为我不知道此刻你的" +
            "内心正在鄙视这微不足道的1元…但在这里，"
                    + "1元就已经足够你把iPhone6带回家，你没看错，我们有iPhone6哦(*^_^*)！ " +
                    "如果你不想再收到该产品的推荐邮件，请点击 这里退订",
            "本邮件如不能正常显示，您可选择在线浏览：请点击此处查看。 看看大家都在团啥子：加太贺、" +
            "大风炊、唛田KTV、赛江南、保利万和影城，还有适合聚餐的大蓉和10人餐。 告别五月病，吃喝玩走起。",
            "高朋团购 成都站|取消订阅",
            "邮件带有附件预览链接，若您转发或回复此邮件时不希望对方预览附件，建议您手动删除链接",
            "如果你不想再收到该产品的推荐邮件，请点击 这里退订",
            "您发给我的信件已经收到。 - 海量、安全、快速、专业！尽在126免费邮 www.126.com This is " +
            "an automatic reply, confirming that your e-mail was received.Thank you 邮箱使用小提示"
                    + " 想让对方更及时看到你的邮件，可以试试发到 无需事先开通，还有短信提醒。了解网易手机邮箱详情",
            "上一次登录 Facebook 之后，发生了许多新鲜事。以下是你错过的一些来自好友的通知。7 个朋友请求 " +
            "你没有新通知。 上一次登录 Facebook 之后，发生了许多新鲜事。以下是你错过的一些来自好友的通知" };

    /**
     * Create new EmailDataUtlities.
     * @param context context to save db.
     */
    public EmailDataUtlities(Context context) {
        mContext = context;
    }

    /**
     * Create a account for the given type, setup inbox and some demo messsage.
     *
     * @param messageCount
     *            default demo message count.
     * @param type
     *            account type.
     * @param name
     *            account name.
     * @param address
     *            account address.
     */
    public void setupTestMessage(int messageCount, int type, String name,
            String address) {
        Account account = setupAccount(type, name, TEST_ACCOUNT_PREFIX
                + address);
        LogUtils.w(TAG, "Setup account " + account.mId);
        Mailbox inbox = Mailbox.newSystemMailbox(mContext, account.mId,
                Mailbox.TYPE_INBOX);
        inbox.mFlags = 24;
        inbox.save(mContext);
        LogUtils.w(TAG, "Setup inbox " + inbox.mId);
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        for (int i = 1; i <= messageCount; i++) {
            Message msg = setupMessage(MESSAGE_PREX + i, account.mId,
                    inbox.mId, true, false, mContext, true, true, address);
            // LogUtils.w(TAG, "commit message " + msg.mId );
            msg.mFlagRead = (sRandom.nextInt() % 10 <= 2);
            msg.mFlagFavorite = (sRandom.nextInt() % 10 == 1);
            msg.addSaveOps(ops);
        }
        try {
            mContext.getContentResolver().applyBatch(EmailContent.AUTHORITY,
                    ops);
            LogUtils.w(TAG, "commit inbox " + messageCount + " message");
        } catch (RemoteException e) {
            LogUtils.w(TAG, "Transaction failed on create test message " + e);
        } catch (OperationApplicationException e) {
            LogUtils.w(TAG, "Transaction failed on create tes message " + e);
        }
    }

    private Account setupAccount(int type, String name, String address) {
        String receiveProtocol = "";
        String receiveAddress = "";
        String sendProtocol = "";
        String sendAddress = "";

        if (type == TYPE_EAS) {
            receiveProtocol = "eas";
            receiveAddress = "m.google.com";
            sendProtocol = "eas";
            sendAddress = "m.google.com";
        } else {
            receiveProtocol = "imap";
            receiveAddress = "imap.google.com";
            sendProtocol = "smtp";
            sendAddress = "smtp.google.com";
        }
        // 1) Setup Account
        HostAuth receive = setupHostAuth(receiveProtocol, receiveAddress, true,
                mContext);

        HostAuth send = setupHostAuth(sendProtocol, sendAddress, true, mContext);
        Account account = setupAccount(name, address, false, mContext, type);
        account.mHostAuthKeyRecv = receive.mId;
        account.mHostAuthKeySend = send.mId;
        account.mHostAuthRecv = receive;
        account.mHostAuthSend = send;
        account.mSignature = "Everything under the sun is possible";
        account.mFlags = account.mFlags | Account.FLAGS_SUPPORTS_SEARCH
                | Account.FLAGS_SUPPORTS_SMART_FORWARD;
        account.save(mContext);
        EmailServiceUtils.setupAccountManagerAccount(mContext, account, true,
                false, false, null);
        sleepAndWait(WAIT_FOR_DB_OPERATION);
        return account;
    }

    private android.accounts.Account[] getTestAccounts(boolean isExchangeType) {
        String accountType = isExchangeType ? ACCOUNT_EXCHANGE_TYPE
                : ACCOUNT_EMAIL_TYPE;
        return AccountManager.get(mContext).getAccountsByType(accountType);
    }

    private boolean existTestAccount(boolean isExchangeType) {
        return getTestAccounts(isExchangeType).length > 0;
    }

    protected void deleteAccountManagerAccount(android.accounts.Account account) {
        AccountManagerFuture<Boolean> future = AccountManager.get(mContext)
                .removeAccount(account, null, null);
        try {
            future.getResult();
        } catch (OperationCanceledException e) {
            // ignore
        } catch (AuthenticatorException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }
    }

    protected void deleteTemporaryAccountManagerAccounts(boolean isExchangeType) {
        for (android.accounts.Account accountManagerAccount : getTestAccounts(isExchangeType)) {
            if (accountManagerAccount.type
                    .equals(isExchangeType ? ACCOUNT_EXCHANGE_TYPE
                            : ACCOUNT_EMAIL_TYPE)) {
                deleteAccountManagerAccount(accountManagerAccount);
            }
        }
    }

    /**
     * Sleep some time.
     */
    public static void sleepAndWait() {
        sleepAndWait(WAIT_FOR_ACTIVITY_START);
    }

    /**
     * Sleep given time.
     * @param times time to sleep.
     */
    public static void sleepAndWait(long times) {
        try {
            Thread.sleep(times);
        } catch (InterruptedException e) {
            // Ingore
        }
    }

    /**
     * Create an account for test purposes.
     */
    private static Account setupAccount(String name, String address,
            boolean saveIt, Context context, int type) {
        Account account = new Account();

        account.mDisplayName = name;
        account.mEmailAddress = address;
        account.mSyncLookback = 1;
        account.mHostAuthKeyRecv = 0;
        account.mHostAuthKeySend = 0;
        account.mSenderName = name;
        account.mPolicyKey = 0;
        account.mSignature = "signature-" + name;
        if (type == TYPE_EAS) {
            account.mSyncKey = "sync-key-" + name;
            account.mSyncInterval = Account.CHECK_INTERVAL_NEVER;
            account.mProtocolVersion = "14.0";
            account.mSecuritySyncKey = "sec-sync-key-" + name;
        } else {
            account.mSyncLookback = 3;
            account.mSyncInterval = Account.CHECK_INTERVAL_NEVER;
            account.mFlags = 2057;
        }
        if (saveIt) {
            account.save(context);
        }
        return account;
    }

    /**
     * Lightweight way of deleting an account for testing.
     */
    private static void deleteAccount(Context context, long accountId) {
        context.getContentResolver().delete(
                ContentUris.withAppendedId(Account.CONTENT_URI, accountId),
                null, null);
    }

    /**
     * Create a hostauth record for test purposes.
     */
    private static HostAuth setupHostAuth(String name, long accountId,
            boolean saveIt, Context context) {
        return setupHostAuth("protocol", name, saveIt, context);
    }

    /**
     * Create a hostauth record for test purposes.
     */
    private static HostAuth setupHostAuth(String protocol, String name,
            boolean saveIt, Context context) {
        HostAuth hostAuth = new HostAuth();

        hostAuth.mProtocol = protocol;
        hostAuth.mAddress = "address-" + name;
        hostAuth.mPort = 100;
        hostAuth.mFlags = 200;
        hostAuth.mLogin = "login-" + name;
        hostAuth.mPassword = "password-" + name;
        hostAuth.mDomain = "domain-" + name;
        hostAuth.mCredentialKey = -1;

        if (saveIt) {
            hostAuth.save(context);
        }
        return hostAuth;
    }

    /**
     * Create a message for test purposes.
     *
     * @param name
     *            message name.
     * @param accountId
     *            accountId.
     * @param mailboxId
     *            mailboxId.
     * @param addBody
     *            add body infor for this message.
     * @param saveIt
     *            need save to db.
     * @param context
     *            context.
     * @param starred
     *            star tag.
     * @param read
     *            read tag.
     * @param address
     *            sender address.
     * @return a tmp message.
     */
    public static Message setupMessage(String name, long accountId,
            long mailboxId, boolean addBody, boolean saveIt, Context context,
            boolean starred, boolean read, String address) {
        Message message = new Message();
        message.mTimeStamp = System.currentTimeMillis();
        message.mFlagRead = read;
        message.mFlagSeen = read;
        message.mFlagLoaded = Message.FLAG_LOADED_COMPLETE;
        message.mFlagFavorite = starred;
        message.mFlagAttachment = false;
        message.mFlags = 0;

        message.mServerId = "serverid " + name;
        message.mServerTimeStamp = 300 + name.length();
        message.mMessageId = "messageid " + name;

        message.mMailboxKey = mailboxId;
        message.mAccountKey = accountId;

        message.mFrom = DEMO_ADDRESS[Math.abs(sRandom.nextInt())
                % DEMO_ADDRESS.length];
        message.mTo = address;
        message.mCc = DEMO_ADDRESS[Math.abs(sRandom.nextInt())
                % DEMO_ADDRESS.length];
        message.mBcc = DEMO_ADDRESS[Math.abs(sRandom.nextInt())
                % DEMO_ADDRESS.length];
        message.mReplyTo = DEMO_ADDRESS[Math.abs(sRandom.nextInt())
                % DEMO_ADDRESS.length];
        message.mSnippet = DEMO_SNAP[Math.abs(sRandom.nextInt())
                % DEMO_SNAP.length];
        message.mSubject = name;
        Address add = Address.firstAddress(message.mFrom);
        message.mDisplayName = (add != null ? add.getPersonal() : message.mFrom);

        if (addBody) {
            message.mText = message.mSnippet;
            message.mHtml = message.mSnippet;
        }

        if (saveIt) {
            message.save(context);
        }
        return message;
    }

    /**
     * Create a test body.
     *
     * @param messageId
     *            the message this body belongs to
     * @param textContent
     *            the plain text for the body
     * @param htmlContent
     *            the html text for the body
     * @param saveIt
     *            if true, write the new attachment directly to the DB
     * @param context
     *            use this context
     */
    public static Body setupBody(long messageId, String textContent,
            String htmlContent, boolean saveIt, Context context) {
        Body body = new Body();
        body.mMessageKey = messageId;
        body.mHtmlContent = htmlContent;
        body.mTextContent = textContent;
        body.mSourceKey = messageId + 0x1000;
        if (saveIt) {
            body.save(context);
        }
        return body;
    }

    /**
     * Create a test attachment. A few fields are specified by params, and all
     * other fields are generated using pseudo-unique values.
     *
     * @param messageId
     *            the message to attach to
     * @param fileName
     *            the "file" to indicate in the attachment
     * @param length
     *            the "length" of the attachment
     * @param flags
     *            the flags to set in the attachment
     * @param saveIt
     *            if true, write the new attachment directly to the DB
     * @param context
     *            use this context
     */
    public static Attachment setupAttachment(long messageId, String fileName,
            long length, int flags, boolean saveIt, Context context) {
        Attachment att = new Attachment();
        att.mSize = length;
        att.mFileName = fileName;
        att.mContentId = "contentId " + fileName;
        att.setContentUri("contentUri " + fileName);
        att.mMessageKey = messageId;
        att.mMimeType = "mimeType " + fileName;
        att.mLocation = "location " + fileName;
        att.mEncoding = "encoding " + fileName;
        att.mContent = "content " + fileName;
        att.mFlags = flags;
        att.mContentBytes = Utility.toUtf8("content " + fileName);
        att.mAccountKey = messageId + 0x1000;
        if (saveIt) {
            att.save(context);
        }
        return att;
    }

    /**
     * Create a test attachment with flags = 0 (see above).
     *
     * @param messageId
     *            the message to attach to
     * @param fileName
     *            the "file" to indicate in the attachment
     * @param length
     *            the "length" of the attachment
     * @param saveIt
     *            if true, write the new attachment directly to the DB
     * @param context
     *            use this context
     */
    public static Attachment setupAttachment(long messageId, String fileName,
            long length, boolean saveIt, Context context) {
        return setupAttachment(messageId, fileName, length, 0, saveIt, context);
    }

    /**
     * clear auto create tmp account.
     */
    public void clearTempAccount() {
        removeEmailAccount();
        /*
         * TestUtils.waitUntil("wait rm eas account", new Condition() {
         *
         * @Override public boolean isMet() { if (existTestAccount(true)) {
         * Log.d(TAG, "try remove tmp eas account success");
         * deleteTemporaryAccountManagerAccounts(true); return false; } else {
         * Log.d(TAG, "remove tmp eas account success"); return true; } } }, 5,
         * 500);
         */

        TestUtils.waitUntil("wait rm imap account", new Condition() {
            @Override
            public boolean isMet() {
                if (existTestAccount(false)) {
                    Log.d(TAG, "try remove tmp imap account success");
                    deleteTemporaryAccountManagerAccounts(false);
                    return false;
                } else {
                    Log.d(TAG, "remove tmp imap account success");
                    return true;
                }
            }
        }, 5, 500);
        sleepAndWait(WAIT_FOR_DB_OPERATION);
    }

    /**
     * auto setup test account and message.
     */
    public void setupTempAccount() {
        /*
         * if (!existTestAccount(true)) { Log.d(TAG, "setup tmp eas account");
         * setupTestMessage(MESSAGE_COUNT, EmailDataUtlities.TYPE_EAS,
         * "ABS001_EAS", "ABS_001@gmail.com"); }
         */
        if (!existTestAccount(false)) {
            Log.d(TAG, "setup tmp imap account");
            setupTestMessage(MESSAGE_COUNT, EmailDataUtlities.TYPE_IMAP,
                    "ABS002_IMAP", "ABS_002@gmail.com");
        }
        sleepAndWait(WAIT_FOR_DB_OPERATION);
    }

    private void removeEmailAccount() {
        final Cursor accountCursor = mContext.getContentResolver().query(
                Account.CONTENT_URI, Account.CONTENT_PROJECTION, null, null,
                null, null);
        if (accountCursor == null) {
            return;
        }
        try {
            while (accountCursor.moveToNext()) {
                final Account account = new Account();
                account.restore(accountCursor);
                Account.delete(mContext, Account.CONTENT_URI, account.mId);
            }
        } finally {
            accountCursor.close();
        }
    }

    /**
     * clean tmp account.
     */
    public void cleanAll() {
        // clean account
        removeEmailAccount();
        // clean mailbox
        final Cursor mailboxCursor = mContext.getContentResolver().query(
                Mailbox.CONTENT_URI, Mailbox.CONTENT_PROJECTION, null, null,
                null, null);
        if (mailboxCursor == null) {
            return;
        }
        try {
            while (mailboxCursor.moveToNext()) {
                final Mailbox mailbox = new Mailbox();
                mailbox.restore(mailboxCursor);
                Mailbox.delete(mContext, Mailbox.CONTENT_URI, mailbox.mId);
            }
        } finally {
            mailboxCursor.close();
        }
        // clean message.
        final Cursor messageCursor = mContext.getContentResolver().query(
                Message.CONTENT_URI, Message.CONTENT_PROJECTION, null, null,
                null, null);
        if (messageCursor == null) {
            return;
        }
        try {
            while (messageCursor.moveToNext()) {
                final Message message = new Message();
                message.restore(messageCursor);
                Message.delete(mContext, Message.CONTENT_URI, message.mId);
            }
        } finally {
            messageCursor.close();
        }
    }
}
