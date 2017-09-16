package com.hesine.nmsg.util;

import android.annotation.SuppressLint;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AESCoder {
//	private static final int BLOCK_SIZE = 16;
//	private static final byte[] dummyBlock = new byte[BLOCK_SIZE];
	private static final String[] hexDigits = { "0", "1", "2", "3", "4", "5","6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
	private static final String key = "0B41883A7B4599F51C1462CF9606CE3C";

	public byte[] rnmDecrypt(String encrypts) throws Exception {	
		byte[] bkey = key2Byte(key);
		return decrypt(encrypts.getBytes(),bkey);
	}
	
	@SuppressLint("DefaultLocale")
	public byte[] rnmDecrypt(String encrypts, String key) throws Exception {	
		byte[] bkey = getKeyOfMd5(key.toUpperCase().getBytes());
		return decrypt(encrypts.getBytes(),bkey);
	}
	
	public byte[] rnmDecrypt(byte[] encrypts) throws Exception {	
		byte[] bkey = key2Byte(key);
		return decrypt(encrypts,bkey);
	}
	
	@SuppressLint("DefaultLocale")
	public byte[] rnmDecrypt(byte[] encrypts, String key) throws Exception {	
		byte[] bkey = getKeyOfMd5(key.toUpperCase().getBytes());
		return decrypt(encrypts,bkey);
	}
	
	public byte[] rnmEncrypt(String encrypts) throws Exception {		
		byte[] bkey = key2Byte(key);
		return encrypt(encrypts.getBytes("UTF-8"),bkey);
	}
	
	@SuppressLint("DefaultLocale")
	public byte[] rnmEncrypt(byte[] encrypts, String key) throws Exception {	
		byte[] bkey=getKeyOfMd5(key.toUpperCase().getBytes());		
		return encrypt(encrypts,bkey);		
	}
	
	@SuppressLint("DefaultLocale")
	public byte[] rnmEncrypt(String encrypts, String key) throws Exception {	
		byte[] bkey=getKeyOfMd5(key.toUpperCase().getBytes());		
		return encrypt(encrypts.getBytes(),bkey);		
	}
	
	public byte[] rnmEncrypt(byte[] encrypts) throws Exception {	
		byte[] bkey=key2Byte(key);		
		return encrypt(encrypts,bkey);		
	}
	
	public String debug(byte[] encrypts) {
		return byteArrayToHexString(encrypts);
	}
	
	private byte[] getKeyOfMd5(byte[] origin) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		return md.digest(origin);
	}

	protected byte[] key2Byte(String key) {
		byte[] fileKey = new byte[16];

		for (int i = 0, j = 0; j < 16;) {
			String s = key.substring(i, i + 2);
			int n = Integer.valueOf(s, 16).intValue();
			fileKey[j] = (byte) n;
			j++;
			i = i + 2;

		}
		
		//return fileKey;
		return key.getBytes();
	}
	
	private byte[] encrypt(byte[] encrypts, byte[] key)throws NoSuchAlgorithmException, NoSuchPaddingException,InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
		Cipher cp = Cipher.getInstance("AES");///CTR/NoPadding
		cp.init(Cipher.ENCRYPT_MODE, secretKeySpec/*, new IvParameterSpec(dummyBlock)*/);
		return cp.doFinal(encrypts);
	}
	
	private byte[] decrypt(byte[] encrypts, byte[] key)throws NoSuchAlgorithmException, NoSuchPaddingException,InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
		Cipher cp = Cipher.getInstance("AES");///CTR/NoPadding
		cp.init(Cipher.DECRYPT_MODE, secretKeySpec/*, new IvParameterSpec(dummyBlock)*/);
		return cp.doFinal(encrypts);
	}

	private static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}

	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;

		// get the first four bit
		int d1 = n / 16;

		// get the second four bit
		int d2 = n % 16;

		return hexDigits[d1] + hexDigits[d2];
	}
}
