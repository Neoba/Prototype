package com.neoba.syncpad;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

import android.util.Log;


public class Postman {
	 static ByteBuffer post(ByteBuffer a) throws Exception {

	        String url = "http://10.42.0.53:2811";
	        URL obj = new URL(url);
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

	        //add reuqest header
	        con.setRequestMethod("POST");
	        con.setRequestProperty("User-Agent", "Android 7.9 Upsidedown cake");
	        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	        con.setRequestProperty("Content-Type", "application/octet-stream");
	        con.setConnectTimeout(10000);
	        // Send post request
	        a.flip();
	        DataOutputStream wr=null;
	        con.setDoOutput(true);
	        try{
	        wr = new DataOutputStream(con.getOutputStream());
	        }
	        catch(Exception e){
	        	Log.d("HTTTTTP",""+e);
	        	return null;
	        }
	        byte[] p=a.array();
	        wr.write(p);
	        wr.flush();
	        wr.close();
	        
	        BufferedInputStream in = new BufferedInputStream(con.getInputStream());
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        int next = in.read();
	        while (next > -1) {
	            bos.write(next);
	            next = in.read();
	        }
	        bos.flush();
	        byte[] result = bos.toByteArray();
	        return ByteBuffer.wrap(result);

	    }


}
