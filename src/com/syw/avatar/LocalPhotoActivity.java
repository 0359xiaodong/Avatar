package com.syw.avatar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.syw.avatar.AlbumFragment.OnAlbumClickedListener;
import com.syw.avatar.PhotoPickerFragment.OnPhotoSelectClickListener;
import com.syw.avatar.util.ThumbnailsUtil;

public class LocalPhotoActivity extends FragmentActivity implements OnAlbumClickedListener
				,OnPhotoSelectClickListener{
    @SuppressWarnings("unused")
    private static final String TAG = LocalPhotoActivity.class.getSimpleName();
    
    private TextView titleTextView;
    private TextView tvLeftArrowBtn;
    private TextView tvRightCancelBtn;

    private AlbumFragment photoFolderFragment;
	private PhotoPickerFragment photoPickerFragment;
	private Fragment currentFragment;
	
	private FragmentManager manager;

    private List<AlbumInfo> listImageInfo = new ArrayList<AlbumInfo>();
    private ContentResolver cr; 
    
    private String albumName = null;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_localphoto);
		
        titleTextView = (TextView) findViewById(R.id.tvTitleName);
        titleTextView.setText("选择相册");
        tvLeftArrowBtn = (TextView) findViewById(R.id.tvTitleArrowBtnLeft);
        tvLeftArrowBtn.setText("相册");
        tvLeftArrowBtn.setVisibility(View.GONE);
        tvLeftArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 左边 "< 相册"按钮, 点击后转向相册fragment;
                showPhotoAlbumFragment();
            }
        });
        tvRightCancelBtn = (TextView) findViewById(R.id.tvTitleBtnRightButton);
        tvRightCancelBtn.setText(getString(R.string.cancel));
        tvRightCancelBtn.setVisibility(View.VISIBLE);
        tvRightCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                LocalPhotoActivity.this.finish();
            }
        });

		manager = getSupportFragmentManager();
		
        photoFolderFragment = new AlbumFragment();
		photoPickerFragment = new PhotoPickerFragment();

        cr = getContentResolver(); 
        listImageInfo.clear();
        
        albumName = getIntent().getStringExtra("albumName");
        
        new ImageLoadAsyncTask().execute();

	}
	
	// 在相册选择界面选择了某个相册后回调该方法 ;
	@Override
	public void onAlbumClickedListener(String albumName, List<PhotoInfo> list) {
        // 替换当前的Fragment;
		showPhotoPickerFragment(albumName, list);
	}

	// 显示某个相册的照片列表Fragment;
    private void showPhotoPickerFragment(String albumName, List<PhotoInfo> list) {
        // 显示左边按钮;
        tvLeftArrowBtn.setVisibility(View.VISIBLE);
        titleTextView.setText(albumName);

        FragmentTransaction transaction = manager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        // 替换当前的Fragment;
        if (!photoPickerFragment.isAdded()) {    // 先判断是否被add过,如果没有add过,直接add这个fragment;
            Bundle args = new Bundle();
            // 这个每次进入前都要把文件夹照片列表发送给photopickfragment;
            PhotoSerializable photoSerializable = new PhotoSerializable();
            photoSerializable.setList(list);
            args.putSerializable("list", photoSerializable);
            photoPickerFragment.setArguments(args);
            transaction.hide(photoFolderFragment).add(R.id.fragment_container, photoPickerFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
        }
        else { // 如果已经add过,隐藏这个fragment,并且显示下一个fragment;
            photoPickerFragment.updateDataList(list);
            transaction.hide(photoFolderFragment).show(photoPickerFragment).commit(); // 隐藏当前的fragment，显示下一个
        }
        currentFragment = photoPickerFragment;
    }

    // 显示相册列表界面;
    private void showPhotoAlbumFragment() {
        // 隐藏左边按钮;
        tvLeftArrowBtn.setVisibility(View.GONE);
        titleTextView.setText("选择相册");
        
        FragmentTransaction transaction = manager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (!photoFolderFragment.isAdded()){
            transaction.hide(photoPickerFragment).add(R.id.fragment_container, photoFolderFragment).commit();
        }
        else{
            transaction.hide(photoPickerFragment).show( photoFolderFragment).commit();
        }
        currentFragment = photoFolderFragment;
    }

    /*
     * 在照片选择界面选择了照片集后回调该方法;
     * fragment将已经选择的照片列表回传;
     */
    @Override
    public void onOKClickListener(PhotoInfo selectedPhoto) {
        Intent data = new Intent();
        data.putExtra("photoPath", selectedPhoto.getPathAbsolute());
        data.putExtra("albumName", titleTextView.getText()); // 把选中的album名字也带回去;
        setResult(RESULT_OK, data);
        finish();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED){
            // 如果相册选择被caceled,那么结束自己直接回到主activity;
            finish();
            return;
        }
        // 根据上面发送过去的请求码来区别
        switch (requestCode) {
            case 50001:
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // 如果当前是选择相册界面按了返回键,直接退出activity;
        if (currentFragment == photoFolderFragment){
            setResult(RESULT_CANCELED);
            finish();
        }
        else{
            // 如果是选择照片界面按了返回键,退回到相册选择界面; 替换当前的Fragment;
            showPhotoAlbumFragment();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.activity_stay, R.anim.photo_picker_activity_close);  
    }

    private class ImageLoadAsyncTask extends AsyncTask<Void, Void, Object>{
        @Override
        protected Object doInBackground(Void... params) {
            //获取缩略图
            ThumbnailsUtil.clear();
            String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID, Thumbnails.DATA };
            Cursor cur = cr.query(Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);

            if (cur!=null&&cur.moveToFirst()) {
                int image_id;
                String image_path;
                int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
                int dataColumn = cur.getColumnIndex(Thumbnails.DATA);
                do {
                    image_id = cur.getInt(image_idColumn);
                    image_path = cur.getString(dataColumn);
                    ThumbnailsUtil.put(image_id, "file://"+image_path);
                } while (cur.moveToNext());
            }

            cur.close();
            //获取原图
            Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, "date_modified DESC");  

            String _path="_data";
            String _album="bucket_display_name";

            HashMap<String,AlbumInfo> myhash = new HashMap<String, AlbumInfo>();
            AlbumInfo albumInfo = null;
            PhotoInfo photo = null;
            if (cursor!=null&&cursor.moveToFirst())
            {
                do{
                    int index = 0;
                    int _id = cursor.getInt(cursor.getColumnIndex("_id")); 
                    String path = cursor.getString(cursor.getColumnIndex(_path));
                    String album = cursor.getString(cursor.getColumnIndex(_album));  
                    List<PhotoInfo> stringList = new ArrayList<PhotoInfo>();
                    photo = new PhotoInfo();
                    if(myhash.containsKey(album)){
                        albumInfo = myhash.remove(album);
                        if(listImageInfo.contains(albumInfo))
                            index = listImageInfo.indexOf(albumInfo);
                        photo.setImageId(_id);
                        photo.setPathFile("file://"+path);
                        photo.setPathAbsolute(path);
                        albumInfo.getList().add(photo);
                        listImageInfo.set(index, albumInfo);
                        myhash.put(album, albumInfo);
                    }else{
                        albumInfo = new AlbumInfo();
                        stringList.clear();
                        photo.setImageId(_id);
                        photo.setPathFile("file://"+path);
                        photo.setPathAbsolute(path);
                        stringList.add(photo);
                        albumInfo.setImage_id(_id);
                        albumInfo.setPath_file("file://"+path);
                        albumInfo.setPath_absolute(path);
                        albumInfo.setName_album(album);
                        albumInfo.setList(stringList);
                        listImageInfo.add(albumInfo);
                        myhash.put(album, albumInfo);
                    }
                }while (cursor.moveToNext());
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            List<PhotoInfo> listInit = null;
            if (albumName != null && albumName.length() > 0){
                for (AlbumInfo ai : listImageInfo){
                    if (ai.getName_album().equals(albumName)){
                        listInit = ai.getList();
                        break;
                    }
                }
            }
            Bundle args = new Bundle();
            AlbumSerializable photoSerializable = new AlbumSerializable();
            photoSerializable.setList(listImageInfo);
            args.putSerializable("list", photoSerializable);
            photoFolderFragment.setArguments(args);
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.fragment_container, photoFolderFragment);
            transaction.commit(); 
            
            currentFragment = photoFolderFragment;
            if (listInit != null){
                showPhotoPickerFragment(albumName, listInit);
            }
        }
    } 
}
