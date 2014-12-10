package com.neoba.syncpad;
/**
 *               DO WHAT YOU WANT TO PUBLIC LICENSE
 *                    Version 2, December 2004
 * 
 * Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
 * 
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this license document, and changing it is allowed as long
 * as the name is changed.
 * 
 *            DO WHAT YOU WANT TO PUBLIC LICENSE
 *   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 * 
 *  0. You just DO WHAT YOU WANT TO.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;
public class AutoResizeTextView extends TextView {

public AutoResizeTextView(Context context) {
    super(context);
    init();
}

public AutoResizeTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
}

private void init() {

    maxTextSize = this.getTextSize();
    if (maxTextSize < 50) {
        maxTextSize = 45;
    }
    minTextSize = 20;
}

private void refitText(String text, int textWidth) {
    if (textWidth > 0) {
        int availableWidth = textWidth - this.getPaddingLeft()
                - this.getPaddingRight();
        float trySize = maxTextSize;

        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
        while ((trySize > minTextSize)
                && (this.getPaint().measureText(text) > availableWidth)) {
            trySize -= 1;
            if (trySize <= minTextSize) {
                trySize = minTextSize;
                break;
            }
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
        }
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
    }
}

@Override
protected void onTextChanged(final CharSequence text, final int start,
        final int before, final int after) {
    refitText(text.toString(), this.getWidth());
}

@Override
protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    if (w != oldw) {
        refitText(this.getText().toString(), w);
    }
}

@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
    refitText(this.getText().toString(), parentWidth);
}

public float getMinTextSize() {
    return minTextSize;
}

public void setMinTextSize(int minTextSize) {
    this.minTextSize = minTextSize;
}

public float getMaxTextSize() {
    return maxTextSize;
}

public void setMaxTextSize(int minTextSize) {
    this.maxTextSize = minTextSize;
}

private float minTextSize;
private float maxTextSize;

}