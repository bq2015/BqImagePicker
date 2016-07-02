package com.bq2015.photocut.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {
    /**
     * 裁剪宽高比
     */
    private static final double ASPECT_RATIO = 1.906;
    private static final String TAG = "ImageUtil";

    public static Bitmap decodeSampledBitmapFromResource(InputStream is, int reqWidth, int reqHeight, String path) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return rotateBitmap(BitmapFactory.decodeStream(is, null, options), getDegress(path));
    }

    /**
     * 通过ExifInterface类读取图片文件的被旋转角度
     *
     * @param path ： 图片文件的路径
     * @return 图片文件的被旋转角度
     */
    public static int readPicDegree(String path) {
        int degree = 0;

        // 读取图片文件信息的类ExifInterface
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
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

        return degree;
    }

    /**
     * 将图片纠正到正确方向
     *
     * @param degree ： 图片被系统旋转的角度
     * @param bitmap ： 需纠正方向的图片
     * @return 纠向后的图片
     */
    public static Bitmap rotateBitmap(int degree, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        if (bitmap != null) {
            Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bm;
        }
        return null;
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return rotateBitmap(BitmapFactory.decodeFile(path, options), getDegress(path));
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    // width height 实际需要的图片尺寸，为了保证裁剪的图片不变形，最好还是算一下缩放比例，这里就不写了
    public static Bitmap loadBitmap(String url, int width, int height) {
        Bitmap bitmap = null;
        if (url == null || !new File(url).exists())
            return bitmap;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(url, opts);
            opts.inSampleSize = calculateSampleSize(opts, width, height);
            opts.inJustDecodeBounds = false;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(url), null, opts);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static int calculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 如果输入的宽或高为0，就按源大小压缩品质
     *
     * @param srcPath
     * @param outPath
     * @param width
     * @param height
     * @return
     */
    public final static String compressBitmap(String srcPath, String outPath,
                                              int width, int height, int maxSize) {
        Bitmap bitmap;
        if (width > 0 && height > 0) {
            bitmap = compressBitmap(srcPath, width, height);
        } else {
            bitmap = compressBitmap(srcPath);
        }

        bitmap = compressBitmap(bitmap, maxSize);//压缩到指定范围内
        int degree = getDegress(srcPath);
        try {
            if (degree != 0)
                bitmap = rotateBitmap(bitmap, degree);
            //竖图裁剪中间部分
            if (bitmap.getWidth() < bitmap.getHeight()) {
                bitmap = CropByRatio(bitmap, ASPECT_RATIO);
            }
            File file = new File(outPath);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.e("panxin", e.getMessage());
            e.printStackTrace();
        }
        return srcPath;
    }

    /**
     * 按一定的宽高比对进行裁剪
     *
     * @param bitmap
     * @param ratio
     * @return
     */

    public static Bitmap CropByRatio(Bitmap bitmap, double ratio) {
        bitmap = Bitmap.createBitmap(bitmap,
                0,
                (int) ((bitmap.getHeight() - bitmap.getWidth() / ratio) / 2),
                bitmap.getWidth(),
                bitmap.getHeight() - (int) ((bitmap.getHeight() + bitmap.getWidth() / ratio) / 2)
        );
        return bitmap;
    }

    /**
     * 循环压缩指定大小范围内
     *
     * @param image
     * @param maxSize
     * @return
     */
    public static Bitmap compressBitmap(Bitmap image, int maxSize) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        //循环判断如果压缩后图片是否大于max kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > maxSize) {
            Log.d(TAG, baos.toByteArray().length / 1024 + " KB");
            baos.reset();//重置baos即清空baos
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 5;
        }
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        //把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public final static Bitmap compressBitmap(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public final static Bitmap compressBitmap(String path, int rqsW, int rqsH) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = caculateInSampleSize(options, rqsW, rqsH);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public final static int caculateInSampleSize(BitmapFactory.Options options, int rqsW, int rqsH) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (rqsW == 0 || rqsH == 0)
            return 1;
        if (height > rqsH || width > rqsW) {
            final int heightRatio = Math.round((float) height / (float) rqsH);
            final int widthRatio = Math.round((float) width / (float) rqsW);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static CompressFormat getImageFormat(String fileName) {
        if (fileName == null || fileName.equals("")) {
            return Bitmap.CompressFormat.JPEG;
        }
        fileName = fileName.toLowerCase();
        if (fileName.endsWith("jpeg") || fileName.endsWith("jpg")) {
            return Bitmap.CompressFormat.JPEG;
        } else if (fileName.endsWith("png")) {
            return Bitmap.CompressFormat.PNG;
        } else {
            return Bitmap.CompressFormat.PNG;
        }
    }

    /**
     * get the orientation of the bitmap {@link android.media.ExifInterface}
     *
     * @param path
     * @return
     */
    public final static int getDegress(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
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
        } catch (IOException e) {
            Log.e("bq2015", e.getMessage());
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * rotate the bitmap
     *
     * @param bitmap
     * @param degress
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }


    /**
     * 从Uri中获取图片的真实路径
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealPathFromUri(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 红米手机note机型不支持
     * 从Uri中获取文件路径
     *
     * @param uri 目标uri
     * @return 返回路径
     */
    @Deprecated
    private String getAbsoluteImagePath(Activity activity, Uri uri) {
        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        //cursor为空
        Cursor cursor = activity.managedQuery(uri, proj, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

}
