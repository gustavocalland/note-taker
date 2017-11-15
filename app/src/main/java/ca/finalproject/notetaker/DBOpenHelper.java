package ca.finalproject.notetaker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 2;

    //Constants for identifying table and columns
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID = "_id";
    public static final String NOTE_TITLE = "noteTitle";
    public static final String NOTE_CREATED = "noteCreated";

    public static final String TABLE_NOTE_DETAILS = "note_details";
    public static final String DETAIL_ID = "detailId";
    public static final String DETAIL_POSITION = "detailPosition";
    public static final String DETAIL_CONTENT = "detailContent";
    public static final String DETAIL_CONTENT_TYPE = "detailContentType";
    public static final String DETAIL_NOTE_ID = "detailNoteId";

    public static final String[] ALL_NOTE_COLUMNS = {NOTE_ID, NOTE_TITLE, NOTE_CREATED};
    public static final String[] ALL_DETAIL_COLUMNS = {DETAIL_ID, DETAIL_POSITION , DETAIL_CONTENT, DETAIL_CONTENT_TYPE, DETAIL_NOTE_ID};

    //SQL to create table
    private static final String TABLE_NOTES_CREATE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NOTE_TITLE + " TEXT, " +
                NOTE_CREATED + " TEXT default CURRENT_TIMESTAMP" +
            ")";

    //SQL to create table
    private static final String TABLE_DETAILS_CREATE =
            "CREATE TABLE " + TABLE_NOTE_DETAILS + " (" +
                DETAIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DETAIL_POSITION + " INTEGER, " +
                DETAIL_CONTENT + " TEXT, " +
                DETAIL_CONTENT_TYPE + " INTEGER, " +
                DETAIL_NOTE_ID + " INTEGER," +
                " FOREIGN KEY ("+DETAIL_NOTE_ID+") REFERENCES "+TABLE_NOTES+"("+NOTE_ID+")" +
            ")";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_NOTES_CREATE);
        db.execSQL(TABLE_DETAILS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE_DETAILS);
        onCreate(db);
    }
}
