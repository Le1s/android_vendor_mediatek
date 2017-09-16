/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2014. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 */

package com.mediatek.videoplayback.sanitytest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.DisplayMetrics;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumDataLoader;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.AlbumSetDataLoader;
import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import android.util.Log;
import com.jayway.android.robotium.solo.Solo;

/**
 * This class provide some useful reflecting methods for test case
 */
public class Utils {
    private static final String TAG = "VideoPlaybackSanityTest/Utils";
    private static final int SHORT_TIME = 500;
    private static final int LONG_TIME = 2000;
    private static final int DRAG_STEP = 5;
    private static final String ACTIVITY_ACTION_NAME = "android.intent.action.VIEW";
    private static final String PACKAGE_NAME = "com.android.gallery3d";
    private static final String ACTIVITY_NAME = "com.android.gallery3d.app.MovieActivity";
    private static final String VIDEO_TYPE = "video/*";
    
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
    public static Field getPrivateField(Class clazz, String filedName) throws NoSuchFieldException {
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
    
    public static void openAlbumWhenAlbumSetPage(String albumName, AlbumSetPage albumSetPage,
            AbstractGalleryActivity gallery, Instrumentation instrumentation, Solo solo) {
        junit.framework.Assert.assertTrue(albumSetPage != null && albumName != null
                && gallery != null && instrumentation != null && solo != null);
        AlbumSetDataLoader adapter = getMemberInObject(albumSetPage, AlbumSetDataLoader.class,
                "mAlbumSetDataAdapter");
        int slotIndex = -1;
        DisplayMetrics dm = new DisplayMetrics();
        solo.getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int try_times = 100;
        int has_try = 0;
        while (slotIndex == -1 && has_try < try_times) {
            int activeStart = getMemberInObject(adapter, int.class, "mActiveStart");
            int activeEnd = getMemberInObject(adapter, int.class, "mActiveEnd");
            junit.framework.Assert.assertTrue("invalid active start/end: start=" + activeStart
                    + ", end=" + activeEnd, (activeStart >= 0 && activeStart < activeEnd));
            
            for (int j = activeStart; j < activeEnd; ++j) {
                MediaSet set = adapter.getMediaSet(j);
                if (set == null) {
                    Log.e(TAG, "slot #" + j + " has null media set!!");
                    continue;
                }
                if (set != null && albumName.equalsIgnoreCase(set.getName())) {
                    Log.i(TAG, "folder found at slot #" + j);
                    slotIndex = j;
                    break;
                }
            }
            if (slotIndex != -1) {
                break;
            }
            solo.drag(screenWidth / 3 * 2, screenWidth / 3, screenHeight / 2, screenHeight / 2,
                    DRAG_STEP);
            solo.sleep(1000);
            has_try++;
        }
        
        junit.framework.Assert.assertTrue(albumName + " folder is not found!!!", slotIndex >= 0);
        
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
        
        junit.framework.Assert.assertTrue("cannot open folder " + albumName, (gallery
                .getStateManager().getTopState() instanceof AlbumPage || gallery.getStateManager()
                .getTopState() instanceof PhotoPage));
    }
    
    public static void openPhotoWhenAlbumPage(String photoName, AlbumPage albumPage,
            AbstractGalleryActivity gallery, Instrumentation instrumentation, Solo solo) {
        junit.framework.Assert.assertTrue(photoName != null && albumPage != null && gallery != null
                && instrumentation != null && solo != null);
        AlbumDataLoader adapter = getMemberInObject(albumPage, AlbumDataLoader.class,
                "mAlbumDataAdapter");
        int slotIndex = -1;
        DisplayMetrics dm = new DisplayMetrics();
        solo.getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int try_times = 100;
        int has_try = 0;
        
        while (slotIndex == -1 && has_try < try_times) {
            int activeStart = getMemberInObject(adapter, int.class, "mActiveStart");
            int activeEnd = getMemberInObject(adapter, int.class, "mActiveEnd");
            junit.framework.Assert.assertTrue("invalid active start/end: start=" + activeStart
                    + ", end=" + activeEnd, (activeStart >= 0 && activeStart < activeEnd));
            for (int j = activeStart; j < activeEnd; ++j) {
                MediaItem item = adapter.get(j);
                if (item == null) {
                    Log.e(TAG, "slot #" + j + " has null media item!!");
                    continue;
                }
                Log.d(TAG, "item: " + item.getName());
                if (item != null && photoName.equalsIgnoreCase(item.getName())) {
                    Log.i(TAG, "file found at slot #" + j);
                    slotIndex = j;
                    break;
                }
            }
            if (slotIndex != -1) {
                break;
            }
            solo.drag(screenWidth / 3 * 2, screenWidth / 3, screenHeight / 2, screenHeight / 2,
                    DRAG_STEP);
            solo.sleep(1000);
            has_try++;
        }
        
        junit.framework.Assert.assertTrue(photoName + "file is not found!!!", slotIndex >= 0);
        
        // tap found slot to enter PhotoPage
        final int itemIdx = slotIndex;
        
        final AlbumPage page = albumPage;
        try {
            
            final Method singleTapUpInAlbumPage = AlbumPage.class.getDeclaredMethod(
                    "onSingleTapUp", int.class);
            singleTapUpInAlbumPage.setAccessible(true);
            
            instrumentation.runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    try {
                        singleTapUpInAlbumPage.invoke(page, itemIdx);
                    } catch (Exception e) {
                        Log.d(TAG, "error occured when invoke onSingleTapUp of AlbumPage");
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception exception) {
            junit.framework.Assert.assertFalse("exception when get singleTapUp method", true);
        }
    }
    
    public static <O extends Object, M extends Object> M getMemberInObject(O object,
            Class<M> memberType, String memberName) {
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz
                .getSuperclass()) {
            try {
                final Field memberField = clazz.getDeclaredField(memberName);
                memberField.setAccessible(true);
                return (M) memberField.get(object);
            } catch (NullPointerException e1) {
                Log.e(TAG, "exception when fetching: ", e1);
            } catch (IllegalArgumentException e2) {
                Log.e(TAG, "exception when fetching: ", e2);
            } catch (IllegalAccessException e3) {
                Log.e(TAG, "exception when fetching: ", e3);
            } catch (NoSuchFieldException e4) {
                Log.e(TAG, "exception when fetching: ", e4);
            }
        }
        return null;
    }
    
    /**
     * Set video player to be the default player
     * 
     * @param packageManager
     *            The name of PackageManager
     * @throws Exception
     */
    public static void setDefaultPlayer(PackageManager packageManager) throws Exception {
        // Retrieve all activities that can be performed for the given intent.
        Intent intent = new Intent(ACTIVITY_ACTION_NAME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(VIDEO_TYPE);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent,
                PackageManager.GET_INTENT_FILTERS);
        int size = resolveInfos.size();
        Log.v(TAG, "<setDefaultPlayer> size = " + size);
        ComponentName[] componentNames = new ComponentName[size];
        for (int i = 0; i < size; i++) {
            // Retrieve the package name and class name of the activities with
            // the same given intent.
            ResolveInfo info = resolveInfos.get(i);
            ActivityInfo activityInfo = info.activityInfo;
            String packageName = activityInfo.packageName;
            String className = activityInfo.name;
            Log.v(TAG, "<setDefaultPlayer> packageName = " + packageName + " className is "
                    + className);
            // Remove all preferred activity mappings, previously added with
            // {@link #addPreferredActivity}
            packageManager.clearPackagePreferredActivities(packageName);
            // Save the component names
            ComponentName name = new ComponentName(packageName, className);
            componentNames[i] = name;
        }
        // Set video player to be the default player
        ComponentName name = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
        IntentFilter intentFilter = new IntentFilter(ACTIVITY_ACTION_NAME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addDataType(VIDEO_TYPE);
        packageManager.addPreferredActivity(intentFilter, IntentFilter.MATCH_CATEGORY_TYPE,
                componentNames, name);
    }
    
}
