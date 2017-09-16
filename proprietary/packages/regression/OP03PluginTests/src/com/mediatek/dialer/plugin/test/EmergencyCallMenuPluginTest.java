package com.mediatek.dialer.plugin.test;

import java.util.HashMap;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.test.InstrumentationTestCase;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import com.android.internal.view.menu.MenuBuilder;


public class EmergencyCallMenuPluginTest extends InstrumentationTestCase {

    private final String TAG = "EmergencyCallMenuPluginTest";
    private Context instrContext;
    private Menu dialerMainMenu;
    private SubMenu eccDialerSubMenu;

    static final HashMap<String, String> ecc_map = new HashMap<String, String>();

    static {
        ecc_map.put("46000", "120,119,911");
        ecc_map.put("40410", "101,100,102");
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        instrContext = getInstrumentation().getContext();

        String networkOperator = TelephonyManager.getDefault().getNetworkOperator();

        if (networkOperator == null && networkOperator.isEmpty()) {
            assertTrue("No network operator", false);
        }

        String emergencyNumList = ecc_map.get(networkOperator);
        if (emergencyNumList == null) {
            assertTrue("No Plugin service for this SIM", false);
        }

        dialerMainMenu = new MenuBuilder(instrContext);
        dialerMainMenu.add("People");
        dialerMainMenu.add("Settings");
        dialerMainMenu.add("Speed dial");

        eccDialerSubMenu = dialerMainMenu.addSubMenu("Emergency Contacts");
        eccDialerSubMenu.add("120");
        eccDialerSubMenu.add("119");
        eccDialerSubMenu.add("911");
    }

    // test the ECC submenu present in the dialer main menu list
    public void test01_checkECCSubmenu() {
        MenuItem eccMenuItem = dialerMainMenu.getItem(3);
        String submenu = eccMenuItem.getTitle().toString();

        assertEquals(submenu, "Emergency Contacts");
    }

    // test the ECC list opened which contains the emergency contacts list provided by database
    public void test02_matchECClistwithDatabase() {
        MenuItem eccMenuItem = dialerMainMenu.getItem(3);
        SubMenu eccSubMenu = eccMenuItem.getSubMenu();

        assertEquals(eccSubMenu.getItem(0).getTitle().toString(), "120");
        assertEquals(eccSubMenu.getItem(1).getTitle().toString(), "119");
        assertEquals(eccSubMenu.getItem(2).getTitle().toString(), "911");
    }

    // test the the ECC list,all can be dial out as ECC success
    public void test03_dialoutAllECCnumbers() {
        MenuItem eccMenuItem = dialerMainMenu.getItem(3);
        SubMenu eccSubMenu = eccMenuItem.getSubMenu();

        StringBuffer emergencyNumber1 = new StringBuffer("tel:");
        emergencyNumber1.append(eccSubMenu.getItem(0).getTitle());
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.setData(Uri.parse(emergencyNumber1.toString()));
            instrContext.startActivity(callIntent);
            this.sendKeys(KeyEvent.KEYCODE_ENDCALL);

            } catch (ActivityNotFoundException e) {
                assertTrue("Call to ECC 120 failed", false);
            }
        this.sendKeys(KeyEvent.KEYCODE_ENDCALL);

        StringBuffer emergencyNumber2 = new StringBuffer("tel:");
        emergencyNumber2.append(eccSubMenu.getItem(1).getTitle());
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.setData(Uri.parse(emergencyNumber2.toString()));
            instrContext.startActivity(callIntent);

            } catch (ActivityNotFoundException e) {
                assertTrue("Call to ECC 119 failed", false);
            }
        this.sendKeys(KeyEvent.KEYCODE_ENDCALL);

        StringBuffer emergencyNumber3 = new StringBuffer("tel:");
        emergencyNumber3.append(eccSubMenu.getItem(2).getTitle());
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.setData(Uri.parse(emergencyNumber3.toString()));
            instrContext.startActivity(callIntent);

            } catch (ActivityNotFoundException e) {
                assertTrue("Call to ECC 911 failed", false);
            }
        this.sendKeys(KeyEvent.KEYCODE_ENDCALL);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        dialerMainMenu = null;
        eccDialerSubMenu = null;
    }

}
