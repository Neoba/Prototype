package com.example.textcheckbox;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.neoba.syncpad.Notes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
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
	ArrayList<Integer> uuid;
	static int w;
	ArrayList<RelativeLayout> llist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checker();
	}

	@Override
	protected void onResume() {
		super.onResume();
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

	public void checker() {
		DatabaseHandler db = new DatabaseHandler(this);
		if (db.getNotesCount() == 0) {
			setContentView(R.layout.activity_none);
			TextView t = (TextView) findViewById(R.id.textView1);
			Typeface tf = Typeface.createFromAsset(getAssets(),
					"fonts/newfontblod.ttf");
			t.setTypeface(tf);

		} else {
			setContentView(R.layout.activity_home);
			List<Notes> notes = db.getAllNotes();
			uuid = new ArrayList<Integer>();
			llist = new ArrayList<RelativeLayout>();
			LinearLayout ll = (LinearLayout) findViewById(R.id.linear1);
			TypedValue tv = new TypedValue();
			for (int i = 0; i < db.getNotesCount(); i++) {
				uuid.add(i, notes.get(i).getID());
				RelativeLayout LL = new RelativeLayout(this);
				final RelativeLayout l = LL;
				LL.setClickable(true);
				AutoResizeTextView t = new AutoResizeTextView(this);
				TextView sharedwith = new TextView(this);
				TextView sharedwithname = new TextView(this);
				ImageView sendicon = new ImageView(this);
				ImageView editicon = new ImageView(this);
				sendicon.setImageResource(R.drawable.ic_share);
				editicon.setImageResource(R.drawable.ic_edit);
				RelativeLayout footer = new RelativeLayout(this);
				RelativeLayout shareddet = new RelativeLayout(this);
				RelativeLayout footericon = new RelativeLayout(this);
				Typeface tf = Typeface.createFromAsset(getAssets(),
						"fonts/newfontlight.ttf");

				t.setTypeface(tf);
				sharedwith.setTypeface(tf);
				sharedwithname.setTypeface(tf, Typeface.BOLD_ITALIC);
				DisplayMetrics displaymetrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(
						displaymetrics);

				int width = displaymetrics.widthPixels;
				w = width;
				String html_text = notes.get(i).getNote();
				Spanned s = Html.fromHtml(html_text);
				sharedwith.setText("shared with ");
				sharedwith.setTextSize(20);
				sharedwithname.setText("@Atul" + " ");
				sharedwithname.setTextSize(20);
				t.setTextSize(100);
				t.setMaxTextSize(500);
				
				t.setMinTextSize(27);
				t.setAddEllipsis(true);

				t.setText(s);

				sharedwith.setId(1);
				sharedwithname.setId(2);
				sendicon.setId(3);
				shareddet.setId(4);
				sendicon.setPadding(width / 5 - 10, 0, 0, 0);
				shareddet.addView(sharedwith);
				footericon.addView(sendicon);

				RelativeLayout.LayoutParams namep;
				namep = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				namep.addRule(RelativeLayout.RIGHT_OF, sharedwith.getId());

				RelativeLayout.LayoutParams sendp;
				sendp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				sendp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				editicon.setPadding(0, 0, width / 5, 0);

				shareddet.addView(sharedwithname, namep);

				footericon.addView(editicon, sendp);

				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

				RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				params1.addRule(RelativeLayout.BELOW, shareddet.getId());

				footer.setLayoutParams(params);
				footer.addView(shareddet);
				footer.addView(footericon, params1);
				footer.setPadding(0, 20, 0, 10);
				LL.setPadding(10, 0, 10, 0);
				LL.setBackgroundColor(Color.parseColor(notes.get(i).getColor()));
				LayoutParams LLParams = new LayoutParams(
						LayoutParams.FILL_PARENT, width);
				RelativeLayout.LayoutParams ladderParams = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);

				t.setLayoutParams(ladderParams);
				LL.setLayoutParams(LLParams);
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) t
						.getLayoutParams();
				layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,
						RelativeLayout.TRUE);
				// sharedwithlayout.addView(editicon,relativeLayoutParams2);
				LL.addView(t, layoutParams);
				LL.addView(footer);

				ll.addView(LL);

				llist.add(i, LL);
				editicon.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent i = new Intent(HomeActivity.this,
								MainActivity.class);
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
			i.putExtra("uuid", 0);
			startActivity(i);
			overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
			break;
		}
		return true;
	}

}
