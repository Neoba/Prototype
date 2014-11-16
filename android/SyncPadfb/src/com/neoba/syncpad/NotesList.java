package com.neoba.syncpad;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NotesList extends ListActivity {

	private String lorem = "Lorem ipsum dolor sit amet, te eum repudiare adipiscing, quando nonumy vocibus per at. Elit intellegebat id usu, at equidem verterem legendos sea. Sea summo clita te. Qui eu enim natum tacimates, qui te facer homero. Nec vivendo placerat postulant an, et quidam fastidii qui. Te dicit atomorum eos, ei esse justo consequuntur mel.el in duis fuisset, ad cum minim adipisci liberavisse, cu nec admodum percipitur. Et usu omnesque probatus. Pri idque consulatu consectetuer te, qui habemus mnesarchum contentiones at, te velit legendos eam. Integre labitur no pri. At mei posse urbanitas.Quo graece facilis cu, per movet soluta id. Esse eius omittam eu has, duo ad augue saepe prodesset. Iusto alterum appellantur mea et. Ad sit solet meliore liberavisse, laudem apeirian at quo.Nam abhorreant percipitur ad, ad duo dicat scribentur, torquatos honestatis eam an. Eum mutat sonet indoctum no, sit in habeo dicam. Te sea porro possim. Quo sint tincidunt suscipiantur ei, mel cu vitae postulant, dicat homero insolens quo et.Id possim scaevola pertinacia sed. Eam debet sensibus eu, alii percipit et mel. Dicant euripidis referrentur ex quo. Vim at ipsum complectitur. Cu vim alienum splendide. Graeci senserit sit te, id tation torquatos eos.Ad vix partiendo consetetur, consulatu concludaturque ut his. Lorem incorrupte vim no, epicurei principes vix an, est ea reprimique neglegentur. Prima habemus sit te, ei quod lorem mazim cum. Elit numquam eos ne, ut illum nonumes gubergren ius. Duo suas possim et, ex debet utinam omittantur cum, ut vis fabellas probatus. Vocibus nusquam dissentias et per.Duo id nibh iudicabit consulatu, fabulas commune his ad. Ne justo dicta splendide vix, utinam impetus efficiendi usu cu. Usu quidam legimus admodum ut, usu ea iisque recteque vulputate. Sit te blandit volutpat forensibus.Quo no mollis similique sententiae. Qui aliquam periculis quaerendum eu, mea id mandamus appellantur. Vis in facete utamur cotidieque. Ex dicit iriure eos, ne his bonorum sensibus. Laboramus delicatissimi in qui. Mei ad delicata constituam, commodo senserit tincidunt ne quo, noster impedit ei eum.Reque detracto id mei. Sonet tollit ut vel, est assum discere liberavisse ad. No audire corpora nec, placerat definitionem eos an, sale democritum ullamcorper id eos. Quem tacimates repudiare est ad, error ubique sed ei, vim legimus laoreet an.Facer graeci ad sit. Sit in nibh legere semper, decore placerat ea sit. Duo clita doctus at, per ea audiam molestiae, persecuti moderatius vel ex. Eum modus noster ea.";
	NotesListAdapter nla;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		DatabaseHandler db = new DatabaseHandler(this);
		List<Notes> notes = db.getAllNotes();
		nla = new NotesListAdapter(this,R.layout.activity_front, notes);
		setListAdapter(nla);
		
		 Calendar cal = Calendar.getInstance();
         cal.add(Calendar.SECOND, 10);
        
         Intent intent = new Intent(this, OfflineSyncService.class);
 
         PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        
         AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         //for 30 mint 60*60*1000
         alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                      60*1000, pintent);
         startService(new Intent(getBaseContext(), OfflineSyncService.class));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.front, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startService(new Intent(this, OfflineSyncService.class));

			Intent i = new Intent(NotesList.this, NotesEditor.class);
			i.putExtra("uuid", 0);
			startActivity(i);
			return true;
		}
		if (id == R.id.action_user) {
			Intent i = new Intent(NotesList.this, UserActivity.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public class CreateNote extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			
			return null;
		}
		
	}
	

	public class NotesListAdapter extends ArrayAdapter<Notes> {

		private final Context context;
		private final List<Notes> values;

		public NotesListAdapter(Context context, int resource,
				List<Notes> values) {
			super(context, resource, values);
			this.context = context;
			this.values = values;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.activity_front, parent,
					false);
			ProgressBar pp=(ProgressBar)rowView.findViewById(R.id.pnNoteLoader);
			pp.setVisibility(View.VISIBLE);
			final TextView textView = (TextView) rowView
					.findViewById(R.id.output_autofit);
			new NoteParse().execute(rowView,values.get(position).getNote());
			ImageButton editb=(ImageButton)rowView.findViewById(R.id.bNotesListLeft);
			editb.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent i = new Intent(NotesList.this,NotesEditor.class);
					i.putExtra("uuid", values.get(position).getID());
					startActivity(i);
					
				}
			});
			
			textView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(NotesList.this,NotesViewerActivity.class);
					i.putExtra("uuid", values.get(position).getID());
					i.putExtra("size", textView.getTextSize());
					startActivity(i);
					
				}
			});
			//ProgressBar s=(ProgressBar)rowView.findViewById(R.id.pnNoteLoader);
			//s.setVisibility(View.VISIBLE);
			//textView.setText(new NeoHTML(values.get(position).getNote(),context).getSpannable());
			//textView.setText(Html.fromHtml(values.get(position).getNote()));
			//textView.setText(new HtmlSpanner().fromHtml("<html><head></head><body>"+values.get(position).getNote()+"</body></ 	html>"));
			
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

		DatabaseHandler db = new DatabaseHandler(this);
		List<Notes> notes = db.getAllNotes();
		nla = new NotesListAdapter(this,R.layout.activity_front, notes);
		this.setListAdapter(nla);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		DatabaseHandler db = new DatabaseHandler(this);
		List<Notes> notes = db.getAllNotes();
		nla = new NotesListAdapter(this,R.layout.activity_front, notes);
		this.setListAdapter(nla);
	}
	
	class NoteParse extends AsyncTask<Object, Void, Void>{

		@Override
		protected Void doInBackground(Object... params) {
			// TODO Auto-generated method stub
			final View cv=(View)params[0];
			String html=(String)params[1];
			html=html.replaceAll(Pattern.quote("[ ]"),"\uE000");
			html=html.replaceAll(Pattern.quote("[*]"),"\uE001");
			final Note note=new NeoHTML(html, NotesList.this).getNote();
			final SpannableStringBuilder ss=note.getcontent();
			Typeface font2 = Typeface.createFromAsset(NotesList.this.getAssets(), "fonts/checkfont.ttf");
			ss.setSpan (new CustomTypefaceSpan("", font2),0, ss.length(),Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
//			final SpannableStringBuilder ss=new SpannableStringBuilder("a");
//			Typeface font = Typeface.createFromAsset(NotesList.this.getAssets(), "fonts/tickfont.ttf");
//			ss.setSpan (new CustomTypefaceSpan("", font),0, 1,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
//			ss.append(s2);
			final AutoResizeTextView aa=((AutoResizeTextView)cv.findViewById(R.id.output_autofit));
			runOnUiThread(new Runnable() {
				public void run() {
					aa.setText("");
					aa.setText(ss);
					ProgressBar pp=(ProgressBar)cv.findViewById(R.id.pnNoteLoader);
					RelativeLayout ss=(RelativeLayout) cv.findViewById(R.id.rlNotelist);
					ss.setBackgroundColor(Color
							.parseColor(note.getColor()));
					pp.setVisibility(View.INVISIBLE);
				}
			});
			return null;
		}
		
	}
	
	

}
