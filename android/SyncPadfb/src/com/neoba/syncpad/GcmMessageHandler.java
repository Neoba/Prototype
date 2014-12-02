package com.neoba.syncpad;


import java.io.IOException;
import java.util.UUID;

import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.neoba.syncpad.ByteMessenger.document;
import com.squareup.otto.Bus;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

public class GcmMessageHandler extends IntentService {

	String title;
	String content;
	String action;
	private Handler handler;
	static String ACTIONNOTELISTUPDATE="com.neoba.syncpad.NOTELISTUPDATE";
	
	public GcmMessageHandler() {
		super("GcmMessageHandler");
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		handler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		title = extras.getString("title");
		content = extras.getString("content");
		action= extras.getString("action");
		showNotification(title,content, getApplicationContext());
		//showToast();
		Log.i("GCM","Received : (" + messageType + ")  "+ extras.getString("title"));

		try {
			JSONObject jaction=new JSONObject(action);
			if(jaction.getString("type").equals("poke"))
				Log.d("GCMPOKE",jaction.getString("username"));
			else if(jaction.getString("type").equals("follow")){
				DBManager db = new DBManager(this);
				db.open();
				db.insertFollower(Long.parseLong( jaction.getString("id")), jaction.getString("username"));
				db.close();

			}else if(jaction.getString("type").equals("permission_grant")){
				
				Log.d("GCMPOKE",jaction.toString());
				DBManager db = new DBManager(this);
				db.open();
				document pushed=new document(jaction.getString("id"),
						jaction.getString("title")
						,Base64.decode(jaction.getString("diff"))
						,jaction.getInt("age")
						,jaction.getString("dict")
						,(byte)jaction.getInt("permission"),false,0);
				db.insertDocFromDigest(pushed);
				Log.d("pushre",pushed.toString());
				db.close();
				notelistupdate();

			}else if(jaction.getString("type").equals("permission_revoke")){
				DBManager db = new DBManager(this);
				db.open();
				db.deleteDoc(jaction.getString("id"));
				db.close();
				notelistupdate();

			}else if(jaction.getString("type").equals("edit")){
				DBManager db = new DBManager(this);
				db.open();
				db.editDoc(jaction.getString("id"),Base64.decode(jaction.getString("diff")),jaction.getInt("age"));
				db.close();
				notelistupdate();

			}else if(jaction.getString("type").equals("delete")){
				DBManager db = new DBManager(this);
				db.open();
				db.deleteDoc(jaction.getString("id"));
				db.close();
				notelistupdate();
				
			}else if(jaction.getString("type").equals("user_deleted")){
				DBManager db = new DBManager(this);
				db.open();
				db.clearPermissions(jaction.getString("docid"));
				db.close();
				notelistupdate();

			}else if(jaction.getString("type").equals("unfollowed")){
				
				DBManager db = new DBManager(this);
				db.open();
				JSONArray docs=jaction.getJSONArray("docs");
				for(int i=0;i<docs.length();i++)
					db.clearPermissions(docs.getString(i),jaction.getString("userid"));
				db.deleteFollower(jaction.getString("userid"));
				db.close();
				notelistupdate();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VcdiffDecodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VcdiffEncodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		WakefulBroadcastReceiver.completeWakefulIntent(intent);

	}
private void notelistupdate(){
	Intent broadcastIntent = new Intent();
    broadcastIntent.setAction(ACTIONNOTELISTUPDATE);
    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
    sendBroadcast(broadcastIntent);
}
	public void showToast() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(),action, Toast.LENGTH_LONG)
						.show();
				
			}
		});

	}

	private void showNotification(String title,String content, Context ctx) {

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.ic_launcher, "SyncPad",System.currentTimeMillis());

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,new Intent(ctx, MainActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(ctx, title,content, contentIntent);

		// Send the notification.
		notificationManager.notify("SyncPad", 0, notification);
		
		try {
		    Uri rt = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), rt);
		    r.play();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
}