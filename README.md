### 权限 ###
需要在清单文件中添加如下权限   
   ` <uses-feature android:name="android.hardware.camera" />`   
   ` <uses-feature android:name="android.hardware.camera.autofocus" />   `
    `<uses-feature android:name="android.hardware.camera.flash" /> `   
   ` <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/> `   
    `<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/> ` 


### 注册Activity ###

需要在清单文件中注册图片裁剪页面
    `<activity android:name="ImageCropActivity"/>`


### 创建裁剪器 YDCropper###
1.创建

    mYDCropper = new YDCropper(this, YDCropper.ImageType.HEAD_PIC);
参数说明:    
第一个为:activity  
第二为:图片上传类型,决定是否压缩  

    public interface ImageType {
        int HEAD_PIC = 1;//头像等需要压缩 300*300
        int PIC = 2;//普通需要压缩 300*300(暂时不用)
        int DEAL_UPHOTO = 3;//保持裁剪大小
    }

2.调用拍照

    mYDCropper.getPhotoFromCamera("headpic");//参数指定当前图片用途:如头像headpic
    
3 . 从相册库里取

    mYDCropper.getPhotoFromPictureLibrary();


### 获取图形Bitmap ###
 
     @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);        
        Bitmap bitmap = mYDCropper.getBitmap(requestCode, resultCode, data);
		.......



### 解决6.0运行时权限 ###
内部已经做判断,直接回调方法onRequestPermissionsResult即可

     @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        mYDCropper.onRequestPermissionsResult(requestCode,permissions,grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


