package com.hesine.nmsg.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Matrix;
import android.media.ExifInterface;

public class ImageUtils {
    public static final String TAG = "ImageUtils";

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

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h, boolean needRecycle) {
        if (null == bitmap) {
            return null;
        }
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
        if (needRecycle && !BitmapOrg.isRecycled()) {
            BitmapOrg.recycle();
        }
        return resizedBitmap;
    }

    public static byte[] resizeImg(String path, float maxLength) {
        int d = getExifOrientation(path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        int l = Math.max(options.outHeight, options.outWidth);
        int be = (int) (l / maxLength);
        if (be <= 0)
            be = 1;
        options.inSampleSize = be;
        bitmap = BitmapFactory.decodeFile(path, options);
        if (d != 0) {
            bitmap = rotate(bitmap, d);
        }
        String[] tempStrArry = path.split("\\.");
        String filePostfix = tempStrArry[tempStrArry.length - 1];
        CompressFormat formatType = null;
        if (filePostfix.equalsIgnoreCase("PNG")) {
            formatType = Bitmap.CompressFormat.PNG;
        } else if (filePostfix.equalsIgnoreCase("JPG") || filePostfix.equalsIgnoreCase("JPEG")) {
            formatType = Bitmap.CompressFormat.JPEG;
        } else if (filePostfix.equalsIgnoreCase("GIF")) {
            formatType = Bitmap.CompressFormat.PNG;
        } else if (filePostfix.equalsIgnoreCase("BMP")) {
            formatType = Bitmap.CompressFormat.PNG;
        } else {
            MLog.error(TAG, "Can't compress the image,because can't support the format:" + filePostfix);
            return null;
        }

        int quality = 100;
        if (be == 1) {
            if (FileEx.getFileSize(path) > 100 * 1024) {
                quality = 80;
            }
        } else {
            quality = 80;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(formatType, quality, baos);
        final byte[] tempArry = baos.toByteArray();
        if (baos != null) {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            baos = null;
        }
        return tempArry;
    }

    public static byte[] resizeImg(String path, int resize, int changesize) {

        MLog.trace(TAG, "request to resize image,path:" + path + " , size:" + resize);

        if (resize <= 1024) {
            MLog.error(TAG, "cancel to compress image,the resize is too small");
            return null;
        }

        File tempFile = new File(path);
        if (!tempFile.exists()) {
            MLog.error(TAG, "failed to find image file by the path:" + path);
            return null;
        }

        int fileSize = (int) tempFile.length();
        MLog.trace(TAG, "successful to find image file,length:" + fileSize);

        byte[] targetBytes;
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;
        Bitmap photo = null;
        String filePostfix = null;
        CompressFormat formatType = null;
        try {
            if (resize >= fileSize) {
                inputStream = new FileInputStream(tempFile);
                targetBytes = new byte[fileSize];
                if (inputStream.read(targetBytes) <= -1) {
                    MLog.error(TAG, "can't read the file to byet[]");
                } else {
                    MLog.trace(TAG, "successful to resize the image,after resize length is:"
                            + targetBytes.length);
                    return targetBytes;
                }

            } else {
                int multiple = fileSize / resize;
                if (fileSize % resize >= 0 && fileSize % resize >= ((fileSize / multiple + 1) / 2)) {
                    multiple++;
                }

                if (multiple > 3) {
                    if (resize == 200 * 1024) {
                        multiple = 3;
                    } else if (resize == 100 * 1024) {
                        multiple = 6;
                    } else if (resize <= 50 * 1024) {
                        multiple = 10;
                    }
                }

                MLog.trace(TAG, "prepare to press sacle:" + multiple);
                Options options = new Options();
                options.inScaled = true;
                if (changesize == 0)
                    options.inSampleSize = 1;
                else
                    options.inSampleSize = multiple;

                int compressCount = 1;
                do {
                    photo = BitmapFactory.decodeFile(path, options);
                    options.inSampleSize = multiple + compressCount;
                    MLog.trace(TAG, "try to encondw image " + compressCount + " times");
                    compressCount++;
                } while (photo == null && compressCount <= 5);

                String[] tempStrArry = path.split("\\.");
                filePostfix = tempStrArry[tempStrArry.length - 1];
                tempStrArry = null;
                MLog.trace(TAG, "filePostfix:" + filePostfix);
                if (filePostfix.equals("PNG") || filePostfix.equals("png")) {
                    formatType = Bitmap.CompressFormat.PNG;
                } else if (filePostfix.equals("JPG") || filePostfix.equals("jpg")
                        || filePostfix.equals("JPEG") || filePostfix.equals("jpeg")) {
                    formatType = Bitmap.CompressFormat.JPEG;
                } else if (filePostfix.equalsIgnoreCase("GIF")) {
                    formatType = Bitmap.CompressFormat.PNG;
                } else if (filePostfix.equalsIgnoreCase("BMP")) {
                    formatType = Bitmap.CompressFormat.PNG;
                } else {
                    MLog.error(TAG, "Can't compress the image,because can't support the format:"
                            + filePostfix);
                    return null;
                }

                int quality = 100;
                while (quality > 0) {
                    baos = new ByteArrayOutputStream();
                    photo.compress(formatType, quality, baos);
                    final byte[] tempArry = baos.toByteArray();
                    MLog.trace(TAG, "successful to resize the image,after resize length is:"
                            + tempArry.length + " ,quality:" + quality);

                    if (tempArry.length <= resize) {
                        targetBytes = tempArry;
                        MLog.trace(TAG, "successful to resize the image,after resize length is:"
                                + targetBytes.length);
                        return targetBytes;
                    }
                    if (tempArry.length >= 1000000) {
                        quality = quality - 10;
                    } else if (tempArry.length >= 260000) {
                        quality = quality - 5;
                    } else {
                        quality = quality - 1;
                    }

                    if (baos != null) {
                        baos.close();
                        baos = null;
                    }
                }

                MLog.error(TAG, "can't compress the photo with the scale size:" + multiple);
            }
        } catch (Exception e) {
            MLog.error(TAG, "Exception ,when reading file:" + MLog.GetStactTrace(e));
        } finally {
            try {
                filePostfix = null;
                formatType = null;

                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }

                if (baos != null) {
                    baos.close();
                    baos = null;
                }

                if (photo != null) {
                    if (!photo.isRecycled()) {
                        photo.recycle();
                    }
                    photo = null;
                }
            } catch (Exception e) {
                MLog.error(TAG,
                        "Exception,when recycel resource after compressing."
                                + MLog.GetStactTrace(e));
            }
        }

        return null;
    }

    
    
    
    public static Bitmap drawBitmapArrowRight(Bitmap bm) {
		Bitmap mode = createModeArrowRight(bm.getWidth(), bm.getHeight());
		Bitmap frame = createFrameArrowRight(bm.getWidth(), bm.getHeight());
		Canvas canvas = new Canvas(mode);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bm, 0f, 0f, paint);
		paint.reset();
		canvas.drawBitmap(frame, 0f, 0f, paint);

		return mode;
	}

	public static Bitmap createModeArrowRight(int width, int height) {
		Bitmap mode = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mode);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		int contentWidth = width - 20;
		int contentHeight = height;
		RectF rect = new RectF(0, 0, contentWidth, contentHeight);
		canvas.drawRoundRect(rect, 10, 10, paint);

		Path path = new Path();
		path.moveTo(contentWidth, 20f);
		path.lineTo(width, 30f);
		path.lineTo(contentWidth, 40f);
		path.lineTo(contentWidth, 20f);
		canvas.drawPath(path, paint);

		return mode;
	}

	public static Bitmap createFrameArrowRight(int width, int height) {
		Bitmap frame = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(frame);
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		paint.setDither(true);
		int contentWidth = width - 20;
		int contentHeight = height;
		RectF rect = new RectF(0, 0, contentWidth, contentHeight);
		canvas.drawRoundRect(rect, 10, 10, paint);

		Path path = new Path();
		path.moveTo(contentWidth, 20f);
		path.lineTo(width, 30f);
		path.lineTo(contentWidth, 40f);
		canvas.drawPath(path, paint);

		paint.reset();
		paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		canvas.drawRect(new RectF(contentWidth - 5f, 20f, contentWidth + 0.5f,
				40f), paint);
		return frame;
	}
	
	
	
	
	
	
	
	public static Bitmap drawBitmapArrowLeft(Bitmap bm) {
		Bitmap mode = createModeArrowLeft(bm.getWidth(), bm.getHeight());
		Bitmap frame = createFrameArrowLeft(bm.getWidth(), bm.getHeight());
		Canvas canvas = new Canvas(mode);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bm, 0f, 0f, paint);
		paint.reset();
		canvas.drawBitmap(frame, 0f, 0f, paint);

		return mode;
	}

	public static Bitmap createModeArrowLeft(int width, int height) {
		Bitmap mode = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mode);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		int arrowWidth = 20;
		int contentWidth = width;
		int contentHeight = height;
		RectF rect = new RectF(arrowWidth, 0, contentWidth, contentHeight);
		canvas.drawRoundRect(rect, 10, 10, paint);

		Path path = new Path();
		path.moveTo(arrowWidth, 20f);
		path.lineTo(0, 30f);
		path.lineTo(arrowWidth, 40f);
		canvas.drawPath(path, paint);

		return mode;
	}

	public static Bitmap createFrameArrowLeft(int width, int height) {
		Bitmap frame = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(frame);
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		paint.setDither(true);
		int arrowWidth = 20;
		int contentWidth = width;
		int contentHeight = height;
		RectF rect = new RectF(arrowWidth, 0, contentWidth, contentHeight);
		canvas.drawRoundRect(rect, 10, 10, paint);

		Path path = new Path();
		path.moveTo(arrowWidth, 20f);
		path.lineTo(0, 30f);
		path.lineTo(arrowWidth, 40f);
		canvas.drawPath(path, paint);

		paint.reset();
		paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		canvas.drawRect(new RectF(arrowWidth-1f, 20f, arrowWidth + 5f,
				40f), paint);
		return frame;
	}
	
}
