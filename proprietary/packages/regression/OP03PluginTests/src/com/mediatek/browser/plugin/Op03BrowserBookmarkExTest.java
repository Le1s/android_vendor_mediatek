package com.mediatek.browser.plugin.test;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BrowserContract;
import android.test.InstrumentationTestCase;
import android.widget.EditText;

import com.mediatek.browser.ext.IBrowserBookmarkExt;
import com.mediatek.browser.plugin.Op03BrowserBookmarkExt;
import com.mediatek.common.MPlugin;
import com.mediatek.common.PluginImpl;
//import com.mediatek.pluginmanager.Plugin;
//import com.mediatek.pluginmanager.PluginManager;
import com.mediatek.xlog.Xlog;

@PluginImpl(interfaceName="com.mediatek.browser.ext.IBrowserBookmarkExt")
public class Op03BrowserBookmarkExTest extends InstrumentationTestCase
{
    private final String TAG = "Op03BrowserBookmarkExTest";
    private static Op03BrowserBookmarkExt mPlugin = null;
    private Context mContext;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = this.getInstrumentation().getContext();
        //Object plugin = PluginManager.createPluginObject(mContext, "com.mediatek.browser.ext.IBrowserBookmarkExt");
	  /* Object plugin = (IBrowserBookmarkExt) MPlugin.createInstance(
                                        IBrowserBookmarkExt.class.getName(), mContext);

        if(plugin instanceof Op03BrowserBookmarkExt) {
            mPlugin = (Op03BrowserBookmarkExt) plugin;
        } */
    }

    @Override    
    protected void tearDown() throws Exception {
        super.tearDown();
        mPlugin = null;
    }

    public void test01_addDefaultBookmarksForCustomer(){
	  int count = 10;
        //if(mPlugin != null){
            String where = BrowserContract.Bookmarks.PARENT + " = ?";
            Cursor cursor = mContext.getContentResolver().query(
            BrowserContract.Bookmarks.CONTENT_URI, 
            new String[]{BrowserContract.Bookmarks.TITLE, BrowserContract.Bookmarks.URL},
            where, 
            new String[]{String.valueOf(1)},
            null);

            if(cursor != null && cursor.getCount() > 0) {
            Xlog.i(TAG, "Number of rows in result " +cursor.getCount());
		assertEquals(count, cursor.getCount());
		cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
            String url = cursor.getString(1);
            String title = cursor.getString(0);
            Xlog.i(TAG, "DATA read from DB = " + url + ", " + title);
            cursor.moveToNext();
          }
	    cursor.close();
        }
    //}
}
}
