package com.neoba.syncpad;

import java.util.List;

import com.neoba.syncpad.ByteMessenger.Share;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

public class ShareAdapter extends ArrayAdapter<Share>{

	public ShareAdapter(Context context, int textViewResourceId,List<Share> objects) {
		super(context, textViewResourceId, objects);
		this.layoutResourceId=textViewResourceId;
		this.context=context;
		this.items=objects;
	}
	private List<Share> items;
	private int layoutResourceId;
	private Context context;
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ShareHolder holder = null;

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		row = inflater.inflate(layoutResourceId, parent, false);
		Button remove=(Button)row.findViewById(R.id.bSLERemove);
		
		remove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				items.remove(position);
				notifyDataSetChanged();
				
			}
		});
		
		holder = new ShareHolder();
		holder.share = items.get(position);
		holder.username = (TextView)row.findViewById(R.id.tvsleUname);
		holder.sw=(Switch)row.findViewById(R.id.swRW);
		row.setTag(holder);
		holder.username.setText(holder.share.username);
		return row;
		
		
	}
	public static class ShareHolder{
		Share share;
		TextView username;
		Switch sw;
	}
}
