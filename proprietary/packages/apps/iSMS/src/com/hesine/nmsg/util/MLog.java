package com.hesine.nmsg.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.hesine.nmsg.Application;
import com.hesine.nmsg.common.EnumConstants;

public final class MLog {
    public static final int LOG_TRACE = 0;
    public static final int LOG_WARNING = 1;
    public static final int LOG_ERROR = 2;
    public static final int LOG_KEYPATH = 3;
    public static final int LOG_NO_LOG = 4;
    private static int LOG_LEVEL = LOG_ERROR;
    public static File file = null;
    public static RandomAccessFile fWriter = null;
    public static final String logpath = File.separator + EnumConstants.ROOT_DIR + "/Log/";
    public static Context mContext = null;

    private static int MAX_LOG_SIZE = 10 * 1024 * 1024;
    private static final String TAG = "Mlog";

    public synchronized static void init(Context c) {
        mContext = c;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                String newLog = FileEx.getSDCardPath() + logpath + "nmsg.log";
                String bakLog = FileEx.getSDCardPath() + logpath + "nmsg-bak.log";
                file = new File(newLog);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                boolean isFileExist = file.exists();
                if (isFileExist && file.length() >= MAX_LOG_SIZE) {
                    File file_bak = new File(bakLog);
                    if (file_bak.exists())
                        file_bak.delete();
                    file.renameTo(file_bak);
                    file = null;
                    file = new File(newLog);
                    isFileExist = false;
                }

                if (fWriter != null) {
                    fWriter.close();
                }

                fWriter = new RandomAccessFile(file, "rws");
                if (isFileExist) {
                    fWriter.seek(file.length());
                }

                error("NmsLog", "java file logger is inited");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void destroy() {
        file = null;
    }

    public static String GetStactTrace(Exception e) {
        if (null == e)
            return null;
        String ret = e.toString();
        StackTraceElement[] stack = e.getStackTrace();
        for (int i = 0; stack != null && i < stack.length; ++i) {
            ret += "\n" + stack[i].toString();
        }
        return ret;
    }

    public static void PrintStackTrace(Exception e) {
        if (null == e)
            return;
        error(EnumConstants.nmsgTagGlobal, "Exception: " + e.toString());
        StackTraceElement[] stack = e.getStackTrace();
        for (int i = 0; stack != null && i < stack.length; ++i) {
            error(EnumConstants.nmsgTagGlobal, stack[i].toString());
        }
    }

    public static void nmsPrintStackTraceByTag(String tag, Exception e) {
        if (null == e)
            return;
        error(tag, "Exception: " + e.toString());
        StackTraceElement[] stack = e.getStackTrace();
        for (int i = 0; stack != null && i < stack.length; ++i) {
            error(tag, stack[i].toString());
        }
    }

    public static void error(String tag, String msg) {
        if (LOG_LEVEL <= LOG_ERROR) {
            Log.e(tag, "thread Id: " + Thread.currentThread().getId() + "  " + msg);
            appendLog(file, tag + "\t" + "thread Id: " + Thread.currentThread().getId() + "  "
                    + msg, 0);
        }
    }

    public static void trace(String tag, String msg) {
        if (LOG_LEVEL <= LOG_TRACE) {
            Log.i(tag, "thread Id: " + Thread.currentThread().getId() + "  " + msg);
            appendLog(file, tag + "\t" + "thread Id: " + Thread.currentThread().getId() + "  "
                    + msg, 1);
        }
    }

    public static void warn(String tag, String msg) {
        if (LOG_LEVEL <= LOG_WARNING) {
            Log.w(tag, "thread Id: " + Thread.currentThread().getId() + "  " + msg);
            appendLog(file, tag + "\t" + "thread Id: " + Thread.currentThread().getId() + "  "
                    + msg, 2);
        }
    }

    public static void appendLog(File file, String content, int level) {
        try {
            if (file == null || !file.exists()) {
                init(Application.getInstance());
                return;
            }
            StringBuffer sb = new StringBuffer();
            sb.append(EnumConstants.SDF2.format(new Date()));
            sb.append("\t ");
            sb.append(level == 1 ? "i" : level == 2 ? "w" : "e");
            sb.append("\t");
            sb.append(content);
            sb.append("\r\n");
            fWriter.write(sb.toString().getBytes());
        } catch (Exception e) {
            Log.e(EnumConstants.nmsgTagGlobal,
                    "log output exception,maybe the log file is not exists," + GetStactTrace(e));
        } finally {

            if (file != null && file.length() >= MAX_LOG_SIZE) {
                init(mContext);
                return;
            }
        }
    }

    static void setLogPriority(int priority) {
        LOG_LEVEL = priority;
    }

    public static int getLogPriority() {
        return LOG_LEVEL;
    }

    public static void readIniFile() {
        String filePath = FileEx.getSDCardPath() + File.separator + "nmsg.ini";
        File file = new File(filePath);
        String logLevelString = null;
        if (!file.exists()) {
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                if (tempString.indexOf("LOG_LEVEL") != -1) {
                    String[] str = tempString.split("=");
                    logLevelString = str[1];
                    logLevelString = logLevelString.replaceAll("\\s", "");
                    logLevelString = logLevelString.replaceAll(";", "");
                    int logLevel = Integer.parseInt(logLevelString);
                    MLog.error(TAG, "log was changed before loglevel is:" + getLogPriority());
                    setLogPriority(logLevel);
                    MLog.error(TAG, "log was changed current loglevel is:" + logLevel);
                    break;
                }
            }
            if(null != reader){
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
