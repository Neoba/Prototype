package com.neoba.syncpad;

import hu.scythe.droidwriter.DroidWriterEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;

import com.neoba.syncpad.ByteMessenger.document;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class NotesEditor extends ActionBarActivity {
	// arraylist for each linearlayout
	ArrayList<LinearLayout> layoutList;
	String value;
	// ArrayList for each edittext
	ArrayList<DroidWriterEditText> editList;
	int count; // the count of linearlayouts or edittexts.
	int prev_val; // (the index of edittext in focus)-1
	CheckBox c; // the checkbox adder
	String s_prev; // the string in edittext in focus.
	int fg;
	String colorcode;
	int checked = 0;
	int auto_cap;
	int fld;
	Spanned s;
	ToggleButton bb, ib, ub; // the bold , italic and underline togglebutton
								// respectively
	boolean isempty;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notes_editor);
		Toolbar toolbar = (Toolbar) findViewById(R.id.tbEdit);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}
		colorcode = "#F8F8FF";

		c = (CheckBox) findViewById(R.id.checkBox1);
		bb = (ToggleButton) findViewById(R.id.boldbutton);
		ib = (ToggleButton) findViewById(R.id.italicbutton);
		ub = (ToggleButton) findViewById(R.id.underbutton);
		layoutList = new ArrayList<LinearLayout>();
		editList = new ArrayList<DroidWriterEditText>();
		count = 0;
		fg = 0;
		auto_cap = 1;
		prev_val = -1;
		s_prev = "";
		value = getIntent().getExtras().getString("uuid");
		fld = 0;
		if (!value.endsWith("N")) {
			isempty = false;
			colorsetter();
			try {
				fetcher();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (VcdiffDecodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// modifier();
		} else {
			value = value.split("N")[0];
			isempty = true;
			colorsetter();
			modifier();

		}
	}

	/*
	 * Adds the checkbox for you..
	 */
	void checkBoxAdder() {
		LinearLayout LL = new LinearLayout(this);
		CheckBox ch = new CheckBox(this);
		final DroidWriterEditText edittext = new DroidWriterEditText(this);
		edittext.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
				| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		edittext.setBackgroundColor(Color.parseColor(colorcode));
		LL.setBackgroundColor(Color.parseColor(colorcode));
		c.setChecked(true);
		if (checked == 1) {
			ch.setChecked(true);
			checked = 0;
		}
		edittext.setSingleLine(false);
		// edittext.setLineSpacing(2, 2);
		edittext.setPadding(4, 20, 0, 20);
		// edittext.setBackgroundColor(Color.WHITE);
		bb = (ToggleButton) findViewById(R.id.boldbutton);
		ib = (ToggleButton) findViewById(R.id.italicbutton);
		ub = (ToggleButton) findViewById(R.id.underbutton);
		edittext.setBoldToggleButton(bb);
		edittext.setItalicsToggleButton(ib);
		edittext.setUnderlineToggleButton(ub);
		LL.setOrientation(LinearLayout.HORIZONTAL);
		LL.setPadding(20, 0, 0, 0);
		LayoutParams LLParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams ladderParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		ch.setLayoutParams(ladderParams);

		edittext.setLayoutParams(dummyParams);

		// The Kevin's suggestion code.

		edittext.setImeOptions(EditorInfo.PARCELABLE_WRITE_RETURN_VALUE);
		edittext.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() != KeyEvent.ACTION_DOWN) {
					// We only look at ACTION_DOWN in this code, assuming that
					// ACTION_UP is redundant.
					// If not, adjust accordingly.
					return false;
				} else if (event.getUnicodeChar() == (int) EditableAccomodatingLatinIMETypeNullIssues.ONE_UNPROCESSED_CHARACTER
						.charAt(0)) {
					// We are ignoring this character, and we want everyone else
					// to ignore it, too, so
					// we return true indicating that we have handled it (by
					// ignoring it).
					return true;
				}

				//
				// Now, just do your event handling as usual...
				//

				else if (keyCode == KeyEvent.KEYCODE_DEL) {
					// Backspace key processing goes here...

					Spanned str = ((EditText) v).getText();

					if (((EditText) v).getSelectionStart() != 0) {
						if (str.length() >= 2) {

							int start = edittext.getSelectionStart();
							int end = edittext.getSelectionEnd();
							SpannableStringBuilder sr = new SpannableStringBuilder(
									edittext.getText());
							sr.replace(start - 1, end, "");

							// getting the selected Text

							// replacing the selected text with empty String and
							// setting it to EditText
							edittext.setText(sr);
							edittext.setSelection(start - 1);
						} else {
							((EditText) v).setText("");
							((EditText) v).setSelection(((EditText) v).length());
						}
					} else {
						if (count != 1) {
							int ac = editList.indexOf(edittext);
							LinearLayout l2 = (LinearLayout) layoutList.get(ac);

							l2.setVisibility(View.INVISIBLE);
							layoutList.remove(ac);
							editList.remove(ac);
							((LinearLayout) findViewById(R.id.rl))
									.removeView(l2);

							count--;
							if (ac >= 1) {
								DroidWriterEditText e2 = (DroidWriterEditText) editList
										.get(ac - 1);
								int cursorPosition1 = e2.getSelectionStart();
								e2.setSelection(e2.getText().length());
								e2.setFocusableInTouchMode(true);
								e2.requestFocus();
								prev_val = editList.indexOf(e2) - 1;

							}
						} else {
							edittext.setHint("Enter Your Note");
						}
					}
					return true;
				}

				else if (keyCode == KeyEvent.KEYCODE_ENTER) {
					String textFromEditView = ((EditText) v).getText()
							.toString();
					if (((EditText) v).length() <= 1) {

						int ac = editList.indexOf(edittext);
						LinearLayout l2 = (LinearLayout) layoutList.get(ac);

						l2.setVisibility(View.INVISIBLE);
						layoutList.remove(ac);
						editList.remove(ac);
						((LinearLayout) findViewById(R.id.rl)).removeView(l2);

						count--;

						if (ac >= 1) {
							DroidWriterEditText e2 = (DroidWriterEditText) editList
									.get(ac - 1);
							int cursorPosition1 = e2.getSelectionStart();
							e2.setSelection(cursorPosition1);
							e2.setFocusableInTouchMode(true);
							e2.requestFocus();
							prev_val = editList.indexOf(e2) - 1;
						}
						s_prev = "";
						fg = 1;
						prev_val = ac - 1;
						c.toggle();
						editTextAdder();

					} else {
						int cursorPosition = edittext.getSelectionStart();
						int cursorPositione = edittext.getSelectionEnd();
						// Here is the problem... rectified :P
						Spannable st = edittext.getText();
						edittext.setText(st.subSequence(0, cursorPosition));
						if (cursorPositione != st.length()) {
							fld = 1;
							String lefto = "<p>"
									+ st.subSequence(cursorPosition,
											st.length()) + "</p>";
							s = Html.fromHtml(lefto);
							prev_val = editList.indexOf(edittext);

							s_prev = "";
							editTextAdder();
						} else {

							edittext.setSelection(edittext.getText().length());
							prev_val = editList.indexOf(edittext);

							s_prev = "";

							modifier();
						}
					}
					return true;
				} else
					return false;

			}
		});

		// edittext.setPadding(-1,0, 0, 0);

		if (s_prev.equals("")) {

			String sv = "";
			if (fld == 1) {
				System.out.println("this is:)");
				SpannableString sf;
				if ((s.length() != 0))
					edittext.setText(s.subSequence(0, s.length() - 2));
				else
					edittext.setText("");
				// count++;
				fld = 0;
				// editTextAdder();
			} else {
				edittext.setText(sv);
			}
		} else {
			if (Html.fromHtml(s_prev).length() >= 2)
				edittext.setText(Html.fromHtml(s_prev).subSequence(0,
						Html.fromHtml(s_prev).length() - 2));
			else
				edittext.setText(Html.fromHtml(s_prev).subSequence(0,
						Html.fromHtml(s_prev).length()));
			System.out.println(Html.fromHtml(s_prev));
		}
		LL.setLayoutParams(LLParams);
		LL.addView(ch);
		LL.addView(edittext);
		layoutList.add(prev_val + 1, LL);
		editList.add(prev_val + 1, edittext);

		if ((prev_val + 1) != count) {
			for (LinearLayout l1 : layoutList) {
				((LinearLayout) findViewById(R.id.rl)).removeView(l1);
				((LinearLayout) findViewById(R.id.rl)).addView(l1);
			}
		} else
			((LinearLayout) findViewById(R.id.rl)).addView(LL);

		count++;
		edittext.setFocusableInTouchMode(true);
		edittext.requestFocus();

		edittext.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				prev_val = editList.indexOf(edittext) - 1;
				((DroidWriterEditText) editList.get(prev_val + 1))
						.setBoldToggleButton(bb);
				((DroidWriterEditText) editList.get(prev_val + 1))
						.setItalicsToggleButton(ib);
				((DroidWriterEditText) editList.get(prev_val + 1))
						.setUnderlineToggleButton(ub);
				return false;
			}
		});

		edittext.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// nothing needed here...
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// nothing needed here...

			}

			@Override
			public void afterTextChanged(Editable e) {
				// TODO Auto-generated method stub

				String textFromEditView = e.toString();
				if (textFromEditView.length() > 0) {
					fg = 0;
					if (textFromEditView.charAt(textFromEditView.length() - 1) == '\n') {

					}
				} else {
					int ac = editList.indexOf(edittext);
					LinearLayout l2 = (LinearLayout) layoutList.get(ac);

					l2.setVisibility(View.INVISIBLE);
					layoutList.remove(ac);
					editList.remove(ac);
					((LinearLayout) findViewById(R.id.rl)).removeView(l2);

					count--;

					if (ac >= 1) {
						DroidWriterEditText e2 = (DroidWriterEditText) editList
								.get(ac - 1);
						int cursorPosition1 = e2.getSelectionEnd();
						e2.setSelection(cursorPosition1);
						e2.setFocusableInTouchMode(true);
						e2.requestFocus();
						prev_val = editList.indexOf(e2) - 1;

					}

					fg = 1;

					prev_val = ac - 1;
					s_prev = "";
					editTextAdder();
					c.toggle();

				}

			}
		});
	}

	// Adds the simple edittext views for you .

	void editTextAdder() {
		LinearLayout LL = new LinearLayout(this);

		final DroidWriterEditText edittext1 = new DroidWriterEditText(this);
		edittext1.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
				| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
				| InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
		edittext1.setSingleLine(false);
		edittext1.setPadding(4, 0, 0, 15);

		c.setChecked(false);

		edittext1.setBackgroundColor(Color.parseColor(colorcode));

		bb = (ToggleButton) findViewById(R.id.boldbutton);
		ib = (ToggleButton) findViewById(R.id.italicbutton);
		ub = (ToggleButton) findViewById(R.id.underbutton);
		edittext1.setBoldToggleButton(bb);
		edittext1.setItalicsToggleButton(ib);
		edittext1.setUnderlineToggleButton(ub);

		LL.setOrientation(LinearLayout.HORIZONTAL);

		LayoutParams LLParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		edittext1.setLayoutParams(dummyParams);
		edittext1.setImeOptions(EditorInfo.PARCELABLE_WRITE_RETURN_VALUE);
		edittext1.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() != KeyEvent.ACTION_DOWN) {
					// We only look at ACTION_DOWN in this code, assuming that
					// ACTION_UP is redundant.
					// If not, adjust accordingly.
					return false;
				} else if (event.getUnicodeChar() == (int) EditableAccomodatingLatinIMETypeNullIssues.ONE_UNPROCESSED_CHARACTER
						.charAt(0)) {
					// We are ignoring this character, and we want everyone else
					// to ignore it, too, so
					// we return true indicating that we have handled it (by
					// ignoring it).
					return true;
				}

				//
				// Now, just do your event handling as usual...
				//

				else if (keyCode == KeyEvent.KEYCODE_DEL) {
					// Backspace key processing goes here...

					Spanned str = ((EditText) v).getText();

					if (((EditText) v).getSelectionStart() != 0) {
						if (str.length() >= 2) {

							int start = edittext1.getSelectionStart();
							int end = edittext1.getSelectionEnd();
							SpannableStringBuilder sr = new SpannableStringBuilder(
									edittext1.getText());
							sr.replace(start - 1, end, "");

							// getting the selected Text

							// replacing the selected text with empty String and
							// setting it to EditText
							edittext1.setText(sr);
							edittext1.setSelection(start - 1);
						} else {
							((EditText) v).setText("");
							((EditText) v).setSelection(((EditText) v).length());
						}
					} else {
						if (count != 1) {
							int ac = editList.indexOf(edittext1);
							LinearLayout l2 = (LinearLayout) layoutList.get(ac);

							l2.setVisibility(View.INVISIBLE);
							layoutList.remove(ac);
							editList.remove(ac);
							((LinearLayout) findViewById(R.id.rl))
									.removeView(l2);

							count--;
							if (ac >= 1) {
								DroidWriterEditText e2 = (DroidWriterEditText) editList
										.get(ac - 1);
								int cursorPosition1 = e2.getSelectionStart();
								e2.setSelection(e2.getText().length());
								e2.setFocusableInTouchMode(true);
								e2.requestFocus();
								prev_val = editList.indexOf(e2) - 1;

							}
						} else {
							edittext1.setHint("Enter Your Note");
						}
					}
					return true;
				}

				else if (keyCode == KeyEvent.KEYCODE_ENTER) {
					String textFromEditView = ((EditText) v).getText()
							.toString();
					if (((EditText) v).length() <= 1) {

						if (edittext1.length() != 0) {
							edittext1.setText(textFromEditView.substring(0,
									textFromEditView.length() - 1));

						}
						prev_val = editList.indexOf(edittext1);
						s_prev = "";
						modifier();

					} else {
						int cursorPosition = edittext1.getSelectionStart();
						int cursorPositione = edittext1.getSelectionEnd();
						// Here is the problem... rectified :P
						Spannable st = edittext1.getText();
						edittext1.setText(st.subSequence(0, cursorPosition));
						if (cursorPositione != st.length()) {
							fld = 1;
							String lefto = "<p>"
									+ st.subSequence(cursorPosition,
											st.length()) + "</p>";
							s = Html.fromHtml(lefto);
						}
						prev_val = editList.indexOf(edittext1);

						s_prev = "";
						modifier();
					}
					return true;
				} else
					return false;

			}
		});
		if (s_prev.equals("")) {
			String sv = "";
			if (fld == 1) {
				System.out.println("this is:)");
				SpannableString sf;
				if (s.length() != 0)
					edittext1.setText(s.subSequence(0, s.length() - 2));
				else {
					edittext1.setText("");
				}

				fld = 0;
			} else {
				edittext1.setText(sv);
			}
		} else // to extract string if the checkbox is toggled.
		{
			if (Html.fromHtml(s_prev).length() >= 2)
				edittext1.setText(Html.fromHtml(s_prev).subSequence(0,
						Html.fromHtml(s_prev).length() - 2));
			else
				edittext1.setText(Html.fromHtml(s_prev).subSequence(0,
						Html.fromHtml(s_prev).length()));
		}

		if (count == 0) {
			edittext1.setHint("Enter your note!");
		}

		LL.setLayoutParams(LLParams);

		LL.addView(edittext1);
		layoutList.add(prev_val + 1, LL);
		editList.add(prev_val + 1, edittext1);

		if ((prev_val + 1) != count) {
			for (LinearLayout l1 : layoutList) {
				((LinearLayout) findViewById(R.id.rl)).removeView(l1);
				((LinearLayout) findViewById(R.id.rl)).addView(l1);
			}
		} else

			((LinearLayout) findViewById(R.id.rl)).addView(LL);

		count++;

		edittext1.setFocusableInTouchMode(true);
		edittext1.requestFocus();

		edittext1.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				prev_val = editList.indexOf(edittext1) - 1;
				((DroidWriterEditText) editList.get(prev_val + 1))
						.setBoldToggleButton(bb);
				((DroidWriterEditText) editList.get(prev_val + 1))
						.setItalicsToggleButton(ib);
				((DroidWriterEditText) editList.get(prev_val + 1))
						.setUnderlineToggleButton(ub);
				return false;
			}
		});

		edittext1.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// nothing needed here...
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// nothing needed here...

			}

			@Override
			public void afterTextChanged(Editable e) {
				// TODO Auto-generated method stub
				float textWidth = edittext1.getPaint()
						.measureText(e.toString());

				final DroidWriterEditText edittex = new DroidWriterEditText(
						NotesEditor.this);
				edittex.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
						| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
						| InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
				edittex.setSingleLine(true);
				edittex.setImeOptions(EditorInfo.PARCELABLE_WRITE_RETURN_VALUE);
				edittex.setHorizontallyScrolling(false);
				edittex.setText("  ");
				float a = edittex.getPaint().measureText(
						edittex.getText().toString());

				if (bb.isChecked()) {
					// //Toast.akeText(getApplicationContext(),
					// "checked",Toast.LENGTH_SHORT).show();

				}

				String textFromEditView = e.toString();

				if (textFromEditView.length() > 0) {

				} else {
					if (count != 1) {
						int ac = editList.indexOf(edittext1);
						LinearLayout l2 = (LinearLayout) layoutList.get(ac);

						l2.setVisibility(View.INVISIBLE);
						layoutList.remove(ac);
						editList.remove(ac);
						((LinearLayout) findViewById(R.id.rl)).removeView(l2);

						count--;
						if (ac >= 1) {
							DroidWriterEditText e2 = (DroidWriterEditText) editList
									.get(ac - 1);
							int cursorPosition1 = e2.getSelectionStart();
							e2.setSelection(e2.getText().length());
							e2.setFocusableInTouchMode(true);
							e2.requestFocus();
							prev_val = editList.indexOf(e2) - 1;

						}
					} else {
						edittext1.setHint("Enter Your Note");
					}
				}

				//
			}
		});
	}

	void modifier() {
		if (c.isChecked()) {
			checkBoxAdder();

		} else {
			editTextAdder();
		}

	}

	public void checked(View v) {

		if (prev_val != -1) {
			LinearLayout l2 = (LinearLayout) layoutList.get(prev_val + 1);
			if (editList.get(prev_val + 1).getText().toString() != null) {
				Spannable s = (editList.get(prev_val + 1)).getText();
				s_prev = Html.toHtml(s);
				// Toast.akeText(getApplicationContext(), s_prev,
				// Toast.LENGTH_LONG).show();
				l2.setVisibility(View.INVISIBLE);
				layoutList.remove(prev_val + 1);
				editList.remove(prev_val + 1);
				((LinearLayout) findViewById(R.id.rl)).removeView(l2);

				count--;
			} else {
				EditText e = new EditText(this);
				e.setText(" ");
				s_prev = Html.toHtml(e.getText());
				l2.setVisibility(View.INVISIBLE);
				layoutList.remove(prev_val + 1);
				editList.remove(prev_val + 1);
				((LinearLayout) findViewById(R.id.rl)).removeView(l2);
				count--;
			}
			if (prev_val + 1 >= 1) {
				DroidWriterEditText e2 = (DroidWriterEditText) editList
						.get(prev_val);
				int cursorPosition1 = e2.getSelectionStart();
				e2.setSelection(cursorPosition1);
				e2.setFocusableInTouchMode(true);
				e2.requestFocus();

			}
			modifier();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.editor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_color) {
			Context mContext = getApplicationContext();
			final String[] colors = new String[] {"Turquoise~#1abc9c",
					"Green sea~#16a085",
					"Emerald~#2ecc71",
					"Nephritis~#27ae60",
					"Peter river~#3498db",
					"Belize hole~#2980b9",
					"Amethyst~#9b59b6",
					"Wisteria~#8e44ad",
					"Wet asphalt~#34495e",
					"Midnight blue~#2c3e50",
					"Sun flower~#f1c40f",
					"Orange~#f39c12",
					"Carrot~#e67e22",
					"Pumpkin~#d35400",
					"Alizarin~#e74c3c",
					"Pomegranate~#c0392b",
					"Ghost~#F8F8FF",
					"Silver~#bdc3c7",
					"Concrete~#95a5a6",
					"Asbestos~#7f8c8d" };
			Dialog dialog2 = new Dialog(this);
			dialog2.setContentView(R.layout.color_picker_dialog);
			ListView lv = (ListView) dialog2.findViewById(R.id.lvColorPicker);
			ColorListAdapter adapter = new ColorListAdapter(this,
					Arrays.asList(colors));
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					colorcode=colors[arg2].split("~")[1];
					changer();
				}
			});
			dialog2.setCancelable(true);
			dialog2.show();
			

		}

		return super.onOptionsItemSelected(item);
	}

	public void onBackPressed() {
		Spannable strin;
		Spanned stri;
		String sop = "";
		EditText edi = new EditText(this);
		edi.setText("");
		stri = edi.getText();
		String html_text = "<html>\n<body>";
		int f = 0;
		for (int i = 0; i < layoutList.size(); i++) {
			LinearLayout l = layoutList.get(i);
			if (l.getChildCount() == 2) {
				if (((CheckBox) l.getChildAt(0)).isChecked()) {

					edi.setText("[*]");
					strin = edi.getText();
					if (strin != null) {
						stri = (Spanned) TextUtils.concat(stri, strin);
						strin = ((DroidWriterEditText) (l.getChildAt(1)))
								.getText();
						sop = "fy";
						stri = (Spanned) TextUtils.concat(stri, strin);
						edi.setText("\n");
						strin = edi.getText();
						stri = (Spanned) TextUtils.concat(stri, strin);
					}

				} else {

					edi.setText("[ ]");
					strin = edi.getText();
					if (strin != null) {
						stri = (Spanned) TextUtils.concat(stri, strin);
						strin = ((DroidWriterEditText) (l.getChildAt(1)))
								.getText();
						sop = "ffy";
						stri = (Spanned) TextUtils.concat(stri, strin);
						edi.setText("\n");
						strin = edi.getText();
						stri = (Spanned) TextUtils.concat(stri, strin);
					}

				}

			} else {

				strin = ((DroidWriterEditText) (l.getChildAt(0))).getText();
				sop = sop + ((DroidWriterEditText) (l.getChildAt(0))).getText();
				stri = (Spanned) TextUtils.concat(stri, strin);
				edi.setText("\n");
				strin = edi.getText();
				stri = (Spanned) TextUtils.concat(stri, strin);

			}
		}

		html_text = colorcode + "\n" + Html.toHtml(stri);
		// try{
		// html_text=replceLast(html_text,"<br>", "");
		// }catch (Exception e) {}
		Log.d("Html ", html_text);

		if ((layoutList.size() == 1)
				&& ((sop.length() == 0) || sop.equals(" "))) {
		}// if the text is empty.
		else {

			DBManager db = new DBManager(NotesEditor.this);
			db.open();
			document doc = db.getDocument(value);
			document newdoc = null;
			try {
				newdoc = new document(value, html_text, new VcdiffEncoder(
						doc.dict, html_text).encode(), doc.age, doc.dict,
						doc.permission, doc.owns, Constants.UNSYNCED_EDIT);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (VcdiffEncodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			db.updateContent(newdoc);
			Log.d(this.getLocalClassName(), "the new one " + newdoc.toString());
			db.close();
		}
		startService(new Intent(this, OfflineSyncService.class));
		finish();

	}

	public void fetcher() throws IOException, VcdiffDecodeException {
		DBManager db = new DBManager(NotesEditor.this);
		db.open();
		String n = db.getDoc(value);
		String ns = "";
		db.close();
		for (int i = 1; i < n.split("\n").length; i++)
			ns += n.split("\n")[i];
		String html_text = ns;
		Pattern p = Pattern.compile("<br>");
		Matcher m = p.matcher(html_text);
		int countbr = 0;
		while (m.find()) {
			countbr += 1;
		}

		String spp[] = new String[countbr];

		if (html_text.contains("<br>")) {
			spp = html_text.split("<br>");

			spp[0] = spp[0] + "</p>";
			int checkflag;

			checkflag = 0;
			if (spp[0].contains("[*]")) {
				spp[0] = spp[0].replace("[*]", "");
				checkflag = 1;
				checked = 1;
			}
			if (spp[0].contains("[ ]")) {
				spp[0] = spp[0].replace("[ ]", "");
				checkflag = 1;
			}
			spp[0] = spp[0] + "</p>";
			System.out.println(spp[0] + " mmmm");
			s = Html.fromHtml(spp[0]);
			fld = 1;
			if (checkflag == 1) {
				checkBoxAdder();
			} else {
				editTextAdder();
			}
			prev_val++;
			for (int i = 1; i <= countbr; i++) {
				checkflag = 0;
				// prev_val++;
				if (spp[i].contains("[*]")) {
					spp[i] = spp[i].replace("[*]", "");
					checkflag = 1;
					checked = 1;

				}
				if (spp[i].contains("[ ]")) {
					spp[i] = spp[i].replace("[ ]", " ");

					if (spp[i].contains("[ ]")) {

					}
					checkflag = 1;
				}
				spp[i] = "<p dir=\"ltr\">" + spp[i] + "</p>";
				System.out.println(spp[i] + " mmmm");
				s = Html.fromHtml(spp[i]);
				fld = 1;
				if (checkflag == 1) {
					checkBoxAdder();
					prev_val++;
				} else {
					editTextAdder();
					prev_val++;
				}

			}
			// /
			prev_val = countbr - 1;

		} else {
			int checkflag = 0;
			if (html_text.contains("[*]")) {
				html_text = html_text.replace("[*]", "");
				checkflag = 1;
				checked = 1;
			}
			if (html_text.contains("[ ]")) {
				html_text = html_text.replace("[ ]", "");
				checkflag = 1;
			}
			s = Html.fromHtml(html_text);
			fld = 1;
			if (checkflag == 1) {
				checkBoxAdder();
				// prev_val++;
			} else {
				editTextAdder();
				// prev_val++;
			}
			// prev_val=1;
		}

	}

	public void colorsetter() {

		DBManager db = new DBManager(NotesEditor.this);
		db.open();
		String note = null;
		try {
			note = db.getDoc(value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VcdiffDecodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.close();
		colorcode = note.split("\n")[0];

		// colorcode = n.getColor();
		System.out.println("Its a color " + colorcode);
		colorcode = colorcode.length() == 0 ? "#F8F8FF" : colorcode;
		((LinearLayout) findViewById(R.id.l1))
				.setBackgroundColor(Color.parseColor(colorcode.charAt(0) == '#' ? colorcode
						: "#F8F8FF"));
	}

	public void changer() {

		for (LinearLayout l1 : layoutList) {
			((LinearLayout) findViewById(R.id.rl)).removeView(l1);
			if (l1.getChildCount() > 1) {
				l1.getChildAt(1)
						.setBackgroundColor(Color.parseColor(colorcode));
				l1.setBackgroundColor(Color.parseColor(colorcode));
			} else {
				l1.getChildAt(0)
						.setBackgroundColor(Color.parseColor(colorcode));
			}
			((LinearLayout) findViewById(R.id.rl)).addView(l1);
		}
		((LinearLayout) findViewById(R.id.l1)).setBackgroundColor(Color
				.parseColor(colorcode));

	}

	public String replceLast(String yourString, String frist, String second) {
		StringBuilder b = new StringBuilder(yourString);
		b.replace(yourString.lastIndexOf(frist), yourString.lastIndexOf(frist)
				+ frist.length(), second);
		return b.toString();
	}
}