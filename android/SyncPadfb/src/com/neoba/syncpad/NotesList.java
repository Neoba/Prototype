package com.neoba.syncpad;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import com.neoba.syncpad.ByteMessenger.document;
import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;
import android.app.AlarmManager;
import android.app.AlertDialog;
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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NotesList extends ListActivity {

	NotesListAdapter nla;
	BroadcastReceiver receiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    try {
	       ViewConfiguration config = ViewConfiguration.get(this);
	       Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	       if(menuKeyField != null) {
	           menuKeyField.setAccessible(true);
	           menuKeyField.setBoolean(config, false);
	       }
	   } catch (Exception e) {
	       e.printStackTrace();
	   }
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		DBManager db = new DBManager(NotesList.this);
		db.open();
		ArrayList<document> a = new ArrayList<ByteMessenger.document>();
		Cursor c = db.getAllUndeletedDocs();
		c.moveToFirst();
		while (!c.isAfterLast()) {
			document d = db.doccursorToDocument(c);
			if(!db.isDocUnSynced(d.id))
			{
				Log.d("NOTELIST", "Unsynced "+d.id);
				d.synced=2;
			}
			else d.synced=0;
			Log.d("NOTELIST", d.toString());
			a.add(d);
			c.moveToNext();
		}
		db.close();
		nla = new NotesListAdapter(this, R.layout.activity_front, a);
		this.setListAdapter(nla);
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
	    		ArrayList<document> a = new ArrayList<ByteMessenger.document>();
	    		Cursor c = db.getAllUndeletedDocs();
	    		c.moveToFirst();
	    		while (!c.isAfterLast()) {
	    			document d = db.doccursorToDocument(c);
	    			if(!db.isDocUnSynced(d.id))
	    			{
	    				Log.d("NOTELIST", "Unsynced "+d.id);
	    				d.synced=2;
	    			}
	    			else d.synced=0;
	    			Log.d("NOTELIST", d.toString());
	    			a.add(d);
	    			c.moveToNext();
	    		}
	    		db.close();
	    		nla = new NotesListAdapter(NotesList.this, R.layout.activity_front, a);
	    		NotesList.this.setListAdapter(nla);
	        }
	    };
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.front, menu);
		return true;
	}
	@Override
	protected void onStart() {
	    super.onStart();
	    LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter("com.neoba.syncpad.NotesList"));
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
		if (id == R.id.action_settings) {
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

			startService(new Intent(this, OfflineSyncService.class));

			Intent i = new Intent(NotesList.this, NotesEditor.class);
			i.putExtra("uuid", newdoc.toString() + "N");
			startActivity(i);
			return true;
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

	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		Toast.makeText(this,
				"Clicked " + getListAdapter().getItem(position).toString(),
				Toast.LENGTH_SHORT).show();
	}

	public class NotesListAdapter extends ArrayAdapter<document> {

		private final Context context;
		private final List<document> values;

		public NotesListAdapter(Context context, int resource,
				List<document> values) {
			super(context, resource, values);
			this.context = context;
			this.values = values;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.activity_front, parent,
					false);
			//ProgressBar pp = (ProgressBar) rowView
			//		.findViewById(R.id.pnNoteLoader);
			//pp.setVisibility(View.VISIBLE);
			final TextView textView = (TextView) rowView
					.findViewById(R.id.output_autofit);
			new NoteParse().execute(rowView, values.get(position).title);
			ImageButton editb = (ImageButton) rowView
					.findViewById(R.id.bNotesListLeft);
			ImageButton shareb = (ImageButton) rowView
					.findViewById(R.id.bNotesListRight);

			if(values.get(position).synced==2)
				((ImageView)rowView.findViewById(R.id.ivsynced)).setVisibility(View.VISIBLE);
			
			shareb.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent i = new Intent(NotesList.this,ShareListActivity.class);
					i.putExtra("docid", values.get(position).id);
					startActivity(i);
				}
			});
			
			editb.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent i = new Intent(NotesList.this, NotesEditor.class);
					i.putExtra("uuid", values.get(position).id);
					startActivity(i);

				}
			});
			String color;
			if(values.get(position).title.length()==0)color="#FFFFFF";
			else
				color =values.get(position).title.split("\n")[0].charAt(0)=='#'?values.get(position).title.split("\n")[0]:"#FFFFFF";
			
			((SquareLayout)rowView.findViewById(R.id.squareLayout1)).setBackgroundColor(Color.parseColor(color));
			textView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent(NotesList.this,
							NotesViewerActivity.class);
					i.putExtra("uuid", values.get(position).id);
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
											
											db.setDeleted(values.get(position).id);
											Log.d("deleted",
													values.get(position).id);
											ArrayList<document> a = new ArrayList<ByteMessenger.document>();
											Cursor c = db.getAllUndeletedDocs();
											c.moveToFirst();
											while (!c.isAfterLast()) {
												document d = db
														.doccursorToDocument(c);
												Log.d("NOTELIST", d.toString());
												a.add(d);
												c.moveToNext();
											}
											db.close();
											nla = new NotesListAdapter(
													NotesList.this,
													R.layout.activity_front, a);
											NotesList.this.setListAdapter(nla);
											startService(new Intent(NotesList.this, OfflineSyncService.class));
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
			 ProgressBar
			 s=(ProgressBar)rowView.findViewById(R.id.pnNoteLoader);
			// s.setVisibility(View.VISIBLE);
			// textView.setText(new
			// NeoHTML(values.get(position).getNote(),context).getSpannable());
			// textView.setText(Html.fromHtml(values.get(position).getNote()));
			// textView.setText(new
			// HtmlSpanner().fromHtml("<html><head></head><body>"+values.get(position).getNote()+"</body></ 	html>"));

			return rowView;
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
		ArrayList<document> a = new ArrayList<ByteMessenger.document>();
		Cursor c = db.getAllUndeletedDocs();
		c.moveToFirst();
		while (!c.isAfterLast()) {
						document d = db.doccursorToDocument(c);
			if(!db.isDocUnSynced(d.id))
				d.synced=2;
			a.add(d);
			c.moveToNext();
		}
		db.close();
		nla = new NotesListAdapter(this, R.layout.activity_front, a);
		this.setListAdapter(nla);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		DBManager db = new DBManager(NotesList.this);
		db.open();
		ArrayList<document> a = new ArrayList<ByteMessenger.document>();
		Cursor c = db.getAllUndeletedDocs();
		c.moveToFirst();
		while (!c.isAfterLast()) {
						document d = db.doccursorToDocument(c);
			if(!db.isDocUnSynced(d.id))
				d.synced=2;
			a.add(d);
			c.moveToNext();
		}
		db.close();
		nla = new NotesListAdapter(this, R.layout.activity_front, a);
		this.setListAdapter(nla);
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
				status = ByteMessenger.Logout(UUID.fromString(PreferenceManager.getDefaultSharedPreferences(NotesList.this).getString("cookie", ":(")));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (status) {
				PreferenceManager.getDefaultSharedPreferences(NotesList.this).edit().remove("cookie").commit();
				PreferenceManager.getDefaultSharedPreferences(NotesList.this).edit().remove("loginlock").commit();
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

}
