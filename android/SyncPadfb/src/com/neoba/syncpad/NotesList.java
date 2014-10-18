package com.neoba.syncpad;

import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NotesList extends ListActivity {

	private String lorem = "Lorem ipsum dolor sit amet, te eum repudiare adipiscing, quando nonumy vocibus per at. Elit intellegebat id usu, at equidem verterem legendos sea. Sea summo clita te. Qui eu enim natum tacimates, qui te facer homero. Nec vivendo placerat postulant an, et quidam fastidii qui. Te dicit atomorum eos, ei esse justo consequuntur mel.el in duis fuisset, ad cum minim adipisci liberavisse, cu nec admodum percipitur. Et usu omnesque probatus. Pri idque consulatu consectetuer te, qui habemus mnesarchum contentiones at, te velit legendos eam. Integre labitur no pri. At mei posse urbanitas.Quo graece facilis cu, per movet soluta id. Esse eius omittam eu has, duo ad augue saepe prodesset. Iusto alterum appellantur mea et. Ad sit solet meliore liberavisse, laudem apeirian at quo.Nam abhorreant percipitur ad, ad duo dicat scribentur, torquatos honestatis eam an. Eum mutat sonet indoctum no, sit in habeo dicam. Te sea porro possim. Quo sint tincidunt suscipiantur ei, mel cu vitae postulant, dicat homero insolens quo et.Id possim scaevola pertinacia sed. Eam debet sensibus eu, alii percipit et mel. Dicant euripidis referrentur ex quo. Vim at ipsum complectitur. Cu vim alienum splendide. Graeci senserit sit te, id tation torquatos eos.Ad vix partiendo consetetur, consulatu concludaturque ut his. Lorem incorrupte vim no, epicurei principes vix an, est ea reprimique neglegentur. Prima habemus sit te, ei quod lorem mazim cum. Elit numquam eos ne, ut illum nonumes gubergren ius. Duo suas possim et, ex debet utinam omittantur cum, ut vis fabellas probatus. Vocibus nusquam dissentias et per.Duo id nibh iudicabit consulatu, fabulas commune his ad. Ne justo dicta splendide vix, utinam impetus efficiendi usu cu. Usu quidam legimus admodum ut, usu ea iisque recteque vulputate. Sit te blandit volutpat forensibus.Quo no mollis similique sententiae. Qui aliquam periculis quaerendum eu, mea id mandamus appellantur. Vis in facete utamur cotidieque. Ex dicit iriure eos, ne his bonorum sensibus. Laboramus delicatissimi in qui. Mei ad delicata constituam, commodo senserit tincidunt ne quo, noster impedit ei eum.Reque detracto id mei. Sonet tollit ut vel, est assum discere liberavisse ad. No audire corpora nec, placerat definitionem eos an, sale democritum ullamcorper id eos. Quem tacimates repudiare est ad, error ubique sed ei, vim legimus laoreet an.Facer graeci ad sit. Sit in nibh legere semper, decore placerat ea sit. Duo clita doctus at, per ea audiam molestiae, persecuti moderatius vel ex. Eum modus noster ea.";
	NotesListAdapter nla;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DatabaseHandler db = new DatabaseHandler(this);
		List<Notes> notes = db.getAllNotes();
		nla = new NotesListAdapter(this,R.layout.activity_front, notes);
		this.setListAdapter(nla);

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
			Intent i = new Intent(NotesList.this, NotesEditor.class);
			i.putExtra("uuid", 0);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
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

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.activity_front, parent,
					false);
			TextView textView = (TextView) rowView
					.findViewById(R.id.output_autofit);
			
			textView.setText(values.get(position).getNote());
			return rowView;
		}

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		DatabaseHandler db = new DatabaseHandler(this);
		List<Notes> notes = db.getAllNotes();
		nla = new NotesListAdapter(this,R.layout.activity_front, notes);
		this.setListAdapter(nla);
	}

}
