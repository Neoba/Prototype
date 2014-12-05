/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba.messages;

import com.neoba.utils.Base64;
import com.neoba.Constants;
import com.neoba.CouchManager;
import com.neoba.Dsyncserver;
import com.neoba.GoogleCloudMessager;
import com.neoba.messages.Message;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
public class GrantPermissionMessage implements Message {

    private boolean unfollowed_user = false;
    private boolean unprivilaged = false;
    private boolean push_successn = false;
    private boolean push_successr = false;
    private boolean push_successw = false;
    Logger logger = Logger.getLogger(GrantPermissionMessage.class);

    public GrantPermissionMessage(UUID doc, HashMap<String, Byte> permissions, UUID session) throws JSONException, Exception {
        String userid = (String) Dsyncserver.usersessions.get(session);
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));
        TreeSet<String> oldreadset = new TreeSet();
        TreeSet<String> oldeditset = new TreeSet();
        JSONArray followers = (JSONArray) (user.get("followers"));

        if (followers.length() < permissions.size()) {
            unfollowed_user = true;
            logger.error(session + " : trying to grant permission to unfollowed users");
            logger.error(session + " : eliminated in size checks");
        }

        if (!unfollowed_user) {
            Boolean f;
            for (String p : permissions.keySet()) {
                f = false;
                for (int i = 0; i < followers.length(); i++) {
                    if (p.equals(((JSONObject) followers.get(i)).get("id"))) {
                        f = true;
                        break;
                    }
                }
                if (!f) {
                    unfollowed_user = true;
                    logger.error(session + " : trying to grant permission to unfollowed users");
                    logger.error(session + " : eliminated in extended checks");
                    break;
                }
            }

        }
        if (!unfollowed_user) {

            JSONObject document = new JSONObject((String) Dsyncserver.cclient.get(doc.toString()));
            if (!((String) document.get("creator")).equals(userid)) {
                logger.error(session + " : unprivilaged to grant permissions");
                this.unprivilaged = true;
                return;
            }

            TreeSet<String> readset = new TreeSet<String>();
            JSONArray readarray = (JSONArray) (document.get("permission_read"));
            for (int i = 0; i < readarray.length(); i++) {
                readset.add(readarray.getString(i));
                oldreadset.add(readarray.getString(i));
            }

            TreeSet<String> editset = new TreeSet<String>();
            JSONArray editarray = (JSONArray) (document.get("permission_edit"));
            for (int i = 0; i < editarray.length(); i++) {
                editset.add(editarray.getString(i));
                oldeditset.add(editarray.getString(i));
            }
            logger.debug(session + " :readset before: " + readset);
            logger.debug(session + " :editset before: " + editset);
            for (String s : readset) {
                if (!permissions.keySet().contains(s)) {
                    permissions.put(s, Constants.PERMISSION_NONE);
                }
            }

            for (String id : permissions.keySet()) {
                JSONObject fojson = new JSONObject((String) Dsyncserver.cclient.get(id));
                TreeSet<String> docsset = new TreeSet<String>();
                JSONArray docsarray = (JSONArray) (fojson.get("docs"));
                for (int i = 0; i < docsarray.length(); i++) {
                    docsset.add(docsarray.getString(i));
                }

                TreeSet<String> editdocsset = new TreeSet<String>();
                JSONArray editdocsarray = (JSONArray) (fojson.get("edit_docs"));
                for (int i = 0; i < editdocsarray.length(); i++) {
                    editdocsset.add(editdocsarray.getString(i));
                }
                readset.remove(id);
                editset.remove(id);
                docsset.remove(doc.toString());
                editdocsset.remove(doc.toString());
                switch (permissions.get(id)) {
                    case Constants.PERMISSION_NONE:
                        readset.remove(id);
                        editset.remove(id);
                        editdocsset.remove(doc.toString());
                        docsset.remove(doc.toString());
                        break;
                    case Constants.PERMISSION_READ:
                        readset.add(id);
                        editset.remove(id);
                        editdocsset.remove(doc.toString());
                        docsset.add(doc.toString());
                        break;
                    case Constants.PERMISSION_EDIT:
                        readset.add(id);
                        editset.add(id);
                        editdocsset.add(doc.toString());
                        docsset.add(doc.toString());
                        break;
                }
                logger.debug(session + " :processed[" + id + "]; readset after: " + readset);
                logger.debug(session + " :processed[" + id + "]; editset after: " + editset);
                docsarray = new JSONArray();
                for (String s : docsset) {
                    docsarray.put(s);
                }
                editdocsarray = new JSONArray();
                for (String s : editdocsset) {
                    editdocsarray.put(s);
                }
                fojson.put("docs", docsarray);
                fojson.put("edit_docs", editdocsarray);

                Dsyncserver.cclient.replace(id, fojson.toString());
            }
            readarray = new JSONArray();
            for (String s : readset) {
                readarray.put(s);
            }
            editarray = new JSONArray();
            for (String s : editset) {
                editarray.put(s);
            }

            for (String s : readset) {
                oldreadset.remove(s);
            }
            for (String s : editset) {
                oldeditset.remove(s);
            }
            if (oldeditset.size() > 0 || oldreadset.size() > 0) {
                TreeSet<String> readpush_android = new TreeSet<String>();
                logger.info(session + " : permission revoked- " + oldeditset);
                for (String s : oldreadset) {
                    for (String rid : CouchManager.get_gcm_rids(s)) {
                        readpush_android.add(rid);
                    }
                }

                JSONObject action = new JSONObject();
                action.put("type", "permission_revoke");
                action.put("id", doc.toString());
                if (readpush_android.size() > 0) {
                    if (GoogleCloudMessager.Push(session, readpush_android, document.getString("title"), user.getString("username") + " revoked access to " + document.getString("title"), action)) {
                        push_successn = true;
                    }
                } else {
                    push_successn = true;
                }
            } else {
                push_successn = true;
            }
            if (push_successn && (editarray.length() > 0 || readarray.length() > 0)) {
                TreeSet<String> readpush_android = new TreeSet<String>();
                TreeSet<String> writepush_android = new TreeSet<String>();
                for (String s : readset) {
                    if (!editset.contains(s)) {
                        for (String rid : CouchManager.get_gcm_rids(s)) {
                            readpush_android.add(rid);
                        }

                    }
                }
                for (String s : editset) {
                    for (String rid : CouchManager.get_gcm_rids(s)) {
                        writepush_android.add(rid);
                    }
                }
                JSONObject action = new JSONObject();
                action.put("type", "permission_grant");
                action.put("dict", document.getString("dict"));
                JSONArray diffa = new JSONArray(document.getString("diff"));
                byte[] diff = new byte[diffa.length()];
                for (int i = 0; i < diffa.length(); i++) {
                    diff[i] = (byte) diffa.getInt(i);
                }
                action.put("diff", Base64.encodeToString(diff, true));
                action.put("age", (Integer) document.get("version"));
                action.put("id", doc.toString());
                action.put("title", document.getString("title"));
                action.put("owner", user.getString("username")+"~"+user.getString("facebook_id"));
                if (writepush_android.size() > 0) {
                    action.put("permission", Constants.PERMISSION_EDIT);
                    if (GoogleCloudMessager.Push(session, writepush_android, document.getString("title"), user.getString("username") + " shared a new document", action)) {
                        push_successw = true;
                    }
                } else {
                    push_successw = true;
                }
                if (readpush_android.size() > 0) {
                    action.put("permission", Constants.PERMISSION_READ);
                    if (GoogleCloudMessager.Push(session, readpush_android, document.getString("title"), user.getString("username") + " shared a new document (read-only)", action)) {
                        push_successr = true;
                    }
                } else {
                    push_successr = true;
                }
            } else {
                push_successr = push_successw = true;
            }
            document.put("permission_read", readarray);
            document.put("permission_edit", editarray);
            logger.info(session + " : updated reads permissions- " + readarray);
            logger.info(session + " : updated edit permissions- " + editarray);
            Dsyncserver.cclient.replace(doc.toString(), document.toString());

        }
    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(6);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.GRANT_PERMISSION);
        if (unfollowed_user) {
            reply.writeInt(Constants.W_ERR_SHARE_WITH_UNFOLLOWED_USER);
        } else if (unprivilaged) {
            reply.writeInt(Constants.W_ERR_UNPRIVILAGED_USER);
        } else if (!push_successn || !push_successw || !push_successr) {
            reply.writeInt(Constants.W_ERR_PUSH_FAILED);
        } else {
            reply.writeInt(Constants.W_SUCCESS);
        }
        return reply;
    }

}
