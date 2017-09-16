package com.hesine.nmsg.util;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringEx {

	public static String MD5(String value){
		return MD5(value.getBytes());
	}

	public static String MD5(byte[] bytes){
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
				  'a', 'b', 'c', 'd', 'e', 'f' }; 
		try { 
			byte[] strTemp = bytes; 
			MessageDigest mdTemp = MessageDigest.getInstance("MD5"); 
			mdTemp.update(strTemp); 
			byte[] md = mdTemp.digest(); 
			int j = md.length; 
			char str[] = new char[j * 2]; 
			int k = 0; 
			for (int i = 0; i < j; i++) { 
				byte b = md[i]; 
				str[k++] = hexDigits[b >> 4 & 0xf]; 
				str[k++] = hexDigits[b & 0xf]; 
			} 
			return new String(str); 
		} catch (Exception e) {
			return null;
		} 
	}
	
	public static int getSubStringCount(String str, String split) {
		return str.split(split).length;
	}

	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String getFileMD5String(File file) {
		MessageDigest messagedigest = null;
		try {
			messagedigest = MessageDigest.getInstance("md5");
			FileInputStream in = new FileInputStream(file);
			FileChannel ch = in.getChannel();
			MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY,
					0, file.length());
			messagedigest.update(byteBuffer);
			in.close();
			return bufferToHex(messagedigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];
		char c1 = hexDigits[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}
}
