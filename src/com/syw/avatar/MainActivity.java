package com.syw.avatar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.syw.avatar.util.ImageUtil;
import com.syw.avatar.util.SLog;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private ImageView ivClipped;
        
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            rootView.findViewById(R.id.tvButtonLocalPhoto).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CropperActivity.class);
                    intent.putExtra("PickWay", "PICK");
                    startActivityForResult(intent, 10001);
                }
            });
            rootView.findViewById(R.id.tvButtonTakePhoto).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CropperActivity.class);
                    intent.putExtra("PickWay", "TAKE");
                    startActivityForResult(intent, 10002);
                }
            });
            ivClipped = (ImageView) rootView.findViewById(R.id.ivClipped);
            return rootView;
        }
        
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_CANCELED){
                SLog.d(TAG, "Canceled Avatar Capture.");
                return;
            }
            // 根据上面发送过去的请求码来区别
            switch (requestCode) {
                case 10001:
                case 10002:
                    final String path = data.getStringExtra("CropperPhotoPath");
                    SLog.d(TAG, "CropperPhotoPath = " + path);
                    if (path != null && path.length() > 0) {
                        Bitmap bmp = ImageUtil.readBitMap(path);
                        ivClipped.setImageBitmap(bmp);
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
