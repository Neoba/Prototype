package com.neoba.syncpad;

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

public class MainActivity extends Activity {
	Button login;
	EditText username;
	EditText password;
	TextView signup;
	TextView wrongpass;

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
		TextView signup = (TextView) findViewById(R.id.tvSignUp);
		signup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Ping().execute(0);
			}
		});

		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				new Login().execute(username.getText().toString(), password
						.getText().toString());
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
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
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
			}

		}
		@Override
		protected Void doInBackground(final Integer... params) {

			try {
				ByteMessenger.Ping();

				Log.d("ok", "PONG");
				runOnUiThread(new Runnable() {
					public void run() {
						if(params[0]==0)Toast.makeText(getBaseContext(), "Pong!",Toast.LENGTH_SHORT).show();
						else wrongpass.setText(R.string.authentication_failure);
						
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
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("Logging in..");
			dialog.show();
		}

		@Override
		protected void onPostExecute(UUID res) {
			super.onPostExecute(res);

			if (dialog.isShowing()) {
				dialog.dismiss();
			}
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
				session = ByteMessenger.Login(params[0], params[1]);
			} catch (Exception e) {

				e.printStackTrace();
			}
			if (session != null)
				saveCookie(session);
			return session;
		}

	}

	public class GetDigest extends
			AsyncTask<UUID, Void, HashMap<UUID, ByteMessenger.document>> {
		private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

		@Override
		protected void onPreExecute() {
			this.dialog
					.setMessage("Logged in. Getting your Documents for the first time..");
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(HashMap<UUID, document> result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			Intent docintent = new Intent("com.neoba.syncpad.DOCSLISTACTIVITY");
			
			startActivity(docintent);
			finish();


		}

		@Override
		protected HashMap<UUID, document> doInBackground(UUID... params) {
			try {
				HashMap<UUID, document> docs= ByteMessenger.getDigest(params[0]);
				DBManager db=new DBManager(MainActivity.this);
				db.open();
				for(UUID a:docs.keySet()){
					db.insertDoc(docs.get(a));
					
				}
				db.getAllDocs();
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

}
