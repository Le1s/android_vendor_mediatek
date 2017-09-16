package com.mediatek.gallery3d.regressiontest;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.jayway.android.robotium.solo.Solo;

import com.android.gallery3d.R;
import com.android.gallery3d.app.ActivityState;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.AlbumSetDataLoader;
import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.GalleryActivity;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.app.PhotoDataAdapter;
import com.android.gallery3d.app.SlideshowPage;
import com.android.gallery3d.data.FilterDeleteSet;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.TileImageView;
import com.android.camera.ui.ShutterButton;
import com.android.camera.CameraActivity;
import com.mediatek.gallery3d.regressiontest.Utils;

public class Gallery2RegressionTestCase extends ActivityInstrumentationTestCase2<GalleryActivity> {

    private static final String TAG = "Gallery2/Gallery2PhotoPageTestCase";
    private static final String IDENTICAL_FILE_NAME_5M = "identical_5M_1.jpg";
    private static final int SHORT_TIME = 500;
    private static final int MID_TIME = 1000;
    private static final int LONG_TIME = 2000;
    private static final int DRAG_STEP = 5;

    private Solo mSolo = null;
    private GalleryActivity mGallery = null;
    private Instrumentation mInstrumentation = null;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mActionBarIconWidth = 0;
    private int mActionBarHeight = 0;
    private int mId = -1;
    private static String mBuckId = null;

    public Gallery2RegressionTestCase(Class<GalleryActivity> activityClass) {
        super(activityClass);
    }

    public Gallery2RegressionTestCase() {
        super(GalleryActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        initResourceRefs();
        mSolo.sleep(MID_TIME);
    }

    private void initResourceRefs() {
        mGallery = (GalleryActivity) getActivity();
        assertNotNull(mGallery);
        mInstrumentation = getInstrumentation();
        assertNotNull(mInstrumentation);
        mSolo = new Solo(mInstrumentation, mGallery);
        assertNotNull(mSolo);

        DisplayMetrics dm = new DisplayMetrics();
        mSolo.getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mActionBarIconWidth = (int) (59 * dm.xdpi / 160);
        mActionBarHeight = (int) ((float) (mGallery.getActionBar().getHeight() * dm.ydpi) / 160 + 10);
        Log.i(TAG, "<initResourceRefs> DisplayMetrics = " + dm.toString());
        Log.i(TAG, "<initResourceRefs> mScreenWidth = " + mScreenWidth);
        Log.i(TAG, "<initResourceRefs> mScreenHeight = " + mScreenHeight);
        Log.i(TAG, "<initResourceRefs> mActionBarIconWidth = " + mActionBarIconWidth);
        Log.i(TAG, "<initResourceRefs> mActionBarHeight = " + mActionBarHeight);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * 1 Launch Gallery
     * 2 Select a folder and enter
     * 3 Select an image to view
     * result: can view image normally
     */
    public void testCase01_DecodeDone() throws Exception {
        enterAlbumPage();
        enterPhotoPage();
        mSolo.sleep(LONG_TIME);
        // check image show
        ActivityState currentState = mGallery.getStateManager().getTopState();
        final Field f_model = PhotoPage.class.getDeclaredField("mModel");
        f_model.setAccessible(true);
        PhotoPage.Model photoDataAdapter = (PhotoPage.Model)f_model.get(currentState);
        assertTrue(photoDataAdapter.getLoadingState(0) == PhotoView.Model.LOADING_COMPLETE);
    }
    
    /**
     * 1 Launch Gallery
     * 2 Select a folder and enter
     * 3 Select an image to view
     * 4 menu-more-slideshow
     * result: can play slideshow normally
     */
    public void testCase02_SlideShow() {
         enterAlbumPage();
         enterPhotoPage();
         //slideshow
         mId = mSolo.getCurrentActivity().getResources().getIdentifier("slideshow","string","com.android.gallery3d");
         mSolo.clickOnMenuItem(mSolo.getString(mId));
         mSolo.sleep(LONG_TIME);
         assertTrue(mGallery.getStateManager().getTopState() instanceof SlideshowPage);
    }

    /**
     * 1 Launch Gallery
     * 2 Select a folder and enter
     * 3 Select an image to view
     * 4 Zoom out photo and enter film mode
     * result: can enter film mode
     */
    public void testCase03_ZoomOutAndZoomIn() {
        enterAlbumPage();
        enterPhotoPage();
        //zoom out
        PointF startPoint1 = new PointF(100, 500);
        PointF startPoint2 = new PointF(800, 200);
        PointF endPoint1 = new PointF(400, 350);
        PointF endPoint2 = new PointF(500, 300);
        mSolo.pinchToZoom(startPoint1, startPoint2, endPoint1, endPoint2);
        mSolo.sleep(LONG_TIME * 2);
        assertTrue(isFilmMode());
        
        //zoom in
        startPoint1.set(400, 350);
        startPoint2.set(500, 300);
        endPoint1.set(100, 500);
        endPoint2.set(800, 200);
        mSolo.pinchToZoom(startPoint1, startPoint2, endPoint1, endPoint2);
        mSolo.sleep(LONG_TIME);
        assertTrue(!isFilmMode());
    }
    
    /**
     * 1 Launch Gallery
     * 2 Select a folder and enter
     * 3 Select an image to view
     * 4 Zoom out photo and enter film mode
     * 5 Slide up and down to delete photo
     * result: can delete successfully
     */
    public void testCase04_SlideToDelete() throws Exception {
        enterAlbumPage();
        enterPhotoPage();
        PointF startPoint1 = new PointF(100, 500);
        PointF startPoint2 = new PointF(800, 200);
        PointF endPoint1 = new PointF(400, 350);
        PointF endPoint2 = new PointF(500, 300);
        mSolo.pinchToZoom(startPoint1, startPoint2, endPoint1, endPoint2);
        mSolo.sleep(LONG_TIME * 2);
        assertTrue(isFilmMode());
        
        //Slide up and down to delete photo
        ActivityState currentState = mGallery.getStateManager().getTopState();
        final Field f_mediaSet = PhotoPage.class.getDeclaredField("mMediaSet");
        f_mediaSet.setAccessible(true);
        FilterDeleteSet mediaSet = (FilterDeleteSet)f_mediaSet.get(currentState);
        int countOrigin = mediaSet.getTotalMediaItemCount();
        
        mSolo.drag(mScreenWidth / 2, mScreenWidth / 2, mScreenHeight / 2, mScreenHeight, DRAG_STEP);
        mSolo.sleep(MID_TIME);
        mSolo.clickOnScreen(mScreenWidth / 2, mScreenHeight / 2);
        mSolo.sleep(MID_TIME);
        assertTrue(mediaSet.getTotalMediaItemCount() < countOrigin);
        
        countOrigin = mediaSet.getTotalMediaItemCount();
        mSolo.pinchToZoom(startPoint1, startPoint2, endPoint1, endPoint2);
        mSolo.sleep(LONG_TIME * 2);
        assertTrue(isFilmMode());
        
        mSolo.drag(mScreenWidth / 2, mScreenWidth / 2, mScreenHeight / 2, 0, DRAG_STEP);
        mSolo.sleep(MID_TIME);
        mSolo.clickOnScreen(mScreenWidth / 2, mScreenHeight / 2);
        mSolo.sleep(MID_TIME);
        assertTrue(mediaSet.getTotalMediaItemCount() < countOrigin);

        Utils.copyFileToResume(mGallery);
        Utils.copyFileToResume(mGallery);
    }
    
    /**
     * 1 Launch Gallery
     * 2 Tap the camera icon to launch camera
     * result: can enter camera capture mode
     */
    public void testCase5_LauchCamera() {
        mSolo.clickOnView(mGallery.findViewById(R.id.action_camera));
        mSolo.sleep(MID_TIME * 5);
        assertTrue(mSolo.getCurrentActivity() instanceof CameraActivity);
        mSolo.sendKey(KeyEvent.KEYCODE_BACK);
        mSolo.sleep(LONG_TIME);
        mSolo.sendKey(KeyEvent.KEYCODE_BACK);
        mSolo.sleep(LONG_TIME);
    }
    
    /**
     * 1 Launch Gallery
     * 2 Tap the camera icon to launch camera
     * 3 capture some pictures rotation screen and click thumbnail go to gallery
     * result: capture successfully and review pictures normally
     */
    /*public void testCase6_CameraCapture throws Exception {
        //launch camera
        mSolo.clickOnView(mGallery.findViewById(R.id.action_camera));
        mSolo.sleep(MID_TIME * 5);
        assertTrue(mSolo.getCurrentActivity() instanceof Camera);
        
        // remove app guide
        SharedPreferences settings = mGallery.getSharedPreferences(
                "application_guide", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("camera_guide", true);
        editor.commit();
        
        // capture three photos
        mId = mGallery.getResources().getIdentifier(
                "com.android.gallery3d:id/shutter_button_photo", null, null);
        ShutterButton photoButton = (ShutterButton) mGallery.findViewById(mId);
        assertTrue(photoButton != null);
        assertTrue(photoButton.isClickable());
        assertTrue(photoButton.isEnabled());
        
        mSolo.setActivityOrientation(Solo.LANDSCAPE);
        mSolo.sleep(MID_TIME);
        mSolo.clickOnView(photoButton);
        
        mSolo.setActivityOrientation(Solo.PORTRAIT);
        mSolo.sleep(MID_TIME);
        mSolo.clickOnView(photoButton);
        
        mSolo.setActivityOrientation(Solo.LANDSCAPE);
        mSolo.sleep(MID_TIME);
        mSolo.clickOnView(photoButton);
        mSolo.sleep(MID_TIME);
        
        //click thumbnail to go to gallery
        mId = mGallery.getResources().getIdentifier(
                "com.android.gallery3d:id/thumbnail", null, null);
        ImageView playButton = (ImageView) mGallery.findViewById(mId);
        mSolo.clickOnView(playButton);
        mSolo.sleep(MID_TIME);
        assertTrue(mSolo.getCurrentActivity() instanceof GalleryActivity);
        ActivityState currentState = mGallery.getStateManager().getTopState();
        assertTrue(currentState instanceof PhotoPage);
        
        final Field f_model = PhotoPage.class.getDeclaredField("mModel");
        f_model.setAccessible(true);
        PhotoPage.Model model = (PhotoPage.Model)f_model.get(currentState);
        assertTrue(model.getMediaItem(0).getWidth() > model.getMediaItem(0).getHeight());
        
        // slide to next photo
        int oldIndex = getCurrentIndex();
        mSolo.drag(mScreenWidth / 3 * 2, mScreenWidth / 3, mScreenHeight / 2, mScreenHeight / 2,
                DRAG_STEP);
        mSolo.sleep(SHORT_TIME);
        assertTrue(getCurrentIndex() == (oldIndex + 1));
        assertTrue(model.getMediaItem(0).getWidth() < model.getMediaItem(0).getHeight());

        // slide to next photo
        mSolo.drag(mScreenWidth / 3 * 2, mScreenWidth / 3, mScreenHeight / 2, mScreenHeight / 2,
                DRAG_STEP);
        mSolo.sleep(SHORT_TIME);
        assertTrue(getCurrentIndex() == (oldIndex + 1));
        assertTrue(model.getMediaItem(0).getWidth() > model.getMediaItem(0).getHeight());
        
        //keycode_power
        mSolo.sendKey(KeyEvent.KEYCODE_POWER);
        mSolo.sleep(LONG_TIME);
        mSolo.sendKey(KeyEvent.KEYCODE_POWER);
        mSolo.sleep(LONG_TIME);
        assertTrue(mGallery.getStateManager().getTopState() instanceof PhotoPage);
    }*/
    
    /**
     * 1 Launch Gallery
     * 2 Select a folder and enter
     * 3 Select an image to view
     * 4 Delete photo
     * result: delete successfully
     */
    public void testCase7_DeleteInPhotoPage() throws Exception {
        enterAlbumPage();
        enterPhotoPage();
        ActivityState currentState = mGallery.getStateManager().getTopState();
        final Field f_mediaSet = PhotoPage.class.getDeclaredField("mMediaSet");
        f_mediaSet.setAccessible(true);
        FilterDeleteSet mediaSet = (FilterDeleteSet)f_mediaSet.get(currentState);
        int countOrigin = mediaSet.getTotalMediaItemCount();
        mId = mSolo.getCurrentActivity().getResources().getIdentifier("delete","string","com.android.gallery3d");
        mSolo.clickOnMenuItem(mSolo.getString(mId));
        mSolo.sleep(MID_TIME);
        mId = mSolo.getCurrentActivity().getResources().getIdentifier("ok","string","com.android.gallery3d");
        mSolo.clickOnButton(mSolo.getString(mId));
        mSolo.sleep(MID_TIME);
        assertTrue(mediaSet.getTotalMediaItemCount() < countOrigin);

        Utils.copyFileToResume(mGallery);
    }
    
    /**
     * 1 Launch Gallery
     * 2 Select a folder and enter
     * 3 Select an image to view
     * 4 Rotate left and right
     * result: the image can be rotated successfully
     */
    public void testCase8_Rotate() throws Exception {
        enterAlbumPage();
        enterPhotoPage();
        ActivityState currentState = mGallery.getStateManager().getTopState();
        final Field f_model = PhotoPage.class.getDeclaredField("mModel");
        f_model.setAccessible(true);
        PhotoPage.Model model = (PhotoPage.Model)f_model.get(currentState);
        
        // rotate left
        mId = mSolo.getCurrentActivity().getResources().getIdentifier("rotate_left","string","com.android.gallery3d");
        mSolo.clickOnMenuItem(mSolo.getString(mId));
        mSolo.sleep(MID_TIME);
        assertTrue(model.getMediaItem(0).getRotation() == 270);
        
        //rotate right
        mId = mSolo.getCurrentActivity().getResources().getIdentifier("rotate_right","string","com.android.gallery3d");
        mSolo.clickOnMenuItem(mSolo.getString(mId));
        mSolo.sleep(MID_TIME);
        assertTrue(model.getMediaItem(0).getRotation() == 0);
    }
    
    /**
     * 1 Launch Gallery
     * 2 Select a folder and enter
     * 3 Select an image to view
     * 4 Delete photo
     * 5 Power off and Power on
     * result: delete successfully and back to thumbnail mode
     */
    public void testCase9_SlectMoreItemDelete() throws Exception {
        enterAlbumPage();
        mId = mSolo.getCurrentActivity().getResources().getIdentifier("select_item","string","com.android.gallery3d");
        mSolo.clickOnMenuItem(mSolo.getString(mId));
        mSolo.sleep(SHORT_TIME);

        // select 2 item
        mSolo.clickOnScreen(mScreenWidth / 6, mScreenHeight / 2);
        mSolo.sleep(SHORT_TIME);
        mSolo.clickOnScreen(mScreenWidth / 2, mScreenHeight / 2);
        mSolo.sleep(SHORT_TIME);
        assertTrue(getCurrentSelectCount() == 2);

        ActivityState currentState = mGallery.getStateManager().getTopState();
        final Field f_mediaSet = AlbumPage.class.getDeclaredField("mMediaSet");
        f_mediaSet.setAccessible(true);
        MediaSet mediaSet = (MediaSet)f_mediaSet.get(currentState);
        int countOrigin = mediaSet.getTotalMediaItemCount();
        
        // click delete button
        mId = mSolo.getCurrentActivity().getResources().getIdentifier("action_delete","id","com.android.gallery3d");
        mSolo.clickOnView(mGallery.findViewById(mId));
        mSolo.sleep(LONG_TIME);
        mId = mSolo.getCurrentActivity().getResources().getIdentifier("ok","string","com.android.gallery3d");
        mSolo.clickOnButton(mSolo.getString(mId));
        mSolo.sleep(LONG_TIME);
        assertTrue(countOrigin > mediaSet.getTotalMediaItemCount());
        
        Utils.copyFileToResume(mGallery);
        Utils.copyFileToResume(mGallery);
    }
    
    /**
     * 1 Launch Gallery
     * 2 exit Gallery
     * result: exit Gallery
     */
    public void testCase10_ExitGallery() {
        enterAlbumPage();
        enterPhotoPage();
        mSolo.sendKey(KeyEvent.KEYCODE_BACK);
        mSolo.sleep(LONG_TIME);
        mSolo.sendKey(KeyEvent.KEYCODE_BACK);
        mSolo.sleep(LONG_TIME);
        mSolo.sendKey(KeyEvent.KEYCODE_BACK);
        mSolo.sleep(LONG_TIME);
    }

    private boolean isFilmMode() {
        ActivityState st = mGallery.getStateManager().getTopState();
        try {
            final Field photoViewField = PhotoPage.class.getDeclaredField("mPhotoView");
            photoViewField.setAccessible(true);
            PhotoView pv = (PhotoView) photoViewField.get(st);
            return pv.getFilmMode();
        } catch (NullPointerException e1) {
            Log.e(TAG, "exception when fetching: ", e1);
            return false;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "exception when fetching: ", e2);
            return false;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "exception when fetching: ", e3);
            return false;
        } catch (NoSuchFieldException e4) {
            Log.e(TAG, "exception when fetching: ", e4);
            return false;
        }
    }

    private int getCurrentIndex() {
        ActivityState st = mGallery.getStateManager().getTopState();
        try {
            final Field dataLoaderField = PhotoPage.class.getDeclaredField("mModel");
            dataLoaderField.setAccessible(true);
            PhotoPage.Model dl = (PhotoPage.Model) dataLoaderField.get(st);
            return dl.getCurrentIndex();
        } catch (NullPointerException e1) {
            Log.e(TAG, "exception when fetching: ", e1);
            return -1;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "exception when fetching: ", e2);
            return -1;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "exception when fetching: ", e3);
            return -1;
        } catch (NoSuchFieldException e4) {
            Log.e(TAG, "exception when fetching: ", e4);
            return -1;
        }
    }

    private int getCurrentSelectCount() {
        ActivityState st = mGallery.getStateManager().getTopState();
        try {
            final Field selectManagerField = AlbumPage.class.getDeclaredField("mSelectionManager");
            selectManagerField.setAccessible(true);
            SelectionManager sm = (SelectionManager) selectManagerField.get(st);
            return sm.getSelectedCount();

        } catch (NullPointerException e1) {
            Log.e(TAG, "exception when fetching: ", e1);
            return -1;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "exception when fetching: ", e2);
            return -1;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "exception when fetching: ", e3);
            return -1;
        } catch (NoSuchFieldException e4) {
            Log.e(TAG, "exception when fetching: ", e4);
            return -1;
        }
    }


    private void enterAlbumPage(){
        // enter album page
        Utils.openAlbumWhenAlbumSetPage("identical_5M", 
                (AlbumSetPage) (mGallery.getStateManager().getTopState()),
                mGallery, mInstrumentation, mSolo);
    }

    private void enterPhotoPage(){
        // enter photo page
        Utils.openPhotoWhenAlbumPage("identical_5M_1", 
                (AlbumPage) (mGallery.getStateManager().getTopState()),
                mGallery, mInstrumentation, mSolo);
    }
}
