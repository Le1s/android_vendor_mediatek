package com.mediatek.smsreg;

import android.content.Context;
import android.os.FileUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class BlackListUnit {
    private static final String TAG = "SmsReg/BlackListUnit";
    private static final String NAME_BLACK_FILE = "blackList";
    private static final String FAKE_IMSI = "460110000011111";

    private static final int LENGTH_HEADER = 1;
    private static final int LENGTH_SIM_IMSI = 15;

    private String mFilePath;
    private static final byte SUB_NUMBER = (byte) PlatformManager.SUB_NUMBER;
    private List<String> mList = new LinkedList<String>();

    public BlackListUnit() {
    }

    /**
     * Initiate length & black list from file; If file no exist, create a new file
     */
    public BlackListUnit(Context context) {
        File fileDir = context.getFilesDir();
        mFilePath = fileDir + "/" + NAME_BLACK_FILE;

        File blackfile = new File(mFilePath);
        if (blackfile.exists()) {
            Log.i(TAG, "Read info from " + mFilePath);
            byte[] data = readFromFile(mFilePath);
            decode(data);

        } else {
            if (!fileDir.exists()) {
                Log.i(TAG, "Folder " + fileDir + " not exist, create it.");
                if (fileDir.mkdir()) {
                    // Chmod for recovery access
                    FileUtils.setPermissions(fileDir, FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IXOTH, -1, -1);
                } else {
                    throw new Error("Failed to create folder " + fileDir + ".");
                }
            }

            Log.i(TAG, "File not exist, create " + mFilePath);
            resetBlackFile();
        }
    }

    /**
     * Add an IMSI into the black list and update file
     * Note:Change the parameter "Subid" to "slotId", mainly to match the CMCC CTA DM Spec.
     * If a SIM card contains multiple subID,need change solution
     */
    public void blockImsi(String imsi, long slotId) {
        Log.i(TAG, "Block imsi " + imsi);

        if (LENGTH_SIM_IMSI != imsi.length()) {
            throw new Error("Error imsi length " + imsi.length());
        }

        mList.set((int) slotId, imsi);

        for (String info: mList) {
            Log.i(TAG, "blocked imsi is " + info);
        }

        writeToFile(encode(), mFilePath);
    }

    /**
     * Find the first IMSI not in black list, then return the index
     */
    public int getMinAvailId(String imsi[]) {
        int index = -1;

        for (int i = 0; i < imsi.length; ++i) {
            if (imsi[i] != null) {
                if (!mList.contains(imsi[i])) {
                    index = i;
                    break;
                }
            }
        }
        Log.i(TAG, "Get the minimum available index " + index);
        return index;
    }

    /**
     * Reset the black list and update file
     */
    public void resetBlackFile() {
        mList = new LinkedList<String>();
        for (int i = 0; i < SUB_NUMBER; ++i) {
            mList.add(FAKE_IMSI);
        }
        writeToFile(encode(), mFilePath);
    }

    /**
     * Decode length & black list info from an byte array
     */
    private void decode(byte[] data) {

        if (data[0] != SUB_NUMBER) {
            throw new Error("Error sim number " + SUB_NUMBER);
        }

        int offset = LENGTH_HEADER;
        mList = new LinkedList<String>();

        for (int i = 0; i < SUB_NUMBER; ++i) {
            byte[] imsi = new byte[LENGTH_SIM_IMSI];
            System.arraycopy(data, offset, imsi, 0, LENGTH_SIM_IMSI);
            offset += LENGTH_SIM_IMSI;
            mList.add(new String(imsi));
        }

        for (String imsi: mList) {
            Log.i(TAG, "blocked imsi is " + imsi);
        }
    }

    /**
     * Encode the length & black list into an byte array
     */
    private byte[] encode() {
        int length_data =  LENGTH_HEADER + LENGTH_SIM_IMSI * SUB_NUMBER;

        byte[] data = new byte[length_data];
        data[0] = SUB_NUMBER;

        int offset = LENGTH_HEADER;
        for (int i = 0; i < SUB_NUMBER; ++i) {
            System.arraycopy(mList.get(i).getBytes(), 0, data, offset, LENGTH_SIM_IMSI);
            offset += LENGTH_SIM_IMSI;

        }
        return data;
    }

    /**
     * Read a byte[] array from file
     */
    private byte[] readFromFile(String filePath) {
        byte[] data = null;
        try {
            InputStream in = new FileInputStream(filePath);

            data = new byte[in.available()];
            in.read(data);
            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "Read data " + (new String(data)));
        return data;
    }

    /**
     * Write a byte[] array to file
     */
    private void writeToFile(byte[] data, String filePath) {
        Log.i(TAG, "Write data " + (new String(data)));

        // Write byte[] to file
        try {
            FileOutputStream out = new FileOutputStream(filePath);

            Log.i(TAG, "Flush & syn");
            out.write(data);
            out.flush();
            out.getFD().sync();
            out.close();

            Log.i(TAG, "Close stream");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
