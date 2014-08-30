package com.neoba.syncpad;

import java.util.UUID;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
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
import android.widget.Toast;
import android.os.Build;

public class SignUpActivity extends Activity {

	EditText uname,pass,passc;
	Button signup;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		uname=(EditText)findViewById(R.id.etspUsername);
		pass=(EditText)findViewById(R.id.etspPassword);
		passc=(EditText)findViewById(R.id.etspPasswordC);
		signup=(Button)findViewById(R.id.bspSignUp);
		
		signup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(pass.getText().toString().equals(passc.getText().toString())){
					new SignUp().execute(uname.getText().toString(),pass.getText().toString());
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Passwords mismatch. please try again..", Toast.LENGTH_SHORT).show();
					pass.setText("");
					passc.setText("");
				}
				
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		return super.onOptionsItemSelected(item);
	}
	
	public class SignUp extends AsyncTask<String, Void, Void> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(SignUpActivity.this);
			dialog.setMessage("Signing Up..");
			dialog.show();
		}

		@Override
		protected void onPostExecute(Void res) {
			super.onPostExecute(res);
			if(dialog!=null)
			if (dialog.isShowing()) {
				dialog.dismiss();
				dialog=null;
			}

		}


		@Override
		protected Void doInBackground(String... params) {
			int reply=0;
			try {
				reply=ByteMessenger.SignUp(params[0], params[1]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(reply==0xFFFF){
				
				try {
					//this is a small hack to ensure that the new user is up in the database
					//by trying to login and logout
					UUID cookie=null;
					while(cookie==null){
						Thread.sleep(5000);
						cookie=ByteMessenger.Login(params[0], params[1],"DUMMY_ID");
					}
					ByteMessenger.Logout(cookie);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				Intent returnIntent = new Intent();
				returnIntent.putExtra("username",params[0]);
				returnIntent.putExtra("password",params[1]);
				setResult(RESULT_OK,returnIntent);
				runOnUiThread(new Runnable() {
					public void run() {
							Toast.makeText(getBaseContext(), "Success! :)",
									Toast.LENGTH_SHORT).show();


					}
				});
				finish();
			}else if(reply==0x8000){
				if (dialog.isShowing()) {
					dialog.dismiss();
					dialog=null;
				}
				runOnUiThread(new Runnable() {
					public void run() {
							Toast.makeText(getBaseContext(), "Sorry, That username is already taken :(",
									Toast.LENGTH_SHORT).show();


					}
				});
				

			}
			return null;
		}

	}


}
