/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * Sample Smack implementation of a client for GCM Cloud Connection Server. This
 * code can be run as a standalone CCS client.
 *
 * <p>For illustration purposes only.
 */
public class GoogleCloudMessager
{
   public static boolean Push(List<String> regs,String title,String content,JSONObject action) throws Exception
    {
        System.out.println( "Sending POST to GCM" );
        //String ou1t=" { \"data\": { \"title\": \"without a doubt the best out put\",\"message\": \"Test Message\"},\"registration_ids\": [\"APA91bG5FNex2MbGC0yRXQ3eg_i8gtDO4c4VHGPvsO3q3ZKzOLKd4GuL6sSu20uEqb0s2or43C6TcnD2JFZSan2eTEOLb3ygqDgzWiWhPe5HLf_C6zaKC6NDtBuzJ_8FaWE-OXBbeCmSXX3nSsDwS5-Wl4Oz5SnhKA\"] } ";
        JSONObject data=new JSONObject();
        JSONObject finalo=new JSONObject();
        data.put("title",title);
        data.put("content",content);
        data.put("action", action);
        JSONArray regids=new JSONArray(regs);
        finalo.put("data", data);
        finalo.put("registration_ids", regids);
        System.out.println(finalo.toString());
        ByteBuffer a=sendPost(ByteBuffer.wrap(finalo.toString().getBytes()));
        if(a==null)
            return false;
        System.out.println(new String(a.array()));
        return true;
        
    }
    static ByteBuffer sendPost(ByteBuffer a) throws Exception {

        String url = "https://android.googleapis.com/gcm/send";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization","key=AIzaSyC7EVwGwadMtVj_XZMBbhsp1YT7b4_Bcf4");
        con.setRequestProperty("Content-Type", "application/json");
        // Send post request
        a.flip();
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.write(a.array());
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        if(responseCode==400)
            return null;
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