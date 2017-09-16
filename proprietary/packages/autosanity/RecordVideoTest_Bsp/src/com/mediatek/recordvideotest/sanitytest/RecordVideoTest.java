package com.mediatek.recordvideotest.sanitytest;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import com.android.camera2.R;
import com.android.camera.CameraActivity;
import com.android.camera.CameraModule;
import com.android.camera.VideoModule;
import com.android.camera.Storage;
import com.jayway.android.robotium.solo.Solo;

public class RecordVideoTest extends ActivityInstrumentationTestCase2<CameraActivity> {

    private Activity mActivity = null;
    private Instrumentation mInstrumentation = null;
    private Solo solo;
    private static final String TAG = "RecordVideoTest";
    private int WAIT_FOR_SYNC = 5000;
    private int RETRY_TIME = 5;
    private String Video_FILE_PATH;
    private String filter = ".3gp";

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
        Video_FILE_PATH = Storage.DIRECTORY;
        Log.v(TAG, "setUp end");
    }

    public void tearDown() throws Exception {
        getActivity().finish();
        super.tearDown();
        SystemClock.sleep(2000);
    }
    
    private void removeGuide() {
        Log.i(TAG, "removeGuide");
        int i = 0;
        boolean searched = solo.searchText("NEXT");
        Log.i("huqing", "searched = " + searched);
        while (!searched && i < 2) {
            searched = solo.searchText("NEXT");
            Log.i("huqing", "searched = " + searched);
            SystemClock.sleep(2000);
            i++;
        }
        if (searched) {
            solo.clickOnText("NEXT");
            SystemClock.sleep(2000);
        }
    }
    
    public void testRecordvideo() throws Throwable {
        Log.v(TAG, "testRecordvideo start");
        SystemClock.sleep(2000);
        int CntBefore = GetFileCntInDir(Video_FILE_PATH, filter);
        removeGuide();
        SystemClock.sleep(WAIT_FOR_SYNC);
        Log.v(TAG, "testRecordvideo click on screen");
        solo.clickOnScreen(475, 485);
        Log.v(TAG, "testRecordvideo click on screen end");
        SystemClock.sleep(3000);
        solo.clickOnScreen(475, 485); 
        SystemClock.sleep(2000);
        chooseMode(1);
        SystemClock.sleep(3000);
        shutterClick();
        SystemClock.sleep(10000);
        shutterClick();
        SystemClock.sleep(3000);
        int CntAfter = GetFileCntInDir(Video_FILE_PATH, filter);
        assertEquals((CntBefore + 1), CntAfter);
        SystemClock.sleep(2000);
        Log.v(TAG, "testRecordvideo end");
    }
    
    private void chooseMode(final int mode) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "chooseMode" + mode);
                        Method onModeSelectedMethod = CameraActivity.class
                                .getDeclaredMethod("onModeSelected", int.class);
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

    private void shutterClick() {
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "capture");
                        Field field = CameraActivity.class
                                .getDeclaredField("mCurrentModule");
                        field.setAccessible(true);
                        CameraModule cameraModule = (VideoModule) field
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
