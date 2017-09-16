package com.mediatek.wifi.plugin.test;


import android.app.Instrumentation;
import android.content.Context;
import android.preference.ListPreference;
import android.provider.Settings;
import android.test.InstrumentationTestCase;


public class WifiSleepPolicyTest extends InstrumentationTestCase {
    private Context context;
    private Instrumentation mInst = null;
    private int mSleepPolicyOptions = 3;
    private String[] entries = { "Always", "Only when plugged in", "Never" };
        private String[] entryValues = { "2", "1", "0" };
        private ListPreference mSleepPolicyPref;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInst = getInstrumentation();
        context = this.getInstrumentation().getContext();
      }


    @Override
    protected void tearDown() throws Exception {
        if (mInst != null) {
            mInst = null;
        }
        super.tearDown();
       }
        private void setSleepPolicyPreference(ListPreference sleepPolicyPref, String[] entriesArray, String[] valuesArray) {		
			String[] newEntries = {entriesArray[1], entriesArray[2]};
			String[] newValues = {valuesArray[1],valuesArray[2]};			
			sleepPolicyPref.setEntries(newEntries);
			sleepPolicyPref.setEntryValues(newValues);		
    }
        public void test01_areSleepPolicyOptions_two() {
        mSleepPolicyPref = new ListPreference(context);
            mSleepPolicyPref.setEntries(entries);
            mSleepPolicyPref.setEntryValues(entryValues);
    	setSleepPolicyPreference(mSleepPolicyPref,entries,entryValues);    	
        CharSequence[] entries = mSleepPolicyPref.getEntries();
        mSleepPolicyOptions = entries.length;
        assertTrue("test01 Failed", (mSleepPolicyOptions == 2));
  }

        public void test02_isSleepPolicyOption_Always_Removed() {
        int sleepPolicy = Settings.Global.getInt(context.getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY, Settings.Global.WIFI_SLEEP_POLICY_NEVER);
                boolean condition = (boolean) (sleepPolicy != Settings.Global.WIFI_SLEEP_POLICY_NEVER);
                assertTrue("Test02_failed " + condition + sleepPolicy, condition);

  }
}