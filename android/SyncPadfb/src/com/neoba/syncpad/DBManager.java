package com.neoba.syncpad;


import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import net.dongliu.vcdiff.VcdiffDecoder;
import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;

import com.neoba.syncpad.ByteMessenger.document;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DBManager {

	// Database Version
	public static final int DATABASE_VERSION = 1;
	// Database Name
	public static final String DATABASE_NAME = "docs.db";
	SQLiteDatabase db;
	Context context;
	DatabaseHelper DBHelper;

	public DBManager(Context context) {
		this.context = context;
		DBHelper = new DatabaseHelper(context);
	}
	


	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String CREATE_BOOK_TABLE = "CREATE TABLE docs ( "
					+ "id TEXT, " + "diff TEXT, "
					+ "dict TEXT,age INTEGER,title TEXT,permission INTEGER,date LONG ,owns INTEGER default 1,  synced INTEGER default 0,syncede INTEGER default 0,syncedd INTEGER default 0)";
			db.execSQL(CREATE_BOOK_TABLE);
			String CREATE_FOLLOWERS_TABLE = "CREATE TABLE follower ( "
					+ "id LONG, " + "username TEXT "
					+")";
			db.execSQL(CREATE_FOLLOWERS_TABLE);
			String CREATE_FOLLOWING_TABLE = "CREATE TABLE following ( "
					+ "id LONG , " + "username TEXT "
					+")";
			db.execSQL(CREATE_FOLLOWING_TABLE);
			String CREATE_PERMISSIONS_TABLE = "CREATE TABLE permissions ( "
					+ "docid TEXT, username TEXT,userid LONG,permission INTEGER "
					+")";
			db.execSQL( CREATE_PERMISSIONS_TABLE);

		}
		


		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS docs");
			db.execSQL("DROP TABLE IF EXISTS follower");
			db.execSQL("DROP TABLE IF EXISTS following");
			db.execSQL("DROP TABLE IF EXISTS permssions");
			this.onCreate(db);

		}

	}

	public DBManager open() {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		DBHelper.close();
	}

	public document getDocument(int rowid) {
		String sql = "SELECT * from docs where rowid=?";
		Cursor c = db.rawQuery(sql, new String[] { Integer.toString(rowid) });
		c.moveToFirst();
		// String i, String t, byte[] d, int a, String di, byte p
		document d =doccursorToDocument(c);
		return d;
	}
	
	public document getDocument(String docid) {
		String sql = "SELECT * from docs where id=?";
		Cursor c = db.rawQuery(sql, new String[] { docid });
		c.moveToFirst();
		// String i, String t, byte[] d, int a, String di, byte p
		document d =new document(c.getString(0), c.getString(4), c.getBlob(1),
				c.getInt(3), c.getString(2), (byte) c.getInt(5),c.getInt(6)==1?true:false,c.getInt(7));
		return d;
	}
	
	public document doccursorToDocument(Cursor c){
		return  new document(c.getString(1), c.getString(5), c.getBlob(2),
				c.getInt(4), c.getString(3), (byte) c.getInt(6),c.getInt(7)==1?true:false,c.getInt(8));
	}
	
	
	public boolean isDocMine(String docid){
		String sql = "SELECT owns from docs where id=?";
		Cursor c = db.rawQuery(sql, new String[] { docid });
		c.moveToFirst();
		int out=c.getInt(0);
		return out==1;
	}
	public boolean isDocEditable(String docid){
		String sql = "SELECT permission from docs where id=?";
		Cursor c = db.rawQuery(sql, new String[] { docid });
		c.moveToFirst();
		int out=c.getInt(0);
		return out==2;
	}
	public void deleteDoc(String id){
		String sql = "delete from docs where id=?";
		SQLiteStatement updateStmt = db.compileStatement(sql);
		updateStmt.clearBindings();
		updateStmt.bindString(1, id);
		updateStmt.executeUpdateDelete();
		db.execSQL("vacuum");
	}
	public void insertDoc(document doc) {
		String sql = "delete from docs where id=?";
		SQLiteStatement updateStmt = db.compileStatement(sql);
		updateStmt.clearBindings();
		updateStmt.bindString(1, doc.id);
		updateStmt.executeUpdateDelete();

		sql = "INSERT INTO docs (id,diff,dict,age,title,permission,date,owns,synced) VALUES(?,?,?,?,?,?,?,?,?)";
		SQLiteStatement insertStmt = db.compileStatement(sql);
		insertStmt.clearBindings();
		insertStmt.bindString(1, doc.id);
		insertStmt.bindBlob(2, doc.diff);
		insertStmt.bindString(3, doc.dict);
		insertStmt.bindLong(4, doc.age);
		insertStmt.bindString(5, doc.title);
		insertStmt.bindLong(6, doc.permission);
		insertStmt.bindLong(7, new Date().getTime());
		insertStmt.bindLong(8, doc.owns?1:0);
		insertStmt.bindLong(9, Constants.UNSYNCED_CREATE);
		insertStmt.executeInsert();
		db.execSQL("vacuum");
	}
	public void editDoc(String id, byte[] diff, int int1) throws IOException, VcdiffDecodeException, VcdiffEncodeException {
		String sql = "update docs set diff=?,age=?,date=? where id=?";
		SQLiteStatement updateStmt = db.compileStatement(sql);
		updateStmt.clearBindings();
		updateStmt.bindBlob(1, diff);
		updateStmt.bindString(4, id);
		updateStmt.bindLong(2, int1);
		updateStmt.bindLong(3,new Date().getTime());

		updateStmt.executeUpdateDelete();
		if(int1%5==0)
		{
			String sql1 = "update docs set dict=?,diff=? where id=?";
			SQLiteStatement updateStmt1 = db.compileStatement(sql1);
			String dict=getDict(id);
			Log.d("updatedictbefore", getDict(id));
			dict=new VcdiffDecoder(dict, diff).decode();
			updateStmt1.bindString(1, dict);
			updateStmt1.bindBlob(2, new VcdiffEncoder(dict, dict).encode());
			updateStmt1.bindString(3, id);
			updateStmt1.executeUpdateDelete();
			Log.d("updatedictafter", getDict(id));
			
		}
	}
	public void insertFollower(Long id,String username) {
		String sql = "INSERT INTO follower (id,username) VALUES(?,?)";
		SQLiteStatement insertStmt = db.compileStatement(sql);
		insertStmt.clearBindings();
		insertStmt.bindLong(1, id);
		insertStmt.bindString(2, username);
		
		insertStmt.executeInsert();
	}
	public void deleteFollower(String id){
		String sql = "delete from follower where id=?";
		SQLiteStatement updateStmt = db.compileStatement(sql);
		updateStmt.clearBindings();
		updateStmt.bindString(1, id);
		updateStmt.executeUpdateDelete();
		db.execSQL("vacuum");
	}
	public void deleteFollowing(String id){
		String sql = "delete from following where username=?";
		SQLiteStatement updateStmt = db.compileStatement(sql);
		updateStmt.clearBindings();
		updateStmt.bindString(1, id);
		updateStmt.executeUpdateDelete();
		db.execSQL("vacuum");
	}
	public void insertFollowing(Long id,String username) {
		String sql = "INSERT INTO following (id,username) VALUES(?,?)";
		SQLiteStatement insertStmt = db.compileStatement(sql);
		insertStmt.clearBindings();
		insertStmt.bindLong(1, id);
		insertStmt.bindString(2, username);
		insertStmt.executeInsert();
	}
	public void insertPermission(String docid,String username,Long userid,int permission) {
		String sql = "INSERT INTO permissions (docid,username,userid,permission) VALUES(?,?,?,?)";
		SQLiteStatement insertStmt = db.compileStatement(sql);
		insertStmt.clearBindings();
		insertStmt.bindString(1, docid);
		insertStmt.bindString(2, username);
		insertStmt.bindLong(3, userid);
		insertStmt.bindLong(4, permission);
		insertStmt.executeInsert();
	}
	public void updateContent(document doc) {
		String sql = "update docs set diff=?,dict=?,age=?,date=?,title=?,syncede=1 where id=?";
		SQLiteStatement updateStmt = db.compileStatement(sql);
		updateStmt.clearBindings();
		updateStmt.bindBlob(1, doc.diff);
		updateStmt.bindString(2, doc.dict);
		updateStmt.bindLong(3, doc.age);
		updateStmt.bindLong(4,new Date().getTime());
		updateStmt.bindString(5, doc.title);
		updateStmt.bindString(6, doc.id);
		updateStmt.executeUpdateDelete();
	}
	public void escalatePermission(String docid,Long id) {
		String sql = "update permissions set permission=2 where userid=? and docid=?";
		SQLiteStatement updateStmt = db.compileStatement(sql);
		updateStmt.clearBindings();
		updateStmt.bindLong(1, id);
		updateStmt.bindString(2, docid);
		updateStmt.executeUpdateDelete();
	}
	public void syncNote(String docid) {
		String sql = "update docs set synced=0 where id=?";
		SQLiteStatement updateStmt = db.compileStatement(sql);
		updateStmt.clearBindings();
		updateStmt.bindString(1, docid);
		updateStmt.executeUpdateDelete();
	}
	public void synceditNote(String docid) {
		String sql = "update docs set syncede=0 where id=?";
		SQLiteStatement updateStmt = db.compileStatement(sql);
		updateStmt.clearBindings();
		updateStmt.bindString(1, docid);
		updateStmt.executeUpdateDelete();
	}
	public String getFollowerUsername(Long userid) {
		String sql = "SELECT username FROM follower where id=?";
		Cursor cursor = db.rawQuery(sql, new String[] {Long.toString(userid)});
		cursor.moveToFirst();
		return cursor.getString(0);
	}
	public String getFollowingUserid(String username) {
		String sql = "SELECT id FROM followingr where username=?";
		Cursor cursor = db.rawQuery(sql, new String[] {username});
		cursor.moveToFirst();
		return cursor.getString(0);
	}
	
	public String getFollowingUsernameFromRowid(int i) {
		String sql = "SELECT username FROM following where rowid=?";
		Cursor cursor = db.rawQuery(sql, new String[] {Integer.toString(i)});
		cursor.moveToFirst();
		return cursor.getString(0);
	}
	
	public Long getFollowerid(String username) {
		String sql = "SELECT id FROM follower where username=?";
		Cursor cursor = db.rawQuery(sql, new String[] {username});
		cursor.moveToFirst();
		return cursor.getLong(0);
	}
	
	public Cursor getPermissions(){
		String sql = "SELECT rowid _id ,* FROM permissions";
		Cursor cursor = db.rawQuery(sql, new String[] {});
		return cursor;
	}
	public Cursor getAllFollower() {

		String sql = "SELECT rowid _id ,* FROM follower";
		Cursor cursor = db.rawQuery(sql, new String[] {});
		return cursor;

	}
	public Cursor getAllFollowing() {
		String sql = "SELECT rowid _id ,* FROM following";
		Cursor cursor = db.rawQuery(sql, new String[] {});
		return cursor;

	}
	public Cursor getAllFollowingUsernames() {
		String sql = "SELECT rowid _id ,username FROM following";
		Cursor cursor = db.rawQuery(sql, new String[] {});
		return cursor;

	}


	public Cursor getAllDocs() {
		String sql = "SELECT rowid _id ,* FROM docs";
		Cursor cursor = db.rawQuery(sql, new String[] {});
		return cursor;
	}
	public Cursor getAllUnsyncedCreateDocs() {
		String sql = "SELECT rowid _id ,* FROM docs where synced=1";
		Cursor cursor = db.rawQuery(sql, new String[] {});
		return cursor;
	}

	public Cursor getAllUnsyncedEditDocs() {
		String sql = "SELECT rowid _id ,* FROM docs where syncede=1";
		Cursor cursor = db.rawQuery(sql, new String[] {});
		return cursor;
	}

	public String getDoc(String id) throws IOException, VcdiffDecodeException {

		String sql = "SELECT dict,diff from docs where id=?";
		Cursor c = db.rawQuery(sql, new String[] { id});
		c.moveToFirst();
		String dict = c.getString(0);
		byte[] diff = c.getBlob(1);
		Log.d("DICTGETDOC", dict+Arrays.toString(diff)+":"+new VcdiffDecoder(dict, diff).decode());
		return new VcdiffDecoder(dict, diff).decode();

	}

	public byte[] getDiff(int rowid) {

		String sql = "SELECT diff from docs where rowid=?";
		Cursor c = db.rawQuery(sql, new String[] { Integer.toString(rowid) });
		c.moveToFirst();
		return c.getBlob(0);

	}

	public String getDict(int rowid) {

		String sql = "SELECT dict from docs where rowid=?";
		Cursor c = db.rawQuery(sql, new String[] { Integer.toString(rowid) });
		c.moveToFirst();
		return c.getString(0);

	}
	public String getDict(String rowid) {

		String sql = "SELECT dict from docs where id=?";
		Cursor c = db.rawQuery(sql, new String[] { rowid});
		c.moveToFirst();
		return c.getString(0);

	}
	
	public String getId(int rowid) {

		String sql = "SELECT id from docs where rowid=?";
		Cursor c = db.rawQuery(sql, new String[] { Integer.toString(rowid) });
		c.moveToFirst();
		Log.d("dbgetid",""+rowid);
		return c.getString(0);

	}

	public void clearPermissions(String docid){
		SQLiteStatement updateStmt=db.compileStatement("DELETE from permissions where docid=?");
		updateStmt.clearBindings();
		updateStmt.bindString(1, docid);
		updateStmt.executeUpdateDelete();
	}
	public void clearPermissions(String docid,String userid){
		SQLiteStatement updateStmt=db.compileStatement("DELETE from permissions where docid=? and userid=?");
		updateStmt.clearBindings();
		updateStmt.bindString(1, docid);
		updateStmt.bindLong(2, Long.parseLong(userid));
		updateStmt.executeUpdateDelete();
	}
	public void Truncate() {

		db.compileStatement("DELETE from docs").executeUpdateDelete();
		db.compileStatement("DELETE from follower").executeUpdateDelete();
		db.compileStatement("DELETE from following").executeUpdateDelete();
		db.compileStatement("DELETE from permissions").executeUpdateDelete();

	}

	public Cursor getPermissionsForDoc(String docid) {
		String sql = "SELECT username,userid,permission FROM permissions where docid=?";
		Cursor cursor = db.rawQuery(sql, new String[] {docid});
		return cursor;
	}



}