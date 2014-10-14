package com.neoba.syncpad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class FbLogin extends Activity {
	int flag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_view);

		final Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
				this, Arrays.asList("user_friends", "email"));

		flag = 0;

		Session.openActiveSession(this, true, new Session.StatusCallback() {

			// callback when session changes state
			@Override
			public void call(Session session, SessionState state,
					Exception exception) {

				if (session.isOpened()) {
					final Session s = session;
					if (flag == 0)
						session.requestNewReadPermissions(newPermissionsRequest);
					flag = 1;
					// make request to the /me API
					Request.newMeRequest(session,
							new Request.GraphUserCallback() {

								// callback after Graph API response with user
								// object

								@Override
								public void onCompleted(GraphUser user,
										Response response) {
									if (user != null) {
										PreferenceManager.getDefaultSharedPreferences(FbLogin.this).edit().putString("access_token", s.getAccessToken()).commit(); 
										new Login().execute(s.getAccessToken(),
												"CONSOLE");
									}

								}
							}).executeAsync();
				}

			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			finish();
		}
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);

	}

	public class Login extends AsyncTask<String, Void, UUID> {
		private ProgressDialog dialoggd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialoggd = new ProgressDialog(FbLogin.this);
			dialoggd.setMessage("Logging in..");
			dialoggd.show();
		}

		@Override
		protected void onPostExecute(UUID res) {
			super.onPostExecute(res);


			// if (res == null) {
			// new Ping().execute(1);
			//
			// } else {
			// new GetDigest().execute(res);
			// }

		}

		@Override
		protected UUID doInBackground(String... params) {
			ArrayList<HashMap<String, String>> session = null;
			try {
				session = ByteMessenger.facebookLoginUser(params[0], params[1]);
			} catch (Exception e) {
				Log.d("FBLOGIN","Login failed "+e);
				e.printStackTrace();
			}
			// if (session != null)
			// saveCookie(session);
			// return session;
			Log.d("FBLOGIN", session.toString());
			Log.d("FBLOGIN", session.toString()+" is one of this");
			dialoggd.dismiss();
			dialoggd = null;
			if (session.get(0).get("result").equals("unregistered")) {
				Intent i = new Intent(FbLogin.this, Signup.class);
				finish();
				startActivity(i);
			}else if (session.get(0).get("result").equals("success")) {
				Log.d("FBLOGIN","Success");
				PreferenceManager.getDefaultSharedPreferences(FbLogin.this).edit().putString("cookie",session.get(1).get("cookie")).commit();
				Intent i = new Intent(FbLogin.this, FrontActivity.class);
				finish();
				startActivity(i);
			}
			return null;
		}

	}

}
