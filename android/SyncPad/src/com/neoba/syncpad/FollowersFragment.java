package com.neoba.syncpad;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class FollowersFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static FollowersFragment newInstance(int sectionNumber) {
		FollowersFragment fragment = new FollowersFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);

		
		return fragment;
	}

	public FollowersFragment() {
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_followers, container,false);
		DBManager db = new DBManager(rootView.getContext());
		db.open();
		ListView followers=(ListView)rootView.findViewById(R.id.lvFollowing);
		followers.setAdapter(new SimpleCursorAdapter(rootView.getContext(),
				R.layout.follower_list_element, db.getAllFollower(), new String[] {"username" }, new int[] { R.id.feleUsername },
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));
		db.close();
		return rootView;
	}
}
