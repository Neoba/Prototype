package com.neoba.syncpad;

import java.util.ArrayList;

import com.neoba.syncpad.SuggestedUsers.Follow;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
public class VFollowerFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    private ListView lvFollowers;
	static String[] names;
	static String[] usernames;
	static String[] urls;

    // newInstance constructor for creating fragment with arguments
    public static VFollowerFragment newInstance(int page, String title,String[] username,String[] name,String[] url) {
        VFollowerFragment fragmentFirst = new VFollowerFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        VFollowerFragment.names=name;
        VFollowerFragment.usernames=username;
        VFollowerFragment.urls=url;
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
        View view = inflater.inflate(R.layout.fragment_follower, container, false);
        lvFollowers=(ListView)view.findViewById(R.id.lvUsersFollowing);
        FollowerAdapter fa=new FollowerAdapter(view.getContext(), VFollowerFragment.names, VFollowerFragment.usernames,  VFollowerFragment.urls);
        lvFollowers.setAdapter(fa);
        lvFollowers.setOnItemClickListener(new OnItemClickListener() {

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
        return view;
        
        
        
    }
	
    public class FollowerAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final String[] values;
		private final String[] usernames;
		private final String[] urls;

		public FollowerAdapter(Context context, String[] values,
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
//						Toast.makeText(FollowerFragment.this, "Following..",
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
