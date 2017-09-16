package com.hesine.nmsg.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import com.hesine.nmsg.common.EnumConstants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

public class Image {

	private static final int BUFFER = 8192;
	private static final int TIMEOUT = 30000;
	private static final int ONE_HUNDRED = 100;
	private static Handler mHandler = null;
	public static final String TAG = "Image";
	private static String imgPath = null;
	private Image() {

	}

	public static void setHandler(Handler handler){
		mHandler = handler;
	}
	
	

	public static String getImgPath() {
		return imgPath;
	}

	public static void setImgPath(String imgPath) {
		Image.imgPath = imgPath;
	}

	public static void saveImage(Context context, String iconName, byte[] buffer) {
		String filePath = null;
		if (FileEx.getSDCardStatus()) {
			filePath = FileEx.getSDCardPath() + File.separator
					+ EnumConstants.ROOT_DIR + File.separator + "image";
		} else {
			filePath = FileEx.getSDCardPath() + File.separator + "image";
		}

		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String path = filePath + File.separator + iconName;
		setImgPath(path);
		File f = new File(path);
		if (f.exists()) {
			return;
		} else {
			FileOutputStream fos = null;
			try {
				File parentFile = f.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
				fos = new FileOutputStream(path);
				fos.write(buffer);
				fos.flush();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fos != null) {
					fos = null;
				}
			}

		}
	}

	public static Bitmap getImageFromLocal(Context context, String iconName) {
		String filePath = null;
		if (iconName == null) {
			return null;
		}
		
		if (FileEx.getSDCardStatus()) {
			filePath = FileEx.getSDCardPath() + File.separator
					+ EnumConstants.ROOT_DIR + File.separator + "image";
		} else {
			filePath = FileEx.getSDCardPath() + File.separator + "image";
		}	
		String path = filePath + File.separator + iconName;
		setImgPath(path);
		File file = new File(path);
		if (file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			file.setLastModified(System.currentTimeMillis());
			return bitmap;
		}
		return null;
	}

	public static byte[] bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, ONE_HUNDRED, bas);
		return bas.toByteArray();
	}

	public static Bitmap saveImage2Local(final Context context,
			final String imgName, final String imgUrl) {
		Bitmap bitmap =null;		
		bitmap = getImageFromLocal(context, imgName);
		if (bitmap == null) {
			new Thread() {
				@Override
				public void run() {
					try {
						Looper.prepare();
						if (imgUrl != null && !"".equals(imgUrl.trim())) {
							URL url = new URL(imgUrl);
							URLConnection conn = url.openConnection();
							conn.setConnectTimeout(TIMEOUT);
							conn.setReadTimeout(TIMEOUT);
							conn.connect();
							BufferedInputStream bis = new BufferedInputStream(
									conn.getInputStream(), BUFFER);
							Bitmap bitmap = BitmapFactory.decodeStream(bis);
							if (bitmap != null) {
								saveImage(context, imgName,
										bitmap2Bytes(bitmap));
							}
							Message msg = mHandler.obtainMessage();
							msg.what = com.hesine.nmsg.common.EnumConstants.SAVE_IMG_SUCCESS;
							mHandler.sendMessage(msg);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

		} 
		return bitmap;
	}
	
	public static String saveImage2LocalForShowImg(final Context context,
			final String imgName, final String imgUrl) {
		String filePath = null;
		if (imgName == null) {
			return null;
		}
		
		if (FileEx.getSDCardStatus()) {
			filePath = FileEx.getSDCardPath() + File.separator
					+ EnumConstants.ROOT_DIR + File.separator + "image";
		} else {
			filePath = FileEx.getSDCardPath() + File.separator + "image";
		}	
		String path = filePath + File.separator + imgName;
		setImgPath(path);
		File file = new File(path);
		if (file.exists()) {			
			return path;
		}else{
			new Thread() {
				@Override
				public void run() {
					try {
						Looper.prepare();
						if (imgUrl != null && !"".equals(imgUrl.trim())) {
							URL url = new URL(imgUrl);
							URLConnection conn = url.openConnection();
							conn.setConnectTimeout(TIMEOUT);
							conn.setReadTimeout(TIMEOUT);
							conn.connect();
							BufferedInputStream bis = new BufferedInputStream(
									conn.getInputStream(), BUFFER);
							Bitmap bitmap = BitmapFactory.decodeStream(bis);
							if (bitmap != null) {
								saveImage(context, imgName,
										bitmap2Bytes(bitmap));
							}
							Message msg = mHandler.obtainMessage();							
							msg.what = com.hesine.nmsg.common.EnumConstants.SAVE_IMG_FOR_SHOW_SUCCESS;
							mHandler.sendMessage(msg);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
			
			return null;
		} 		
	}

	public static Bitmap loadBitmap(final String imageURL, String localDir) {
		String bitmapName = imageURL
				.substring(imageURL.lastIndexOf("/") + 1);
		File cacheDir = new File(localDir);
		File[] cacheFiles = cacheDir.listFiles();
		int i = 0;
		if(null != cacheFiles) {
			for (; i < cacheFiles.length; i++) {
				if (bitmapName.equals(cacheFiles[i].getName())) {
					break;
				}
			}

			if (i < cacheFiles.length) {
				return BitmapFactory.decodeFile(localDir + bitmapName);
			}
		}

		return null;
	}
	
	public static Bitmap getBitmapFromFile(String filePath){
	    Bitmap bp = null;
	    if(TextUtils.isEmpty(filePath)){
	        MLog.trace(TAG, "filePath is empty");
	        return null;
	    }else if(!FileEx.ifFileExisted(filePath)){
	        MLog.trace(TAG, "image is not existed");
	        return null;
	    }
	    bp = BitmapFactory.decodeFile(filePath);
	    if(null == bp){
	        MLog.trace(TAG, "can not parse avatar fileOath: " + filePath);
	    }
	    return bp;
	}
}
