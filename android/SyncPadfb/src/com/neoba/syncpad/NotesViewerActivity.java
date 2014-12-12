package com.neoba.syncpad;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import com.neoba.syncpad.ByteMessenger.document;
import com.ocpsoft.pretty.time.PrettyTime;

import net.dongliu.vcdiff.exception.VcdiffDecodeException;

import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.os.Build;
import android.preference.PreferenceManager;

public class NotesViewerActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notes_viewer);
		String id = getIntent().getExtras().getString("uuid");
		int synced=0;
		DBManager db = new DBManager(NotesViewerActivity.this);
		db.open();

			Cursor c=db.getDocumentForViewer(id);
			final document d =new document(c.getString(1), c.getString(5), c.getBlob(2),
					c.getInt(4), c.getString(3), (byte) c.getInt(6),
					c.getInt(8) == 1 ? true : false, c.getInt(9),c.getString(12));
			Long ltime=c.getLong(7);
		
		if (!db.isDocUnSynced(id)) {
			Log.d("NOTELIST", "Unsynced " + id);
			d.synced = 2;
		} else
			d.synced = 0;
		ArrayList<String > shares=db.getShares(d.id);
		db.close();
		TextView tv = (TextView) findViewById(R.id.tvNotesViewText);

		float size = getIntent().getExtras().getFloat("size");
		if (!d.title.equals("")) {
			((RelativeLayout) findViewById(R.id.rlNoteViewer))
					.setBackgroundColor(Color.parseColor(d.title.split("\n")[0]));
			Window s=getWindow();
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
				s.setStatusBarColor(Color.parseColor(d.title.split("\n")[0]));
			String html=d.title;
			html = html.replaceAll(Pattern.quote("[ ]"), "\uE000");
			html = html.replaceAll(Pattern.quote("[*]"), "\uE001");
			final SpannableNote notesp = new NeoHTML(html, NotesViewerActivity.this)
					.getNote();
			final SpannableStringBuilder ss = notesp.getcontent();
			Typeface font2 = Typeface.createFromAsset(
					NotesViewerActivity.this.getAssets(), "fonts/checkfont.ttf");
			ss.setSpan(new CustomTypefaceSpan("", font2), 0, ss.length(),
					Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
			tv.setText(ss);
			
			TextView shareinfo=(TextView)findViewById(R.id.tvSharedUsers);
			Log.d("SHARES",shares+"");
			if(d.owns && shares.size()==0)
				shareinfo.setVisibility(View.GONE);
			else if(d.owns && shares.size()>0)
			{
				String sharestring="Shared with ";
				if(shares.size()==1)
					sharestring+="@"+shares.get(0).split("~")[0];
				else if(shares.size()==2)
					sharestring+=("@"+shares.get(0).split("~")[0]+" and @"+shares.get(1).split("~")[0]);
				else if(shares.size()==3)
					sharestring+=("@"+shares.get(0).split("~")[0]+", @"+shares.get(1).split("~")[0]+" and 1 other");
				else
					sharestring+=("@"+shares.get(0).split("~")[0]+", @"+shares.get(1).split("~")[0]+" and "+(shares.size()-2)+" others");
				shareinfo.setText(sharestring);
			}
			else if(!d.owns)
				shareinfo.setText("Created by @"+d.owner.split("~")[0]);
			
		} else {
			((RelativeLayout) findViewById(R.id.rlNoteViewer))
					.setBackgroundColor(Color.parseColor("#FFFFFF"));
		}
		
		tv.setTextSize(size);
		tv.setMovementMethod(new ScrollingMovementMethod());
		
		
		ImageButton editb = (ImageButton) 
				findViewById(R.id.bNotesListLeftv);
		ImageButton shareb = (ImageButton) 
				findViewById(R.id.bNotesListRightv);
		if(!d.owns)
		{
			editb.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 10f));
			shareb.setVisibility(View.GONE);
		}
		if(d.permission!=2)
			editb.setVisibility(View.GONE);


		shareb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				
				Intent i = new Intent(NotesViewerActivity.this,ShareListActivity.class);
				i.putExtra("docid", d.id);
				startActivity(i);

			}
		});

		editb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(NotesViewerActivity.this, NotesEditor.class);
				i.putExtra("uuid", d.id);
				startActivity(i);

			}
		});
		String color;
		if (d.title.length() == 0)
			color = "#FFFFFF";
		else
			color = d.title.split("\n")[0].charAt(0) == '#' ? d.title.split("\n")[0] : "#FFFFFF";
	((RelativeLayout) findViewById(R.id.rlNoteViewer))
			.setBackgroundColor(Color.parseColor(color));
	((TextView)findViewById(R.id.tvTimeStamp)).setText(new PrettyTime().format(new Date(ltime)));
		
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
