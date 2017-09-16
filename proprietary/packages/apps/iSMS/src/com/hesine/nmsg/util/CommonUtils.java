package com.hesine.nmsg.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.hesine.nmsg.Application;
import com.hesine.nmsg.activity.ConversationActivity;
import com.hesine.nmsg.bean.ServiceInfo;

@SuppressLint({ "SdCardPath", "SimpleDateFormat" })
@SuppressWarnings("deprecation")
public class CommonUtils {
    public static final String TAG = "CommonUtils";
    public static String storagepath = "/data/data/com.hesine.imessage";

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static String getCachePath() {
        String path = null;
        String sdCardPath = getMemPath();
        if (!TextUtils.isEmpty(sdCardPath)) {
            path = sdCardPath + File.separator + "imessage" + "/Cache/";
            ;
        }
        return path;
    }

    public static String getMemPath() {
        return storagepath;
    }

    public static String getSDCardPath() {
        File sdDir = null;
        String sdStatus = Environment.getExternalStorageState();

        if (TextUtils.isEmpty(sdStatus)) {
            return storagepath;
        }

        boolean sdCardExist = sdStatus.equals(android.os.Environment.MEDIA_MOUNTED);

        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
            return sdDir.toString();
        }

        return storagepath;
    }

    public static boolean getSDCardStatus() {
        boolean ret = false;
        String sdStatus = Environment.getExternalStorageState();
        MLog.trace(TAG, "=" + sdStatus + "=");
        if (sdStatus.equals(Environment.MEDIA_MOUNTED))
            ret = true;
        return ret;
    }

    public static boolean isNumeric1(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath);
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean delAllFile(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);
                delFolder(path + "/" + tempList[i]);
                flag = true;
            }
        }
        return flag;
    }

    public static boolean delFile(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (file.isDirectory()) {
            return flag;
        }
        File temp = null;
        temp = new File(path);
        if (temp.isFile()) {
            temp.delete();
        }
        return flag;
    }

    public static String setHesineMail(String number) {
        StringBuffer sb = new StringBuffer();
        if (!TextUtils.isEmpty(number)) {
            sb.append(number.startsWith("+") ? number.substring(1, number.length()) : number);
            sb.append("@hissage.com");
        }
        return sb.toString();
    }

    public static byte[] readZeroTerminateBytes(DataInputStream dis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b = dis.readByte();
        DataOutputStream dos = new DataOutputStream(baos);
        while (b != 0) {
            dos.writeByte(b);
            b = dis.readByte();
        }
        return baos.toByteArray();
    }

    public static boolean validateEmail(String email) {
        boolean tag = true;
        final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        final Pattern pattern = Pattern.compile(pattern1);
        final Matcher mat = pattern.matcher(email);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    public static String getLocalIp() {
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            return localMachine.getHostAddress();
        } catch (UnknownHostException e) {
            MLog.PrintStackTrace(e);
            return null;
        }
    }

    public static String getPreLevelPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        int start = path.lastIndexOf("/");
        if (start != -1) {
            return path.substring(0, start);
        }
        return path;
    }

    public static String getFileName(String pathandname, boolean isExt) {
        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            if (isExt) {
                String[] pars = pathandname.split("\\.");
                return pathandname.substring(start + 1, end) + "." + pars[pars.length - 1];
            } else {
                return pathandname.substring(start + 1, end);
            }
        } else {
            return null;
        }
    }

    public static String getNumber(String linkman) {
        if (linkman != null && linkman.length() > 0) {
            if (linkman.split("<").length > 1) {
                String str = linkman.split("<")[1];
                if (str != null && str.length() > 0) {
                    return str.substring(0, str.length() - 1);
                }
            }
        }
        return linkman;
    }

    public static String getUserName(String user) {
        if (user != null && user.length() > 0) {
            if (user.split("<").length > 1) {
                String str = user.split("<")[0];
                if (str != null && str.length() > 0) {
                    if ((str.startsWith("\"") || str.startsWith("\'"))
                            && (str.endsWith("\"") || str.endsWith("\'")))
                        return str.substring(1, str.length() - 1);
                    else
                        return str;
                } else {
                    return user.split("<")[1];
                }
            }
        }
        return user;
    }

    public static String getUserAddr(String addr) {
        if (addr != null && addr.length() > 0) {
            if (addr.contains("<") && addr.contains(">")) {
                String str = addr.split("<")[1];
                if (str != null && str.length() > 0) {
                    return str.substring(0, str.length() - 1);
                }
            } else {
                return addr;
            }
        }
        return addr;
    }

    public static boolean isEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean isExistsFile(String filepath) {
        try {
            if (TextUtils.isEmpty(filepath)) {
                return false;
            }
            File file = new File(filepath);
            return file.exists();
        } catch (Exception e) {
            // e.printStackTrace();
            MLog.error(TAG,
                    "the file is not exists file path is: " + filepath + MLog.GetStactTrace(e));
            return false;
        }
    }

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

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                        .hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            MLog.PrintStackTrace(ex);
            return null;
        }
        return null;
    }

    public static String getFileExt(String filepath) {
        String ext = "";
        String[] tmp = filepath.split("\\.");
        if (tmp.length > 0) {
            ext = "." + tmp[tmp.length - 1];
        }
        return ext;
    }

    public static void copy(String src, String dest) {// **********
        InputStream is = null;
        OutputStream os = null;

        try {
            is = new BufferedInputStream(new FileInputStream(src));
            os = new BufferedOutputStream(new FileOutputStream(dest));

            byte[] b = new byte[256];
            int len = 0;
            try {
                while ((len = is.read(b)) != -1) {
                    os.write(b, 0, len);

                }
                os.flush();
            } catch (IOException e) {
                MLog.PrintStackTrace(e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        MLog.PrintStackTrace(e);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            MLog.PrintStackTrace(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    MLog.PrintStackTrace(e);
                }
            }
        }
    }

    public static void nmsStream2File(byte[] stream, String filepath) throws Exception {
        FileOutputStream outStream = null;
        try {
            File f = new File(filepath);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            outStream = new FileOutputStream(f);
            outStream.write(stream);
            outStream.flush();
        } catch (IOException e) {
            MLog.PrintStackTrace(e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                    outStream = null;
                } catch (IOException e) {
                    MLog.PrintStackTrace(e);
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
    }

    public static String readFile(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        int length = fis.available();
        byte[] buffer = new byte[length];
        fis.read(buffer);
        String res = new String(buffer, "ISO-8859-1");// EncodingUtils.getAsciiString(buffer);
        fis.close();
        return res;
    }

    public static boolean isPhoneNumberValid(String number) {
        boolean isValid = false;
        if (number == null || number.length() <= 0) {
            MLog.trace(TAG, "isPhoneNumberValid, number is null");
            return false;
        }
        Pattern PHONE = Pattern.compile( // sdd = space, dot, or dash
                "(\\+[0-9]+[\\- \\.]*)?" // +<digits><sdd>*
                        + "(\\([0-9]+\\)[\\- \\.]*)?" // (<digits>)<sdd>*
                        + "([0-9][0-9\\- \\.][0-9\\- \\.]+[0-9])");
        Matcher matcher = PHONE.matcher(number);
        isValid = matcher.matches();
        return isValid;
    }

    public static int NmsGetSystemTime() {
        long millSeconds = System.currentTimeMillis();
        return (int) (millSeconds / 1000);
    }

    public static String getDate(long date) {
        Date dateData = new Date(date);
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        return formater.format(dateData);
    }

    public static void createLoseSDCardNotice(Context context) {
        Toast.makeText(context, com.hesine.nmsg.R.string.chat_lose_sdcard, Toast.LENGTH_SHORT)
                .show();
    }

    public static void copyToClipboard(Context context, String s) {
        ClipboardManager cm = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(s);
    }

    public static boolean isNetworkOK(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        NetworkInfo mobNetInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (activeNetInfo != null && mobNetInfo != null) {
            return true;
        } else {
            return false;
        }
    }

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            MLog.error(TAG, MLog.GetStactTrace(ex));
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                }

            }
        }
        return degree;
    }

    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }

    public static String getValidName(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        String regEx = "[\"<>,]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("");
    }

    public static String file_Read(String dirPath, String fileName) {
        String ret = null;
        try {
            File file;
            FileInputStream in;

            file = new File(dirPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = null;
            file = new File(dirPath, fileName);

            file.createNewFile();
            in = new FileInputStream(file);
            int length = (int) file.length();
            byte[] temp = new byte[length];
            in.read(temp, 0, length);
            ret = new String(temp, "UTF-8");
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static void file_Write(String dirPath, byte[] bytes, String fileName) {
        try {
            File file;
            FileOutputStream out;
            file = new File(dirPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = null;
            file = new File(dirPath, fileName);

            file.createNewFile();
            out = new FileOutputStream(file);
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void file_Write(String dirPath, String data, String fileName) {
        try {
            file_Write(dirPath, data.getBytes("UTF-8"), fileName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static boolean currentActivityIsNmsg(String account) {
        Application.getInstance();
		ActivityManager am = (ActivityManager) Application.getInstance().getSystemService(
                Application.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(2).get(0).topActivity;
        if (cn.getClassName().endsWith("com.hesine.nmsg.activity.ConversationActivity")) {
            ConversationActivity instance = ConversationActivity.getInstance();
            String currentAccount = null;
            if (null != instance) {
                currentAccount = instance.getCurrentAccount();
            }
            if (null != currentAccount) {
                return currentAccount.equals(account);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isExistSystemContactViaAccount(ServiceInfo si) {
        String account = si.getAccount();
        if (TextUtils.isEmpty(account)) {
            MLog.error(TAG, "isExistSystemContactViaEmail. email is empty!");
            return false;
        }

        String encodeAccount = Uri.encode(account);
        if (TextUtils.isEmpty(encodeAccount)) {
            MLog.error(TAG, "isExistSystemContactViaEmail. encodeEmail is empty!");
            return false;
        }
        Cursor cursor = null;
        boolean result = false;
        try {
            Uri lookupUri = Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, encodeAccount);
            cursor = Application.getInstance().getContentResolver()
                    .query(lookupUri, null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                if (cursor.getCount() > 0) {
                    if (cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
                            .equals(si.getName())) {
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            MLog.error(TAG, "email: " + account + ". encodeEmail: " + encodeAccount);
            MLog.PrintStackTrace(e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return result;
    }

    public static boolean addContactInPhonebook(ServiceInfo si) {
        Bitmap bp = Image.getBitmapFromFile(si.getIcon());
        ContentResolver resolver = Application.getInstance().getContentResolver();
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation op1 = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_NAME, null).build();
        operations.add(op1);

        Uri uri = Data.CONTENT_URI;
        ContentProviderOperation op2 = ContentProviderOperation.newInsert(uri)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, si.getName()).build();
        operations.add(op2);

        if (bp != null) {
            ContentProviderOperation op3 = ContentProviderOperation.newInsert(uri)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                    .withValue(Photo.PHOTO, Image.bitmap2Bytes(bp)).build();
            operations.add(op3);
        }
        ContentProviderOperation op4 = ContentProviderOperation.newInsert(uri)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                .withValue(Email.DATA, si.getAccount()).withValue(Email.TYPE, Email.TYPE_WORK)
                .build();
        operations.add(op4);
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, operations);
            return true;
        } catch (Exception e) {
            MLog.PrintStackTrace(e);
            return false;
        }
    }

    public static void updateContactInPhonebook(ServiceInfo si) {
        Bitmap bp = Image.getBitmapFromFile(si.getIcon());
        Cursor c = null;
        if (null != bp) {
            try {
                ContentResolver cr = Application.getInstance().getContentResolver();
                long rawContactId = 0;
                int photoRow = 0;
                String select = String.format("%s=? AND %s='%s'", Email.DATA, Data.MIMETYPE,
                        Email.CONTENT_ITEM_TYPE);
                String[] project = new String[] { Data.RAW_CONTACT_ID };
                c = cr.query(Data.CONTENT_URI, project, select,
                        new String[] { si.getAccount() }, null);

                if (null != c && c.moveToFirst()) {
                    rawContactId = c.getLong(c.getColumnIndex(Data.RAW_CONTACT_ID));
                }
                if (null != c) {
                    c.close();
                }

                String where = Data.RAW_CONTACT_ID + " = " + rawContactId + " AND " + Data.MIMETYPE
                        + "='" + Photo.CONTENT_ITEM_TYPE + "'";
                c = cr.query(Data.CONTENT_URI, null, where, null, null);
                if (null != c && c.moveToFirst()) {
                    photoRow = c.getInt(c.getColumnIndexOrThrow(Data._ID));
                }

                if (null != c) {
                    c.close();
                }

                ContentValues values = new ContentValues();

                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                // values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
                values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, Image.bitmap2Bytes(bp));
                values.put(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                if (photoRow > 0) {
                    cr.update(ContactsContract.Data.CONTENT_URI, values, Data._ID + " = "
                            + photoRow, null);
                } else {
                    cr.insert(Data.CONTENT_URI, values);
                }
            } catch (Exception e) {
                MLog.PrintStackTrace(e);
                if(null != c){
                    c.close();
                }
            }
        }
    }
}
