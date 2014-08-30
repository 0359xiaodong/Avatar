package com.syw.avatar;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.syw.avatar.util.ThumbnailsUtil;

public class AlbumAdapter extends BaseAdapter{

//    private static final String TAG = AlbumListAdapter.class.getSimpleName();
            
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    
    private Context mContext;
	private List<AlbumInfo> dataList;

	public AlbumAdapter(Context c, List<AlbumInfo> dataList) {

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
		return 0;
	}

	/**
	 * 存放列表项控件句柄
	 */
	private class ViewHolder {
		public ImageView coverImageView;
		public TextView albumItemTitleView;
		public TextView albumItemCountView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_album, parent, false);
			viewHolder.coverImageView = (ImageView) convertView.findViewById(R.id.album_item_cover);
			viewHolder.albumItemTitleView = (TextView) convertView.findViewById(R.id.album_item_title);
			viewHolder.albumItemCountView = (TextView) convertView.findViewById(R.id.album_item_count);
			convertView.setTag(viewHolder);
		} 
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		final AlbumInfo item =  dataList.get(position);
		
		String displayItemUri =ThumbnailsUtil.MapgetHashValue(item.getImage_id(),item.getPath_file());
		imageLoader.displayImage(displayItemUri, viewHolder.coverImageView);

		viewHolder.albumItemTitleView.setText(item.getName_album());
		viewHolder.albumItemCountView.setText(""+item.getList().size());
		return convertView;
	}
}
