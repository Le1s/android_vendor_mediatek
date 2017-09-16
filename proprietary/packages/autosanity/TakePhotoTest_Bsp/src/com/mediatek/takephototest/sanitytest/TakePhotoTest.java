package com.mediatek.takephototest.sanitytest;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.app.Instrumentation;
import android.media.AudioManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.storage.StorageManager;

import com.android.camera.CameraActivity;
import com.android.camera.CameraModule;
import com.android.camera.PhotoModule;
import com.android.camera2.R;
import com.android.camera.Storage;
import com.jayway.android.robotium.solo.Solo;

import java.lang.Integer;

/**
 * @author MTK54140
 */
public class TakePhotoTest extends
        ActivityInstrumentationTestCase2<CameraActivity> {
    
    private Activity mActivity = null;
    private Solo solo;
    private int id;
    // private ShutterButton photoButton;
    private ImageView playButton;
    private static final String TAG = "TakePhotoTest";
    private int WAIT_FOR_SYNC = 5000;
    private int RETRY_TIME = 5;
    private String filter = ".jpg";
    private static String Photo_FILE_PATH;
    
    /**
     * constructor
     */
    public TakePhotoTest() {
        super(CameraActivity.class);
        
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Log.v(TAG, "setUp start");
        setActivityInitialTouchMode(false);
        Thread.sleep(500);
        solo = new Solo(getInstrumentation(), getActivity());
        mActivity = (CameraActivity) getActivity();
        Photo_FILE_PATH = Storage.DIRECTORY;
        Log.v(TAG, "setUp end");
    }
    
    public void tearDown() throws Exception {
        getActivity().finish();
        super.tearDown();
        SystemClock.sleep(2000);
    }
    
    // public void testAppguide() throws Exception {
    // Log.v(TAG, "testAppguide start");
    // SharedPreferences settings = mActivity.getSharedPreferences(
    // "application_guide", 0);
    // SharedPreferences.Editor editor = settings.edit();
    // editor.putBoolean("camera_guide", true);
    // editor.commit();
    // Log.v(TAG, "testAppguide end");
    // }
    
    public void testTakephoto() throws Exception {
        Log.v(TAG, "testTakephoto start");
        SystemClock.sleep(2000);
        int CntBefore = GetFileCntInDir(Photo_FILE_PATH, filter);
//        chooseMode(0);
        SystemClock.sleep(5000);
        removeGuide();
        SystemClock.sleep(2000);
        capture();
        SystemClock.sleep(5000);
        int CntAfter = GetFileCntInDir(Photo_FILE_PATH, filter);
        assertEquals((CntBefore + 1), CntAfter);
        SystemClock.sleep(3000);
        Log.v(TAG, "testTakephoto end");
    }
    
    private void removeGuide() {
        Log.i(TAG, "removeGuide");
        int i = 0;
        boolean searched = solo.searchText("NEXT");
        Log.i(TAG, "searched = " + searched);
        while (!searched && i < 2) {
            searched = solo.searchText("NEXT");
            Log.i(TAG, "searched = " + searched);
            SystemClock.sleep(2000);
            i++;
        }
        if (searched) {
            solo.clickOnText("NEXT");
            SystemClock.sleep(2000);
        }
    }

    private void capture() {
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "capture");
                        Field field = CameraActivity.class
                                .getDeclaredField("mCurrentModule");
                        field.setAccessible(true);
                        CameraModule cameraModule = (PhotoModule) field
                                .get(mActivity);
                        cameraModule.onShutterButtonClick();
                    } catch (NoSuchFieldException e) {
                        Log.i(TAG, "no such method exception");
                    } catch (IllegalAccessException e) {
                        Log.i(TAG, "IllegalAccessException");
                    }
                }
            });
        } catch (Throwable e) {
            // TODO: handle exception
        }
    }

    
    private void chooseMode(final int mode) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "chooseMode" + mode);
                        Method onModeSelectedMethod = CameraActivity.class.getDeclaredMethod(
                                "onModeSelected", int.class);
                        onModeSelectedMethod.setAccessible(true);
                        onModeSelectedMethod.invoke(mActivity, mode);
                    } catch (NoSuchMethodException e) {
                        Log.i(TAG, "no such method exception");
                    } catch (IllegalAccessException e) {
                        Log.i(TAG, "IllegalAccessException");
                    } catch (InvocationTargetException e) {
                        Log.i(TAG, "InvocationTargetException");
                    }
                }
            });
        } catch (Throwable e) {
            // TODO: handle exception
        }
    }
    
    private static class FileFilterByName implements FilenameFilter {
        private String mFilter;
        
        public FileFilterByName(String filter) {
            mFilter = filter;
        }
        
        public boolean isRightTypeFile(String file) {
            if (file.toLowerCase().endsWith(mFilter)) {
                return true;
            } else {
                return false;
            }
        }
        
        public boolean accept(File dir, String filename) {
            // TODO Auto-generated method stub
            return isRightTypeFile(filename);
        }
    }
    
    private int GetFileCntInDir(String dirName, String filter) {
        int cnt = 0;
        File fdir = new File(dirName);
        if (true == fdir.isDirectory()) {
            File[] fls = fdir.listFiles(new FileFilterByName(filter));
            
            for (int i = 0; i < fls.length; i++) {
                Log.i(TAG, "" + i + fls[i].getName());
                if (true == fls[i].isFile()) {
                    cnt++;
                }
            }
        }
        Log.i(TAG, "cnt: " + cnt);
        return cnt;
    }
    
    public void deleteFileInCameraFld() {
        File CameraDir = new File(Photo_FILE_PATH);
        Log.i(TAG, "***************CameraDir = " + CameraDir);
        Log.i(TAG, "***************CameraDir is Dir?" + CameraDir.isDirectory());
        if (true == CameraDir.isDirectory()) {
            File[] CameraDirChildren = CameraDir.listFiles();
            Log.i(TAG, "*************Before File Cnt = "
                    + CameraDirChildren.length);
            for (int i = 0; i < CameraDirChildren.length; i++) {
                Log.v("ls file ", i + CameraDirChildren[i].getName());
                if (true == CameraDirChildren[i].isFile()) {
                    Log.i(TAG, "delete result=" + CameraDirChildren[i].delete());
                }
            }
            File[] CameraDirChildrenAfter = CameraDir.listFiles();
            Log.i(TAG, "***********After File Cnt = "
                    + CameraDirChildrenAfter.length);
        } else {
            Log.i(TAG, "**********ERROR!!!!!CameraDir is not a directory");
        }
        
    }
}
