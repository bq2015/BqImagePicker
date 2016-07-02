package com.bq2015.photocut.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;


import java.io.File;
/************************************************************
 * Author:  fangqq
 * Description:  获取、裁剪照片类
 * Date:2016/5/5
 ************************************************************/
public class PhotoUtil {


	public final static int CAMERA = 10001;
	public final static int LIBRARY = 10002;
	public static final int CROP = 10003;
	public final static String CACHE_IMAGE = "/imagecache";


	private PhotoUtil() {
		
	}

	/**
	 * 调用相机并获取图片
	 */
	public static String getPhotoFromCamera(Activity activity, int requestResult,
			String fileName) {
		File file = StorageUtils.getCacheDirectory(activity);
		String dirName = file.getAbsolutePath() + "/";

		File dir = new File(dirName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		file = new File(dir, fileName);
		if (file.exists())
			file.delete();
		String cameraPath = file.getAbsolutePath();
		Uri uri = Uri.fromFile(file);
		intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		activity.startActivityForResult(intent, requestResult);
		Log.d("file*****", cameraPath);
		return cameraPath;
	}

	/**
	 * 调用图库并获取图片
	 */
	public static void getPhotoFromPictureLibrary(Activity activity, int requestResult) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		activity.startActivityForResult(intent, requestResult);
	}

	/**
	 * 将裁剪后的图片保存到本地指定目录
	 */
	public static void saveImage(Context context,Bitmap bm, String ImageName) {
		if (bm != null) {
			FileUtil.writeImage(bm, StorageUtils.getCacheDirectory(context)
					.getAbsolutePath() +"/"+CACHE_IMAGE+"/" + ImageName, 100);
		}
	}
	
	public static Uri judgeBitmapDimension(File file, Intent data, int requestCode)
			throws OutOfMemoryError {
		Uri uri = null;
		if (requestCode == CAMERA) {
			try {
				uri = Uri.fromFile(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (requestCode == LIBRARY && data != null) {
			uri = data.getData();
		}
		if (uri == null) {
			return null;
		}

		return uri;
	}

}
