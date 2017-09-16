package com.mediatek.gallery3d.sanitytest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Instrumentation;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumDataLoader;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.AlbumSetDataLoader;
import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.jayway.android.robotium.solo.Solo;

/**
 * This class provide some useful reflecting methods for test case
 */
public class Utils {
    private static final String TAG = "GalleryTest/Utils";
    private static final int SHORT_TIME = 500;
    private static final int MID_TIME = 1000;
    private static final int LONG_TIME = 2000;
    private static final int DRAG_STEP = 5;

    private Utils() {
    }

    /**
     * Get a private/protected field from a declared class
     * 
     * @param clazz
     *            The class where you need to get private/protected field
     * @param filedName
     *            The name of this field
     * @return The field required
     * @throws NoSuchFieldException
     */
    public static Field getPrivateField(Class clazz, String filedName)
            throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(filedName);
        field.setAccessible(true);
        return field;
    }

    /**
     * Get a private/protected method from a declared class
     * 
     * @param clazz
     *            The class where you need to get private/protected method
     * @param methodName
     *            The name of this method
     * @return The field method
     * @throws NoSuchMethodException
     */
    public static Method getPrivateMethod(Class clazz, String methodName,
            Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    /**
     * Get a private/protected constructor from a declared class
     * 
     * @param clazz
     *            The class where you need to get private/protected constructor
     * @param parameterTypes
     * @return The constructor
     * @throws NoSuchMethodException
     */
    public static Constructor getPrivateConstructor(Class clazz,
            Class<?>... parameterTypes) throws NoSuchMethodException {
        Constructor constructor = clazz.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor;
    }

    public static void openAlbumWhenAlbumSetPage(String albumName,
            AlbumSetPage albumSetPage, AbstractGalleryActivity gallery,
            Instrumentation instrumentation, Solo solo) {
        junit.framework.Assert.assertTrue(albumSetPage != null
                && albumName != null && gallery != null
                && instrumentation != null && solo != null);
        AlbumSetDataLoader adapter = getMemberInObject(albumSetPage,
                AlbumSetDataLoader.class, "mAlbumSetDataAdapter");
        int slotIndex = -1;
        DisplayMetrics dm = new DisplayMetrics();
        solo.getCurrentActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int try_times = 100;
        int has_try = 0;
        while (slotIndex == -1 && has_try < try_times) {
            int activeStart = getMemberInObject(adapter, int.class,
                    "mActiveStart");
            int activeEnd = getMemberInObject(adapter, int.class, "mActiveEnd");
            Log.i(TAG, "<openAlbumWhenAlbumSetPage> activeStart = "
                    + activeStart + ", activeEnd = " + activeEnd);
            junit.framework.Assert.assertTrue("<openAlbumWhenAlbumSetPage> "
                    + "invalid active start/end: start=" + activeStart
                    + ", end=" + activeEnd,
                    (activeStart >= 0 && activeStart < activeEnd));

            for (int j = activeStart; j < activeEnd; ++j) {
                MediaSet set = adapter.getMediaSet(j);
                if (set == null) {
                    Log.i(TAG, "<openAlbumWhenAlbumSetPage> slot #" + j
                            + " has null media set!!");
                    continue;
                }
                if (set != null && albumName.equalsIgnoreCase(set.getName())) {
                    Log.i(TAG,
                            "<openAlbumWhenAlbumSetPage> folder found at slot #"
                                    + j);
                    slotIndex = j;
                    break;
                }
            }
            if (slotIndex != -1) {
                break;
            }
            solo.drag(screenWidth / 3 * 2, screenWidth / 3, screenHeight / 2,
                    screenHeight / 2, DRAG_STEP);
            solo.sleep(1000);
            has_try++;
        }

        junit.framework.Assert.assertTrue("<openAlbumWhenAlbumSetPage> "
                + albumName + " folder is not found!!!", slotIndex >= 0);

        // tap found slot to enter AlbumPage
        final int albumIdx = slotIndex;
        final AlbumSetPage page = albumSetPage;
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                page.onSingleTapUp(albumIdx);
            }
        });

        try {
            Thread.sleep(LONG_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "<openAlbumWhenAlbumSetPage> getTopState() = "
                + gallery.getStateManager().getTopState());
        junit.framework.Assert
                .assertTrue(
                        "<openAlbumWhenAlbumSetPage> cannot open folder "
                                + albumName,
                        (gallery.getStateManager().getTopState() instanceof AlbumPage || gallery
                                .getStateManager().getTopState() instanceof PhotoPage));
    }

    public static void openPhotoWhenAlbumPage(String photoName,
            AlbumPage albumPage, AbstractGalleryActivity gallery,
            Instrumentation instrumentation, Solo solo) {
        junit.framework.Assert.assertTrue(photoName != null
                && albumPage != null && gallery != null
                && instrumentation != null && solo != null);
        AlbumDataLoader adapter = getMemberInObject(albumPage,
                AlbumDataLoader.class, "mAlbumDataAdapter");
        int slotIndex = -1;
        DisplayMetrics dm = new DisplayMetrics();
        solo.getCurrentActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int try_times = 100;
        int has_try = 0;

        while (slotIndex == -1 && has_try < try_times) {
            int activeStart = getMemberInObject(adapter, int.class,
                    "mActiveStart");
            int activeEnd = getMemberInObject(adapter, int.class, "mActiveEnd");
            Log.i(TAG, "<openPhotoWhenAlbumPage> activeStart = " + activeStart
                    + ", activeEnd = " + activeEnd);
            junit.framework.Assert.assertTrue(
                    "invalid active start/end: start=" + activeStart + ", end="
                            + activeEnd,
                    (activeStart >= 0 && activeStart < activeEnd));
            for (int j = activeStart; j < activeEnd; ++j) {
                MediaItem item = adapter.get(j);
                if (item == null) {
                    Log.i(TAG, "<openPhotoWhenAlbumPage> slot #" + j
                            + " has null media item!!");
                    continue;
                }
                Log.i(TAG, "<openPhotoWhenAlbumPage> item: " + item.getName());
                if (item != null && photoName.equalsIgnoreCase(item.getName())) {
                    Log.i(TAG, "<openPhotoWhenAlbumPage> file found at slot #"
                            + j);
                    slotIndex = j;
                    break;
                }
            }
            if (slotIndex != -1) {
                break;
            }
            solo.drag(screenWidth / 3 * 2, screenWidth / 3, screenHeight / 2,
                    screenHeight / 2, DRAG_STEP);
            solo.sleep(1000);
            has_try++;
        }

        junit.framework.Assert.assertTrue("<openPhotoWhenAlbumPage> "
                + photoName + "file is not found!!!", slotIndex >= 0);

        // tap found slot to enter PhotoPage
        final int itemIdx = slotIndex;

        final AlbumPage page = albumPage;
        try {
            final Method singleTapUpInAlbumPage = AlbumPage.class
                    .getDeclaredMethod("onSingleTapUp", int.class);
            singleTapUpInAlbumPage.setAccessible(true);
            instrumentation.runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    try {
                        singleTapUpInAlbumPage.invoke(page, itemIdx);
                    } catch (Exception e) {
                        Log.i(TAG,"<openPhotoWhenAlbumPage> "
                                + "error occured when invoke onSingleTapUp of AlbumPage");
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception exception) {
            junit.framework.Assert.assertFalse("<openPhotoWhenAlbumPage> "
                    + "exception when get singleTapUp method", true);
        }

        try {
            Thread.sleep(LONG_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        junit.framework.Assert.assertTrue(
                "<openPhotoWhenAlbumPage> cannot open file " + photoName,
                gallery.getStateManager().getTopState() instanceof PhotoPage);
    }

    public static <O extends Object, M extends Object> M getMemberInObject(
            O object, Class<M> memberType, String memberName) {
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz
                .getSuperclass()) {
            try {
                final Field memberField = clazz.getDeclaredField(memberName);
                memberField.setAccessible(true);
                return (M) memberField.get(object);
            } catch (NullPointerException e1) {
                Log.e(TAG, "<getMemberInObject> NullPointerException when fetching");
            } catch (IllegalArgumentException e2) {
                Log.e(TAG, "<getMemberInObject> IllegalArgumentException when fetching");
            } catch (IllegalAccessException e3) {
                Log.e(TAG, "<getMemberInObject> IllegalAccessException when fetching");
            } catch (NoSuchFieldException e4) {
                Log.e(TAG, "<getMemberInObject> NoSuchFieldException when fetching");
            }
        }
        return null;
    }
}
