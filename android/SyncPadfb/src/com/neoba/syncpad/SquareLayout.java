package com.neoba.syncpad;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class SquareLayout extends RelativeLayout {

	public SquareLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public SquareLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
            // or you can use this if you want the square to use height as it basis
            // super.onMeasure(heightMeasureSpec, heightMeasureSpec); 
    }
}