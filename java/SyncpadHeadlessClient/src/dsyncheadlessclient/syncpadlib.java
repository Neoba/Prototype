/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dsyncheadlessclient;

import static dsyncheadlessclient.DsyncHeadlessClient.version;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author root
 */
public class syncpadlib {

    private static final byte version = 0x02;
    private static String url = "http://localhost:2811";

    public static boolean DEBUG = false;

    static boolean ping() {
        ByteBuffer buff = ByteBuffer.allocate(6);
        buff.put(version);
        buff.put((byte) 0x01);
        buff.put("PING".getBytes());
        try {
            ByteBuffer in = sendPost(buff);
            if (syncpadlib.DEBUG) {
                System.out.println("Res:");
                printhex(in.array(), in.array().length);
            }
            return true;
        } catch (Exception err) {
            //System.err.println(err);
            return false;
        }

    }

    static ArrayList<HashMap<String, String>> createNote(UUID cookie) {
        ByteBuffer buff = ByteBuffer.allocate(16 + 2 + 4 + 16);
        buff.put(version);
        buff.put((byte) 0x02);
        buff.putInt(0xFFFF);
        UUID randid = UUID.randomUUID();
        syncpadlib.putUUID(buff, cookie);
        syncpadlib.putUUID(buff, randid);

        ArrayList<HashMap<String, String>> returnlist = new ArrayList<HashMap<String, String>>();
        try {
            ByteBuffer in = syncpadlib.sendPost(buff);
            if (syncpadlib.DEBUG) {
                System.out.println("Res:");
                printhex(in.array(), in.array().length);
            }
            if (in.getInt(2) == 0xFFFF) {
                HashMap<String, String> result = new HashMap<String, String>();
                result.put("result", "success");
                returnlist.add(result);
                HashMap<String, String> cookieh = new HashMap<String, String>();
                cookieh.put("id", randid.toString());
                returnlist.add(cookieh);
            } else {
                System.err.println("som error");
                HashMap<String, String> result = new HashMap<String, String>();
                result.put("result", "error");
                returnlist.add(result);
            }
        } catch (Exception ex) {
            HashMap<String, String> result = new HashMap<String, String>();
            result.put("result", "connection_fail");
            returnlist.add(result);
        }

        return returnlist;
    }

    static boolean deleteNote(String uuid_, UUID cookie) {
        {
            UUID docuid = UUID.fromString(uuid_);
            ByteBuffer buff = null;
            if (docuid != null) {
                buff = ByteBuffer.allocate(2 + 4 + 16 + 16);
                buff.put(version);
                buff.put((byte) 0x0B);
                buff.putInt(0x0000DE1E);
                syncpadlib.putUUID(buff, cookie);
                buff.putLong(docuid.getLeastSignificantBits());
                buff.putLong(docuid.getMostSignificantBits());
            }
            try {
                ByteBuffer in = syncpadlib.sendPost(buff);
                if (syncpadlib.DEBUG) {
                    System.out.println("Res:");
                    printhex(in.array(), in.array().length);
                }
                if (in.getInt(2) == 0xFFFF) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception ex) {
                return false;
            }
        }
    }

    static ArrayList<HashMap<String, String>> facebookLoginUser(String access_token, String regid) throws Exception {
        ByteBuffer buff = ByteBuffer.allocate(6 + 1 + access_token.length() + 4 + regid.length());
        buff.put(version);
        buff.put((byte) 0xF6);
        buff.putInt(access_token.length());
        buff.put(access_token.getBytes());
        buff.put((byte) 0x0C);
        buff.putInt(regid.length());
        buff.put(regid.getBytes());
        ArrayList<HashMap<String, String>> returnlist = new ArrayList<HashMap<String, String>>();
        try {
            ByteBuffer in = sendPost(buff);
            if (syncpadlib.DEBUG) {
                System.out.println("Res:");
                printhex(in.array(), in.array().length);
            }
            if (in.getInt(2) == 0xFFFF) {

                UUID cookie = new UUID(in.getLong(14), in.getLong(6));
                int usernamel = in.getInt(22);
                byte[] unarray = new byte[usernamel];
                for (int j = 0; j < usernamel; j++) {
                    unarray[j] = in.get(26 + j);
                }
                String dict = Charset.forName("UTF-8").decode(ByteBuffer.wrap(unarray)).toString();
                HashMap<String, String> result = new HashMap<String, String>();
                result.put("result", "success");
                returnlist.add(result);
                HashMap<String, String> cookieh = new HashMap<String, String>();
                cookieh.put("cookie", cookie.toString());
                returnlist.add(cookieh);
                HashMap<String, String> un = new HashMap<String, String>();
                cookieh.put("username", dict);
                returnlist.add(un);
            } else {
                System.err.println("error not signed up");
                HashMap<String, String> result = new HashMap<String, String>();
                result.put("result", "unregistered");
                returnlist.add(result);
            }
        } catch (Exception err) {
            System.err.println(err);
            return returnlist;
        }
        return returnlist;

    }

    static ArrayList<HashMap<String, String>> facebookCreateUser(String username, String access_token) throws IOException, MalformedURLException, JSONException, Exception {
        ByteBuffer buff = ByteBuffer.allocate(6 + username.length() + 4 + access_token.length());
        buff.put(version);
        buff.put((byte) 0xF5);
        buff.putInt(username.length());
        buff.put(username.getBytes());
        buff.putInt(access_token.length());
        buff.put(access_token.getBytes());
        JSONObject facebook_obj = jsonGet("https://graph.facebook.com/v2.1/me?fields=id,name,email,friends&access_token=" + access_token);
        //String suggested = json_get("https://graph.facebook.com/" + facebook_obj.getString("id")).getString("username");
        //System.out.println("We suggest you take this name: " + suggested);
        ByteBuffer in;
        try {
            in = sendPost(buff);
            if (syncpadlib.DEBUG) {
                System.out.println("Res:");
                printhex(in.array(), in.array().length);
            }
        } catch (Exception err) {
            System.err.println(err);
            return null;
        }
        ArrayList<HashMap<String, String>> sugestions = new ArrayList<HashMap<String, String>>();
        if (in.getInt(2) == 0xFFFF) {
            int suggestion_size = in.getInt(6), base = 10;
            HashMap<String, String> result = new HashMap<String, String>();
            result.put("result", "success");
            sugestions.add(result);
            for (int i = 0; i < suggestion_size; i++) {
                HashMap<String, String> temp = new HashMap<String, String>();
                temp.put("id", Long.toString(in.getLong(base)));
                int names = in.getInt(base + 8);
                StringBuilder dictnew = new StringBuilder();
                for (int h = 0; h < names; h++) {
                    dictnew.append((char) in.get(base + 4 + 8 + h));
                }
                temp.put("username", dictnew.toString());
                base += (8 + 4 + names);
                sugestions.add(temp);

            }
            return sugestions;
        } else if (in.getInt(2) == 0x8000) {
            HashMap<String, String> result = new HashMap<String, String>();
            result.put("result", "user_exits");
            sugestions.add(result);
            return sugestions;
        }
        return null;
    }

    static ByteBuffer sendPost(ByteBuffer a) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Android 7.9 Upsidedown cake");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/octet-stream");
        // Send post request
        a.flip();
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.write(a.array());
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        if (DEBUG) {
            System.out.println("Req:");
            printhex(a.array(), a.array().length);
        }
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

    public static void putUUID(ByteBuffer buff, UUID cookie) {
        buff.putLong(cookie.getLeastSignificantBits());
        buff.putLong(cookie.getMostSignificantBits());
    }

    public static JSONObject jsonGet(String url) throws MalformedURLException, IOException, JSONException {

        URL obj;
        obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        int responseCode = con.getResponseCode();
        //try {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject ret = new JSONObject(response.toString());

        return ret;
    }

    public static void printhex(byte[] b, int count) {
        int rem = count;
        String outs;
        int ran = 0;
        //System.out.println(count + "B dump");
        for (int i = 0; i < ((count / 10) + 1); i++) {
            for (int j = 0; (j < rem && j < 10); j++) {
                outs = String.format("%02X ", b[j + count - rem] & 0xff);

                System.out.print(outs);
                ran++;
            }
            for (int j = 0; j < 29 - (ran * 2 + ran - 1); j++) {
                System.out.print(" ");
            }
            ran = 0;
            for (int j = 0; (j < rem && j < 10); j++) {
                System.out.print((char) (b[j + count - rem] >= 30 && b[j + count - rem] <= 127 ? b[j + count - rem] : '.') + " ");
            }
            rem -= 10;

            System.out.println("");

        }

    }
}
