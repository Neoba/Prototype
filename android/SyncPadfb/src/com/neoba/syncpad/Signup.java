package com.neoba.syncpad;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.neoba.syncpad.FbLogin.Login;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.os.Build;
import android.preference.PreferenceManager;

public class Signup extends Activity {
	
	String access_token;
	EditText username;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_signup);
		Button signup = (Button) findViewById(R.id.bSignup);
		username=(EditText) findViewById(R.id.etUsername);
		access_token=PreferenceManager.getDefaultSharedPreferences(Signup.this).getString("access_token", "defaultStringIfNothingFound");
		signup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new SignupTask().execute();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signup, menu);
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
	ArrayList<HashMap<String, String>> session = null;
	public class SignupTask extends AsyncTask<String, Void, UUID> {
		private ProgressDialog dialoggd;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.dialoggd = new ProgressDialog(Signup.this);
			this.dialoggd.setMessage("Signing up..");
	        this.dialoggd.show();
		}

		@Override
		protected void onPostExecute(UUID res) {
			super.onPostExecute(res);
			dialoggd=null;

			// if (res == null) {
			// new Ping().execute(1);
			//
			// } else {
			// new GetDigest().execute(res);
			// }

		}

		@Override
		protected UUID doInBackground(String... params) {
			
			try {
				session = ByteMessenger.facebookCreateUser(username.getText().toString(), access_token);
			} catch (Exception e) {

				e.printStackTrace();
			}
			// if (session != null)
			// saveCookie(session);
			// return session;
			
			dialoggd.dismiss();
			dialoggd=null;
			
			if(session!=null && session.get(0).get("result").equals("success")){
				
				new Login().execute(access_token,
						PreferenceManager.getDefaultSharedPreferences(Signup.this).getString("regid", ":("));

	
			}
			return null;
		}

	}
	public class Login extends AsyncTask<String, Void, UUID> {
		private ProgressDialog dialoggd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(UUID res) {
			super.onPostExecute(res);
		}

		@Override
		protected UUID doInBackground(String... params) {
			ArrayList<HashMap<String, String>> lsession = null;
			do{
			try {
				lsession = ByteMessenger.facebookLoginUser(params[0], params[1]);
			} catch (Exception e) {
				Log.d("FBLOGIN","Login failed "+e);
				e.printStackTrace();
			}
			}while(lsession.get(0).get("result").equals("unregistered"));
			// if (session != null)
			// saveCookie(session);
			// return session;
			Log.d("SIGNUP", lsession.toString());
			Log.d("SIGNUP", session.toString()+" is one of signups");
			if (lsession.get(0).get("result").equals("unregistered")) {

				Log.d("SIGNUP","Login Failed");
			}else if (lsession.get(0).get("result").equals("success")) {
				
				PreferenceManager.getDefaultSharedPreferences(Signup.this).edit().putString("cookie",lsession.get(1).get("cookie")).commit();
				PreferenceManager.getDefaultSharedPreferences(Signup.this).edit().putString("username",lsession.get(1).get("username")).commit();
				String n="",f="",url="";
				if(session.size()==1){
					Intent i = new Intent(Signup.this, NotesList.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					finish();
					startActivity(i);
				}else{
					for(int i=1;i<session.size();i++){
						n+=(session.get(i).get("username"));
						n+=",";
						JSONObject user;
						
						try {
							user=ByteMessenger.jsonGet("https://graph.facebook.com/v2.1/"+session.get(i).get("id")+"?fields=id,name,picture&access_token="+access_token);
							f+=user.getString("name");
							f+=",";
							url+=user.getJSONObject("picture").getJSONObject("data").getString("url");
							url+=" ";
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

					n=n.substring(0, n.length());
					f=f.substring(0, f.length());
					url=url.substring(0, url.length());
					Intent i = new Intent(Signup.this,SuggestedUsers.class);
					i.putExtra("usernames", n);
					i.putExtra("names", f);
					i.putExtra("pics", url);
					finish();
					startActivity(i);
				}
				
				
			}
			return null;
		}

	}

}
