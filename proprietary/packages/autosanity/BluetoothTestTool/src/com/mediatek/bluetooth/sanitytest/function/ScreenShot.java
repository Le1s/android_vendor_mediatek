package com.mediatek.bluetooth.sanitytest.function;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class ScreenShot {
    private static final String TAG = "ScreenShot";
    private static long mRecord = 1;
    private static final String mBasePath = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "SanityRecord"
            + File.separator;
    private static final int MAX_RECORD_NUM = 9;

    private static Bitmap takeScreenShot(Activity activity) {
        try {
            View view = activity.getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap bitmapCache = view.getDrawingCache();
            Rect frame = new Rect();
            activity.getWindow().getDecorView()
                    .getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;

            int width = activity.getWindowManager().getDefaultDisplay()
                    .getWidth();
            int height = activity.getWindowManager().getDefaultDisplay()
                    .getHeight();

            Bitmap bitmap = Bitmap.createBitmap(bitmapCache, 0,
                    statusBarHeight, width, height - statusBarHeight);
            if (bitmapCache != null) {
                bitmapCache.recycle();
                bitmapCache = null;
            }
            view.destroyDrawingCache();
            return bitmap;
        } catch (RuntimeException e) {
            Log.e(TAG,
                    "java.lang.RuntimeException: Canvas: trying to use a recycled bitmap");
            System.gc();
            return null;
        }
    }

    private static void savePic(Bitmap bitmap, File filePath) {
        if (bitmap == null)
            return;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            if (null != fos) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    private static String generateFilePath(String timeStame) {
        String path = mBasePath + timeStame + File.separator;
        String fileName = String.valueOf(++mRecord) + ".png";
        path += fileName;
        return path;
    }

    private static void deleteOldRecords() {
        File folder = new File(mBasePath);
        File[] files = folder.listFiles();
        if (files == null)
            return;
        List<Long> list = new ArrayList<Long>();
        for (File file : files) {
            if (file.isDirectory()) {
                list.add(Long.valueOf(file.getName()));
            }
        }
        Collections.sort(list, new Comparator<Long>() {
            public int compare(Long arg0, Long arg1) {
                return (int) (arg1 - arg0);
            }
        });
        int num = 0;
        for (Long name : list) {
            num++;
            if (num > MAX_RECORD_NUM) {
                String folerPath = mBasePath + String.valueOf(name);
                File file = new File(folerPath);
                deleteAll(file);
            } else {
                continue;
            }
        }
    }

    public static void deleteAll(File file) {
        if (file.isFile() || file.list().length == 0) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            for (File f : files) {
                deleteAll(f);
                f.delete();
            }
        }
    }

    public static void shoot(Activity a, String timeStame) {
        try {
            String path = generateFilePath(timeStame);
            if (path == null) {
                return;
            }
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            ScreenShot.savePic(ScreenShot.takeScreenShot(a), file);
            ScreenShot.deleteOldRecords();
        } catch (RuntimeException e) {
            // M: Make sure exception do not disturb next sanity.
        }
    }
}
