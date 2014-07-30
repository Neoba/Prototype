package com.neoba.syncpad;

import java.util.ArrayList;

import com.neoba.syncpad.ByteMessenger.Share;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ShareListActivity extends Activity {
	ShareAdapter sa;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_list);
		ListView slist=(ListView) findViewById(R.id.lvShares);
		Button add=(Button)findViewById(R.id.bSLAdd);
		final EditText user=(EditText)findViewById(R.id.etSLusername);
		sa=new ShareAdapter(this, R.layout.share_list_element, new ArrayList<Share>());
		slist.setAdapter(sa);
		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sa.add(new Share("@"+user.getText().toString()));
				user.setText("");
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.share_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
