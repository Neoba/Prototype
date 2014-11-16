package com.neoba.syncpad;

import android.text.SpannableStringBuilder;

public class SpannableNote {

	String color;
	SpannableStringBuilder content;

	public SpannableNote(SpannableStringBuilder SpannableStringBuilder, String color) {
		super();
		this.content = SpannableStringBuilder;
		this.color = color;
	}

	public SpannableStringBuilder getcontent() {
		return content;
	}

	public void setcontent(SpannableStringBuilder content) {
		this.content = content;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	
}
