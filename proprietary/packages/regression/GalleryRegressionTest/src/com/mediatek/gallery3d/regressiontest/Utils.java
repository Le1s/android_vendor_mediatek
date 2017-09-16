/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2012. All rights reserved.
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
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.gallery3d.regressiontest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.hardware.input.InputManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.ActivityState;
import com.android.gallery3d.app.AlbumDataLoader;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.AlbumSetDataLoader;
import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.GalleryActivity;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.jayway.android.robotium.solo.Solo;

/**
 * This class provide some useful reflecting methods for test case
 */
public class Utils {
    private static final String TAG = "Gallery2/Utils";
    private static final int SHORT_TIME = 500;
    private static final int MID_TIME = 1000;
    private static final int LONG_TIME = 2000;
    private static final int DRAG_STEP = 5;
    private static final String ACTIVITY_ACTION_NAME = "android.intent.action.VIEW";
    private static final String PACKAGE_NAME = "com.android.gallery3d";
    private static final String ACTIVITY_NAME = "com.android.gallery3d.app.MovieActivity";
    private static final String VIDEO_TYPE = "video/*";
    
    private Utils() {
    }

    public static void openAlbumWhenAlbumSetPage(String albumName, AlbumSetPage albumSetPage,
            AbstractGalleryActivity gallery, Instrumentation instrumentation, Solo solo) {
        junit.framework.Assert.assertTrue(albumSetPage != null && albumName != null
                && gallery != null && instrumentation != null && solo != null);
        AlbumSetDataLoader adapter = getMemberInObject(albumSetPage, AlbumSetDataLoader.class,
                "mAlbumSetDataAdapter");
        if (adapter == null) {
            return;
        }
        int slotIndex = -1;
        DisplayMetrics dm = new DisplayMetrics();
        solo.getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int try_times = 100;
        int has_try = 0;
        while (slotIndex == -1 && has_try < try_times) {
            int activeStart = adapter.getActiveStart();
            int activeEnd = adapter.getActiveEnd();
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
            solo.drag(screenWidth / 2, screenWidth / 2, screenHeight / 3 * 2, screenHeight / 3,
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

        junit.framework.Assert.assertTrue("cannot open folder " + albumName, gallery.getStateManager()
                .getTopState() instanceof AlbumPage);
    }

    public static void openPhotoWhenAlbumPage(String photoName, AlbumPage albumPage,
            AbstractGalleryActivity gallery, Instrumentation instrumentation, Solo solo) {
        junit.framework.Assert.assertTrue(photoName != null && albumPage != null && gallery != null
                && instrumentation != null && solo != null);
        AlbumDataLoader adapter = getMemberInObject(albumPage, AlbumDataLoader.class,
                "mAlbumDataAdapter");
        if (adapter == null) {
            return;
        }
        int slotIndex = -1;
        DisplayMetrics dm = new DisplayMetrics();
        solo.getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int try_times = 100;
        int has_try = 0;
        
        while (slotIndex == -1 && has_try < try_times) {
            int activeStart = adapter.getActiveStart();
            int activeEnd = adapter.getActiveEnd();
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
            solo.drag(screenWidth / 2, screenWidth / 2, screenHeight / 3 * 2, screenHeight / 3,
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

        try {
            Thread.sleep(LONG_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        junit.framework.Assert.assertTrue("cannot open file " + photoName, gallery.getStateManager()
                .getTopState() instanceof PhotoPage);
    }

    public static void copyFileToResume (Context context) {
        String srcPath = "";
        String destPath = "";
        File root = new File("/storage/emulated/0/identical_8M");
        if (root.exists()){
            Log.d(TAG, "<copyFileToResume> root " + root.getAbsolutePath());
            File[] fs = root.listFiles();
            if (fs == null) {
                return;
            }
            if (fs.length >= 2) {
                srcPath = fs[1].getAbsolutePath();
            }
        }
        File file = new File("/storage/emulated/0/identical_5M/identical_5M_1.jpg");
        String fileNamePrefix = "/storage/emulated/0/identical_5M/identical_5M_";
        if (!file.exists()) {
            Log.d(TAG, "<copyFileToResume> !file.exists()");
            //copyToResume one file
            destPath = "/storage/emulated/0/identical_5M/identical_5M_1.jpg";
        } else {
            Log.d(TAG, "<copyFileToResume> file.exists()");
            File toFile = null;
            //copyToResume this file
            do {
                destPath = "";
                toFile = null;
                int random = (int)(Math.random() * 100) +1;
                destPath = fileNamePrefix + random + ".jpg";
                Log.d(TAG, "<copyFileToResume> destPath " + destPath);
                toFile = new File(destPath);
            } while (toFile.exists());
        }
        Log.d(TAG, "<copyFileToResume> srcPath " + srcPath);
        Log.d(TAG, "<copyFileToResume> destPath " + destPath);
        try {
            FileInputStream is = new FileInputStream(srcPath);
            FileOutputStream os = new FileOutputStream(destPath);
            byte[] buffer = new byte[4096];
            while (is.available() > 0) {
                  int n = is.read(buffer);
                  os.write(buffer, 0, n);
             }
            is.close();
            os.close();
        } catch (IOException ex) {
            Log.e(TAG, "<copyFileToResume> " + ex);
        }
       String[] paths = {"/storage/emulated/0/identical_5M/"};
       MediaScannerConnection.scanFile(context, paths, null, null);
    }

    public static <O extends Object, M extends Object> M getMemberInObject(O object,
            Class<M> memberType, String memberName) {
        try {
            final Field memberField = object.getClass().getDeclaredField(memberName);
            memberField.setAccessible(true);
            return (M) memberField.get(object);
        } catch (NullPointerException e1) {
            Log.e(TAG, "exception when fetching: ", e1);
            return null;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "exception when fetching: ", e2);
            return null;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "exception when fetching: ", e3);
            return null;
        } catch (NoSuchFieldException e4) {
            Log.e(TAG, "exception when fetching: ", e4);
            return null;
        }
    }
}
