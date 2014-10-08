package com.neoba.syncpad;

import java.util.Arrays;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class FbLogin extends Activity{
	int flag;
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.login_view);
	        
	        final Session.NewPermissionsRequest newPermissionsRequest = new Session
	        	      .NewPermissionsRequest(this, Arrays.asList("user_friends","email"));
	        	   
	        
	        
	         flag=0;
	        
	        
	        Session.openActiveSession(this, true, new Session.StatusCallback() {

	  	      // callback when session changes state
	  	      @Override
	  	      public void call(Session session, SessionState state, Exception exception) {
	  	    	 
	  	        if (session.isOpened()) {
	  	        	final Session s=session;
	  	        	if(flag==0)
	  	        		session.requestNewReadPermissions(newPermissionsRequest);  
	  	        	flag=1;
	  	          // make request to the /me API
	  	          Request.newMeRequest(session, new Request.GraphUserCallback() {

	  	            // callback after Graph API response with user object
	  	        	  
	  	            @Override
	  	            public void onCompleted(GraphUser user, Response response) {
	  	              if (user != null) {
	  	            	
	  	                TextView welcome = (TextView) findViewById(R.id.textView1);
	  	                welcome.setText("Hello, " + user.getName() + " ");
	  	                EditText e=(EditText)findViewById(R.id.editText1);
	  	                e.setText(s.getAccessToken()+" n");
	  	              
	  	                
	  	              }
	  	            
	  	            }
	  	          }).executeAsync();
	  	        }
	  	       
	  	        	 
	  	        
//	  	      else if (!session.isOpened()) {
//	  	        // Ask for username and password
//	  	        OpenRequest op = new Session.OpenRequest(MainActivity.this);
	//
//	  	        op.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
//	  	        op.setCallback(null);
	//
//	  	        List<String> permissions = new ArrayList<String>();
//	  	        permissions.add("publish_stream");
//	  	        permissions.add("user_likes");
//	  	        permissions.add("email");
//	  	        permissions.add("user_birthday");
//	  	        op.setPermissions(permissions);
	//
//	  	       
//	  	        session.openForPublish(op);
//	  	    }
	  	       
	  	      }
	  	    });
	    }
	   

	    @Override
	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	      super.onActivityResult(requestCode, resultCode, data);
	      if(requestCode==1)
	      {
	    	 finish(); 
	      }
	      Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	     
	    }


}
