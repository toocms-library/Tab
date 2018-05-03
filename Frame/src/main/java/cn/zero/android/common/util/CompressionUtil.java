package cn.zero.android.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;

public class CompressionUtil {

	/**
	 * 将图片压缩
	 * 
	 * @param originalPath
	 *            原件图片路径
	 * @param path
	 *            压缩之后要保存的路径
	 * @return String
	 */
	public static String compressionBitmap(String originalPath) {
		try {
			File originalFile = new File(originalPath);
			Bitmap bmp = decodeFile(originalFile);
			String newName = DigestUtils.md5(String.valueOf(SystemClock.currentThreadTimeMillis())) + ".jpg";
			File f = new File(FileManager.getCompressFilePath() + newName);
			f.createNewFile();
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(f);
			if (bmp != null)
				bmp.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
			fOut.flush();
			fOut.close();
			return f.getPath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private static Bitmap decodeFile(File f) throws FileNotFoundException {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(new FileInputStream(f), null, o);
		// The new size we want to scale to
		final int REQUIRED_HEIGHT = 800;
		final int REQUIRED_WIDTH = 480;
		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_WIDTH && height_tmp / 2 < REQUIRED_HEIGHT)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale++;
		}
		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	}

}
