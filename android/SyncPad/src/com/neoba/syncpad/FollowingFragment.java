package com.neoba.syncpad;

import java.util.ArrayList;
import java.util.UUID;

import com.neoba.syncpad.DocsListActivity.Delete;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
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
		followers.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
				b.setIcon(android.R.drawable.ic_dialog_alert);
				b.setMessage("Unfollow User?");
				b.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								DBManager db = new DBManager(getActivity());
								db.open();
								new Unfollow().execute(db.getFollowingUsernameFromRowid(position+1));
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

				return true;
			}
			
		});
		followers.setAdapter(new SimpleCursorAdapter(rootView.getContext(),
				R.layout.following_list_element, db.getAllFollowing(), new String[] {"username" }, new int[] { R.id.fnleUsername },
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));
		db.close();
		//followers.setAdapter(new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, new String[] {"@hans","@gerrard","@docshared","@evernote"}));
		return rootView;
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
}