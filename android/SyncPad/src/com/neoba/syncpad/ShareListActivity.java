package com.neoba.syncpad;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.neoba.syncpad.ByteMessenger.Share;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ShareListActivity extends Activity {
	ShareAdapter sa;
	ShareSchema sch;
	ArrayList<String> usernames;
	ArrayList<Share> shares;
	ListView slist;
	String docid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_list);
		docid = getIntent().getExtras().getString("docid");
		DBManager db = new DBManager(getApplicationContext());
		db.open();
		// sch=new ShareSchema();
		// sch.setDocid(db.getId(rowid));

		slist = (ListView) findViewById(R.id.lvShares);
		Button add = (Button) findViewById(R.id.bSLAdd);
		final Spinner sp = (Spinner) findViewById(R.id.spFollower);

		Cursor uc = db.getAllFollower();
		usernames = new ArrayList<String>();
		if (uc.moveToFirst()) {
			do {
				usernames.add(uc.getString(2));
			} while (uc.moveToNext());
		}

		sp.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, usernames));
		
		shares=new ArrayList<ByteMessenger.Share>();
		Cursor c=db.getPermissionsForDoc(docid);
		if(c.moveToFirst()){
			do{
				shares.add(new Share(docid,c.getLong(1),c.getString(0),(byte) c.getInt(2)));
			}while(c.moveToNext());
		}
		
		db.close();
		sa = new ShareAdapter(this, R.layout.share_list_element,shares);
		slist.setAdapter(sa);
		
		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for(Share i:shares){
					if(i.username.equals((String) sp.getSelectedItem()))
						return;
				}
				DBManager db = new DBManager(getApplicationContext());
				db.open();
				shares.add(new Share(docid,db.getFollowerid((String) sp.getSelectedItem()) ,(String) sp.getSelectedItem(),(byte)1));
				slist.setAdapter(sa);
				Log.d("PERMADD",""+shares);
				db.close();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.share_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_sync_share) {
			new ShareSync().execute(shares);
		}
		return super.onOptionsItemSelected(item);
	}

	public class ShareAdapter extends ArrayAdapter<Share> {

		public ShareAdapter(Context context, int textViewResourceId,List<Share> objects) {
			super(context, textViewResourceId, objects);
			this.layoutResourceId = textViewResourceId;
			this.context = context;
			this.items = objects;
		}

		private List<Share> items;
		private int layoutResourceId;
		private Context context;

		@SuppressLint({ "ViewHolder", "CutPasteId" })
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ShareHolder holder = null;

			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			final View row = inflater.inflate(layoutResourceId, parent, false);
			Button remove = (Button) row.findViewById(R.id.bSLERemove);
			Switch rw = (Switch) row.findViewById(R.id.swRW);
			rw.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					items.get(position).setPermission((byte)(isChecked?2:1));
					Log.d("PERMCHANGE", ""+items);
				}
			});
			remove.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					items.remove(position);
					notifyDataSetChanged();
					Log.d("PERMREM", ""+items);
				}
			});

			holder = new ShareHolder();
			holder.share = items.get(position);
			holder.username = (TextView) row.findViewById(R.id.tvsleUname);
			holder.sw = (Switch) row.findViewById(R.id.swRW);
			row.setTag(holder);
			holder.username.setText(holder.share.username);
			if(holder.share.permission==2)holder.sw.toggle();
			return row;

		}

		public class ShareHolder {
			Share share;
			TextView username;
			Switch sw;
		}
	}
	public class ShareSync extends AsyncTask<ArrayList<Share>, Void, ArrayList<Share>> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(ShareListActivity.this);
			dialog.setMessage("Sharing..");
			dialog.show();
		}

		@Override
		protected void onPostExecute(ArrayList<Share> result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			
			if(result==null){
				runOnUiThread(new Runnable(){
			          @Override
			          public void run(){
			        	  Toast.makeText(getApplicationContext(),"Sorry.. Share failed :(", Toast.LENGTH_LONG).show();
			          }
			       });
			}else{
				DBManager db = new DBManager(getApplicationContext());
				db.open();
				db.clearPermissions(docid);
				for(Share s:result){
					db.insertPermission(docid, s.username, s.userid, s.permission);
				}
				db.close();
			}
		}

		@Override
		protected ArrayList<Share> doInBackground(ArrayList<Share>... params) {
			ArrayList<Share> result = null;
			try {
				result=ByteMessenger.ShareMessage(params[0], UUID.fromString(docid), getCookie());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}

	}
	private UUID getCookie() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String name = sharedPreferences.getString("cookie", null);
		Log.d("sess", name == null ? "null" : name);
		if (name != null)
			return UUID.fromString(name);
		else
			return null;

	}
}
