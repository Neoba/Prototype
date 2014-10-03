
package com.example.textcheckbox;


import hu.scythe.droidwriter.DroidWriterEditText;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.textcheckbox.R.id;


import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Build;

public class MainActivity extends Activity  {
	//arraylist for each linearlayout
	ArrayList<LinearLayout> layoutList;
	int value;
	//ArrayList for each edittext
	ArrayList<DroidWriterEditText> editList;
	
	int count; //the count of linearlayouts or edittexts.
	int prev_val; //(the index of edittext in focus)-1
	CheckBox c; //the checkbox adder
	String s_prev; //the string in edittext in focus.
	int fg; 
	String colorcode;
	int fld;
	Spanned s;
	ToggleButton bb,ib,ub; //the bold , italic and underline togglebutton respectively
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        c=(CheckBox)findViewById(R.id.checkBox1);
        bb=(ToggleButton)findViewById(R.id.boldbutton);
        ib=(ToggleButton)findViewById(R.id.italicbutton);
        ub=(ToggleButton)findViewById(R.id.underbutton);
        layoutList=new ArrayList<LinearLayout>();
        editList=new ArrayList<DroidWriterEditText>();
        count=0;
        fg=0;
        prev_val=-1;
        s_prev="";
        value = getIntent().getExtras().getInt("uuid");
        fld=0;
        if(value!=0)
        {
        	colorsetter();
        	fetcher();
        	 modifier();
        }
        else
        {
        	colorsetter();
        	 modifier();
        
        }
//        /*
//         * The bold button clicked function
//         */
//        bb.setOnClickListener(new OnClickListener() {
//        	 
//    		@Override
//    		public void onClick(View v) {
//     
//    		 if(bb.isChecked())
//    		 {
//    			 Toast.makeText(getApplicationContext(), "checked",Toast.LENGTH_SHORT).show();
//    			
//    			 Spannable spannable = (Spannable)editList.get(prev_val+1).getText();
//    			 StyleSpan boldSpan = new StyleSpan( Typeface.BOLD );
//    			    
//    			 spannable.setSpan( boldSpan, editList.get(prev_val+1).getSelectionStart(),editList.get(prev_val+1).getSelectionStart(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//    			  
//    			
//    			  
//    			 int startSelection = editList.get(prev_val+1).getSelectionStart();
//    			 int endSelection = editList.get(prev_val+1).getSelectionEnd();
//    			 if(startSelection>endSelection){
//
//    			     startSelection = editList.get(prev_val+1).getSelectionEnd();
//    			     endSelection = editList.get(prev_val+1).getSelectionStart();
//
//    			 }
//    			 Spannable s = editList.get(prev_val+1).getText();
//    			 s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startSelection, endSelection, 0);
//    			((EditText)editList.get(prev_val+1)).setText(s);
//    			((EditText)editList.get(prev_val+1)).setSelection(startSelection, endSelection);
//    			//editList.add(prev_val+1,e);
//    		 }
//     
//    		}
//     
//    	});
//   









    }
    
    void checkBoxAdder()
    {
    	LinearLayout LL = new LinearLayout(this);
	    CheckBox ch = new CheckBox(this);
	    final DroidWriterEditText edittext=new DroidWriterEditText(this);
	    edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		edittext.setBackgroundColor(Color.parseColor(colorcode));
		LL.setBackgroundColor(Color.parseColor(colorcode));
	    edittext.setSingleLine(false);
		//edittext.setLineSpacing(2, 2);
		edittext.setPadding(4,20,0,20);
	   // edittext.setBackgroundColor(Color.WHITE);
	    bb=(ToggleButton)findViewById(R.id.boldbutton);
        ib=(ToggleButton)findViewById(R.id.italicbutton);
        ub=(ToggleButton)findViewById(R.id.underbutton);
	    edittext.setBoldToggleButton(bb);
		edittext.setItalicsToggleButton(ib);
		edittext.setUnderlineToggleButton(ub);
	    LL.setOrientation(LinearLayout.HORIZONTAL);
	    LL.setPadding(20, 0, 0, 0);
	    LayoutParams LLParams = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
	    LinearLayout.LayoutParams ladderParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        ch.setLayoutParams(ladderParams);
	       
        edittext.setLayoutParams(dummyParams);
       // edittext.setPadding(-1,0, 0, 0);
        if(s_prev.equals(""))
        {
        	String sv="";
        	edittext.setText(sv);
	    }
        else
        {
        	if(Html.fromHtml(s_prev).length()>=2)
	       edittext.setText(Html.fromHtml(s_prev).subSequence(0, Html.fromHtml(s_prev).length()-2));
        	else
          edittext.setText(Html.fromHtml(s_prev).subSequence(0, Html.fromHtml(s_prev).length()));		
	       System.out.println(Html.fromHtml(s_prev));
        }
        LL.setLayoutParams(LLParams);
        LL.addView(ch);
        LL.addView(edittext);
        layoutList.add(prev_val+1, LL);
        editList.add(prev_val+1, edittext);
        
	     if((prev_val+1)!=count)
	     {
	    	 for(LinearLayout l1:layoutList)
	    	 {
	    		 ((LinearLayout) findViewById(R.id.rl)).removeView(l1);   
	    		 ((LinearLayout) findViewById(R.id.rl)).addView(l1);
	    	 }
	     }
	     else
	    	 ((LinearLayout) findViewById(R.id.rl)).addView(LL);
	       
	     count++;
	     edittext.setFocusableInTouchMode(true);
	     edittext.requestFocus();
	        
	        
	        
	     edittext.setOnTouchListener(new OnTouchListener() {
	           

	    	 @Override
	    	 public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub	
	    		 prev_val=editList.indexOf(edittext)-1;
	    		 ((DroidWriterEditText)editList.get(prev_val+1)).setBoldToggleButton(bb);
	    		 ((DroidWriterEditText)editList.get(prev_val+1)).setItalicsToggleButton(ib);
	    		 ((DroidWriterEditText)editList.get(prev_val+1)).setUnderlineToggleButton(ub);
	    		 return false;
	    	 }
	     });
	        

	     edittext.addTextChangedListener(new TextWatcher() {
	        	  

	    	 @Override
	    	 public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        	    //nothing needed here...
	        	  }

	    	 @Override
	    	 public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	    //nothing needed here...
	        		  
	    	 }

	    	 @Override
	    	 public void afterTextChanged(Editable e) {
					// TODO Auto-generated method stub
					
	    		 
//	    		 if(bb.isChecked())
//	    		 {
//	    			// Toast.makeText(getApplicationContext(), "checked",Toast.LENGTH_SHORT).show();
//	    			  Spannable spannable = (Spannable)editList.get(prev_val+1).getText();
//	    			    StyleSpan boldSpan = new StyleSpan( Typeface.BOLD );
//	    			    spannable.setSpan( boldSpan, editList.get(prev_val+1).getSelectionStart(),editList.get(prev_val+1).getSelectionStart(), Spannable.SPAN_INCLUSIVE_INCLUSIVE );
//	    		 }
	    		
	    		 
	    		 
	    		 
//	    		 bb=(ToggleButton)findViewById(R.id.boldbutton);
//	    		 int position =edittext.getSelectionStart();
//	  
//	    		 int styleStart=position-1;
//	    		 int cursorLoc=position;
//	                
//	                    if (position < 0){
//	                        position = 0;
//	                    }
//
//	                    if (position > 0){
//
//	                        if (styleStart > position || position > (cursorLoc + 1)){
//	                            //user changed cursor location, reset
//	                            if (position - cursorLoc > 1){
//	                                //user pasted text
//	                                styleStart = cursorLoc;
//	                            }
//	                            else{
//	                                styleStart = position - 1;
//	                            }
//	                    	 styleStart = position-1;
//	                        }
//	                    
//
//	                        if (bb.isChecked()){  
//	                            StyleSpan[] ss = e.getSpans(styleStart, position, StyleSpan.class);
//	                            Toast.makeText(getApplicationContext(), "fsdf",Toast.LENGTH_SHORT).show();
//	                            for (int i = 0; i < ss.length; i++) {
//	                                if (ss[i].getStyle() == android.graphics.Typeface.BOLD){
//	                                    e.removeSpan(ss[i]);
//	                                }
//	                            }
//	                            e.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), styleStart, position, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//	                        }
//	                        else
//	                        	 Toast.makeText(getApplicationContext(), "sorry",Toast.LENGTH_SHORT).show();
//	                    
//	                
//	                        
//	                        
//	                        
//	                        
//	    		 
//	                    }
	                    
	    		 
	    		 
	    		 
	    		 
	    		 
	    		 
	    		 
	    		 
	    		 
	    		 
	    		 
	    		 String textFromEditView = e.toString();
					 if(textFromEditView.length()>0)
					 {
						fg=0;
						if(textFromEditView.charAt(textFromEditView.length()-1)=='\n')
						{
							
						 
							if(textFromEditView.length()<=1)
							{
								int ac=editList.indexOf(edittext);
								LinearLayout l2=(LinearLayout)layoutList.get(ac);
							
								l2.setVisibility(View.INVISIBLE);
								layoutList.remove(ac);
								editList.remove(ac);
								((LinearLayout) findViewById(R.id.rl)).removeView(l2);
							
								count--;
							 
								if(ac>=1)
								{
									DroidWriterEditText e2=(DroidWriterEditText)editList.get(ac-1);
									int cursorPosition1 = e2.getSelectionStart();
									e2.setSelection(cursorPosition1);
									e2.setFocusableInTouchMode(true);
									e2.requestFocus();
									prev_val=editList.indexOf(e2)-1;
								}
								s_prev="";
								fg=1;
								prev_val=ac-1;
								c.toggle();
								editTextAdder();
							}
							else
							{
								int cursorPosition = edittext.getSelectionStart();
								 Spannable s = edittext.getText();
								 edittext.setText(s.subSequence(0,s.length()-1));
								// edittext.append(" ");
							
						 
								edittext.setSelection(edittext.getText().length());
								prev_val=editList.indexOf(edittext);
					//	 Toast.makeText(getApplicationContext(), prev_val+"", Toast.LENGTH_SHORT).show();
						 
						// Toast.makeText(getApplicationContext(), textFromEditView, Toast.LENGTH_SHORT).show();
								s_prev="";
							
								modifier();
					
							}
						 
						 
						}
					 }
					 else
					 {
						 int ac=editList.indexOf(edittext);
						 LinearLayout l2=(LinearLayout)layoutList.get(ac);
						 
						 l2.setVisibility(View.INVISIBLE);
						 layoutList.remove(ac);
						 editList.remove(ac);
						 ((LinearLayout) findViewById(R.id.rl)).removeView(l2);
						
						 count--;
						 
						 if(ac>=1)
						 {
							 DroidWriterEditText e2=(DroidWriterEditText)editList.get(ac-1);
							 int cursorPosition1 = e2.getSelectionEnd();
							 e2.setSelection(cursorPosition1);
							 e2.setFocusableInTouchMode(true);
							 e2.requestFocus();
							 prev_val=editList.indexOf(e2)-1;
						
						 }
						
						// prev_val=ac-1;
						 fg=1;
						
						 prev_val=ac-1;
							 s_prev="";
						 editTextAdder();
						 c.toggle();
						 
					}
		        	   //
				}
	        });
    	}

    	void editTextAdder()
    	{
    		LinearLayout LL = new LinearLayout(this);
    		
    		final DroidWriterEditText edittext1=new DroidWriterEditText(this);
    		edittext1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
    		edittext1.setSingleLine(false);
    		//edittext1.setLineSpacing(2, 2);
    		edittext1.setPadding(4,20,0,20);
    		
    		
    		edittext1.setBackgroundColor(Color.parseColor(colorcode));
    		//edittext1.setBackgroundColor(Color.WHITE);
    		 bb=(ToggleButton)findViewById(R.id.boldbutton);
    	        ib=(ToggleButton)findViewById(R.id.italicbutton);
    	        ub=(ToggleButton)findViewById(R.id.underbutton);
    		edittext1.setBoldToggleButton(bb);
    		edittext1.setItalicsToggleButton(ib);
    		edittext1.setUnderlineToggleButton(ub);

    		
    		LL.setOrientation(LinearLayout.HORIZONTAL);
	      // LL.setPadding(4, 0, 0, 30);
    		LayoutParams LLParams = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
	       
	     
    		LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
	       
    		edittext1.setLayoutParams(dummyParams);
	      
	     	if(s_prev.equals(""))
    		{
    			String sv=" ";
    			if(fld==1)
        		{
    				SpannableString sf;
    				
        			edittext1.setText(s.subSequence(0, s.length()-2));

    				//count++;
    				fld=0;
        			//editTextAdder();
        		}
    			else
    			{
    				edittext1.setText(sv);
    			}
    		}
    		else
    		{
    			if(Html.fromHtml(s_prev).length()>=2)
    			       edittext1.setText(Html.fromHtml(s_prev).subSequence(0, Html.fromHtml(s_prev).length()-2));
    		        	else
    		          edittext1.setText(Html.fromHtml(s_prev).subSequence(0, Html.fromHtml(s_prev).length()));	
    		}
	      
    		if(count==0)
	        {
	        	edittext1.setHint("Enter your note!");
	        }
    		
    		LL.setLayoutParams(LLParams);
	      
	       LL.addView(edittext1);
	       layoutList.add(prev_val+1, LL);
	        editList.add(prev_val+1,edittext1);
	       
	        if((prev_val+1)!=count)
	        {
	       for(LinearLayout l1:layoutList)
	       {
	    	   ((LinearLayout) findViewById(R.id.rl)).removeView(l1);   
	    	   ((LinearLayout) findViewById(R.id.rl)).addView(l1);
	       }
	        }
	        else
	        	
	        ((LinearLayout) findViewById(R.id.rl)).addView(LL);
	        
	        
	     	        
	        
	        
	        count++;
	        
	             edittext1.setFocusableInTouchMode(true);
	        edittext1.requestFocus();
//	        final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollView1));
//	        scrollview.post(new Runnable() {
//	            @Override
//	            public void run() {
//	                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
//	            }
//	        });

	        
	        edittext1.setOnTouchListener(new OnTouchListener() {
		           

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					prev_val=editList.indexOf(edittext1)-1;
					 ((DroidWriterEditText)editList.get(prev_val+1)).setBoldToggleButton(bb);
		    		 ((DroidWriterEditText)editList.get(prev_val+1)).setItalicsToggleButton(ib);
		    		 ((DroidWriterEditText)editList.get(prev_val+1)).setUnderlineToggleButton(ub);
					return false;
				}
	        });
	        
	        
	        
//	        edittext1.setOnKeyListener(new OnKeyListener() {                 
//	            @Override
//	            public boolean onKey(View v, int keyCode, KeyEvent event) {
//	            	System.out.println("hai");
//	            	Toast.makeText(getApplicationContext(), "fgergregregv", Toast.LENGTH_SHORT).show();
//	                   if(keyCode == KeyEvent.KEYCODE_DEL){  
//	                	   System.out.println("hai");
//	                	   if(edittext1.getText().toString()==null){
//	                		   
//	                	   if(count!=1)
//							{
//							 int ac=editList.indexOf(edittext1);
//							 LinearLayout l2=(LinearLayout)layoutList.get(ac);
//							 System.out.println("hai");
//							 
//							 l2.setVisibility(View.INVISIBLE);
//							 layoutList.remove(ac);
//							 editList.remove(ac);
//							 ((LinearLayout) findViewById(R.id.rl)).removeView(l2);
//							
//							 count--;
//							 if(ac>=1)
//							 {
//								 DroidWriterEditText e2=(DroidWriterEditText)editList.get(ac-1);
//							 int cursorPosition1 = e2.getSelectionStart();
//							 e2.setSelection(e2.getText().length());
//							 e2.setFocusableInTouchMode(true);
//						        e2.requestFocus();
//						        prev_val=editList.indexOf(e2)-1;
//							
//							 }
//							}
//							 else
//							 {
//								 edittext1.setHint("Enter Your Note");
//							 }
//	                	   modifier();
//	                	   
//	                	   }
//	                     
//	                     }
//	            return false;       
//	                }
//	        });
	        edittext1.addTextChangedListener(new TextWatcher() {
	        	  

	        	  @Override
	        	  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        	    //nothing needed here...
	        	  }

	        	  @Override
	        	  public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	    //nothing needed here...
	        		  
	        	  }

				@Override
				public void afterTextChanged(Editable e) {
					// TODO Auto-generated method stub
					
					 if(bb.isChecked())
		    		 {
		    			// Toast.makeText(getApplicationContext(), "checked",Toast.LENGTH_SHORT).show();
//		    			  Spannable spannable = (Spannable)editList.get(prev_val+1).getText();
//		    			    StyleSpan boldSpan = new StyleSpan( Typeface.BOLD );
//		    			    spannable.setSpan( boldSpan, editList.get(prev_val+1).getSelectionStart(),editList.get(prev_val+1).getSelectionStart(), Spannable.SPAN_INCLUSIVE_INCLUSIVE );
//		    		
						 }
					
					 String textFromEditView = e.toString();
					
					if(textFromEditView.length()>0)
					{
					 if(textFromEditView.charAt(textFromEditView.length()-1)=='\n')
					 {
						
						 
						 if(textFromEditView.length()<=1)
						 {
							 
							
							 
							 edittext1.setText(textFromEditView.substring(0,textFromEditView.length()-2));
							
							// edittext1.setSelection(cursorPosition-1);
							 prev_val=editList.indexOf(edittext1);
							 Toast.makeText(getApplicationContext(), prev_val+"", Toast.LENGTH_SHORT).show();
							 
							// Toast.makeText(getApplicationContext(), textFromEditView, Toast.LENGTH_SHORT).show();
							 s_prev="";
							modifier();
							 
							
						 }
						 else
						 {
						 int cursorPosition = edittext1.getSelectionStart();
						 //Here is the problem... :P
						 Spannable s = edittext1.getText();
						 edittext1.setText(s.subSequence(0,s.length()-1));
						// edittext1.append(" ");
						// Toast.makeText(get, text, duration)
						// edittext1.setSelection(cursorPosition-2);
						 prev_val=editList.indexOf(edittext1);
					//	 Toast.makeText(getApplicationContext(), prev_val+"", Toast.LENGTH_SHORT).show();
						 
						 Toast.makeText(getApplicationContext(), textFromEditView, Toast.LENGTH_SHORT).show();
						 s_prev="";
						modifier();
						 }
						 
						 
}
					}
					else
					{
						if(count!=1)
						{
						 int ac=editList.indexOf(edittext1);
						 LinearLayout l2=(LinearLayout)layoutList.get(ac);
						 
						 l2.setVisibility(View.INVISIBLE);
						 layoutList.remove(ac);
						 editList.remove(ac);
						 ((LinearLayout) findViewById(R.id.rl)).removeView(l2);
						
						 count--;
						 if(ac>=1)
						 {
							 DroidWriterEditText e2=(DroidWriterEditText)editList.get(ac-1);
						 int cursorPosition1 = e2.getSelectionStart();
						 e2.setSelection(e2.getText().length());
						 e2.setFocusableInTouchMode(true);
					        e2.requestFocus();
					        prev_val=editList.indexOf(e2)-1;
						
						 }
						}
						 else
						 {
							 edittext1.setHint("Enter Your Note");
						 }
					}
					
		        	   //
				}
	        	});
    }
	void modifier()
	{
		if(c.isChecked())
		{
			checkBoxAdder();
	    
	}
		else
		{
			editTextAdder();
		}

	}
	public void checked(View v)
	{
		
			
		 LinearLayout l2=(LinearLayout)layoutList.get(prev_val+1);
		 
		 Spannable s = (editList.get(prev_val+1)).getText();
		 s_prev=Html.toHtml(s);
		 Toast.makeText(getApplicationContext(), s_prev, Toast.LENGTH_LONG).show();
		 l2.setVisibility(View.INVISIBLE);
		 layoutList.remove(prev_val+1);
		 editList.remove(prev_val+1);
		 ((LinearLayout) findViewById(R.id.rl)).removeView(l2);
		
		 count--;
		 
		 if(prev_val+1>=1)
		 {
		 DroidWriterEditText e2=(DroidWriterEditText)editList.get(prev_val);
		 int cursorPosition1 = e2.getSelectionStart();
		 e2.setSelection(cursorPosition1);
		 e2.setFocusableInTouchMode(true);
	        e2.requestFocus();
	
		 }
		 
		
		modifier();
	}

	
	
	
	
	
	  @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.main, menu);
	        return true;
	    }
	  
	   
	   @Override
	   public boolean onMenuItemSelected(int featureId, MenuItem item) {

	       int itemId = item.getItemId();
//	       Spannable strin;
//	       Spanned stri;
//	       EditText edi=new EditText(this);
//	       edi.setText("");
//	       stri=edi.getText();
	       switch (itemId) {
	       case R.id.action_cart:
	    	   Context mContext = getApplicationContext();
	    	   Dialog dialog = new Dialog(this);
			   dialog.setTitle("Color Picker");
			
			   dialog.setCancelable(true);
			 //  initilizeMap();
			   dialog.setContentView(R.layout.custom_dialog);
			   
			   dialog.show();
			   Button cb1 = (Button) dialog.findViewById(R.id.cb1);
               cb1.setOnClickListener(new OnClickListener() {

                   @Override
                   public void onClick(View v) {

                	  // Toast.makeText(getApplicationContext(), "#2ecc71",Toast.LENGTH_SHORT).show();
                       colorcode="#2ecc71";
                       changer();

                   }
               });
               Button cb2 = (Button) dialog.findViewById(R.id.cb2);
               cb2.setOnClickListener(new OnClickListener() {

                   @Override
                   public void onClick(View v) {
                	   colorcode="#3498db";
                	   changer();
                       

                   }
               });
               
               Button cb3 = (Button) dialog.findViewById(R.id.cb3);
               cb3.setOnClickListener(new OnClickListener() {

                   @Override
                   public void onClick(View v) {

                	   colorcode="#9b59b6";
                	   changer();
                       

                   }
               });
               
               Button cb4 = (Button) dialog.findViewById(R.id.cb4);
               cb4.setOnClickListener(new OnClickListener() {

                   @Override
                   public void onClick(View v) {

                	   colorcode="#f1c40f";
                	   changer();
                                              

                   }
               });
               
               Button cb5 = (Button) dialog.findViewById(R.id.cb5);
               cb5.setOnClickListener(new OnClickListener() {

                   @Override
                   public void onClick(View v) {

                       
                	   colorcode="#e67e22";
                	   changer();
                       
                   }
               });
               Button cb6 = (Button) dialog.findViewById(R.id.cb6);
               cb6.setOnClickListener(new OnClickListener() {

                   @Override
                   public void onClick(View v) {

                	   colorcode="#e74c3c";
                	   changer();
                       

                   }
               });
               
               Button cb7 = (Button) dialog.findViewById(R.id.cb7);
               cb7.setOnClickListener(new OnClickListener() {

                   @Override
                   public void onClick(View v) {

                	   colorcode="#95a5a6";
                	   changer();
                       

                   }
               });
               
               Button cb8 = (Button) dialog.findViewById(R.id.cb8);
               cb8.setOnClickListener(new OnClickListener() {

                   @Override
                   public void onClick(View v) {

                       
                	   colorcode="#7f8c8d";
                	   changer();
                       
                   }
               });
               
               Button cb9 = (Button) dialog.findViewById(R.id.cb9);
               cb9.setOnClickListener(new OnClickListener() {

                   @Override
                   public void onClick(View v) {

                	   colorcode="#f39c12";
                	   changer();
                       

                   }
               });
	           break;
	           

	       }

	       return true;
	   }
	   public void onBackPressed()
	   {
		   Spannable strin;
	       Spanned stri;
	       EditText edi=new EditText(this);
	       edi.setText("");
	       stri=edi.getText();
		   String html_text="<html>\n<body>";
    	   
    	   for(int i=0;i<layoutList.size();i++)
    	   {
    		  LinearLayout l=layoutList.get(i);
    		  if(l.getChildCount()==2)
    		  {
    			  if(((CheckBox)l.getChildAt(0)).isChecked())
    			  {
    				  html_text=html_text+"\n<p class='namechecked'>";
    				  edi.setText("[*]");
    				  strin=edi.getText();
    				  stri=(Spanned)TextUtils.concat(stri, strin);
    				  strin=((DroidWriterEditText)(l.getChildAt(1))).getText();
    				
    				  stri=(Spanned) TextUtils.concat(stri, strin);
    				  edi.setText("\n");
    				  strin=edi.getText();
    				  stri=(Spanned)TextUtils.concat(stri, strin);
    				 // stri=edi.getText();
    			  }
    			  else
    			  {
    				  html_text=html_text+"\n<p class='nameunchecked'>"; 
    				  edi.setText("[ ]");
    				  strin=edi.getText();
    				  stri=(Spanned)  TextUtils.concat(stri, strin);
    				  strin=((DroidWriterEditText)(l.getChildAt(1))).getText();
    				
    				  stri=(Spanned)TextUtils.concat(stri, strin);
    				  edi.setText("\n");
    				  strin=edi.getText();
    				  stri=(Spanned)TextUtils.concat(stri, strin);
    				//  stri=edi.getText();
    			  }
    			  
    			  html_text=html_text+((DroidWriterEditText)(l.getChildAt(1))).getText();
    			  html_text=html_text+"</p>";
    		  }
    		  else
    		  {
    			  html_text=html_text+"\n<p class='textf'>";
    			  html_text=html_text+((DroidWriterEditText)(l.getChildAt(0))).getText();
    			  html_text=html_text+"</p>";
	    			 
    				 
    			  strin=((DroidWriterEditText)(l.getChildAt(0))).getText();
    				
    			  stri=(Spanned)TextUtils.concat(stri, strin);
    			  edi.setText("\n");
    			  strin=edi.getText();
    			  stri=(Spanned)TextUtils.concat(stri, strin);
	    			 
    		  }
    	   }
    	   //stri=edi.getText();
			  html_text=html_text+"\n</body>\n</html>";
			  html_text=Html.toHtml(stri);
			  Log.d("Html ", html_text);
			  Toast.makeText(getApplicationContext(), html_text, Toast.LENGTH_LONG).show();
			  DatabaseHandler db = new DatabaseHandler(this);
			  if(value==0)
				  db.addNote(new Notes(html_text,colorcode));
			 else
			 {
				 Notes n=db.getNote(value);
				 n.setName(html_text);
				 n.setColor(colorcode);
				 db.updateNote(n);
			 }
			  finish();
			  overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
		   
	   }
	
	
	
	   
	   public void fetcher()
	   {
		   DatabaseHandler db = new DatabaseHandler(this);
		   Notes n=db.getNote(value);
		   String html_text=n.getNote();
		   System.out.println(n.getColor()+"kk");
		   s=Html.fromHtml(html_text);
		  
		 
		   
		   System.out.println(html_text+"\n the orig:\n"+s.subSequence(0, s.length()-2));
		   fld=1;
		 //  editTextAdder();
	   }
	
	   public void colorsetter()
	   {
		   if(value==0)
			   colorcode="#2ecc71";
		   
		   else
		   {
			   DatabaseHandler db = new DatabaseHandler(this);
			   Notes n=db.getNote(value);
			   colorcode=n.getColor();
			   System.out.println(n.getColor()+"kk");
			  
			   
		   }
		   ((LinearLayout) findViewById(R.id.l1)).setBackgroundColor(Color.parseColor(colorcode));
	   }
	
	
	
	public void changer()
	{
		 
	       for(LinearLayout l1:layoutList)
	       {
	    	   ((LinearLayout) findViewById(R.id.rl)).removeView(l1); 
	    	   if(l1.getChildCount()>1)
	    	   {
	    	   l1.getChildAt(1).setBackgroundColor(Color.parseColor(colorcode));
	    	   l1.setBackgroundColor(Color.parseColor(colorcode));
	    	   }
	    	   else
	    	   {
	    		   l1.getChildAt(0).setBackgroundColor(Color.parseColor(colorcode));
	    	   }
	    	   ((LinearLayout) findViewById(R.id.rl)).addView(l1);
	       }
	       ((LinearLayout) findViewById(R.id.l1)).setBackgroundColor(Color.parseColor(colorcode));
	        
	}
	
	
	
}




