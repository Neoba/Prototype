package com.neoba.syncpad;

import java.util.ArrayList;
import java.util.UUID;

import com.neoba.syncpad.NotesList.NoteListAdapter;
import com.neoba.syncpad.SuggestedUsers.Follow;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
public class VFollowingFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    private ListView lvFollowings;
	static String[] names;
	static String[] usernames;
	static String[] urls;

    // newInstance constructor for creating fragment with arguments
    public static VFollowingFragment newInstance(int page, String title,String[] username,String[] name,String[] url) {
        VFollowingFragment fragmentFirst = new VFollowingFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        VFollowingFragment.names=name;
        VFollowingFragment.usernames=username;
        VFollowingFragment.urls=url;
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");  
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following, container, false);
        lvFollowings=(ListView)view.findViewById(R.id.lvUsersFollowing);
        FollowingAdapter fa=new FollowingAdapter(view.getContext(), VFollowingFragment.names, VFollowingFragment.usernames,  VFollowingFragment.urls);
        lvFollowings.setAdapter(fa);
        lvFollowings.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(getActivity(),VitalsActivity.class);
				if(usernames[arg2].split("~")[0].equals(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("username", "default")))
					startActivity(new Intent(getActivity(),UserActivity.class));
				else{
					i.putExtra("user",usernames[arg2]);
					startActivity(i);
				}
				
			}
		});
        lvFollowings.setOnItemLongClickListener(new OnItemLongClickListener() {



			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					final int arg2, long arg3) {
				final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
				b.setIcon(android.R.drawable.ic_dialog_alert);
				b.setMessage("Unfollow User?");
				b.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								DBManager db = new DBManager(getActivity());
								db.open();
								new Unfollow().execute(arg2+"~"+db.getFollowingUsernameFromRowid(arg2+1));
								db.close();
								
							}
						});
				b.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						});

				b.show();

				return false;
			}
        }); 
        return view;  
        
    }
    public class Unfollow extends AsyncTask<String, Void, Void>{
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(getActivity());
			this.dialog.setMessage("Unfollowing..");
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}
		}
		@Override
		protected Void doInBackground(final String... params) {
			ArrayList<UUID> docs = null;
			try {
				docs=ByteMessenger.Unfollow(getCookie(), params[0].split("~")[1]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DBManager db = new DBManager(getActivity());
			db.close();
			db.open();
			if(docs!=null){
				for(UUID s:docs){
					db.deleteDoc(s.toString());
				}
				db.deleteFollowing(params[0].split("~")[1]+"~"+params[0].split("~")[2]);
				int index=Integer.parseInt(params[0].split("~")[0]);
				String tem[] = new String[FollowerFragment.names.length-1];
				String tem1[] = new String[FollowerFragment.names.length-1];
				String tem2[] = new String[FollowerFragment.names.length-1];
				
				for(int i=0;i<index;i++)
					tem[i]=VFollowingFragment.names[i];
				for(int i=index+1;i<tem.length;i++)
					tem[i]=VFollowingFragment.names[i];
				
				
				for(int i=0;i<index;i++)
					tem1[i]=VFollowingFragment.usernames[i];
				for(int i=index+1;i<tem1.length;i++)
					tem1[i]=VFollowingFragment.usernames[i];
				
				
				for(int i=0;i<index;i++)
					tem2[i]=VFollowingFragment.urls[i];
				for(int i=index+1;i<tem.length;i++)
					tem2[i]=VFollowingFragment.urls[i];
				
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						
						FollowingAdapter fa=new FollowingAdapter(getActivity(), VFollowingFragment.names, VFollowingFragment.usernames,  VFollowingFragment.urls);
				        lvFollowings.setAdapter(fa);
						
					}
				});

			}
			db.close();
			return null;
		}
		
	}
    private UUID getCookie() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		String name = sharedPreferences.getString("cookie", null);
		Log.d("sess", name == null ? "null" : name);
		if (name != null)
			return UUID.fromString(name);
		else
			return null;

	}
    public class FollowingAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final String[] values;
		private final String[] usernames;
		private final String[] urls;

		public FollowingAdapter(Context context, String[] values,
				String[] usernames, String[] urls) {
			super(context, R.layout.suggestions_list, values);

			this.context = context;
			this.values = values;
			this.usernames = usernames;
			this.urls = urls;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.suggestions_list, parent,
					false);
			TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
			TextView textView2 = (TextView) rowView
					.findViewById(R.id.secondLine);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
			final ImageView followb = (ImageView) rowView
					.findViewById(R.id.ivFollow);
			followb.setVisibility(View.INVISIBLE);

//			if (followed.get(usernames[position]))
//				followb.setImageResource(R.drawable.ic_followok);

			final ProgressBar pb = (ProgressBar) rowView
					.findViewById(R.id.pbProfile);
			followb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
//					if (!followed.get(usernames[position])) {
//						Toast.makeText(FollowingFragment.this, "Following..",
//								Toast.LENGTH_SHORT).show();
//
//						followb.setVisibility(View.INVISIBLE);
//						pb.setVisibility(View.VISIBLE);
//						new Follow().execute(usernames[position]);
//					}
				}
			});

			textView.setText(values[position]);
			textView2.setText("@" + usernames[position]);
			
			Picasso.with(context).load(urls[position])
					.resize(70,70).transform(new RoundedTransformation(60, 0))
					.into(imageView);
			return rowView;
		}
	}
}
