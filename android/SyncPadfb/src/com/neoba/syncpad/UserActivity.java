package com.neoba.syncpad;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
		
		username=(TextView)findViewById(R.id.tvSharedUsers);
		followercount=(TextView)findViewById(R.id.tvFollowerCount);
		image=(ImageView)findViewById(R.id.ivPicture);
		cover=(ImageView)findViewById(R.id.ivCover);
		name=(TextView)findViewById(R.id.tvTimeStamp);
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
	AlertDialog alertDialog=null;
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
		        
		        db.open();
		        c=db.getAllFollowerUsernames();
		        c.moveToFirst();
		        ArrayList<String> b=new ArrayList<String>();
		        while(!c.isAfterLast())   {
		        	b.add(c.getString(1));
		        	c.moveToNext();
		        }
		        db.close();
		        
		        Log.d("FBLOGINFF",a+""+b);
		       
		        final String[] ingusernames=new String[a.size()];
		        final String[] ingnames=new String[a.size()];
		        final String[] ingurls=new String[a.size()];
		        
		        final String[] erusernames=new String[b.size()];
		        final String[] ernames=new String[b.size()];
		        final String[] erurls=new String[b.size()];
		        
		        int i=0;
		        for(String s:a)
		        {
		        	ingusernames[i]=s.split("~")[0];
		        	try {
						JSONObject user=ByteMessenger.jsonGet("https://graph.facebook.com/v2.1/"+s.split("~")[1]+"?fields=id,name,picture&access_token="+access_token);
						ingnames[i]=user.getString("name");
						ingurls[i]=user.getJSONObject("picture").getJSONObject("data").getString("url");
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
		        i=0;
		        for(String s:b)
		        {
		        	Log.d("er usrname",s);
		        	erusernames[i]=s.split("~")[0];
		        	try {
						JSONObject user=ByteMessenger.jsonGet("https://graph.facebook.com/v2.1/"+s.split("~")[1]+"?fields=id,name,picture&access_token="+access_token);
						ernames[i]=user.getString("name");
						erurls[i]=user.getJSONObject("picture").getJSONObject("data").getString("url");
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
						
						followercount.setText(ingusernames.length+(ingusernames.length==1?" FOLLOWER":" FOLLOWERS"));
					    adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(),ingnames,ingusernames,ingurls,ernames,erusernames,erurls);
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
		    	String ernames[];
		    	String erusernames[];
		    	String erurls[];
		    	
		        public MyPagerAdapter(android.support.v4.app.FragmentManager fragmentManager,String[] names,String[] usernames,String[] urls,String[] ernames,String[] erusernames,String[] erurls) {
		        	super(fragmentManager);
		        	this.names=names;
		            this.usernames=usernames;
		            this.urls=urls;
		        	this.ernames=ernames;
		            this.erusernames=erusernames;
		            this.erurls=erurls;
		        	
		        }
				// Returns total number of pages
		        @Override
		        public int getCount() {
		            return NUM_ITEMS;
		        }

		        // Returns the fragment to display for that page
		        @Override
		        public android.support.v4.app.Fragment getItem(int position) {
		        	Log.d("FragmentDistributary",""+Arrays.toString(usernames)+Arrays.toString(erusernames))	;
		            switch (position) {
		            case 0: // Fragment # 0 - This will show FirstFragment
		                return FollowerFragment.newInstance(0, "Page # 1",erusernames,ernames,erurls);
		            case 1: // Fragment # 0 - This will show FirstFragment different title
		                return FollowingFragment.newInstance(1, "Page # 2",usernames,names,urls);
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
	 
		public class FollowUser extends AsyncTask<String, Void, Long> {
			private ProgressDialog dialog;

			@Override
			protected void onPostExecute(Long result) {
				super.onPostExecute(result);
				if (dialog.isShowing()) {
					dialog.dismiss();
					dialog=null;
				}

				
			}

			@Override
			protected void onPreExecute() {

				this.dialog= new ProgressDialog(UserActivity.this);
				this.dialog.setMessage("Following user..");
				this.dialog.show();
			}

			@Override
			protected Long doInBackground(String... params) {

				String doc=null;
				try {
					doc = ByteMessenger.FollowUser(params[0], UUID.fromString(PreferenceManager.getDefaultSharedPreferences(UserActivity.this).getString("cookie", ":(")));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if(doc!=null){
					DBManager db=new DBManager(UserActivity.this);
					db.open();
					Long id=Long.parseLong(doc.split("~")[0]);
					db.insertFollowing(id,params[0]+"~"+doc.split("~")[1]);
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


}
