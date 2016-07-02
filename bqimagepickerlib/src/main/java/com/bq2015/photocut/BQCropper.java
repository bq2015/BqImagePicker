package com.bq2015.photocut;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.bq2015.photocut.util.FileUtil;
import com.bq2015.photocut.util.ImageUtil;
import com.bq2015.photocut.util.PickerHelper;
import com.bq2015.photocut.util.StorageUtils;

import java.io.File;


/************************************************************
 * Author:  fangqq
 * Description:  上传图片文件处理类:图片选取(拍照\相册),裁剪
 * Date:2016/5/5
 ************************************************************/
public class BQCropper {


    public int mImageType;
    public static final String CROP_CACHE = "/cropCache";
    private final Intent mIntent;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;//拍照权限请求
    private static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 101;//读写权限请求
    private final Bundle mBundle;
    private Activity mActivity;
    private String srcPath;
    private String outPath = null;
    private int width = 800;//压缩目标宽度
    private int hight = 480;//压缩目标高度
    private File file = null;
    private int mMaxSize = 200;//默认压缩在300KB 以内


    public interface ImageType {
        int HEAD_PIC = 1;//头像:需要压缩  压缩300*300
        int NO_CLIP = 2;//其他需要压缩图(暂时不用)
        int CROP_RECT = 3;//长方形裁剪
    }


    /**
     * 构造器
     *
     * @param activity
     * @param imageType 图片上传类型(决定压缩与否)
     */
    public BQCropper(Activity activity, int imageType) {
        mActivity = activity;
        mImageType = imageType;
        mIntent = new Intent(activity, ImageCropActivity.class);
        mBundle = new Bundle();


    }

    public BQCropper width(int width) {
        this.width = width;
        return this;
    }

    public BQCropper hight(int hight) {
        this.hight = hight;
        return this;
    }

    public BQCropper maxSize(int maxSize) {
        mMaxSize = maxSize;
        return this;
    }

    /**
     * 拍照取得图片
     */

    public void getPhotoFromCamera() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(mActivity,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            createCachefile();//先创建临身缓存文件夹
            srcPath = PickerHelper.getPhotoFromCamera(mActivity,
                    PickerHelper.CAMERA,
                    String.valueOf(System.currentTimeMillis()));
        }

    }


    /**
     * 从相册库里取得图片
     */
    public void getPhotoFromPictureLibrary() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
        } else {
            createCachefile();//先创建临身缓存文件夹
            PickerHelper.getPhotoFromPictureLibrary(mActivity, PickerHelper.LIBRARY);
        }

    }

    /**
     * 申请权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createCachefile();//先创建临身缓存文件夹
                srcPath = PickerHelper.getPhotoFromCamera(mActivity,
                        PickerHelper.CAMERA,
                        String.valueOf(System.currentTimeMillis()));
            } else {
                Toast.makeText(mActivity, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                createCachefile();//先创建临身缓存文件夹
                PickerHelper.getPhotoFromPictureLibrary(mActivity, PickerHelper.LIBRARY);
            } else {
                Toast.makeText(mActivity, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 跳转裁剪页面进行裁剪
     */
    public void beginCrop(int type, Intent data) {
        if (mImageType == ImageType.NO_CLIP) {
            return;
        }

        if (type == PickerHelper.CAMERA) {
            mBundle.putString(ImageCropActivity.PATH, srcPath);
        } else if (type == PickerHelper.LIBRARY && data != null) {
            Uri uri = data.getData();
            mBundle.putParcelable(ImageCropActivity.URI, uri);
        }
        mBundle.putString(ImageCropActivity.CACHE_FOLDER, CROP_CACHE);
        mBundle.putInt(ImageCropActivity.IS_CAMERA, type);
        mBundle.putInt(ImageCropActivity.IMG_TYPE, mImageType);
        mIntent.putExtra(ImageCropActivity.BUNDLE, mBundle);
        mActivity.startActivityForResult(mIntent, PickerHelper.CROP);
    }

    /**
     * 取得裁剪后的Bitmap,参数直接传递onActivityResult的三个参数即可
     * 使用时需注意判空操作
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        意图信息
     * @return
     */
    public Bitmap getBitmap(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;
        if (resultCode == Activity.RESULT_OK && (mImageType == ImageType.HEAD_PIC) || mImageType == ImageType.CROP_RECT) {
            switch (requestCode) {
                case PickerHelper.CAMERA:
                    beginCrop(PickerHelper.CAMERA, data);
                    break;
                case PickerHelper.LIBRARY:
                    beginCrop(PickerHelper.LIBRARY, data);
                    break;
                case PickerHelper.CROP:
                    String cropImagePath = data.getStringExtra(ImageCropActivity.CROP_IMAGEPATH);
                    bitmap = BitmapFactory.decodeFile(cropImagePath);
                    break;
            }
        } else if (resultCode == Activity.RESULT_OK && mImageType == ImageType.NO_CLIP) {
            //拍照后压缩
            if (requestCode == PickerHelper.CAMERA) {
                ImageUtil.compressBitmap(srcPath, outPath, width, hight, mMaxSize);
            } else if (requestCode == PickerHelper.LIBRARY) {
                //从相册取出后压缩
                Uri uri = data.getData();
                ImageUtil.compressBitmap(ImageUtil.getRealPathFromUri(mActivity, uri),
                        outPath, width, hight, mMaxSize);
            }
            bitmap = BitmapFactory.decodeFile(outPath);
        }
        return bitmap;
    }

    /**
     * 对选择后的图片,进行压缩返回文件路径
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @return
     */

    public String getCompressPath(int requestCode, int resultCode, Intent data) {
        String path = null;
        if (resultCode == Activity.RESULT_OK && (mImageType == ImageType.HEAD_PIC) || mImageType == ImageType.CROP_RECT) {
            switch (requestCode) {
                case PickerHelper.CAMERA:
                    beginCrop(PickerHelper.CAMERA, data);
                    break;
                case PickerHelper.LIBRARY:
                    beginCrop(PickerHelper.LIBRARY, data);
                    break;
                case PickerHelper.CROP:
                    path = data.getStringExtra(ImageCropActivity.CROP_IMAGEPATH);
                    break;
            }
        } else if (resultCode == mActivity.RESULT_OK) {
            //拍照后压缩
            if (requestCode == PickerHelper.CAMERA) {
                ImageUtil.compressBitmap(srcPath, outPath, width, hight, mMaxSize);
            } else if (requestCode == PickerHelper.LIBRARY) {
                //从相册取出后压缩
                Uri uri = data.getData();
                ImageUtil.compressBitmap(ImageUtil.getRealPathFromUri(mActivity, uri),
                        outPath, width, hight, mMaxSize);
            }
            path = outPath;
        }

        return path;
    }


    /**
     * 创建缓文件夹及文件
     */
    private void createCachefile() {
        file = new File(StorageUtils.getCacheDirectory(mActivity).getAbsolutePath()

                + CROP_CACHE);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(file, String.valueOf(System.currentTimeMillis()) + ".jpg");
        this.outPath = file.getAbsolutePath();
    }

    /**
     * 删除缓文件夹及文件
     *
     * @return
     */
    public boolean clearCache() {
        File file = new File(StorageUtils.getCacheDirectory(mActivity).getAbsolutePath()
                + CROP_CACHE);
        return FileUtil.deleteDir(file);
    }

}
