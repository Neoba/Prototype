package com.neoba.syncpad;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

public class DebugActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] values = new String[] { "Poke" };

		    final ArrayList<String> list = new ArrayList<String>();
		    for (int i = 0; i < values.length; ++i) {
		      list.add(values[i]);
		    }
		    final StableArrayAdapter adapter = new StableArrayAdapter(this,
		        android.R.layout.simple_list_item_1, list);
		    setListAdapter(adapter);
		    getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

		        @Override
		        public void onItemClick(AdapterView<?> parent, final View view,
		            int position, long id) {
		        		if(position==0){
		        			AlertDialog.Builder alert = new AlertDialog.Builder(DebugActivity.this);

		        			alert.setTitle("Poke");
		        			alert.setMessage("This person will get a notification");

		        			// Set an EditText view to get user input 
		        			final EditText input = new EditText(DebugActivity.this);
		        			alert.setView(input);

		        			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        			public void onClick(DialogInterface dialog, int whichButton) {
		        			  String value = input.getText().toString();
		        			  // Do something with value!
		        			  
		        			  }
		        			});

		        			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        			  public void onClick(DialogInterface dialog, int whichButton) {
		        			    // Canceled.
		        			  }
		        			});

		        			alert.show();
		        		}
		        }

		      });
	}
	private class StableArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public StableArrayAdapter(Context context, int textViewResourceId,
	        List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }

	    @Override
	    public long getItemId(int position) {
	      String item = getItem(position);
	      return mIdMap.get(item);
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }

	  }

}
