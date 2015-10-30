package com.jx.tw319qrc.ui;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jx.tw319qrc.R;
import com.jx.tw319qrc.data.TW319LocationItem;

public class GridViewLocationAdapter extends BaseAdapter {

	private Context mContext;
	private OnClickListener mOnClickListener;
	private ArrayList<TW319LocationItem> mLocationItems;
	private int mResIdOfItemBackground = -1;

	public GridViewLocationAdapter(Context context,
			OnClickListener onClickListener, int resIdOfItemBackground,
			ArrayList<TW319LocationItem> locationItems) {
		mContext = context;
		mOnClickListener = onClickListener;
		mLocationItems = locationItems;
		mResIdOfItemBackground = resIdOfItemBackground;
	}

	@Override
	public int getCount() {
		return mLocationItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mLocationItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		TW319LocationItem item = (TW319LocationItem) getItem(position);
		if (null == convertView) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = new View(mContext);
			view = inflater.inflate(R.layout.view_location, parent, false);
		} else {
			view = convertView;
		}
		view.setTag(item);
		TextView textView = (TextView) view.findViewById(R.id.textViewLocation);
		textView.setText(item.getName());
		textView.setBackgroundResource(mResIdOfItemBackground);
		ImageView imageViewLocationGreen = (ImageView) view.findViewById(R.id.imageViewLocation);
		int imageVisible = item.isDataCached() ? View.VISIBLE : View.GONE;
		imageViewLocationGreen.setVisibility(imageVisible);

		if (mOnClickListener != null)
			view.setOnClickListener(mOnClickListener);
		return view;
	}

}
