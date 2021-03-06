package com.neoba.syncpad;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import net.dongliu.vcdiff.VcdiffDecoder;
import net.dongliu.vcdiff.VcdiffEncoder;

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

	static class document {

		public byte[] diff;
		public int age;
		public String dict;
		public String title;
		public byte permission;
		public String id;
		public boolean owns;

		document(String i, String t, byte[] d, int a, String di, byte p) {
			this.id = i;
			this.diff = d;
			this.age = a;
			this.dict = di;
			this.title = t;
			this.permission = p;
			this.owns = true;
		}

		document(String i, String t, byte[] d, int a, String di, byte p,
				boolean o) {
			this.id = i;
			this.diff = d;
			this.age = a;
			this.dict = di;
			this.title = t;
			this.permission = p;
			this.owns = o;
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

	public static ArrayList<Share> ShareMessage(ArrayList<Share> shares,
			UUID docid, UUID cookie) throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 16 + 16 + 9 * shares.size());
		buff.put((byte) 0x01);
		buff.put((byte) 0x08);
		buff.putInt(shares.size());
		putcookie(buff, cookie);
		buff.putLong(docid.getLeastSignificantBits());
		buff.putLong(docid.getMostSignificantBits());
		for (Share s : shares) {
			switch (s.permission) {
			case 1:
				buff.put((byte) 0x01);
				break;
			case 2:
				buff.put((byte) 0x02);
				break;
			}

			buff.putLong(s.userid);

		}
		ByteBuffer in = Postman.post(buff);
		buff.clear();

		if (in.getInt(2) == 0xFFFF)
			return shares;
		return null;
	}

	public static boolean Logout(UUID cookie) throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 16);
		buff.put((byte) 0x01);
		buff.put((byte) 0x09);
		buff.putInt(0xFFFF);
		putcookie(buff, cookie);
		ByteBuffer in = Postman.post(buff);
		buff.clear();
		return in != null;

	}

	public static boolean Delete(UUID cookie, UUID docuid) throws Exception {

		ByteBuffer buff = ByteBuffer.allocate(2 + 4 + 16 + 16);
		buff.put((byte) 0x01);
		buff.put((byte) 0x0B);
		buff.putInt(0x0000DE1E);
		putcookie(buff, cookie);
		buff.putLong(docuid.getLeastSignificantBits());
		buff.putLong(docuid.getMostSignificantBits());
		buff.clear();
		ByteBuffer in = Postman.post(buff);
		if (in == null)
			return false;
		if (in.getInt(2) == 0xFFFF) {
			return true;

		} else {
			return false;
		}

	}

	public static ArrayList<UUID> Unfollow(UUID cookie,String username) throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 16 + username.length());
		buff.put((byte) 0x01);
		buff.put((byte) 0x0C);
		buff.putInt(username.length());
		putcookie(buff, cookie);
		buff.put(username.getBytes());
		ArrayList<UUID> temp=new ArrayList<UUID>();
		ByteBuffer in = Postman.post(buff);
		buff.clear();
		if (in.getInt(2) == 0xFFFF) {
			System.out.println("unfollowed " + in.getLong(6));
			int count = in.getInt(6 + 8);
			int base = 6 + 8 + 4;
			for (int i = 0; i < count; i++) {
				UUID id = new UUID(in.getLong(base + 8), in.getLong(base));
				base += 16;
				System.out.println("removing " + id + " from cache");
				temp.add(id);
			}
			return temp;
		}else
			return null;

	}

	public static int SignUp(String username, String password) throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 20 + username.length());
		buff.put((byte) 0x01);
		buff.put((byte) 0x05);
		buff.putInt(username.length());
		buff.put(username.getBytes());
		for (byte b : MessageDigest.getInstance("SHA").digest(
				password.getBytes())) {
			buff.put(b);
		}
		ByteBuffer in = Postman.post(buff);
		buff.clear();
		if (in != null)
			return in.getInt(2);
		else
			return 0;
	}

	public static UUID Login(String username, String password, String regid)
			throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 20 + username.length() + 4
				+ regid.length() + 1);
		UUID cookie = null;
		buff.put((byte) 0x01);
		buff.put((byte) 0x06);
		buff.putInt(username.length());
		buff.put(username.getBytes());
		for (byte b : MessageDigest.getInstance("SHA").digest(
				password.getBytes())) {
			buff.put(b);
		}
		buff.put((byte) 0x0A);
		buff.putInt(regid.length());
		buff.put(regid.getBytes());
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

	@SuppressLint("UseSparseArrays")
	public static ArrayList<Object> getDigest(UUID cookie) throws Exception {
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
			boolean owns = in.get(base + 28 + sdiff + sdict + stitle + 4 + 1) == 0x1;
			base = base + 32 + sdiff + sdict + stitle + 1 + 1;
			cache.put(id, new document(id.toString(), title, diff, age, dict,
					perm, owns));

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
					docs.diff = new VcdiffEncoder(docs.dict, docs.dict)
							.encode();
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

	public static Long FollowUser(String username, UUID cookie)
			throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 16 + username.length());
		buff.put((byte) 0x01);
		buff.put((byte) 0x07);
		buff.putInt(username.length());
		putcookie(buff, cookie);
		buff.put(username.getBytes());
		ByteBuffer in = Postman.post(buff);
		buff.clear();
		if (in.getInt(2) == 0xFFFF) {
			Log.d("FOLLOWW!", "followed " + in.getLong(6));
			return in.getLong(6);
		}
		return null;
	}

	public static Long PokeUser(String username, UUID cookie) throws Exception {
		ByteBuffer buff = ByteBuffer.allocate(6 + 16 + username.length());
		buff.put((byte) 0x01);
		buff.put((byte) 0x0A);
		buff.putInt(username.length());
		putcookie(buff, cookie);
		buff.put(username.getBytes());
		ByteBuffer in = Postman.post(buff);
		buff.clear();
		if (in.getInt(2) == 0xFFFF) {
			Log.d("poked!", "poked " + in.getLong(6));
			return in.getLong(6);
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
		try {
			if (in.getInt(2) == 0xFFFF) {
				UUID docid = new UUID(in.getLong(14), in.getLong(6));
				document rdoc = new document(docid.toString(), docname,
						new VcdiffEncoder("", "").encode(), -1, "", (byte) 2);
				return rdoc;
			} else {
				Log.d("CREATEDOC", "error " + Integer.toHexString(in.getInt(2)));
			}
		} catch (Exception e) {
			Log.d("CREATEDOC", "server down probably " + in.getInt(2));
			return null;
		}
		buff.clear();
		return null;
	}

}
