package com.jx.tw319qrc.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jx.tw319qrc.R;
import com.jx.tw319qrc.data.TW319LocationItem;
import com.jx.tw319qrc.data.TW319StoreItem;

public class ListViewVillageAdapter extends BaseAdapter {

	private Context mContext;
	private OnClickListener mOnClickListener;
	private ArrayList<TW319LocationItem> mStoreItems;

	public ListViewVillageAdapter(Context context,
			OnClickListener onClickListener,
			ArrayList<TW319LocationItem> locationItems) {
		mContext = context;
		mOnClickListener = onClickListener;
		mStoreItems = locationItems;
	}

	@Override
	public int getCount() {
		return mStoreItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mStoreItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		TW319StoreItem item = (TW319StoreItem) getItem(position);
		if (null == convertView) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = new View(mContext);
			view = inflater.inflate(R.layout.view_store, null);
		} else {
			view = convertView;
		}
		view.setTag(item);
		TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
		textViewName.setText(item.getName());
		TextView textViewAddress = (TextView) view
				.findViewById(R.id.textViewAddress);
		textViewAddress.setText(item.getAddress());
		ImageView imageViewIcon = (ImageView) view
				.findViewById(R.id.imageViewIcon);
		Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(),
				item.getIconId());
		imageViewIcon.setImageBitmap(bm);
		if (mOnClickListener != null)
			view.setOnClickListener(mOnClickListener);
		return view;
	}
}
