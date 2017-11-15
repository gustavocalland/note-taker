package ca.finalproject.notetaker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by gustavo on 2017-04-30.
 */

public class NoteDetail {
    public static final int DETAIL_TYPE_TEXT  = 0;
    public static final int DETAIL_TYPE_IMAGE = 1;

    private Integer detailId;
    private int originalPosition;
    private int position;
    private String content;
    private int contentType;
    private Integer noteId;

    public NoteDetail(Integer id, int position, String content, int contentType, Integer noteId){
        this.detailId = id;
        this.originalPosition = position;
        this.position = position;
        this.content  = content;
        this.contentType = contentType;
        this.noteId = noteId;
    }

    /**
     *  Goes through each element in the list and checks if one of them needs an insert or an update
     * on the database
     * @return TRUE if yes, FALSE if not
     */
    public static boolean doesListNeedInsertUpdate(ArrayList<NoteDetail> list){
        boolean needInsertOrUpdate = false;
        for(NoteDetail n : list){
            if(n.needsInsert() || n.needsUpdate()){
                needInsertOrUpdate = true;
            }
        }
        return needInsertOrUpdate;
    }

    /**
     *  Checks if this noteDetail needs to be inserted into the database or not. If it already has an
     * ID, it means that it has been inserted before
     * @return TRUE if yes, FALSE if no
     */
    public boolean needsInsert(){ return this.detailId == null && this.position>=0; }
    /**
     *   Checks if this noteDetail needs to be updated into the database or not. If the text was
     *  changed by the user, it means it does.
     * @return TRUE if yes, FALSE if no
     */
    public boolean needsUpdate(){ return this.detailId!= null && this.originalPosition != this.position; }

    public Integer getDetailId() { return detailId; }
    public int getPosition() { return position; }
    public void setPosition(int pos){position = pos;};
    public int getNoteId() { return noteId; }
    public String getContent() { return content; }
    public int getContentType() { return contentType; }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

class TextDetail extends NoteDetail{
    private String initialText;
    private EditText editText;

    public TextDetail(){
        this(null, "", 0, null);
    }

    public TextDetail(int position){
        this(null, "", position, null);
    }

    public TextDetail(Integer id, String text, int position, Integer noteId){
        super(id, position, text, DETAIL_TYPE_TEXT, noteId);
        initialText = text;
        this.editText = new EditText(NoteDetailsActivity.context);
        this.editText.setText(text);
        this.editText.setTextColor(Color.BLACK);
        this.editText.setBackgroundColor(Color.TRANSPARENT);
        this.editText.setHint("Type your text here...");
    }

    public void addToContainer(LinearLayout l){
        l.addView(this.editText);
    }
    public void removeFromContainer(LinearLayout l){
        l.removeView(this.editText);
    }


    public void requestFocus(){
        this.editText.requestFocus();
    }

    /**
     *   Checks if this noteDetail needs to be updated into the database or not. If the text was
     *  changed by the user, it means it does.
     * @return TRUE if yes, FALSE if no
     */
    @Override
    public boolean needsUpdate(){
        if(!super.needsUpdate()) {
            if (!getContent().equals(initialText)) {
                return true;
            }

        }
        return false;
    }

    @Override
    public String getContent(){
        return editText.getText().toString();
    }
    public void setContent(String str) { editText.setText(str);}
    public void appendText(String newText) {
        String currentText = this.editText.getText().toString();
        this.editText.setText(currentText+" "+newText);
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////
class ImageDetail extends NoteDetail{
    private ImageView imgView;

    public ImageDetail(Integer id, String path, int position, Integer noteId){
        super(id, position, path, DETAIL_TYPE_IMAGE, noteId);
        this.imgView = new ImageView(NoteDetailsActivity.context);

        //Tags the imageview with the path information, so we can zoom on the image later
        this.imgView.setTag(path);
    }

    public void addToContainer(LinearLayout l){
        l.addView(this.imgView);
    }

    public ImageView getImgView() { return imgView; }
}
////////////////////////////////////////////////////////////////////////////////////////////////////