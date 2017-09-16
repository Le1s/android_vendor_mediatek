package com.mediatek.recordvideotest.sanitytest;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.res.Configuration;
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

import com.android.camera.ui.PickerButton;
import com.android.camera.ui.ShutterButton;
import com.android.camera.R;
import com.android.camera.CameraActivity;
import com.android.camera.Storage;
import com.jayway.android.robotium.solo.Solo;

public class RecordVideoTest extends ActivityInstrumentationTestCase2<CameraActivity> {

    private Activity mActivity = null;
    private Instrumentation mInstrumentation = null;
    private Solo solo;
    private int id;
    private ShutterButton recordButton;
    private ImageView mThumbnailView;
    private PickerButton switchButton;
    private static final String TAG = "RecordVideoTest";
    private int WAIT_FOR_SYNC = 2000;
    private int RETRY_TIME = 100;
    private String Video_FILE_PATH;
    private String filter = ".3gp";
    private int state = 0;

    /**
     * constructor
     */
    public RecordVideoTest() {
        super(CameraActivity.class);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Log.v(TAG, "setUp start");
        solo = new Solo(getInstrumentation(), getActivity());
        mActivity = getActivity();
        id = mActivity.getResources().getIdentifier(
                "com.android.gallery3d:id/shutter_button_video", null, null);
        recordButton = (ShutterButton) mActivity.findViewById(id);

        id = mActivity.getResources().getIdentifier(
                "com.android.gallery3d:id/thumbnail", null, null);
        mThumbnailView = (ImageView) mActivity.findViewById(id);
        
        id = mActivity.getResources().getIdentifier(
                "com.android.gallery3d:id/onscreen_camera_picker", null, null);
        switchButton = (PickerButton) mActivity.findViewById(id);
        Video_FILE_PATH = Storage.getFileDirectory();
        deleteFileInVideoFld();
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
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("camera_guide", true);
        editor.commit();
        Log.v(TAG, "testAppguide end");
    }

    public void record() {
        int CntBefore = GetFileCntInDir(Video_FILE_PATH, filter);
        checkViewStatus();
        solo.clickOnView(recordButton);
        SystemClock.sleep(20000);
        checkViewStatus();
        solo.clickOnView(recordButton);
       SystemClock.sleep(WAIT_FOR_SYNC);
        checkViewStatus();
        solo.clickOnScreen(442, 722);
        SystemClock.sleep(WAIT_FOR_SYNC);
        solo.clickOnScreen(240, 322);
        SystemClock.sleep(WAIT_FOR_SYNC);
        int CntAfter = GetFileCntInDir(Video_FILE_PATH, filter);
        assertEquals((CntBefore + 1), CntAfter);
        Log.v(TAG, "mThumbnailView" + mThumbnailView);
        solo.clickOnView(mThumbnailView);
        SystemClock.sleep(5000);
        if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            solo.clickOnScreen(593, 391);
        } else {
            solo.clickOnScreen(363, 591);
        }
    }

//    public void test03() throws Throwable {
//        Log.v(TAG, "testRecordvideoFront start");
//        checkViewStatus();
//        Log.v(TAG, "click On switch button! switchButton = " + switchButton);
//        solo.clickOnView(switchButton);
//        checkViewStatus();
//        record();
//        Log.v(TAG, "testRecordvideoFront end");
//    }
    
    public void test02() throws Throwable {
        Log.v(TAG, "testRecordvideoback start");
        SystemClock.sleep(WAIT_FOR_SYNC);
        record();
        Log.v(TAG, "testRecordvideoback end");
    }
    
    
    private void checkViewStatus() {
        Log.v(TAG, "checkViewStatus");
        for (int i = 0; i < RETRY_TIME; ++i) {
            if (recordButton != null && recordButton.isClickable() && recordButton.isEnabled()) {
                Log.v(TAG, "shutter button is ready!!!");
                break;
            }
            Log.v(TAG, "wait for Shutter button ready!!!");
            SystemClock.sleep(WAIT_FOR_SYNC);
        }
        assertTrue(recordButton != null);
        assertTrue(recordButton.isClickable());
        assertTrue(recordButton.isEnabled());
        
        

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

    private static class FileFilterByName implements FilenameFilter/* .3gp */
    {
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

    public void deleteFileInVideoFld() {
        File VideoDir = new File(Video_FILE_PATH);
        Log.i(TAG, "***************VideoDir = " + VideoDir);
        Log.i(TAG, "***************VideoDir is Dir?" + VideoDir.isDirectory());
        if (true == VideoDir.isDirectory()) {
            File[] VideoDirChildren = VideoDir.listFiles();
            Log.i(TAG, "*************Before File Cnt = "
                    + VideoDirChildren.length);
            for (int i = 0; i < VideoDirChildren.length; i++) {
                Log.v("ls file ", i + VideoDirChildren[i].getName());
                if (true == VideoDirChildren[i].isFile()) {
                    Log.i(TAG, "delete result=" + VideoDirChildren[i].delete());
                }
            }
            File[] VideoDirChildrenAfter = VideoDir.listFiles();
            Log.i(TAG, "***********After File Cnt = "
                    + VideoDirChildrenAfter.length);
        } else {
            Log.i(TAG, "**********ERROR!!!!!VideoDir is not a directory");
        }
    }
}
