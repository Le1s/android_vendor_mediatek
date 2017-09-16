package com.hesine.nmsg.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.hesine.nmsg.common.EnumConstants;

import android.os.Environment;
import android.text.TextUtils;

public class FileEx {
    public static int getFileSize(String filepath) {
        try {
            if (TextUtils.isEmpty(filepath)) {
                return -1;
            }
            File file = new File(filepath);
            return (int) file.length();
        } catch (Exception e) {
            MLog.PrintStackTrace(e);
            return -1;
        }
    }

    public static void copyFile(android.content.Context context, InputStream inputStream,
            File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        FileOutputStream output = null;
        BufferedOutputStream outBuff = null;

        try {
            inBuff = new BufferedInputStream(inputStream);
            output = new FileOutputStream(targetFile);
            outBuff = new BufferedOutputStream(output);

            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }

            outBuff.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != inBuff) {
                inBuff.close();
            }
            if (null != outBuff) {
                outBuff.close();
            }
            if (null != output) {
                output.close();
            }
            if (null != inputStream) {
                inputStream.close();
            }
        }
    }

    public static void copyFileFromRawSource(android.content.Context context, int rawSourceId,
            String targetFile) throws IOException {
        copyFile(context, context.getResources().openRawResource(rawSourceId), new File(targetFile));
    }

    public static void copyFileFromRawSource(android.content.Context context, int rawSourceId,
            File targetFile) throws IOException {
        copyFile(context, context.getResources().openRawResource(rawSourceId), targetFile);
    }

    public static boolean DeleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    DeleteFile(f);
                }
            }
            return file.delete();
        }

        return true;
    }

    public static boolean ifFileExisted(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        File file = new File(path);
        return file.exists();
    }

    public static void write(String path, String data) {
        write(path, data.getBytes());
    }

    public static void write(String path, byte[] bytes) {
        try {
            File file;
            FileOutputStream out;
            // file = new File(path);
            // if (!file.exists()) {
            // file.mkdirs();
            // }
            file = null;
            String dir = path.substring(0, path.lastIndexOf("/"));
            file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
            String name = path.substring(path.lastIndexOf("/") + 1);
            file = new File(dir, name);
            file.createNewFile();
            out = new FileOutputStream(file);
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean DeleteFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    DeleteFile(f);
                }
            }
            return file.delete();
        }

        return true;
    }

    public static boolean RenameFile(String sourcePath, String destPath) {
        File sfile = new File(sourcePath);
        File dfile = new File(destPath);
        return sfile.renameTo(dfile);
    }

    public static String getSDCardPath() {
        File sdDir = null;
        String sdStatus = Environment.getExternalStorageState();

        if (TextUtils.isEmpty(sdStatus)) {
            return EnumConstants.STORAGE_PATH;
        }

        boolean sdCardExist = sdStatus.equals(android.os.Environment.MEDIA_MOUNTED);

        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
            return sdDir.toString();
        }

        return EnumConstants.STORAGE_PATH;
    }

    public static boolean getSDCardStatus() {
        boolean ret = false;
        String sdStatus = Environment.getExternalStorageState();

        if (sdStatus.equals(Environment.MEDIA_MOUNTED))
            ret = true;
        return ret;
    }

    public static byte[] readFile(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        int length = fis.available();
        byte[] buffer = new byte[length];
        fis.read(buffer);
        fis.close();
        return buffer;
    }

}
