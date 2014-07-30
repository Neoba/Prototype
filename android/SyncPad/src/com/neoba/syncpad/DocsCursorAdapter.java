package com.neoba.syncpad;

import java.util.Date;

import com.ocpsoft.pretty.time.PrettyTime;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class DocsCursorAdapter extends SimpleCursorAdapter {

	Context context;
	int layout;
	@SuppressWarnings("deprecation")
	public DocsCursorAdapter(Context context, int layout, Cursor c,String[] from, int[] to) {
		super(context, layout, c, from, to);
        this.context = context;
        this.layout = layout;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void bindView(View v, Context context, Cursor c) {
		// TODO Auto-generated method stub
		super.bindView(v, context, c);
		PrettyTime p = new PrettyTime();
		Date d=	new Date (c.getLong(c.getColumnIndex("date")));
        String name = p.format(d);
  

        TextView name_text = (TextView) v.findViewById(R.id.number_entry);
        if (name_text != null) {
            name_text.setText(name);
        }
	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Cursor c = getCursor();
		 
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layout, parent, false);
        String name = c.getString(c.getColumnIndex("title"));
 
        /**
         * Next set the name of the entry.
         */    
        TextView name_text = (TextView) v.findViewById(R.id.title_entry);
        if (name_text != null) {
            name_text.setText("#"+name);
        }
 
        return v;
	}



}
