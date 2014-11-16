package com.neoba.syncpad;


import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;

public class NotesViewerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notes_viewer);
		int id=getIntent().getExtras().getInt("uuid");
		DatabaseHandler db = new DatabaseHandler(this);
		String note=db.getNote(id).getNote();
		float size=getIntent().getExtras().getFloat("size");
		
		((RelativeLayout)findViewById(R.id.rlNoteViewer)).setBackgroundColor(Color.parseColor(note.split("\n")[0]));
		TextView tv=(TextView)findViewById(R.id.tvNotesViewText);
		tv.setText(new NeoHTML(note, NotesViewerActivity.this).getNote().getcontent());
		tv.setTextSize(size);
		tv.setMovementMethod(new ScrollingMovementMethod());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notes_viewer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
