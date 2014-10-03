package com.example.textcheckbox;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {
	ArrayList<Integer>uuid;
	static int w;
	ArrayList<RelativeLayout>llist;
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
	        ActionBar actionBar = getActionBar();
	        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#330000ff")));
	        checker();
	    }
	 @Override
	 	protected void onResume()
	 	{
		 	super.onResume();
	     //Restore state here
		 	checker();
	 	}
	 
	 @Override
	 	protected void onRestart() {
		// TODO Auto-generated method stub
		 	super.onRestart();
		 	checker();
	 	}
	@Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.home, menu);
	        return true;
	    }
	  
	   public void checker()
	   {
			 DatabaseHandler db = new DatabaseHandler(this);
			 if(db.getNotesCount()==0)
			 {
				 setContentView(R.layout.activity_none);
				 TextView t=(TextView)findViewById(R.id.textView1);
				 Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/newfontblod.ttf");
				 t.setTypeface(tf);
				 
			 }
			 else
			 {
				 setContentView(R.layout.activity_home);
				 List<Notes>notes = db.getAllNotes();
				 uuid=new ArrayList<Integer>();
				 llist=new ArrayList<RelativeLayout>();
				 LinearLayout ll=(LinearLayout)findViewById(R.id.linear1);
				 TypedValue tv = new TypedValue();
				 int actionBarHeight;
				 
				 if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
				 {
				     actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
				 }
				 else
				 {
					 actionBarHeight=10; 
				 }
				 for(int i=0;i<db.getNotesCount();i++)
				 {
					uuid.add(i,notes.get(i).getID());
					RelativeLayout LL=new RelativeLayout(this);
					final RelativeLayout l=LL;
					LL.setClickable(true);
					AutoResizeTextView t=new AutoResizeTextView(this);
					TextView sharedwith=new TextView(this);
					TextView sharedwithname=new TextView(this);
					ImageView editicon=new ImageView(this);
					ImageView sendicon=new ImageView(this);
					editicon.setImageResource(R.drawable.pen29);
					sendicon.setImageResource(R.drawable.send4);
					RelativeLayout footer=new RelativeLayout(this);
					RelativeLayout shareddet=new RelativeLayout(this);
					RelativeLayout footericon=new RelativeLayout(this);
					Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/newfontlight.ttf");
					t.setTypeface(tf);
					sharedwith.setTypeface(tf);
					sharedwithname.setTypeface(tf,Typeface.BOLD_ITALIC);
					DisplayMetrics displaymetrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
					       
					int width = displaymetrics.widthPixels;
					 w=width;
					String html_text=notes.get(i).getNote();
					Spanned s=Html.fromHtml(html_text);
					
					String namel="";
					
					Document doc = Jsoup.parse(html_text);
					Elements contents=doc.select("p");
					for (Element topic : contents) {
						 if(topic.hasClass("textf"))
						 {
							 namel=namel+topic.text();
						 }
						 else if(topic.hasClass("namechecked"))
						 {
							namel=namel+getResources().getString(R.string.checked)+topic.text(); 
						 }
						 else
							 namel=namel+getResources().getString(R.string.unchecked)+topic.text(); 
						 namel=namel+"\n ";
					 }
					
					
					sharedwith.setText("shared with ");
					sharedwith.setTextSize(20);
					sharedwithname.setText("@Atul"+" ");
					sharedwithname.setTextSize(20);
					t.setTextSize(100);
				    t.setMaxTextSize(100);
				   
				    t.setMinTextSize(27);
				    t.setAddEllipsis(true);
				    if(i==0)
				    t.setText("\n"+s);
				    else
				    	t.setText(s);
					
					
					 
				    
				    
				    //LinearLayout.LayoutParams params1
				     //= new LinearLayout.LayoutParams(
				    		// LayoutParams.FILL_PARENT,
				    	//	 LayoutParams.FILL_PARENT);
				    
				    //params1.gravity = Gravity.BOTTOM;
				    //params1.bottomMargin=0;
				    
				    
					sharedwith.setId(1);
				  	sharedwithname.setId(2);
				  	editicon.setId(3);  
				    shareddet.setId(4);
				    editicon.setPadding(width/5-10, 0, 0,0);
				  	shareddet.addView(sharedwith);
				  	footericon.addView(editicon);
				  
				  	RelativeLayout.LayoutParams namep; 
					namep = new RelativeLayout.LayoutParams(
						        RelativeLayout.LayoutParams.WRAP_CONTENT,
						        RelativeLayout.LayoutParams.WRAP_CONTENT);
					namep.addRule(RelativeLayout.RIGHT_OF,
						       sharedwith.getId());
					
					 
					RelativeLayout.LayoutParams sendp; 
					sendp = new RelativeLayout.LayoutParams(
					        RelativeLayout.LayoutParams.WRAP_CONTENT,
					        RelativeLayout.LayoutParams.WRAP_CONTENT);
					sendp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					sendicon.setPadding(0,0,width/5, 0);
					
					shareddet.addView(sharedwithname,namep);
					
					footericon.addView(sendicon,sendp);
					
					
					
				  	RelativeLayout.LayoutParams params
				     = new RelativeLayout.LayoutParams(
				    		 LayoutParams.FILL_PARENT,
				    		 LayoutParams.WRAP_CONTENT);
				    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				    
				    RelativeLayout.LayoutParams params1
				     = new RelativeLayout.LayoutParams(
				    		 LayoutParams.FILL_PARENT,
				    		 LayoutParams.WRAP_CONTENT);
				    params1.addRule(RelativeLayout.BELOW,
						       shareddet.getId());
				    
				    footer.setLayoutParams(params);
					footer.addView(shareddet);
					footer.addView(footericon,params1);
					footer.setPadding(0,20,0, 20);
					
				  
				    if(i==0)
				    {
				    	  LL.setPadding(10, actionBarHeight,10, 0);
				    }
				    else
				    	  LL.setPadding(10, 0, 10, 0);
				  
				   	LL.setBackgroundColor(Color.parseColor(notes.get(i).getColor()));
			       
				    LayoutParams LLParams = new LayoutParams(LayoutParams.FILL_PARENT,width);
				    RelativeLayout.LayoutParams ladderParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				    
				    t.setLayoutParams(ladderParams);
				    LL.setLayoutParams(LLParams);
				  //  t.setGravity(Gravity.CENTER_VERTICAL);
					 //t.setPadding(10, 0, 0,0);
					
					
					 
					 
					 
					
					// RelativeLayout.LayoutParams relativeLayoutParams; 
					 //relativeLayoutParams = new RelativeLayout.LayoutParams(
						//        RelativeLayout.LayoutParams.WRAP_CONTENT,
						  //      RelativeLayout.LayoutParams.WRAP_CONTENT);
					// relativeLayoutParams.addRule(RelativeLayout.RIGHT_OF,
						//        sharedwith.getId());
					 
					 
				//	 RelativeLayout.LayoutParams relativeLayoutParams1; 
					// relativeLayoutParams1 = new RelativeLayout.LayoutParams(
						//       200,
						  //      200);
					
					
					// RelativeLayout.LayoutParams relativeLayoutParams2; 
					// relativeLayoutParams2 = new RelativeLayout.LayoutParams(
					//	       200,
						//        200);
					 
					// relativeLayoutParams2.addRule(RelativeLayout.RIGHT_OF,
					//	       editicon.getId());
					 
					
//					 relativeLayoutParams1.addRule(RelativeLayout.BELOW,
//						        sharedwith.getId());
					
					// sharedwithlayout.addView(editicon,relativeLayoutParams1);
					 
					 
					 
					 
					 
					 
					 
					 
					 RelativeLayout.LayoutParams layoutParams = 
							    (RelativeLayout.LayoutParams)t.getLayoutParams();
						 layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
					// sharedwithlayout.addView(sendicon,relativeLayoutParams2);
					 LL.addView(t,layoutParams);
					 LL.addView(footer);
					
					 ll.addView(LL);
					
					// t.setLayoutParams(layoutParams);
					 llist.add(i,LL);
					 LL.setOnClickListener(new OnClickListener()
					 {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							 Intent i = new Intent(HomeActivity.this, MainActivity.class);
							 Toast.makeText(getApplicationContext(),uuid.get(llist.indexOf(l))+" ", Toast.LENGTH_SHORT).show();
							 i.putExtra("uuid", uuid.get(llist.indexOf(l)));
							 startActivity(i);
						}
						
					});
				}
			 }
			 db.close();
	   }
	   @Override
	   public boolean onMenuItemSelected(int featureId, MenuItem item) {

	       int itemId = item.getItemId();
	       switch (itemId) {
	       case R.id.action_home:
	    	   		Intent i = new Intent(HomeActivity.this, MainActivity.class);
				    i.putExtra("uuid",0);
	                startActivity(i);
	    	        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
	    	        break;
	       }
	       return true;
	   }
}
