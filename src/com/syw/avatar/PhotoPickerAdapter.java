package com.syw.avatar;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.syw.avatar.util.ThumbnailsUtil;

public class PhotoPickerAdapter extends BaseAdapter{
//    private static final String TAG = PhotoPickerAdapter.class.getSimpleName();
            
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    
    private Context mContext;
	private List<PhotoInfo> dataList;

	public PhotoPickerAdapter(Context c, List<PhotoInfo> dataList) {

		mContext = c;
		this.dataList = dataList;
	}
	
	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 存放列表项控件句柄
	 */
	private class ViewHolder {
		public ImageView imageView;
		public ToggleButton tgButton;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item_img, parent, false);
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
			viewHolder.tgButton = (ToggleButton) convertView.findViewById(R.id.toggle_button);
		} 
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		PhotoInfo item = null;
		if (dataList != null && dataList.size() > position){
			item = dataList.get(position);
		}
		if (item == null) {
			viewHolder.imageView.setImageResource(R.drawable.defaultpic);
		}
		else {
		    String displayItemUri = ThumbnailsUtil.MapgetHashValue(item.getImageId(),item.getPathFile());
		    imageLoader.displayImage(displayItemUri, viewHolder.imageView);
		}
		
        viewHolder.tgButton.setVisibility(View.GONE);
        convertView.setTag(viewHolder);

        return convertView;
	}

}
