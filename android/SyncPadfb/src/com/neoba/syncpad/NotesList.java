package com.neoba.syncpad;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import com.neoba.syncpad.ByteMessenger.document;
import com.ocpsoft.pretty.time.PrettyTime;

import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NotesList extends ActionBarActivity {


	BroadcastReceiver receiver;
	private GCMReceiver receiver2;
	NoteListAdapter nl;
	GridView lv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notes_list);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        FloatingActionButton fabButton = new FloatingActionButton.Builder(this)
        .withDrawable(getResources().getDrawable(R.drawable.ic_add_white_24dp))
        .withButtonColor(getResources().getColor(R.color.pink_a200))
        .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
        .withMargins(0, 0, 16, 16)
        .create();
        fabButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				createNote();
				
			}
		});
		lv=(GridView)findViewById(R.id.lvNotesList);
		IntentFilter filter = new IntentFilter(GcmMessageHandler.ACTIONNOTELISTUPDATE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver2 = new GCMReceiver();
		registerReceiver(receiver2, filter);

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		lv.setDivider(null);
//		lv.setDividerHeight(0);

		DBManager db = new DBManager(NotesList.this);
		db.open();
		nl = new NoteListAdapter(NotesList.this,  db.getAllUndeletedDocs());
		lv.setAdapter(nl);
		db.close();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 10);
		Intent intent = new Intent(this, OfflineSyncService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// for 30 mint 60*60*1000
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				60 * 1000, pintent);
		startService(new Intent(getBaseContext(), OfflineSyncService.class));

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				DBManager db = new DBManager(NotesList.this);
				db.open();
				nl = new NoteListAdapter(NotesList.this,  db.getAllUndeletedDocs());
				lv.setAdapter(nl);
				db.close();

			}
		};
	}

	public class GCMReceiver extends BroadcastReceiver {


		@Override
		public void onReceive(Context context, Intent intent) {
			DBManager db = new DBManager(NotesList.this);
			db.open();
			nl = new NoteListAdapter(NotesList.this,  db.getAllUndeletedDocs());
			lv.setAdapter(nl);
			db.close();

		}

	}

	@Override
	public void onDestroy() {
		this.unregisterReceiver(receiver2);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.front, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
				new IntentFilter("com.neoba.syncpad.NotesList"));
	}

	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		super.onStop();
	}

	// this is
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_logout) {

			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						new Logout().execute();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						// No button clicked
						break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure?")
					.setPositiveButton("Yes", dialogClickListener)
					.setNegativeButton("No", dialogClickListener).show();
		}
		if (id == R.id.action_user) {
			Intent i = new Intent(NotesList.this, UserActivity.class);
			startActivity(i);
			return true;
		}
		if (id == R.id.action_debug) {
			Intent i = new Intent(NotesList.this, DebugActivity.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class CreateNote extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			return null;
		}

	}


	
	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onResume() {

		super.onResume();
		DBManager db = new DBManager(NotesList.this);
		db.open();
		nl = new NoteListAdapter(NotesList.this,  db.getAllUndeletedDocs());
		lv.setAdapter(nl);
		db.close();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		DBManager db = new DBManager(NotesList.this);
		db.open();
		nl = new NoteListAdapter(NotesList.this,  db.getAllUndeletedDocs());
		lv.setAdapter(nl);
		db.close();
	}

	class NoteParse extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			// TODO Auto-generated method stub
			final View cv = (View) params[0];
			String html = (String) params[1];
			html = html.replaceAll(Pattern.quote("[ ]"), "\uE000");
			html = html.replaceAll(Pattern.quote("[*]"), "\uE001");
			final SpannableNote note = new NeoHTML(html, NotesList.this)
					.getNote();
			final SpannableStringBuilder ss = note.getcontent();
			Typeface font2 = Typeface.createFromAsset(
					NotesList.this.getAssets(), "fonts/checkfont.ttf");
			ss.setSpan(new CustomTypefaceSpan("", font2), 0, ss.length(),
					Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
			final AutoResizeTextView aa = ((AutoResizeTextView) cv
					.findViewById(R.id.output_autofit));
			runOnUiThread(new Runnable() {
				public void run() {
					aa.setText(ss);
					ProgressBar pp = (ProgressBar) cv
							.findViewById(R.id.pnNoteLoader);
					pp.setVisibility(View.INVISIBLE);
				}
			});
			return null;
		}

	}

	public class Logout extends AsyncTask<Void, Void, Void> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(NotesList.this);
			this.dialog.setMessage("Logging out..");
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			boolean status = false;
			try {
				status = ByteMessenger.Logout(UUID.fromString(PreferenceManager
						.getDefaultSharedPreferences(NotesList.this).getString(
								"cookie", ":(")));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (status) {
				PreferenceManager.getDefaultSharedPreferences(NotesList.this)
						.edit().remove("cookie").commit();
				PreferenceManager.getDefaultSharedPreferences(NotesList.this)
						.edit().remove("loginlock").commit();
				DBManager db = new DBManager(NotesList.this);
				db.open();
				db.Truncate();
				db.close();

				NotesList.this.finish();
			} else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(),
								"Sorry.. Can't connect to our server :(",
								Toast.LENGTH_LONG).show();
					}
				});
			}
			return null;
		}

	}

public class NoteListAdapter extends CursorAdapter{

	public NoteListAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View rowView, Context arg1, Cursor c) {
		// TODO Auto-generated method stub
		final TextView textView = (TextView) rowView
				.findViewById(R.id.output_autofit);
		
		final document d =new document(c.getString(1), c.getString(5), c.getBlob(2),
				c.getInt(4), c.getString(3), (byte) c.getInt(6),
				c.getInt(8) == 1 ? true : false, c.getInt(9),c.getString(12));
		Long ltime=c.getLong(7);
		ArrayList<String > shares;
		((TextView)rowView.findViewById(R.id.tvTimeStamp)).setText(new PrettyTime().format(new Date(ltime)));
		//c.getLong(columnIndex)
		DBManager db = new DBManager(NotesList.this);
		db.open();
		if (!db.isDocUnSynced(d.id)) {
			Log.d("NOTELIST", "Unsynced " + d.id);
			d.synced = 2;
		} else
			d.synced = 0;
		shares=db.getShares(d.id);
		
			
		
		TextView shareinfo=(TextView)rowView.findViewById(R.id.tvSharedUsers);
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
		db.close();
		new NoteParse().execute(rowView, d.title);
		ImageButton editb = (ImageButton) rowView
				.findViewById(R.id.bNotesListLeft);
		ImageButton shareb = (ImageButton) rowView
				.findViewById(R.id.bNotesListRight);
		if(!d.owns)
		{
			editb.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 10f));
			shareb.setVisibility(View.GONE);
		}
		if(d.permission!=2)
			editb.setVisibility(View.GONE);
		if (d.synced == 2)
			((ImageView) rowView.findViewById(R.id.ivsynced))
					.setVisibility(View.VISIBLE);

		shareb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				
				Intent i = new Intent(NotesList.this,ShareListActivity.class);
				i.putExtra("docid", d.id);
				startActivity(i);

			}
		});

		editb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(NotesList.this, NotesEditor.class);
				i.putExtra("uuid", d.id);
				startActivity(i);

			}
		});
		String color;
		if (d.title.length() == 0)
			color = "#FFFFFF";
		else
			color = d.title.split("\n")[0].charAt(0) == '#' ? d.title.split("\n")[0] : "#FFFFFF";

		((SquareLayout) rowView.findViewById(R.id.squareLayout1))
				.setBackgroundColor(Color.parseColor(color));
		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Intent i = new Intent(NotesList.this,
						NotesViewerActivity.class);
				i.putExtra("uuid", d.id);
				i.putExtra("size", textView.getTextSize());
				startActivity(i);

			}
		});
		textView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				new AlertDialog.Builder(NotesList.this)
						.setTitle("Delete entry")
						.setMessage(
								"Are you sure you want to delete this entry?")
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										// continue with delete
										DBManager db = new DBManager(
												NotesList.this);
										db.open();

										db.setDeleted(d.id);
										nl = new NoteListAdapter(NotesList.this,  db.getAllUndeletedDocs());
										NotesList.this.lv.setAdapter(nl);
										db.close();
										startService(new Intent(
												NotesList.this,
												OfflineSyncService.class));
									}
								})
						.setNegativeButton(android.R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
										// do nothing
									}
								})

						.show();
				return false;
			}
		});
		ProgressBar s = (ProgressBar) rowView
				.findViewById(R.id.pnNoteLoader);

		s.setVisibility(View.VISIBLE);
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup parent) {
	      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
	        View retView = inflater.inflate(R.layout.notes_list_element, parent, false);
	 
	        return retView;
	}
	class NoteParse extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			// TODO Auto-generated method stub
			final View cv = (View) params[0];
			String html = (String) params[1];
			html = html.replaceAll(Pattern.quote("[ ]"), "\uE000");
			html = html.replaceAll(Pattern.quote("[*]"), "\uE001");
			final SpannableNote note = new NeoHTML(html, NotesList.this)
					.getNote();
			final SpannableStringBuilder ss = note.getcontent();
			Typeface font2 = Typeface.createFromAsset(
					NotesList.this.getAssets(), "fonts/checkfont.ttf");
			ss.setSpan(new CustomTypefaceSpan("", font2), 0, ss.length(),
					Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
			// final SpannableStringBuilder ss=new SpannableStringBuilder("a");
			// Typeface font =
			// Typeface.createFromAsset(NotesList.this.getAssets(),
			// "fonts/tickfont.ttf");
			// ss.setSpan (new CustomTypefaceSpan("", font),0,
			// 1,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
			// ss.append(s2);
			final AutoResizeTextView aa = ((AutoResizeTextView) cv
					.findViewById(R.id.output_autofit));
			runOnUiThread(new Runnable() {
				public void run() {
					aa.setText("");
					aa.setText(ss);
					ProgressBar pp = (ProgressBar) cv
							.findViewById(R.id.pnNoteLoader);
					pp.setVisibility(View.INVISIBLE);
				}
			});
			return null;
		}

	}
	
}
void createNote(){
	UUID newdoc = UUID.randomUUID();
	DBManager db = new DBManager(NotesList.this);
	db.open();
	try {
		db.insertDoc(new document(newdoc.toString(), "",
				new VcdiffEncoder("", "").encode(), -1, "", (byte) 2,
				true, Constants.UNSYNCED_CREATE));
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (VcdiffEncodeException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	db.close();

	startService(new Intent(NotesList.this, OfflineSyncService.class));

	Intent i = new Intent(NotesList.this, NotesEditor.class);
	i.putExtra("uuid", newdoc.toString() + "N");
	startActivity(i);
}
}
