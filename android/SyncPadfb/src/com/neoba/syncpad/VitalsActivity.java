package com.neoba.syncpad;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.internal.ay;
import com.neoba.syncpad.FollowingFragment.FollowingAdapter;
import com.squareup.picasso.Picasso;

public class VitalsActivity extends ActionBarActivity {

	TextView name;
	TextView followercount;
	TextView username;
	ImageView image, cover;
	ProgressBar pb;
	FragmentPagerAdapter adapterViewPager;
	ViewPager vpPager;
	Toolbar toolbar;
	String userkey;
	String vital;
	JSONObject userjson;
	String access_token;
	String[] ingusernames;
	String[] ingnames;
	String[] ingurls;

	String[] erusernames;
	String[] ernames;
	String[] erurls;
	FloatingActionButton fabButton;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		toolbar = (Toolbar) findViewById(R.id.tbUser);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			MarginLayoutParams mp = (MarginLayoutParams) toolbar
					.getLayoutParams();
			mp.topMargin = 0;
			toolbar.setLayoutParams(mp);
		}
		getSupportActionBar().setTitle("");
		// getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		vpPager = (ViewPager) findViewById(R.id.vpPager);
		// adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(),new
		// String[] {},new String[] {},new String[] {});
		// vpPager.setAdapter(adapterViewPager);

		PagerTabStrip strip = (PagerTabStrip) findViewById(R.id.pager_header);
		strip.setDrawFullUnderline(false);
		strip.setTabIndicatorColor(Color.WHITE);

		username = (TextView) findViewById(R.id.tvSharedUsers);
		followercount = (TextView) findViewById(R.id.tvFollowerCount);
		image = (ImageView) findViewById(R.id.ivPicture);
		cover = (ImageView) findViewById(R.id.ivCover);
		name = (TextView) findViewById(R.id.tvTimeStamp);
		pb = (ProgressBar) findViewById(R.id.pbProfileLoad);

		username.setVisibility(View.INVISIBLE);
		image.setVisibility(View.INVISIBLE);
		followercount.setVisibility(View.INVISIBLE);
		cover.setVisibility(View.INVISIBLE);
		name.setVisibility(View.INVISIBLE);

		userkey = getIntent().getExtras().getString("user");

		username.setText("@" + userkey.split("~")[0]);
		new VitalsFetch().execute(userkey);
	}

	class VitalsFetch extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try {
				vital = ByteMessenger.UserVitals(params[0].split("~")[0], UUID
						.fromString(PreferenceManager
								.getDefaultSharedPreferences(
										VitalsActivity.this).getString(
										"cookie", ":(")));
				access_token = PreferenceManager.getDefaultSharedPreferences(
						VitalsActivity.this).getString("access_token",
						"defaultStringIfNothingFound");
				Log.d("Check url",
						"https://graph.facebook.com/v2.1/"
								+ vital.split("@")[1].split("~")[1]
								+ "?fields=cover,name,picture&access_token="
								+ access_token);
				userjson = ByteMessenger
						.jsonGet("https://graph.facebook.com/v2.1/"
								+ vital.split("@")[1].split("~")[1]
								+ "?fields=cover,name,picture&access_token="
								+ access_token);
				Log.d("ss", Arrays.toString(vital.split("@")));

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (vital.split("@")[0].equals("1")) {

							fabButton = new FloatingActionButton.Builder(
									VitalsActivity.this)
									.withDrawable(
											getResources()
													.getDrawable(
															R.drawable.ic_clear_white_24dp))
									.withButtonColor(
											getResources().getColor(
													R.color.orange_500))
									.withGravity(Gravity.TOP | Gravity.RIGHT)
									.withMargins(0, 258 - 35, 16, 0).create();

							fabButton.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									new Unfollow().execute(vital.split("@")[1]);

								}
							});

						} else {

							fabButton = new FloatingActionButton.Builder(
									VitalsActivity.this)
									.withDrawable(
											getResources()
													.getDrawable(
															R.drawable.ic_person_add_white_24dp))
									.withButtonColor(
											getResources().getColor(
													R.color.light_blue_500))
									.withGravity(Gravity.TOP | Gravity.RIGHT)
									.withMargins(0, 258 - 35, 16, 0).create();

							fabButton.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									new FollowUser().execute(vital.split("@")[1]
											.split("~")[0]);

								}
							});
						}

					}
				});

				ArrayList<String> a = new ArrayList<String>();
				if (!vital.split("@")[3].equals("X"))
					for (int i = 0; i < vital.split("@")[3].split("&").length; i++)
						a.add(vital.split("@")[3].split("&")[i]);
				ArrayList<String> b = new ArrayList<String>();
				if (!vital.split("@")[2].equals("X"))
					for (int i = 0; i < vital.split("@")[2].split("&").length; i++)
						b.add(vital.split("@")[2].split("&")[i]);

				ingusernames = new String[a.size()];
				ingnames = new String[a.size()];
				ingurls = new String[a.size()];

				erusernames = new String[b.size()];
				ernames = new String[b.size()];
				erurls = new String[b.size()];
				Log.d("u=liss", a + "" + b);
				int i = 0;
				for (String s : a) {
					ingusernames[i] = s;
					try {
						JSONObject user = ByteMessenger
								.jsonGet("https://graph.facebook.com/v2.1/"
										+ s.split("~")[1]
										+ "?fields=id,name,picture&access_token="
										+ access_token);
						ingnames[i] = user.getString("name");
						ingurls[i] = user.getJSONObject("picture")
								.getJSONObject("data").getString("url");
						i++;
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
				i = 0;
				for (String s : b) {
					Log.d("er usrname", s);
					erusernames[i] = s;
					try {
						JSONObject user = ByteMessenger
								.jsonGet("https://graph.facebook.com/v2.1/"
										+ s.split("~")[1]
										+ "?fields=id,name,picture&access_token="
										+ access_token);
						ernames[i] = user.getString("name");
						erurls[i] = user.getJSONObject("picture")
								.getJSONObject("data").getString("url");
						i++;
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					try {
						name.setText(userjson.getString("name"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Picasso.with(VitalsActivity.this)
							.load("https://graph.facebook.com/v2.1/"
									+ vital.split("@")[1].split("~")[1]
									+ "/picture?type=large&access_token="
									+ access_token).resize(100, 100)
							.transform(new RoundedTransformation(200, 0))
							.into(image);

					try {

						Picasso.with(VitalsActivity.this)
								.load(userjson.getJSONObject("cover")
										.getString("source")).fit()
								.centerCrop().into(cover);
						cover.setColorFilter(Color.argb(50, 0, 0, 0));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					followercount.setText(erusernames.length
							+ (erusernames.length == 1 ? " FOLLOWER"
									: " FOLLOWERS"));
					adapterViewPager = new MyPagerAdapter(
							getSupportFragmentManager(), ingnames,
							ingusernames, ingurls, ernames, erusernames, erurls);
					vpPager.setAdapter(adapterViewPager);

					pb.setVisibility(View.INVISIBLE);
					username.setVisibility(View.VISIBLE);
					followercount.setVisibility(View.VISIBLE);
					image.setVisibility(View.VISIBLE);
					cover.setVisibility(View.VISIBLE);
					name.setVisibility(View.VISIBLE);

				}
			});
			return null;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.vitals, menu);
		return true;
	}

	AlertDialog alertDialog = null;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			LayoutInflater li = LayoutInflater.from(this);
			View promptsView = li.inflate(R.layout.dialog_follow, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			alertDialogBuilder.setView(promptsView);
			final EditText userInput = (EditText) promptsView
					.findViewById(R.id.editTextDialogUserInputPoke);

			// set dialog message
			alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Log.d("Listview action", userInput
											.getText().toString());
									if (alertDialog.isShowing())
										alertDialog.dismiss();
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									alertDialog.dismiss();
									new FollowUser().execute(userInput
											.getText().toString());
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			// create alert dialog
			alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class MyPagerAdapter extends FragmentPagerAdapter {
		private static int NUM_ITEMS = 2;

		String names[];
		String usernames[];
		String urls[];
		String ernames[];
		String erusernames[];
		String erurls[];

		public MyPagerAdapter(
				android.support.v4.app.FragmentManager fragmentManager,
				String[] names, String[] usernames, String[] urls,
				String[] ernames, String[] erusernames, String[] erurls) {
			super(fragmentManager);
			this.names = names;
			this.usernames = usernames;
			this.urls = urls;
			this.ernames = ernames;
			this.erusernames = erusernames;
			this.erurls = erurls;

		}

		// Returns total number of pages
		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		// Returns the fragment to display for that page
		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			Log.d("FragmentDistributary", "" + Arrays.toString(usernames)
					+ Arrays.toString(erusernames));
			switch (position) {
			case 0: // Fragment # 0 - This will show FirstFragment
				return VFollowerFragment.newInstance(0, "Page # 1",
						erusernames, ernames, erurls);
			case 1: // Fragment # 0 - This will show FirstFragment different
					// title
				return VFollowingFragment.newInstance(1, "Page # 2", usernames,
						names, urls);
			default:
				return null;
			}
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "                      FOLLOWERS                      ";

			case 1:
				return "                      FOLLOWING                      ";

			default:
				break;
			}
			return null;
		}

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

			this.dialog = new ProgressDialog(VitalsActivity.this);
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
										VitalsActivity.this).getString(
										"cookie", ":(")));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (doc != null) {
				DBManager db = new DBManager(VitalsActivity.this);
				db.open();
				Long id = Long.parseLong(doc.split("~")[0]);
				db.insertFollowing(id, params[0] + "~" + doc.split("~")[1]);
				Log.d("FOLLOWING", doc + "" + params[0]);
				db.close();

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						fabButton = new FloatingActionButton.Builder(
								VitalsActivity.this)
								.withDrawable(
										getResources().getDrawable(
												R.drawable.ic_check_white_24dp))
								.withButtonColor(
										getResources().getColor(
												R.color.green_500))
								.withGravity(Gravity.TOP | Gravity.RIGHT)
								.withMargins(0, 258 - 35, 16, 0).create();

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

	public class Unfollow extends AsyncTask<String, Void, Void> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(VitalsActivity.this);
			this.dialog.setMessage("Unfollowing..");
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(Void result) {

		}

		@Override
		protected Void doInBackground(final String... params) {
			ArrayList<UUID> docs = null;
			try {
				docs = ByteMessenger.Unfollow(getCookie(),
						params[0].split("~")[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DBManager db = new DBManager(VitalsActivity.this);
			db.close();
			db.open();
			if (docs != null) {
				for (UUID s : docs) {
					db.deleteDoc(s.toString());
				}
				db.deleteFollowing(params[0]);

				if (dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						fabButton = new FloatingActionButton.Builder(
								VitalsActivity.this)
								.withDrawable(
										getResources().getDrawable(
												R.drawable.ic_check_white_24dp))
								.withButtonColor(
										getResources().getColor(
												R.color.green_500))
								.withGravity(Gravity.TOP | Gravity.RIGHT)
								.withMargins(0, 258 - 35, 16, 0).create();

					}
				});

			}
			db.close();
			return null;
		}

	}

	private UUID getCookie() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(VitalsActivity.this);
		String name = sharedPreferences.getString("cookie", null);
		Log.d("sess", name == null ? "null" : name);
		if (name != null)
			return UUID.fromString(name);
		else
			return null;

	}
}
