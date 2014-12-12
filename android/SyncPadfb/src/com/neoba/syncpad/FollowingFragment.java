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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
public class FollowingFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    private ListView lvFollowings;
	static String[] names;
	static String[] usernames;
	static String[] urls;

    // newInstance constructor for creating fragment with arguments
    public static FollowingFragment newInstance(int page, String title,String[] username,String[] name,String[] url) {
        FollowingFragment fragmentFirst = new FollowingFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        FollowingFragment.names=name;
        FollowingFragment.usernames=username;
        FollowingFragment.urls=url;
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
        FollowingAdapter fa=new FollowingAdapter(view.getContext(), FollowingFragment.names, FollowingFragment.usernames,  FollowingFragment.urls);
        lvFollowings.setAdapter(fa);

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
								new Unfollow().execute(db.getFollowingUsernameFromRowid(arg2+1).split("~")[0]);
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
		protected Void doInBackground(String... params) {
			ArrayList<UUID> docs = null;
			try {
				docs=ByteMessenger.Unfollow(getCookie(), params[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DBManager db = new DBManager(getActivity());
			db.open();
			if(docs!=null){
				for(UUID s:docs){
					db.deleteDoc(s.toString());
				}
				
				db.deleteFollowing(params[0]);
					
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
