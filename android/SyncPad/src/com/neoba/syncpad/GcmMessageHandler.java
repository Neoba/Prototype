package com.neoba.syncpad;

import java.io.IOException;
import java.util.UUID;

import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.neoba.syncpad.ByteMessenger.document;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

public class GcmMessageHandler extends IntentService {

	String title;
	String content;
	String action;
	private Handler handler;

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
		showToast();
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
						,(byte)jaction.getInt("permission"));
				db.insertDoc(pushed);
				Log.d("pushre",pushed.toString());
				db.close();
			}else if(jaction.getString("type").equals("permission_revoke")){
				DBManager db = new DBManager(this);
				db.open();
				db.deleteDoc(jaction.getString("id"));
				db.close();
			}else if(jaction.getString("type").equals("edit")){
				DBManager db = new DBManager(this);
				db.open();
				db.editDoc(jaction.getString("id"),Base64.decode(jaction.getString("diff")),jaction.getInt("age"));
				db.close();
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

	public void showToast() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(),action, Toast.LENGTH_LONG)
						.show();
				showNotification(title,content, getApplicationContext());
			}
		});

	}

	private void showNotification(String title,String content, Context ctx) {

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.common_signin_btn_icon_light, "SyncPad",System.currentTimeMillis());

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,new Intent(ctx, MainActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(ctx, title,content, contentIntent);

		// Send the notification.
		notificationManager.notify("SyncPad", 0, notification);
	}
}