package com.mediatek.takephototest.sanitytest;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
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
import com.android.camera.R;
import com.android.camera.Storage;
import com.android.camera.ui.PickerButton;
import com.android.camera.ui.ShutterButton;
import com.jayway.android.robotium.solo.Solo;

public class TakePhotoTest extends
        ActivityInstrumentationTestCase2<CameraActivity> {
    
    private Activity mActivity = null;
    private Solo solo;
    private int id;
    private int switchId;
    private ShutterButton photoButton;
    private ImageView playButton;
    private ImageView switchButton;
    private static final String TAG = "TakePhotoTest";
    private int WAIT_FOR_SYNC = 2000;
    private int RETRY_TIME = 100;
    private String filter = ".jpg";
    private static String Photo_FILE_PATH;
    private int state;
    
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
        mActivity = getActivity();
        id = mActivity.getResources().getIdentifier(
                "com.android.gallery3d:id/shutter_button_photo", null, null);
        Log.v(TAG, "setUp +++++++++++id = " + id);
        photoButton = (ShutterButton) mActivity.findViewById(id);
        id = mActivity.getResources().getIdentifier(
                "com.android.gallery3d:id/thumbnail", null, null);
        playButton = (ImageView) mActivity.findViewById(id);
        Log.v(TAG, "setUp +++++++++++id = " + id + ", playButton = " + playButton);
        switchId = mActivity.getResources().getIdentifier(
                "com.android.gallery3d:id/onscreen_camera_picker", null, null);
        switchButton = (ImageView) mActivity.findViewById(switchId);
        Log.v(TAG, "setUp +++++++++++switchId = " + switchId + ", mActivity.findViewById(switchId) = " + mActivity.findViewById(switchId));
        Log.v(TAG, "setUp +++++++++++switchId = " + switchId + ", switchButton = " + switchButton);
        Photo_FILE_PATH = Storage.getFileDirectory();
        deleteFileInCameraFld();
        Log.v(TAG, "setUp end");
    }
    
    public void tearDown() throws Exception {
        getActivity().finish();
        super.tearDown();
        SystemClock.sleep(2000);
    }
    
    public void test01() throws Exception {
        Log.v(TAG, "testAppguide start");
        SharedPreferences settings = mActivity.getSharedPreferences(
                "application_guide", 0);
        boolean isLive = settings.getBoolean("camera_guide", false);
        Log.v(TAG, "testAppguide isLive = " + isLive);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("camera_guide", true);
        editor.commit();
        assertTrue(settings.getBoolean("camera_guide", false));
        Log.v(TAG, "testAppguide end");
    }
    
    public void test02() throws Exception {
        Log.v(TAG, "testTakephotoBack start");
        SystemClock.sleep(2000);
        int CntBefore = GetFileCntInDir(Photo_FILE_PATH, filter);
        checkViewStatus();
        Log.v(TAG, "click On Photo button!");
        solo.clickOnView(photoButton);
        checkViewStatus();
        SystemClock.sleep(2000);
        solo.clickOnScreen(442, 722);
        SystemClock.sleep(2000);
        solo.clickOnScreen(240, 322);
        SystemClock.sleep(3000);
        int CntAfter = GetFileCntInDir(Photo_FILE_PATH, filter);
        assertEquals((CntBefore + 1), CntAfter);
        solo.clickOnView(playButton);
        SystemClock.sleep(3000);
        Log.v(TAG, "testTakephotoBack end");
    }
    
//    public void test03() throws Exception {
//        Log.v(TAG, "testTakePhotoFront start");
//        SystemClock.sleep(2000);
//        int CntBefore = GetFileCntInDir(Photo_FILE_PATH, filter);
//        checkViewStatus();
//        Log.v(TAG, "click On switch button! switchButton = " + switchButton);
//        solo.clickOnView(switchButton);
//        checkViewStatus();
//        SystemClock.sleep(5000);
//        Log.v(TAG, "click On Photo button!");
//        solo.clickOnView(photoButton);
//        checkViewStatus();
//        SystemClock.sleep(2000);
//        solo.clickOnScreen(442, 722);
//        SystemClock.sleep(2000);
//        solo.clickOnScreen(240, 322);
//        SystemClock.sleep(3000);
//        int CntAfter = GetFileCntInDir(Photo_FILE_PATH, filter);
//        assertEquals((CntBefore + 1), CntAfter);
//        solo.clickOnView(playButton);
//        SystemClock.sleep(3000);
//        Log.v(TAG, "testTakephotoBack end");
//    }
    
    private void checkViewStatus() {
        Log.v(TAG, "checkViewStatus");
        for (int i = 0; i < RETRY_TIME; ++i) {
            if (photoButton != null && photoButton.isClickable()
                    && photoButton.isEnabled()) {
                Log.v(TAG, "shutter button is Ready!!!");
                break;
            }
            Log.v(TAG, "wait for shutter button Ready!!!");
            SystemClock.sleep(WAIT_FOR_SYNC);
        }
        assertTrue(photoButton != null);
        assertTrue(photoButton.isClickable());
        assertTrue(photoButton.isEnabled());
        
        
        for (int i = 0; i < RETRY_TIME; ++i) {
            SystemClock.sleep(WAIT_FOR_SYNC);
            if (state == 1) {
                Log.v(TAG, "Camera state is Idle!!! ");
                break;
            } else {
                checkCameraState();
                Log.v(TAG, "wait for Camera state Idle!!!");
            }
            SystemClock.sleep(WAIT_FOR_SYNC);
        }
    }
    
    private void checkCameraState() {
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "checkCameraState");
                        Field field = CameraActivity.class
                                .getDeclaredField("mCameraState");
                        field.setAccessible(true);
                        state = (Integer) field
                                .get(mActivity);
                        Log.i(TAG, "checkCameraState state = " + state);
                    } catch (NoSuchFieldException e) {
                        Log.i(TAG, "no such method exception");
                    } catch (IllegalAccessException e) {
                        Log.i(TAG, "IllegalAccessException");
                    }
                }
            });
        } catch (Throwable e) {
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
