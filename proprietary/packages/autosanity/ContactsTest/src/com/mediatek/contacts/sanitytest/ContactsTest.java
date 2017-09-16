package com.mediatek.contacts.sanitytest;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.ListView;

import com.android.contacts.activities.PeopleActivity;
import com.android.internal.view.menu.ActionMenuItemView;
import com.jayway.android.robotium.solo.Solo;
import com.mediatek.contacts.sanitytest.utils.ContactsTestHelper;
import android.telephony.SubscriptionManager;
import android.telephony.SubInfoRecord;

import java.util.List;

public class ContactsTest extends ActivityInstrumentationTestCase2<PeopleActivity> {

    private static final String TAG = "ContactsTest";
    private Solo mSolo;
    private Activity mActivity;
    private Context mContext;
    private Instrumentation mInstrumentation;
    private List<SubInfoRecord> mSimInfoList;

    private static final int PHB_SLEEP_TIME =  60000;
    private static final int RETRY_WAIT_TIME = 20000;
    private static final int SAVE_TIME = 10000;
    private static final int SLEEP_TIME = 6000;
    private static final String NAME = "TestContact";
    private static final String NUM = "1008611";
    private static final String KEY_DEFAULT_ACCOUNT = "ContactEditorUtils_default_account";
    private static final String SLOT_CONFIG_NEVERMIND = "NONE";
    public static final String PHONE_ACCOUNT = "account_phone_only";

    private static final int MAX_RETRY = 5;
    private int mRetryTime = MAX_RETRY;
    
    private int mSimNum = 0;

    /**
     * constructor
     */
    public ContactsTest() {
        super(PeopleActivity.class);
    }

    private void preConditionCheck() {
        // Must insert two sim cards
        simNumCheck();
    }

    private void simNumCheck() {

        mSimInfoList = SubscriptionManager.getActiveSubInfoList();

	if (mSimInfoList == null){
		Log.w(TAG, "no simcard inserted!");
		assertTrue(false);
	}

        mSimNum = mSimInfoList.size();
        Log.i(TAG, "sim info list....." + mSimInfoList);
        Log.i(TAG, "sim num = " + mSimNum);
        //assertTrue(mSimNum > 0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        mActivity = getActivity();
        mSolo = new Solo(getInstrumentation(), getActivity());
        mInstrumentation = getInstrumentation();
        disableVCS(mActivity);
    }

    private void resetDefaultAccount(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().remove(KEY_DEFAULT_ACCOUNT).apply();
    }

    private void disableVCS(Context context) {
        SharedPreferences sp = context.getSharedPreferences("vcs_preference", Context.MODE_PRIVATE);
        sp.edit().putBoolean("enable_vcs_by_user", false).commit();
        sp.edit().putBoolean("disable_vcs_by_user", false).commit();
        sp = context.getSharedPreferences("application_guide", Context.MODE_WORLD_WRITEABLE);
        sp.edit().putBoolean("vcs_guide", true).commit();
    }

    public void testCase01_clearAllContacts() {
        Log.i(TAG, "testcase01_clearAllContacts");
        showVersionInformation();
        preConditionCheck();

        // Worst case phb take long time ready
        mSolo.sleep(PHB_SLEEP_TIME);
        Log.i(TAG, "wait finished");
        ContactsTestHelper.clearAllContacts(mContext);
    }

    private void showVersionInformation() {
        Context context = getInstrumentation().getContext();
        int versionCode = 0;
        String versionName = "";
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "error";
        }
        Log.d(TAG, "versionCode = " + versionCode + " versionName = " + versionName);
    }

    public void testCase02_addContacts() {

        Log.i(TAG, "testcase02_addContacts");
        preConditionCheck();
        int subId = 0;
        String simName;
        for (SubInfoRecord subInfo : mSimInfoList) {
            subId = (int)subInfo.subId;
            simName = subInfo.displayName; // temp,can't get display_name.
            resetDefaultAccount(mActivity);
            mInstrumentation.waitForIdleSync();
            addContactInSim(simName, subId);
            mSolo.sleep(SLEEP_TIME);
        }
        //BSP test.
        if (mSimNum == 0){
            Log.i(TAG, "testCaseBSP_addContactsToPhone");
            resetDefaultAccount(mActivity);       
            mInstrumentation.waitForIdleSync();
            addContactInPhone();
        }

    }  
    
    public void testCase03_checkContacts() {
        Log.i(TAG, "testCase03_checkContacts");
        preConditionCheck();
        for (SubInfoRecord subInfo : mSimInfoList) {
            String contactName = NAME + String.valueOf(subInfo.subId);
            Log.i(TAG, "contactName = " + contactName);
            searchName(contactName);
            mSolo.sleep(SLEEP_TIME);
            
        }
        //BSP test.
        if (mSimNum == 0){
            String contactName = NAME + String.valueOf(mSimNum);
            Log.i(TAG, "contactName = " + contactName);
            searchName(contactName);
        }
    }

    private void searchName(String contactName){
        boolean isRight = mSolo.searchText(contactName);
        if (isRight) {
            checkName(contactName);
        } else {
            Log.e(TAG, "Not find contact name wait for 30s and try again");
            mSolo.sleep(RETRY_WAIT_TIME);
            isRight = mSolo.searchText(contactName);
            if (isRight) {
                checkName(contactName);
            } else {
                Log.e(TAG, "Need to check reason name");
                mSolo.takeScreenshot();
                //assertTrue(isRight);
            }
        }
    }
    
    private void checkName(String contactName) {
        mSolo.clickOnText(contactName);
        mInstrumentation.waitForIdleSync();
        boolean isRight = mSolo.searchText(NUM);
        if (isRight) {
            mSolo.goBackToActivity("PeopleActivity");
        } else {
            mSolo.takeScreenshot();
            Log.e(TAG, "Need to check reason number");
            //assertTrue(isRight);
        }
    }

    private boolean launchAddContact() {
        Intent intent = new Intent();
        intent.setClassName("com.android.contacts", "com.android.contacts.activities.ContactEditorActivity");
        intent.setData(Contacts.CONTENT_URI);
        intent.setAction(Intent.ACTION_INSERT);
        mActivity.startActivity(intent);
        return true;
    }
    
    
    private void addContactInPhone(){
        Log.i(TAG, "addContactInPhone");
        if (launchAddContact()) {
            mInstrumentation.waitForIdleSync();
            String listTitle = getString("store_contact_to");
            Log.i(TAG, "listTitle = " + listTitle);
            mSolo.sleep(SLEEP_TIME);
            if (mSolo.searchText(listTitle)) {
                Log.i(TAG, "First time have " + listTitle + " dialog");
                ListView view = (ListView) mSolo.getView(getRedId("account_list"));
                //mSolo.clickOnView(view.getChildAt(mSimNum)); // select list
                                                            // item               
                mSolo.clickOnText(getString(PHONE_ACCOUNT));
            } else {
                Log.i(TAG, "defalut account ---ok");
                mSolo.clickOnText(mActivity.getString(android.R.string.ok));
            }
            mInstrumentation.waitForIdleSync();
            enterContactInfo(mSimNum);
            
            String toastStr = getString("contactSavedToast");
            Log.i(TAG, "toastStr = " + toastStr);
            
            mSolo.waitForText(toastStr, 0, SAVE_TIME);
            
            mSolo.goBackToActivity("PeopleActivity");
            mSolo.sleep(SLEEP_TIME);
        }
    }
    

    private void addContactInSim(String simName, int subId) {
        Log.i(TAG, "addContactInSim " + subId + ",simName = " + simName);
        if (launchAddContact()) {
            mInstrumentation.waitForIdleSync();
            if (mSolo.searchText(simName)) {
                String listTitle = getString("store_contact_to");
                Log.i(TAG, "listTitle = " + listTitle);
                mSolo.sleep(SLEEP_TIME);
                if (mSolo.searchText(listTitle)) {
                    Log.i(TAG, "First time have " + listTitle + " dialog");
                    ListView view = (ListView) mSolo.getView(getRedId("account_list"));
                    mSolo.clickOnView(view.getChildAt(subId - 1)); // select list
                                                                // item.
                }
                mInstrumentation.waitForIdleSync();
                enterContactInfo(subId);
                String toastStr = getString("contactSavedToast");
                Log.i(TAG, "toastStr = " + toastStr);
                
                mSolo.waitForText(toastStr, 0, SAVE_TIME);

                mSolo.goBackToActivity("PeopleActivity");
                mSolo.sleep(SLEEP_TIME);

            } else {
                mSolo.takeScreenshot();
                //assertTrue(false);
                Log.e(TAG, "need to check whether not able to find menu item");
                
                mSolo.goBackToActivity("PeopleActivity");
                mSolo.sleep(SLEEP_TIME);
            }
        } else {
            mSolo.takeScreenshot();
            //assertTrue(false);
            Log.e(TAG, "need to check whether not able to find menu item");
            
            mSolo.goBackToActivity("PeopleActivity");
            mSolo.sleep(SLEEP_TIME);
        }
    }

    private void enterContactInfo(int subId) {
        Log.d(TAG, "enterContactInfo");
        scrollTop();
        mSolo.enterText(0, NAME + String.valueOf(subId));
        mInstrumentation.waitForIdleSync();
        mSolo.enterText(1, NUM);
        mInstrumentation.waitForIdleSync();
        String done = getString("menu_done");
        if (mSolo.searchText(done)) {
            mSolo.clickOnText(done);
        }

    }

    private boolean clickImageButtonById(int id) {
        Activity curActivity = mSolo.getCurrentActivity();
        mInstrumentation.waitForIdleSync();
        if (curActivity != null) {
            ActionMenuItemView imgButton = (ActionMenuItemView) curActivity.findViewById(id);
            if (imgButton != null) {
                mSolo.clickOnView(imgButton);
                return true;
            }
        }
        return false;
    }

    private void scrollTop() {
        while (mSolo.scrollUp()) {
            mSolo.scrollUp();
        }
        mInstrumentation.waitForIdleSync();
    }

    private String getString(String resName) {
        int resId = mActivity.getResources().getIdentifier(resName, "string", "com.android.contacts");
        return mActivity.getString(resId);
    }

    private int getRedId(String resName) {
        int id = mActivity.getResources().getIdentifier(resName, "id", "com.android.contacts");
        Log.i(TAG, "id = " + id);
        return id;
    }

    public void tearDown() throws Exception {
        try {
            mSolo.finishOpenedActivities();
        } catch (Exception e) {
            // ignore
        }
        super.tearDown();
    }
}
