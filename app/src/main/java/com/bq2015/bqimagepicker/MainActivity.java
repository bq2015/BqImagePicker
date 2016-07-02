package com.bq2015.bqimagepicker;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bq2015.photocut.BQCropper;
import com.bq2015.photocut.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private CircleImageView mCircleIv;
    private BQCropper mBQCropper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCircleIv = (CircleImageView) findViewById(R.id.circle_iv);
        mBQCropper = new BQCropper(this, BQCropper.ImageType.HEAD_PIC);
    }

    /**
     * 从相机选择图片
     * @param view  view
     */
    public void pickFromCamera(View view) {
        mBQCropper.getPhotoFromCamera();
    }

    /**
     * 从相册库里选择
     * @param view view
     */
    public void pickFromPotoLib(View view) {
        mBQCropper.getPhotoFromPictureLibrary();
    }

    /**
     * 显示取回的图片
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的intent数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String picPath = mBQCropper.getCompressPath(requestCode, resultCode, data);
        mCircleIv.setImageBitmap(BitmapFactory.decodeFile(picPath));
    }
}
