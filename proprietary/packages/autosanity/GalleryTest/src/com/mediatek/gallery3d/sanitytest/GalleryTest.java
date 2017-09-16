package com.mediatek.gallery3d.sanitytest;

import java.io.File;

import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;

import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.GalleryActivity;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.ui.PhotoView;

import com.jayway.android.robotium.solo.Solo;

import com.mediatek.gallery3d.sanitytest.Utils;

import com.mediatek.storage.StorageManagerEx;

public class GalleryTest extends
        ActivityInstrumentationTestCase2<GalleryActivity> {

    private static final String TAG = "GalleryTest";
    private static final String TEST_IMAGE = "testimageJD";
    private static String TEST_ALBUM = "sdcard0";
    private static final int SLEEP_TIME = 2000;
    private GalleryActivity mGallery;
    private Solo mSolo;
    private Instrumentation mInstrumentation;

    public GalleryTest() {
        super(GalleryActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mGallery = (GalleryActivity) getActivity();
        mInstrumentation = getInstrumentation();
        mSolo = new Solo(getInstrumentation(), getActivity());
    }

    public void tearDown() throws Exception {
        getActivity().finish();
        super.tearDown();
    }

    public void test_gallery() throws Exception {
        mSolo.sleep(SLEEP_TIME);
        Log.i(TAG, "[test_gallery] check environment");
        checkEnv();

        Log.i(TAG, "[test_gallery] test start");
        // into TEST_ALBUM
        Utils.openAlbumWhenAlbumSetPage(TEST_ALBUM, (AlbumSetPage) (mGallery
                .getStateManager().getTopState()), mGallery, mInstrumentation,
                mSolo);
        mSolo.sleep(SLEEP_TIME);

        // if AlbumPage, into PhotoPage
        if (mGallery.getStateManager().getTopState() instanceof AlbumPage) {
            Utils.openPhotoWhenAlbumPage(TEST_IMAGE, (AlbumPage) (mGallery
                    .getStateManager().getTopState()), mGallery,
                    mInstrumentation, mSolo);
        }
        mSolo.sleep(SLEEP_TIME);

        // check image show
        PhotoPage.Model photoDataAdapter = Utils.getMemberInObject(
                (PhotoPage) mGallery.getStateManager().getTopState(),
                PhotoPage.Model.class, "mModel");
        Log.i(TAG, "[test_gallery] PhotoDataAdapter = " + photoDataAdapter);
        assertTrue(
                "[test_gallery] Load image fail, PhotoDataAdapter.getLoadingState(0) = "
                        + photoDataAdapter.getLoadingState(0), photoDataAdapter
                        .getLoadingState(0) == PhotoView.Model.LOADING_COMPLETE);
        Log.i(TAG, "[test_gallery] test end");
    }

    private void checkEnv() {
        // get default storage path
        String defaultStoragePath = null;
        try {
            defaultStoragePath = StorageManagerEx.getDefaultPath();
            Log.i(TAG, "[checkEnv] defaultStoragePath = " + defaultStoragePath);
        } catch (RuntimeException e) {
            Log.i(TAG, "[checkEnv] RuntimeException when StorageManagerEx.getDefaultPath()", e);
            File file = Environment.getExternalStorageDirectory();
            if (file != null) {
                defaultStoragePath = file.getAbsolutePath();
                Log.i(TAG, "[checkEnv] from Environment, defaultStoragePath = " + defaultStoragePath);
            } else {
                Log.i(TAG, "[checkEnv] Environment.getExternalStorageDirectory() == null");
            }
        }
        assertTrue("[checkEnv] invalide defaultStoragePath = " + defaultStoragePath,
                defaultStoragePath != null && !defaultStoragePath.equals(""));

        // check storage state
        StorageManager sm = (StorageManager) mGallery
                .getSystemService(Context.STORAGE_SERVICE);
        assertTrue("[checkEnv] StorageManager is null", sm != null);
        String volumeState = sm.getVolumeState(defaultStoragePath);
        assertTrue("[checkEnv] volumeState is " + volumeState,
                Environment.MEDIA_MOUNTED.equalsIgnoreCase(volumeState));
        Log.i(TAG, "[checkEnv] check storage state, pass");

        // check if test image is exist
        String testFilePath = defaultStoragePath + "/" + TEST_IMAGE + ".jpg";
        Log.i(TAG, "[checkEnv] testFilePath = " + testFilePath);
        File file = new File(testFilePath);
        assertTrue("[checkEnv] test file is not exist", file
                .exists());
        Log.i(TAG, "[checkEnv] check test file exist, pass");

        // check if test image is in media database
        ContentResolver resolver = mGallery.getContentResolver();
        Uri uri = Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { ImageColumns.TITLE, ImageColumns.DATA };
        String whereClause = ImageColumns.DATA + " = ?";
        String[] whereClauseArgs = new String[] { testFilePath };
        Cursor cursor = resolver.query(uri, projection, whereClause,
                whereClauseArgs, null);
        assertTrue("[checkEnv] cursor == null, fail to query media datebase",
                cursor != null);
        assertTrue("[checkEnv] cursor getCount() = " + cursor.getCount()
                + ", not found image [" + file.getAbsolutePath()
                + "] in media database ", cursor.getCount() == 1);
        cursor.close();
        cursor = null;
        Log.i(TAG, "[checkEnv] check test image in media database, pass");

        // check if gallery activity has window focus
        boolean hasFocus = mGallery.hasWindowFocus();
        assertTrue("[checkEnv] gallery activity does not have window focus",
                hasFocus);
        Log.i(TAG, "[checkEnv] check gallery activity has window focus, pass");

        // init TEST_ALBUM
        if (defaultStoragePath != null) {
            TEST_ALBUM = defaultStoragePath.substring(defaultStoragePath
                    .lastIndexOf("/") + 1, defaultStoragePath.length());
        }

        Log.i(TAG, "[checkEnv] Environment is OK, TEST_ALBUM = " + TEST_ALBUM);
    }
}
