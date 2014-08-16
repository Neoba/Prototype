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


public class FollowingFragment extends Fragment {

	private static final String ARG_SECTION_NUMBER = "section_number";


	public static FollowingFragment newInstance(int sectionNumber) {
		FollowingFragment fragment = new FollowingFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public FollowingFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_following, container,
				false);
		DBManager db = new DBManager(rootView.getContext());
		db.open();
		ListView followers=(ListView)rootView.findViewById(R.id.lvFollowing);
		followers.setAdapter(new SimpleCursorAdapter(rootView.getContext(),
				R.layout.following_list_element, db.getAllFollowing(), new String[] {"username" }, new int[] { R.id.fnleUsername },
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));
		db.close();
		//followers.setAdapter(new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, new String[] {"@hans","@gerrard","@docshared","@evernote"}));
		return rootView;
	}
}