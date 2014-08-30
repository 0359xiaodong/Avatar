package com.syw.avatar.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore.Images;

public class ImageUtil {

    public static String md5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);

        for (byte b : hash) {
            int i = (b & 0xFF);
            if (i < 0x10) hex.append('0');
            hex.append(Integer.toHexString(i));
        }

        return hex.toString();
    }
    
    /** 
     * 根据指定的图像路径和大小来获取缩略图 
     * 此方法有两点好处： 
     *     1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度， 
     *        第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 
     *     2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 
     *        用这个工具生成的图像不会被拉伸。 
     * @param fileName 图像的路径 
     * @param width 指定输出图像的宽度 
     * @param height 指定输出图像的高度 
     * @return 生成的缩略图 
     */  
    public static boolean generateThumb(String pathImage, String thumbPath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true; 
        // 获取这个图片的宽和高，注意此处的bitmap为null  
        BitmapFactory.decodeFile(pathImage, options);
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
        Bitmap bmp = BitmapFactory.decodeFile(pathImage, options);
        if (ratio > 1.0f){ // 只有图像比请求缩放的宽高要大的时候才需要进行缩放;
            bmp = ImageUtil.zoomBitmap(bmp, newW, newH); // 缩放;
        }
        
        try {
            FileOutputStream out = new FileOutputStream(thumbPath);
            // 按原有质量的60%压缩;
            if (bmp.compress(Bitmap.CompressFormat.JPEG, 60, out)){
                out.flush();
            }
            else{
                ImageUtil.SaveBitmap(bmp, thumbPath);
            }
            out.close();
        } 
        catch (FileNotFoundException e) { // for FileOutputStream;
            e.printStackTrace();
        }
        catch (IOException e) { // for out.flush & out.close;
            e.printStackTrace();
        }
        bmp.recycle();
        return true;
    }

    public static boolean generateThumb(Bitmap bitmap, String thumbPath, int reqWidth, int reqHeight) {
        int w = bitmap.getWidth(); // 图像实际宽度;
        int h = bitmap.getHeight(); // 图像实际高度;
        int newW = w, newH = h; // 需要转换的新尺寸;
        float ratio = 1;
        Bitmap bmp = bitmap;
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
        
        if (ratio > 1.0f){ // 只有图像比请求缩放的宽高要大的时候才需要进行缩放;
            bmp = ImageUtil.zoomBitmap(bitmap, newW, newH); // 缩放;
        }
        
        try {
            FileOutputStream out = new FileOutputStream(thumbPath);
            // 按原有质量的60%压缩;
            if (bmp.compress(Bitmap.CompressFormat.JPEG, 60, out)){
                out.flush();
            }
            else{
                ImageUtil.SaveBitmap(bmp, thumbPath);
            }
            out.close();
        } 
        catch (FileNotFoundException e) { // for FileOutputStream;
            e.printStackTrace();
        }
        catch (IOException e) { // for out.flush & out.close;
            e.printStackTrace();
        }
        bmp.recycle();
        return true;
    }

    public static String getImageAbsPathFromId(Context context, long imageId) {
        final Uri uriImages = Images.Media.EXTERNAL_CONTENT_URI;
        final ContentResolver cr = context.getContentResolver();
        String where = Images.ImageColumns._ID + "=" + imageId; 
        final String[] PROJECTION_IMAGES = new String[] { Images.ImageColumns.DATA};
        String filePath = null;
        // 根据ImageId查询文件绝对路径;
        final Cursor cursorImages = cr.query(uriImages, PROJECTION_IMAGES, where, null, null);
        try{
            if (cursorImages != null && cursorImages.moveToFirst()) {
                filePath = cursorImages.getString(0);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            cursorImages.close();
        }
        return filePath;
    }

    /** 
     * 读取图片属性：旋转的角度 
     * @param path 图片绝对路径 
     * @return degree旋转的角度 
     */  
       public static int getPicDegree(String path) {  
           int degree  = 0;  
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
           }
           catch (IOException e) {  
                   e.printStackTrace();  
           }  
           return degree;  
       }  
       
       public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidht = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidht, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newbmp;
    }
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;

    }

    public static String SaveBitmap(Bitmap bmp, String path, String name) {
        // File file = new File("mnt/sdcard/picture");
        File file = new File(path);
        String fullPath = null;
        if (!file.exists()){
            file.mkdirs();
        }
        fullPath = file.getPath() + "/" + name;
        if (new File(path+name).exists()){
            return fullPath;
        }
        
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fullPath);

            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return fullPath;
    }

    public static String SaveBitmap(Bitmap bmp, String fullPath) {
        int lastIndex = fullPath.lastIndexOf('/');
        if (lastIndex == -1){
            return null;
        }
        String path = fullPath.substring(0, lastIndex);
        String name = fullPath.substring(lastIndex+1);
        SaveBitmap(bmp, path, name);
        return fullPath;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
        final int reflectionGap = 4;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2, width, height / 2, matrix, false);

        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint deafalutPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);

        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff,
                TileMode.CLAMP);
        paint.setShader(shader);
        // Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

        return bitmapWithReflection;
    }

    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public static Bitmap readBitMap(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Config.ARGB_8888;
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        return bm;
    }

    public static Bitmap postRotateBitamp(Bitmap bmp, float degree) {
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
        return resizeBmp;
    }

    public static Bitmap reverseBitmap(Bitmap bmp, int flag) {
        float[] floats = null;
        switch (flag) {
            case 0: 
                floats = new float[] { -1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f };
                break;
            case 1: 
                floats = new float[] { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
                break;
        }

        if (floats != null) {
            Matrix matrix = new Matrix();
            matrix.setValues(floats);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        }

        return bmp;
    }

    public static Bitmap doodle(Bitmap src, Bitmap watermark, int x, int y) {
        Bitmap newb = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(newb);
        canvas.drawBitmap(src, 0, 0, null);
        canvas.drawBitmap(watermark, (src.getWidth() - watermark.getWidth()) / 2, (src.getHeight() - watermark.getHeight()) / 2, null); 
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        watermark.recycle();
        watermark = null;
        return newb;
    }

    public static Bitmap drawText(Bitmap src, String msg, int x, int y) {
        Canvas canvas = new Canvas(src);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawText(msg, x, y, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return src;
    }

    public static Bitmap emboss(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        int pixR = 0;
        int pixG = 0;
        int pixB = 0;

        int pixColor = 0;

        int newR = 0;
        int newG = 0;
        int newB = 0;

        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int pos = 0;
        for (int i = 1, length = height - 1; i < length; i++) {
            for (int k = 1, len = width - 1; k < len; k++) {
                pos = i * width + k;
                pixColor = pixels[pos];

                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);

                pixColor = pixels[pos + 1];
                newR = Color.red(pixColor) - pixR + 127;
                newG = Color.green(pixColor) - pixG + 127;
                newB = Color.blue(pixColor) - pixB + 127;

                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                pixels[pos] = Color.argb(255, newR, newG, newB);
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap film(Bitmap bmp) {
        final int MAX_VALUE = 255;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        int pixR = 0;
        int pixG = 0;
        int pixB = 0;

        int pixColor = 0;

        int newR = 0;
        int newG = 0;
        int newB = 0;

        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int pos = 0;
        for (int i = 1, length = height - 1; i < length; i++) {
            for (int k = 1, len = width - 1; k < len; k++) {
                pos = i * width + k;
                pixColor = pixels[pos];

                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);

                newR = MAX_VALUE - pixR;
                newG = MAX_VALUE - pixG;
                newB = MAX_VALUE - pixB;

                newR = Math.min(MAX_VALUE, Math.max(0, newR));
                newG = Math.min(MAX_VALUE, Math.max(0, newG));
                newB = Math.min(MAX_VALUE, Math.max(0, newB));

                pixels[pos] = Color.argb(MAX_VALUE, newR, newG, newB);
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap sunshine(Bitmap bmp, int centerX, int centerY) {
        final int width = bmp.getWidth();
        final int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        int pixR = 0;
        int pixG = 0;
        int pixB = 0;

        int pixColor = 0;

        int newR = 0;
        int newG = 0;
        int newB = 0;
        int radius = Math.min(centerX, centerY);

        final float strength = 150F; 
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int pos = 0;
        for (int i = 1, length = height - 1; i < length; i++) {
            for (int k = 1, len = width - 1; k < len; k++) {
                pos = i * width + k;
                pixColor = pixels[pos];

                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);

                newR = pixR;
                newG = pixG;
                newB = pixB;

                int distance = (int) (Math.pow((centerY - i), 2) + Math.pow(centerX - k, 2));
                if (distance < radius * radius) {
                    int result = (int) (strength * (1.0 - Math.sqrt(distance) / radius));
                    newR = pixR + result;
                    newG = pixG + result;
                    newB = pixB + result;
                }

                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                pixels[pos] = Color.argb(255, newR, newG, newB);
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

}
