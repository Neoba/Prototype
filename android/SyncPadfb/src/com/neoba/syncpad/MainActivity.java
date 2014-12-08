package com.neoba.syncpad;


import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.neoba.syncpad.ByteMessenger.document;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	int flag;
	GoogleCloudMessaging gcm;
	String regid = "Cannot connect to push service..";
	String PROJECT_NUMBER = "1053985856790";
	public void clicked(View v)
	{
		
		getRegId();
		
	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
//        ActionBar actionBar = getActionBar();
//        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000ff")));
        setContentView(R.layout.activity_main);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.neoba.syncpad", 
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
        } catch (NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        TextView pingbutton = (TextView) findViewById(R.id.tvPing);
        if(!(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("cookie", "default").equals("default"))){
			Intent i = new Intent(MainActivity.this	, NotesList.class);
			finish();
			startActivity(i);
        }
		pingbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Ping().execute(0);
			}
		});

    }
   

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if(requestCode==1)
      {
    	 finish(); 
      }
      Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
     
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
	
		if (id == R.id.action_debugf) {
			Intent i=new Intent(this,NotesList.class);
			startActivity(i);
		    return true;
		}
		return super.onOptionsItemSelected(item);
	}

    public void getRegId() {
		new AsyncTask<Void, Void, Boolean>() {



			@Override
			protected void onPreExecute() {

			}

			@Override
			protected Boolean doInBackground(Void... params) {
				String msg = "";
				boolean f;
				int tries = 5;
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
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

				if (res)
				{
					Log.d("GCMID", regid);
					PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("regid", regid).commit(); 
					startActivity(new Intent(MainActivity.this,FbLogin.class));
					finish();
				}


			}
		}.execute(null, null, null);
	}
    /**
     * Async Tasks for LoginActivity
     * 
     *  1)Ping
     * 
     */
    
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


		}

		@Override
		protected Void doInBackground(final Integer... params) {

			try {
				ByteMessenger.Ping();
				dialog.dismiss();
				dialog=null;
				Log.d("ok", "PONG");
				runOnUiThread(new Runnable() {
					public void run() {
						if (params[0] == 0)
							Toast.makeText(getBaseContext(), "Pong!",
									Toast.LENGTH_SHORT).show();
					}
				});
			} catch (Exception e) {

				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(
								getBaseContext(),
								"Cannot connect to the server at the moment :(",
								Toast.LENGTH_SHORT).show();
						//wrongpass.setText("");
					}
				});
				e.printStackTrace();
			}

			return null;
		}

	}

}
