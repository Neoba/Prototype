package com.neoba.syncpad;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.*;
import com.facebook.model.GraphObject;
import com.squareup.picasso.Picasso;
public class UserActivity extends FragmentActivity {

	TextView name;
	TextView followercount;
	TextView username;
	ImageView image,cover;
	ProgressBar pb;
	FragmentPagerAdapter adapterViewPager;
	ViewPager vpPager ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		vpPager = (ViewPager) findViewById(R.id.vpPager);
//        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(),new String[] {},new String[] {},new String[] {});
//        vpPager.setAdapter(adapterViewPager);
		
		username=(TextView)findViewById(R.id.tvUsername);
		followercount=(TextView)findViewById(R.id.tvFollowerCount);
		image=(ImageView)findViewById(R.id.ivPicture);
		cover=(ImageView)findViewById(R.id.ivCover);
		name=(TextView)findViewById(R.id.tvName);
		pb=(ProgressBar)findViewById(R.id.pbProfileLoad);
		
		username.setVisibility(View.INVISIBLE);
		image.setVisibility(View.INVISIBLE);
		followercount.setVisibility(View.INVISIBLE);
		cover.setVisibility(View.INVISIBLE);
		name.setVisibility(View.INVISIBLE);
		
		username.setText("@"+PreferenceManager.getDefaultSharedPreferences(UserActivity.this).getString("username", "default"));
		new UserFetch().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.user, menu);
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
	
	public class UserFetch extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			
			final String access_token=PreferenceManager.getDefaultSharedPreferences(UserActivity.this).getString("access_token", "defaultStringIfNothingFound");
			try {
				final JSONObject userjson=ByteMessenger.jsonGet("https://graph.facebook.com/v2.1/me?fields=cover,name,picture&access_token="+access_token);
				DBManager db=new DBManager(UserActivity.this);
		        db.open();
		        Cursor c=db.getAllFollowingUsernames();
		        c.moveToFirst();
		        ArrayList<String> a=new ArrayList<String>();
		        while(!c.isAfterLast())   {
		        	a.add(c.getString(1));
		        	c.moveToNext();
		        }
		        db.close();
		        Log.d("FBLOGINFF",a+"");
		       
		        final String[] usernames=new String[a.size()];
		        final String[] names=new String[a.size()];
		        final String[] urls=new String[a.size()];
		        int i=0;
		        for(String s:a)
		        {
		        	usernames[i]=s.split("~")[0];
		        	try {
						JSONObject user=ByteMessenger.jsonGet("https://graph.facebook.com/v2.1/"+s.split("~")[1]+"?fields=id,name,picture&access_token="+access_token);
						names[i]=user.getString("name");
						urls[i]=user.getJSONObject("picture").getJSONObject("data").getString("url");
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
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						try {
							name.setText(userjson.getString("name"));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Picasso.with(UserActivity.this).load("https://graph.facebook.com/v2.1/me/picture?type=square&access_token="+access_token).transform(new RoundedTransformation(2000,0))
								.into(image);
						try {
							Picasso.with(UserActivity.this).load(userjson.getJSONObject("cover").getString("source")).fit().centerCrop().into(cover);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						followercount.setText(usernames.length+(usernames.length==1?" FOLLOWER":" FOLLOWERS"));
					    adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(),names,usernames,urls);
					    vpPager.setAdapter(adapterViewPager);
						
						
						pb.setVisibility(View.INVISIBLE);
						username.setVisibility(View.VISIBLE);
						followercount.setVisibility(View.VISIBLE);
						image.setVisibility(View.VISIBLE);
						cover.setVisibility(View.VISIBLE);
						name.setVisibility(View.VISIBLE);
						
					}
				});
				

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
			
			return null;
		}

		
	}
	
	
	 public static class MyPagerAdapter extends FragmentPagerAdapter {
		    private static int NUM_ITEMS = 2;

		    	String names[];
		    	String usernames[];
		    	String urls[];
		    	
		        public MyPagerAdapter(android.support.v4.app.FragmentManager fragmentManager,String[] names,String[] usernames,String[] urls) {
		        	super(fragmentManager);
		        	this.names=names;
		            this.usernames=usernames;
		            this.urls=urls;
		        	
		        }
				// Returns total number of pages
		        @Override
		        public int getCount() {
		            return NUM_ITEMS;
		        }

		        // Returns the fragment to display for that page
		        @Override
		        public android.support.v4.app.Fragment getItem(int position) {
		            switch (position) {
		            case 0: // Fragment # 0 - This will show FirstFragment
		                return FollowerFragment.newInstance(0, "Page # 1",usernames,names,urls);
		            case 1: // Fragment # 0 - This will show FirstFragment different title
		                return FollowerFragment.newInstance(1, "Page # 2",usernames,names,urls);
		            default:
		                return null;
		            }
		        }

		        // Returns the page title for the top indicator
		        @Override
		        public CharSequence getPageTitle(int position) {
		            switch (position) {
					case 0:
						return "FOLLOWERS";

					case 1:
						return "FOLLOWING";

					default:
						break;
					}
					return null;
		        }

		    }


}
