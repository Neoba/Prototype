package com.neoba.syncpad;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.neoba.syncpad.ByteMessenger.Share;
import com.squareup.picasso.Picasso;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ShareListActivity extends Activity {
	ShareAdapter sa;
	ShareSchema sch;
	ArrayList<String> usernamesl;
	ArrayList<Share> shares;
	ListView slist;
	String docid;
	String access_token;
	String[] usernames;
	public HashMap<String,String> names;
	public HashMap<String,String> urls;
	ProgressBar pb;
	Spinner sp;
	ImageButton add,shareb;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_list);
		docid = getIntent().getExtras().getString("docid");
		DBManager db = new DBManager(getApplicationContext());
		pb=(ProgressBar)findViewById(R.id.pbShares);
		db.open();
		// sch=new ShareSchema();
		// sch.setDocid(db.getId(rowid));
		
		access_token=PreferenceManager.getDefaultSharedPreferences(ShareListActivity.this).getString("access_token", "defaultStringIfNothingFound");
		slist = (ListView) findViewById(R.id.lvShares);
		add = (ImageButton) findViewById(R.id.bSLAdd);
		shareb = (ImageButton) findViewById(R.id.bSLShare);
		add.setVisibility(View.INVISIBLE);
		shareb.setVisibility(View.INVISIBLE);
		
		sp = (Spinner) findViewById(R.id.spFollower);
		sp.setVisibility(View.INVISIBLE);
		Cursor uc = db.getAllFollower();
		usernamesl = new ArrayList<String>();
		if (uc.moveToFirst()) {
			do {
				usernamesl.add(uc.getString(2));
			} while (uc.moveToNext());
		}
		usernames = new String[usernamesl.size()];
		usernamesl.toArray(usernames);
		new GetAllUrls().execute();
		//sp.setAdapter(new ShareSpinnerAdapter(this, usernames));

		shares = new ArrayList<ByteMessenger.Share>();
		Cursor c = db.getPermissionsForDoc(docid);
		if (c.moveToFirst()) {
			do {
				shares.add(new Share(docid, c.getLong(1), c.getString(0),
						(byte) c.getInt(2)));
			} while (c.moveToNext());
		}

		db.close();

		shareb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new ShareSync().execute(shares);
			}
		});
		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (Share i : shares) {
					if (i.username.equals((String) sp.getSelectedItem()))
						return;
				}
				DBManager db = new DBManager(getApplicationContext());
				db.open();
				try{
				shares.add(new Share(docid, db.getFollowerid((String) sp
						.getSelectedItem()), (String) sp.getSelectedItem(),
						(byte) 1));
					slist.setAdapter(sa);
				}
				catch(Exception e){
					Log.d("Bug","FIC");
				}
				
				Log.d("PERMADD", "" + shares);
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

		}
		return super.onOptionsItemSelected(item);
	}

	public class ShareAdapter extends ArrayAdapter<Share> {

		public ShareAdapter(Context context, int textViewResourceId,
				List<Share> objects) {
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
			ImageButton remove = (ImageButton) row.findViewById(R.id.bSLERemove);
			android.support.v7.widget.SwitchCompat rw = (android.support.v7.widget.SwitchCompat) row.findViewById(R.id.swRW);
			rw.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					items.get(position).setPermission(
							(byte) (isChecked ? 2 : 1));
					Log.d("PERMCHANGE", "" + items);
				}
			});
			remove.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					items.remove(position);
					notifyDataSetChanged();
					Log.d("PERMREM", "" + items);
				}
			});

			holder = new ShareHolder();
			holder.share = items.get(position);
			holder.username = (TextView) row.findViewById(R.id.tvsleUname);
			holder.name = (TextView) row.findViewById(R.id.tvsleName);
			holder.profilepic = (ImageView) row.findViewById(R.id.ivslePicture);
			holder.sw = (android.support.v7.widget.SwitchCompat) row.findViewById(R.id.swRW);
			row.setTag(holder);
			
			holder.username.setText("@"+holder.share.username.split("~")[0]);
			holder.name.setText(names.get(holder.share.username));
			Picasso.with(context).load(urls.get(holder.share.username	))
			.resize(70,70).transform(new RoundedTransformation(60, 0))
			.into(holder.profilepic);
			
			if (holder.share.permission == 2)
				holder.sw.toggle();
			return row;

		}

		public class ShareHolder {
			Share share;
			TextView username;
			TextView name;
			ImageView profilepic;
			android.support.v7.widget.SwitchCompat sw;
		}
	}

	public class ShareSync extends
			AsyncTask<ArrayList<Share>, Void, ArrayList<Share>> {
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
				dialog = null;
			}

			if (result == null) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(),
								"Sorry.. Share failed :(", Toast.LENGTH_LONG)
								.show();
					}
				});
			} else {
				DBManager db = new DBManager(getApplicationContext());
				db.open();
				db.clearPermissions(docid);
				for (Share s : result) {
					db.insertPermission(docid, s.username, s.userid,
							s.permission);
				}
				db.close();
			}
		}

		@Override
		protected ArrayList<Share> doInBackground(ArrayList<Share>... params) {
			ArrayList<Share> result = null;
			try {
				result = ByteMessenger.ShareMessage(params[0],
						UUID.fromString(docid), getCookie());
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
	 public class ShareSpinnerAdapter extends ArrayAdapter<String> {
			private final Context context;
			private final String[] values;

			public ShareSpinnerAdapter(Context context, String[] values) {
				super(context, R.layout.suggestions_list,R.id.firstLine, values);

				this.context = context;
				this.values = values;
			}

			public View getView(final int position, View convertView,
					ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View rowView = inflater.inflate(R.layout.suggestions_list, parent,
						false);
				TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
				TextView textView2 = (TextView) rowView
						.findViewById(R.id.secondLine);
				ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
				final ImageView followb = (ImageView) rowView
						.findViewById(R.id.ivFollow);
				followb.setVisibility(View.GONE);

				final ProgressBar pb = (ProgressBar) rowView
						.findViewById(R.id.pbProfile);

				textView2.setText("@" + usernames[position].split("~")[0]);

					textView.setText(names.get(usernames[position]));
					Picasso.with(context).load(urls.get(usernames[position]	))
							.resize(70,70).transform(new RoundedTransformation(60, 0))
							.into(imageView);
		
				return rowView;
			}
			
			public View getDropDownView(final int position, View convertView,
					ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View rowView = inflater.inflate(R.layout.suggestions_list, parent,
						false);
				TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
				TextView textView2 = (TextView) rowView
						.findViewById(R.id.secondLine);
				ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
				final ImageView followb = (ImageView) rowView
						.findViewById(R.id.ivFollow);
				followb.setVisibility(View.GONE);

				final ProgressBar pb = (ProgressBar) rowView
						.findViewById(R.id.pbProfile);

				textView2.setText("@" + usernames[position].split("~")[0]);

					textView.setText(names.get(usernames[position]));
					Picasso.with(context).load(urls.get(usernames[position]	))
							.resize(70,70).transform(new RoundedTransformation(60, 0))
							.into(imageView);
		
				return rowView;
			}
		}
	 
	 public class GetAllUrls extends AsyncTask<String, Void, UUID> {

		@Override
		protected UUID doInBackground(String... arg0) {
			names=new HashMap<String,String>();
			urls=new HashMap<String,String>();
			for(String s:usernames){
				JSONObject user;
				try {
					user = ByteMessenger.jsonGet("https://graph.facebook.com/v2.1/"+s.split("~")[1]+"?fields=id,name,picture&access_token="+access_token);
					names.put(s,user.getString("name"));
					urls.put(s,user.getJSONObject("picture").getJSONObject("data").getString("url"));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					sp.setVisibility(View.VISIBLE);
					add.setVisibility(View.VISIBLE);
					shareb.setVisibility(View.VISIBLE);
					pb.setVisibility(View.INVISIBLE);
					ShareSpinnerAdapter ssp=new ShareSpinnerAdapter(ShareListActivity.this,usernames);
					Log.d("Stst",names+"");
					sp.setAdapter(ssp);
					sa = new ShareAdapter(ShareListActivity.this, R.layout.share_list_element, shares);
					slist.setAdapter(sa);
					
				}
			});

			return null;
		}
	 
	 }
}