package com.bq2015.photocut;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ayundi.photocut.R;
import com.bq2015.photocut.util.ImageUtil;
import com.bq2015.photocut.util.PickerHelper;
import com.bq2015.photocut.util.StorageUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/************************************************************
 * Author:  fangqq
 * Description:  上传图片文件处理页面:图片选取(拍照\相册),裁剪
 * Date:2016/5/4
 ************************************************************/

public class ImageCropActivity extends Activity {

    private static final String TAG = "ImageCropActivity";
    public static final double ASPECT_RATIO = 1.906;
    public static final String CROP_IMAGEPATH = "cropImagePath";
    public static final String PATH = "path";
    public static final String IS_CAMERA = "isCamera";
    public static final String IMG_TYPE = "imgtype";
    public static final String BUNDLE = "bundle";
    public static final String URI = "uri";
    public static final String CACHE_FOLDER = "cacheFolder";

    private static Context mContext;
    private ClipImageView imageView;
    private Uri uri;
    private String avatarPath;
    private int isCamera = PickerHelper.CAMERA;// 是图库还是相机传过来的照片，以便做旋转角度处理
    private int imgType = 0;// 供图片裁剪使用，区别上传图片时图片名称来自于不同的界面
    private boolean loadFinish = false;

    private Button btnCancel;
    private Button btnSure;

    private BitmapWorkerTask task = null;
    private LinearLayout loadingLayout;
    private ClipView mClipView;
    private ClipCicleView mCicleView;
    private String mCacheFolder = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_crop);
        loadFinish = false;
        mContext = ImageCropActivity.this;
        Bundle bundle = this.getIntent().getBundleExtra(BUNDLE);
        uri = bundle.getParcelable(URI);
        avatarPath = bundle.getString(PATH);
        isCamera = bundle.getInt(IS_CAMERA);
        imgType = bundle.getInt(IMG_TYPE);
        mCacheFolder = bundle.getString(CACHE_FOLDER);

        initView();
    }

    private void initView() {
        mClipView = (ClipView) findViewById(R.id.rect_view);
        mCicleView = (ClipCicleView) findViewById(R.id.circle_view);

        if (imgType == 1) {
            mClipView.setVisibility(View.GONE);
            mCicleView.setVisibility(View.VISIBLE);
        } else if (imgType == 3) {
            mClipView.setVisibility(View.VISIBLE);
            mCicleView.setVisibility(View.GONE);
        }

        imageView = (ClipImageView) this.findViewById(R.id.cropImg);
        loadingLayout = (LinearLayout) findViewById(R.id.rl_loading_layout);
        loadingLayout.setVisibility(View.GONE);
        btnCancel = (Button) this.findViewById(R.id.btn_crop_cancel);
        btnSure = (Button) this.findViewById(R.id.btn_ok);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                task.cancel(true);
                ImageCropActivity.this.finish();
            }
        });
        btnSure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loadFinish) {
                    clipAndWriteImage();
                    ImageCropActivity.this.finish();
                }
            }
        });
        loadingLayout.setVisibility(View.VISIBLE);
        String filePath = "";
        if (isCamera == PickerHelper.CAMERA) {
            filePath = avatarPath;
        } else {
            filePath = ImageUtil.getRealPathFromUri(this, uri);
        }
        loadBitmap(filePath, imageView);
    }


    public void loadBitmap(String path, ClipImageView imageView) {
        task = new BitmapWorkerTask(imageView);
        task.execute(path);
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ClipImageView> imageViewReference;
        private String path = "";

        public BitmapWorkerTask(ClipImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            imageViewReference = new WeakReference<ClipImageView>(imageView);
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            path = params[0];
            if (isCamera == 0 && (path == null || path.equals(""))) {
                ContentResolver cr = getContentResolver();
                InputStream is = null;
                Bitmap bitmap = null;
                try {
                    is = cr.openInputStream(uri);
                    bitmap = ImageUtil.decodeSampledBitmapFromResource(is, 500, 500, path);
                    is.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bitmap;
            }
            return ImageUtil.decodeSampledBitmapFromResource(path, 500, 500);
            // return ImageUtil.compressBitmap(uri.getPath());
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            loadingLayout.setVisibility(View.GONE);
            if (bitmap != null) {
                final ClipImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    loadFinish = true;
                }
            }
        }
    }

    // 对图片进行处理并写入本地缓存
    private void clipAndWriteImage() {
        String fileName = "/" + System.currentTimeMillis() + ".jpg";
        String path = StorageUtils.getCacheDirectory(mContext).getAbsolutePath() + mCacheFolder + fileName;
        if (imgType == 1) {
            // 来自注册界面，需要对图片尺寸进行处理
            PickerHelper.saveImage(mContext, imageView.clipForCircle(), fileName);
            ImageUtil.compressBitmap(path, path, 200, 200, 100);
        } else if (imgType == 3) {
            // 来自上传相片，无需对图片尺寸进行处理
            PickerHelper.saveImage(mContext, imageView.clipForRct((float) ASPECT_RATIO), fileName);
            ImageUtil.compressBitmap(path, path, 800, 480, 100);
        }

        Intent mIntent = new Intent();
        mIntent.putExtra(CROP_IMAGEPATH, path);
        setResult(RESULT_OK, mIntent);
        this.finish();
    }


    @Override
    protected void onDestroy() {
        loadingLayout.setVisibility(View.GONE);
        super.onDestroy();
    }

}
