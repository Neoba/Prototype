package com.neoba.syncpad;

import java.util.UUID;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class OfflineSyncService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		Toast.makeText(getApplicationContext(), "Service Created", 1).show();
		Log.d("Serv", "Crea");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Service Destroy", 1).show();
		Log.d("Serv", "Dest");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("Serv", "ok");
		
		new Ping().execute(0);
		return super.onStartCommand(intent, flags, startId);
	}
	public class Ping extends AsyncTask<Integer, Void, Void> {
		private ProgressDialog dialog;

		@Override
		protected Void doInBackground(final Integer... params) {

			try {
				ByteMessenger.Ping();
				Log.d("ok", "PONG");
				//send all the create notes
				
				DBManager db = new DBManager(OfflineSyncService.this);
				db.open();
				Cursor cursor=db.getAllUnsyncedCreateDocs();
			    cursor.moveToFirst();
			    while (!cursor.isAfterLast()) {
			      String id=cursor.getString(1);
			      if(ByteMessenger.createNote(
			    		  UUID.fromString(PreferenceManager.getDefaultSharedPreferences(OfflineSyncService.this).getString("cookie", "default")),
			    		  UUID.fromString(id)).get(0).get("result").equals("success")){
			    	  db.syncNote(id);
			      }
			      cursor.moveToNext();
			    }
				db.close();

			} catch (Exception e) {


				e.printStackTrace();
			}

			return null;
		}

	}
}