/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Atul Vinayak
 */
class DocumentDeleteMessage {

    private boolean candelete = false;
    private boolean creatordeleted = false;
    private boolean push_success = false;
    private boolean doc_exists = false;
    Logger logger = Logger.getLogger(DocumentEditMessage.class);

    public DocumentDeleteMessage(UUID doc, UUID sessid) throws JSONException, Exception {
        JSONObject json = new JSONObject((String) Dsyncserver.cclient.get(doc.toString()));
        if (json != null) {
            doc_exists = true;

        }
        JSONArray readers = json.getJSONArray("permission_read");
        JSONArray editors = json.getJSONArray("permission_edit");
        String user = (String) Dsyncserver.usersessions.get(sessid);
        JSONObject userobject = new JSONObject((String) Dsyncserver.cclient.get(user));
        String username = (String) userobject.get("username");
        JSONArray docs_all = userobject.getJSONArray("docs");
        JSONArray docs_edit = userobject.getJSONArray("edit_docs");

        for (int i = 0; i < readers.length(); i++) {
            if (user.equals((String) readers.get(i))) {
                candelete = true;
                creatordeleted = false;
            }
        }

        if (((String) json.get("creator")).equals(user)) {
            candelete = true;
            creatordeleted = true;
            logger.info(sessid + " :deleted by the creator itself");
        }

        ArrayList<String> readlist_android = new ArrayList<String>();
        if (doc_exists && candelete) {
            if (creatordeleted) {

                for (int i = 0; i < readers.length(); i++) {

                    JSONObject reader = new JSONObject((String) Dsyncserver.cclient.get(readers.getString(i)));
                    JSONArray docs = reader.getJSONArray("docs");
                    JSONArray edocs = reader.getJSONArray("edit_docs");
                    JSONArray new_docs = new JSONArray();
                    JSONArray new_edocs = new JSONArray();
                    for (int j = 0; j < docs.length(); j++) {
                        if (!docs.get(j).equals(doc.toString())) {
                            new_docs.put(docs.get(j));
                        }
                    }
                    for (int j = 0; j < edocs.length(); j++) {
                        if (!edocs.get(j).equals(doc.toString())) {
                            new_edocs.put(edocs.get(j));
                        }
                    }

                    reader.put("docs", new_docs);
                    reader.put("edit_docs", new_edocs);

                    Dsyncserver.cclient.replace(readers.getString(i), reader.toString());

                    for (String rid : CouchManager.get_gcm_rids(readers.getString(i))) {
                        readlist_android.add(rid);
                    }
                }

                JSONObject owner = new JSONObject((String) Dsyncserver.cclient.get(user));
                JSONArray docs = owner.getJSONArray("docs");
                JSONArray edocs = owner.getJSONArray("edit_docs");
                JSONArray new_docs = new JSONArray();
                JSONArray new_edocs = new JSONArray();
                for (int j = 0; j < docs.length(); j++) {
                    if (!docs.get(j).equals(doc.toString())) {
                        new_docs.put(docs.get(j));
                    }
                }
                for (int j = 0; j < edocs.length(); j++) {
                    if (!edocs.get(j).equals(doc.toString())) {
                        new_edocs.put(edocs.get(j));
                    }
                }

                owner.put("docs", new_docs);
                owner.put("edit_docs", new_edocs);

                Dsyncserver.cclient.replace(user, owner.toString());

                logger.debug(sessid + " GCM ids: " + readlist_android);
                JSONObject action = new JSONObject();
                action.put("type", "delete");
                action.put("id", doc.toString());
                if (!readlist_android.isEmpty()) {
                    logger.info(sessid + " pushing to android devices..");
                    if (GoogleCloudMessager.Push(sessid, readlist_android, json.getString("title"), "This document was deleted by the creator", action)) {
                        push_success = true;
                        Dsyncserver.cclient.delete(doc.toString());
                    }
                } else {
                    push_success = true;
                    Dsyncserver.cclient.delete(doc.toString());
                }

            } else {

                JSONArray new_readers = new JSONArray();
                JSONArray new_editors = new JSONArray();
                JSONArray new_docs_all = new JSONArray();
                JSONArray new_docs_edit = new JSONArray();

                for (int i = 0; i < readers.length(); i++) {
                    if (!readers.get(i).equals(user)) {
                        new_readers.put(readers.get(i));
                    }
                }
                for (int i = 0; i < editors.length(); i++) {
                    if (!editors.get(i).equals(user)) {
                        new_editors.put(editors.get(i));
                    }
                }

                for (int i = 0; i < docs_all.length(); i++) {
                    if (!docs_all.get(i).equals(doc.toString())) {
                        new_docs_all.put(docs_all.get(i));
                    }
                }
                for (int i = 0; i < docs_edit.length(); i++) {
                    if (!docs_edit.get(i).equals(doc.toString())) {
                        new_docs_edit.put(docs_edit.get(i));
                    }
                }

                for (String rid : CouchManager.get_gcm_rids((String) json.get("creator"))) {
                    readlist_android.add(rid);
                }
                logger.debug(sessid + " GCM ids of creator : " + readlist_android);
                JSONObject action = new JSONObject();
                action.put("type", "user_deleted");
                action.put("docid", doc.toString());
                action.put("userid", user);
                if (!readlist_android.isEmpty()) {
                    logger.info(sessid + " pushing to android devices..");

                    if (GoogleCloudMessager.Push(sessid, readlist_android, json.getString("title"), username + " removed his permissions for this note", action)) {
                        push_success = true;
                        json.put("permission_read", new_readers);
                        json.put("permission_edit", new_editors);
                        Dsyncserver.cclient.replace(doc.toString(), json.toString());
                        userobject.put("docs", new_docs_all);
                        userobject.put("edit_docs", new_docs_all);
                        Dsyncserver.cclient.replace(user, userobject.toString());

                    }
                } else {
                    push_success = true;
                    json.put("permission_read", new_readers);
                    json.put("permission_edit", new_editors);
                    Dsyncserver.cclient.replace(doc.toString(), json.toString());
                    userobject.put("docs", new_docs_all);
                    userobject.put("edit_docs", new_docs_all);
                    Dsyncserver.cclient.replace(user, userobject.toString());
                }

            }
        }
    }

    ByteBuf result() {
        ByteBuf reply = buffer(2 + 4);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.DOCUMENT_EDIT);

        if (!candelete) {
            reply.writeInt(Constants.W_ERR_UNPRIVILAGED_USER);
        } else if (!doc_exists) {
            reply.writeInt(Constants.W_ERR_DOCUMENT_NONEXISTANT);
        } else if (!push_success) {
            reply.writeInt(Constants.W_ERR_PUSH_FAILED);
        } else {
            reply.writeInt(Constants.W_SUCCESS);
        }
        return reply;
    }

}
