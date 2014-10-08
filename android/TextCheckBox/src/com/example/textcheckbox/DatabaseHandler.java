package com.example.textcheckbox;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
	 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "notesManager";
 
    // Contacts table name
    private static final String TABLE_NOTES = "notes";
 
    // Notes Table Columns names
    private static final String KEY_ID = "uuid";
    private static final String KEY_NOTE = "note";
    private static final String KEY_COLOR = "color";
   
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
    	  String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                  + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NOTE + " TEXT," +KEY_COLOR+" TEXT NOT NULL"+")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
 
        // Create tables again
        onCreate(db);
    }
    
    
    
    
    // Adding new note
    void addNote(Notes note) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE, note.getNote()); // Contact Name
        values.put(KEY_COLOR,note.getColor());
        System.out.println(note.getColor()+"here");
        // Inserting Row
        db.insert(TABLE_NOTES, null, values);
        db.close(); // Closing database connection
    }
 
    // Getting single note
    Notes getNote(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_NOTES, new String[] { KEY_ID,
                KEY_NOTE,KEY_COLOR }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        Notes note = new Notes(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),cursor.getString(2));
        System.out.println(note.getColor()+"on getting note");
        // return note
        db.close();
        return note;
    }
     
    // Getting All notes
    public List<Notes> getAllNotes() {
        List<Notes> noteList = new ArrayList<Notes>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NOTES;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Notes note = new Notes();
                note.setID(Integer.parseInt(cursor.getString(0)));
                note.setName(cursor.getString(1));
                note.setColor(cursor.getString(2));
               
                // Adding notes to list
                noteList.add(note);
            } while (cursor.moveToNext());
        }
        db.close();
        // return note list
        return noteList;
    }
 
    // Updating single note
    public int updateNote(Notes note) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE, note.getNote());
        values.put(KEY_COLOR, note.getColor());
        
        // updating row
       	int ret=db.update(TABLE_NOTES, values, KEY_ID + " = ?",
                new String[] { String.valueOf(note.getID()) });
        db.close();
        return ret;
        
    }
 
    // Deleting single note
    public void deleteNote(Notes note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, KEY_ID + " = ?",
                new String[] { String.valueOf(note.getID()) });
        db.close();
    }
 
 
    // Getting notes Count
    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        
        
        // return count
        int ret=cursor.getCount();
        db.close();
        return ret;
    }
}