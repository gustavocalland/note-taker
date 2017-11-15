package ca.finalproject.notetaker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * The following issues won't be fixed on time to deliver this project, but they will be fixed in
 *  the next version of the app, that I will upload on "github.com/gustavocalland"
 *
 *
 * TODO:
 * * Ask for camera permissions only when I'm about to take a picture
 * * Only start camera after getting geolocation (create LOADING screen?). The geolocation should not
 * be taken in the "onCreate" method, but only when the camera is started
 * * Block taking pictures if the user doesnt have permissions (onRequestPermissionsResult). Crash
 * the app if the user doesnt have store permissions
 * * When taking the picture, pass the name of the image through the intent (didnt work for some reason)
 * * Looks like coarse_location permissions don't work for some reason. Check it.
 * * Do something when google play services is not detected
 *
 */

public class MainActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EDITOR_REQUEST_CODE = 1001;
    private CursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cursorAdapter = new NotesCursorAdapter(this, null, 0);

        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, NoteDetailsActivity.class);
                Uri uri = Uri.parse(NotesContentProvider.CONTENT_URI_NOTES + "/" + id);
                intent.putExtra(NotesContentProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });

        getLoaderManager().initLoader(0, null, this);

        PermissionManager.verifyAllPermissions(this);
        if(PermissionManager.isGogleServicesAvailable(this)){
            //Do something
        };
    }



    /** Called when the ADD NOTE button is pressed */
    private void insertNote(String noteTitle) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle);
        Uri noteUri = getContentResolver().insert(NotesContentProvider.CONTENT_URI_NOTES, values);
        Log.d("MainActivity", "Inserted note " + noteUri.getLastPathSegment());
    }


    /** Called when a note is clicked. Opens the view that allows it being edited */
    public void openEditorForNewNote(View view) {
        Intent intent = new Intent(this, NoteDetailsActivity.class);
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_delete_all:
                deleteAllNotes();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void deleteAllNotes() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            getContentResolver().delete(NotesContentProvider.CONTENT_URI_DETAILS,null,null);
                            //TODO: Delete all saved pictures
                            getContentResolver().delete(NotesContentProvider.CONTENT_URI_NOTES, null, null);
                            restartLoader();
                            Toast.makeText(MainActivity.this, "All notes deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all notes?")
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NotesContentProvider.CONTENT_URI_NOTES, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    //Callback function called when the user finishes editing a note and returns to the main screen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            restartLoader();
        }
    }
}
