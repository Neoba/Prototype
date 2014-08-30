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
import java.util.UUID;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class GoogleCloudMessager {

    static Logger logger = Logger.getLogger(GoogleCloudMessager.class);
    static UUID session;

    public static boolean Push(UUID sess, List<String> regs, String title, String content, JSONObject action) throws Exception {
        logger.debug(sess + " GCM Post Initializing");
        session = sess;
        //String ou1t=" { \"data\": { \"title\": \"without a doubt the best out put\",\"message\": \"Test Message\"},\"registration_ids\": [\"APA91bG5FNex2MbGC0yRXQ3eg_i8gtDO4c4VHGPvsO3q3ZKzOLKd4GuL6sSu20uEqb0s2or43C6TcnD2JFZSan2eTEOLb3ygqDgzWiWhPe5HLf_C6zaKC6NDtBuzJ_8FaWE-OXBbeCmSXX3nSsDwS5-Wl4Oz5SnhKA\"] } ";
        JSONObject data = new JSONObject();
        JSONObject finalo = new JSONObject();
        data.put("title", title);
        data.put("content", content);
        data.put("action", action);
        JSONArray regids = new JSONArray(regs);
        finalo.put("data", data);
        finalo.put("registration_ids", regids);
        logger.debug(sess + "GCM push payload: " + finalo.toString());
        ByteBuffer a = sendPost(ByteBuffer.wrap(finalo.toString().getBytes()));
        if (a == null) {
            return false;
        }
        logger.debug((new String(a.array())));
        return true;

    }

    static ByteBuffer sendPost(ByteBuffer a) throws Exception {

        byte[] result = null;
        int tries=Constants.GCM_MAX_RETRY;
        while (tries>=0) {
            try {
                String url = "https://android.googleapis.com/gcm/send";
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "key=AIzaSyC7EVwGwadMtVj_XZMBbhsp1YT7b4_Bcf4");
                con.setRequestProperty("Content-Type", "application/json");

                a.flip();
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.write(a.array());
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();

                BufferedInputStream in = new BufferedInputStream(con.getInputStream());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int next = in.read();
                while (next > -1) {
                    bos.write(next);
                    next = in.read();
                }
                bos.flush();
                result = bos.toByteArray();
                break;
            } catch (Exception e) {
                logger.error(session + " :GCM Connection error- " + e.getMessage());
                logger.info(session+" retrying GCM :"+tries);
                //infinite looping this ugh..
                Thread.sleep(0x3e8);
                tries+=1;
            }
        }
        if (result != null) {
            return ByteBuffer.wrap(result);
        } else {
            return null;
        }

    }
}
