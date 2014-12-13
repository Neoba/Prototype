package com.neoba.syncpad;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import com.squareup.picasso.Picasso;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.preference.PreferenceManager;

public class SuggestedUsers extends ActionBarActivity {

	HashMap<String, Boolean> followed;
	ListView lvsugg;
	SuggestionsAdapter adapter;
	String[] usernames, names, urls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggested_users);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.tbSU);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setTitle("Suggested Followers");
		Intent i = getIntent();
		lvsugg = (ListView) findViewById(R.id.lvUsersFollowing);
		usernames = i.getExtras().getString("usernames").split(",");
		names = i.getExtras().getString("names").split(",");
		urls = i.getExtras().getString("pics").split(" ");
		followed = new HashMap<String, Boolean>();
		for (String s : usernames)
			followed.put(s, false);
		Log.d("SUGG", Arrays.toString(names));
		adapter = new SuggestionsAdapter(this, names, usernames, urls);
		lvsugg.setAdapter(adapter);

	}



	public class SuggestionsAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final String[] values;
		private final String[] usernames;
		private final String[] urls;

		public SuggestionsAdapter(Context context, String[] values,
				String[] usernames, String[] urls) {
			super(context, R.layout.suggestions_list, values);

			this.context = context;
			this.values = values;
			this.usernames = usernames;
			this.urls = urls;
		}
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}
		@Override
		public View getDropDownView(final int position, View convertView,
				ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}
		public View getCustomView(final int position, View convertView,
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

			if (followed.get(usernames[position]))
				followb.setImageResource(R.drawable.ic_followok);

			final ProgressBar pb = (ProgressBar) rowView
					.findViewById(R.id.pbProfile);
			followb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (!followed.get(usernames[position])) {
						Toast.makeText(SuggestedUsers.this, "Following..",
								Toast.LENGTH_SHORT).show();

						followb.setVisibility(View.INVISIBLE);
						pb.setVisibility(View.VISIBLE);
						new FollowUser().execute(usernames[position]);
					}
				}
			});

			textView.setText(values[position]);
			textView2.setText("@" + usernames[position]);
			Picasso.with(context).load(urls[position])
					.transform(new RoundedTransformation(60, 0))
					.into(imageView);
			return rowView;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.suggested_users, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Log.d("SIGNUP","Login success");
			
			Intent i = new Intent(SuggestedUsers.this, NotesList.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			finish();
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	public class FollowUser extends AsyncTask<String, Void, Long> {
		private ProgressDialog dialog;

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
			if (dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}

		}

		@Override
		protected void onPreExecute() {

			this.dialog = new ProgressDialog(SuggestedUsers.this);
			this.dialog.setMessage("Following user..");
			this.dialog.show();
		}

		@Override
		protected Long doInBackground(String... params) {

			String doc = null;
			try {
				doc = ByteMessenger.FollowUser(params[0], UUID
						.fromString(PreferenceManager
								.getDefaultSharedPreferences(
										SuggestedUsers.this).getString(
										"cookie", ":(")));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (doc != null) {
				DBManager db = new DBManager(SuggestedUsers.this);
				db.open();
				Long id = Long.parseLong(doc.split("~")[0]);
				db.insertFollowing(id, params[0] + "~" + doc.split("~")[1]);
				Log.d("FOLLOWING", doc + "" + params[0]);
				db.close();

				followed.put(params[0], true);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.notifyDataSetChanged();

					}
				});


			} else {

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
