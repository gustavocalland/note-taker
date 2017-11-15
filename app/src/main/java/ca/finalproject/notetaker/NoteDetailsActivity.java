package ca.finalproject.notetaker;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NoteDetailsActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1;
    public static final String IMG_FOLDER_PATH = Environment.getExternalStorageDirectory().toString()
                                                    + File.separator + "NoteTaker"
                                                    + File.separator + "saved_images";
    public static final File IMAGES_FOLDER = new File(IMG_FOLDER_PATH);

    public static Context context;

    private String action;
    private String noteFilter;
    private String noteDetailFilter;
    private String thisNoteId;

    private boolean didTitleChange = false;

    private double currentLatitude;
    private double currentLongitude;

    private LinearLayout getContainer(){
        return (LinearLayout) findViewById(R.id.notesEditorContainer);
    }

    /** Interface depicting a function to be called once the user location is obtained */
    public static interface TitleObtainedCallback {
        public void onTitleObtained(String newTitle);
    }

    private ArrayList<NoteDetail> details = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        NoteDetailsActivity.context = getApplicationContext();

        // Creates a folder that will store the images, if it already doesnt exist
        IMAGES_FOLDER.mkdirs();

        LocationProvider.getLocation(NoteDetailsActivity.context, new LocationProvider.LocationCallback(){
            @Override
            public void onLocationObtained(Location location) {
                currentLatitude  = location.getLatitude();
                currentLongitude = location.getLongitude();

                Log.d("GUSTAVO", "Location localized: "+currentLatitude+","+currentLongitude);
            }
        });

        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);

        MenuItem deleteItem      = menu.findItem(R.id.action_delete);
        MenuItem addImageItem    = menu.findItem(R.id.action_add_image);
        MenuItem changeTitleItem = menu.findItem(R.id.action_change_title);

        if (action.equals(Intent.ACTION_INSERT)) {
            deleteItem.setVisible(false);
            changeTitleItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                deleteNote();
                break;
            case R.id.action_add_image:
                takePicture();
                break;
            case R.id.action_change_title:
                getTitleInput(new TitleObtainedCallback() {
                    @Override
                    public void onTitleObtained(String newTitle) {
                        setTitle(newTitle);
                        didTitleChange = true;
                    }
                });
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }

    /////////////////////////////DATA FUNCTIONS/////////////////////////////////////////////////////

    /**
     *  Initializes this view with the default values, if inserting a new note; or the values
     * loaded from the database, if editing an already existing note
     */
    private void initData(){
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(NotesContentProvider.CONTENT_ITEM_TYPE);
        details.clear();

        //I'm creating a new note
        if (uri == null) {
            action = Intent.ACTION_INSERT;

            // Setting the title of the view with a placeholder. Before exiting this view (and
            //saving the note), a new title will be asked from the user. This title will be
            //displayed when the user tries to edit the note in the future.
            setTitle("New note");
            TextDetail newText = new TextDetail();
            details.add(newText);
            newText.addToContainer(getContainer());
            newText.requestFocus();
        } else {//Editing an already existing note
            action = Intent.ACTION_EDIT;
            thisNoteId = uri.getLastPathSegment();

            // Searches the database for the note with the ID specified and sets the title of the
            //page with the note's title.
            noteFilter = DBOpenHelper.NOTE_ID + "=" + thisNoteId;
            Cursor noteCursor = getContentResolver().query(uri, DBOpenHelper.ALL_NOTE_COLUMNS, noteFilter, null, null);
            noteCursor.moveToFirst();
            setTitle(noteCursor.getString(noteCursor.getColumnIndex(DBOpenHelper.NOTE_TITLE)));

            // Searches the database for all the noteDetails with the noteId specified, and sets the
            //contents of the view with the obtained values
            Uri noteDetailURI = Uri.parse(NotesContentProvider.CONTENT_URI_DETAILS_FOR_NOTE + "/" + thisNoteId);
            noteDetailFilter = DBOpenHelper.DETAIL_NOTE_ID + "=" + thisNoteId;
            Cursor noteDetailCursor = getContentResolver().query(noteDetailURI, DBOpenHelper.ALL_NOTE_COLUMNS, noteDetailFilter, null, null);
            noteDetailCursor.moveToFirst();

            TextDetail lastTextNote = null;
            do{
                int contentType = noteDetailCursor.getInt(noteDetailCursor.getColumnIndex(DBOpenHelper.DETAIL_CONTENT_TYPE));
                switch (contentType){
                    case NoteDetail.DETAIL_TYPE_TEXT:
                        TextDetail text = new TextDetail(noteDetailCursor.getInt(noteDetailCursor.getColumnIndex(DBOpenHelper.DETAIL_ID)),
                                noteDetailCursor.getString(noteDetailCursor.getColumnIndex(DBOpenHelper.DETAIL_CONTENT)),
                                noteDetailCursor.getInt(noteDetailCursor.getColumnIndex(DBOpenHelper.DETAIL_POSITION)),
                                noteDetailCursor.getInt(noteDetailCursor.getColumnIndex(DBOpenHelper.DETAIL_NOTE_ID))
                        );
                        text.addToContainer(getContainer());
                        details.add(text);
                        lastTextNote = text;
                        break;
                    case NoteDetail.DETAIL_TYPE_IMAGE:
                        ImageDetail img = new ImageDetail(noteDetailCursor.getInt(noteDetailCursor.getColumnIndex(DBOpenHelper.DETAIL_ID)),
                                noteDetailCursor.getString(noteDetailCursor.getColumnIndex(DBOpenHelper.DETAIL_CONTENT)),
                                noteDetailCursor.getInt(noteDetailCursor.getColumnIndex(DBOpenHelper.DETAIL_POSITION)),
                                noteDetailCursor.getInt(noteDetailCursor.getColumnIndex(DBOpenHelper.DETAIL_NOTE_ID))
                        );
                        img.addToContainer(getContainer());
                        details.add(img);
                        loadImageFromDevice(img.getContent(),img.getImgView());
                        break;
                    default:
                        throw new NullPointerException("Invalid type: "+contentType);

                }
            }while(noteDetailCursor.moveToNext());
            if (lastTextNote != null) {
                lastTextNote.requestFocus();
            }
        }
    }

    /**
     * Called when I press the button to return to the main view
     */
    private void finishEditing() {
        switch (action) {
            case Intent.ACTION_INSERT:
                if (details.size() == 1 && details.get(0).getContent().isEmpty()) {
                    setResult(RESULT_CANCELED);
                } else {
                    getTitleInput(new TitleObtainedCallback() {
                        @Override
                        public void onTitleObtained(String newTitle) {
                            setTitle(newTitle);
                            insertNotes(newTitle);
                            finish();
                        }
                    });
                }
                break;
            case Intent.ACTION_EDIT:
                if (details.size() == 1 && details.get(0).getContent().isEmpty()) {
                    deleteNote();
                } else if (!NoteDetail.doesListNeedInsertUpdate(details) && !didTitleChange) {
                    setResult(RESULT_CANCELED);
                } else {
                    updateNotes();
                }
                finish();
        }
    }

    private void deleteNote() {
        getContentResolver().delete(NotesContentProvider.CONTENT_URI_DETAILS, noteDetailFilter, null);
        getContentResolver().delete(NotesContentProvider.CONTENT_URI_NOTES, noteFilter, null);
        Toast.makeText(this, "Note deleted!", Toast.LENGTH_SHORT).show();
        //TODO: Delete pictures from the system
        setResult(RESULT_OK);
        finish();
    }

    private void updateNotes() {
        if(didTitleChange){
            ContentValues values = new ContentValues();
            values.put(DBOpenHelper.NOTE_TITLE,getTitle().toString());
            String filter = DBOpenHelper.NOTE_ID + "=" + thisNoteId;
            getContentResolver().update(NotesContentProvider.CONTENT_URI_NOTES, values, filter, null);
            didTitleChange = false;
        }

        insertUpdateNoteDetails();
        Toast.makeText(this, "Note updated!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNotes(String noteTitle) {
        ContentValues noteValues = new ContentValues();
        noteValues.put(DBOpenHelper.NOTE_TITLE, noteTitle);
        Uri newNoteURI = getContentResolver().insert(NotesContentProvider.CONTENT_URI_NOTES, noteValues);
        thisNoteId = newNoteURI.getLastPathSegment();

        insertUpdateNoteDetails();

        setResult(RESULT_OK);
    }

    /**
     *  Goes through the list of all note details created for this view and inserts/updates them
     */
    private void insertUpdateNoteDetails(){
        if(thisNoteId==null){
            throw new RuntimeException("The note ID should have been set by the time you call " +
                    "this method. Either by 'insertNotes' or 'initData' methods...");
        }

        ContentValues detailValues = new ContentValues();
        for(NoteDetail n : details){
            detailValues.clear();

            detailValues.put(DBOpenHelper.DETAIL_NOTE_ID,Integer.valueOf(thisNoteId));
            detailValues.put(DBOpenHelper.DETAIL_POSITION,n.getPosition());
            detailValues.put(DBOpenHelper.DETAIL_CONTENT,n.getContent());
            detailValues.put(DBOpenHelper.DETAIL_CONTENT_TYPE,n.getContentType());

            if(n.needsInsert()){
                getContentResolver().insert(NotesContentProvider.CONTENT_URI_DETAILS, detailValues);
            }else if(n.needsUpdate()){
                String filter = DBOpenHelper.DETAIL_ID + "=" + n.getDetailId();
                getContentResolver().update(NotesContentProvider.CONTENT_URI_DETAILS, detailValues, filter, null);
            }
        }
    }

    private void getTitleInput(final TitleObtainedCallback callback){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insert a title for this note");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputTitleText = input.getText().toString();
                callback.onTitleObtained(inputTitleText);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    ////////////////////////UTILITY FUNCTIONS///////////////////////////////////////////////////////
    private String camPicImageName = "";
    /**
     *  Opens up the device's camera, takes a picture, saves it on the "{@value: #IMG_FOLDER_PATH}"
     * and then shows it on an ImageView
     */
    private void takePicture(){
        //Defines the name and path of the image that will be taken
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        camPicImageName = "NotePicture-"+currentDateTime+".png";
        Uri fileUri = Uri.fromFile(new File(IMG_FOLDER_PATH, camPicImageName));

        // Opens the camera and takes a picture. The picture is saved with the "imageName" provided
        //above, on the path especified by the "fileUri"
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(takePictureIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST:
                    setExifCoordinates(camPicImageName);
                    createImageViewAndLoadImageFromDevice(camPicImageName);
                    break;
                default:
                    Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * Sets the exif coordinates data for the image that was just created
     * @param imgName
     * @throws IOException
     */
    private void setExifCoordinates(String imgName){
        try {
            ExifInterface exif = new ExifInterface(NoteDetailsActivity.IMG_FOLDER_PATH+"/"+imgName);
            if (exif != null) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPSDecoder.convert(currentLatitude));
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPSDecoder.latitudeRef(currentLatitude));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPSDecoder.convert(currentLongitude));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPSDecoder.longitudeRef(currentLongitude));

                exif.saveAttributes();
            }
        } catch (IOException e) {
            Log.e("NoteDetails","Error while trying to save the exif information of a picture",e);
        }
    }

    /**
     *  Creates an imageView and then adds it to the container.
     *  This method assumes that an image with this name already exists on the system, and tries to
     * load it to display on the imageView
     * @param imageName The name of the image that is saved on the system.
     */
    private void createImageViewAndLoadImageFromDevice(String imageName){
        ImageDetail addedImage = new ImageDetail(null, imageName, details.size(),
                null //This may be a new note, without a noteID. Thus, the NoteID will only be set when the note is saved
        );
        addedImage.addToContainer(getContainer());
        details.add(addedImage);
        loadImageFromDevice(imageName, addedImage.getImgView());

        //After adding the image, also add a EditText after the image, and requests focus
        TextDetail addedTextAfterImage = new TextDetail(details.size());
        addedTextAfterImage.addToContainer(getContainer());
        addedTextAfterImage.requestFocus();
        details.add(addedTextAfterImage);
    }

    /**
     * Loads a image saved on the system and displays it on an ImageView
     * @param imgName The name of the image that is saved on the system.
     * @param targetImageView ImageView where the image will be displayed
     */
    private void loadImageFromDevice(final String imgName, final ImageView targetImageView){
        Uri fileUri = Uri.fromFile(new File(IMG_FOLDER_PATH, imgName));

        try {
            //After the image is taken, we get it's thumbnail to show on the imageView on the screen
            Bitmap bitmap = ImageLoader.getThumbnail(fileUri, this);
            targetImageView.setImageBitmap(bitmap);
            targetImageView.setTag(fileUri.getEncodedPath());
            targetImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPopupImageOptions(v, targetImageView);
                }
            });
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void openPopupImageOptions(final View v, final ImageView targetImgView){
        CharSequence options[] = new CharSequence[] {"View", "Show on Map", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What do you want to do with this image?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selectedOption) {
                String path = (String) v.getTag();
                switch (selectedOption){
                    case 0:
                        if (path != null) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri imgUri = Uri.parse("file://" + path);
                            intent.setDataAndType(imgUri, "image/*");
                            startActivity(intent);
                        }
                        break;
                    case 1:
                        try {
                            ExifInterface exif = new ExifInterface(path);
                            double obtainedLatitude = GPSDecoder.decode(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE), exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
                            double obtainedLongitude = GPSDecoder.decode(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE), exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
                            Log.d("NoteTaker", "Picture geolocation taken: "+obtainedLatitude+","+obtainedLongitude);

                            Intent intent = new Intent(NoteDetailsActivity.this, MapActivity.class);
                            intent.putExtra("currentLatitude",obtainedLatitude);
                            intent.putExtra("currentLongitude",obtainedLongitude);
                            startActivity(intent);
                        }catch(IOException e){
                            Log.e("NoteDetails","Error while trying to obtain the exif information of a picture",e);
                        }
                        break;
                    case 2:
                        LinearLayout container = getContainer();

                        NoteDetail targetNoteDetail = null;
                        //Searches for the imgView on the details list
                        for(NoteDetail d : details) {
                            if (d.getContentType() == NoteDetail.DETAIL_TYPE_IMAGE
                                    && ((ImageDetail) d).getImgView() == targetImgView) {
                                targetNoteDetail = d;
                                break;
                            }
                        }

                        if(targetNoteDetail!=null) {
                            //Delets the selected ImageView and links the editText behind it
                            //with the one after it
                            int indexImgView = details.indexOf(targetNoteDetail);
                            TextDetail previousTextDetail = (TextDetail) details.get(indexImgView - 1);
                            TextDetail posteriorTextDetail = (TextDetail) details.get(indexImgView + 1);

                            previousTextDetail.appendText(posteriorTextDetail.getContent());

                            container.removeView(targetImgView);
                            posteriorTextDetail.removeFromContainer(container);

                            // Check if there are more elements in the details after the imgView and the
                            //editText after it, then adjust their positions
                            if (details.size() > indexImgView + 2) {
                                for (int i = indexImgView + 2; i < details.size(); i++) {
                                    NoteDetail nd = details.get(i);
                                    nd.setPosition(nd.getPosition() - 2);
                                }
                            }

                            //Elements with position -1 will be deleted during the saveUpdate
                            posteriorTextDetail.setPosition(-1);
                            targetNoteDetail.setPosition(-1);

                            details.remove(posteriorTextDetail);
                            details.remove(targetNoteDetail);

                            if(posteriorTextDetail.getDetailId()!=null) {
                                String filter = DBOpenHelper.DETAIL_ID + "=" + posteriorTextDetail.getDetailId();
                                getContentResolver().delete(NotesContentProvider.CONTENT_URI_DETAILS, filter, null);
                            }
                            if(targetNoteDetail.getDetailId()!=null){
                                String filter = DBOpenHelper.DETAIL_ID + "=" + targetNoteDetail.getDetailId();
                                getContentResolver().delete(NotesContentProvider.CONTENT_URI_DETAILS, filter, null);
                            }

                        }else{
                            throw new RuntimeException("ImageView not found on the details list. " +
                                    "This should not be happening");
                        }

                    break;
                }
            }
        });
        builder.show();
    }
}