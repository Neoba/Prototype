package com.neoba.syncpad;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.UUID;

import net.dongliu.vcdiff.VcdiffDecoder;
import net.dongliu.vcdiff.VcdiffEncoder;

import android.util.Log;

public class ByteMessenger {
	static class user{
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
	static class document {

		public byte[] diff;
		public int age;
		public String dict;
		public String title;
		public byte permission;
		public String id;

		document(String i, String t, byte[] d, int a, String di, byte p) {
			this.id = i;
			this.diff = d;
			this.age = a;
			this.dict = di;
			this.title = t;
			this.permission = p;
		}
	}
	static class Share{
		public long userid;
		public String username;
		public long getUserid() {
			return userid;
		}
		public void setUserid(long userid) {
			this.userid = userid;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public byte getPermission() {
			return permission;
		}
		public void setPermission(byte permission) {
			this.permission = permission;
		}
		public byte permission;
		public Share(long userid, String username, byte permission) {
			super();
			this.userid = userid;
			this.username = username;
			this.permission = permission;
		}
		public Share() {
			// TODO Auto-generated constructor stub
		}
		public Share(String string) {
			// TODO Auto-generated constructor stub
			this.username=string;
		}

		
	}

	public static int Ping() throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6);
		buff.put((byte) 0x01);
		buff.put((byte) 0x01);
		buff.put("PING".getBytes());
		ByteBuffer in = Postman.post(buff);
		buff.clear();
		int result = in.getInt(2);
		return result;
	}

	public static UUID Login(String username, String password) throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 20 + username.length());
		UUID cookie = null;
		buff.put((byte) 0x01);
		buff.put((byte) 0x06);
		buff.putInt(username.length());
		buff.put(username.getBytes());
		for (byte b : MessageDigest.getInstance("SHA").digest(
				password.getBytes())) {
			buff.put(b);
		}
		ByteBuffer in = Postman.post(buff);
		buff.clear();
		if (in != null) {
			if (in.getInt(2) == 0xFFFF) {
				cookie = new UUID(in.getLong(14), in.getLong(6));
				Log.d("NEOBA", "recived a cookie :) --> " + cookie);
				return cookie;
			} else {
				Log.d("NEOBA", "error logging in");
				return null;
			}
		}
		return cookie;

	}

	public static HashMap<UUID, document> getDigest(UUID cookie)
			throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 16);
		HashMap<UUID, document> cache = new HashMap<UUID, document>();
		buff.put((byte) 0x01);
		buff.put((byte) 0x04);
		buff.putInt(0xFFFF);
		putcookie(buff, cookie);
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
			base = base + 32 + sdiff + sdict + stitle + 1;
			cache.put(id, new document(id.toString(), title, diff, age, dict,
					perm));

			Log.d("NEOBA", "added to cache: " + cache.get(id).title);

		}
		return cache;
	}

	private static void putcookie(ByteBuffer buff, UUID cookie) {
		buff.putLong(cookie.getLeastSignificantBits());
		buff.putLong(cookie.getMostSignificantBits());
	}

	public static document Editdoc(document docs, UUID cookie) throws Exception {
		UUID docuid = UUID.fromString(docs.id);
		if (docuid != null) {
			ByteBuffer buff = ByteBuffer.allocate(2 + 4 + 16 + 16
					+ docs.diff.length + 4);
			buff.put((byte) 0x01);
			buff.put((byte) 0x03);
			buff.putInt(docs.diff.length);
			putcookie(buff, cookie);
			buff.putLong(docuid.getLeastSignificantBits());
			buff.putLong(docuid.getMostSignificantBits());
			for (byte b : docs.diff) {
				buff.put(b);
			}
			buff.putInt(docs.age + 1);
			ByteBuffer in = Postman.post(buff);
			buff.clear();
			if (in.getInt(2) == 0xFFFF) {
				docs.age += 1;

				if (docs.age % 5 == 0) {
					docs.dict = new VcdiffDecoder(docs.dict, docs.diff)
							.decode();
					Log.d("Bytemessenger", "dictionary updated.. new dict: \n"
							+ docs.dict);
				}
				return docs;
			} else
				Log.d("Bytemessgnger.getdocs",
						Integer.toHexString(in.getInt(2)));

		}
		return null;
	}

	public static document CreateDoc(String docname, UUID cookie)
			throws Exception {

		ByteBuffer buff = ByteBuffer.allocate(16 + 2 + 4 + docname.length());
		buff.put((byte) 0x01);
		buff.put((byte) 0x02);
		buff.putInt(docname.length());
		putcookie(buff, cookie);
		buff.put(docname.getBytes());
		ByteBuffer in = Postman.post(buff);
		if (in.getInt(2) == 0xFFFF) {
			UUID docid = new UUID(in.getLong(14), in.getLong(6));
			document rdoc = new document(docid.toString(), docname,new VcdiffEncoder("", "").encode(), -1, "", (byte) 2);
			return rdoc;
		} else {
			Log.d("CREATEDOC", "som error "+in.getInt(2));
		}
		buff.clear();
		return null;
	}

}
