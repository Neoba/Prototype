package com.neoba.syncpad;

import java.lang.reflect.Field;
import java.util.UUID;

import com.neoba.syncpad.ByteMessenger.document;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class DocsListActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_docs_list);
		getOverflowMenu();
		DBManager db = new DBManager(this);
		db.open();

//		this.setListAdapter(new SimpleCursorAdapter(this,
//				R.layout.docs_list_element, db.getAllDocs(), new String[] {
//						"title", "date" }, new int[] { R.id.title_entry,
//						R.id.number_entry },
//				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));
		this.setListAdapter(new DocsCursorAdapter(this,R.layout.docs_list_element,db.getAllDocs(), new String[] {
						"title", "date" }, new int[] { R.id.title_entry,
						R.id.number_entry }));

		db.close();
	}
	 private void getOverflowMenu() {

		    try {
		       ViewConfiguration config = ViewConfiguration.get(this);
		       Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
		       if(menuKeyField != null) {
		           menuKeyField.setAccessible(true);
		           menuKeyField.setBoolean(config, false);
		       }
		   } catch (Exception e) {
		       e.printStackTrace();
		   }
		 }
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Log.d(this.getClass().getName(), "clicked list element at " + position);
		Intent in = new Intent("com.neoba.syncpad.DOCVIEWER");
		
		in.putExtra("rowid", position + 1);
		DBManager db = new DBManager(this);
		db.open();
		in.putExtra("docid", db.getId(position+1));
		db.close();
		startActivityForResult(in, 0);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.docs_list, menu);
		return true;
	}
	AlertDialog alertDialog=null;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_logout) {
			
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	SharedPreferences sharedPreferences = PreferenceManager
						.getDefaultSharedPreferences(DocsListActivity.this);
				Editor editor = sharedPreferences.edit();
				editor.remove("cookie");
				editor.commit();
				DBManager db = new DBManager(DocsListActivity.this);
				db.open();
				db.Truncate();
				db.close();
				
				DocsListActivity.this.finish();
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //No button clicked
			            break;
			        }
			    }
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
			    .setNegativeButton("No", dialogClickListener).show();
			
			
			
		}
		if (id == R.id.action_users) {
			startActivity(new Intent("com.neoba.syncpad.USERS"));
		}
		if (id == R.id.action_create) {
			LayoutInflater li = LayoutInflater.from(this);
			View promptsView = li.inflate(R.layout.dialog_create, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setView(promptsView);
			final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

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
									new CreateDocument().execute(userInput.getText().toString());
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
		}
		return super.onOptionsItemSelected(item);
	}

	public class CreateDocument extends AsyncTask<String, Void, document> {
		private ProgressDialog dialog;

		@Override
		protected void onPostExecute(document result) {
			super.onPostExecute(result);
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			
			DBManager db = new DBManager(DocsListActivity.this);
			db.open();

			DocsListActivity.this.setListAdapter(new DocsCursorAdapter(DocsListActivity.this,R.layout.docs_list_element,db.getAllDocs(), new String[] {
				"title", "date" }, new int[] { R.id.title_entry,
				R.id.number_entry }));

			db.close();
			
		}

		@Override
		protected void onPreExecute() {
			if(alertDialog.isShowing())
				alertDialog.dismiss();
			this.dialog= new ProgressDialog(DocsListActivity.this);
			this.dialog.setMessage("Creating a new document..");
			this.dialog.show();
		}

		@Override
		protected document doInBackground(String... params) {

			document doc=null;
			try {
				doc = ByteMessenger.CreateDoc(params[0],getCookie());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(doc!=null){
				DBManager db=new DBManager(DocsListActivity.this);
				db.open();
				db.insertDoc(doc);
				db.close();
			}else
			{
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
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
	private UUID getCookie() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String name = sharedPreferences.getString("cookie", null);
		Log.d("sess", name == null ? "null" : name);
		if (name != null)
			return UUID.fromString(name);
		else
			return null;

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		   Intent intent = getIntent();
		    finish();
		    startActivity(intent);
		
	}
}
