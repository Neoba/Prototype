package com.neoba.syncpad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
//
//import net.dongliu.vcdiff.VcdiffDecoder;
//import net.dongliu.vcdiff.VcdiffEncoder;


import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

public class ByteMessenger {
	
	static class user {
		String username;
		Long id;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

	}
	
	

	
	@SuppressLint("UseSparseArrays")
	public static ArrayList<Object> getDigest(UUID cookie) throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 16);
		HashMap<UUID, document> cache = new HashMap<UUID, document>();
		buff.put((byte) 0x02);
		buff.put((byte) 0x04);
		buff.putInt(0xFFFF);
		putUUID(buff, cookie);
		ByteBuffer in = Postman.post(buff);
		buff.clear();
		int docount = in.getInt(2);
		int base = 6;
		for (int i = 0; i < docount; i++) {
			UUID id = new UUID(in.getLong(base + 8), in.getLong(base));

			int sdiff = in.getInt(base + 16);
			byte[] diff = new byte[sdiff];

			for (int j = 0; j < sdiff; j++) {
				diff[j] = in.get(base + 20 + j);
			}
			int sdict = in.getInt(base + 20 + sdiff);
			byte[] dictarray = new byte[sdict];

			for (int j = 0; j < sdict; j++) {
				dictarray[j] = in.get(base + 24 + sdiff + j);
			}
			String dict = Charset.forName("UTF-8")
					.decode(ByteBuffer.wrap(dictarray)).toString();
			int stitle = in.getInt(base + 24 + sdiff + sdict);
			byte[] titlearray = new byte[stitle];

			for (int j = 0; j < stitle; j++) {
				titlearray[j] = in.get(base + 28 + sdiff + sdict + j);
			}

			String title = Charset.forName("UTF-8")
					.decode(ByteBuffer.wrap(titlearray)).toString();
			int age = in.getInt(base + 28 + sdiff + sdict + stitle);
			byte perm = in.get(base + 28 + sdiff + sdict + stitle + 4);
			boolean owns = in.get(base + 28 + sdiff + sdict + stitle + 4 + 1) == 0x1;
			base = base + 32 + sdiff + sdict + stitle + 1 + 1;
			cache.put(id, new document(id.toString(), title, diff, age, dict,
					perm, owns,0));

			Log.d("NEOBA", "added to cache: " + cache.get(id).title);

		}
		HashMap<Long, String> follower = new HashMap<Long, String>();
		ArrayList<Object> ret = new ArrayList<Object>();
		ret.add(cache);
		int followerc = in.getInt(base);
		base += 4;
		for (int i = 0; i < followerc; i++) {
			int strc = in.getInt(base);
			base += 4;
			byte[] ff = new byte[strc];

			for (int j = 0; j < strc; j++) {
				ff[j] = in.get(base + j);

			}
			base += strc;
			String dict = Charset.forName("UTF-8").decode(ByteBuffer.wrap(ff))
					.toString();
			Long ii = in.getLong(base);
			Log.d("GD", "follower" + dict);
			Log.d("GD", "with id" + ii);
			follower.put(ii, dict);
			base += 8;
		}
		HashMap<Long, String> following = new HashMap<Long, String>();
		followerc = in.getInt(base);
		base += 4;
		System.out.println(followerc);
		for (int i = 0; i < followerc; i++) {
			int strc = in.getInt(base);
			base += 4;
			byte[] ff = new byte[strc];

			for (int j = 0; j < strc; j++) {
				ff[j] = in.get(base + j);

			}
			base += strc;
			String dict = Charset.forName("UTF-8").decode(ByteBuffer.wrap(ff))
					.toString();
			Log.d("GD", "following" + dict);
			Long ii = in.getLong(base);
			Log.d("GD", " id " + ii);
			following.put(ii, dict);
			base += 8;
		}
		ret.add(follower);
		ret.add(following);

		int ownc = in.getInt(base);
		ArrayList<ShareSchema> shares = new ArrayList<ShareSchema>();
		base += 4;
		for (int i = 0; i < ownc; i++) {

			UUID doc = new UUID(in.getLong(base + 8), in.getLong(base));
			System.err.println("OWNS!--> " + doc.toString());
			ShareSchema ss = new ShareSchema(doc.toString());
			base += 16;
			int reac = in.getInt(base);
			base += 4;
			for (int j = 0; j < reac; j++) {
				System.err.println("READS!-->" + in.getLong(base));
				ss.addread(in.getLong(base));
				base += 8;
			}
			reac = in.getInt(base);
			base += 4;
			for (int j = 0; j < reac; j++) {
				System.err.println(in.getLong(base));
				ss.addedit(in.getLong(base));
				base += 8;
			}
			shares.add(ss);
		}
		ret.add(shares);
		return ret;
	}

	static class document {

		public byte[] diff;
		public int age;
		public String dict;
		public String title;
		public byte permission;
		public String id;
		public boolean owns;
		public int synced;
		
		document(String id, String title, byte[] diff, int age, String dict, byte permission,boolean owns,int synced) {
			this.id = id;
			this.diff = diff;
			this.age = age;
			this.dict = dict;
			this.title = title;
			this.permission = permission;
			this.owns = owns;
			this.synced=synced;
		}



		public String toString() {
			return this.id + Arrays.toString(diff) + this.dict
					+ this.permission + this.age + this.title;
		}
	}

	static class Share {
		public long userid;
		public String docid;
		public String username;
		public byte permission;

		public Share(String docid, long userid, String username, byte permission) {
			super();
			this.docid = docid;
			this.userid = userid;
			this.username = username;
			this.permission = permission;
		}

		public void setPermission(byte p) {
			this.permission = p;
		}

		@Override
		public String toString() {
			return username + " " + permission + " *";
		}

	}

	public static Long FollowUser(String username, UUID cookie)
			throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 16 + username.length());
		buff.put((byte) 0x02);
		buff.put((byte) 0x07);
		buff.putInt(username.length());
		putUUID(buff, cookie);
		buff.put(username.getBytes());
		ByteBuffer in = Postman.post(buff);
		buff.clear();
		if (in.getInt(2) == 0xFFFF) {
			Log.d("FOLLOW", "followed " + in.getLong(6));
			return in.getLong(6);
		}
		return null;
	}

	private static void putUUID(ByteBuffer buff, UUID cookie) {
		buff.putLong(cookie.getLeastSignificantBits());
		buff.putLong(cookie.getMostSignificantBits());
	}

	public static int Ping() throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6);
		buff.put((byte) 0x02);
		buff.put((byte) 0x01);
		buff.put("PING".getBytes());
		ByteBuffer in = Postman.post(buff);
		buff.clear();
		int result = in.getInt(2);
		return result;
	}

	static ArrayList<HashMap<String, String>> facebookLoginUser(
			String access_token, String regid) throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 1 + access_token.length() + 4
				+ regid.length());
		buff.put((byte) 0x02);
		buff.put((byte) 0xF6);
		buff.putInt(access_token.length());
		buff.put(access_token.getBytes());
		buff.put((byte) 0x0C);
		buff.putInt(regid.length());
		buff.put(regid.getBytes());
		ArrayList<HashMap<String, String>> returnlist = new ArrayList<HashMap<String, String>>();
		try {
			ByteBuffer in = Postman.post(buff);

			if (in.getInt(2) == 0xFFFF) {

				UUID cookie = new UUID(in.getLong(14), in.getLong(6));
				int usernamel = in.getInt(22);
				byte[] unarray = new byte[usernamel];
				for (int j = 0; j < usernamel; j++) {
					unarray[j] = in.get(26 + j);
				}
				String dict = Charset.forName("UTF-8")
						.decode(ByteBuffer.wrap(unarray)).toString();
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

	static ArrayList<HashMap<String, String>> facebookCreateUser(
			String username, String access_token) throws IOException,
			MalformedURLException, JSONException, Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + username.length() + 4
				+ access_token.length());
		buff.put((byte) 0x02);
		buff.put((byte) 0xF5);
		buff.putInt(username.length());
		buff.put(username.getBytes());
		buff.putInt(access_token.length());
		buff.put(access_token.getBytes());
		JSONObject facebook_obj = jsonGet("https://graph.facebook.com/v2.1/me?fields=id,name,email,friends&access_token="
				+ access_token);
		// String suggested = json_get("https://graph.facebook.com/" +
		// facebook_obj.getString("id")).getString("username");
		// System.out.println("We suggest you take this name: " + suggested);
		ByteBuffer in;
		try {
			in = Postman.post(buff);

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
	
   

	public static JSONObject jsonGet(String url) throws MalformedURLException,
			IOException, JSONException {

		URL obj;
		obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
		int responseCode = con.getResponseCode();
		// try {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		JSONObject ret = new JSONObject(response.toString());

		return ret;
	}
	
    static ArrayList<HashMap<String, String>> createNote(UUID cookie,UUID randid) {
        ByteBuffer buff = ByteBuffer.allocate(16 + 2 + 4 + 16);
        buff.put((byte)0x02);
        buff.put((byte) 0x02);
        buff.putInt(0xFFFF);
        putUUID(buff, cookie);
        putUUID(buff, randid);

        ArrayList<HashMap<String, String>> returnlist = new ArrayList<HashMap<String, String>>();
        try {
            ByteBuffer in = Postman.post(buff);
            if (in.getInt(2) == 0xFFFF) {
                HashMap<String, String> result = new HashMap<String, String>();
                result.put("result", "success");
                returnlist.add(result);

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
	
	
	//
	// public static ArrayList<Share> ShareMessage(ArrayList<Share> shares,
	// UUID docid, UUID cookie) throws Exception {
	// ByteBuffer buff = ByteBuffer.allocate(6 + 16 + 16 + 9 * shares.size());
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x08);
	// buff.putInt(shares.size());
	// putUUID(buff, cookie);
	// buff.putLong(docid.getLeastSignificantBits());
	// buff.putLong(docid.getMostSignificantBits());
	// for (Share s : shares) {
	// switch (s.permission) {
	// case 1:
	// buff.put((byte) 0x01);
	// break;
	// case 2:
	// buff.put((byte) 0x02);
	// break;
	// }
	//
	// buff.putLong(s.userid);
	//
	// }
	// ByteBuffer in = Postman.post(buff);
	// buff.clear();
	//
	// if (in.getInt(2) == 0xFFFF)
	// return shares;
	// return null;
	// }
	//
	// public static boolean Logout(UUID cookie) throws Exception {
	// ByteBuffer buff = ByteBuffer.allocate(6 + 16);
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x09);
	// buff.putInt(0xFFFF);
	// putUUID(buff, cookie);
	// ByteBuffer in = Postman.post(buff);
	// buff.clear();
	// return in != null;
	//
	// }
	//
	// public static boolean Delete(UUID cookie, UUID docuid) throws Exception {
	//
	// ByteBuffer buff = ByteBuffer.allocate(2 + 4 + 16 + 16);
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x0B);
	// buff.putInt(0x0000DE1E);
	// putUUID(buff, cookie);
	// buff.putLong(docuid.getLeastSignificantBits());
	// buff.putLong(docuid.getMostSignificantBits());
	// buff.clear();
	// ByteBuffer in = Postman.post(buff);
	// if (in == null)
	// return false;
	// if (in.getInt(2) == 0xFFFF) {
	// return true;
	//
	// } else {
	// return false;
	// }
	//
	// }
	//
	// public static ArrayList<UUID> Unfollow(UUID cookie,String username)
	// throws Exception {
	// ByteBuffer buff = ByteBuffer.allocate(6 + 16 + username.length());
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x0C);
	// buff.putInt(username.length());
	// putUUID(buff, cookie);
	// buff.put(username.getBytes());
	// ArrayList<UUID> temp=new ArrayList<UUID>();
	// ByteBuffer in = Postman.post(buff);
	// buff.clear();
	// if (in.getInt(2) == 0xFFFF) {
	// System.out.println("unfollowed " + in.getLong(6));
	// int count = in.getInt(6 + 8);
	// int base = 6 + 8 + 4;
	// for (int i = 0; i < count; i++) {
	// UUID id = new UUID(in.getLong(base + 8), in.getLong(base));
	// base += 16;
	// System.out.println("removing " + id + " from cache");
	// temp.add(id);
	// }
	// return temp;
	// }else
	// return null;
	//
	// }
	//
	// public static int SignUp(String username, String password) throws
	// Exception {
	// ByteBuffer buff = ByteBuffer.allocate(6 + 20 + username.length());
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x05);
	// buff.putInt(username.length());
	// buff.put(username.getBytes());
	// for (byte b : MessageDigest.getInstance("SHA").digest(
	// password.getBytes())) {
	// buff.put(b);
	// }
	// ByteBuffer in = Postman.post(buff);
	// buff.clear();
	// if (in != null)
	// return in.getInt(2);
	// else
	// return 0;
	// }
	//
	// public static UUID Login(String username, String password, String regid)
	// throws Exception {
	// ByteBuffer buff = ByteBuffer.allocate(6 + 20 + username.length() + 4
	// + regid.length() + 1);
	// UUID cookie = null;
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x06);
	// buff.putInt(username.length());
	// buff.put(username.getBytes());
	// for (byte b : MessageDigest.getInstance("SHA").digest(
	// password.getBytes())) {
	// buff.put(b);
	// }
	// buff.put((byte) 0x0A);
	// buff.putInt(regid.length());
	// buff.put(regid.getBytes());
	// ByteBuffer in = Postman.post(buff);
	// buff.clear();
	// if (in != null) {
	// if (in.getInt(2) == 0xFFFF) {
	// cookie = new UUID(in.getLong(14), in.getLong(6));
	// Log.d("NEOBA", "recived a cookie :) --> " + cookie);
	// return cookie;
	// } else {
	// Log.d("NEOBA", "error logging in");
	// return null;
	// }
	// }
	// return cookie;
	//
	// }
	//
	// @SuppressLint("UseSparseArrays")
	// public static ArrayList<Object> getDigest(UUID cookie) throws Exception {
	// ByteBuffer buff = ByteBuffer.allocate(6 + 16);
	// HashMap<UUID, document> cache = new HashMap<UUID, document>();
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x04);
	// buff.putInt(0xFFFF);
	// putUUID(buff, cookie);
	// ByteBuffer in = Postman.post(buff);
	// buff.clear();
	// int docount = in.getInt(2);
	// int base = 6;
	// for (int i = 0; i < docount; i++) {
	// UUID id = new UUID(in.getLong(base + 8), in.getLong(base));
	//
	// int sdiff = in.getInt(base + 16);
	// byte[] diff = new byte[sdiff];
	//
	// for (int j = 0; j < sdiff; j++) {
	// diff[j] = in.get(base + 20 + j);
	// }
	// int sdict = in.getInt(base + 20 + sdiff);
	// byte[] dictarray = new byte[sdict];
	//
	// for (int j = 0; j < sdict; j++) {
	// dictarray[j] = in.get(base + 24 + sdiff + j);
	// }
	// String dict = Charset.forName("UTF-8")
	// .decode(ByteBuffer.wrap(dictarray)).toString();
	// int stitle = in.getInt(base + 24 + sdiff + sdict);
	// byte[] titlearray = new byte[stitle];
	//
	// for (int j = 0; j < stitle; j++) {
	// titlearray[j] = in.get(base + 28 + sdiff + sdict + j);
	// }
	//
	// String title = Charset.forName("UTF-8")
	// .decode(ByteBuffer.wrap(titlearray)).toString();
	// int age = in.getInt(base + 28 + sdiff + sdict + stitle);
	// byte perm = in.get(base + 28 + sdiff + sdict + stitle + 4);
	// boolean owns = in.get(base + 28 + sdiff + sdict + stitle + 4 + 1) == 0x1;
	// base = base + 32 + sdiff + sdict + stitle + 1 + 1;
	// cache.put(id, new document(id.toString(), title, diff, age, dict,
	// perm, owns));
	//
	// Log.d("NEOBA", "added to cache: " + cache.get(id).title);
	//
	// }
	// HashMap<Long, String> follower = new HashMap<Long, String>();
	// ArrayList<Object> ret = new ArrayList<Object>();
	// ret.add(cache);
	// int followerc = in.getInt(base);
	// base += 4;
	// for (int i = 0; i < followerc; i++) {
	// int strc = in.getInt(base);
	// base += 4;
	// byte[] ff = new byte[strc];
	//
	// for (int j = 0; j < strc; j++) {
	// ff[j] = in.get(base + j);
	//
	// }
	// base += strc;
	// String dict = Charset.forName("UTF-8").decode(ByteBuffer.wrap(ff))
	// .toString();
	// Long ii = in.getLong(base);
	// Log.d("GD", "follower" + dict);
	// Log.d("GD", "with id" + ii);
	// follower.put(ii, dict);
	// base += 8;
	// }
	// HashMap<Long, String> following = new HashMap<Long, String>();
	// followerc = in.getInt(base);
	// base += 4;
	// System.out.println(followerc);
	// for (int i = 0; i < followerc; i++) {
	// int strc = in.getInt(base);
	// base += 4;
	// byte[] ff = new byte[strc];
	//
	// for (int j = 0; j < strc; j++) {
	// ff[j] = in.get(base + j);
	//
	// }
	// base += strc;
	// String dict = Charset.forName("UTF-8").decode(ByteBuffer.wrap(ff))
	// .toString();
	// Log.d("GD", "following" + dict);
	// Long ii = in.getLong(base);
	// Log.d("GD", " id " + ii);
	// following.put(ii, dict);
	// base += 8;
	// }
	// ret.add(follower);
	// ret.add(following);
	//
	// int ownc = in.getInt(base);
	// ArrayList<ShareSchema> shares = new ArrayList<ShareSchema>();
	// base += 4;
	// for (int i = 0; i < ownc; i++) {
	//
	// UUID doc = new UUID(in.getLong(base + 8), in.getLong(base));
	// System.err.println("OWNS!--> " + doc.toString());
	// ShareSchema ss = new ShareSchema(doc.toString());
	// base += 16;
	// int reac = in.getInt(base);
	// base += 4;
	// for (int j = 0; j < reac; j++) {
	// System.err.println("READS!-->" + in.getLong(base));
	// ss.addread(in.getLong(base));
	// base += 8;
	// }
	// reac = in.getInt(base);
	// base += 4;
	// for (int j = 0; j < reac; j++) {
	// System.err.println(in.getLong(base));
	// ss.addedit(in.getLong(base));
	// base += 8;
	// }
	// shares.add(ss);
	// }
	// ret.add(shares);
	// return ret;
	// }
	//
	// private static void putUUID(ByteBuffer buff, UUID cookie) {
	// buff.putLong(cookie.getLeastSignificantBits());
	// buff.putLong(cookie.getMostSignificantBits());
	// }
	//
	// public static document Editdoc(document docs, UUID cookie) throws
	// Exception {
	// UUID docuid = UUID.fromString(docs.id);
	// if (docuid != null) {
	// ByteBuffer buff = ByteBuffer.allocate(2 + 4 + 16 + 16
	// + docs.diff.length + 4);
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x03);
	// buff.putInt(docs.diff.length);
	// putUUID(buff, cookie);
	// buff.putLong(docuid.getLeastSignificantBits());
	// buff.putLong(docuid.getMostSignificantBits());
	// for (byte b : docs.diff) {
	// buff.put(b);
	// }
	// buff.putInt(docs.age + 1);
	// ByteBuffer in = Postman.post(buff);
	// buff.clear();
	// if (in.getInt(2) == 0xFFFF) {
	// docs.age += 1;
	//
	// if (docs.age % 5 == 0) {
	// docs.dict = new VcdiffDecoder(docs.dict, docs.diff)
	// .decode();
	// docs.diff = new VcdiffEncoder(docs.dict, docs.dict)
	// .encode();
	// Log.d("Bytemessenger", "dictionary updated.. new dict: \n"
	// + docs.dict);
	// }
	// return docs;
	// } else
	// Log.d("Bytemessgnger.getdocs",
	// Integer.toHexString(in.getInt(2)));
	//
	// }
	// return null;
	// }
	//
	// public static Long FollowUser(String username, UUID cookie)
	// throws Exception {
	// ByteBuffer buff = ByteBuffer.allocate(6 + 16 + username.length());
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x07);
	// buff.putInt(username.length());
	// putUUID(buff, cookie);
	// buff.put(username.getBytes());
	// ByteBuffer in = Postman.post(buff);
	// buff.clear();
	// if (in.getInt(2) == 0xFFFF) {
	// Log.d("FOLLOWW!", "followed " + in.getLong(6));
	// return in.getLong(6);
	// }
	// return null;
	// }
	//
	// public static Long PokeUser(String username, UUID cookie) throws
	// Exception {
	// ByteBuffer buff = ByteBuffer.allocate(6 + 16 + username.length());
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x0A);
	// buff.putInt(username.length());
	// putUUID(buff, cookie);
	// buff.put(username.getBytes());
	// ByteBuffer in = Postman.post(buff);
	// buff.clear();
	// if (in.getInt(2) == 0xFFFF) {
	// Log.d("poked!", "poked " + in.getLong(6));
	// return in.getLong(6);
	// }
	// return null;
	// }
	//
	// public static document CreateDoc(String docname, UUID cookie)
	// throws Exception {
	//
	// ByteBuffer buff = ByteBuffer.allocate(16 + 2 + 4 + docname.length());
	// buff.put((byte) 0x01);
	// buff.put((byte) 0x02);
	// buff.putInt(docname.length());
	// putUUID(buff, cookie);
	// buff.put(docname.getBytes());
	// ByteBuffer in = Postman.post(buff);
	// try {
	// if (in.getInt(2) == 0xFFFF) {
	// UUID docid = new UUID(in.getLong(14), in.getLong(6));
	// document rdoc = new document(docid.toString(), docname,
	// new VcdiffEncoder("", "").encode(), -1, "", (byte) 2);
	// return rdoc;
	// } else {
	// Log.d("CREATEDOC", "error " + Integer.toHexString(in.getInt(2)));
	// }
	// } catch (Exception e) {
	// Log.d("CREATEDOC", "server down probably " + in.getInt(2));
	// return null;
	// }
	// buff.clear();
	// return null;
	// }

}
