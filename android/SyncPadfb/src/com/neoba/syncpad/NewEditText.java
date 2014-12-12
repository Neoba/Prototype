package com.neoba.syncpad;

import java.util.Random;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import hu.scythe.droidwriter.DroidWriterEditText;

public class NewEditText extends DroidWriterEditText {

    private Random r = new Random();

    public NewEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NewEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NewEditText(Context context) {
        super(context);
    }

    public void setRandomBackgroundColor() {
        setBackgroundColor(Color.rgb(r.nextInt(256), r.nextInt(256), r
                .nextInt(256)));
    }

    
    
 
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
    	
    	 //Passing FALSE as the SECOND ARGUMENT (fullEditor) to the constructor 
        // will result in the key events continuing to be passed in to this 
        // view.  Use our special BaseInputConnection-derived view
        InputConnectionAccomodatingLatinIMETypeNullIssues baseInputConnection = 
          new InputConnectionAccomodatingLatinIMETypeNullIssues(this, false);

         //In some cases an IME may be able to display an arbitrary label for a 
         // command the user can perform, which you can specify here.  A null value
         // here asks for the default for this key, which is usually something 
         // like Done.
         outAttrs.actionLabel = null;

         //Special content type for when no explicit type has been specified. 
         // This should be interpreted (by the IME that invoked 
         // onCreateInputConnection())to mean that the target InputConnection 
         // is not rich, it can not process and show things like candidate text 
         // nor retrieve the current text, so the input method will need to run 
         // in a limited "generate key events" mode.  This disables the more 
         // sophisticated kinds of editing that use a text buffer.
         outAttrs.inputType = InputType.TYPE_NULL;

         //This creates a Done key on the IME keyboard if you need one
         //outAttrs.imeOptions = EditorInfo//;

         return baseInputConnection;
    	
//        return new NewInputConnection(super.onCreateInputConnection(outAttrs),
//                true);
    }

    private class NewInputConnection extends InputConnectionWrapper {

        public NewInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                NewEditText.this.setRandomBackgroundColor();
                // Un-comment if you wish to cancel the backspace:
                // return false;
            }
            return super.sendKeyEvent(event);
        }

    }

}