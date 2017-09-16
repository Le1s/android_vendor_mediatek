package com.hesine.nmsg.db;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hesine.nmsg.Application;

public class LocalStore {

	public final static String TABLE_MESSAGES 		= "messages";
	public final static String TABLE_MESSAGE_ITEM	= "message_item";
	public final static String TABLE_ACCOUNTS	 	= "accounts";
	public final static String TABLE_USERINFO		= "userinfo";	
	
    private final static String TAG = "LocalStore";

    private final static int DB_VERSION = 5;
    public final static String DB_NAME = "localstore.db";
    
    private static LocalStore mLocalStore = null;
    private String mDbDir;
    private SQLiteDatabase mDatabase = null;

    
    public synchronized static LocalStore instance() {
        if (mLocalStore == null) {
            mLocalStore = new LocalStore();
        }
        return mLocalStore;
    }

    private LocalStore() {
        mDbDir = Application.getInstance()
    			.getApplicationContext().getFilesDir().getAbsolutePath() + "/";
        openDatabase();
    }
    
    private void openDatabase() {
        
        SQLiteDatabase db = null;
        String dbFileName = mDbDir + DB_NAME;
        try {
        	Context context = Application.getInstance()
        			.getApplicationContext();
            if (!new File(dbFileName).exists()) {
                db = context.openOrCreateDatabase(dbFileName, Context.MODE_PRIVATE, null);
                
            } else {
                db = context.openOrCreateDatabase(dbFileName, Context.MODE_PRIVATE, null);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db == null) {
            return;
        }
        mDatabase = db;

    }

    public void closeDatabase() {
        synchronized (mLocalStore) {
            if (mDatabase != null) {
                mDatabase.close();
                mDatabase = null;
            }
        }
    }
    
    public synchronized long insert(String table, String nullColumnHack, ContentValues values) {  
    	return mDatabase.insert(table, nullColumnHack, values);
    }
    
    public synchronized int update(String table, ContentValues values, String whereClause, String[] whereArgs) {  
    	return mDatabase.update(table, values, whereClause, whereArgs);
    }
    
    public synchronized long replace(String table, String nullColumnHack, ContentValues initialValues) {  
    	return mDatabase.replace(table, nullColumnHack, initialValues);
    }
    
    public synchronized long replaceOrThrow (String table, String nullColumnHack, ContentValues initialValues) {  
    	return mDatabase.replaceOrThrow(table, nullColumnHack, initialValues);
    }

    public synchronized Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {   
        Cursor c = mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);  
        return c;  
    }
    public synchronized Cursor rewquery(String sql,String [] selectionArgs) {   
    	Cursor c = mDatabase.rawQuery(sql, selectionArgs);
    	return c;  
    }
    
    public synchronized int del(String tableName, int id) {  
        return mDatabase.delete(tableName, "_id=?", new String[] { String.valueOf(id) });  
    }
    
    public synchronized int delete(String tableName, String whereClause, String[] whereArgs) {  
        return mDatabase.delete(tableName, whereClause, whereArgs);  
    }
    
    public synchronized SQLiteDatabase getDatabase(){
    	return mDatabase;
    }
    
    public void delete() {
    	synchronized (mLocalStore) {

			try {
				if(mDatabase != null){
					mDatabase.close();
					mDatabase = null;
				}
			} catch (Exception e) {
				Log.i(TAG, "delete(): Unable to close db");
			}
			
			try {
				File dbFileName = new File(mDbDir + DB_NAME);
				if(dbFileName.exists()){
					dbFileName.delete();
				}
			} catch (Exception e) {
				Log.i(TAG, "delete(): Unable to delete backing DB file");
			}
		}
    }
    
    public Boolean isDbUpgrade(){
    	if(mDatabase.getVersion() < DB_VERSION)
    		return true;
    	return false;
    }
    
    public void dbDbUpgrade(){
    	synchronized (mLocalStore) {
    		int ver = mDatabase.getVersion();
    		if(ver < DB_VERSION){
    			mDatabase.execSQL("DROP TABLE IF EXISTS messages");
    			mDatabase.execSQL("CREATE TABLE messages (" +
                    "_id INTEGER PRIMARY KEY, " +
                    "account TEXT, " +
                    "thread_id INTEGER default 0, " +
                    "sms_id INTEGER default 0, " +
                    "msg_uuid TEXT UNIQUE, " +
                    "msg_type INTEGER  default 0, " +
                    "sender TEXT, " +
                    "receiver TEXT, " +
                    "sms TEXT, " +
                    "status INTEGER  default 0, " +
                    "update_time INTEGER" +
                    ")");
    			
    			mDatabase.execSQL("DROP TABLE IF EXISTS message_item");
    			mDatabase.execSQL("CREATE TABLE message_item (" +
    					"_id INTEGER PRIMARY KEY, " +
                        "msg_uuid TEXT, " +
                        "item_id INTEGER default 0, " +
                        "subject TEXT, " +
                        "desc TEXT, " +
                        "short_link TEXT, " +
                        "long_link TEXT, " +
                        "body TEXT, " +
                        "attach_type TEXT, " +
                        "attach_name TEXT, " +
                        "attach_size TEXT, " +
                        "attach_url TEXT, " +
                        "attachment TEXT" +
                        ")");

    			mDatabase.execSQL("DROP TABLE IF EXISTS accounts");
    			mDatabase.execSQL("CREATE TABLE accounts (" +
    					"_id INTEGER PRIMARY KEY, " +
                        "account TEXT, " +
                        "account_name TEXT, " +
                        "email TEXT, " +
                        "phone_number TEXT, " +
                        "desc TEXT, " +
                        "status INTEGER default 1, " +
                        "icon TEXT, " +
                        "is_insert INTEGER default 0, " +
                        "is_exist INTEGER default 0, " +
                        "update_time INTEGER" +
                        ")");
    			
    			mDatabase.execSQL("DROP TABLE IF EXISTS userinfo");
    			mDatabase.execSQL("CREATE TABLE userinfo (" +
    					"_id INTEGER PRIMARY KEY, " +
                        "account TEXT, " +
                        "user_name TEXT, " +
                        "email TEXT, " +
                        "phone_number TEXT, " +
                        "user_sex  int, " +
                        "icon int, " +
                        "sign TEXT, " +
                        "age INTEGER, " +                        
                        "update_time INTEGER" +                        
                        ")");
    			
    			new Thread(new Runnable() {
					@Override
					public void run() {
		    			mDatabase.execSQL("CREATE INDEX messages_idx_tid on messages(thread_id)");
		    			mDatabase.execSQL("CREATE INDEX messages_idx_sid on messages(sms_id)");
		    			mDatabase.execSQL("CREATE INDEX messages_idx_uuid on messages(msg_uuid)");
		    			mDatabase.execSQL("CREATE INDEX messages_idx_sms on messages(sms)");
		    			mDatabase.execSQL("CREATE INDEX messages_idx_time on messages(update_time)");
		    			mDatabase.execSQL("CREATE INDEX message_item_idx_uuid on message_item(msg_uuid)");
		    			mDatabase.execSQL("CREATE INDEX message_item_idx_id on message_item(item_id)");
		    			mDatabase.execSQL("CREATE INDEX message_item_idx_body on message_item(body)");
		    			mDatabase.execSQL("CREATE INDEX message_item_idx_desc on message_item(desc)");
		    			mDatabase.execSQL("CREATE INDEX accounts_idx_account on accounts(account)");
		    			mDatabase.execSQL("CREATE INDEX accounts_idx_name on accounts(account_name)");
		    			mDatabase.execSQL("CREATE INDEX accounts_idx_insert on accounts(is_insert)");
		    			mDatabase.execSQL("CREATE INDEX accounts_idx_time on accounts(update_time)");
		    			mDatabase.execSQL("CREATE INDEX userinfo_idx_name on userinfo(user_name)");
		    			mDatabase.execSQL("CREATE INDEX userinfo_idx_number on userinfo(phone_number)");
					}
				});
    		}
    		mDatabase.setVersion(DB_VERSION);
    	}
    }
    
}


