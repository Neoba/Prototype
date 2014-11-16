package com.neoba.syncpad;


import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import org.xml.sax.Attributes;

class NeoHTML extends DefaultHandler {

	SpannableStringBuilder s;
	String html;
	Context context;
	Note note;
	
	
	
	public Note getNote() {
		return note;
	}

	public NeoHTML(String html,Context context) {
		s = new SpannableStringBuilder("");
		this.html = html;
		this.context=context;
		 generateNote();
	}
	
	public void generateNote(){
		String content="",colorcode=html.split("\n")[0];
		for(int i=1;i<html.split("\n").length;i++)
			content+=html.split("\n")[i];
		Spanned x=Html.fromHtml(content);
		SpannableStringBuilder ss=new SpannableStringBuilder(x);
		note=new Note(ss,colorcode);
	}

	public SpannableStringBuilder getSpannable() {
		return s;
	}

	public void setS(SpannableStringBuilder s) {
		this.s = s;
	}

	public void getXml() {
		try {

			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
			DefaultHandler defaultHandler = new DefaultHandler() {

				int boldTag = 0;
				int italicsTag = 0;
				int underlineTag = 0;
				int pointer = 0;
				boolean brpassed=false;
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {

					if (qName.equalsIgnoreCase("B")) {
						boldTag += 1;
					}
					if (qName.equalsIgnoreCase("I")) {
						italicsTag += 1;
					}
					if (qName.equalsIgnoreCase("U")) {
						underlineTag += 1;
					}
				}

				public void characters(char ch[], int start, int length)
						throws SAXException {
					int tstart=start;
					

						if(new String(ch, start, length).startsWith("[ ]")){
							s.append("b");
							Typeface font = Typeface.createFromAsset(context.getAssets(), "tickfont.ttf");
							s.setSpan (new CustomTypefaceSpan("", font),pointer, pointer+1,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
							pointer+=1;
							tstart+=3;
						}
						
						else if(new String(ch, start, length).startsWith("[*]")){
							s.append("a");
							Typeface font = Typeface.createFromAsset(context.getAssets(), "tickfont.ttf");
							s.setSpan (new CustomTypefaceSpan("", font),pointer, pointer+1,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
							pointer+=1;
							tstart+=3;
						}


					
					if (boldTag > 0 || italicsTag > 0 || underlineTag > 0) {
						s.append(new String(ch, tstart, length));
						if (boldTag > 0) {
							
							s.setSpan(new StyleSpan(
									android.graphics.Typeface.BOLD), pointer,
									pointer + length,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

						}
						if (italicsTag > 0) {

							s.setSpan(new StyleSpan(
									android.graphics.Typeface.ITALIC), pointer,
									pointer + length,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

						}
						if (underlineTag > 0) {

							s.setSpan(new UnderlineSpan(), pointer, pointer
									+ length,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}

					} else {
						s.append(new String(ch, tstart, length));
					}

					pointer += length;
				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {

					if (qName.equalsIgnoreCase("B")) {
						boldTag -= 1;
					}
					if (qName.equalsIgnoreCase("BR")) {
						s.append("\n");
						pointer+=1;
						brpassed=true;
					}
					if (qName.equalsIgnoreCase("I")) {
						italicsTag -= 1;
					}
					if (qName.equalsIgnoreCase("U")) {
						underlineTag -= 1;
					}
				}
			};
			saxParser.parse(new InputSource(new StringReader(html)),
					defaultHandler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
