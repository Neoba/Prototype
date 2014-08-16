package com.neoba.syncpad;

import java.util.Locale;
import java.util.UUID;

import com.neoba.syncpad.ByteMessenger.document;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FollActivity extends Activity implements ActionBar.TabListener {

	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_foll);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.foll, menu);
		return true;
	}
	AlertDialog alertDialog=null;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_poke) {

			LayoutInflater li = LayoutInflater.from(this);
			View promptsView = li.inflate(R.layout.dialog_follow, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			alertDialogBuilder.setView(promptsView);
			final EditText userInput = (EditText) promptsView
					.findViewById(R.id.editTextDialogUserInput);

			// set dialog message
			alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Log.d("Listview action", userInput.getText().toString());
									if(alertDialog.isShowing())
										alertDialog.dismiss();
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									alertDialog.dismiss();
									new PokeUser().execute(userInput.getText().toString());
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
		if (id == R.id.action_follow) {
			LayoutInflater li = LayoutInflater.from(this);
			View promptsView = li.inflate(R.layout.dialog_follow, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			alertDialogBuilder.setView(promptsView);
			final EditText userInput = (EditText) promptsView
					.findViewById(R.id.editTextDialogUserInput);

			// set dialog message
			alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Log.d("Listview action", userInput.getText().toString());
									if(alertDialog.isShowing())
										alertDialog.dismiss();
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									alertDialog.dismiss();
									new FollowUser().execute(userInput.getText().toString());
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

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			if(position==0)
				return FollowersFragment.newInstance(position + 1);
			else
				return FollowingFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return "FOLLOWERS";
			case 1:
				return "FOLLOWING";
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
			}
			
			startActivity(new Intent("com.neoba.syncpad.USERS"));
			FollActivity.this.finish();
			
		}

		@Override
		protected void onPreExecute() {
			if(alertDialog.isShowing())
				alertDialog.dismiss();
			this.dialog= new ProgressDialog(FollActivity.this);
			this.dialog.setMessage("Following user..");
			this.dialog.show();
		}

		@Override
		protected Long doInBackground(String... params) {

			Long doc=null;
			try {
				doc = ByteMessenger.FollowUser(params[0], getCookie());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			if(doc!=null){
				DBManager db=new DBManager(FollActivity.this);
				db.open();
				db.insertFollowing(doc,params[0]);
				Log.d("FOLLOWING",doc+""+params[0]);
				db.close();
			}else
			{

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				   runOnUiThread(new Runnable(){

				          @Override
				          public void run(){
				        	  Toast.makeText(getApplicationContext(),"Sorry.. Can't connect to our server :(", Toast.LENGTH_LONG).show();
				          }
				       });
				
			}

		
			return null;
		}

		
	}

	public class PokeUser extends AsyncTask<String, Void, Long> {
		private ProgressDialog dialog;

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			
			startActivity(new Intent("com.neoba.syncpad.USERS"));
			FollActivity.this.finish();
			
		}

		@Override
		protected void onPreExecute() {
			if(alertDialog.isShowing())
				alertDialog.dismiss();
			this.dialog= new ProgressDialog(FollActivity.this);
			this.dialog.setMessage("Poking user..");
			this.dialog.show();
		}

		@Override
		protected Long doInBackground(String... params) {

			Long doc=null;
			try {
				doc = ByteMessenger.PokeUser(params[0], getCookie());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			if(doc!=null){

				Log.d("poked",doc+""+params[0]);
			}else
			{

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				   runOnUiThread(new Runnable(){

				          @Override
				          public void run(){
				        	  Toast.makeText(getApplicationContext(),"Sorry.. Can't connect to our server :(", Toast.LENGTH_LONG).show();
				          }
				       });
				
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


}
