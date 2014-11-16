/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import java.util.HashMap;

/**
 *
 * @author atul
 */
public class CouchViews {

    HashMap<String, String> views;

    public HashMap<String, String> getViews() {
        return views;
    }

    public void setViews(HashMap<String, String> view) {
        this.views = view;
    }

    public CouchViews() {
        views=new HashMap<String,String>();
        views.put("created_docs", "function (doc, meta) {\n"
                + "  if(doc.type==\"document\")\n"
                + "  	emit(doc.creator, meta.id);\n"
                + "}");

        views.put("idsview", "function (doc, meta) {\n"
                + "  if(doc.type==\"document\")\n"
                + "  	emit(meta.id,doc, null);\n"
                + "}");

        views.put("usernamelistview", "function (doc, meta) {\n"
                + "  if(doc.type==\"user\")\n"
                + "  	emit(doc.username, null);\n"
                + "}");
        
        views.put("userstoid", "function (doc, meta) {\n"
                + "  if(doc.type==\"user\")\n"
                + "  	emit(doc.username, meta.id);\n"
                + "}");
        views.put("usernametofacebookid", "function (doc, meta) {\n"
                + "  if(doc.type==\"user\")\n"
                + "  {\n"
                + "    emit(doc.username,doc.facebook_id);\n"
                + "  }\n"
                + "  \n"
                + "}");
        
        views.put("facebookidtoid", "function (doc, meta) {\n"
                + "  if(doc.type==\"user\")\n"
                + "  {\n"
                + "    emit(doc.facebook_id,{\"id\":meta.id,\"username\":doc.username});\n"
                + "  }\n"
                + "  \n"
                + "}");

    }

}
