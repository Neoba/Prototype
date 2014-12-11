package com.neoba.syncpad;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

import android.app.ActionBar;
import android.util.Log;


public class Postman {
	 static ByteBuffer post(ByteBuffer a) throws Exception {
		 
	        String url = "http://192.168.1.103:2811";
	        String url2 = "http://192.168.1.102:2811";
	        HttpURLConnection con;
	        try{
	        	URL obj = new URL(url);
	        	con = (HttpURLConnection) obj.openConnection();
	        }catch(Exception e){
	        	URL obj = new URL(url2);
	        	con = (HttpURLConnection) obj.openConnection();
	        }
	        con.setRequestMethod("POST");
	        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
	        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	        con.setRequestProperty("Content-Type", "application/octet-stream");
	        con.setConnectTimeout(10000);
	        
	        a.flip();
	        DataOutputStream wr=null;
	        con.setDoOutput(true);
	        try{
	        wr = new DataOutputStream(con.getOutputStream());
	        }
	        catch(Exception e){
	        	//Log.d("HTTP err",""+e);
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
