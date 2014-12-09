package com.neoba.syncpad;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.PorterDuff;
public class ColorListAdapter extends ArrayAdapter<String> {

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.color_picker_element, parent,
				false);
		TextView tv = (TextView) rowView.findViewById(R.id.tvColorName);
		tv.setText(values.get(position).split("~")[0]);
		ImageView iv=(ImageView)rowView.findViewById(R.id.ivColorPreview);
		GradientDrawable bgShape = (GradientDrawable)context.getResources().getDrawable(R.drawable.circle_color_preview); 
		bgShape.setColor(Color.parseColor(values.get(position).split("~")[1]));
		//bgShape.setColor(Color.GREEN);
		//Log.d("Colorss",Integer.toHexString(Color.GREEN)+"("+Integer.parseInt(values.get(position).split("~")[1], 16)+")"+Integer.toHexString(Integer.parseInt(values.get(position).split("~")[1], 16)));
		iv.setImageDrawable(bgShape);
		return rowView;
	}
	

	private final Context context;
	private final List<String> values;
	int[] cols;
	public ColorListAdapter(Context context,  List<String> values) {
		super(context, R.layout.color_picker_element, values);
		this.context = context;
		this.values = values;
	}

}