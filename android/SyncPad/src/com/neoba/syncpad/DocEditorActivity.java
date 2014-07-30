package com.neoba.syncpad;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import com.neoba.syncpad.ByteMessenger.document;

import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class DocEditorActivity extends Activity {

	int rowid;
	EditText doceditor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doc_editor);
		doceditor=(EditText) findViewById(R.id.etDocument);
		DBManager db=new DBManager(this);
		db.open();
		rowid=getIntent().getExtras().getInt("rowid");
		String doc=null;
		try {
			doc = db.getDoc(rowid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VcdiffDecodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		doceditor.setText(doc);
		db.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.doc_editor_activity_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_sync) {
			Log.d(this.getClass().getCanonicalName(), "syncc");
			DBManager db=new DBManager(this);
			db.open();
			document doc=db.getDocument(rowid);
			try {
				doc.diff=new VcdiffEncoder(doc.dict, doceditor.getText().toString()).encode();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (VcdiffEncodeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			new DocEditor().execute(doc);
			db.close();
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	public class DocEditor extends	AsyncTask<document, Void,document> {
		private ProgressDialog dialog = new ProgressDialog(DocEditorActivity.this);

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			this.dialog.setMessage("Syncing this document..");
			this.dialog.show();
			super.onPreExecute();
		}


		@Override
		protected void onPostExecute(document result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			if(result!=null)
			{
	 			DBManager db=new DBManager(DocEditorActivity.this);
	 			db.open();
	 			db.updateContent(result);
	 			db.close();
			}
			
			super.onPostExecute(result);
		}


		@Override
		protected document doInBackground(document... params) {
			try {
				document new1=ByteMessenger.Editdoc(params[0], getCookie());
				return new1;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}



	}

}
