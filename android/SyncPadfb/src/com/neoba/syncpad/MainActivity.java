package com.neoba.syncpad;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

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

	public void clicked(View v)
	{
		startActivity(new Intent(MainActivity.this,FbLogin.class));
	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000ff")));
        setContentView(R.layout.activity_main);
        TextView pingbutton = (TextView) findViewById(R.id.tvPing);
        if(!(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("cookie", "default").equals("default"))){
			Intent i = new Intent(MainActivity.this	, FrontActivity.class);
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
