package com.neoba.syncpad;

import java.io.IOException;

import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DocViewerActivity extends Activity {

	int rowid;
	String docid=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doc_viewer);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//Long id=;
		TextView document_text=(TextView) findViewById(R.id.tvDocumentContent);
		document_text.setMovementMethod(new ScrollingMovementMethod());
		DBManager db=new DBManager(this);
		db.open();
		rowid=getIntent().getExtras().getInt("rowid");
		docid=getIntent().getExtras().getString("docid");
		String doc=null;
		try {
			doc = db.getDoc(docid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VcdiffDecodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		document_text.setText(doc);
		db.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.doc_viewer_activity_actions, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		   Intent intent = getIntent();
		    finish();
		    startActivity(intent);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_edit) {
			
			Intent in=new Intent("com.neoba.syncpad.DOCEDITOR");
			in.putExtra("rowid",rowid);
			in.putExtra("docid",docid);
			startActivityForResult(in, 0);
			return true;
		}
		if (id == R.id.action_share) {
			
			Intent in=new Intent("com.neoba.syncpad.SHARELIST");
			in.putExtra("docid",docid);
			startActivityForResult(in, 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
