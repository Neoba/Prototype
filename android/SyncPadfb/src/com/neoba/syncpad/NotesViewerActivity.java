package com.neoba.syncpad;

import java.io.IOException;
import java.util.UUID;

import net.dongliu.vcdiff.exception.VcdiffDecodeException;

import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;
import android.preference.PreferenceManager;

public class NotesViewerActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notes_viewer);
		String id = getIntent().getExtras().getString("uuid");

		DBManager db = new DBManager(NotesViewerActivity.this);
		db.open();
		String note = null;
		try {
			note = db.getDoc(id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VcdiffDecodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.close();
		TextView tv = (TextView) findViewById(R.id.tvNotesViewText);

		float size = getIntent().getExtras().getFloat("size");
		if (!note.equals("")) {
			((RelativeLayout) findViewById(R.id.rlNoteViewer))
					.setBackgroundColor(Color.parseColor(note.split("\n")[0]));
			Window s=getWindow();
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
				s.setStatusBarColor(Color.parseColor(note.split("\n")[0]));
			tv.setText(new NeoHTML(note, NotesViewerActivity.this).getNote()
					.getcontent());
		} else {
			((RelativeLayout) findViewById(R.id.rlNoteViewer))
					.setBackgroundColor(Color.parseColor("#FFFFFF"));
		}

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
