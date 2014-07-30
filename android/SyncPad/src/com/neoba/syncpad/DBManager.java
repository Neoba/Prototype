package com.neoba.syncpad;

import java.io.IOException;
import java.util.Date;

import net.dongliu.vcdiff.VcdiffDecoder;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;

import com.neoba.syncpad.ByteMessenger.document;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class DBManager {

	// Database Version
	public static final int DATABASE_VERSION = 1;
	// Database Name
	public static final String DATABASE_NAME = "docs.db";
	public static final String DATABASE_TABLE = "docs";
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
					+ "id TEXT PRIMARY KEY, " + "diff TEXT, "
					+ "dict TEXT,age INTEGER,title TEXT,permission INTEGER,date LONG )";
			db.execSQL(CREATE_BOOK_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS docs");
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
		document d = new document(c.getString(0), c.getString(4), c.getBlob(1),
				c.getInt(3), c.getString(2), (byte) c.getInt(5));
		return d;
	}

	public void insertDoc(document doc) {
		String sql = "INSERT INTO docs (id,diff,dict,age,title,permission,date) VALUES(?,?,?,?,?,?,?)";
		SQLiteStatement insertStmt = db.compileStatement(sql);
		insertStmt.clearBindings();
		insertStmt.bindString(1, doc.id);
		insertStmt.bindBlob(2, doc.diff);
		insertStmt.bindString(3, doc.dict);
		insertStmt.bindLong(4, doc.age);
		insertStmt.bindString(5, doc.title);
		insertStmt.bindLong(6, doc.permission);
		insertStmt.bindLong(7, new Date().getTime());
		insertStmt.executeInsert();
	}
	public void updateContent(document doc) {
		String sql = "update docs set diff=?,dict=?,age=?,date=? where id=?";
		SQLiteStatement updateStmt = db.compileStatement(sql);
		updateStmt.clearBindings();
		updateStmt.bindBlob(1, doc.diff);
		updateStmt.bindString(2, doc.dict);
		updateStmt.bindLong(3, doc.age);
		updateStmt.bindLong(4,new Date().getTime());
		updateStmt.bindString(5, doc.id);
		updateStmt.executeUpdateDelete();
	}

	public Cursor getAllDocs() {

		String sql = "SELECT rowid _id ,* FROM docs";
		Cursor cursor = db.rawQuery(sql, new String[] {});
		return cursor;

	}

	public String getDoc(int rowid) throws IOException, VcdiffDecodeException {

		String sql = "SELECT dict,diff from docs where rowid=?";
		Cursor c = db.rawQuery(sql, new String[] { Integer.toString(rowid) });
		c.moveToFirst();
		String dict = c.getString(0);
		byte[] diff = c.getBlob(1);
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

	public void Truncate() {
		String sql = "DELETE from docs";
		SQLiteStatement insertStmt = db.compileStatement(sql);
		insertStmt.clearBindings();
		insertStmt.executeUpdateDelete();
		
	}

}