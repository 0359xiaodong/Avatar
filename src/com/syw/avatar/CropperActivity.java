package com.syw.avatar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.syw.avatar.util.ImageUtil;
import com.syw.avatar.util.SLog;
import com.syw.avatar.widget.ClipImageView;

public class CropperActivity extends Activity{
    private static final String TAG = CropperActivity.class.getSimpleName();
    private AlertDialog progressDialog;

    private String pickWay = null; // 照片选取方式: TAKE/PICK;
    private String imgCachePath = Constants.BasePhotoUrlDiskCached;
    private String pathTmpImage = null;
    private String pathPhotoTaked = null;
    private String pathCropperImage = null; // 剪辑后的头像图片存放路径;
    private Bitmap croppedImage; // 剪辑后的bitmap;
    private Bitmap initialImage; // 原始bitmap;
    
    private String albumName; // 带回去的相册名称;
    
    private TextView tvCancel;
    private TextView tvRetakePhoto;
    private TextView tvUseThisCropperPhoto;
    private ClipImageView civCropperPreview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cropper);

        tvCancel = (TextView)findViewById(R.id.tv_cancel);
        tvRetakePhoto = (TextView)findViewById(R.id.tv_retakephoto);
        tvUseThisCropperPhoto = (TextView)findViewById(R.id.tv_use_this_cropper_photo);
        civCropperPreview = (ClipImageView)findViewById(R.id.civ_cropper_preview);
        
        findViewById(R.id.tv_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initialImage.recycle();
                startImageLocalPickActivity();
            }
        });
        findViewById(R.id.tv_retakephoto).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initialImage.recycle();
                startImageCaptureActivity();
                
            }
        });
        
        findViewById(R.id.tv_use_this_cropper_photo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 此处获取剪裁后的bitmap
                Bitmap croppedImage = civCropperPreview.clip();
                
                long now = System.currentTimeMillis();
                String photoId = String.format("%d.%03d", now/1000, now%1000);
                String fileName = photoId+".jpg";
                pathCropperImage = imgCachePath + "/" + fileName;
                try {
                    FileOutputStream out = new FileOutputStream(pathCropperImage);
                    SLog.d(TAG, "try to compress file : " + pathCropperImage);
                    // 按原有质量的60%压缩;
                    if (croppedImage.compress(Bitmap.CompressFormat.JPEG, 60, out)){
                        out.flush();
                    }
                    else{
                        ImageUtil.SaveBitmap(croppedImage, pathCropperImage);
                        SLog.d(TAG, "try to save file : " + pathCropperImage);
                    }
                    out.close();
                } 
                catch (FileNotFoundException e) { // for FileOutputStream;
                    e.printStackTrace();
                }
                catch (IOException e) { // for out.flush & out.close;
                    e.printStackTrace();
                }
                ImageUtil.SaveBitmap(croppedImage, pathCropperImage);
                // 删除拍照的原图;
                if (pathPhotoTaked != null){
                    new File(pathPhotoTaked).delete();
                }
                
                Intent data = new Intent();
                if (pathCropperImage == null){
                    pathCropperImage = "";
                }
                data.putExtra("CropperPhotoPath", pathCropperImage);
                setResult(RESULT_OK, data);
                CropperActivity.this.finish();
            }
        });
        
        pickWay = getIntent().getStringExtra("PickWay");
        imgCachePath = getIntent().getStringExtra("ImgCachePath");
        if (imgCachePath==null || imgCachePath.length() <= 0){
            imgCachePath = Constants.BasePhotoUrlDiskCached;
        }
        
        if (pickWay.equals("TAKE")){ //拍照方式;
            tvCancel.setVisibility(View.GONE);
            tvRetakePhoto.setVisibility(View.VISIBLE);
            startImageCaptureActivity();
        }
        else if (pickWay.equals("PICK")){ // 本地相册选取;
            tvRetakePhoto.setVisibility(View.GONE);
            tvCancel.setVisibility(View.VISIBLE);
            startImageLocalPickActivity();
        }
        else{
            finish();
        }
    }

    private void startImageCaptureActivity() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        long now = System.currentTimeMillis();
        String photoId = String.format("%d.%03d", now/1000, now%1000);
        String fileName = photoId+".jpg";
        pathTmpImage = imgCachePath + "/" + fileName;
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://" + pathTmpImage));
        startActivityForResult(intent, 20000);
    }

    private void startImageLocalPickActivity(){
        Intent intent = new Intent();
        intent.setClass(this, LocalPhotoActivity.class);
        if (albumName != null && albumName.length() > 0){
            intent.putExtra("albumName", albumName);
        }
        startActivityForResult(intent, 20001);
        overridePendingTransition(R.anim.photo_picker_activity_open, R.anim.activity_stay);
    }
    
    @Override
    public void onPause(){
        super.onPause();
        // 清理bitmap资源;
        if (initialImage != null && !initialImage.isRecycled()){
            initialImage.recycle();
            initialImage = null;
        }
        if (croppedImage != null && !croppedImage.isRecycled()){
            croppedImage.recycle();
            croppedImage = null;
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        // 清理bitmap资源;
        if (initialImage != null && !initialImage.isRecycled()){
            initialImage.recycle();
            initialImage = null;
        }
        if (croppedImage != null && !croppedImage.isRecycled()){
            croppedImage.recycle();
            croppedImage = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED){
            // 删除拍照的原图;
            if (pathPhotoTaked != null){
                new File(pathPhotoTaked).delete();
            }
            
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        // 根据上面发送过去的请求码来区别
        switch (requestCode) {
            case 20000: // take photo;
                pathPhotoTaked = pathTmpImage;
                break;
            case 20001: // local photo;
                albumName = data.getStringExtra("albumName");
                pathTmpImage = data.getStringExtra("photoPath");
                break;
            default:
                break;
        }
        tvUseThisCropperPhoto.setEnabled(false);
        new ExtractThumbTask(true, pathTmpImage, 1536, 2048).execute();
    }

    /*
     * 生成缩略图异步Task;
     * @param imageView for scrollView;
     */
    public class ExtractThumbTask extends AsyncTask<Object, Void, String>{
        String srcImgPath=null, dstImgPath=null;
        int reqWidth=1536, reqHeight=2048;
        boolean showProgressDialog = true;
        Bitmap bmp = null;

        // 传入的宽是小于高的;
        public ExtractThumbTask(boolean showProgressDialog, String srcImgPath, int width, int height){
            this.showProgressDialog = showProgressDialog;
            this.reqWidth = width;
            this.reqHeight = height;
            this.srcImgPath = srcImgPath;
        }
        protected void onPreExecute() {
            // 启动一个转圈等待进度对话框;
            if (showProgressDialog){
                progressDialog = new AlertDialog.Builder(CropperActivity.this)
                    .setCancelable(false)
                    .create();
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
            }
        }
        
        @Override
        protected String doInBackground(Object... params) {
            long now = System.currentTimeMillis();
            String photoId = String.format("%d.%03d", now/1000, now%1000);
            String dstFileName = photoId+".jpg";
            dstImgPath = imgCachePath + "/" + dstFileName;
            BitmapFactory.Options options = new BitmapFactory.Options();  
            options.inJustDecodeBounds = true; 
            // 获取这个图片的宽和高，注意此处的bitmap为null  
            BitmapFactory.decodeFile(srcImgPath, options);
            int w = options.outWidth; // 图像实际宽度;
            int h = options.outHeight; // 图像实际高度;
            int newW = w, newH = h; // 需要转换的新尺寸;
            float ratio = 1;
            if (w <= h){ // 纵向图片;
                ratio = Math.max((float)w/reqWidth, (float)h/reqHeight);
            }
            else{ // 横向图片;
                ratio = Math.max((float)h/reqWidth, (float)w/reqHeight);
            }
            
            if (ratio < 1.0f){ // 图像比缩放请求的要小;用源图;
                // Calculate inSampleSize
                ratio = 1;
            }
            newW = (int) (w / ratio); newH = (int) (h / ratio);
            
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            options.inSampleSize = 1;
            // 读出bmp;
            bmp = BitmapFactory.decodeFile(srcImgPath, options);
            if (ratio > 1.0f){ // 只有图像比请求缩放的宽高要大的时候才需要进行缩放;
                bmp = ImageUtil.zoomBitmap(bmp, newW, newH); // 缩放;
            }
            /*
            try {
                FileOutputStream out = new FileOutputStream(dstImgPath);
                SLog.d(TAG, "try to compress file : " + dstImgPath);
                // 按原有质量的60%压缩;
                if (bmp.compress(Bitmap.CompressFormat.JPEG, 60, out)){
                    out.flush();
                }
                else{
                    ImageUtil.SaveBitmap(bmp, dstImgPath);
                    SLog.d(TAG, "try to save file : " + dstImgPath);
                }
                out.close();
            } 
            catch (FileNotFoundException e) { // for FileOutputStream;
                e.printStackTrace();
            }
            catch (IOException e) { // for out.flush & out.close;
                e.printStackTrace();
            }
            */
//            bmp.recycle();
            
            //图片旋转处理;
            /*
            int rotate = ImageUtil.getPicDegree(dstImgPath); // 获取旋转角度;
            if (rotate == 0 && options.outWidth > options.outHeight){ // 未旋转但是宽大于高也转一下;
                // 宽大于高,旋转90度;
                rotate = 90;
            }
            if (rotate != 0){
                Bitmap rotateBmp = ImageUtil.postRotateBitamp(bmp, rotate);
                bmp.recycle();
                bmp = rotateBmp;
            }
            */
            return dstImgPath; 
        }

        @Override
        protected void onPostExecute(String thumbFullPath) {
            if (showProgressDialog){
                progressDialog.dismiss();
            }
            tvUseThisCropperPhoto.setEnabled(true);
            civCropperPreview.setImageBitmap(bmp);
            initialImage = bmp;
        }
    }
}
