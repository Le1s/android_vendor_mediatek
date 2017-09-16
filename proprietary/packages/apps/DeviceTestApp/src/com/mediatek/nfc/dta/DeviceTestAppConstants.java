package com.mediatek.nfc.dta;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.InputStream;  
import java.io.OutputStream;  
import android.util.Log;

public class DeviceTestAppConstants  {
    
    /// ***  DTA Java Version ***
    public static final String DTA_JAVA_VERSION = "14102101";    
    /// **********************
    
    public static final String DATA_PATTERN_NUMBER = "PATTERN_NUMBER";
    public static final String ACTION_OPERATION_TEST_START = "action.operation.test.start";
    public static final String ACTION_DTA_MAIN_START = "action.dta.main.start";
    public static final String ACTION_DTA_PLATFORM_START = "action.dta.platform.start";
    public static final String ACTION_DTA_OPERATION_START = "action.dta.operation.start";
    public static final String ACTION_DTA_P2P_START = "action.dta.p2p.start";
    public static final String ACTION_DTA_SWP_START = "action.dta.swp.start";
    public static final String DTA_INSTRUMENT_NAME = "DTA_INSTRUMENT_NAME";
    public static final String DTA_DEST_CONFIG_PATH = "DTA_DEST_CONFIG_PATH";

    public static final int TEST_TYPE_PLATFORM = 0;
    public static final int TEST_TYPE_OPERATION = 1;
    public static final int TEST_TYPE_P2P = 2;

    //config folder
    public static final String DTA_CONFIG_FOLDER_SOURCE = "/system/etc/nfc_conformance/DTA_Config/";
    public static final String DTA_CONFIG_FOLDER_DEST   = "/system/etc/nfc_conformance/DTA_Config/";
    
    public static byte[] hexToBinary(String s){
        int len = s.length();
        byte[] data = new byte[len/2];
        if( len%2 != 0)
            return null;    
        for(int i = 0; i < len; i += 2 ) {
            data[i/2] = (byte)((Character.digit(s.charAt(i), 16) << 4)+ Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String binaryToHex(byte[] buf, int len){
        String ret = "";
        for(int i = 0; i < len; i++){
            ret = ret + "0123456789ABCDEF".charAt((byte)0xf&buf[i]>>4) + "0123456789ABCDEF".charAt(buf[i]&(byte)0xf) + " ";
        }
        return ret;
    }     
    
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static List<String> listFilesForFolder(final File folder) {
        List<String> fileList = new ArrayList<String>();
        
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                //System.out.println(fileEntry.getName());
                Log.d("DTA", "" + fileEntry.getName());
                fileList.add(fileEntry.getName());
            }
        }
        return fileList;
    }
    
}
