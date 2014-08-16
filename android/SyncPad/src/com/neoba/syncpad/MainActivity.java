package com.neoba.syncpad;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import com.neoba.syncpad.ByteMessenger.document;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity {
	Button login;
	EditText username;
	EditText password;
	TextView pingbutton;
	TextView wrongpass;
	TextView Join;
	GoogleCloudMessaging gcm;
	String regid = "Cannot connect to push service..";
	String PROJECT_NUMBER = "1053985856790";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (getCookie() != null) {
			startActivity(new Intent("com.neoba.syncpad.DOCSLISTACTIVITY"));
			finish();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		login = (Button) findViewById(R.id.bLogin);
		username = (EditText) findViewById(R.id.etUsername);
		password = (EditText) findViewById(R.id.etPassword);
		wrongpass = (TextView) findViewById(R.id.tvWrong);
		Join = (TextView) findViewById(R.id.tvSignup);
		TextView pingbutton = (TextView) findViewById(R.id.tvSignUp);
		pingbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Ping().execute(0);
			}
		});

		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getRegId();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class Ping extends AsyncTask<Integer, Void, Void> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("Trying to connect..");
			dialog.show();
		}

		@Override
		protected void onPostExecute(Void res) {
			super.onPostExecute(res);
			if (dialog.isShowing()) {
				dialog.dismiss();
				dialog=null;
			}

		}

		@Override
		protected Void doInBackground(final Integer... params) {

			try {
				ByteMessenger.Ping();

				Log.d("ok", "PONG");
				runOnUiThread(new Runnable() {
					public void run() {
						if (params[0] == 0)
							Toast.makeText(getBaseContext(), "Pong!",
									Toast.LENGTH_SHORT).show();
						else
							wrongpass.setText(R.string.authentication_failure);

					}
				});
			} catch (Exception e) {

				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(
								getBaseContext(),
								"Cannot connect to the server at the moment :(",
								Toast.LENGTH_SHORT).show();
						wrongpass.setText("");
					}
				});
				e.printStackTrace();
			}

			return null;
		}

	}

	public class Login extends AsyncTask<String, Void, UUID> {
		private ProgressDialog dialoggd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialoggd = new ProgressDialog(MainActivity.this);
			dialoggd.setMessage("Logging in..");
			dialoggd.show();
		}

		@Override
		protected void onPostExecute(UUID res) {
			super.onPostExecute(res);

			dialoggd.dismiss();
			dialoggd=null;
			if (res == null) {
				new Ping().execute(1);

			} else {
				wrongpass.setText("");
				new GetDigest().execute(res);
			}

		}

		@Override
		protected UUID doInBackground(String... params) {
			UUID session = null;
			try {
				session = ByteMessenger.Login(params[0], params[1], params[2]);
			} catch (Exception e) {

				e.printStackTrace();
			}
			if (session != null)
				saveCookie(session);
			return session;
		}

	}

	public class GetDigest extends AsyncTask<UUID, Void, HashMap<UUID, ByteMessenger.document>> {
		private ProgressDialog dialogl;

		@Override
		protected void onPreExecute() {
			dialogl = new ProgressDialog(MainActivity.this);
			dialogl.setMessage("Logged in. Getting your Documents for the first time..");
			dialogl.show();
		}

		@Override
		protected void onPostExecute(HashMap<UUID, document> result) {
			dialogl.dismiss();
			dialogl=null;
			Intent docintent = new Intent("com.neoba.syncpad.DOCSLISTACTIVITY");
			startActivity(docintent);
			finish();
		}

		@Override
		protected HashMap<UUID, document> doInBackground(UUID... params) {
			try {
				@SuppressWarnings("unchecked")
				ArrayList<Object> obtained = ByteMessenger.getDigest(params[0]);
				@SuppressWarnings("unchecked")
				HashMap<UUID, document> docs = (HashMap<UUID, document>) obtained
						.get(0);
				@SuppressWarnings("unchecked")
				HashMap<Long, String> follower = (HashMap<Long, String>) obtained
						.get(1);
				@SuppressWarnings("unchecked")
				HashMap<Long, String> following = (HashMap<Long, String>) obtained
						.get(2);
				@SuppressWarnings("unchecked")
				ArrayList<ShareSchema> shares = (ArrayList<ShareSchema>) obtained
						.get(3);
				DBManager db = new DBManager(MainActivity.this);
				db.open();
				for (UUID a : docs.keySet()) {
					db.insertDoc(docs.get(a));

				}
				for (Long a : follower.keySet()) {
					db.insertFollower(a, follower.get(a));

				}
				for (Long a : following.keySet()) {
					db.insertFollowing(a, following.get(a));

				}
				db.close();

				db.open();
				for (ShareSchema s : shares) {
					String docid = s.docid;
					for (Long id : s.read)
						db.insertPermission(docid, db.getFollowerUsername(id),id, 1);
					for (Long id : s.edit)
						db.escalatePermission(docid, id);
				}
				db.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
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

	private void saveCookie(UUID cookie) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		editor.putString("cookie", cookie.toString());
		editor.commit();
	}

	public void getRegId() {
		new AsyncTask<Void, Void, Boolean>() {

			private ProgressDialog dialogr;

			@Override
			protected void onPreExecute() {
				dialogr = new ProgressDialog(MainActivity.this);
				this.dialogr.setMessage("Registering for push notifications..");
				this.dialogr.show();
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				String msg = "";
				boolean f;
				int tries = 5;
				if (gcm == null) {
					gcm = GoogleCloudMessaging
							.getInstance(getApplicationContext());
				}
				do {
					f = false;
					tries -= 1;
					try {
						Log.d("PUSH", tries + regid);
						regid = gcm.register(PROJECT_NUMBER);
					} catch (IOException ex) {
						msg = ex.getMessage();
						f = true;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} while (f && tries >= 0);

				return tries >= 0;

			}

			@Override
			protected void onPostExecute(Boolean res) {
				dialogr.dismiss();
				dialogr=null;
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getBaseContext(), regid,
								Toast.LENGTH_SHORT).show();
						wrongpass.setText("");
					}
				});
				if (res)
					new Login().execute(username.getText().toString(), password
							.getText().toString(), regid);

			}
		}.execute(null, null, null);
	}

}
