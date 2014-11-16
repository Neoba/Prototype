package com.neoba.syncpad;

import java.util.HashSet;

public class ShareSchema {
	String docid;
	HashSet<Long> read;
	HashSet<Long> edit;
	public ShareSchema(String id) {
		this.docid=id;
		read=new HashSet<Long>();
		edit=new HashSet<Long>();
	}
	public void addread(Long id){
		read.add(id);
	}
	
	public void addedit(Long id){
		edit.add(id);
	}
	
	public boolean isedit(Long id){
		return read.contains(id) && edit.contains(id);
	}
}