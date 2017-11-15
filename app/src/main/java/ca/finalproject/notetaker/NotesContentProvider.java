package ca.finalproject.notetaker;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class NotesContentProvider extends ContentProvider{

    private static final String AUTHORITY = "ca.finalproject.notetaker.notesprovider";
    private static final String BASE_PATH_NOTES = "notes";
    private static final String BASE_PATH_DETAILS = "noteDetails";
    private static final String PATH_DETAILS_FOR_NOTE = "noteDetails/forNote";

    public static final Uri CONTENT_URI_NOTES = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_NOTES );
    public static final Uri CONTENT_URI_DETAILS = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_DETAILS );
    public static final Uri CONTENT_URI_DETAILS_FOR_NOTE = Uri.parse("content://" + AUTHORITY + "/" + PATH_DETAILS_FOR_NOTE);

    // Constant to identify the requested operation
    private static final int NOTES = 1;
    private static final int NOTES_ID = 2;
    private static final int DETAILS = 3;
    private static final int DETAILS_NOTE_ID = 4;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "NoteId";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH_NOTES, NOTES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH_NOTES +  "/#", NOTES_ID);
        uriMatcher.addURI(AUTHORITY, BASE_PATH_DETAILS, DETAILS);
        uriMatcher.addURI(AUTHORITY, PATH_DETAILS_FOR_NOTE + "/#", DETAILS_NOTE_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case NOTES_ID:
                selection = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();
                //No break. Jumps to the next condition...
            case NOTES:
                return database.query(DBOpenHelper.TABLE_NOTES, DBOpenHelper.ALL_NOTE_COLUMNS,
                        selection, null, null, null, DBOpenHelper.NOTE_CREATED + " DESC");
            case DETAILS_NOTE_ID:
                selection = DBOpenHelper.DETAIL_NOTE_ID + "=" + uri.getLastPathSegment();
                //No break. Jumps to the next condition...
            case DETAILS:
                return database.query(DBOpenHelper.TABLE_NOTE_DETAILS, DBOpenHelper.ALL_DETAIL_COLUMNS,
                        selection, null, null, null, DBOpenHelper.DETAIL_POSITION + " ASC");
            default:
                throw new SQLException("Failed do a query " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;
        switch (uriMatcher.match(uri)){
            case NOTES:
                id = database.insert(DBOpenHelper.TABLE_NOTES, null, values);
                return Uri.parse(BASE_PATH_NOTES + "/" + id);
            case DETAILS:
                id = database.insert(DBOpenHelper.TABLE_NOTE_DETAILS, null, values);
                return Uri.parse(BASE_PATH_DETAILS + "/" + id);
            default:
                throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)){
            case NOTES:
                return database.delete(DBOpenHelper.TABLE_NOTES, selection, selectionArgs);
            case DETAILS:
                return database.delete(DBOpenHelper.TABLE_NOTE_DETAILS, selection, selectionArgs);
            default:
                throw new SQLException("Failed to delete table " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)){
            case NOTES:
                return database.update(DBOpenHelper.TABLE_NOTES, values, selection, selectionArgs);
            case DETAILS:
                return database.update(DBOpenHelper.TABLE_NOTE_DETAILS, values, selection, selectionArgs);
            default:
                throw new SQLException("Failed to update table " + uri);
        }
    }
}
